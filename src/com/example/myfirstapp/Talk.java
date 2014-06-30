package com.example.myfirstapp;

import java.util.ArrayList;

public class Talk {
	
	private String deviceName;
	private String deviceAddress;
	
	private ArrayList<String> messages;
	
	public Talk(String deviceName, String deviceAddress) {
		setDeviceName(deviceName);
		setDeviceAddress(deviceAddress);
		
		messages = new ArrayList<String>();
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

	public ArrayList<String> getMessages() {
		return messages;
	}
	
	public void setMessages(ArrayList<String> messages) {
		this.messages = messages;
	}

	public String getLastMessage() {
		if(!messages.isEmpty())
			return messages.get(messages.size()-1);
		return "";
	}
	
}
