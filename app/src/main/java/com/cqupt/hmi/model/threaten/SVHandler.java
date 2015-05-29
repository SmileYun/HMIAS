package com.cqupt.hmi.model.threaten;

import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;

import com.cqupt.hmi.R;
import com.cqupt.hmi.app.AppContant;
import com.cqupt.hmi.entity.CanMsgInfo;
import com.cqupt.hmi.entity.CanMsgInfo.DISPLAYTYPE;
import com.cqupt.hmi.persenter.Dispatcher;
import com.cqupt.hmi.ui.base.HMIActivity;

public class SVHandler extends Dispatcher.AbHandler {

    private static final DISPLAYTYPE LEVEL = DISPLAYTYPE.SURFACEVIEW;

    public SVHandler() {
        super(LEVEL);
    }

    private static final int LEVEL1TIME = 500;
    private static final int LEVEL2TIME = 200;
    private static final int AUDIO = AudioManager.STREAM_ALARM;
    //{推荐速度，当前速度，时间,}X
    private static final float[] SGX = new float[]{0.428f, 0.46f, 0.23f, 0.428f, 0.46f, 0.489f, 0.428f, 0.46f, 0.8f};
    private static final float[] SGY = new float[]{0.595f, 0.692f, 0.439f, 0.595f, 0.692f, 0.439f, 0.595f, 0.692f, 0.439f};
    private static final int[] RID1 = new int[]{
            R.drawable.cb,
            R.drawable.uvr,
            R.drawable.dsr,
            R.drawable.sg_green,
            R.drawable.sg_yellow,
            R.drawable.sg_red,
            R.drawable.ojr,
            R.drawable.ahs,
            R.drawable.cpp_1,
            R.drawable.cpp_2
    };

