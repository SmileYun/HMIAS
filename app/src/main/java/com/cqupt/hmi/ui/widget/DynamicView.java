package com.cqupt.hmi.ui.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.cqupt.hmi.R;
import com.cqupt.hmi.app.AppContant;
import com.cqupt.hmi.ui.base.HMIActivity;


public class DynamicView extends SurfaceView implements Callback {
    private final String TAG = "DynamicView";

    private Bundle info;

    private SurfaceHolder mHolder;

    private int nowBitmapResID = -1;

    private Bitmap nowBitmap = null;

    private Point[] positions = new Point[3];

    private Paint[] paints = new Paint[3];

    private float[] textFactors = {0.17f, 0.14f, 0.099f};

    private Context mContext;

    private RectF rectF;

    private int mScreenW, mScreenH;


    public DynamicView(Context context) {
        this(context, null);
    }

    public DynamicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0x52551314) {
                Canvas c = mHolder.lockCanvas();
                if (c != null)
                    drawDynamicView(c, info);
            }
        }

        ;
    };

    public DynamicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);

        for (int i = 0; i < paints.length; i++) {
            paints[i] = new Paint();
            paints[i].setColor(Color.RED);
//			Typeface mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/microyahei.ttf");
//			paints[i].setTypeface(mTypeface);
            paints[i].setTextSize(HMIActivity.mScreenWidth * textFactors[i % 3]);
            paints[i].setAntiAlias(true);
        }
        if (mContext instanceof HMIActivity) {
            mScreenW = ((HMIActivity) mContext).mScreenWidth;
            mScreenH = ((HMIActivity) mContext).mScreenHeight;
            rectF = new RectF(0, 0, mScreenW, mScreenH);
        }
    }

    //background_res_id
    private void drawDynamicView(Canvas canvas, Bundle moreInfo) {
        if (moreInfo.getInt("BMAP1") != nowBitmapResID) {
            nowBitmap = loadOptiBitmap(moreInfo.getInt("BMAP1", 0));
        }
        drawBackground(canvas, nowBitmap);
        Point[] _positions = (Point[]) moreInfo.getParcelableArray("locations");
        if (_positions != null) {
            positions = new Point[_positions.length];
            for (int i = 0; i < _positions.length; i++) {
                //if (positions == null)
                positions[i] = _positions[i];
            }
        } else {
            positions = null;
        }
        if (positions != null) {
            drawText(canvas, positions, paints);
        }
        mHolder.unlockCanvasAndPost(canvas);
    }


    private void drawBackground(Canvas canvas, Bitmap bitmap) {
        paints[0].setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paints[0]);
        paints[0].setXfermode(new PorterDuffXfermode(Mode.SRC));
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bitmap, null, rectF, null);
    }


    private void drawText(Canvas canvas, Point[] positions, Paint[] text) {
        int factor = 0;
        for (Point p : positions) {
            if (p != null) {
                String textDraw = info.getInt(AppContant.TEXT_STR_DRAWTEXT[factor]) + "";
                float textDrawLen = text[factor].measureText(textDraw);
                canvas.drawText(textDraw + factor, p.x - textDrawLen, p.y, text[factor]);
                factor++;
            }
        }

//		String timeStr = String.valueOf(info.getInt("time"));
//		float timeLen = paints[0].measureText(timeStr);
//		canvas.drawText(timeStr, mScreenW * info.getFloat("")-timeLen, (mScreenH * info.getFloat("")), paints[0]);
//		
//		String maxSpeed = String.valueOf(info.getInt("maxspeed"));
//		float SpeedLen = paints[1].measureText(maxSpeed);
//		canvas.drawText(maxSpeed, mScreenW * info.getFloat("") - SpeedLen, (mScreenH * info.getFloat("")), paints[1]);
//		
//		String curSpeed = String.valueOf(info.getInt("curspeed"));
//		float cSpeedLen = paints[2].measureText(curSpeed);
//		canvas.drawText(curSpeed, mScreenW * info.getFloat("") - cSpeedLen, (mScreenH * info.getFloat("")), paints[2]);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "-- surfaceView has been created! ");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        mHandler = null;
    }

    private Bitmap loadOptiBitmap(int resId) {
        if (resId != 0) {
            nowBitmapResID = resId;
        } else {
            nowBitmapResID = resId = R.drawable.icom;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inInputShareable = true;
        options.inPurgeable = true;
        BitmapFactory.decodeResource(getResources(), resId, options);
//		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inSampleSize = 1;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId, options);

        return bitmap;
    }

    public void updateSV(Bundle info) {
        this.info = info;
        Message msg = mHandler.obtainMessage();
//		msg.setData(info);
        msg.what = 0x52551314;
        msg.sendToTarget();
    }

}
