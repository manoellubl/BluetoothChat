package com.example.myfirstapp;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ChatActivity extends Activity {
	
	private ArrayAdapter<String> arrayAdapter;
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothConnection bluetoothConnection;
	
	private String deviceName;
	private String deviceAddress;
	private ArrayList<String> messages;
	
	public static final int MESSAGE_READ = 1;
	public static final int MESSAGE_WRITE = 2;
	public static final int CONNECTED = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		
		initialize();
		startConnection();
		setResult();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		bluetoothConnection.stop();
	}
	
	@SuppressWarnings("unchecked")
	private void initialize() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		bluetoothConnection = new BluetoothConnection(this, mHandler);
		bluetoothConnection.start();
		
		Intent data = getIntent();
		deviceName = data.getExtras().getString(MainActivity.EXTRA_DEVICE_NAME);
		deviceAddress = data.getExtras().getString(MainActivity.EXTRA_DEVICE_ADDRESS);
		messages = (ArrayList<String>) data.getExtras().getSerializable(MainActivity.EXTRA_DEVICE_MESSAGES);
		
		arrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item, messages);
		ListView listView = (ListView) findViewById(R.id.in);
		listView.setAdapter(arrayAdapter);
	}
	
	private void startConnection() {
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
		bluetoothConnection.connect(device);
	}
	
	public void sendMessage(View view) {
		EditText editText = (EditText) findViewById(R.id.edit_text_out);
		String msg = editText.getText().toString();
		if (msg.trim().length() < 1) return;
		editText.setText("");
		if (!bluetoothConnection.write(msg))
			toastMessage("You are not connected");
	}
	
	public void showMessage(String msg) {
		arrayAdapter.add(msg);
		//setResult();
	}
	
	private void toastMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void setResult() {
		Intent data = new Intent();
		
		data.putExtra(MainActivity.EXTRA_DEVICE_ADDRESS, deviceAddress);
		data.putExtra(MainActivity.EXTRA_DEVICE_MESSAGES, messages);
		
		setResult(RESULT_OK, data);
	}
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_WRITE:
                String writeMessage = (String) msg.obj;
                showMessage("Me: " + writeMessage);
                break;
                
            case MESSAGE_READ:
                String readMessage = (String) msg.obj;
                showMessage(deviceName + ": " + readMessage);
                break;
            
            case CONNECTED:
            	toastMessage("Connected");
                break;
            
            default:
            	break;
            }
        }
    };
}