    @Override
    public Bundle response(CanMsgInfo cinfo) {
        Bundle bd = new Bundle();
        bd.putInt("bitmap_or_surfaceview", LEVEL.ordinal());
        bd.putInt("DisplayLevel", cinfo.getmDisplayType());
        byte[] info = cinfo.getData();
        //报警级别
        int alaLevel = info[0] % 4;
        //信号灯颜色
        int color = (info[4] >> 4) & 0x03;
        // 最大引导速度
        int maxSpeed = (info[1] & 0x7f);
        // 最小引导速度
        int minSpeed = (info[2] & 0x7f);
        // 当前行车速度
        int currentSpeed = (info[3] & 0xff);
        // 剩余时间
        int remainTime = (info[6] & 0x7f);
        // 危险点距离本车距离
        int dgrDis = (((info[4] & 0x03) << 8) + (info[5] & 0xff));

        bd.putInt(AppContant.SCENCE, ((info[0] & 0xfc) >> 2));
        switch (((info[0] & 0xfc) >> 2)) {
            case 0x04: //cb
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[0], AUDIO, LEVEL1TIME);

                }
                break;
            case 0x08:// dsr
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[2], AUDIO, LEVEL1TIME);

                }
                break;
            case 0x09:// sg
                if (alaLevel == 0x01) {
                    if (color == 0x00) {
                        //green
                        bundlePutInt(bd, RID1[3], maxSpeed, SGX[0], SGY[0],
                                currentSpeed, SGX[1], SGY[1],
                                remainTime, SGX[2], SGY[2], AUDIO, LEVEL1TIME);

                    } else if (color == 0x01) {
                        //yellow
                        bundlePutInt(bd, RID1[4], maxSpeed, SGX[3], SGY[3],
                                currentSpeed, SGX[4], SGY[4],
                                remainTime, SGX[5], SGY[5], AUDIO, LEVEL1TIME);

                    } else if (color == 0x02) {
                        //red
                        bundlePutInt(bd, RID1[5], maxSpeed, SGX[6], SGY[6],
                                currentSpeed, SGX[7], SGY[7],
                                remainTime, SGX[8], SGY[8], AUDIO, LEVEL1TIME);

                    }
                }
                break;
            case 0x0a:// ojr
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[6], dgrDis, 0.588f, 0.630f, AUDIO, LEVEL1TIME);

                }
                break;

            case 0x0b:// ahs
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[7], AUDIO, LEVEL1TIME);

                }
                break;
            case 0x0c:// cpp
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[8], dgrDis, 0.637f, 0.737f, AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x02) {

                    bundlePutInt(bd, RID1[9], dgrDis, 0.637f, 0.737f, AUDIO, LEVEL2TIME);

                }
                break;
            default:
                bd = null;
                break;
        }
        return bd;
    }

    //方法重载：
    //方法一：CB、DSR、AHS使用此方法
    private Bundle bundlePutInt(Bundle bundle, int bmapID1, int audio, int time) {
        bundle.putInt("BMAP1", bmapID1);
        bundle.putInt("AUDIO", audio);
        bundle.putInt("TIME", time);
        bundle.putParcelableArray("locations", null);
        return bundle;
    }

    //方法二：UVR、OJR、CPP使用此方法
    private Bundle bundlePutInt(Bundle bundle, int bmapID1, int distance, float xValue, float yValue, int audio, int time) {
        Point[] ps = new Point[1];
        ps[0] = new Point();
        bundle.putInt("BMAP1", bmapID1);
        bundle.putFloat("XVALUE", xValue);
        bundle.putFloat("YVALUE", yValue);
        ps[0].x = (int) (xValue * HMIActivity.mScreenWidth);
        ps[0].y = (int) (yValue * HMIActivity.mScreenHeight);
        bundle.putInt("AUDIO", audio);
        bundle.putInt("TIME", time);
        bundle.putInt(AppContant.TEXT_STR_DRAWTEXT[0], distance);
        bundle.putParcelableArray("locations", ps);
        return bundle;
    }

    //方法三：SG使用此方法
    private Bundle bundlePutInt(Bundle bundle, int bmapID1, int maxSpeed, float maxSpeedX, float maxSpeedY,
                                int currentSpeed, float currentSpeedX, float currentSpeedY,
                                int remainTime, float TimeX, float TimeY, int audio, int time) {
        Point[] ps = new Point[3];
        for (int i = 0; i < 3; i++) {
            ps[i] = new Point();
        }

        bundle.putInt("BMAP1", bmapID1);

        bundle.putInt(AppContant.TEXT_STR_DRAWTEXT[0], maxSpeed);
        bundle.putFloat("MAXX", maxSpeedX);
        bundle.putFloat("MAXY", maxSpeedY);
        ps[0].x = (int) (maxSpeedX * HMIActivity.mScreenWidth);
        ps[0].y = (int) (maxSpeedY * HMIActivity.mScreenHeight);
/*		bundle.putInt("MIN", minSpeed);
        bundle.putFloat("MINX", minSpeedX);
		bundle.putFloat("MINY", minSpeedY);*/
        bundle.putInt(AppContant.TEXT_STR_DRAWTEXT[1], currentSpeed);
        bundle.putFloat("CURX", currentSpeedX);
        bundle.putFloat("CURY", currentSpeedY);
        ps[1].x = (int) (currentSpeedX * HMIActivity.mScreenWidth);
        ps[1].y = (int) (currentSpeedY * HMIActivity.mScreenHeight);

        bundle.putInt(AppContant.TEXT_STR_DRAWTEXT[2], remainTime);
        bundle.putFloat("REMX", TimeX);
        bundle.putFloat("REMY", TimeY);

        ps[2].x = (int) (TimeX * HMIActivity.mScreenWidth);
        ps[2].y = (int) (TimeY * HMIActivity.mScreenHeight);
        bundle.putInt("AUDIO", audio);
        bundle.putInt("TIME", time);
        bundle.putParcelableArray("locations", ps);
        return bundle;
    }
}
