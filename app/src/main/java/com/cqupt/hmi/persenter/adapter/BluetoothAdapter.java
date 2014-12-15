package com.cqupt.hmi.persenter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BluetoothAdapter extends AdapterBase{

	public BluetoothAdapter(Context context, ArrayList<String> mList) {
		super(context, mList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder h ;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent,false);
			h = new ViewHolder();
			h.tx = (TextView) convertView;
			convertView.setTag(h);
		}else {
			h = (ViewHolder) convertView.getTag();
		}
		
		h.tx.setText(mList.get(position).toString());
		return convertView;
	}
	
	static class ViewHolder{
		TextView tx;//
	}
}
