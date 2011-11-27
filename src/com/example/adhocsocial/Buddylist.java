package com.example.adhocsocial;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import android.util.Log;

public class Buddylist{
//attributes
	private static final int UPDATE_INTERVAL_MS = 100;
	private static final double STALE_BUDDY_S = 120;
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
	public Buddylist(){
		bl = new Hashtable<String, Buddy>();
		//For now, dont run the cleanup thread
		/*keepRunning = true;
		clearThread = new Thread(update);
		clearThread.start();*/
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
		}
	
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
