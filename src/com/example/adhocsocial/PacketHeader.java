package com.example.adhocsocial;

public class PacketHeader {
	//unique identifier for each packet from me
	private int packetID;
	private static int lastID = -1;
	
	private EthernetHeader ethrHeader;  //contains source and destination
	
	private int maxHop;		//The maximum number of hops this packet is allowed to propagate
	private int currentHop; //The current hop count
	private int maxTime;	//The maximum amount of time this message can stay in the tuple space (Not used)
	private String type;	//message type
	private String application; //application name for this message	
	
	private double sentTime;
	
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
				type.equals(p.type) && application.equals(p.application));
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
	
	public void setSentTime(double t){
		sentTime = t;
	}
	
	public double getSentTime(){
		return sentTime;
	}
}
