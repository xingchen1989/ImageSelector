package com.xingchen.imageselector.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * 限制图片分辨率，避免图片过大时，Glide自动压缩后图片不清晰
 */
public class ResolutionLimitedTarget extends BitmapImageViewTarget {
    /**
     * 默认分辨率阈值
     */
    public static final float DEFAULT_RESOLUTION = 2560f;

    public ResolutionLimitedTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        super.onResourceReady(resizeBitmap(resource), transition);
    }

    /**
     * 重新调整Bitmap大小
     *
     * @param resource
     * @return
     */
    private Bitmap resizeBitmap(Bitmap resource) {
        int srcWidth = resource.getWidth();
        int srcHeight = resource.getHeight();

        if (srcWidth > DEFAULT_RESOLUTION && srcHeight > DEFAULT_RESOLUTION) {
            float scale = DEFAULT_RESOLUTION / Math.max(srcWidth, srcHeight);
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            return Bitmap.createBitmap(resource, 0, 0, srcWidth, srcHeight, matrix, true);
        }
        return Bitmap.createBitmap(resource, 0, 0, srcWidth, srcHeight);
    }
}
