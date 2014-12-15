package com.cqupt.hmi.core.util;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * Created by Yun on 2014/12/12.
 */
public class AnimationUtils {
    public static Animation getAlphaAnimation(float from, float to, long duration) {
        Animation alpha = new AlphaAnimation(from, to);
        alpha.setDuration(duration);
        alpha.setFillAfter(true);
        return alpha;
    }

    public static Animation getScaleAnimation(float fromX, float toX, float fromY, float toY, long duration) {
        Animation alpha = new ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        alpha.setDuration(duration);
        alpha.setFillAfter(true);
        return alpha;
    }
}
