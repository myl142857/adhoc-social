package com.example.adhocsocial;

//This class was edited from the original class created for the Posit mobile project

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.util.Log;

public class EthernetHeader implements Serializable {

	public static final String TAG = "Adhoc";
	
	//where this packet originally came from
	private String source;
	//where this packet is destined for
	private String destination; //If destination is "" then it is a broadcast message for everyone
	//where this packet was sent from (ie. if it came from a hop this will not = source)
	private String sentFrom;
	
	public EthernetHeader (String source, String destination) {
		this.source = source;
		this.destination = destination;
	}
	
	//broadcast header
	public EthernetHeader (String source) {
		this.source = source;
		this.destination = "";
	}
	
	//don't check the 'sentFrom' to see if it is equal
	public boolean equals(EthernetHeader e){
		return (source.equals(e.source) && destination.equals(e.destination));
	}
	
	public String getSource() {
		return source;
	}


	public void setSource(String s) {
		this.source = s;
	}
	
	public void setSentFrom(String addr){
		this.sentFrom = addr;
	}
	
	public String getSentFrom(){
		return this.sentFrom;
	}

	public String getDestination() {
		return destination;
	}


	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String toString() {
		return "  Source: " + source + "\r\n  Destination: " + destination + "\r\n" +
				"  Sent From: " + sentFrom + "\r\n";
	}
	
	public String toXlsString(){
		return "'"+sentFrom + "\t" + "'"+source + "\t" + "'"+destination;
	}
	
	/**
	 * Write this object to a serialized byte array
	 * @param baos
	 * @throws IOException
	 */
	public byte[] writeToBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.flush();
		return baos.toByteArray();
	}
	
	/**
	 * Reads an instance of AdhocData from a serialized byte stream.
	 * @param bais
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public EthernetHeader readFromBytes(byte[] bytes) 
					throws IOException,	ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		EthernetHeader data = (EthernetHeader)ois.readObject();

		// For development/debug 
		Log.d(TAG, "source = " + data.source);
		Log.d(TAG, "destination = " + data.destination);
		return data;
	}
	
	public int getSize(){
		int size = 0;
		size += source.length()*2;
		size += destination.length()*2;
		return size;
	}
}
