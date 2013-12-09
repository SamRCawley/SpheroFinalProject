package com.ccsu.sphero.proj;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import orbotix.robot.base.*;

import orbotix.robot.sensor.AccelerometerData;
import orbotix.robot.sensor.LocatorData;

public class SpheroController
{
    /**
     * Robot to control
     */
    private Robot mRobot;
    float speed = 0.0f;
    float heading = 0.0f;
    boolean ledOn = false;
    int yaw = 0;
    double distanceTraveled = 0;
    float distanceTarget = 0;
	int turnsForArc = 0;
	int turnsComplete = 0;
	int anglePart = 0;
	String operation = null;
	boolean isReady = true;
	int minArcLength = 10;
	float partLength = 0;
	ArrayList<String[]> commandList = new ArrayList<String[]>();
	SharedPreferences mySharedPrefs;
	private String saveKey = "savedScript";
	private String prefsName = "SpheroProject";
	boolean killCommand = false;
	double prevDistanceTraveled = 0;
	int idleCycles = 0;
	Long timeIdle;
	boolean isRunning = false;
	Context context;
    /**
     * The Sphero Connection View
     */
    private LocatorData lastLocation; 
    private Handler mHandler = new Handler();

    public SpheroController() {}
    
    public void setRobot(Robot mRobot)
    {
    	this.mRobot = mRobot;
    }
    public void updateYaw(int yaw)
    {
    	this.yaw = yaw;
    }
    
    public void runCommands()
    {
    	//Log.v("debug", "isReady = " + isReady + " distanceTraveled = " + distanceTraveled);
    	
    	String nextCommand = commandList.get(0)[0];
    	if(nextCommand.equalsIgnoreCase("color"))
    	{
    		Log.v("Command", "color");
    		color(Integer.parseInt(commandList.get(0)[1]), Integer.parseInt(commandList.get(0)[2]), Integer.parseInt(commandList.get(0)[3]));
    		commandList.remove(0);
    	}
    	else if (isReady && distanceTraveled>1)
    	{
    		calibrate();
    		Log.v("CALIBRATING", "Spero is ready and trying to calibrate with location x: " + lastLocation.getPositionX() + " y: " + lastLocation.getPositionY());
    	}
    	else if(isReady && distanceTraveled<=1)
    	{
    		if(nextCommand.equalsIgnoreCase("arc"))
    		{
    			Log.v("Command", "Arc Begin");
    			calibrate(); //calibrate for "straight" direction if previous command was turn
    			final float radius = Float.parseFloat(commandList.get(0)[1]);
    			final int angle = Integer.parseInt(commandList.get(0)[2]);
    			final float speed = Float.parseFloat(commandList.get(0)[3]);
    			isReady = false;
    			mHandler.postDelayed(new Runnable() {  //delay for calibration
    	            @Override
    	            public void run() {
    	            	Arc(radius, angle, speed);
    	            }
    	        }, 1000);
    			commandList.remove(0);
    		}
    		else if(nextCommand.equalsIgnoreCase("roll"))
    		{
    			Log.v("Command", "Roll begin");
    			calibrate(); //calibrate for "straight" direction if previous command was turn
    			final float distance = Float.parseFloat(commandList.get(0)[1]);
    			final float speed = Float.parseFloat(commandList.get(0)[2]);
    			isReady = false;
    			mHandler.postDelayed(new Runnable() {  //delay for calibration
    	            @Override
    	            public void run() {
    	            	Roll(distance, speed);
    	            }
    	        }, 1000);
    			commandList.remove(0);
    		}
    		else if(nextCommand.equalsIgnoreCase("turn"))
    		{
    			Log.v("Command", "Turn begin");
    			calibrate();
    			Turn(Integer.parseInt(commandList.get(0)[1]));
    			commandList.remove(0);
    		}
    	}
    	if(commandList.size()>0 && !killCommand)
    	{
	    	mHandler.postDelayed(new Runnable() {  //delay and loop
	            @Override
	            public void run() {
	            	runCommands();
	            }
	        }, 50);
    	}
    	else //Script end cleanup
    	{
    		if(commandList.size()==0 && isReady)
    		{
    			isRunning = false;
	    		Toast toast = Toast.makeText(context, "Script Completed", Toast.LENGTH_SHORT);
				toast.show();
    		}
    		else if(killCommand)
    		{
    			isRunning = false;
        		isReady = true;
    			Toast toast = Toast.makeText(context, "Script Terminated", Toast.LENGTH_SHORT);
				toast.show();
    		}
    	}
    }
    
