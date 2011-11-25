package com.example.adhocsocial;

import java.security.KeyStore.Entry;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import android.util.Log;

/*
 * HopList keeps a list of MAC addresses and a list of hop lengths it took for 
 * a packet to be received for the last 2 minutes of data.
 */

public class HopList {
	private static final String TAG = "Adhoc";
	//number of ms a hop counter becomes stale
	protected static final int STALE_MS = 120;
	//Hash table with key = MAC address; value = Queue of hop count & time
	HashMap<String, Queue<HopEntry>> nodeList = new HashMap<String, Queue<HopEntry>>();
	private double lastClean;
	private volatile boolean lock;
	private static String myAddr;
	private Thread cleanupThread;
	private boolean keepRunning;
	
	public HopList(){
		lastClean = -10000;
		lock = false;
		keepRunning = true;
		cleanupThread = new Thread(cleanup);
		cleanupThread.start();
	}
	
	protected void finalize(){
		keepRunning = false;
	}
	
	/*
	 * The cleanup will run in the background every 2 minutes but only if a 
	 * cleanup hasn't been executed within the last minute.
	 */
	private Runnable cleanup = new Runnable(){
		public void run(){
			while(keepRunning){
				if (lastClean + 60 < TimeKeeper.getSeconds()){
					cleanList();
				}
				try {
					Thread.sleep(STALE_MS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	private void retrieveLock(){
		while(lock){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		lock = true;
	}
	
	private void releaseLock(){
		lock = false;
	}
	
	//reads a packet header and stores the hop info into the list
	public void addPacket(PacketHeader ph){
		retrieveLock();
		String source = ph.getEathrnetHeader().getSource();
		Integer hopCount = ph.getCurrentHop();
		Double time = TimeKeeper.getSeconds();
		int id = ph.getPacketID();
		HopEntry newValue = new HopEntry(id, hopCount, time);
		Queue<HopEntry> listQueue = (Queue<HopEntry>)nodeList.get(source);
		Log.i(TAG, "Adding packet to hopList");
		if (listQueue == null){
			listQueue = new LinkedList<HopEntry>();
			listQueue.add(newValue);
			nodeList.put(source, listQueue);
			Log.i(TAG, "Unique hop entry saved");
		}
		else{
			listQueue.add(newValue);
			Log.i(TAG, "Hop entry added");
		}
		releaseLock();
	}
	
	//removes stale data from the list
	private void cleanList(){
		if (nodeList.isEmpty()) return;
		int cleanCount = 0;
		retrieveLock();
		int count = nodeList.size();
		HopEntry entry;
		Queue<HopEntry> listQueue;
		Iterator<Map.Entry<String, Queue<HopEntry>>> iterator = nodeList.entrySet().iterator();
		Map.Entry<String, Queue<HopEntry>> itEntry;
		for (int i = 0; i < count; i++){
			itEntry = iterator.next();
			listQueue = itEntry.getValue();
			entry = listQueue.peek();
			while (entry != null && entry.getTime() + STALE_MS < TimeKeeper.getSeconds()){
				listQueue.remove();
				entry = listQueue.peek();
				cleanCount++;
			}
			if (listQueue.isEmpty()){
				nodeList.remove(itEntry.getKey());
			}
		}
		lastClean = TimeKeeper.getSeconds();
		releaseLock();
		Log.i(TAG, Integer.toString(cleanCount)+ " hop entries cleaned");
	}
	
	//searches through entries for the given address and returns the minimum hop count of that list
	public int getMinHops(String addr){
		if (addr.equals(myAddr)) return 0;
		if (!nodeList.containsKey(addr)) return -1;
		retrieveLock();
		//only clean once every 5 seconds - speeds things up
		if (lastClean + 5 < TimeKeeper.getSeconds()){
			cleanList();
			if (!nodeList.containsKey(addr)) return -1;
		}
		int minHops = 1000000000;
		Queue<HopEntry> listQueue = nodeList.get(addr);
		HopEntry currentObj;
		int count = listQueue.size();
		for (int i = 0; i < count; i++){
			currentObj = listQueue.remove();
			if (currentObj.getHopCount() < minHops)
				minHops = currentObj.getHopCount();
			listQueue.add(currentObj);
		}
		releaseLock();
		return minHops;
	}
	
	//gets minimum hops for last packet received
	public int getMinLastPacketHops(String addr){
		if (addr.equals(myAddr)) return 0;
		if (!nodeList.containsKey(addr)) return -1;
		retrieveLock();
		int minHops=1000000000;
		int packetID=-1;
		Queue<HopEntry> listQueue = nodeList.get(addr);
		HopEntry currentObj;
		int count = listQueue.size();
		//get the last packetID
		for (int i = 0; i < count; i++){
			currentObj = listQueue.remove();
			packetID = currentObj.getPacketID();
			minHops = currentObj.getHopCount();
			listQueue.add(currentObj);
		}
		//get min hop count for the last packetID
		for (int i = 0; i < count; i++){
			currentObj = listQueue.remove();
			if (currentObj.getPacketID() == packetID && currentObj.getHopCount() < minHops)
				minHops = currentObj.getHopCount();
			listQueue.add(currentObj);
		}
		releaseLock();
		return minHops;
	}
	
	//gets minimum hops for given packet ID and source addr
	public int getMinPacketHops(String addr, int packetID){
		if (addr.equals(myAddr)) return 0;
		if (!nodeList.containsKey(addr)) return -1;
		retrieveLock();
		int minHops=1000000000;
		Queue<HopEntry> listQueue = nodeList.get(addr);
		HopEntry currentObj;
		int count = listQueue.size();
		//get min hop count for the last packetID
		for (int i = 0; i < count; i++){
			currentObj = listQueue.remove();
			if (currentObj.getPacketID() == packetID && currentObj.getHopCount() < minHops)
				minHops = currentObj.getHopCount();
			listQueue.add(currentObj);
		}
		releaseLock();
		return minHops;
	}
	
	//sets my address so if my address is asked for in the above function, 0 will always be returned
	public static void setMyAddr(String addr){
		myAddr = addr;
	}
	
	private class HopEntry{
		int packetID;
		int hopCount;
		double time;
		
		public HopEntry(int id, int hops, double time){
			this.packetID = id;
			this.hopCount = hops;
			this.time = time;
		}
		
		public int getPacketID(){
			return packetID;
		}
		
		public int getHopCount(){
			return hopCount;
		}
		
		public double getTime(){
			return time;
		}
	}
}
