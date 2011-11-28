package com.example.adhocsocial;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

//PullDisc will ask for user data on intervals
public class PullDisc extends DiscNodes {
	protected static final int MAX_HOPS = 5;
	protected static final int PULL_MS = 30000;
	private boolean keepRunning = false;
	private Thread pullThread;
	public PullDisc(HopList hopList, Queue<Packet> sendQueue,HashMap<String,Queue<Packet>> receiveQueue, Buddylist list, String myName){
		super(hopList, sendQueue, receiveQueue,list, myName);
		keepRunning = true;
		pullThread = new Thread(pullDat);
		pullThread.start();
	}
	protected void finalize(){
		keepRunning = false;
	}
	private Runnable pullDat = new Runnable(){
		@Override
		public void run() {
			Packet p;
			Packet ack;
			int c;
			EthernetHeader eheader = new EthernetHeader(myAddress);
			EthernetHeader ackHeader;
			while(keepRunning){
				p = new Packet(eheader, myName);
				p.setMaxHop(MAX_HOPS);
				p.setMessageType("Pull");
				p.getHeader().setType(PacketHeader.TYPE_REQ);
				sendPacket(p);
				
				c = 0;
				while(receiveQueue.containsKey("Pull") && !receiveQueue.get("Pull").isEmpty()){
					p = receiveQueue.get("Pull").remove();
					//DO SOMETHING WITH THIS PACKET HERE
					ackHeader = new EthernetHeader(myAddress,p.getEthernetHeader().getSource());
					ack = new Packet(ackHeader, myName);
					ack.setMessageType("Name");
					ack.setMaxHop(p.getMaxHop());
					ack.getHeader().setType(PacketHeader.TYPE_ACK);
					sendPacket(ack);
				}
				
				try {
					Thread.sleep(PULL_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
}
