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
import android.widget.ListView;
import android.widget.TextView;


public class AdhocTrialActivity extends Activity {
	private volatile AdhocControl control;
	private volatile boolean runCheck = false;
	private volatile String msg;
	private Thread checkThread;
	final Handler mHandler = new Handler();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        control = AdhocControl.startControl();
        
        final EditText txtName = (EditText)findViewById(R.id.nameEntry);
        final Button startButton = (Button)findViewById(R.id.btnStart);
        final EditText txtMessages = (EditText)findViewById(R.id.txtMessageBox);
        final EditText txtSendMessage = (EditText)findViewById(R.id.txtMessage);
        final Button btnSend = (Button)findViewById(R.id.btnSend);
        final Button btnAdd = (Button)findViewById(R.id.btnAdd);
        final Button btnRemove = (Button)findViewById(R.id.btnRemove);
        
        txtMessages.setEnabled(control.isStarted());
        txtSendMessage.setEnabled(control.isStarted());
        btnSend.setEnabled(control.isStarted());
        btnAdd.setEnabled(control.isStarted());
        btnRemove.setEnabled(control.isStarted());
    }
    
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
    
}