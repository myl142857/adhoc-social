package com.example.adhocsocial;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;

public class Logger {
	protected static final String TAG = "Logger";
	protected static final String LOG_PATH = "/sdcard";
	protected static final String LOG_NAME = "Adhoc-Social_Log.txt";
	protected static final String XLS_LOG_NAME = "Adhoc-Social_Log.xls";
	protected static final String BUDDY_LOG_NAME = "Adhoc-Social_Buddies.htm";
	
	private static boolean started = false;
	private static BufferedWriter out;
	
	public static boolean startLogger(){
		if (started) return true;
		java.io.File file = new java.io.File(LOG_PATH , LOG_NAME);
        if (!file.exists()) {
        	try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "couldnt create log 1");
				return false;
			}
        }
        
        java.io.File file2 = new java.io.File(LOG_PATH , XLS_LOG_NAME);
        if (!file2.exists()) {
        	try {
				file2.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "couldnt create log 2");
				return false;
			}
        }
        
        java.io.File file3 = new java.io.File(LOG_PATH , BUDDY_LOG_NAME);
        if (!file3.exists()) {
        	try {
				file3.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "couldnt create log 3");
				return false;
			}
        }
        
        started = true;
        writeLine("\r\n\r\n-------------------------------------\r\n" +
        			  "Logging started  " + getTime() +
        			  "\r\n-------------------------------------\r\n\r\n");
		 
        writeXlsLine("\r\n\r\nTime\tMS\tReceive Size\tSent From\tSource\tDestination\tPacket ID"+
				"\tMax Hop\tCurrent Hop\tMax Time\tType\tApplication\t"+
				"Message Type\tMessage"+
				"\t\tSent Size\tSent From\tSource\tDestination\tPacket ID"+
				"\tMax Hop\tCurrent Hop\tMax Time\tType\tApplication\t"+
				"Message Type\tMessage\r\n");
		
        writeBuddyLine("<br><br>\r\n\r\nSTART<br>------------<br>\r\n");
        
        Log.i(TAG, "Logger started");
		return true;
	}
	
	private static String getTime(){
		Time now = new Time();
        now.setToNow();
		return Integer.toString(now.month) +
		  "-" + Integer.toString(now.monthDay) + "-" + Integer.toString(now.year) +
		  " " + Integer.toString(now.hour) + ":" + Integer.toString(now.minute) + ":" +
		  Integer.toString(now.second);
	}
	
	public static boolean writeLine(String line){
		if (started){
			try {
				// Create file 
	        	FileWriter fstream = new FileWriter(LOG_PATH+"/"+LOG_NAME,true);
	        	out = new BufferedWriter(fstream);
				out.write(line);
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG,"Could not write to Log");
			}
			return true;
		}
		else
			return false;
	}
	
	public static boolean writeXlsLine(String line){
		if (started){
			try {
				// Create file 
	        	FileWriter fstream = new FileWriter(LOG_PATH+"/"+XLS_LOG_NAME,true);
	        	out = new BufferedWriter(fstream);
				out.write(line);
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG,"Could not write to Log");
			}
			return true;
		}
		else
			return false;
	}
	
	public static boolean writeBuddyLine(String line){
		if (started){
			try {
				// Create file 
	        	FileWriter fstream = new FileWriter(LOG_PATH+"/"+BUDDY_LOG_NAME,true);
	        	out = new BufferedWriter(fstream);
				out.write(line);
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG,"Could not write to Log");
			}
			return true;
		}
		else
			return false;
	}
	
	public static boolean writePacketReceived(Packet p){
		boolean success = true;
		Time now = new Time();
        now.setToNow();
        success = success && writeLine("->Packet RECEIVED " + getTime() + ", " + Integer.toString(TimeKeeper.getTicks()*TimeKeeper.MS_PER_TICK) + "\r\n" + p.toString() + "\r\n");

        success = success && writeXlsLine(getTime()+ "\t" + Integer.toString(TimeKeeper.getTicks()*TimeKeeper.MS_PER_TICK)+"\t"+p.toXlsString()+"\r\n");
        return success;
	}
	
	public static boolean writePacketSent(Packet p){
		boolean success = true;
		Time now = new Time();
        now.setToNow();
        success = success && writeLine("<-Packet SENT " + getTime() + ", " + Integer.toString(TimeKeeper.getTicks()*TimeKeeper.MS_PER_TICK) + "\r\n" + p.toString() + "\r\n");
        
        success = success && writeXlsLine(getTime()+ "\t" + Integer.toString(TimeKeeper.getTicks()*TimeKeeper.MS_PER_TICK)+"\t\t\t\t\t\t\t\t\t\t\t\t\t\t"+p.toXlsString()+"\r\n");
        return success;
	}
	
	public static boolean writePacket(Packet p){
		boolean success = true;
		Time now = new Time();
        now.setToNow();
        success = success && writeLine("Packet " + getTime()+ "\t" + Integer.toString(TimeKeeper.getTicks()*TimeKeeper.MS_PER_TICK) + ", " + Integer.toString(TimeKeeper.getTicks()) + "\r\n" + p.toString() + "\r\n");

        return success;
	}
	
	public static boolean writeBuddy(Buddy b, String color){
		String line = "<font color=\"" + color + "\">";
		line += "Name: " + b.getName() +"<br>\r\n";
		line += "Address: " + b.getAddress() + "<br>\r\n";
		line += "Updated At: " + Double.toString(b.getLastUpd()) + "<br>\r\n";
		line += "Pinged At: " + Double.toString(b.getLastPinged()) + "<br>\r\n";
		line += "</font><br>-------------\r\n";
		return writeBuddyLine(line);
	}
	
	public static boolean writeBuddyAdded(Buddy b){
		return writeBuddy(b, "green");
	}
	
	public static boolean writeBuddyRemoved(Buddy b){
		return writeBuddy(b, "red");
	}
	
	public static boolean writeBuddyPinged(Buddy b){
		return writeBuddy(b, "black");
	}
	
	public static boolean writeBuddyUpdated(Buddy b){
		return writeBuddy(b, "blue");
	}
	
	public static boolean writeWhoIsThere(){
		return writeBuddyLine("--------------------<br>[  Who is there?  ]<br>--------------------<br>");
	}
}
