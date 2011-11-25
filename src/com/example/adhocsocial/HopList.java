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
	HashMap<String, Queue<Object[]>> nodeList = new HashMap<String, Queue<Object[]>>();
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
		Object[] newValue = new Object[2];
		newValue[0] = hopCount;
		newValue[1] = time;
		Queue<Object[]> listQueue = (Queue<Object[]>)nodeList.get(source);
		Log.i(TAG, "Adding packet to hopList");
		if (listQueue == null){
			listQueue = new LinkedList<Object[]>();
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
		Object[] entry;
		Queue<Object[]> listQueue;
		Iterator<Map.Entry<String, Queue<Object[]>>> iterator = nodeList.entrySet().iterator();
		Map.Entry<String, Queue<Object[]>> itEntry;
		for (int i = 0; i < count; i++){
			itEntry = iterator.next();
			listQueue = itEntry.getValue();
			entry = listQueue.peek();
			while (entry != null && (Double)entry[1] + STALE_MS < TimeKeeper.getSeconds()){
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
		Queue<Object[]> listQueue = nodeList.get(addr);
		Object[] currentObj;
		int count = listQueue.size();
		int h;
		for (int i = 0; i < count; i++){
			currentObj = listQueue.remove();
			if ((Integer)currentObj[0] < minHops)
				minHops = (Integer)currentObj[0];
			listQueue.add(currentObj);
		}
		releaseLock();
		return minHops;
	}
	
	//sets my address so if my address is asked for in the above function, 0 will always be returned
	public static void setMyAddr(String addr){
		myAddr = addr;
	}
}
