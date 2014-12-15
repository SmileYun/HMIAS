package com.cqupt.hmi.persenter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.cqupt.hmi.core.util.SegmentToCanMsgInfo;
import com.cqupt.hmi.entity.CanMsgInfo;
import com.cqupt.hmi.entity.CanMsgInfo.DISPLAYTYPE;
import com.cqupt.hmi.model.RecvThread;
import com.cqupt.hmi.model.RecvThread.SegmentMsgHandler;
import com.cqupt.hmi.model.bluetoothconn.BluetoothConn;
import com.cqupt.hmi.model.threaten.BMHandler;
import com.cqupt.hmi.model.threaten.ByteArrayToSegment;
import com.cqupt.hmi.model.threaten.CanMsgCache.Segment;
import com.cqupt.hmi.model.threaten.SVHandler;
import com.cqupt.hmi.ui.base.IUI;

import java.io.IOException;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class Dispatcher implements SegmentMsgHandler {
    private static String TAG = "Dispatcher";

    private static Dispatcher mInstanceDispatcher;

    private IUI mContentView;

    private AbHandler mBMAbHandler, mSVHandler;

    private RecvThread mRecvThread;

    private BluetoothSocket mBluetoothSocket;

//	private HandlerThread mHandler;

    private Timer mTQueryTBAT;


    private Dispatcher() {
        initResponseChain();
    }

    public static Dispatcher getInstance() {
        if (mInstanceDispatcher == null) {
            synchronized (Dispatcher.class) {
                if (mInstanceDispatcher == null)
                    mInstanceDispatcher = new Dispatcher();
            }
        }
        return mInstanceDispatcher;
    }

    public void setIUI(IUI ui) {
        this.mContentView = ui;
    }

    /**
     * 初始化责任链
     */
    private void initResponseChain() {
        mBMAbHandler = new BMHandler();
        mSVHandler = new SVHandler();
        mBMAbHandler.setNext(mSVHandler);
    }

    public boolean connectBTandRecv(BluetoothDevice device) {
        try {
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            mBluetoothSocket.connect();

            if (mBluetoothSocket != null) {
                //建立连接后，初始化线程并设置处理后启动
                mRecvThread = new RecvThread(mBluetoothSocket);
                mRecvThread.setHandler(new ByteArrayToSegment());
                mRecvThread.setSenter(this);
                mRecvThread.start();
                //设置定时器， 0.5s 轮询缓存表
                mTQueryTBAT = new Timer();
                mTQueryTBAT.scheduleAtFixedRate(new ObserveThreatenTableAtRate(), 1000, 500);
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

	/*public interface ICommand{
        void excute();
	}
	
	public void postCommand(Runnable command){
		
	}*/

    public static abstract class AbHandler {
        private DISPLAYTYPE level = DISPLAYTYPE.UNKNOW;

        private AbHandler nextHandler;

        public AbHandler() {
        }

        public AbHandler(DISPLAYTYPE _level) {
            this.level = _level;
        }

        public void setNext(AbHandler h) {
            nextHandler = h;
        }

        public final Bundle handleMessage(CanMsgInfo info) {
            if (info.getType() == this.level) {
                return this.response(info);
            } else if (this.nextHandler != null) {
                return this.nextHandler.handleMessage(info);
            } else {
                Log.e(TAG, " CanMsgInfo can't be handled !");
                return null;
            }
        }

        public abstract Bundle response(CanMsgInfo info);
    }

    @Override
    public void handlerMsg(Segment sg) {
        notifyUI(sg);
    }

    final class ObserveThreatenTableAtRate extends TimerTask {

        @Override
        public void run() {
            Segment _s = mRecvThread.queryHighLevelReturnSg();
            notifyUI(_s);
        }
    }

    private void notifyUI(Segment sg) {
        CanMsgInfo info = null;
        if (sg != null) {
            info = SegmentToCanMsgInfo.segmentToCanMsgInfo(sg);
            Bundle _b = mBMAbHandler.handleMessage(info);
            if (_b != null) {
                mContentView.show(_b);
            } else {
                mContentView.stop();
            }
        }else {
           mContentView.stop();
        }
    }

    public void registerBTConnState(Context context) {
        if (context instanceof Observer) {
            BluetoothConn.getInstance(context.getApplicationContext()).addObserver((Observer) context);
            BluetoothConn.getInstance(context.getApplicationContext()).setObs((BluetoothConn.Update) context);
        }
    }
}
