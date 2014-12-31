package com.bajeli.centralino;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ServiceReceiver extends BroadcastReceiver {
    TelephonyManager telephonyMng;

    List<String> whiteListNumbers = new ArrayList<String>();
    static final String TAG="ServiceReceiver";
    
    public void onReceive(Context context, Intent intent) {
    	whiteListNumbers.add("+393338456087");
    	whiteListNumbers.add("+393470339493");//papa
    	whiteListNumbers.add("+393470621948");//carm
    	whiteListNumbers.add("+393470149386");//mamm
    	whiteListNumbers.add("+393454927178");//giani
    	whiteListNumbers.add("+393701302955");//tiziana
        whiteListNumbers.add("+393478387938");//Paolo Vittoria
        whiteListNumbers.add("+393405592036");//Vittoria

    	
    	
        PhoneStateListener phoneListener = new PhoneStateListener();
        telephonyMng = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        
        /* telephonyMng.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        Log.d("calling", telephony.getLine1Number());*/
        
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE); 

        switch (telephonyMng.getCallState()) {

            case TelephonyManager.CALL_STATE_RINGING:
                    String phoneNr= intent.getStringExtra("incoming_number");
                    Log.d(TAG, phoneNr);
                    if (isNumberInList(phoneNr)) 
                         MainActivity.commuta();
                    
                    break;
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
    	telephonyMng.listen(null, PhoneStateListener.LISTEN_NONE);
    }

}
