/*
 * File: AdhocService.java
 * 
 * Copyright (C) 2010 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package com.example.adhocsocial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import com.example.adhoctrial.R;

//import org.hfoss.posit.rwg.Constants;

/*import org.hfoss.posit.android.ListFindsActivity;
import org.hfoss.posit.android.PositMain;
import org.hfoss.posit.android.R;
import org.hfoss.posit.android.utilities.Utils;
import org.hfoss.posit.rwg.RwgManager;
import org.hfoss.posit.rwg.RwgReceiver;
import org.hfoss.posit.rwg.RwgSender;
import org.hfoss.third.CoreTask;*/

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * This class was edited from the original class created for the Posit mobile project
 * 
 * Original author:
 * @author rmorelli
 *
 */
public class AdhocService extends Service {
	protected static final String TAG = "Adhoc";

	public static final String MAC_ADDRESS = "Mac Address";
	public static final int MAX_PACKET_SIZE = 2048; // 1 K
	//public static final String IP_SUBNET = "192.";
	public static final int DEFAULT_PORT_BCAST = 8888;
	public static final int ADHOC_NOTIFICATION = 1;
	public static final int NEWFIND_NOTIFICATION = 2;
	public static final String MODE = "Mode";
	public static final int MODE_ADHOC = 1;           // Rooted phones only
	public static final int MODE_INFRASTRUCTURE = 2;  // Uses hotspot
	private static final int START_STICKY = 1;

	public static AdhocService adhocInstance = null;
	
	private static NotificationManager mNotificationManager;

	private Notification mNotification;

	private String mAppDataPath;  // Where our configuration files are

	private boolean mInAdhocMode = false;
	private WifiManager mWifi;
	private WifiInfo mWifiInfo;

	private static String mMacAddress = "";
	private static short myHash3;
	private static short myHash2;
	private static short myHash1;
	private CoreTask mCoretask;
	
	private int mGroupSize;
	private String mSSID;


	@Override
	public void onCreate() {
		super.onCreate();

		// Get Preferences
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		mGroupSize = Integer.parseInt(sp.getString("Group Size", "3"));
		mSSID = sp.getString("SSID", "MobiNode");
		Log.i(TAG, "Preferences SSID= " + mSSID + " Group Size = " + mGroupSize );

		// Create notification manager.
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}


	/**
	 * Copies the configuration a file to the phone.
	 * @param filename
	 * @param resource
	 */
	private void copyBinary(String filename, int resource) {
		File outFile = new File(filename);
		InputStream is = this.getResources().openRawResource(resource);
		byte buf[]=new byte[1024];
		int len;
		try {
			OutputStream out = new FileOutputStream(outFile);
			while((len = is.read(buf))>0) {
				out.write(buf,0,len);
			}
			out.close();
			is.close();
		} catch (IOException e) {
			Log.e(TAG, "Couldn't install file - " + filename + "!");
			//Utils.showToast(this,"Couldn't install file - "+filename+"!");
		}
	}

