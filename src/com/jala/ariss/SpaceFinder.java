package com.jala.ariss;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jala.ariss.Preview;

import com.jala.ariss.model.Coord;
import com.jala.ariss.model.SpaceObject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.Camera.CameraInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SpaceFinder extends Activity 
	implements SensorEventListener, LocationListener{
	
	private TextView _orientationTextView;
	private TextView _locationTextView;
	private TextView _issLocationTextView;	
	
	private SensorManager _sensorManager;
	private Sensor _rotVectSensor;
	private float[] _orientationVals = new float[3];
	private LocationManager _locationManager;
	private long _locationUptimeAtResume;
	
	private final float[] _rotationMatrix = new float[16];	
	
	private SpaceObject _iss;
	private Coord _userLocation;
	private boolean _obtainingLocation;
	
	Camera _camera;
    private int _defaultCameraId;
    int _numberOfCameras;
    int _cameraCurrentlyLocked;
    private Preview _preview;
    
    private DrawView _drawView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		//Set up camera preview
//		_camera = getCameraInstance(); 
		_preview = new Preview(this);
		_drawView = new DrawView(this, 0, 0);
		
		setContentView(R.layout.activity_arisscam);
		
		obtainDefaultCameraId();		
		
		// Set up layouts
		FrameLayout frameLayout = (FrameLayout)findViewById(R.id.cameraPreview);
		frameLayout.addView(_preview);
		frameLayout.addView(_drawView);
				
		RelativeLayout orientationRelativeLayout = (RelativeLayout)findViewById(R.id.orientationTextLayout);
		orientationRelativeLayout.bringToFront();
		
		RelativeLayout locationRelativeLayout = (RelativeLayout)findViewById(R.id.locationTextLayout);
		locationRelativeLayout.bringToFront();
		
		RelativeLayout issLocationRelativeLayout = (RelativeLayout)findViewById(R.id.issLocationTextLayout);
		issLocationRelativeLayout.bringToFront();
		
		_orientationTextView = (TextView)findViewById(R.id.orientationTextView);		
		MarginLayoutParams params=(MarginLayoutParams )_orientationTextView.getLayoutParams();
		params.leftMargin=10;		 
		params.topMargin=10;
		_orientationTextView.setLayoutParams(params);
		_orientationTextView.setTextColor(Color.GREEN);
		
		_locationTextView = (TextView)findViewById(R.id.locationTextView);
		params=(MarginLayoutParams )_locationTextView.getLayoutParams();
		params.leftMargin=10;		 
		params.topMargin=90;
		_locationTextView.setLayoutParams(params);
		_locationTextView.setTextColor(Color.GREEN);
		
		_issLocationTextView = (TextView)findViewById(R.id.issLocationTextView);
		params=(MarginLayoutParams )_issLocationTextView.getLayoutParams();
		params.leftMargin=10;		 
		params.topMargin=260;
		_issLocationTextView.setLayoutParams(params);
		_issLocationTextView.setTextColor(Color.GREEN);
					
		// Obtain sensor service and get rotation vector sensor for obtaining the orientation
		_sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		_rotVectSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		
		// Obtain location service
		_locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);		
		
		_iss = new SpaceObject();
		_userLocation = new Coord();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.arissmap, menu);
		return true;
	}

	@Override
	protected void onResume(){		
		super.onResume();		
		
		//Open default camera and set on Preview		
		_camera = Camera.open();
        _cameraCurrentlyLocked = _defaultCameraId;
        _preview.setCamera(_camera);
        
        _drawView.updatePosition(_preview.getWidth()/2, _preview.getHeight()/2);
		
		// Register listener for the sensor manager
		_sensorManager.registerListener(this, _rotVectSensor, SensorManager.SENSOR_DELAY_NORMAL);		
		
		// Obtain location providers based on an Accuracy Critera object
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		
		List<String> enabledProviders = _locationManager.getProviders(criteria, true);
		
		if(!enabledProviders.isEmpty()){
			for(String enabledProvider : enabledProviders){
				_locationManager.requestSingleUpdate(enabledProvider, this, null);
			}
		}		

		_locationUptimeAtResume = SystemClock.uptimeMillis();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		_sensorManager.unregisterListener(this);
		_locationManager.removeUpdates(this);
		
		releaseCamera();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
			
			// Convert the rotation vector to a 4x4 matrix for remaping the y axis to the z axis
			SensorManager.getRotationMatrixFromVector(_rotationMatrix,  event.values);
			SensorManager.remapCoordinateSystem(_rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, _rotationMatrix);			
			SensorManager.getOrientation(_rotationMatrix, _orientationVals);
			
			// Convert the result from radian to degrees
			for(int i = 0; i < _orientationVals.length; ++i){
				_orientationVals[i] = (float)Math.toDegrees(_orientationVals[i]);
			}
			
			float azimuth = _orientationVals[0] < 0? 360 + _orientationVals[0] : _orientationVals[0]; 
			float elevation = -_orientationVals[1]; 
			
			_orientationTextView.setText(" Azimuth: " + azimuth + "\n Elevation: " + elevation );
			
			if(!_obtainingLocation)
				new ISSLocationTask().execute("http://api.open-notify.org/iss-now/v1/");		
		
			
			_drawView.updatePosition(_preview.getWidth()/2, _preview.getHeight()/2);        
			
			ARMapping();
		}
		
	}	
	
	private void ARMapping(){
		
		if(_camera != null){
			float camH_Angle = _camera.getParameters().getHorizontalViewAngle()/2;
			float camV_Angle = _camera.getParameters().getVerticalViewAngle()/2;
			float camWidth = _preview.getWidth();
			float camHeight = _preview.getHeight();
			
			float azimuth = _iss.getAzimuth();
			float elevation = _iss.getElevation();	
			
			if(_orientationVals[0] < (camH_Angle + azimuth) && _orientationVals[0] > (-camH_Angle + azimuth)
				&& _orientationVals[1] < (camV_Angle + elevation) && _orientationVals[1] > (-camV_Angle + elevation)){
				
				_drawView.updateColor(255, 0, 255, 0);				
			}
			else{
				_drawView.updateColor(255, 255, 0, 0);				
			}
						
			int xPos = 0;
			int yPos = 0;
			
			xPos = (int)calculatePos(azimuth, _orientationVals[0], camWidth, camH_Angle);
			yPos = (int)calculatePos(elevation, _orientationVals[1], camHeight, camV_Angle);
			
			_drawView.updatePosition(xPos, yPos);
		}	
		
	}
	
	private float calculatePos(float issAngle, float observerAngle, float max, float camAngle){
		
		float middle = camAngle/2;
		float leftLimit = observerAngle - middle;
		float rightLimit = observerAngle + middle;
		float pos = 0;
		
		if(issAngle >= leftLimit && issAngle < rightLimit){
			//The iss is in focus
			
			float percentage = (issAngle - leftLimit) / camAngle;
			
			pos = max * percentage;
		}
		else if (issAngle < leftLimit){
			pos = 0;
		}
		else{
			pos = max - 1;
		}
		
		return pos;
	}
	
	private void obtainDefaultCameraId(){
		// Find the total number of cameras available
        _numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        CameraInfo cameraInfo = new CameraInfo();
            for (int i = 0; i < _numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                    _defaultCameraId = i;
                }
            }
	}	 

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
	
    private void releaseCamera(){
    	// Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (_camera != null)
        {
            _preview.setCamera(null);
            _camera.release();
            _camera = null;
        }
    }	
	
	private void updateISSCoordinates(String respStr){

		try {
			JSONObject obj = new JSONObject(respStr);
			JSONObject issloc = obj.getJSONObject("iss_position");
			 
			//TODO: Uncomment this	 
			_iss.setCoordinates(Float.parseFloat(issloc.getString("latitude")),
					Float.parseFloat(issloc.getString("longitude")));
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		//HARDCODED SUN
//		_iss.setCoordinates(12.072f, 294.800f);
		
		Coord issLocation = _iss.getCoordinates();
		_iss.calculateHorizontalCoordinates(_userLocation);
		
		_issLocationTextView.setText("\nISS Location: " + issLocation.getLatitude() + ", " + issLocation.getLongitude() + 
										"\nAzimuth: " + _iss.getAzimuth() + " Elevation: " + -_iss.getElevation());			
		
		
	}
	
	class ISSLocationTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... urls) {
			String response = "";			
			
			if(urls.length == 0){
				return null;
			}
			
			HttpClient httpClient = new DefaultHttpClient();
			String url = urls[0];
			HttpGet get = new HttpGet(URI.create(url));
					
			get.setHeader("content-type", "application/json");
					    		 
			HttpResponse resp;
			try {
				resp = httpClient.execute(get);
				
				response = EntityUtils.toString(resp.getEntity());				
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			_obtainingLocation = true;
			
			return  response;
		}
	
		@Override
		protected void onPostExecute(String result){
			
			updateISSCoordinates(result);	
			_obtainingLocation = false;
		}
	}
		
	@Override
	public void onLocationChanged(Location location) {
		_userLocation.setLatitude((float)location.getLatitude());
		_userLocation.setLongitude((float)location.getLongitude());
		String provider = location.getProvider();
		String accuracy = String.valueOf(location.getAccuracy());
		String timeToFixValue = String.valueOf(SystemClock.uptimeMillis() - _locationUptimeAtResume);
		
		_locationTextView.setText( "\n" + String.valueOf(_userLocation.getLatitude()) + ", " +
									String.valueOf(_userLocation.getLongitude()) +
									"\nProvider: " + provider + 
									"\nAccuracy: " + accuracy + " meters." + 
									"\nTimeToFixValue: " + timeToFixValue);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
