package com.example.adhocsocial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;

public class AdhocControl {
	public static AdhocControl control;
	/*
	 * MASKING_BYTES
	 * 1 = 255.0.0.0
	 * 2 = 255.255.0.0
	 * 3 = 255.255.255.0
	 */
	public static final char MASKING_BYTES = 1;
	//These make up the IP
	//[byte1].[byte2].[byte3].[byte4]
	public static final char IP_BYTE_1 = 192;
	public static final char IP_BYTE_2 = 168;
	public static final char IP_BYTE_3 = 2;
	
	private volatile Queue<Packet> sendQueue;
	private volatile Queue<Packet> receiveQueue;
	
	private UdpReceiver udpR;
	private UdpSender udpS;
	private Thread thr;
	private boolean started = false;
	private TimeKeeper time;
	
	private static AdhocTrialActivity main;
	private static EditText text;
	
	private static Packet receivedPacket;
	
	public static AdhocControl startControl(AdhocTrialActivity m, EditText t){
		main = m;
		text = t;
		if (control == null)
			control = new AdhocControl();
		return control;
	}
	
	public AdhocControl(){
		sendQueue = new LinkedList<Packet>();
		receiveQueue = new LinkedList<Packet>();
		time = new TimeKeeper(10);
		time.startTimer();
		
		try {
        	udpS = new UdpSender(sendQueue);
        	udpR = new UdpReceiver(sendQueue, receiveQueue, time);
		} catch (BindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean startAdhoc(){
		if (started) return true;
		main.startAdhocService();
		udpR.startThread();
		udpS.startThread();
		started = true;
		return true;
	}
	
	public boolean stopAdhoc(){
		if (!started) return true;
		main.stopAdhocService();
		udpR.stopThread();
		udpS.stopThread();
		started = false;
		return true;
	}
	
	public boolean sendPacket(Packet p){
		sendQueue.add(p);
		return true;
	}
	
	public static void packetReceived(Packet p){
		receivedPacket = p;
		main.runOnUiThread(updateText);
	}
	
	static Runnable updateText = new Runnable(){
		public void run(){
			text.setText(receivedPacket.getMessage());
		}
	};
	
	public boolean isStarted(){
		return started;
	}
	
	public String getMyAddress(){
		return AdhocService.getMacAddress();
	}
	
	public Packet getNextPacket(){
		if (! receiveQueue.isEmpty()){
			return receiveQueue.remove();
		}
		else{
			return null;
		}
	}
}