    public void updateLastLocation(LocatorData location)
    {
        lastLocation = location;
        if(isRunning)
        {
        	distanceLogic();
        }
    }
    
    private void distanceLogic()
    {
        distanceTraveled = Math.sqrt(lastLocation.getPositionX()*lastLocation.getPositionX()
        		+lastLocation.getPositionY()*lastLocation.getPositionY());
        if(Math.abs(distanceTraveled - prevDistanceTraveled) < 1)
        {
        	if((System.currentTimeMillis() - timeIdle) > 7000)  //If hasn't moved more than 1cm in 10 seconds then stop and do next command
        	{
        		RollCommand.sendStop(mRobot);
            	Log.v("Idle", "Robot was idle, moving to next command");
            	distanceTarget = 0;
            	distanceTraveled = 0;
            	isReady = true;
        	}
        }
        else
        {
        	prevDistanceTraveled = distanceTraveled;
        	timeIdle = System.currentTimeMillis();
        }
        //Log.v("Position", "X="+lastLocation.getPositionX()+"    Y="+lastLocation.getPositionY());
        //Log.v("Distance", "Distance Traveled = "+distanceTraveled + " Target = " + distanceTarget);
        float distanceRemaining = (float) (distanceTarget-distanceTraveled);
                
        if(distanceTarget > 1 &&  distanceRemaining < 100)
        {
        	if(speed > distanceRemaining/100)							//at 100 cm to destination begin slowing down
        	{
        		Log.v("Speed", "Reducing speed because distanceRemaining: " + distanceRemaining);
        		float reducedSpeed = distanceRemaining/100;
        		if(reducedSpeed > .2)									   //limit reduced speed to 20% (adjust if necessary)
        			RollCommand.sendCommand(mRobot, 0, reducedSpeed);
        	}
        }
        int distanceTolerance = 2;										//2cm tolerance for being at final location
        if(distanceTarget > 1 && distanceTraveled >= distanceTarget-distanceTolerance)  //simple logic to stop when distance reached
        {																//@todo add additional tolerance logic for "good enough" condition
        	RollCommand.sendStop(mRobot);
        	Log.v("Stop", "Stop was called, roll complete");
        	distanceTarget = 0;
        	distanceTraveled = 0;
        	mHandler.postDelayed(new Runnable() {  //delay and loop
                @Override
                public void run() {
                	isReady = true;
                }
            }, 1000);
        }
        
        if(operation !=null && operation.equalsIgnoreCase("arc"))  //Turning logic for arc
        {
          
    		if(distanceTraveled-(partLength*(turnsComplete-1)) >= partLength && turnsForArc>turnsComplete)
    		{
    			turnsComplete++;
    			Turn(anglePart);
    			if(turnsComplete==turnsForArc)
    			{
    				operation = null;
    			}
    		}
        }
    }
    /**
     * 
     * @brief Sets the color of the Sphero using RGB values
     * 
     */
    public void color(int R, int G, int B)
    {
            RGBLEDOutputCommand.sendCommand(mRobot, R, G, B);   
    }

    /**
     * @brief Rolls the Sphero a set distance at a given speed
     * 
     */
    public void Roll(float distance, final float speed){
    	isReady=false;
    	this.speed = speed;
    	distance = distance * 100;
        RollCommand.sendCommand(mRobot, 0, this.speed);
        distanceTarget = distance;
    }
    
