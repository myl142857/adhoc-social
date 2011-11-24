package com.example.adhocsocial;
import java.util.Hashtable;

public class Buddylist{
//attributes
	Hashtable<String, Buddy> bl;
	
	
//constructors
	public Buddylist(){
		bl = new Hashtable();
	}
	
//methods
	public int Add(String address, String name, int hops){
		//take address and instantiate a Buddy
		Buddy buddy = new Buddy(address, name, hops);
		//address = hashtable key
		//Buddy = hashtable value
		bl.put(address, buddy);		
		//return 1 upon sucessful add
		return 1;
	}
	
	public Buddy Remove (String address){
		Buddy temp = bl.remove(address);
		return temp;
	}
	
	public boolean Ping (String address, double time){
		double t = TimeKeeper.getSeconds();
		boolean ACKRecd = false;
		//make pingPacket
		
		//give pingPacket to UDPSender
		
		try{
		while(TimeKeeper.getSeconds()-t < time){
			Thread.sleep(10);
			if (ACKRecd)
				return true;
		}
			return false;
		}
		catch(Exception e){return false;}
		
	}
	
	public void Chat (){
	}

}
