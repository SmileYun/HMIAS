package com.cqupt.hmi.ui;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.Toast;

import com.cqupt.hmi.R;
import com.cqupt.hmi.core.ioc.CCIoCView;
import com.cqupt.hmi.core.util.AnimationUtils;
import com.cqupt.hmi.entity.CanMsgInfo;
import com.cqupt.hmi.model.bluetoothconn.BluetoothConn;
import com.cqupt.hmi.model.threaten.CanMsgCache;
import com.cqupt.hmi.persenter.Dispatcher;
import com.cqupt.hmi.ui.base.HMIActivity;
import com.cqupt.hmi.ui.widget.DynamicView;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends HMIActivity implements Callback, Observer, BluetoothConn.Update {
    protected String TAG = "MainActivity";

    private BluetoothAdapter bta;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int REQUEST_DISCOVERY = 2;

    private Dispatcher mDispatcher;

    @CCIoCView(id = R.id.img1)
    private ImageView display_1;

    @CCIoCView(id = R.id.img2)
    private ImageView display_2;

    @CCIoCView(id = R.id.surface)
    private DynamicView mDynamicView;

    @CCIoCView(id = R.id.hasRecv)
    private ImageView mHasRecv;

    private Handler mHandler;

    private ToneGenerator mToneGenerator;

    @CCIoCView(id = R.id.connection, onClick = "connectionClick")
    private ImageView mConnection;

    private int Voice_Value = 100;

    private boolean isSilence = false;

    private Timer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMainContentView(R.layout.threaten_layout);
        mHandler = new Handler(this);
        mConnection.setVisibility(View.VISIBLE);
    }

    // 蓝牙连接
    public void connectBlueth() {
        bta = BluetoothAdapter.getDefaultAdapter();
        if (bta != null) {
            if (!bta.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                MainActivity.this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Intent intent = new Intent(MainActivity.this, ActivityBluetoothDeviceList.class);
                MainActivity.this.startActivityForResult(intent, REQUEST_DISCOVERY);
            }
        } else {
            Toast.makeText(MainActivity.this, "不能打开蓝牙,程序即将关闭", Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(MainActivity.this, ActivityBluetoothDeviceList.class);
                    MainActivity.this.startActivityForResult(intent, REQUEST_DISCOVERY);
                } else {
                    Log.d("error", "BT 没开");
                    Toast.makeText(MainActivity.this, "不能打开蓝牙,程序即将关闭", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_DISCOVERY:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        connect((BluetoothDevice) data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    }
                    break;
                }
        }
    }


    public void connect(BluetoothDevice device) {
        mDispatcher = Dispatcher.getInstance();
        mDispatcher.registerBTConnState(MainActivity.this);
        if (mDispatcher.connectBTandRecv(device)) {
            mHandler.obtainMessage(CONNBTNINVISIBLE).sendToTarget();
        }
    }

    /**
     * menu菜单   搜索蓝牙设备，建立连接
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.serch) {
            connectBlueth();
        } else if (item.getItemId() == R.id.exit) {
            exitApp();
        } else if (item.getItemId() == R.id.silence) {
            if (!isSilence) {
                isSilence = true;
                setVoice_Value(0);
            } else {
                isSilence = false;
                setVoice_Value(100);
            }
        }

        return true;
    }

    private void exitApp() {
        System.exit(0);
    }


    private boolean isOneLevel = true;

    @Override
    public void show(Bundle b) {
        //收到报文
        mHandler.obtainMessage(HAS_MSG).sendToTarget();
        
        if (b.getInt("bitmap_or_surfaceview") == CanMsgInfo.DISPLAYTYPE.BITMAP.ordinal()) {
            Message msg = mHandler.obtainMessage(TYPE_BITMAP);
            if (b.getInt("TIME") == 500)
                isOneLevel = true;
            else
                isOneLevel = false;
            msg.setData(b);
            msg.sendToTarget();
        } else if (b.getInt("bitmap_or_surfaceview") == CanMsgInfo.DISPLAYTYPE.SURFACEVIEW.ordinal()) {
            Message msg = mHandler.obtainMessage(TYPE_SURFACE);
            msg.setData(b);
            msg.sendToTarget();
        }
    }

    /**
     * 根据资源ID，设置显示图片&声音
     *
     * @param RImgID_1,RImgID_2 资源ID
     * @param RVoiceId          声源ID
     * @param time              1/频率
     */
    private void displayImg(int RImgID_1, int RImgID_2, int RVoiceId, int time) {
        display_1.setImageBitmap(BitmapFactory.decodeResource(getResources(), RImgID_1)); // 后面一张不动的图片
        display_2.setImageBitmap(BitmapFactory.decodeResource(getResources(), RImgID_2)); // 闪烁的图片

        if (mToneGenerator == null) {
            mToneGenerator = new ToneGenerator(RVoiceId, Voice_Value);//VOICE_LEVEL == RVoiceId
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mHandler.obtainMessage().sendToTarget();
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new BlinkingImgView(), 0, 500);
    }

    private class BlinkingImgView extends TimerTask {
        private boolean isFlag = false;

        @Override
        public void run() {
            if (isFlag) {
                isFlag = false;
                mHandler.obtainMessage(VISIBLE).sendToTarget();
                if (isOneLevel)
                    mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500);
                else
                    mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200);
            } else {
                if (!isOneLevel)
                    mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200);
                isFlag = true;
                mHandler.obtainMessage(IMVISIBLE).sendToTarget();
            }
        }
    }


    @Override
    public void stop() {
        if((mDynamicView.getVisibility() == View.GONE || display_1.getVisibility() == View.GONE) && mHasRecv.getVisibility() == View.GONE)
           mHandler.obtainMessage(HAS_NO_MSG).sendToTarget();
        
//        if (mDynamicView.getVisibility() == View.VISIBLE || mHasRecv.getVisibility() == View.VISIBLE)
        if (mDynamicView.getVisibility() == View.VISIBLE)
            mHandler.obtainMessage(123).sendToTarget();

        if (display_1.getVisibility() == View.VISIBLE)
            mHandler.obtainMessage(STOPTIMER).sendToTarget();
    }


    public static final int TYPE_BITMAP = 1;
    public static final int TYPE_SURFACE = 2;
    public static final int VISIBLE = 3;
    public static final int IMVISIBLE = 4;
    public static final int CONNBTNINVISIBLE = 5;
    public static final int CONNBTNVISIBLE = 6;
    public static final int STOPTIMER = 7;
    public static final int ISSAFE = 8;
    public static final int HAS_MSG = 9;//收到报文
    public static final int HAS_NO_MSG = 10;//收到报文
    private static final int VOICE_LEVEL = AudioManager.STREAM_MUSIC;
    private int nowScence = -1;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case TYPE_BITMAP:
                Bundle b = msg.getData();
                if (b == null)
                    break;
                int RidImg_1, RidImg_2, time;
                RidImg_1 = b.getInt("BMAP1");
                RidImg_2 = b.getInt("BMAP2");
                time = b.getInt("time");

                if (nowScence != RidImg_1 && RidImg_1 != 0) {
                    nowScence = RidImg_1;
                    mDynamicView.setVisibility(View.GONE);
                    displayImg(RidImg_1, RidImg_2, VOICE_LEVEL, time);
                }

                if (b.getInt("DisplayLevel") == CanMsgCache.Segment.LEVEL.SAFE.getLevel()) {
// 该段代码为 在收到541安全报文时，在界面显示
//                    if (b.getBoolean(AppContant.HAS_541_SAFE, false)) {
//                        //有541安全报文
//                        mHasRecv.setVisibility(View.VISIBLE);
//                        mHandler.obtainMessage(ISSAFE).sendToTarget();
//                        //TODO 重复代码
//                        if (display_1.getVisibility() == View.VISIBLE)
//                            mHandler.obtainMessage(STOPTIMER).sendToTarget();
//                    } else {
//                        stop();
//                    }
                }
                break;

            case TYPE_SURFACE:
                mDynamicView.setVisibility(View.VISIBLE);
                mHandler.obtainMessage(STOPTIMER).sendToTarget();
                Bundle sb = msg.getData();
                if (sb == null)
                    break;
                mDynamicView.updateSV(sb);
                if (sb.getInt("DisplayLevel") == CanMsgCache.Segment.LEVEL.SAFE.getLevel()) {
// 该段代码为 在收到541安全报文时，在界面显示
//                if (sb.getBoolean(AppContant.HAS_541_SAFE, false)) {
//                    //有541安全报文
//                    mHasRecv.setVisibility(View.VISIBLE);
//                    mHandler.obtainMessage(ISSAFE).sendToTarget();
//                    //TODO 重复代码
//                    if (display_1.getVisibility() == View.VISIBLE)
//                        mHandler.obtainMessage(STOPTIMER).sendToTarget();
//                } else {
//                    stop();
//                }
            }
                break;

            case VISIBLE: //图片闪烁
                if (mDynamicView.getVisibility() == View.VISIBLE)
                    mDynamicView.setVisibility(View.GONE);
                display_1.setVisibility(View.VISIBLE);
                display_2.setVisibility(View.VISIBLE);
                break;

            case IMVISIBLE:
                if (mDynamicView.getVisibility() == View.VISIBLE)
                    mDynamicView.setVisibility(View.GONE);
                display_1.setVisibility(View.VISIBLE);
                display_2.setVisibility(View.INVISIBLE);
                break;
            case CONNBTNINVISIBLE:
                mConnection.setVisibility(View.GONE);
                mHasRecv.setVisibility(View.VISIBLE);
                break;
            case CONNBTNVISIBLE:
                mConnection.setVisibility(View.VISIBLE);
                mHasRecv.setVisibility(View.GONE);
                break;
            case STOPTIMER:   //停止定时器，停止闪烁
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                    nowScence = -1;
                    display_1.setVisibility(View.GONE);
                    display_2.setVisibility(View.GONE);
                }
                break;
            case ISSAFE:   //接受541且安全
                display_1.setVisibility(View.GONE);
                display_2.setVisibility(View.GONE);
                mDynamicView.setVisibility(View.GONE);
                break;
            case HAS_MSG:   //接受到报文
                mHasRecv.setVisibility(View.GONE);
                break;
            case HAS_NO_MSG:   //未接受到报文
                mHasRecv.setVisibility(View.VISIBLE);
                break;
            default:
                display_1.setVisibility(View.GONE);
                display_2.setVisibility(View.GONE);
                mDynamicView.setVisibility(View.GONE);
//                mHasRecv.setVisibility(View.GONE);
//                m
                break;
        }
        return true; //信息都在此处理
    }

    /**
     * button监听器
     *
     * @param v
     */
    public void connectionClick(View v) {
        Animation anim = this.clickAnimation(500);
        v.startAnimation(anim);
        connectBlueth();
    }

    private static Animation clickAnimation(long durationMillis) {
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(AnimationUtils.getAlphaAnimation(1.0f, 0.5f, durationMillis));
        set.addAnimation(AnimationUtils.getScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f, durationMillis));
        set.setDuration(durationMillis);
        return set;
    }

    @Override
    public void update(Observable observable, Object data) {
        mHandler.obtainMessage(CONNBTNVISIBLE).sendToTarget();
    }

    public void myUpdate(Object data) {
        mHandler.obtainMessage(CONNBTNVISIBLE).sendToTarget();
    }

    public void setVoice_Value(int voice_Value) {
        Voice_Value = voice_Value;
        mToneGenerator = new ToneGenerator(VOICE_LEVEL, voice_Value);
    }
}