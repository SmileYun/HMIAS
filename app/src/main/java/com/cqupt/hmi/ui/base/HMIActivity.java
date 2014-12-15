package com.cqupt.hmi.ui.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.cqupt.hmi.R;
import com.cqupt.hmi.core.ioc.CCIoCEventListener;
import com.cqupt.hmi.core.ioc.CCIoCView;
import com.cqupt.hmi.persenter.Dispatcher;

import java.lang.reflect.Field;

public abstract class HMIActivity extends FragmentActivity implements IUI {
    protected String TAG = "HMIActivity";
    public static int mScreenWidth;
    public static int mScreenHeight;
    protected float mScreenDensity;
    protected DisplayMetrics mDisplayMetrics;
    @CCIoCView(id = R.id.main_content_linearlayout)
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        getScreenDisplay();
        iocInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this instanceof IUI){
           Dispatcher.getInstance().setIUI(this);
//			System.out.println("----r--->" + TAG +"setIUI");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this instanceof IUI){
//			Dispatcher.getInstance().setIUI(null);
        }
    }

    @SuppressWarnings("deprecation")
    private void getScreenDisplay() {
        Display _d = getWindowManager().getDefaultDisplay();
        mScreenHeight = _d.getHeight();
        mScreenWidth = _d.getWidth();
        mDisplayMetrics = new DisplayMetrics();
        _d.getMetrics(mDisplayMetrics);
        mScreenDensity = mDisplayMetrics.density;
    }

    protected final void iocInit() {
        Field[] fields = null;
        Class<? extends HMIActivity> clazz = getClass();
        do {
            fields = clazz.getDeclaredFields();
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    try {
                        field.setAccessible(true);

                        if (field.get(this) != null)
                            continue;

                        CCIoCView viewInject = field.getAnnotation(CCIoCView.class);
                        if (viewInject != null) {

                            int viewId = viewInject.id();
                            field.set(this, findViewById(viewId));

                            setListener(field, viewInject.onClick(), CCIoCEventListener.CLICK);
                            setListener(field, viewInject.longClick(), CCIoCEventListener.LONGCLICK);
                            setListener(field, viewInject.itemClick(), CCIoCEventListener.ITEMCLICK);
                            setListener(field, viewInject.itemLongClick(), CCIoCEventListener.ITEMLONGCLICK);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            clazz = (Class<HMIActivity>) clazz.getSuperclass();
            if (clazz.getSimpleName().equals("FragmentActivity"))
                break;
        } while ((fields = clazz.getDeclaredFields()) != null);
    }

    /**
     *
     * @param field
     *            the field
     * @param methodName
     *            the method name
     * @param method
     *            the method
     * @throws Exception
     *             the exception
     */
    private void setListener(Field field, String methodName, int method) throws Exception {
        if (methodName == null || methodName.trim().length() == 0)
            return;

        Object obj = field.get(this);

        switch (method) {
            case CCIoCEventListener.CLICK:
                if (obj instanceof View) {
                    ((View) obj).setOnClickListener(new CCIoCEventListener(this).click(methodName));
                }
                break;
            case CCIoCEventListener.ITEMCLICK:
                if (obj instanceof AbsListView) {
                    ((AbsListView) obj).setOnItemClickListener(new CCIoCEventListener(this).itemClick(methodName));
                }
                break;
            case CCIoCEventListener.LONGCLICK:
                if (obj instanceof View) {
                    ((View) obj).setOnLongClickListener(new CCIoCEventListener(this).longClick(methodName));
                }
                break;
            case CCIoCEventListener.ITEMLONGCLICK:
                if (obj instanceof AbsListView) {
                    ((AbsListView) obj).setOnItemLongClickListener(new CCIoCEventListener(this).itemLongClick(methodName));
                }
                break;
            default:
                break;
        }
    }

    protected void setMainContentView(int layoutResID) {
//		LayoutParams lParams = new LayoutParams(this, null);
//		lParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setMainContentView(layoutResID, null);
    }

    protected void setMainContentView(int layoutResID,LayoutParams lp) {
        View mainView = getLayoutInflater().inflate(layoutResID, null);
        if (mainLayout == null)
            mainLayout = (RelativeLayout) findViewById(R.id.main_content_linearlayout);
        mainLayout.addView(mainView);
        iocInit();
    }
}