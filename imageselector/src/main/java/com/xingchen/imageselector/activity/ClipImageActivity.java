package com.xingchen.imageselector.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.xingchen.imagecropper.view.CropImageView;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ImageSelector;
import com.xingchen.imageselector.utils.VersionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class ClipImageActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvCrop;
    private RequestConfig config;
    private CropImageView cropImageView;
    private boolean isCameraImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_image);
        setStatusBarColor();//设置状态栏颜色
        initData();//初始化数据
        initView();//初始化视图
        initListener();//初始化监听器
        ImageSelectorActivity.openActivity(this, ImageSelector.SELECTOR_RESULT_CODE, config);//启动图片选择器
    }

    /**
     * 启动图片选择器
     *
     * @param activity
     * @param config
     */
    public static void openActivity(Activity activity, int requestCode, RequestConfig config) {
        Intent intent = new Intent(activity, ClipImageActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动图片选择器
     *
     * @param fragment
     * @param config
     */
    public static void openActivity(Fragment fragment, int requestCode, RequestConfig config) {
        Intent intent = new Intent(fragment.getActivity(), ClipImageActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        fragment.startActivityForResult(intent, requestCode);
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
        config = getIntent().getParcelableExtra(ImageSelector.KEY_CONFIG);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        tvCrop = findViewById(R.id.tv_crop);
        cropImageView = findViewById(R.id.cropImageView);
    }

    private void initListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Uri> imageContentUris = new ArrayList<>();
                Uri uri = saveBitmap(cropImageView.getCroppedImage());
                if (uri != null) {
                    imageContentUris.add(uri);
                    saveImageAndFinish(imageContentUris, isCameraImage);
                } else {
                    finish();
                }
            }
        });
    }

    /**
     * 保存裁剪后的图片
     *
     * @param bitmap
     */
    private Uri saveBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            try {
                File file = new File(getExternalFilesDir("crop"), UUID.randomUUID().toString() + ".jpg");
                FileOutputStream byteArrayOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                return Uri.fromFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bitmap.recycle();
            }
        }
        return null;
    }

    /**
     * 保存图片，并把选中的图片通过Intent传递给上一个Activity
     *
     * @param imageContentUris
     * @param isCameraImage
     */
    private void saveImageAndFinish(ArrayList<Uri> imageContentUris, boolean isCameraImage) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(ImageSelector.SELECT_RESULT, imageContentUris);
        intent.putExtra(ImageSelector.IS_CAMERA_IMAGE, isCameraImage);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.SELECTOR_RESULT_CODE && data != null) {
            isCameraImage = data.getBooleanExtra(ImageSelector.IS_CAMERA_IMAGE, false);
            ArrayList<Uri> imageContentUris = data.getParcelableArrayListExtra(ImageSelector.SELECT_RESULT);
            if (imageContentUris != null && imageContentUris.size() > 0) {
                cropImageView.setImageURI(imageContentUris.get(0));
            }
        }
    }
}
