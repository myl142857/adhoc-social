package com.example.adhocsocial;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.example.adhoctrial.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class AdhocTrialActivity extends Activity {
	private volatile AdhocControl control;
	private volatile boolean runCheck = false;
	private volatile Packet receivedPacket;
	private Thread checkThread;
	final Handler mHandler = new Handler();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Button btnStart = (Button) findViewById(R.id.button1);
        final Button btnSend = (Button) findViewById(R.id.button2);
        final EditText edtResults = (EditText) findViewById(R.id.editText1);
        final EditText edtMessage = (EditText) findViewById(R.id.editText2);
        control = AdhocControl.startControl(this, edtResults);
        if (control.isStarted()){
        	btnStart.setBackgroundColor(Color.RED);
        	btnStart.setText("Stop");
        }
        else{
        	btnStart.setBackgroundColor(Color.GREEN);
        	btnStart.setText("Start");
        }
        
        
        
        btnStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (control.isStarted()) {
					//STOP
					control.stopAdhoc();
					btnStart.setBackgroundColor(Color.GREEN);
					btnStart.setText("Start");
					
					runCheck = false;
					checkThread.interrupt();
				}
				else{
					//START
					control.startAdhoc();
					btnStart.setBackgroundColor(Color.RED);
					btnStart.setText("Stop");
					
					runCheck = true;
					checkThread = new Thread(checkMsg);
					checkThread.start();
				}
				
			}
		});
        
        btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!control.isStarted()) return;
				runOnUiThread(sendMsg);
			}
		});
        
        edtMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event == null || event.getAction() == 0){
					if (!control.isStarted()) return true;
					runOnUiThread(sendMsg);
					return true;
				}
				return true;
			}
		});
    }
    
    Runnable sendMsg = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String myAddress = control.getMyAddress();
			final EditText edtMessage = (EditText) findViewById(R.id.editText2);
			EthernetHeader ethrHeader = new EthernetHeader(myAddress);
			Packet p = new Packet(ethrHeader, edtMessage.getText().toString());
			control.sendPacket(p);
		}
    	
    };
    
    public void startAdhocService() {
		Intent serviceIntent = new Intent();
		serviceIntent.setClass(this, AdhocService.class);
		startService(serviceIntent);
	}
    
    public void stopAdhocService() {
		Intent serviceIntent = new Intent();
		serviceIntent.setClass(this, AdhocService.class);
		stopService(serviceIntent);
	}
    
    private Runnable checkMsg = new Runnable(){
    	public void run(){
    		Packet p;
    		while(runCheck){
    			p = control.getNextPacket();
    			if (p != null){
    				receivedPacket = p;
    				mHandler.post(updateText);
    			}
    			try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    };
    
    Runnable updateText = new Runnable(){
		public void run(){
			EditText text = (EditText) findViewById(R.id.editText1);
			text.setText(receivedPacket.getMessage());
			
		}
	};
    
}