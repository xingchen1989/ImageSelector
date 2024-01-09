package com.xingchen.imageselector.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.xingchen.imageselector.entry.MediaData;
import com.xingchen.imageselector.view.MyGSYVideoPlayer;

import java.util.List;

public class MediaPagerAdapter extends PagerAdapter {

    private final List<MediaData> mediaList;

    public MediaPagerAdapter(List<MediaData> mediaList) {
        this.mediaList = mediaList;
    }

    @Override
    public int getCount() {
        return mediaList == null ? 0 : mediaList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        MediaData mediaData = mediaList.get(position);
        if (mediaData.getMimeType().startsWith("image")) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setMaximumScale(10.0f);
            Glide.with(container.getContext())
                    .load(mediaData.getContentUri())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(photoView);
            container.addView(photoView);
            return photoView;
        } else if (mediaData.getMimeType().startsWith("video")) {
            ImageView thumbImageView = new ImageView(container.getContext());
            MyGSYVideoPlayer videoPlayer = new MyGSYVideoPlayer(container.getContext());
            OrientationUtils orientation = new OrientationUtils((Activity) container.getContext(), videoPlayer);
            videoPlayer.getFullscreenButton().setOnClickListener(view -> orientation.resolveByClick());
            Glide.with(container.getContext())
                    .load(mediaData.getContentUri())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(thumbImageView);
            videoPlayer.setThumbImageView(thumbImageView);
            videoPlayer.getBackButton().setVisibility(View.INVISIBLE);
            videoPlayer.setUp(mediaData.getContentUri().toString(), true, "");
            container.addView(videoPlayer);
            return videoPlayer;
        }
        return new View(container.getContext());
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object instanceof MyGSYVideoPlayer) {
            MyGSYVideoPlayer videoPlayer = (MyGSYVideoPlayer) object;
            container.removeView(videoPlayer);
            videoPlayer.release();
        } else {
            container.removeView((View) object);
        }
    }
}
