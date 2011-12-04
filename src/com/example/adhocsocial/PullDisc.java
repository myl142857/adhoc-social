package com.example.adhocsocial;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

//PullDisc will ask for user data on intervals
public class PullDisc extends DiscNodes {
	//set PULL_MS to a value greater than 0 if the pull should be automatic in a thread
	public static final int PULL_MS = 0;
	private boolean keepRunning = false;
	private Thread pullThread;
	public PullDisc(HopList hopList, Queue<Packet> sendQueue,HashMap<String,Queue<Packet>> receiveQueue, Buddylist list, String myName){
		super(hopList, sendQueue, receiveQueue,list, myName);
		if (PULL_MS > 0){
			keepRunning = true;
			pullThread = new Thread(pullDat);
			pullThread.start();
		}
	}
	public PullDisc(Queue<Packet> sendQueue,HashMap<String,Queue<Packet>> receiveQueue, Buddylist list, String myName){
		super(sendQueue, receiveQueue,list, myName);
		if (PULL_MS > 0){
			keepRunning = true;
			pullThread = new Thread(pullDat);
			pullThread.start();
		}
	}
	protected void finalize(){
		keepRunning = false;
	}
	private Runnable pullDat = new Runnable(){
		@Override
		public void run() {
			while(keepRunning){
				getBuddies();
				
				try {
					Thread.sleep(PULL_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	public boolean getBuddies(){
		if (myAddress == null || myAddress.equals("")) return false;
		Packet p;	
		EthernetHeader eheader = new EthernetHeader(myAddress);
		p = new Packet(eheader, myName);
		p.setMaxHop(MAX_HOPS);
		p.setMessageType("Pull");
		p.getHeader().setType(PacketHeader.TYPE_REQ);
		sendPacket(p);
		return true;
	}
}
