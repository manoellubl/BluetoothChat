package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {
	
	private List<Talk> talks;
	
	private BluetoothAdapter bluetoothAdapter;
	private TalksAdapter talksAdapter;
	
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_START_CHAT = 2;
	
	public static String EXTRA_DEVICE_NAME = "device_name";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static String EXTRA_DEVICE_MESSAGES = "device_messages";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversas);
		
		talks = new ArrayList<Talk>();
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		talksAdapter = new TalksAdapter(this, R.layout.conversas_item, talks);
		
		ListView listView = (ListView) findViewById(R.id.list_conversas);
		listView.setAdapter(talksAdapter);
		listView.setOnItemClickListener(talkClickListener);
		
		enableDiscoverability(0);
	}
	
	public void searchDevices(View view) {
		if (!bluetoothIsEnabled()) {
			enableDiscoverability(0);
		}
		else {
			Intent intent = new Intent(this, DisplayDevicesActivity.class);
			startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
		}
	}
	
	private void enableDiscoverability(int time) {
		
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time);
		startActivity(discoverableIntent);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
        	if (resultCode == RESULT_OK)
				creatTalk(data);
            break;
            
		case REQUEST_START_CHAT:
			if (resultCode == RESULT_OK) {
        		String deviceAddress = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
				ArrayList<String> messages = (ArrayList<String>) data.getExtras().getSerializable(EXTRA_DEVICE_MESSAGES);
				
				for (Talk talk : talks) {
					if (talk.getDeviceAddress().equals(deviceAddress)) {
						talk.setMessages(messages);
						talksAdapter.notifyDataSetChanged();
					}
				}
        	}
            break;
        
        default:
        	break;
        }
	}
	
	private void creatTalk(Intent data) {
		String deviceName = data.getExtras().getString(EXTRA_DEVICE_NAME);
		String deviceAddress = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
		
		Talk talk = null;
		boolean isNew = true;
		
		for (Talk t : talks)
			if (t.getDeviceAddress().equals(deviceAddress)) {
				talk = t;
				isNew = false;
			}
		
		if (isNew) {
			talk = new Talk(deviceName, deviceAddress);
			talksAdapter.add(talk);
		}
		
		initializeChat(talk);
    }
	
	private boolean bluetoothIsEnabled() {
		if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
			return false;
		return true;
	}
	
	private void initializeChat(Talk talk) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(EXTRA_DEVICE_NAME, talk.getDeviceName());
		intent.putExtra(EXTRA_DEVICE_ADDRESS, talk.getDeviceAddress());
		intent.putExtra(EXTRA_DEVICE_MESSAGES, talk.getMessages());
		startActivityForResult(intent, REQUEST_START_CHAT);
	}
    
    private OnItemClickListener talkClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	Talk talk = talks.get(position);
        	initializeChat(talk);
        }
    };
	
}