	/**
	 * Disables phone's Wifi if it is connected.  Needed to put the
	 * WiFi into adhoc mode. 
	 */
	public void disableWifi() {
		if (mWifi.isWifiEnabled()) {
			mWifi.setWifiEnabled(false);
			try {
				do {
					Thread.sleep(3000);
				} while(mWifi.isWifiEnabled());	 
			} catch (InterruptedException e) {
				// nothing
			}
		}
		Log.d(TAG, "Wifi should now be disabled!");    	
	}

	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(TAG, "AdhocService,  Starting, id " + startId);
		handleStartUp();
	}

	/**
	 * Replaces onStart(Intent, int) in pre 2.2 versions.
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
	public int onStartCommand(Intent intent, int flags, int startId) {
		//super.onStart(intent, startId);

		Log.i(TAG, "AdhocService,  Starting, id " + startId);
		handleStartUp();

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}
	
	/**
	 * @param addr a phone's MAC address. A simple polynomial has is used.
	 * @return 
	 */
	public static int rwgHash3(String addr) {
		final int A = 33;
	    int L = addr.length();
		long sum = 0;
		for (int k = 0; k < addr.length(); k++) {
			sum += ((int)addr.charAt(k) * (int)Math.pow(A, L));
			L--;
		}
		return (int)(sum % 0x100);
		
	}
	
	public static int rwgHash2(String addr) {
		final int A = 33;
	    int L = addr.length();
		long sum = 0;
		for (int k = 0; k < addr.length(); k++) {
			sum += ((int)addr.charAt(k) * (int)Math.pow(A, L));
			L--;
		}
		return (int)((sum>>8) % 0x100);
		
	}
	
	public static int rwgHash1(String addr) {
		final int A = 33;
	    int L = addr.length();
		long sum = 0;
		for (int k = 0; k < addr.length(); k++) {
			sum += ((int)addr.charAt(k) * (int)Math.pow(A, L));
			L--;
		}
		return (int)((sum>>16) % 0x100);
		
	}
	
	/**
	 * Handles the start up of the service for both 2.2 and pre-2.2 versions. 
	 * The service can be run in either INFRASTRUCTURE_MODE, in which it
	 * relies on a hotspot in the vicinity. Or, for ROOTED phones, in ADHOC_MODE,
	 * in which it communicates peer-to-peer in true adhoc fashion.  In either case
	 * the phone needs a unique ID.  If it is connected to WiFi, the phone's MAC
	 * address can be used.  Otherwise we use its IMEI.  This is mostly useful
	 * for ADHOC_MODE where the phones share a subnet of 192.168.2.NN where NN is
	 * the hash of the phone's Unique ID. 
	 * @param intent  The intent received from PositMain
	 */
	private void handleStartUp() {
		//mAdhocMode = intent.getIntExtra(MODE, MODE_INFRASTRUCTURE);

		// Get WiFi status
		mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifi.getConnectionInfo();
		mMacAddress = mWifiInfo.getMacAddress();

		// If we can't get the phone's unique MAC address, use its IMEI as a unique ID
		if (mMacAddress == null) {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			mMacAddress = tm.getDeviceId();
		}
		UdpReceiver.setMyAddress(mMacAddress);
		HopList.setMyAddr(mMacAddress);
		UdpSender.setMyAddress(mMacAddress);
		DiscNodes.setMyAddress(mMacAddress);
		// The hash of its MAC or IMEI is used as the IP address with 192.168.2.myHash
		myHash3 = (short) rwgHash3(mMacAddress);
		myHash2 = (short) rwgHash2(mMacAddress);
		myHash1 = (short) rwgHash1(mMacAddress);
		Log.i(TAG, "MAC Address = " + mMacAddress + " myHash1 = " + myHash1
				+ " myHash2 = " + myHash2+ " myHash3 = " + myHash3);

		boolean success = false;
		try {
			success = initializeAdhocMode();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start RWG if the WiFi was set up correctly
		if (success) {
			if (!success) {
				stopSelf();
			} else {
				mInAdhocMode = true;
				Log.i(TAG, "adhoc on");
			}
		} else {
			Log.e(TAG, "Adhoc service aborting");
		}
	}

	/**
	 * Performs all the methods required to start the phone in ADHOC_MODE. 
	 * This mode requires root access.  If the phone has root access, the
	 * configuration file (tiwlan.ini) is loaded from resources (if not already
	 * loaded) and then a series of root-level commands are run that switch the
	 * phone's default tiwlan0 interface to one that runs in adhoc mode. 
	 * @return
	 */
	private boolean initializeAdhocMode() {
		Log.i(TAG, "Initializing Adhoc mode");

		// Setup Coretask 
		mCoretask = new CoreTask();
		mCoretask.setPath(this.getApplicationContext().getFilesDir().getParent());
		mAppDataPath = getApplicationContext().getFilesDir().getParent();
		Log.d(TAG, "Current directory is "+ mAppDataPath);
		
		java.io.File file = new java.io.File(mAppDataPath , "tiwlan.ini");
        if (!file.exists()) {
        	try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "couldnt create tiwlan");
			}
        	copyBinary(mAppDataPath+"/tiwlan.ini", R.raw.tiwlan);
        }

		if (mCoretask.hasRootPermission()) {
			Log.i(TAG, "This phone IS ROOTED.");
		}
		else {
			Log.i(TAG, "This phone IS NOT ROOTED");
		}

		Log.i(TAG, "Disabling wifi");
		disableWifi();

		Log.i(TAG, "Starting WiFi in Adhoc mode");
		boolean ok = mCoretask.runRootCommand("insmod /system/lib/modules/wlan.ko");

		Log.i(TAG, "Loaded wlan kernel module, insmod wlan.ko ok = " + ok);
		ok = mCoretask.runRootCommand("wlan_loader -f /system/etc/wifi/Fw1251r1c.bin -e /proc/calibration -i "+mAppDataPath+"/tiwlan.ini");
		Log.i(TAG, "Configured wlan kernel module, wlan_loader... ok = " + ok);

		String mask = "255.0.0.0";
		String myIP = Integer.toString(AdhocControl.IP_BYTE_1) + ".";
		if (AdhocControl.MASKING_BYTES >= 2){
			myIP += Integer.toString(AdhocControl.IP_BYTE_2) + ".";
			mask = "255.255.0.0";
		}
		if (AdhocControl.MASKING_BYTES >= 3){
			myIP += Integer.toString(AdhocControl.IP_BYTE_3) +".";
			mask = "255.255.255.0";
		}
		if (AdhocControl.MASKING_BYTES <= 1)
			myIP += myHash1 + ".";
		if (AdhocControl.MASKING_BYTES <= 2)
			myIP += myHash2 + ".";
		if (AdhocControl.MASKING_BYTES <= 3)
			myIP += myHash3;
		
		Log.i(TAG, "Configuring the network interface with IP = " + myIP);
		ok = mCoretask.runRootCommand("ifconfig tiwlan0 " + myIP + " netmask " + mask);
		Log.i(TAG, "Configured network interface, ifconfig ... ok = " + ok);

		Log.i(TAG, "Starting Wifi");
		ok = mCoretask.runRootCommand("ifconfig tiwlan0 up");
		Log.i(TAG, "Started Wifi ... ok = " + ok);

		return ok;  
	}

	/**
	 * Disables Wifi. This is necessary to run in ad-hoc mode. This is called in initAdhocMode()
	 * and also from onDestroy().
	 * @return
	 */
	private boolean stopWifi() {
		boolean ok = mCoretask.runRootCommand("ifconfig tiwlan0 down");
		if (ok) {
			Log.i(TAG, "Shut down adhoc network");
		} else {
			Log.e(TAG, "Unable to shut down adhoc network");
		}
		ok = mCoretask.runRootCommand("wlan_loader -f /system/etc/wifi/Fw1251r1c.bin -e /proc/calibration -i /system/etc/wifi/tiwlan.ini");
		if (ok) {
			Log.i(TAG, "Reconfigured kernel module wlan");
		} else {
			Log.e(TAG, "Unable to reconfigure kernel module wlan");
		}
		ok = mCoretask.runRootCommand("rmmod wlan");
		if (ok) {
			Log.i(TAG, "Removed kernel module , rmmod wlan");
		} else {
			Log.i(TAG, "Unable to remove kernel module , rmmod wlan");
		}
		return ok;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// If a ROOTED phone and in MODE_ADHOC mode
		stopWifi();
		if (mInAdhocMode) {
			mCoretask.runRootCommand("ifconfig tiwlan0 down");
			adhocInstance = null;
		}
		mInAdhocMode = false;
		Log.d(TAG,"Destroyed Adhoc service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Returns the phone's MAC addresss.
	 * @param cxt
	 * @return
	 */
	public static String getMacAddress() {
		return mMacAddress;
	}


	public static boolean isRunning() {
		return adhocInstance  != null;
	}


}
