package com.xingchen.imageselector.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
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
        Glide.with(mContext)
                .load(mImageList.get(position).getContentUri())
                .thumbnail(0.1f)
                .override(2048, 2048)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(currentView);
        container.addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object instanceof PhotoView) {
            PhotoView view = (PhotoView) object;
            view.setImageDrawable(null);
            container.removeView(view);
        }
    }
}
