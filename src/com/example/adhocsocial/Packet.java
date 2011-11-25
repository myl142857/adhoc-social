package com.example.adhocsocial;

//This class was edited from the original class created for the Posit mobile project
//Originally called RwgPacket

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;



import android.util.Log;

public class Packet implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String TAG = "Adhoc";
	
	private PacketHeader header;
	
	private String messageType;
	private String message;  //contains special message
	
	
	public Packet(){
		header = new PacketHeader();
	}

	public Packet(EthernetHeader ethrHeader, String data) {
		header = new PacketHeader();
		this.header.setEthrHeader(ethrHeader);
		message = data;
	}

	public String getMessage(){
		return message;
	}

	public byte[] getData(){
		return message.getBytes();
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
	public static Packet readFromBytes(byte[] bytes) 
					throws IOException,	ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Packet data = (Packet)ois.readObject();

		return data;
	}	

	public EthernetHeader getEthernetHeader() {
		return header.getEathrnetHeader();
	}

	public byte[] toBytes() {
		return toString().getBytes();
	}

	public int getPacketID() {
		return header.getPacketID();
	}

	public void incrementHop(){
		header.incrementHopCount(); 
		
	}
	
	public void setMaxHop(int h){
		header.setMaxHop(h);
	}
	
	public int getMaxHop(){
		return header.getMaxHop();
	}
	
	public void setType(String t){
		header.setType(t);
	}
	
	public String getType(){
		return header.getType();
	}
	
	public void setApplication(String app){
		header.setApplication(app);
	}
	
	public String getApplication(){
		return header.getApplication();
	}
	
	public PacketHeader getHeader(){
		return header;
	}
	
	public String getMessageType(){
		return messageType;
	}
	
	public void setMessageType(String t){
		messageType = t;
	}
	
	public boolean okToSend(){
		return (header.getMaxHop() > header.getCurrentHop());
	}
	
	public String toString(){
		int size = this.getSize();
		String s = "Size = " + Integer.toString(size) + " bytes \n";
		s += header.toString();
		s += "  message: " + message + "\n";
		return s;
	}
	
	public int getSize(){
		int size = 0;
		size += message.length()*2;
		size += header.getSize();
		return size;
	}
}