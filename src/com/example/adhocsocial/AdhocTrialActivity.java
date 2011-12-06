package com.example.adhocsocial;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import com.example.adhoctrial.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.PopupWindow;
import android.widget.Toast;



public class AdhocTrialActivity extends Activity {
	private volatile AdhocControl control;
	final Handler mHandler = new Handler();
	private volatile boolean runCheck = false;
	private volatile String msg;
	private Thread checkThread;
	private AdhocTrialActivity me;
	private Thread textUpdate;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        control = AdhocControl.startControl();
        me = this;
        if (control.getView() == AdhocControl.VIEW_MAIN)
        	setContentView(R.layout.main);
        else
        	setContentView(R.layout.buddylist);
        
        final EditText txtName = (EditText)findViewById(R.id.nameEntry);
    	final Button startButton = (Button)findViewById(R.id.btnStart);
        final EditText txtMessages = (EditText)findViewById(R.id.txtMessageBox);
        final EditText txtSendMessage = (EditText)findViewById(R.id.txtMessage);
        final Button btnSend = (Button)findViewById(R.id.btnSend);
        final Button btnAdd = (Button)findViewById(R.id.btnAdd);
        final Button btnRemove = (Button)findViewById(R.id.btnRemove);
        final Button btnCancel = (Button)findViewById(R.id.btnCancel);
        final ListView lstBuddies = (ListView)findViewById(R.id.lstBuddies);
        
        refreshControls.run();
        
        if (txtName.getText().toString().equals("")){
        	txtName.setText(control.getDefaultName());
        }
        
        startButton.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		String items[] = {"Broadcast", "Pull"};
        		AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle("Select Mode");
				builder.setItems(items,dlgType);
				AlertDialog alert = builder.create();
				builder.show();
        	}
        });
        
        btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				control.sendMessage(txtSendMessage.getText().toString());
				txtSendMessage.setText("");
			}
		});
        
        btnAdd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				control.refreshBuddyList();
				LinkedList<Buddy> buddies = control.getAvailableBuddies();
				final String[] items = new String[buddies.size()];
				for (int i = 0; i<buddies.size();i++){
					items[i] = buddies.get(i).getName();
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle("Pick a buddy");
				builder.setItems(items,dlgListen);
				AlertDialog alert = builder.create();
				builder.show();
			}
		});
        
        btnRemove.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LinkedList<Buddy> buddies = control.getChatList();
				String items[] = new String[buddies.size()];
				for (int i = 0; i<buddies.size();i++){
					items[i] = buddies.get(i).getName();
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle("Pick a buddy");
				builder.setItems(items,dlgListen);
				AlertDialog alert = builder.create();
				builder.show();
			}
		});
        
        /*btnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setView(AdhocControl.VIEW_MAIN);
			}
		});
        
        lstBuddies.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				control.indexSelected(arg2);
			}
        	
		});*/
        txtSendMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event == null || event.getAction() == 0){
					if (!control.isStarted()) return true;
					control.sendMessage(txtSendMessage.getText().toString());
					txtSendMessage.setText("");
					return true;
				}
				return true;
			}
		});
    }
    
    private OnClickListener dlgListen = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			control.indexSelected(which);
		}
	};
	
	private OnClickListener dlgType = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			final EditText txtName = (EditText)findViewById(R.id.nameEntry);
			control.setDiscoveryType(which);
			startAdhocService();
    		control.startAdhoc(txtName.getText().toString());
    		refreshControls.run();
    		textUpdate = new Thread(update);
    		textUpdate.start();
		}
	};
    
    private Runnable update = new Runnable(){
    	public void run(){
    		while (true){
    			if (control.canRefresh())
    				mHandler.post(refreshControls);
	    		if (control.chatUpdated()){
	    			mHandler.post(setText);
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
    
    private Runnable setText = new Runnable(){
    	public void run(){
    		if (control.isStarted()){
	    		final EditText txtMessages = (EditText)findViewById(R.id.txtMessageBox);
	    		txtMessages.setText(control.getChatMessages());
    		}
    	}
    };
    
    private void setView(int view){
    	control.setView(view);
    	if (control.getView() == AdhocControl.VIEW_MAIN)
        	setContentView(R.layout.main);
        else
        	setContentView(R.layout.buddylist);
    }
    
    private Runnable refreshControls = new Runnable(){
    	public void run(){
    		final EditText txtName = (EditText)findViewById(R.id.nameEntry);
        	final Button startButton = (Button)findViewById(R.id.btnStart);
    		final EditText txtMessages = (EditText)findViewById(R.id.txtMessageBox);
            final EditText txtSendMessage = (EditText)findViewById(R.id.txtMessage);
            final Button btnSend = (Button)findViewById(R.id.btnSend);
            final Button btnAdd = (Button)findViewById(R.id.btnAdd);
            final Button btnRemove = (Button)findViewById(R.id.btnRemove);
            txtName.setEnabled(!control.isStarted());
            startButton.setEnabled(!control.isStarted());
        	txtMessages.setEnabled(control.isStarted());
            txtSendMessage.setEnabled(control.isStarted());
            btnSend.setEnabled(control.isStarted());
            btnAdd.setEnabled(control.isStarted());
            btnRemove.setEnabled(control.isStarted());
    	}
    };
    
    public void startAdhocService() {
    	if (!AdhocService.isStarted()){
			Intent serviceIntent = new Intent();
			serviceIntent.setClass(this, AdhocService.class);
			startService(serviceIntent);
    	}
	}
    
    public void stopAdhocService() {
		Intent serviceIntent = new Intent();
		serviceIntent.setClass(this, AdhocService.class);
		stopService(serviceIntent);
	}
    
}