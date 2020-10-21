package com.xingchen.imageselector.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.xingchen.imageselector.entry.Image;
import com.xingchen.imageselector.utils.ResolutionLimitedTarget;

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
}
