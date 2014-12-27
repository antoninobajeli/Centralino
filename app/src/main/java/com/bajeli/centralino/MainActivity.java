package com.bajeli.centralino;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	public static String TAG = "MainAct";
	static int REQUEST_ENABLE_BT=2;
	
	static BluetoothAdapter mBluetoothAdapter;
	//public static UUID  APRIPORTA_UUID =new UUID("00001101-0000-1000-8000-00805f9b34fb");
	public static UUID  APRIPORTA_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	public static String deviceName="BluetoothBee";
	static ConnectThread connectedDevice;
	
	
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
	
}
