package com.cqupt.hmi.persenter.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public abstract class AdapterBase extends BaseAdapter{

	protected ArrayList<String> mList;
	
	protected Context mContext;

	public AdapterBase(Context context, ArrayList<String> mList) {
		super();
		this.mList = mList;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
