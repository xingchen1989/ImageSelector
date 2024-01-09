package com.xingchen.imageselector.activity;

import static com.xingchen.imageselector.model.MediaModel.mSelectMedias;
import static com.xingchen.imageselector.model.MediaModel.mTotalMedias;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.gyf.immersionbar.ImmersionBar;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.adapter.MediaPagerAdapter;
import com.xingchen.imageselector.databinding.ActivityMediaPreviewBinding;
import com.xingchen.imageselector.entry.MediaData;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ImageSelector;
import com.xingchen.imageselector.view.MyGSYVideoPlayer;

public class PreviewActivity extends AppCompatActivity {
    private ActivityMediaPreviewBinding binding;
    private RequestConfig requestConfig;

    public static void openActivity(Activity activity, RequestConfig config, int position, int requestCode) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        intent.putExtra(ImageSelector.KEY_POSITION, position);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.clTitle.setVisibility(View.GONE);
            binding.clBottom.setVisibility(View.GONE);
        } else {
            binding.clTitle.setVisibility(View.VISIBLE);
            binding.clBottom.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ImmersionBar.with(this).titleBar(R.id.cl_title).init();
        int currentPosition = getIntent().getIntExtra(ImageSelector.KEY_POSITION, 0);
        requestConfig = (RequestConfig) getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
        binding.fixVpImage.setAdapter(new MediaPagerAdapter(mTotalMedias));
        binding.fixVpImage.addOnPageChangeListener(new PageChangeListener());
        binding.fixVpImage.setCurrentItem(currentPosition);
    }

    private MyGSYVideoPlayer getCurrentVisiblePlayer() {
        // 获取当前可见的GSYVideoPlayer实例
        int currentPosition = binding.fixVpImage.getCurrentItem();
        View currentView = binding.fixVpImage.getChildAt(currentPosition);
        if (currentView instanceof MyGSYVideoPlayer) {
            return (MyGSYVideoPlayer) currentView;
        }
        return null;
    }

    private void initListener() {
        binding.fixVpImage.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < binding.fixVpImage.getChildCount(); i++) {
                    View currentView = binding.fixVpImage.getChildAt(i);
                    if (currentView instanceof MyGSYVideoPlayer) {
                        MyGSYVideoPlayer videoPlayer = (MyGSYVideoPlayer) currentView;
                        if (videoPlayer.isInPlayingState()) videoPlayer.onVideoPause();
                    }
                }
            }
        });

        binding.tvSelect.setOnClickListener(v -> {
            updateData(binding.fixVpImage.getCurrentItem());
            updateView(binding.fixVpImage.getCurrentItem());
        });

        binding.btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });

        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void updateView(int position) {
        binding.btnConfirm.setEnabled(mSelectMedias.size() != 0);
        binding.ivSelect.setSelected(mTotalMedias.get(position).isSelected());
        binding.tvIndicator.setText(String.format("%1$s/%2$s", position + 1, mTotalMedias.size()));
    }

    private void updateData(int position) {
        MediaData mediaData = mTotalMedias.get(position);
        if (mSelectMedias.contains(mediaData)) {
            mSelectMedias.remove(mediaData);
            mediaData.setSelected(false);
        } else if (requestConfig.isSingle) {
            for (MediaData item : mSelectMedias) {
                item.setSelected(false);
            }
            mSelectMedias.clear();
            mSelectMedias.add(mediaData);
            mediaData.setSelected(true);
        } else if (requestConfig.maxCount <= 0 || mSelectMedias.size() < requestConfig.maxCount) {
            mSelectMedias.add(mediaData);
            mediaData.setSelected(true);
        }
    }

    private class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            updateView(position);
        }
    }
}
