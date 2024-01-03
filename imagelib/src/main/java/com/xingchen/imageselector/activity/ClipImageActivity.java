package com.xingchen.imageselector.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gyf.immersionbar.ImmersionBar;
import com.xingchen.imagecropper.view.CropImageView;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ImageSelector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class ClipImageActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvConfirm;
    private CropImageView cropImageView;
    private boolean isCameraImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_clip);
        initView();//初始化视图
        initListener();//初始化监听器
    }

    /**
     * 启动图片选择器
     *
     * @param activity
     * @param config
     */
    public static void openActivity(Activity activity, RequestConfig config, int requestCode) {
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
    public static void openActivity(Fragment fragment, RequestConfig config, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), ClipImageActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ImmersionBar.with(this).titleBar(R.id.cl_title).init();
        ivBack = findViewById(R.id.iv_back);
        tvConfirm = findViewById(R.id.tv_confirm);
        cropImageView = findViewById(R.id.cropImageView);
        Serializable config = getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
        SelectorActivity.openActivity(this, config, ImageSelector.REQ_IMAGE_CODE);
    }

    private void initListener() {
        tvConfirm.setOnClickListener(v -> {
            Uri uri = saveBitmap(cropImageView.getCroppedImage());
            if (uri != null) {
                ArrayList<Uri> imageContentUris = new ArrayList<>();
                imageContentUris.add(uri);
                saveImageAndFinish(imageContentUris, isCameraImage);
            } else {
                finish();
            }
        });

        ivBack.setOnClickListener(v -> finish());
    }

    /**
     * 创建图片文件
     *
     * @return
     */
    private File createImageFile() {
        String fileName = String.format("JPEG_%s.jpg", UUID.randomUUID().toString());
        return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
    }

    /**
     * 保存裁剪后的图片
     *
     * @param bitmap
     */
    private Uri saveBitmap(Bitmap bitmap) {
        try {
            File file = createImageFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            bitmap.recycle();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        if (requestCode == ImageSelector.REQ_IMAGE_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                isCameraImage = data.getBooleanExtra(ImageSelector.IS_CAMERA_IMAGE, false);
                ArrayList<Uri> imageContentUris = data.getParcelableArrayListExtra(ImageSelector.SELECT_RESULT);
                if (imageContentUris != null && imageContentUris.size() > 0) {
                    cropImageView.setImageURI(imageContentUris.get(0));
                }
            } else finish();
        }
    }
}
