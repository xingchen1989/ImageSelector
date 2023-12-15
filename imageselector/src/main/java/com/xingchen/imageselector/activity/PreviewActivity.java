package com.xingchen.imageselector.activity;

import static com.xingchen.imageselector.model.ImageModel.mSelectImages;
import static com.xingchen.imageselector.model.ImageModel.mTotalImages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.gyf.immersionbar.ImmersionBar;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.adapter.FixExceptionViewPager;
import com.xingchen.imageselector.adapter.ImagePagerAdapter;
import com.xingchen.imageselector.entry.ImageData;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ImageSelector;

public class PreviewActivity extends AppCompatActivity {
    private ImageView ivBack;
    private ImageView ivSelect;
    private TextView tvSelect;
    private TextView tvIndicator;
    private FrameLayout btnConfirm;
    private FixExceptionViewPager fixViewPager;
    private RequestConfig config;//图片浏览器的配置信息

    public static void openActivity(Activity activity, RequestConfig config, int position, int requestCode) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(ImageSelector.POSITION, position);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        initView();
        initListener();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ImmersionBar.with(this).titleBar(R.id.cl_title).init();
        config = (RequestConfig) getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
        ivBack = findViewById(R.id.iv_back);
        tvSelect = findViewById(R.id.tv_select);
        ivSelect = findViewById(R.id.iv_select);
        btnConfirm = findViewById(R.id.btn_confirm);
        tvIndicator = findViewById(R.id.tv_indicator);
        fixViewPager = findViewById(R.id.fix_vp_image);
        fixViewPager.setAdapter(new ImagePagerAdapter(mTotalImages));
        fixViewPager.addOnPageChangeListener(new PageChangeListener());
        fixViewPager.setCurrentItem(getIntent().getIntExtra(ImageSelector.POSITION, 0));
    }

    private void initListener() {
        tvSelect.setOnClickListener(v -> {
            updateData(fixViewPager.getCurrentItem());
            updateView(fixViewPager.getCurrentItem());
        });

        btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });

        ivBack.setOnClickListener(v -> finish());
    }

    private void updateView(int position) {
        btnConfirm.setEnabled(mSelectImages.size() != 0);
        ivSelect.setSelected(mTotalImages.get(position).isSelected());
        tvIndicator.setText(String.format("%1$s/%2$s", position + 1, mTotalImages.size()));
    }

    private void updateData(int position) {
        ImageData imageData = mTotalImages.get(position);
        if (mSelectImages.contains(imageData)) {
            mSelectImages.remove(imageData);
            imageData.setSelected(false);
        } else if (config.isSingle) {
            for (ImageData item : mSelectImages) {
                item.setSelected(false);
            }
            mSelectImages.clear();
            mSelectImages.add(imageData);
            imageData.setSelected(true);
        } else if (config.maxCount <= 0 || mSelectImages.size() < config.maxCount) {
            mSelectImages.add(imageData);
            imageData.setSelected(true);
        }
    }

    private class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            updateView(position);
        }
    }
}
