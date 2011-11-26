package com.example.adhocsocial;
import java.util.Enumeration;
import java.util.Hashtable;

public class Buddylist{
//attributes
	Hashtable<String, Buddy> bl;
	
	
//constructors
	public Buddylist(){
		bl = new Hashtable<String, Buddy>();
	}
	
//methods
	public int add(String address, String name, int hops){
		//take address and instantiate a Buddy
		Buddy buddy = new Buddy(address, name, hops);
		//address = hashtable key
		//Buddy = hashtable value
		bl.put(address, buddy);		
		//return 1 upon sucessful add
		return 1;
	}
	
	public Buddy remove (String address){
		Buddy temp = bl.remove(address);
		return temp;
	}
	
	public Hashtable<String, Buddy> getList (){
		return bl;
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
	
	public void updateList(double refreshRate){
		Enumeration<E> <String> keysEnum = bl.keys();
		Enumeration <Buddy>  valsEnum = bl.elements();
		
		String address = keysEnum.nextElement();
		Buddy buddy = valsEnum.nextElement();
		
		do{
			Buddy temp = bl.get(address);
			double timePassed = TimeKeeper.getSeconds() - temp.getLastUpd();
			if (timePassed > refreshRate)
				this.remove(address);				
		}while (keysEnum.hasMoreElements());
		
		do{
			
		}while (valsEnum.hasMoreElements());
		
	}
	
	/*
	public static boolean Pong(PingPacket P){
		
	}
	
	
	public static boolean Ping (String address, double time){
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
	*/

}
