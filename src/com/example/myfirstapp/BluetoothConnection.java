package com.example.myfirstapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;

public class BluetoothConnection {
	private static final String NAME = "MyFirstApp";
    private static final UUID MY_UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothConnection bluetoothConnection;
    private final BluetoothAdapter mAdapter;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;
    
    public BluetoothConnection(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        bluetoothConnection = this;
    }
    
    public void setHandler(Handler handler) {
    	mHandler = handler;
    }
    
    private void startAccept() {
    	if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
    	}
    }
    
    private void stopAccept() {
    	if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
    }
    
    private void startConnect(BluetoothDevice device) {
		if (mConnectThread == null) {
			mConnectThread = new ConnectThread(device);
			mConnectThread.start();
		}
	}
    
    private void stopConnect() {
    	if (mConnectThread != null) {
    		mConnectThread.cancel();
    		mConnectThread = null;
    	}
    }
    
    private void startConnected(BluetoothSocket socket) {
		if (mConnectedThread == null) {
			mConnectedThread = new ConnectedThread(socket);
			mConnectedThread.start();
		}
	}
    
	private void stopConnected() {
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
	}
    
    public synchronized void start() {
    	stopConnect();
    	stopConnected();
    	startAccept();
    }

    public synchronized void connect(BluetoothDevice device) {
        stopConnect();
        stopConnected();
        startConnect(device);
    }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
    	stopAccept();
        stopConnect();
        stopConnected();
        
        startConnected(socket);
        mHandler.obtainMessage(ChatActivity.CONNECTED, -1, -1, -1).sendToTarget();
    }
    
    public synchronized void stop() {
        stopAccept();
        stopConnect();
        stopConnected();
    }

    public boolean write(String message) {
    	byte[] out = message.getBytes();
        ConnectedThread r;
        synchronized (this) {
        	if (mConnectedThread == null) return false;
            r = mConnectedThread;
        }
        r.write(out);
        return true;
    }
    
    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        
        public AcceptThread() {
        	try {
                mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME,MY_UUID);
            } catch (IOException e) {}
        }
        
        public void run() {
        	BluetoothSocket socket = null;
            
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                
                if (socket != null) {
                    synchronized (this) {
                        connected(socket, socket.getRemoteDevice());
                        break;
                    }
                }
            }
        }
        
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {}
        }
    }
    
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        
        public ConnectThread(BluetoothDevice device) {
        	mmDevice = device;
            
        	try {
        		mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {}
        }
        
        public void run() {
            mAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                cancel();
                return;
            }
            
            synchronized (this) {
            	BluetoothSocket socket = mmSocket;
            	mmSocket = null;
            	connected(socket, mmDevice);
            }
        }
        
        public void cancel() {
            closeSocket();
        }
        
        private void closeSocket() {
        	if (mmSocket != null) {
        		try {
					mmSocket.close();
				} catch (IOException e) { }
        		mmSocket = null;
        	}
        }
    }
    
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            try {
            	mmInStream = socket.getInputStream();
            	mmOutStream = socket.getOutputStream();
            } catch (IOException e) { }
        }
        
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    
                    mHandler.obtainMessage(ChatActivity.MESSAGE_READ, -1, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                	bluetoothConnection.start();
                	break;
                }
            }
        }
        
        public void write(byte[] buffer) {
            try {
            	mmOutStream.write(buffer);
                String writeMessage = new String(buffer);
                mHandler.obtainMessage(ChatActivity.MESSAGE_WRITE, -1, -1, writeMessage).sendToTarget();
            } catch (IOException e) { }
        }
        
        public void cancel() {
        	closeInputStream();
        	closeOutputStream();
        	closeBluetoothSocket();
        }
        
        private void closeInputStream() {
        	if (mmInStream != null) {
                try {
                	mmInStream.close();
                } catch (Exception e) { }
                mmInStream = null;
        	}
        }
        
        private void closeOutputStream() {
        	if (mmOutStream != null) {
        		try {
                	mmOutStream.close();
                } catch (Exception e) { }
                mmOutStream = null;
        	}
        }
        
        private void closeBluetoothSocket() {
        	if (mmSocket != null) {
        		try {
        			mmSocket.close();
        		} catch (Exception e) { }
        		mmSocket = null;
        	}
        }
    }
	
}
