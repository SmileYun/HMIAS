package com.cqupt.hmi.model.threaten;

import android.media.AudioManager;
import android.os.Bundle;

import com.cqupt.hmi.R;
import com.cqupt.hmi.entity.CanMsgInfo;
import com.cqupt.hmi.entity.CanMsgInfo.DISPLAYTYPE;
import com.cqupt.hmi.persenter.Dispatcher;


public class BMHandler extends Dispatcher.AbHandler {

    private static final DISPLAYTYPE LEVEL = DISPLAYTYPE.BITMAP;

    public BMHandler() {
        super(LEVEL);
    }

    private static final int LEVEL1TIME = 500;
    private static final int LEVEL2TIME = 200;
    private static final int AUDIO = AudioManager.STREAM_ALARM;
    private static final int[] RID1 = new int[]{
            R.drawable.rew,
            R.drawable.rew,
            R.drawable.fcw,
            R.drawable.fcw,
            R.drawable.icw_left,
            R.drawable.icw_right,
            R.drawable.icw_left,
            R.drawable.icw_right,
            R.drawable.cfcw,
            R.drawable.cfcw,
            R.drawable.dnpw,
            R.drawable.dnpw,
            R.drawable.lcw_left,
            R.drawable.lcw_right,
            R.drawable.lcw_left,
            R.drawable.lcw_right
    };
    private static final int[] RID2 = new int[]{
            R.drawable.rew_1,
            R.drawable.rew_2,
            R.drawable.fcw_1,
            R.drawable.fcw_2,
            R.drawable.icw_1_left,
            R.drawable.icw_1_right,
            R.drawable.icw_2_left,
            R.drawable.icw_2_right,
            R.drawable.cfcw_1,
            R.drawable.cfcw_2,
            R.drawable.dnpw_1,
            R.drawable.dnpw_2,
            R.drawable.lcw_1_left,
            R.drawable.lcw_1_right,
            R.drawable.lcw_2_left,
            R.drawable.lcw_2_right
    };

    @Override
    public Bundle response(CanMsgInfo cinfo) {
        Bundle bd = new Bundle();
        bd.putInt("DisplayLevel", cinfo.getmDisplayType());
        byte[] info = cinfo.getData();
        int alaLevel = info[0] & 0x03;
        int dir = (info[4] >> 6) & 0x03;

        switch (((info[0] & 0xfc) >> 2)) {
            case 0x00: //rew
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[0], RID2[0], AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x02) {

                    bundlePutInt(bd, RID1[1], RID2[1], AUDIO, LEVEL2TIME);

                }
                break;
            case 0x01:// fcw
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[2], RID2[2], AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x02) {

                    bundlePutInt(bd, RID1[3], RID2[3], AUDIO, LEVEL2TIME);

                }
                break;
            case 0x02:// icw
                if (alaLevel == 0x01 && dir == 0x01) {

                    bundlePutInt(bd, RID1[4], RID2[4], AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x01 && dir == 0x02) {

                    bundlePutInt(bd, RID1[5], RID2[5], AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x02
                        && dir == 0x01) {

                    bundlePutInt(bd, RID1[6], RID2[6], AUDIO, LEVEL2TIME);

                } else if (alaLevel == 0x02 && dir == 0x02) {

                    bundlePutInt(bd, RID1[7], RID2[7], AUDIO, LEVEL2TIME);

                }
                break;
            case 0x03:// cfcw
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[8], RID2[8], AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x02) {

                    bundlePutInt(bd, RID1[9], RID2[9], AUDIO, LEVEL2TIME);

                }
                break;
            case 0x05:// dnpw
                if (alaLevel == 0x01) {

                    bundlePutInt(bd, RID1[10], RID2[10], AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x02) {

                    bundlePutInt(bd, RID1[11], RID2[11], AUDIO, LEVEL2TIME);

                }
                break;

            case 0x06:// lcw
                if (alaLevel == 0x01 && dir == 0x01) {

                    bundlePutInt(bd, RID1[12], RID2[12], AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x01
                        && dir == 0x02) {

                    bundlePutInt(bd, RID1[13], RID2[13], AUDIO, LEVEL1TIME);

                } else if (alaLevel == 0x02
                        && dir == 0x01) {

                    bundlePutInt(bd, RID1[14], RID2[14], AUDIO, LEVEL2TIME);

                } else if (alaLevel == 0x02
                        && dir == 0x02) {

                    bundlePutInt(bd, RID1[15], RID2[15], AUDIO, LEVEL2TIME);

                }
                break;

        }
        return bd;
    }

    private Bundle bundlePutInt(Bundle bundle, int bmapID1, int bmapID2, int audio, int time) {
        bundle.putInt("bitmap_or_surfaceview", LEVEL.ordinal());
        bundle.putInt("BMAP1", bmapID1);
        bundle.putInt("BMAP2", bmapID2);
        bundle.putInt("AUDIO", audio);
        bundle.putInt("TIME", time);
        return bundle;
    }

}
