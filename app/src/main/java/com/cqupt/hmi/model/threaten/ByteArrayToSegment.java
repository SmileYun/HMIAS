package com.cqupt.hmi.model.threaten;

import android.util.Log;

import com.cqupt.hmi.model.RecvThread.RawCanMsgHandler;
import com.cqupt.hmi.model.threaten.CanMsgCache.Segment;
import com.cqupt.hmi.model.threaten.CanMsgCache.Segment.LEVEL;

public class ByteArrayToSegment implements RawCanMsgHandler {

    @Override
    public Segment handlerRawCanMsg(char[] mCanMsgCharBuffer) {
        int canId = 0;
        byte[] data = new byte[8];
        Segment _s = new Segment();

        LEVEL level = getLevel(mCanMsgCharBuffer);
        canId = getCanId(mCanMsgCharBuffer);
        data = getData(mCanMsgCharBuffer);

        _s.setLevel(level);
        _s.setCanID(canId);
        _s.setData(data);
        _s.setFlag(1);

        return _s;
    }

    private byte[] getData(char[] mCanMsgCharBuffer) {
        byte[] data = new byte[8];
        for (int i = 2; i < mCanMsgCharBuffer.length; i++) {
            data[i - 2] = (byte) mCanMsgCharBuffer[i];
        }
        return data;
    }

    private LEVEL getLevel(char[] mCanMsgCharBuffer) {
        LEVEL _level = null;
        if (mCanMsgCharBuffer[0] == 0x41 && isAlarmScence(mCanMsgCharBuffer)) {
            _level = LEVEL.HIGH;
        } else if (mCanMsgCharBuffer[0] == 0x31 && isAlarmScence(mCanMsgCharBuffer)) {
            _level = LEVEL.MIDDLE;
        } else if (mCanMsgCharBuffer[0] == 0x21 && isAlarmScence(mCanMsgCharBuffer)) {
            _level = LEVEL.LOW;
        } else {// if (isSafeScence(mCanMsgCharBuffer)) {
            _level = LEVEL.SAFE;
        }
        return _level;
    }

    private int getCanId(char[] mCanMsgCharBuffer) {
        int canId = new Integer(0);
        canId = (mCanMsgCharBuffer[1] << 8) + mCanMsgCharBuffer[0];
        return canId;
    }

    //
    private boolean isAlarmScence(char[] mCanMsgCharBuffer) {
        if ((((mCanMsgCharBuffer[2] & 0xfc) >> 2) >= 0x00) && (((mCanMsgCharBuffer[2] & 0xfc) >> 2) < 0x0D)) {
            if (((mCanMsgCharBuffer[2] % 4) == 0x01) || ((mCanMsgCharBuffer[2] % 4) == 0x02)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //
    private boolean isSafeScence(char[] mCanMsgCharBuffer) {
        if ((((mCanMsgCharBuffer[2] & 0xfc) >> 2) >= 0x00) && (((mCanMsgCharBuffer[2] & 0xfc) >> 2) < 0x0D)) {
            if (((mCanMsgCharBuffer[2] % 4) == 0x00)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
