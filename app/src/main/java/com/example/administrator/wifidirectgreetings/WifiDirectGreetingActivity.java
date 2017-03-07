package com.example.administrator.wifidirectgreetings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class WifiDirectGreetingActivity extends AppCompatActivity {

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pManager.Channel mLocalServiceChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    public static final String TAG = "WifiDirectGreeting";
    public static final String WIFI_DIRECT_SERVICE_TYPE = "WifiDirectServiceType";
    public static final String WIFI_DIRECT_SERVICE_INSTANCE = "WifiDirectGreetings";

    WifiP2pManager.ActionListener mWifiActionListener;
    Map<String, String> greetings = new HashMap<String, String>();
    WifiP2pServiceInfo srvcInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct_greeting);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mLocalServiceChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WifiGreetingBroadcastReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        Log.i(TAG,"Broadcast Receiver Registered");

        mManager.clearServiceRequests(mChannel, null);

        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {
                Log.i(TAG, "DnsSdTxtRecord available -" + record.toString()
                        + " from " + device.deviceName);
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
                Log.i(TAG, "onBonjourServiceAvailable " + instanceName);
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);
        
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(WIFI_DIRECT_SERVICE_INSTANCE, WIFI_DIRECT_SERVICE_TYPE);

        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
                Log.i(TAG, "addServiceRequest succeeded");
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Log.i(TAG, "addServiceRequest failure with code " + code);
            }

        });
        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
                Log.i(TAG, "discoverServices succeeded");
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.i(TAG, "P2P isn't supported on this device.");
                } else {
                    Log.i(TAG, "discoverServices failure");
                }
            }
        });

        //
        greetings.put("greeting", "Hi,there");
        srvcInfo = WifiP2pDnsSdServiceInfo.newInstance(WIFI_DIRECT_SERVICE_INSTANCE, WIFI_DIRECT_SERVICE_TYPE, greetings);
        mManager.addLocalService(mLocalServiceChannel, srvcInfo, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "Local service added successfully");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.i(TAG, "Local service addition failed : " + reasonCode);
            }
        });
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

}
