package com.example.adhocsocial;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

public class Chat {
	protected static final int MAX_MESSAGE_HOPS = 6;
	protected static final int MAX_MESSAGES = 4;
	protected static final int MESSAGE_CHECK_MS = 10;
	protected static final int PING_AT_S = 30;
	protected static final int PING_INTERVAL_S = 10;
	private static String myAddress;
	private Buddylist list;
	private LinkedList<Buddy> chatBuddies;
	private LinkedList<String> messages = new LinkedList<String>();
	protected volatile Queue<Packet> sendQueue;
	protected volatile HashMap<String,Queue<Packet>> receiveQueue;
	private String myName;
	private boolean updated = false;
	private Thread chatThread;
	private boolean keepRunning;
	
	public Chat(Buddylist list, Queue<Packet> sendQueue, HashMap<String,Queue<Packet>> receiveQueue, String myName){
		this.list = list;
		this.sendQueue = sendQueue;
		this.myName = myName;
		this.receiveQueue = receiveQueue;
		chatBuddies = new LinkedList<Buddy>();
		
		keepRunning = true;
		chatThread = new Thread(messageCheck);
		chatThread.start();
	}
	
	protected void finalize(){
		keepRunning = false;
	}
	
	//need to add buddies to list
	public boolean addBuddyToChat(Buddy b){
		if(list.inList(b)){
			chatBuddies.add(b);
		}
		return true;
	}
	
	public boolean removeBuddyFromChat(Buddy b){
		for (int i=0;i<chatBuddies.size();i++){
			if (chatBuddies.get(i).getAddress().equals(b.getAddress())){
				chatBuddies.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public static void setMyAddress(String addr){
		myAddress = addr;
	}
	
	private void sendPacket(Packet p){
		sendQueue.add(p);
	}
	
	public boolean sendText(String message){
		checkBuddiesAlive();
		if (chatBuddies.size()<=0) return false;
		EthernetHeader h;
		Packet p;
		int count = chatBuddies.size();
		for (int i = 0; i < count; i++){
			//need to check if buddy is still in list!
			h = new EthernetHeader(myAddress,chatBuddies.get(i).getAddress());
			p = new Packet(h, message);
			p.setMessageType("Message");
			p.getHeader().setType(PacketHeader.TYPE_DAT);
			p.setMaxHop(MAX_MESSAGE_HOPS);
			sendPacket(p);
		}
		addMessage(myName + ": " + message);
		return true;
	}
	
	/*private Runnable receiveText = new Runnable(){
		
	}*/
	
	public void checkBuddiesAlive(){
		if (chatBuddies.size()<=0) return;
		Buddy b;
		double timePassed;
		int c=0;
		while(c<chatBuddies.size()){
			b = chatBuddies.get(c);
			if(list.inList(b)){//buddy is in list
				//ping buddy if they are getting too old
				timePassed = TimeKeeper.getSeconds() - b.getLastUpd();
				if (timePassed >= PING_AT_S  && 
						b.getLastPinged() + PING_INTERVAL_S <= TimeKeeper.getSeconds()){
					//ping every 10 seconds for the last half of the timeout
					ping(b.getAddress());
					Log.i("Chat","pinged buddy");
				}
				c++;
			}
			else{
				addMessage(b.getName()+ " has disconnected.");
				chatBuddies.remove(c);
			}
		}
	}
	
	private void addMessage(String s){
		if (messages.size() > (MAX_MESSAGES-1))
			messages.remove();
		messages.add(s);
		updated = true;
	}
	
	public String getMessages(){
		String s = "";
		if (messages.size() <= 0)
			return s;
		for (int i = 0; i<messages.size();i++){
			s+=messages.get(i);
			if (i != messages.size()-1)
				s+="\n";
		}
		return s;
	}
	
	public boolean isUpdated(){
		return updated;
	}
	
	public LinkedList<Buddy> getChatList(){
		return chatBuddies;
	}
	
	public int ping (String address){
		if (!list.inList(address)) return -1;
		list.getBuddy(address).setLastPinged(TimeKeeper.getSeconds());
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
	
	private Runnable messageCheck = new Runnable(){
		public void run(){
			Packet p;
			String name;
			Buddy b;
			while (keepRunning){
				checkBuddiesAlive();
				while (receiveQueue.containsKey("Message") && !receiveQueue.get("Message").isEmpty()){
					p = receiveQueue.get("Message").remove();
					b = list.getBuddy(p.getEthernetHeader().getSource());
					if (b==null)
						name = "null";
					else
						name = b.getName();
					addMessage(name+": "+p.getMessage());
				}
				try {
					Thread.sleep(MESSAGE_CHECK_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
}
