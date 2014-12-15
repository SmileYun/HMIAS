package com.cqupt.hmi.core.ioc;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.lang.reflect.Method;

public class CCIoCEventListener implements OnClickListener, OnLongClickListener, OnItemClickListener, OnItemLongClickListener {

    private Object mHandler;

    private String ClickName;

    private String LongClickName;

    private String ItemClickName;

    private String ItemLongClickName;

    /** The Constant CLICK. */
    public static final int CLICK = 0;

    /** The Constant LONGCLICK. */
    public static final int LONGCLICK = 1;

    /** The Constant ITEMCLICK. */
    public static final int ITEMCLICK = 2;

    /** The Constant ITEMLONGCLICK. */
    public static final int ITEMLONGCLICK = 3;

    public CCIoCEventListener(Object handler) {
        this.mHandler = handler;
    }

    public CCIoCEventListener longClick(String name) {
        this.LongClickName = name;
        return this;
    }

    public CCIoCEventListener click(String name) {
        this.ClickName = name;
        return this;
    }

    public CCIoCEventListener itemClick(String name) {
        this.ItemClickName = name;
        return this;
    }

    public CCIoCEventListener itemLongClick(String name) {
        this.ItemLongClickName = name;
        return this;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return invokeItemLongClick(mHandler, ItemLongClickName, parent, view, position, id);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        invokeItemClick(mHandler, ItemClickName, parent, view, position, id);
    }

    @Override
    public boolean onLongClick(View v) {
        return invokeLongClick(mHandler, LongClickName, v);
    }

    @Override
    public void onClick(View v) {
        invokeClick(mHandler, ClickName, v);
    }

    /**
     *
     * @param handler
     * @param methodName
     * @param params
     * @return Object 返回类型
     */
    private Object invokeClick(Object handler, String methodName, Object... params) {
        if (handler == null)
            return null;
        Method method = null;
        try {
            method = handler.getClass().getDeclaredMethod(methodName, View.class);
            method.setAccessible(true);
            if (method != null)
                return method.invoke(handler, params);
            else
                throw new CCAppException("no such method:" + methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * 调用LongClick()
     *
     * @param handler
     * @param methodName
     * @param params
     * @return true , if successful
     */
    private boolean invokeLongClick(Object handler, String methodName, Object... params) {
        if (handler == null)
            return false;
        Method method = null;
        try {
            // public boolean onLongClick(View v)
            method = handler.getClass().getDeclaredMethod(methodName, View.class);
            method.setAccessible(true);
            if (method != null) {
                Object obj = method.invoke(handler, params);
                return obj == null ? false : Boolean.valueOf(obj.toString());
            } else
                throw new CCAppException("no such method:" + methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @param handler
     * @param methodName
     * @param params
     * @return Object 返回类型
     */
    private Object invokeItemClick(Object handler, String methodName, Object... params) {
        if (handler == null)
            return null;
        Method method = null;
        try {

            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            method.setAccessible(true);
            if (method != null)
                return method.invoke(handler, params);
            else
                throw new CCAppException("no such method:" + methodName);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean invokeItemLongClick(Object handler, String methodName, Object... params) {
        Method method = null;
        try {
            if (handler == null) {
                throw new com.cqupt.hmi.core.ioc.CCAppException("invokeItemLongClickMethod: handler is null :");
            }
            // /onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,long
            // arg3)
            method = handler.getClass().getDeclaredMethod(methodName, AdapterView.class, View.class, int.class, long.class);
            method.setAccessible(true);
            if (method != null) {
                Object obj = method.invoke(handler, params);
                return Boolean.valueOf(obj == null ? false : Boolean.valueOf(obj.toString()));
            } else
                throw new CCAppException("no such method:" + methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
