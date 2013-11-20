package com.ccsu.sphero.proj;

import java.util.List;

import com.ccsu.sphero.proj.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.robot.sensor.AttitudeData;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.robot.sensor.LocatorData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

/**
 * Activity for controlling the Sphero with five control buttons.
 */
public class SpheroController extends Activity
{
    /**
     * Robot to control
     */
    private Robot mRobot;
    float speed = 0.0f;
    float heading = 0.0f;
    boolean ledOn = false;
    private Button ledToggle;
    private Button tmpButton;
    private Button powerButton;
    private Button rollButton;
    private Button arcButton;
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
    /**
     * The Sphero Connection View
     */
    private SpheroConnectionView mSpheroConnectionView;
    //The views that will show the streaming data
    private LocatorData lastLocation; 
    private Handler mHandler = new Handler();

    /**
     * AsyncDataListener that will be assigned to the DeviceMessager, listen for streaming data, and then do the
     */
    private DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) 
        {

            if(data instanceof DeviceSensorsAsyncData)
            {

                //get the frames in the response
                List<DeviceSensorsData> data_list = ((DeviceSensorsAsyncData)data).getAsyncData();
                if(data_list != null)
                {
                	 
                    for(DeviceSensorsData datum : data_list)
                    {
                    	AttitudeData attitude = datum.getAttitudeData();
                        if(attitude != null){
                            yaw = attitude.getAttitudeSensor().yaw;
                        }
                    	
                    	
                        LocatorData locatorData = datum.getLocatorData();
                        if( locatorData != null ) 
                        {
                            lastLocation = locatorData;
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
                    }
                }
            }
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ledToggle = (Button)findViewById(R.id.btnLEDToggle);
        ledToggle.setOnClickListener(ledToggleListener);
        tmpButton = (Button)findViewById(R.id.tmpButton);
        tmpButton.setOnClickListener(tmpListener);
        powerButton = (Button)findViewById(R.id.btnPowerOff);
        powerButton.setOnClickListener(powerOffListener);
        rollButton = (Button)findViewById(R.id.btnRoll);
        rollButton.setOnClickListener(rollListener);
        arcButton = (Button)findViewById(R.id.Arc);
        arcButton.setOnClickListener(arcListener);
        mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
        // Set the connection event listener 
        mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
        	// If the user clicked a Sphero and it failed to connect, this event will be fired
			@Override
			public void onRobotConnectionFailed(Robot robot) {}
			// If there are no Spheros paired to this device, this event will be fired
			@Override
			public void onNonePaired() {}
			// The user clicked a Sphero and it successfully paired.
			@Override
			public void onRobotConnected(Robot robot) {
				mRobot = robot;
				// Skip this next step if you want the user to be able to connect multiple Spheros
				mSpheroConnectionView.setVisibility(View.GONE);
				mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 0);
                    	BackLEDOutputCommand.sendCommand(mRobot, 0.0f);
                    	ToggleBackLED();
                        DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
                        requestDataStreaming();
                    }
                }, 1000);
			}
			@Override
			public void onBluetoothNotEnabled() {
				// See UISample Sample on how to show BT settings screen, for now just notify user
				Toast.makeText(SpheroController.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
    }
    
    private OnClickListener ledToggleListener = new OnClickListener(){

		@Override
		public void onClick(View btn) {
			ToggleBackLED();
		}
    };
    
    
    private OnClickListener tmpListener = new OnClickListener(){

		@Override
		public void onClick(View btn) {
			Turn(45);
		}
    };
    
    private OnClickListener powerOffListener = new OnClickListener(){

		@Override
		public void onClick(View btn) {
			SleepCommand.sendCommand(mRobot, 0, 0);
		}
    };
    
    private OnClickListener rollListener = new OnClickListener(){

		@Override
		public void onClick(View btn) {
			
			calibrate();
			mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                	Roll(.3f,  .9f);
                }
            }, 500);
			
		}
    };
    
    private OnClickListener arcListener = new OnClickListener(){

 		@Override
 		public void onClick(View btn) {
 			
 			calibrate();
 			mHandler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                 	Arc(.5f, 180, .3f);
                 }
             }, 500);
 			
 		}
     };
    
    
    /**
     * Called when the user comes back to this app
     */
    @Override
    protected void onResume() {
    	super.onResume();
        // Refresh list of Spheros
        mSpheroConnectionView.showSpheros();
    }
    
    /**
     * Called when the user presses the back or home button
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	// Disconnect Robot properly
    	DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mDataListener);
    	RobotProvider.getDefaultProvider().disconnectControlledRobots();
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
    	SetHeadingCommand.sendCommand(mRobot, angle);
    	//ConfigureLocatorCommand.sendCommand(mRobot, 0, 0, 0, yaw);
    }
    
    public void ToggleBackLED()
    {
    	if(!ledOn)
    	{
    		StabilizationCommand.sendCommand(mRobot, false);
    		BackLEDOutputCommand.sendCommand(mRobot, 1.0f);
    		ledOn = true;
    		ledToggle.setText(R.string.toggleOff);
    	}
    	else
    	{
    		calibrate();
    		StabilizationCommand.sendCommand(mRobot, true);
    		BackLEDOutputCommand.sendCommand(mRobot, 1.0f);
    		ledOn = false;
    		ledToggle.setText(R.string.toggleOn);
    	}
    }
    
    private void calibrate()
    {
    	ConfigureLocatorCommand.sendCommand(mRobot, 0, 0, 0, yaw);
    	SetHeadingCommand.sendCommand(mRobot, 0);
    }
    
   
    
    private void Arc(float radius, int angle, float speed)
    {	
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
    
    private void requestDataStreaming() {

        if(mRobot != null){

            // Set up a bitmask containing the sensor information we want to stream
            final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_FILTERED_ALL |
                    SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL | SetDataStreamingCommand.DATA_STREAMING_MASK_LOCATOR_ALL;

            // Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
            final int divisor = 40;

            // Specify the number of frames that will be in each response. You can use a higher number to "save up" responses
            // and send them at once with a lower frequency, but more packets per response.
            final int packet_frames = 1;

            // Count is the number of async data packets Sphero will send you before
            // it stops. Make response_count 0 for infinite streaming.
            final int response_count = 0;

            //Send this command to Sphero to start streaming
            SetDataStreamingCommand.sendCommand(mRobot, divisor, packet_frames, mask, response_count);
        }
    }
}
