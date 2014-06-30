package com.example.myfirstapp;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TalksAdapter extends ArrayAdapter<Talk> {
	
	private int textViewResourceId;
	
	public TalksAdapter(Context context, int textViewResourceId, List<Talk> users) {
		super(context, textViewResourceId, users);
		this.textViewResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Talk talk = getItem(position);    
		
		if (convertView == null)
			convertView = LayoutInflater.from(getContext()).inflate(textViewResourceId, parent, false);
		
		TextView name = (TextView) convertView.findViewById(R.id.device_name);
		TextView text = (TextView) convertView.findViewById(R.id.device_text);
		
		name.setText(talk.getDeviceName());
		text.setText(talk.getLastMessage());
		
		return convertView;
	}
}