    /**
     * @brief Turns the Sphero in place by a given angle 0-360
     * 
     */
    public void Turn(int angle)
    {
    	final boolean oldStatus = isReady;
    	isReady = false;
    	//int adjustment = 7; //adjustment for angle loss occurring
    	//if(angle>0)
    	//	angle+= adjustment;
    	//if(angle<0)
    	//	angle-= adjustment;
    	SetHeadingCommand.sendCommand(mRobot, angle);
    	mHandler.postDelayed(new Runnable() {  //delay and loop
            @Override
            public void run() {
            	isReady = oldStatus;
            	Log.v("command", "Turn returning status to = " + isReady);
            }
        }, 1000);
    	//ConfigureLocatorCommand.sendCommand(mRobot, 0, 0, 0, yaw);
    }
    
    
    public void calibrate()
    {
    	ConfigureLocatorCommand.sendCommand(mRobot, 0, 0, 0, yaw);
    	SetHeadingCommand.sendCommand(mRobot, 0);
    }
    
    public void Arc(float radius, int angle, float speed)
    {	
    	if(speed>0.5)
    		speed = 0.3f; //speeds above 0.3 are inaccurate
    	isReady = false;
    	calibrate();
    	partLength = minArcLength;
    	float length = (float) (angle*Math.PI*radius/180);
    	turnsForArc = (int) Math.ceil((length*100)/minArcLength);  //will result in 1 turn if length > minArcLength
    	int frictionLoss = 3;
    	anglePart = (int)Math.ceil(angle*1.0/turnsForArc) + frictionLoss;
    	Log.v("ANGLE of ARC", " "+anglePart);
    	Log.v("Turns for Arc", " "+turnsForArc);
    	operation = "arc";
    	
    	if(minArcLength > length*100)  //condition for turns less than 10cm
    	{
    		partLength = length*100;
    	}
    	Turn(anglePart);   //complete first sub-turn
    	turnsComplete = 1;
    	final float tempLength = length;  //final vars for runnable
    	final float tempSpeed = speed;
    	mHandler.postDelayed(new Runnable() {  //begin roll of for full length distance
            @Override
            public void run() {
            	Roll(tempLength, tempSpeed);
            }
        }, 500);
    	   	
    }
    
    public void runScript(String script, TextView tvErrors, Robot mRobot, Context context)
    {
    	this.context = context;
    	this.mRobot = mRobot;
    	tvErrors.setText(" ");
    	try{
	    	//BufferedReader br = openfile(context);
    		InputStream is = new ByteArrayInputStream(script.getBytes());
    		BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    	Parser p = new Parser();
	    	p.parse(br);
	    	//is checked valid due to parser throwing errors
	    	//br = openfile(context);
	    	String command;
	    	is = new ByteArrayInputStream(script.getBytes());
    		br = new BufferedReader(new InputStreamReader(is));
	    	while((command = br.readLine()) != null){
	    		String commandParams[] = command.split(" ");
	    		commandList.add(commandParams);
	    	}
	    	killCommand = false;
	    	isRunning = true;
	    	timeIdle = System.currentTimeMillis();
	    	runCommands();
    	}
    	catch(Exception e){
    		tvErrors.setText(e.getMessage());
    	}
    	
    }
   
    public void saveScript(Context context, String script)
    {
    	mySharedPrefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
    	SharedPreferences.Editor out = mySharedPrefs.edit();
		out.putString(saveKey, script);
		out.commit();
    }
    
    public String loadScript(Context context)
    {
    	String savedScript = "";
    	mySharedPrefs = context.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
    	savedScript = mySharedPrefs.getString(saveKey, "error");
    	return savedScript;
    }
    
    public void sendKillCommand()
    {
    	RollCommand.sendStop(mRobot);
    	killCommand = true;
    	isRunning = false;
    }
}

