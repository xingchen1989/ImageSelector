package com.xingchen.imageselector.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.xingchen.imageselector.entry.Image;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<Image> mImageList;

    public ImagePagerAdapter(Context mContext, List<Image> mImageList) {
        this.mContext = mContext;
        this.mImageList = mImageList;
    }

    @Override
    public int getCount() {
        return mImageList == null ? 0 : mImageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        PhotoView currentView = new PhotoView(mContext);
        currentView.setMaximumScale(10.0f);
        Uri uri = mImageList.get(position).getContentUri();
        Glide.with(mContext)
                .asBitmap()
                .load(uri)
                .thumbnail(0.1f)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(new ResolutionLimitedTarget(currentView));
        container.addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    /**
     * 限制图片分辨率，避免图片过大时，Glide自动压缩后图片不清晰
     */
    private static class ResolutionLimitedTarget extends BitmapImageViewTarget {
        /**
         * 分辨率阈值
         */
        private final float resolution = 1920f;

        ResolutionLimitedTarget(ImageView view) {
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

            if (srcWidth > resolution && srcHeight > resolution) {
                float scale = resolution / Math.max(srcWidth, srcHeight);
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                return Bitmap.createBitmap(resource, 0, 0, srcWidth, srcHeight, matrix, true);
            }
            return Bitmap.createBitmap(resource, 0, 0, srcWidth, srcHeight);
        }
    }
}
