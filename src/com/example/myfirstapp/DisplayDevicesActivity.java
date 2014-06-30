package com.example.myfirstapp;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DisplayDevicesActivity extends Activity {
	
	private ArrayAdapter<String> arrayAdapter;
	private BluetoothAdapter bluetoothAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_display_devices);
		
		setResult(Activity.RESULT_CANCELED);
		
		Button searchButton = (Button) findViewById(R.id.button_search_devices);
        searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                searchDevices();
            }
        });
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		arrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_list_item);
		
		ListView listView = (ListView) findViewById(R.id.devices_list);
		listView.setAdapter(arrayAdapter);
		listView.setOnItemClickListener(mDeviceClickListener);
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
		
		showPairedDevices();
		searchDevices();
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null) {
        	bluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(receiver);
    }
	
	private void showPairedDevices() {
		if (bluetoothAdapter.isEnabled()) {
			Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
			for (BluetoothDevice device : pairedDevices) {
				addToList(device.getName() + "\n" + device.getAddress());
			}
		}
	}
	
	private void searchDevices() {
		if (bluetoothAdapter.isEnabled()) {
			if (bluetoothAdapter.isDiscovering()) {
				bluetoothAdapter.cancelDiscovery();
	        }
			bluetoothAdapter.startDiscovery();
		}
	}

	public void addToList(String str) {
		if (!isInList(arrayAdapter, str))
			arrayAdapter.add(str);
	}
	
	public boolean isInList(ArrayAdapter<String> arrayAdapter, String str) {
		for (int i = 0; i < arrayAdapter.getCount(); i++)
			if(arrayAdapter.getItem(i).equals(str))
				return true;
		return false;
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetoothAdapter.cancelDiscovery();
            
            String info = ((TextView) view).getText().toString();
            String name = info.substring(0, info.length() - 18);
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra(MainActivity.EXTRA_DEVICE_NAME, name);
            intent.putExtra(MainActivity.EXTRA_DEVICE_ADDRESS, address);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
    
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            addToList(device.getName() + "\n" + device.getAddress());
	        }
	    }
	};
}
