package com.xingchen.imageselector.activity;

import static com.xingchen.imageselector.model.MediaModel.mSelectMedias;
import static com.xingchen.imageselector.model.MediaModel.mTotalMedias;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.gyf.immersionbar.ImmersionBar;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.adapter.ImagePagerAdapter;
import com.xingchen.imageselector.databinding.ActivityMediaPreviewBinding;
import com.xingchen.imageselector.entry.MediaData;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ImageSelector;

public class PreviewActivity extends AppCompatActivity {
    private ActivityMediaPreviewBinding binding;
    private RequestConfig requestConfig;//图片浏览器的配置信息

    public static void openActivity(Activity activity, RequestConfig config, int position, int requestCode) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(ImageSelector.POSITION, position);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
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

    /**
     * 初始化控件
     */
    private void initView() {
        ImmersionBar.with(this).titleBar(R.id.cl_title).init();
        requestConfig = (RequestConfig) getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
        binding.fixVpImage.setAdapter(new ImagePagerAdapter(mTotalMedias));
        binding.fixVpImage.addOnPageChangeListener(new PageChangeListener());
        binding.fixVpImage.setCurrentItem(getIntent().getIntExtra(ImageSelector.POSITION, 0));
    }

    private void initListener() {
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
