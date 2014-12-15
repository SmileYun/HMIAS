package com.cqupt.hmi.ui;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.cqupt.hmi.R;
import com.cqupt.hmi.core.ioc.CCIoCView;
import com.cqupt.hmi.model.bluetoothconn.BluetoothConn;
import com.cqupt.hmi.persenter.adapter.BluetoothAdapter;
import com.cqupt.hmi.ui.base.HMIActivity;

import java.util.ArrayList;

public class ActivityBluetoothDeviceList extends HMIActivity {
	protected String TAG = "ActivityBluetoothDeviceList";
	private BluetoothConn mBluetoothConn;
	
	@CCIoCView(id = R.id.bluet_device_list, itemClick = "itemClik")
	private ListView mListView;
	
	@CCIoCView(id = R.id.login_close_button, onClick = "closeList")
	private ImageButton closebtn;

	private ArrayList<String> mItemList = new ArrayList<String>();
	
	private BluetoothAdapter mAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMainContentView(R.layout.bluediscactivity);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void init() {
		mBluetoothConn = BluetoothConn.getInstance(getApplicationContext());
		mBluetoothConn.discovery();
		mBluetoothConn.setmDeviceChangeListener(new BluetoothConn.DeviceChangeListener() {

			@Override
			public void onDeviceFind(BluetoothDevice device) {
				StringBuilder sb = new StringBuilder();
				sb.append(device.getAddress());
				sb.append('\n');
				sb.append(device.getName());
				String str = sb.toString();
				
				mItemList.add(str);
				mAdapter.notifyDataSetChanged();
			}
		});
		mAdapter = new BluetoothAdapter(this, mItemList);
		mListView.setAdapter(mAdapter);
	}

	protected void closeList(View v){
		finish();
	}
	
	protected void itemClik(AdapterView<?> parent, View view, int position, long id){
		Intent data = new Intent();
		data.putExtra("postion", position);
		data.putExtra(BluetoothDevice.EXTRA_DEVICE, mBluetoothConn.getmBluetoothDeviceList().get(position));
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	@Override
	public void show(Bundle b) {
	}

	@Override
	public void stop() {
	}
}
