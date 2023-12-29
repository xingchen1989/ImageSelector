package com.xingchen.imageselector.adapter;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.xingchen.imageselector.entry.MediaData;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    private final List<MediaData> mImageList;

    public ImagePagerAdapter(List<MediaData> mImageList) {
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
        PhotoView currentView = new PhotoView(container.getContext());
        currentView.setMaximumScale(10.0f);
        Uri uri = mImageList.get(position).getContentUri();
        Glide.with(container.getContext())
                .load(uri)
                .thumbnail(0.1f)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(currentView);
        container.addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
