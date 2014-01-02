package com.example.wifidirect;

import com.example.wifidirect.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

public class MyReceiver extends BroadcastReceiver {
	private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
    
	public MyReceiver(WifiP2pManager manager, Channel channel,
			MainActivity activity) {
		super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			 if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				 mActivity.isWifiP2pEnable=true;  // Wifi Direct is enabled
				
			 }
			 else
			 {
				 mActivity.isWifiP2pEnable=false; // Wifi Direct is not enabled
				
			 }
		}
		else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers
			if(mManager!=null)
			{
				mManager.requestPeers(mChannel,(PeerListListener)mActivity.peerListListener );
			}
		}
		else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// Respond to new connection or disconnections
			if(mManager==null)
        		return ;
			
			NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	 if (networkInfo.isConnected()) {
        		 mManager.requestConnectionInfo(mChannel, mActivity);
        	
        	
        		 
        	 }
        	 
        	 int targetpeer = mActivity.targetpeer;
        	 if(targetpeer==-1)
        	 {
        		 mActivity.cur_state=0;
        		 mActivity.changeview();
        	 }
        	 else
        	 {
        		 
        		 WifiP2pDevice device = mActivity.peers.get(targetpeer);
        		 
        		 if(device.status==WifiP2pDevice.CONNECTED)
        		 {
        			;
        		 }
        		 else
        		 {
        			 mActivity.cur_state=0;
            		 mActivity.changeview();
        			 
        		 }
        		 
        		 
        	 
        	 }
        	 
			
		}
		else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			// Respond to this device's wifi state changing
			mActivity.SetDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
			WifiP2pDevice device = mActivity.Me;
			if(device.status!=WifiP2pDevice.CONNECTED)
			{
				mActivity.cur_state=0;
       		 	mActivity.changeview();
			}
		}
		
	}
}
