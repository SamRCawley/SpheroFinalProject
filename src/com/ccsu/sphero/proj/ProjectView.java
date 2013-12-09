package com.ccsu.sphero.proj;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.ccsu.sphero.proj.R;
import com.ccsu.sphero.proj.SpheroController;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.robot.sensor.AccelerometerData;
import orbotix.robot.sensor.AttitudeData;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.robot.sensor.LocatorData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

/**
 * Activity for controlling the Sphero with five control buttons.
 */
public class ProjectView extends Activity
{
    /**
     * Robot to control
     */
	private SpheroController spheroController = new SpheroController();
    private Robot mRobot;
    boolean ledOn = false;
    public Button ledToggle;
    public Button scriptButton;
    public Button powerButton;
    public EditText etScript;
    public EditText etURI;
    public Button getURL;
    public TextView tvErrors;
    public Button btnSave;
    public Button btnLoad;
    public Button btnKillCommand;
    /**
     * The Sphero Connection View
     */
    private SpheroConnectionView mSpheroConnectionView;
    //The views that will show the streaming data
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
                        	spheroController.updateYaw(attitude.getAttitudeSensor().yaw);
                        }
                    	
                    	
                        LocatorData locatorData = datum.getLocatorData();
                        if( locatorData != null ) 
                        {
                        	spheroController.updateLastLocation(locatorData);
                            
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
        scriptButton = (Button)findViewById(R.id.btnRunScript);
        scriptButton.setOnClickListener(scriptListener);
        powerButton = (Button)findViewById(R.id.btnPowerOff);
        powerButton.setOnClickListener(powerOffListener);
        etURI = (EditText) findViewById(R.id.etURI);
        etScript = (EditText) findViewById(R.id.etScript);
        getURL = (Button) findViewById(R.id.btnGetURL);
        getURL.setOnClickListener(URLButtonListener);
        tvErrors = (TextView) findViewById(R.id.tvErrors);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(btnSaveListener);
        btnLoad = (Button) findViewById(R.id.btnLoad);
        btnLoad.setOnClickListener(btnLoadListener);
        btnKillCommand = (Button) findViewById(R.id.btnKillCommand);
        btnKillCommand.setOnClickListener(btnKillCommandListener);
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
				spheroController.setRobot(mRobot);
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
				Toast.makeText(ProjectView.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
    }
    
    private OnClickListener ledToggleListener = new OnClickListener(){

		@Override
		public void onClick(View btn) {
			ToggleBackLED();
		}
    };
    
    private OnClickListener URLButtonListener = new OnClickListener() {
    	public void onClick(View v) {
    		String strURL = etURI.getText().toString();
    		etScript.setText("");
    		try {
				URL url = new URL(strURL);
				
				URLConnection connection = url.openConnection();
				HttpURLConnection httpConnection = (HttpURLConnection)connection;
				int responseCode = httpConnection.getResponseCode();
				if(responseCode == HttpURLConnection.HTTP_OK){
					InputStream in = new BufferedInputStream(httpConnection.getInputStream());
					BufferedReader r = new BufferedReader(new InputStreamReader(in));
					String line;
					String webText = "";
					while((line = r.readLine())!=null)
					{
						webText += line + "\n";
					}
					etScript.setText(webText);
					in.close();
				}
				else
					etScript.setText("HTTP error " + httpConnection.getResponseCode());
			} catch (MalformedURLException e) {
				tvErrors.setText("<<<Malformed URL Exception>>>");
			} catch (IOException e) {
				tvErrors.setText("<<<IO Exception>>>");
			}
    		
    	}
    };
    
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
    		spheroController.calibrate();
    		StabilizationCommand.sendCommand(mRobot, true);
    		BackLEDOutputCommand.sendCommand(mRobot, 0f);
    		ledOn = false;
    		ledToggle.setText(R.string.toggleOn);
    	}
    }
    
    private OnClickListener scriptListener = new OnClickListener(){

		@Override
		public void onClick(View btn) {
			if(!ledOn)
			{
				spheroController.runScript(etScript.getText().toString().trim(), tvErrors, mRobot, getApplicationContext());
				Toast toast = Toast.makeText(getApplicationContext(), "Script Started", Toast.LENGTH_SHORT);
				toast.show();
				
			}
			else{
				Toast toast = Toast.makeText(getApplicationContext(), "Positioning must be off", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
    };
    
    private OnClickListener powerOffListener = new OnClickListener(){

		@Override
		public void onClick(View btn) {
			SleepCommand.sendCommand(mRobot, 0, 0);
		}
    };
   
    private OnClickListener btnSaveListener = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			spheroController.saveScript(getApplicationContext(), etScript.getText().toString());
			Toast toast = Toast.makeText(getApplicationContext(), "Script Saved", Toast.LENGTH_SHORT);
			toast.show();
		}
    	
    };
    
    private OnClickListener btnLoadListener = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			String script = spheroController.loadScript(getApplicationContext());
			etScript.setText(script);
		}
    	
    };
    
    private OnClickListener btnKillCommandListener = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			spheroController.sendKillCommand();
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
        spheroController = new SpheroController();
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
    	spheroController.sendKillCommand();
    	spheroController = null;
    }
    
    
    private void requestDataStreaming() {

        if(mRobot != null){

            // Set up a bitmask containing the sensor information we want to stream
            final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL | SetDataStreamingCommand.DATA_STREAMING_MASK_LOCATOR_ALL;

            // Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
            final int divisor = 10;

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
