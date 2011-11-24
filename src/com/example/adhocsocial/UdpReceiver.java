/*
 * File: UdpReceiver.java
 * 
 * Copyright (C) 2010 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package com.example.adhocsocial;

//This class was edited from the original class created for the Posit mobile project

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

//import org.hfoss.posit.rwg.RwgReceiver;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;

/**
 * Class running as a separate thread, and responsible for receiving data packets over the UDP protocol.
 * @author Rabie
 * Adapted from:
 * @see  http://code.google.com/p/adhoc-on-android/
 *
 */
public class UdpReceiver implements Runnable {
	private static final String TAG = "Adhoc";

	private DatagramSocket mDatagramSocket;
	//private MulticastSocket mDatagramSocket;

	private volatile boolean keepRunning = true;
	private Thread udpReceiverthread;
	
	private DatagramPacket packet;
	private Packet msgPacket;
	
	private volatile Queue<Packet> sendQueue;
	private volatile Queue<Packet> receiveQueue;
	private Queue<Object[]> receivedQueue = new LinkedList<Object[]>();
	private TimeKeeper time;
	private boolean timerOwned = false;
	
	private static String myAddress="";

	public UdpReceiver(Queue<Packet> sendQueue, Queue<Packet> receiveQueue) throws SocketException, UnknownHostException, BindException {
		TimeKeeper t = new TimeKeeper(100);
		timerOwned = true;
		int port = AdhocService.DEFAULT_PORT_BCAST;
		mDatagramSocket = new DatagramSocket(port);
		mDatagramSocket.setSoTimeout(0);            // Infinite timeout;
		this.sendQueue = sendQueue;
		this.receiveQueue = receiveQueue;
		this.time = t;
	}

	public UdpReceiver(Queue<Packet> sendQueue, Queue<Packet> receiveQueue, TimeKeeper time) throws SocketException, UnknownHostException, BindException {
		//this.parent = parent;
		int port = AdhocService.DEFAULT_PORT_BCAST;
		mDatagramSocket = new DatagramSocket(port);
		mDatagramSocket.setSoTimeout(0);            // Infinite timeout
		//mDatagramSocket.connect(InetAddress.getByName("192.168.2.255"), 8888);
		this.sendQueue = sendQueue;
		this.receiveQueue = receiveQueue;
		this.time = time;
		timerOwned = false;
	}

	public void startThread(){
		keepRunning = true;
		udpReceiverthread = new Thread(this);
		udpReceiverthread.start();
		
		receivedQueue.clear();
		if (timerOwned)
			time.startTimer();
	}

	public void stopThread(){
		keepRunning = false;
		mDatagramSocket.close();
		udpReceiverthread.interrupt();
		
		if (timerOwned)
			time.stopTimer();
	}

	public void run(){
		while(keepRunning) {
			try {
				// 52kb buffer
				//byte[] buffer = new byte[AdhocService.MAX_PACKET_SIZE];
				byte[] buffer = new byte[52000];
				packet = new DatagramPacket(buffer,buffer.length);				

				mDatagramSocket.receive(packet);  // This blocks indefinitely
				
				//A packet has been received!
				
				String ip = packet.getAddress().toString();
				int port = packet.getPort();
			    Log.i(TAG, " Received packet socket addr= " + packet.getSocketAddress().toString());
				
				Log.i(TAG, " updReceiver received a packet ip=" + ip + " port= " + port);

			    byte[] payload = new byte[packet.getLength()];

			    System.arraycopy(packet.getData(), 0, payload, 0, packet.getLength());

			    msgPacket = Packet.readFromBytes(payload);
			    
			    /*
			     * If the packet we receive is for us, put in receive queue
				 * If the packet we receive is not for us, put in send queue (to be forwarded)
				 */
			    String s = msgPacket.getEthernetHeader().getSource();
			    if (!msgPacket.getEthernetHeader().getSource().equals(myAddress) && 
			    		!packetReceived(msgPacket.getHeader())){
				    if(msgPacket.getEthernetHeader().getDestination().equals("")){
				    	//This is a broadcast message
				    	addToReceiveQueue(msgPacket);
				    	msgPacket.incrementHop();
				    	sendQueue.add(msgPacket);
				    }
				    else
				    {
						if (msgPacket.getEthernetHeader().getDestination().equals(myAddress))
							addToReceiveQueue(msgPacket);
						else{
							msgPacket.incrementHop();
							sendQueue.add(msgPacket);
						}
				    }
			    }
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Log.i(TAG, " Exiting the receiver loop");
	}
	
	private void addToReceiveQueue(Packet p){
		Logger.writePacketReceived(p);
		receiveQueue.add(p);
		Object[] pair = new Object[2];
		pair[0] = p.getHeader();
		pair[1] = time.getSeconds();
		receivedQueue.add(pair);
	}
	
	public static void setMyAddress(String addr){
		myAddress = addr;
	}
	
	private boolean packetReceived(PacketHeader p){
		refreshReceivedList();
		boolean result = false;
		int queueLength = receivedQueue.size();
		if (queueLength <= 0) return false;
		Object check[];
		for (int i=0; i < queueLength; i++){
			check = receivedQueue.remove();
			if (((PacketHeader)check[0]).equals(p)){
				result =  true;
			}
			receivedQueue.add(check);
		}
		return result;
	}
	
	private void refreshReceivedList(){
		Object[] h = receivedQueue.peek();
		if (h == null)
			return;
		while(((Double)h[1] + 30.0) < time.getSeconds()){
			receivedQueue.remove();
			h = receivedQueue.peek();
			if (h == null)
				return;
		}
	}
}
