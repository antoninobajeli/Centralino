package com.bajeli.centralino;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.provider.Settings;
import android.app.PendingIntent;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener {
	public static String TAG = "MainAct";
	static int REQUEST_ENABLE_BT=2;
	
	static BluetoothAdapter mBluetoothAdapter;
	//public static UUID  APRIPORTA_UUID =new UUID("00001101-0000-1000-8000-00805f9b34fb");
	public static UUID  APRIPORTA_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	public static String deviceName="BluetoothBee";
	static ConnectThread connectedDevice;
    private static final long POINT_RADIUS = 200; // in Meters
    private static final double latitude=37.541245;
    private static final double longitude=15.106205;
    LocationManager locationManager;
    static int MODE=0; // 0 master centralino, 1 slaves remote



    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button button= (Button) findViewById(R.id.btexit);
		button.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        exit();
		    }
		});
		
		
		
		
		Button buttonCommuta= (Button) findViewById(R.id.btcommuta);
		buttonCommuta.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        commuta();
		    }
		});
		
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
		}
		
		// controllo se il buelthooth e' attivo
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    
		    
		}
		
		connectToBee();
		
		
		
		
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		/*			
		mBluetoothAdapter.startDiscovery();
		Log.d(TAG,"mBluetoothAdapter laubched");
		*/
		//ConnectThread




        if (MODE==1) {
            // new for location
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //for debugging...
            // allow notification on the MainActivity
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);


            // check if enabled and if not send user to the GSP settings
            // Better solution would be to display a dialog and suggesting to
            // go to the settings
            boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!enabled) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }


            //i'm defining and registering the receiver for the notification
            String ACTION_FILTER = "com.bajeli.centralino.";
            registerReceiver(new LocationReceiver(), new IntentFilter(ACTION_FILTER));


            //Setting up My Broadcast Intent
            Intent i = new Intent(ACTION_FILTER);
            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), -1, i, 0);


            // setting up locationManager in orther to FIRE the PendingIntent
            locationManager.addProximityAlert(latitude, longitude, POINT_RADIUS, -1, pi);


            //TODO mock positions
            setMockLocation(locationManager, 37.541245, 15.106205, 20);
            setMockLocation(locationManager, 37.54124, 15.10620, 20);
            setMockLocation(locationManager, 37.5412, 15.1062, 20);
            setMockLocation(locationManager, 37.541, 15.106, 20);
            setMockLocation(locationManager, 37.54, 15.10, 20);
            setMockLocation(locationManager, 37.5, 15.1, 20);
            setMockLocation(locationManager, 37.6, 15.11, 20);
        }
    }


    private void setMockLocation(LocationManager lm,double latitude, double longitude, float accuracy) {
        lm.addTestProvider (LocationManager.GPS_PROVIDER,
                "requiresNetwork" == "",
                "requiresSatellite" == "",
                "requiresCell" == "",
                "hasMonetaryCost" == "",
                "supportsAltitude" == "",
                "supportsSpeed" == "",
                "supportsBearing" == "",
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);

        Location newLocation = new Location(LocationManager.GPS_PROVIDER);

        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setAccuracy(accuracy);
        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        newLocation.setTime(SystemClock.elapsedRealtimeNanos());

        lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        lm.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null,System.currentTimeMillis());


        try {
            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
        }catch (Exception e){
            e.printStackTrace();
        }

    }



	private static void connectToBee(){
		// elenco dei device accoppiati
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		        //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		        Log.d(TAG,"device.getAddress() "+device.getAddress());
		        Log.d(TAG,"device.getUuids() "+device.getUuids());
		        Log.d(TAG,"device.getName() "+device.getName());
		        ParcelUuid[] uuid= device.getUuids();
		        if (uuid!=null){
		        	for (int i=0;i<uuid.length;i++){
		        		Log.d(TAG,"uuid >>> "+uuid[i].getUuid());
		        	}
		        }
		        
		        if (device.getName().equals(deviceName)){
		        	connectedDevice= new ConnectThread(device);
		        	connectedDevice.run();
		        }
		        
		    }
		}
	}
	
	public static void commuta(){
		
		
		byte apri=101;
		byte chiudi=111;
		byte tmpbuf[] = new byte[1];
		
		tmpbuf[0]=apri;
		if (connectedDevice==null){
			Log.d(TAG, "connectedDevice is null");
			return;
		}
		if (connectedDevice.connectedToApriportaThread==null){
			connectToBee();
			Log.d(TAG, "connectedToApriportaThread is null");
			return;
		}
		   
		connectedDevice.connectedToApriportaThread.write(tmpbuf);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tmpbuf[0]=chiudi;
		connectedDevice.connectedToApriportaThread.write(tmpbuf);
		
	}
	
	
	public void exit(){
		this.finish();
	}
	
	
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	    	Log.d(TAG,"BroadcastReceiver onReceive");
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            Log.d(TAG,"device.getAddress() "+device.getAddress());
	            ParcelUuid[] uuid= device.getUuids();
	            if (uuid!=null)
	               Log.d(TAG, "UUID: " + uuid.length);
	            else
	            	Log.d(TAG, " null ");
		        
	            
	            //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	        }
	    }
	};
			
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(mReceiver);
		
		super.onDestroy();
	}





    @Override
    public void onLocationChanged(Location newLocation) {

        Location old = new Location("OLD");
        old.setLatitude(latitude);
        old.setLongitude(longitude);

        double distance = newLocation.distanceTo(old);

        Log.i("onLocationChanged", "Distance: " + distance);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
