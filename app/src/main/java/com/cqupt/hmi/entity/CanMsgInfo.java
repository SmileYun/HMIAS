package com.cqupt.hmi.entity;

import com.cqupt.hmi.model.threaten.CanMsgCache.Segment;


public class CanMsgInfo {
    private int mCanID; // 2

    private byte[] mData; // 8

    public static enum DISPLAYTYPE{BITMAP, SURFACEVIEW, UNKNOW}




    public CanMsgInfo(Segment info) {
        mCanID = info.getCanID();
        mData = info.getData();
    }

    public CanMsgInfo(int id, byte[] data) {
        super();
        this.mCanID = id;
        this.mData = data;
    }

    public int getId() {
        return mCanID;
    }

    public void setId(int id) {
        this.mCanID = id;
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        this.mData = data;
    }

    /**
     *
     * 返回处理类型   BMHandler.LEVEL SVHandler.LEVEL
     * @return
     * DISPLAYTYPE    返回类型
     */
    public DISPLAYTYPE getType(){
        DISPLAYTYPE _type = DISPLAYTYPE.UNKNOW;
        switch (((mData[0] & 0xfc) >> 2)){
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
            case 0x05:
            case 0x06:
                _type=DISPLAYTYPE.BITMAP;
                break;
            case 0x04:
            case 0x07:
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0c:
                _type=DISPLAYTYPE.SURFACEVIEW;
                break;
        }

        return _type;
    }


}
