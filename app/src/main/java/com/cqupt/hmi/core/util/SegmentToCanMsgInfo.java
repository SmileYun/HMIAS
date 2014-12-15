package com.cqupt.hmi.core.util;


import com.cqupt.hmi.entity.CanMsgInfo;
import com.cqupt.hmi.model.threaten.CanMsgCache;

public class SegmentToCanMsgInfo {

    public static CanMsgInfo segmentToCanMsgInfo(CanMsgCache.Segment segment) {
        CanMsgInfo canMsgInfo = new CanMsgInfo(segment);
        canMsgInfo.setId(segment.getCanID());
        canMsgInfo.setData(segment.getData());
        return canMsgInfo;
    }

}
