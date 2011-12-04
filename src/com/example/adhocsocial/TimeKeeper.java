package com.example.adhocsocial;

public class TimeKeeper{
	public static final int MS_PER_TICK = 10;
	private static volatile int currentTicks = 0;
	private static volatile boolean keepRunning = false;
	private static Thread timerThread;
	
	public static void startTimer(){
		if (keepRunning) return;
		keepRunning = true;
		timerThread = new Thread(runMe);
		timerThread.start();
	}
	
	public static void stopTimer(){
		keepRunning = false;
		timerThread.interrupt();
	}
	
	public static int getTicks(){
		return currentTicks;
	}
	
	public static double getSeconds(){
		return currentTicks*MS_PER_TICK/1000.0;
	}
	
	public static int getMsPerTick(){
		return MS_PER_TICK;
	}
	
	public static Runnable runMe = new Runnable(){
		public void run(){
			while(keepRunning){
				try {
					Thread.sleep(MS_PER_TICK);
					currentTicks++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
}
