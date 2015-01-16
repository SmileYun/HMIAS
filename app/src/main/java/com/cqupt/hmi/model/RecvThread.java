package com.cqupt.hmi.model;


import android.bluetooth.BluetoothSocket;

import com.cqupt.hmi.model.threaten.CanMsgCache;

import java.util.HashMap;

public class RecvThread {

    private BluetoothSocket mBluetoothSocket;

    private boolean isRecv = true;

    private char[] mCanMsgCharBuffer = new char[10];

    private CanMsgCache mCanMsgCache = CanMsgCache.getCacheInstance();

    private RawCanMsgHandler mHandler = null;

    private SegmentMsgHandler mSenter = null;

    private int nowLevel = CanMsgCache.Segment.LEVEL.SAFE.getLevel();


    public RecvThread(BluetoothSocket mBluetoothSocket) {
        this.mBluetoothSocket = mBluetoothSocket;
    }

    public void start() {
        new Thread(r).start();
    }

    private Runnable r = new Runnable() {

        @Override
        public void run() {
            int looper_recv = 10;
            int _readIntResult = -1;

            try {
                while (isRecv) {
                    while (looper_recv > 0 && ((_readIntResult = mBluetoothSocket.getInputStream().read()) != -1)) {

                        mCanMsgCharBuffer[10 - looper_recv] = (char) (_readIntResult & 0xFF);

                        if (mCanMsgCharBuffer[0] != 0x41 && mCanMsgCharBuffer[0] != 0x31 && mCanMsgCharBuffer[0] != 0x21) {
                            looper_recv = 11;
                        }

                        if (looper_recv == 9 && mCanMsgCharBuffer[1] != 0x05) {
                            looper_recv = 11;
                        }
                        --looper_recv;
                    }
                    looper_recv = 10;
                    if (mHandler != null) {
                        CanMsgCache.Segment _s = mHandler.handlerRawCanMsg(mCanMsgCharBuffer);
                        //存储
                        mCanMsgCache.update(_s);
                        //高于当前级别则通知
                        if (_s.getLevel() >= nowLevel & _s.getLevel() != CanMsgCache.Segment.LEVEL.SAFE.getLevel()) {
                            mSenter.handlerMsg(_s);
                        }
                    }
                    
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public HashMap<String, Object> queryHighLevel() {
        HashMap<String, Object> _m;
        _m = mCanMsgCache.queryHighLevel();
        return _m;
    }

    /**
     *
     * 返回优先级别最高场景
     *
     * @return Segment, 如果不存在, 则返回null
     */
    public CanMsgCache.Segment queryHighLevelReturnSg() {
        CanMsgCache.Segment _s = null;
        _s = mCanMsgCache.queryHighLevelReturnSg();
        return _s;
    }

    /**
     * @Description 处理原始信息以存入缓存
     */
    public interface RawCanMsgHandler {
        CanMsgCache.Segment handlerRawCanMsg(char[] mCanMsgCharBuffer);
    }

    /**
     * @Description 接到信息后通知
     */
    public interface SegmentMsgHandler {
        void handlerMsg(CanMsgCache.Segment sg);
    }


    public void setHandler(RawCanMsgHandler handler) {
        this.mHandler = handler;
    }

    public void setSenter(SegmentMsgHandler handler) {
        this.mSenter = handler;
    }

    public void setNowLevel(int nowLevel) {
        this.nowLevel = nowLevel;
    }
}
