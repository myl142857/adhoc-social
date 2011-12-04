package com.example.adhocsocial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
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
	public static final int VIEW_MAIN = 0;
	public static final int VIEW_LIST = 0;
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
	
	/*
	 * 0: broadcast
	 * 1: who is there?
	 */
	public static final int DISCOVERY_TYPE = 1;
	
	private volatile Queue<Packet> sendQueue;
	private volatile HashMap<String,Queue<Packet>> receiveQueue;
	
	private Buddylist buddylist;
	
	private UdpReceiver udpR;
	private UdpSender udpS;
	private Thread startThread;
	//private HopList hopList;
	private boolean started = false;
	private String myName;
	
	private static Packet receivedPacket;
	private DiscNodes discovery;
	private Chat chat;
	
	private LinkedList<Buddy> lastList;
	private int listSelected;
	private int currentView = VIEW_MAIN;
	
	private boolean refreshMe = false;
	
	public static AdhocControl startControl(){
		if (control == null)
			control = new AdhocControl();
		return control;
	}
	
	public AdhocControl(){
		Logger.startLogger();
		sendQueue = new LinkedList<Packet>();
		receiveQueue = new HashMap<String, Queue<Packet>>();
		TimeKeeper.startTimer();
		//hopList = new HopList();
		
		try {
        	udpS = new UdpSender(sendQueue);
        	udpR = new UdpReceiver(sendQueue, receiveQueue);
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
	
	public void refreshBuddyList(){
		if (DISCOVERY_TYPE == 1 && PullDisc.PULL_MS <= 0){
			PullDisc pull = (PullDisc) discovery;
			pull.getBuddies();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pull.loadBuddies();
		}
	}
	
	public int getView(){
		return currentView;
	}
	
	public void setView(int view){
		currentView = view;
	}
	
	private Runnable startMeUp = new Runnable(){
		public void run(){
			int timeout = 10000;
			int currentTime = 0;
			while(!AdhocService.isStarted() && currentTime<timeout){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				currentTime+=10;
			}
			udpR.startThread();
			udpS.startThread();
			buddylist = new Buddylist(sendQueue);
			if (DISCOVERY_TYPE <= 0){
				discovery = new PushDisc(sendQueue, receiveQueue,buddylist,myName);
			}
			else{
				discovery = new PullDisc(sendQueue, receiveQueue,buddylist,myName);
			}
			chat = new Chat(buddylist, sendQueue, receiveQueue, myName);
			started = true;
			refreshMe = true;
		}
	};
	
	public boolean startAdhoc(String name){
		if (started) return true;
		myName = name;
		startThread = new Thread(startMeUp);
		startThread.start();
		return true;
	}
	
	public boolean stopAdhoc(){
		if (!started) return true;
		udpR.stopThread();
		udpS.stopThread();
		started = false;
		return true;
	}
	
	public boolean sendPacket(Packet p){
		p.setMessageType("Message");
		p.getHeader().setType(PacketHeader.TYPE_DAT);
		sendQueue.add(p);
		return true;
	}
	
	public boolean isStarted(){
		return started;
	}
	
	public String getMyAddress(){
		return AdhocService.getMacAddress();
	}
	
	/*public int getMinHop(String addr, int packetID){
		return hopList.getMinPacketHops(addr, packetID);
	}*/
	
	public boolean sendMessage(String message){
		return chat.sendText(message);
	}
	
	public LinkedList<Buddy> getChatList(){
		lastList = chat.getChatList();
		listSelected=0;
		return lastList;
	}
	
	public LinkedList<Buddy> getAvailableBuddies(){
		LinkedList<Buddy> chatBuddies = control.getChatList();
		LinkedList<Buddy> buddyList = buddylist.getList();
		LinkedList<Buddy> availableBuddies = new LinkedList<Buddy>();
		listSelected=1;
		boolean found;
		for (int i = 0; i<buddyList.size();i++){
			found = false;
			for (int j = 0; j<chatBuddies.size();j++){
				if (chatBuddies.get(j).getAddress().equals(buddyList.get(i).getAddress()))
					found = true;
			}
			if (found == false){
				availableBuddies.add(buddyList.get(i));
			}
		}
		lastList = availableBuddies;
		return availableBuddies;
	}
	
	public void indexSelected(int index){
		Buddy b = lastList.get(index);
		if (listSelected == 0){
			//remove from chat
			chat.removeBuddyFromChat(b);
		}
		else{
			//add to chat
			chat.addBuddyToChat(b);
		}
	}
	
	public boolean chatUpdated(){
		if (chat == null)
			return false;
		return chat.isUpdated();
	}
	
	public String getChatMessages(){
		return chat.getMessages();
	}
	
	public boolean canRefresh(){
		if (refreshMe){
			refreshMe = false;
			return true;
		}
		else
			return false;
	}
}
