package com.example.adhocsocial;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import android.text.format.Time;
import android.util.Log;

public class Logger {
	protected static final String TAG = "Logger";
	protected static final String LOG_PATH = "/sdcard";
	protected static final String LOG_NAME = "Adhoc-Social_Log.txt";
	
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
				Log.e(TAG, "couldnt create log");
				return false;
			}
        }
        
        started = true;
        writeLine("\n\n-------------------------------------\n" +
        			  "Logging started  " + getTime() +
        			  "\n-------------------------------------\n\n");
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
	
	public static boolean writePacketReceived(Packet p){
		boolean success = true;
		Time now = new Time();
        now.setToNow();
        success = success && writeLine("->Packet RECEIVED " + getTime() + "\n");
        success = success && writeLine(p.toString() + "\n");
        return success;
	}
	
	public static boolean writePacketSent(Packet p){
		boolean success = true;
		Time now = new Time();
        now.setToNow();
        success = success && writeLine("<-Packet SENT " + getTime() + "\n");
        success = success && writeLine(p.toString() + "\n");
        return success;
	}
	
	public static boolean writePacket(Packet p){
		boolean success = true;
		Time now = new Time();
        now.setToNow();
        success = success && writeLine("Packet " + getTime() + "\n");
        success = success && writeLine(p.toString() + "\n");
        return success;
	}
}
