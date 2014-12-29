package com.bajeli.centralino;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LocationReceiver extends BroadcastReceiver {
    TelephonyManager telephonyMng;

    List<String> whiteListNumbers = new ArrayList<String>();
    static final String TAG="ServiceReceiver";

    // Listener location events
    @Override
    public void onReceive(Context context, Intent intent) {

        final String key = LocationManager.KEY_PROXIMITY_ENTERING;
        final Boolean entering = intent.getBooleanExtra(key, false);

        if (entering) {
            Toast.makeText(context, "entering", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "exiting", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNumberInList(String phoneNr){
    	for(String str: whiteListNumbers) {
            if(str.trim().equals(phoneNr)){
            	return true;
            }
        }
        return false;
    }
    
    public void onDestroy() {

    }

}
