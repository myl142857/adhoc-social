package com.example.adhocsocial;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public abstract class DiscNodes {
	protected static final int RECEIVE_CHECK_TIME = 100;
	protected static String myAddress="";
	protected Buddylist list;
	protected HopList hopList;
	protected volatile Queue<Packet> sendQueue;
	protected volatile HashMap<String,Queue<Packet>> receiveQueue;
	protected String myName;
	private Thread receiveThread;
	private boolean keepRunning = false;
	public DiscNodes(HopList hopList, Queue<Packet> sendQueue, HashMap<String,Queue<Packet>> receiveQueue, Buddylist list, String myName){
		this.list = list;
		this.hopList = hopList;
		this.sendQueue = sendQueue;
		this.myName = myName;
		this.receiveQueue = receiveQueue;
		keepRunning = true;
		receiveThread = new Thread(receive);
		receiveThread.start();
	}
	private Runnable receive = new Runnable(){
		public void run(){
			Packet p;
			int c;
			String source;
			while(keepRunning){
				c = 0;
				while(receiveQueue.containsKey("Name") && !receiveQueue.get("Name").isEmpty()){
					p = receiveQueue.get("Name").remove();
					//DO SOMETHING WITH THIS PACKET HERE
					source = p.getHeader().getEathrnetHeader().getSource();
					if (list.inList(source)){
						list.updateBuddy(source);
					}
					else{
						list.add(source, p.getMessage(), hopList.getMinPacketHops(source, p.getHeader().getPacketID()));
					}
				}
				try {
					Thread.sleep(RECEIVE_CHECK_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	protected boolean sendPacket(Packet p){
		sendQueue.add(p);
		return true;
	}
	public static void setMyAddress(String addr){
		myAddress = addr;
	}
}
