package com.example.adhocsocial;

public class TimeKeeper implements Runnable{
	private int msPerTick;
	private volatile int currentTicks;
	private volatile boolean keepRunning = false;
	private Thread timerThread;
	
	public TimeKeeper(){
		currentTicks = 0;
		msPerTick = 100;
	}
	
	public TimeKeeper(int msPerTick){
		currentTicks = 0;
		this.msPerTick = msPerTick;
	}
	
	public void startTimer(){
		keepRunning = true;
		timerThread = new Thread(this);
		timerThread.start();
	}
	
	public void stopTimer(){
		keepRunning = false;
		timerThread.interrupt();
	}
	
	public int getTicks(){
		return currentTicks;
	}
	
	public double getSeconds(){
		return currentTicks*msPerTick/1000.0;
	}
	
	public int getMsPerTick(){
		return msPerTick;
	}
	
	public void run(){
		while(keepRunning){
			try {
				Thread.sleep(msPerTick);
				currentTicks++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
