package com.example.adhocsocial;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

//PushDisc will broadcast my position on intervals
public class PushDisc extends DiscNodes {
	protected static final int BROADCAST_MS = 5000;
	private boolean keepRunning = false;
	private Thread broadcastThread;
	
	public PushDisc(HopList hopList, Queue<Packet> sendQueue, HashMap<String,Queue<Packet>> receiveQueue, Buddylist list, String myName){
		super(hopList, sendQueue, receiveQueue, list, myName);
		keepRunning = true;
		broadcastThread = new Thread(broadcast);
		broadcastThread.start();
	}
	
	public PushDisc(Queue<Packet> sendQueue, HashMap<String,Queue<Packet>> receiveQueue, Buddylist list, String myName){
		super(sendQueue, receiveQueue, list, myName);
		keepRunning = true;
		broadcastThread = new Thread(broadcast);
		broadcastThread.start();
	}
	
	protected void finalize(){
		keepRunning = false;
	}
	
	private Runnable broadcast = new Runnable(){
		public void run(){
			while(keepRunning){
				if (!myAddress.equals("")){
					EthernetHeader eheader = new EthernetHeader(myAddress);
					String name = myName;
					Packet p = new Packet(eheader, name);
					p.setMessageType("Name");
					p.setMaxHop(MAX_HOPS);
					p.getHeader().setType(PacketHeader.TYPE_DAT);
					sendPacket(p);
				}
				try {
					Thread.sleep(BROADCAST_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
}
