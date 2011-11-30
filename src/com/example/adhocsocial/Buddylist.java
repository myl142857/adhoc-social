package com.example.adhocsocial;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

public class Buddylist{
//attributes
	private static final int UPDATE_INTERVAL_MS = 100;
	private static final double STALE_BUDDY_S = 120;
	private volatile Queue<Packet> sendQueue;
	private static String myAddress;
	Hashtable<String, Buddy> bl;
	Thread clearThread;
	boolean keepRunning = false;
	Runnable update = new Runnable(){
		public void run(){
			while(keepRunning){
				updateList(STALE_BUDDY_S);
				try {
					Thread.sleep(UPDATE_INTERVAL_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	
//constructors
	public Buddylist(Queue<Packet> sendQueue){
		bl = new Hashtable<String, Buddy>();
		this.sendQueue = sendQueue;
		keepRunning = true;
		clearThread = new Thread(update);
		clearThread.start();
	}
	
//methods
	protected void finalize(){
		keepRunning = false;
	}
	public int add(String address, String name, int hops){
		//take address and instantiate a Buddy
		Buddy buddy = new Buddy(address, name, hops);
		//address = hashtable key
		//Buddy = hashtable value
		bl.put(address, buddy);		
		//return 1 upon successful add
		return 1;
	}
	
	public Buddy remove (String address){
		Buddy temp = bl.remove(address);
		return temp;
	}
	
	/*public Hashtable<String, Buddy> getList (){
		return bl;
	}*/
	
	public LinkedList<Buddy> getList(){
		LinkedList<Buddy> list = new LinkedList<Buddy>();
		Buddy buddy;
		Enumeration <Buddy>  valsEnum = bl.elements();	
		while (valsEnum.hasMoreElements()){
			buddy = valsEnum.nextElement();
			list.add(buddy);		
		}
		return list;
	}
	
	public boolean inList (String address){
		return bl.containsKey(address);
	}
	
	public boolean inList (Buddy buddy){		
		return bl.contains(buddy);
	}
	
	public Buddy getBuddy (String address){
		//if element does not exist in hashtable, returns null		
		Buddy b = bl.get(address);
		if (b == null)
			b = new Buddy();//instance with default constructor
		return b;
		
	}
	
	public void updateBuddy (String address){
		Buddy b = bl.get(address);
		b.update();
	}
	
	public void updateBuddy (String address, String name){
		Buddy b = bl.get(address);
		b.setName(name);
		b.update();
	}
	
	public boolean nameChanged(String address, String name){
		Buddy b = bl.get(address);
		return (!name.equals(b.getName()));
	}
	
	public void updateList(double refreshRate){
		
		String address;
		Buddy temp, buddy;
		double timePassed;
		
		//Enumeration <String> keysEnum = bl.keys();
		Enumeration <Buddy>  valsEnum = bl.elements();	
		
		/*
		do{
			address = keysEnum.nextElement();
			temp = bl.get(address);
			timePassed = TimeKeeper.getSeconds() - temp.getLastUpd();
			if (timePassed > refreshRate)
				this.remove(address);
			else
				temp.update();
		}while (keysEnum.hasMoreElements());
		*/
		
		
		while (valsEnum.hasMoreElements()){
			buddy = valsEnum.nextElement();
			timePassed = TimeKeeper.getSeconds() - buddy.getLastUpd();
			if (timePassed > refreshRate){
				this.remove(buddy.getAddress());
				Log.i("Buddylist","Removed stale buddy");
			}
			else if (timePassed > (refreshRate/2.0) && 
					buddy.getLastPinged() + 10.0 < TimeKeeper.getSeconds()){
				//ping every 10 seconds for the last half of the timeout
				ping(buddy.getAddress());
				Log.i("Buddylist","pinged buddy");
			}
		}
	
	}
	
	public int ping (String address){
		if (!inList(address)) return -1;
		getBuddy(address).setLastPinged(TimeKeeper.getSeconds());
		//make pingPacket
		EthernetHeader header = new EthernetHeader(myAddress,address);
		Packet pingPacket = new Packet(header,"");
		pingPacket.getHeader().setType(PacketHeader.TYPE_PING);
		pingPacket.setMessageType("ping");
		pingPacket.setMaxHop(DiscNodes.MAX_HOPS);
		
		//give pingPacket to UDPSender
		sendPacket(pingPacket);
		
		return pingPacket.getPacketID();
	}
	
	protected boolean sendPacket(Packet p){
		sendQueue.add(p);
		return true;
	}
	
	public static void setMyAddress(String addr){
		myAddress = addr;
	}
}
