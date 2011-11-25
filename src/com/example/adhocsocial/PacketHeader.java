package com.example.adhocsocial;

import java.io.Serializable;

public class PacketHeader implements Serializable {
	public static final String TYPE_ACK = "ack";
	public static final String TYPE_DAT = "dat";
	public static final String TYPE_REQ = "req";
	
	//unique identifier for each packet from me
	private int packetID;
	private static int lastID = -1;
	
	private EthernetHeader ethrHeader;  //contains source and destination
	
	private int maxHop;		//The maximum number of hops this packet is allowed to propagate
	private int currentHop; //The current hop count
	private int maxTime;	//The maximum amount of time this message can stay in the tuple space (Not used)
	private String type;	//message type
	private String application; //application name for this message	
	
	public PacketHeader(){
		setID();
		maxHop = 1;
		currentHop = 0;
		maxTime = 0;
		type = "";
		application = "";
	}
	
	public boolean equals(PacketHeader p){
		return (ethrHeader.equals(p.ethrHeader) && maxHop == p.maxHop &&
				currentHop == p.currentHop && maxTime == p.maxTime &&
				type.equals(p.type) && application.equals(p.application) &&
				packetID == p.packetID);
	}
	
	private void setID(){
		packetID = ++lastID;
	}
	
	public int getPacketID(){
		return packetID;
	}
	
	public void setEthrHeader(EthernetHeader e){
		ethrHeader = e;
	}
	
	public EthernetHeader getEathrnetHeader(){
		return ethrHeader;
	}
	
	public void setMaxHop(int h){
		maxHop = h;
	}
	
	public int getMaxHop(){
		return maxHop;
	}
	
	public void incrementHopCount(){
		currentHop++;
	}
	
	public int getCurrentHop(){
		return currentHop;
	}
	
	public void setMaxTime(int t){
		maxTime = t;
	}
	
	public int getMaxTime(){
		return maxTime;
	}
	
	public void setType(String t){
		type = t;
	}
	
	public String getType(){
		return type;
	}
	
	public void setApplication(String a){
		application = a;
	}
	
	public String getApplication(){
		return application;
	}
	
	public int getSize(){
		//4 integers so 4*4 bytes to start out with
		int size = 16;
		size += type.length()*2;
		size += application.length()*2;
		size += ethrHeader.getSize();
		return size;
	}
	
	public String toString(){
		String s = "";
		s += ethrHeader.toString();
		s += "  Packet ID: " + Integer.toString(packetID) + "\r\n";
		s += "  Max Hop: " + Integer.toString(maxHop) + "\r\n";
		s += "  Current Hop: " + Integer.toString(currentHop) + "\r\n";
		s += "  Max Time: " + Integer.toString(maxTime) + "\r\n";
		s += "  Type: " + type + "\r\n";
		s += "  Application: " + application + "\r\n";
		return s;
	}
	
	public String toXlsString(){
		String s = "";
		s += ethrHeader.toXlsString();
		s += "\t" + Integer.toString(packetID);
		s += "\t" + Integer.toString(maxHop);
		s += "\t" + Integer.toString(currentHop);
		s += "\t" + Integer.toString(maxTime);
		s += "\t" + type;
		s += "\t" + application;
		return s;
	}
}
