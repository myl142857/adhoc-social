package com.example.adhocsocial;
public class Buddy{
	//attributes
	String address;
	String name;
	int minHops;
	double lastTimeUpdated;
	//constructors
	public Buddy(){
		address = "";
		name = "NoBuddy";
		minHops = 0;
		lastTimeUpdated = TimeKeeper.getSeconds();
	}
	
	public Buddy (String add, String n, int hops){
		address = add;
		name = n;
		minHops = hops;
		lastTimeUpdated = TimeKeeper.getSeconds();
	}
	//methods
	
	public String getAddress (){
		return address;
	}
	
	public String getName(){
		return name;
	}
	
	public void setHops(int h){
		minHops = h;
	}
	
	public int getHops(){
		return minHops;
	}
	
	public double getLastUpd(){
		return lastTimeUpdated;
	}
	
	public void update(){
		lastTimeUpdated = TimeKeeper.getSeconds();
	}
}