package com.xingchen.imageselector.view;

import android.content.Context;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.xingchen.imageselector.R;

/**
 * 解决视频被裁剪成正方形
 * 因为某些视频是 270 度的旋转属性，导致渲染布局在布局的时候，需要 Rotate ，然后旋转导致重新布局计算大小的时候 RelativeLayout 的最终返回大小计算不对。
 */
public class MyGSYVideoPlayer extends StandardGSYVideoPlayer {
    public MyGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public MyGSYVideoPlayer(Context context) {
        super(context);
    }

    public MyGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_fixed_cropped;
    }
}
