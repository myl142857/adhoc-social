package com.example.adhocsocial;

import java.util.LinkedList;
import java.util.Queue;

public class Chat {
	protected static final int MAX_MESSAGE_HOPS = 6;
	protected static final int MAX_MESSAGES = 5;
	private static String myAddress;
	private Buddylist list;
	private LinkedList<Buddy> chatBuddies;
	private LinkedList<String> messages = new LinkedList<String>();
	protected volatile Queue<Packet> sendQueue;
	private String myName;
	private boolean updated = false;
	
	public Chat(Buddylist list, Queue<Packet> sendQueue, String myName){
		this.list = list;
		this.sendQueue = sendQueue;
		this.myName = myName;
		chatBuddies = new LinkedList<Buddy>();
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
		int c=0;
		while(c<chatBuddies.size()){
			if(list.inList(chatBuddies.get(c))){//buddy is in list
				c++;
			}
			else{
				addMessage(chatBuddies.get(c).getName()+ " has disconnected.");
				chatBuddies.remove(c);
			}
		}
	}
	
	private void addMessage(String s){
		if (messages.size() > MAX_MESSAGES)
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
				s+="\r\n";
		}
		return s;
	}
	
	public boolean isUpdated(){
		return updated;
	}
	
	public LinkedList<Buddy> getChatList(){
		return chatBuddies;
	}
}
