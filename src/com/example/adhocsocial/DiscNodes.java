package com.example.adhocsocial;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

public abstract class DiscNodes {
	public static final int MAX_HOPS = 5;
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
			while(keepRunning){
				loadBuddies();
				try {
					Thread.sleep(RECEIVE_CHECK_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	public boolean loadBuddies(){
		Packet p;
		String source;
		while(receiveQueue.containsKey("Name") && !receiveQueue.get("Name").isEmpty()){
			p = receiveQueue.get("Name").remove();
			//DO SOMETHING WITH THIS PACKET HERE
			source = p.getHeader().getEathrnetHeader().getSource();
			if (list.inList(source)){
				list.updateBuddy(source, p.getMessage());
			}
			else{
				list.add(source, p.getMessage(), hopList.getMinPacketHops(source, p.getHeader().getPacketID()));
			}
		}
		if (myAddress != null && !myAddress.equals("")){
			EthernetHeader ackHeader;
			Packet ack;
			while(receiveQueue.containsKey("Pull") && !receiveQueue.get("Pull").isEmpty()){
				p = receiveQueue.get("Pull").remove();
				//DO SOMETHING WITH THIS PACKET HERE
				ackHeader = new EthernetHeader(myAddress,p.getEthernetHeader().getSource());
				ack = new Packet(ackHeader, myName);
				ack.setMessageType("Name");
				ack.setMaxHop(p.getMaxHop()+1);
				ack.getHeader().setType(PacketHeader.TYPE_ACK);
				sendPacket(ack);
				
				source = p.getHeader().getEathrnetHeader().getSource();
				if (list.inList(source)){
					list.updateBuddy(source, p.getMessage());
				}
				else{
					list.add(source, p.getMessage(), hopList.getMinPacketHops(source, p.getHeader().getPacketID()));
				}
			}
			
			while(receiveQueue.containsKey("ping") && !receiveQueue.get("ping").isEmpty()){
				p = receiveQueue.get("ping").remove();
				//return pong message
				ackHeader = new EthernetHeader(myAddress,p.getEthernetHeader().getSource());
				ack = new Packet(ackHeader, "");
				ack.setMaxHop(p.getMaxHop()+1);
				ack.setMessageType("pong");
				ack.getHeader().setType(PacketHeader.TYPE_PONG);
				sendPacket(ack);
				
				source = p.getHeader().getEathrnetHeader().getSource();
				if (list.inList(source)){
					list.updateBuddy(source);
				}
			}
			
			while(receiveQueue.containsKey("pong") && !receiveQueue.get("pong").isEmpty()){
				p = receiveQueue.get("pong").remove();
				source = p.getHeader().getEathrnetHeader().getSource();
				if (list.inList(source)){
					list.updateBuddy(source);
				}
				Log.i("DiscNodes","pong!");
			}
		}
		return true;
	}
	
	protected boolean sendPacket(Packet p){
		sendQueue.add(p);
		return true;
	}
	public static void setMyAddress(String addr){
		myAddress = addr;
	}
}
