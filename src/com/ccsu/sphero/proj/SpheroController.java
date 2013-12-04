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

import orbotix.robot.base.*;

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
	int minArcLength = 20;
	float partLength = 0;
	ArrayList<String[]> commandList = new ArrayList<String[]>();
	SharedPreferences mySharedPrefs;
	private String saveKey = "savedScript";
	private String prefsName = "SpheroProject";
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
    	if(commandList.size()==0)
    		return;
    	String nextCommand = commandList.get(0)[0];
    	if(nextCommand.equalsIgnoreCase("color"))
    	{
    		color(Integer.parseInt(commandList.get(0)[1]), Integer.parseInt(commandList.get(0)[2]), Integer.parseInt(commandList.get(0)[3]));
    		commandList.remove(0);
    	}
    	else if(isReady)
    	{
    		if(nextCommand.equalsIgnoreCase("arc"))
    		{
    			calibrate();
    			Arc(Float.parseFloat(commandList.get(0)[1]), Integer.parseInt(commandList.get(0)[2]), Float.parseFloat(commandList.get(0)[3]));
    			commandList.remove(0);
    		}
    		else if(nextCommand.equalsIgnoreCase("roll"))
    		{
    			calibrate();
    			Roll(Float.parseFloat(commandList.get(0)[1]), Float.parseFloat(commandList.get(0)[2]));
    			commandList.remove(0);
    		}
    		else if(nextCommand.equalsIgnoreCase("turn"))
    		{
    			Turn(Integer.parseInt(commandList.get(0)[1]));
    			commandList.remove(0);
    		}
    	}
    	mHandler.postDelayed(new Runnable() {  //delay and loop
            @Override
            public void run() {
            	runCommands();
            }
        }, 50);
    }
    
    public void updateLastLocation(LocatorData location)
    {
        lastLocation = location;
        if(!isReady)
        	distanceLogic();

    }
    
    private void distanceLogic()
    {
        distanceTraveled = Math.sqrt(lastLocation.getPositionX()*lastLocation.getPositionX()
        		+lastLocation.getPositionY()*lastLocation.getPositionY());
        //Log.v("Position", "X="+lastLocation.getPositionX()+"    Y="+lastLocation.getPositionY());
        //Log.v("Distance", "Distance Traveled = "+distanceTraveled + " Target = " + distanceTarget);
        float distanceRemaining = (float) (distanceTarget-distanceTraveled);
        
        if(distanceTarget > 0 &&  distanceRemaining < 30)
        {
        	if(speed > distanceRemaining/30)							//at 30 cm to destination begin slowing down
        	{
        		float reducedSpeed = distanceRemaining/30;
        		if(reducedSpeed > .2)									   //limit reduced speed to 20% (adjust if necessary)
        			RollCommand.sendCommand(mRobot, 0, reducedSpeed);
        	}
        }
        int distanceTolerance = 2;										//2cm tolerance for being at final location
        if(distanceTarget > 0 && distanceTraveled >= distanceTarget-distanceTolerance)  //simple logic to stop when distance reached
        {																//@todo add additional tolerance logic for "good enough" condition
        	RollCommand.sendStop(mRobot);
        	Log.v("Stop", "Stop was called, roll complete");
        	distanceTarget = 0;
        	distanceTraveled = 0;
        	isReady = true;
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
    	int adjustment = 3; //adjustment for angle loss occurring
    	angle += adjustment;
    	SetHeadingCommand.sendCommand(mRobot, angle);
    	mHandler.postDelayed(new Runnable() {  //delay and loop
            @Override
            public void run() {
            	isReady = oldStatus;
            }
        }, 100);
    	//ConfigureLocatorCommand.sendCommand(mRobot, 0, 0, 0, yaw);
    }
    
    
    public void calibrate()
    {
    	ConfigureLocatorCommand.sendCommand(mRobot, 0, 0, 0, yaw);
    	SetHeadingCommand.sendCommand(mRobot, 0);
    }
    
    public void Arc(float radius, int angle, float speed)
    {	
    	isReady = false;
    	calibrate();
    	partLength = minArcLength;
    	float length = (float) (angle*Math.PI*radius/180);
    	turnsForArc = (int) ((length*100)/minArcLength)+1;  //will result in 1 turn if length > minArcLength
    	anglePart = angle/turnsForArc;
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
    
    public void runScript(String script, TextView tvErrors, Robot mRobot)
    {
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
	    	runCommands();
    	}
    	catch(Exception e){
    		tvErrors.setText(e.toString());
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
}

