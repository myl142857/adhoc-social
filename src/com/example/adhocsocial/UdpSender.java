/*
 * File: UpdSender.java
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
import java.util.*;

//import org.hfoss.posit.rwg.RwgPacket;

import android.content.Context;
import android.net.wifi.WifiManager;
//import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;


/**
 * Sends packets from the protocol later over a UDP socket. Adapted from:
 * @see  http://code.google.com/p/adhoc-on-android/
 */
public class UdpSender implements Runnable{
	private static final String TAG = "Adhoc";
	private static final int RUN_INCREMENT = 10;

	private volatile boolean keepRunning = true;
	private Thread udpSenderthread;
	
	private volatile Queue<Packet> sendQueue;
	private Queue<Object[]> sentQueue = new LinkedList<Object[]>();
	
	private DatagramSocket datagramSocket;	
	private InetAddress group;
	
	private volatile int currentTicks;
	
	private static String myAddress="";
	
	public UdpSender(Queue<Packet> sendQueue) throws SocketException, UnknownHostException, BindException{
	    datagramSocket = new DatagramSocket(8881);
	    this.sendQueue = sendQueue;
	    currentTicks = 0;
	}
	
	public void startThread(){
		keepRunning = true;
		udpSenderthread = new Thread(this);
		sentQueue.clear();
		udpSenderthread.start();
	}
	
	public void stopThread(){
		keepRunning = false;
		datagramSocket.close();
		udpSenderthread.interrupt();
	}

	/**
	 * Sends data over a UDP socket. 
	 * Called from RWG layer.
	 * @param data is the packet which is to be sent. 
	 * @throws IOException  
	 */
	//public boolean sendPacket(AdhocData<AdhocFind> data) throws IOException {
	public boolean sendPacket(Packet packet) throws IOException {
		Log.e(TAG,  " UdpSender: sendPacket() sending data = " + packet);

		packet.getEthernetHeader().setSentFrom(myAddress);
		Logger.writePacketSent(packet);
		byte[] payload = packet.writeToBytes();  // Serialize the data (might throw I/O)

		if (payload.length <= AdhocService.MAX_PACKET_SIZE) {
			broadcast(payload);
			Object[] pair = new Object[2];
			pair[0] = packet.getHeader();
			pair[1] = (Double)(currentTicks*RUN_INCREMENT/1000.0);
			sentQueue.add(pair);
			return true; 
		} else {
			Log.e(TAG, " sendPacket:  Packet length=" + payload.length + " exceeds max size, not sent");
			return false;
		}
	}
	
	/**
	 * A special definition exists for the IP broadcast address 255.255.255.255. 
	 * It is the broadcast address of the zero network (0.0.0.0), which in 
	 * Internet Protocol standards stands for this network, i.e. the local network. 
	 * Transmission to this address is limited by definition, in that it 
	 * does not cross the routers connecting the local network to the Internet.
	 * @param bytes
	 */
	private void  broadcast (byte[] bytes){
		Log.i(TAG, " broadcast() bytes size =" + bytes.length);

		InetAddress IPAddress;
		try {
			String broadcastAddr = Integer.toString(AdhocControl.IP_BYTE_1) +".";
			if (AdhocControl.MASKING_BYTES >= 2)
				broadcastAddr += Integer.toString(AdhocControl.IP_BYTE_2) + ".";
			if (AdhocControl.MASKING_BYTES >= 3)
				broadcastAddr += Integer.toString(AdhocControl.IP_BYTE_3) +".";
			if (AdhocControl.MASKING_BYTES == 1)
				broadcastAddr += "255.255.255";
			else if (AdhocControl.MASKING_BYTES == 2)
				broadcastAddr += "255.255";
			else if (AdhocControl.MASKING_BYTES == 3)
				broadcastAddr += "255";
			IPAddress = InetAddress.getByName(broadcastAddr);  // Broadcast address 
			Log.i(TAG,  " Sending to IPAddress = " + IPAddress + " port = " + AdhocService.DEFAULT_PORT_BCAST);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length,IPAddress, AdhocService.DEFAULT_PORT_BCAST);
			datagramSocket.setBroadcast(true);
			datagramSocket.send(packet);
		} catch (UnknownHostException e) {
			Log.e(TAG, " UnknownHostException");
			e.printStackTrace();
		} catch (SocketException e) {
			Log.e(TAG, " SocketException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, " IOException");
			e.printStackTrace();
		}
	}
	
	public void closeSocket(){
		datagramSocket.close();
	}
	
	private boolean packetSent(PacketHeader p){	
		refreshSentList();
		boolean result = false;
		int queueLength = sentQueue.size();
		if (queueLength <= 0) return false;
		Object[] check;
		for (int i=0; i < queueLength; i++){
			check = sentQueue.remove();
			if (((PacketHeader)check[0]).equals(p)){
				result =  true;
			}
			sentQueue.add(check);
		}
		return result;
	}
	
	private void refreshSentList(){
		Object[] h = sentQueue.peek();
		if (h == null)
			return;
		while(((Double)h[1] + 30.0) < (currentTicks*RUN_INCREMENT/1000.0)){
			sentQueue.remove();
			h = sentQueue.peek();
			if (h == null)
				return;
		}
	}

	public void run(){
		Packet p;
		while(keepRunning) {
			while (!sendQueue.isEmpty()){
				try {
					/*
					 * 
					 * Need to check here if packet was already previously sent
					 * Within the last 30 seconds
					 * 
					 */
					p = sendQueue.remove();
					if (p.okToSend() && !packetSent(p.getHeader()))
						sendPacket(p);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(RUN_INCREMENT);
				currentTicks++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void setMyAddress(String addr){
		myAddress = addr;
	}
}
