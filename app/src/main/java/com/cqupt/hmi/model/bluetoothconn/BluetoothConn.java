package com.cqupt.hmi.model.bluetoothconn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class BluetoothConn extends Observable {

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    /* 用来存储搜索到的蓝牙设备 */
    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<BluetoothDevice>();

    private volatile boolean mIsDiscoveryFinished = false;

    private Context mContext;

    private BroadcastReceiver mDeviceFound, mDisConnectState, mDiscoveryFinished;

    private DeviceChangeListener mDeviceChangeListener = null;

    // private BluetoothStateChangeListener mBluetoothStateChangeListener = null;

    private boolean mIsBTConnection = false;

    private static volatile BluetoothConn mInstance;

    private BluetoothConn(Context mContext) {
        this.mContext = mContext;
        IntentFilter _DiscoveryFinishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter _DeviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter _DisConnStateFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        mDeviceFound = new DeviceFound();
        mDisConnectState = new DisConnectState();
        mDiscoveryFinished = new DiscoveryFinished();
        this.mContext.registerReceiver(mDeviceFound, _DeviceFoundFilter);
        this.mContext.registerReceiver(mDiscoveryFinished, _DiscoveryFinishedFilter);
        this.mContext.registerReceiver(mDisConnectState, _DisConnStateFilter);
    }

    /**
     * 用全局context ; getApplicationContext()
     *
     * @param mContext
     * @return BluetoothConn    返回类型
     */
    public static BluetoothConn getInstance(Context mContext) {
        if (mInstance == null) {
            synchronized (BluetoothConn.class) {
                if (mInstance == null) {
                    mInstance = new BluetoothConn(mContext);
                }
            }
        }
        return mInstance;
    }

    private final class DiscoveryBluetoothDevice implements Runnable {
        @Override
        public void run() {
            /* 开始搜索 */
            mBluetoothAdapter.startDiscovery();
            Log.d("--DiscoveryBluetoothDevice->", "BluetoothAdapter.startDiscovery()");
            for (; ; ) {
                if (mIsDiscoveryFinished) {
                    /* TODO 搜索完成 */
                    mBluetoothAdapter.cancelDiscovery();
                    break;
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void discovery() {
        mIsDiscoveryFinished = false;
        cancleDiscovery();
        new Thread(new DiscoveryBluetoothDevice()).start();
    }

    private final class DiscoveryFinished extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /* 卸载注册的接收器 */
//			mContext.unregisterReceiver(mDeviceFound);
//			mContext.unregisterReceiver(mDiscoveryFinished);
            mIsDiscoveryFinished = true;
        }

    }

    private final class DeviceFound extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            mBluetoothDeviceList.add(device);
            if (mDeviceChangeListener != null)
                mDeviceChangeListener.onDeviceFind(device);
        }

    }

    public void setmDeviceChangeListener(DeviceChangeListener mDeviceChangeListener) {
        this.mDeviceChangeListener = mDeviceChangeListener;
    }

    public interface DeviceChangeListener {
        void onDeviceFind(BluetoothDevice device);
    }

    public interface BluetoothStateChangeListener {
        void onBluetoothStateChange(boolean isConn);
    }

    public synchronized List<BluetoothDevice> getmBluetoothDeviceList() {
        return mBluetoothDeviceList;
    }

    /**
     * 蓝牙断开连接广播
     */
    private final class DisConnectState extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mIsBTConnection = false;
            notifyObservers(mIsBTConnection);
            if (obs != null)
                obs.myUpdate(mIsBTConnection);
        }
    }

    /**
     * 取消蓝牙连接
     */
    public void cancleDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
    }

    public interface Update {
        void myUpdate(Object data);
    }

    private Update obs;

    public void setObs(Update obs) {
        this.obs = obs;
    }
}
