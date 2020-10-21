package com.xingchen.imageselector.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.xingchen.imageselector.R;
import com.xingchen.imageselector.adapter.FixExceptionViewPager;
import com.xingchen.imageselector.adapter.ImagePagerAdapter;
import com.xingchen.imageselector.entry.Image;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ImageSelector;
import com.xingchen.imageselector.utils.VersionUtils;

import java.util.ArrayList;

public class PreviewActivity extends AppCompatActivity {
    private FixExceptionViewPager viewPager;
    private TextView tvIndicator;
    private TextView tvConfirm;
    private TextView tvSelect;
    private FrameLayout btnConfirm;

    private int position;//初始位置
    private static ArrayList<Image> mSelectImages;//当前已经选中的图片
    private static ArrayList<Image> mTotalImages;//当前所有可显示的图片
    private RequestConfig config;//图片浏览器的配置信息

    public static void openActivity(Activity activity, int requestCode, int position, RequestConfig config, ArrayList<Image> selectImages, ArrayList<Image> totalImages) {
        mSelectImages = selectImages;
        mTotalImages = totalImages;
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(ImageSelector.POSITION, position);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        setStatusBarColor();//设置状态栏颜色
        initData();
        initView();
        initViewPager();
        initListener();
        changeSelect(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSelectImages = null;
        mTotalImages = null;
    }

    /**
     * 修改状态栏颜色
     */
    private void setStatusBarColor() {
        if (VersionUtils.isAndroidL()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorGray));
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        position = getIntent().getIntExtra(ImageSelector.POSITION, 0);
        config = getIntent().getParcelableExtra(ImageSelector.KEY_CONFIG);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        viewPager = findViewById(R.id.vp_image);
        tvIndicator = findViewById(R.id.tv_indicator);
        tvConfirm = findViewById(R.id.tv_confirm);
        tvSelect = findViewById(R.id.tv_select);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, mTotalImages);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initListener() {
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSelect();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Activity关闭时，通过Intent把用户的操作(确定/返回)传给ImageSelectActivity。
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void changeSelect(int position) {
        Drawable drawable = mSelectImages.contains(mTotalImages.get(position)) ?
                getResources().getDrawable(R.drawable.icon_image_select) :
                getResources().getDrawable(R.drawable.icon_image_un_select);
        tvSelect.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        tvIndicator.setText(String.format("%1$s/%2$s", position + 1, mTotalImages.size()));
        if (mSelectImages.size() == 0) {
            btnConfirm.setEnabled(false);
            tvConfirm.setText(R.string.selector_send);
        } else {
            btnConfirm.setEnabled(true);
            if (config.isSingle) {
                tvConfirm.setText(R.string.selector_send);
            } else if (config.maxSelectCount <= 0) {
                tvConfirm.setText(String.format(getString(R.string.selector_send) + "(%1$s)", mSelectImages.size()));
            } else {
                tvConfirm.setText(String.format(getString(R.string.selector_send) + "(%1$s/%2$s)", mSelectImages.size(), config.maxSelectCount));
            }
        }
    }

    private void clickSelect() {
        int position = viewPager.getCurrentItem();
        if (mTotalImages != null && mTotalImages.size() > position) {
            Image image = mTotalImages.get(position);
            if (mSelectImages.contains(image)) {
                mSelectImages.remove(image);
            } else if (config.isSingle) {
                mSelectImages.clear();
                mSelectImages.add(image);
            } else if (config.maxSelectCount <= 0 || mSelectImages.size() < config.maxSelectCount) {
                mSelectImages.add(image);
            }
            changeSelect(position);
        }
    }
}
