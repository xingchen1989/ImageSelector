package com.xingchen.imageselector.activity;

import static com.xingchen.imageselector.utils.ImageSelector.REQ_IMAGE_CODE;
import static com.xingchen.imageselector.utils.ImageSelector.SELECT_RESULT;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.gyf.immersionbar.ImmersionBar;
import com.xingchen.imageselector.databinding.ActivityImageClipBinding;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ImageSelector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class ClipImageActivity extends AppCompatActivity {
    private ActivityImageClipBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageClipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        new Handler().post(() -> {
            ImmersionBar.with(this).titleBar(binding.clTitle).init();
            Serializable config = getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
            SelectorActivity.openActivity(ClipImageActivity.this, config, REQ_IMAGE_CODE);
        });
    }

    private void initListener() {
        binding.tvConfirm.setOnClickListener(v -> {
            Uri uri = saveBitmap(binding.cropImageView.getCroppedImage());
            if (uri != null) {
                ArrayList<Uri> imageContentUris = new ArrayList<>();
                imageContentUris.add(uri);
                saveImageAndFinish(imageContentUris);
            } else {
                finish();
            }
        });

        binding.ivBack.setOnClickListener(v -> finish());
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
     */
    private void saveImageAndFinish(ArrayList<Uri> imageContentUris) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(SELECT_RESULT, imageContentUris);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMAGE_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<Uri> imageContentUris = data.getParcelableArrayListExtra(SELECT_RESULT);
                if (imageContentUris != null && imageContentUris.size() > 0) {
                    binding.cropImageView.setImageURI(imageContentUris.get(0));
                }
            } else finish();
        }
    }
}
