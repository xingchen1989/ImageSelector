package com.xingchen.imageselectordemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xingchen.imageselector.entry.PermissionTip;
import com.xingchen.imageselector.utils.ActionType;
import com.xingchen.imageselector.utils.ImageSelector;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 0x00000011;
    private ImageAdapter mImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    private void initListener() {
        findViewById(R.id.btn_single).setOnClickListener(this);
        findViewById(R.id.btn_limit).setOnClickListener(this);
        findViewById(R.id.btn_clip).setOnClickListener(this);
        findViewById(R.id.btn_pick_video).setOnClickListener(this);
        findViewById(R.id.btn_take_and_clip).setOnClickListener(this);
        findViewById(R.id.btn_single_no_camera).setOnClickListener(this);
    }

    private void initView() {
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mImageAdapter = new ImageAdapter(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(mImageAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pick_video:// 选择视频
                ImageSelector.builder()
                        .setCrop(false)
                        .setActionType(ActionType.PICK_VIDEO)
                        .start(this, REQUEST_CODE);// 打开相册
                break;
            case R.id.btn_single://单选
                ImageSelector.builder()
                        .useCamera(true)// 设置是否使用拍照
                        .setSingle(true)//设置是否单选
                        .canPreview(true)// 是否点击放大图片查看，默认为true
                        .start(this, REQUEST_CODE);// 打开相册
                break;
            case R.id.btn_single_no_camera:// 单选图片，不拍照
                ImageSelector.builder()
                        .setCrop(false)
                        .setSingle(true)//设置是否单选
                        .useCamera(false)
                        .canPreview(false)// 不预览
                        .setActionType(ActionType.PICK_PHOTO)
                        .start(this, REQUEST_CODE);// 打开相册
                break;
            case R.id.btn_limit:// 多选(最多9张)
                ImageSelector.builder()
                        .useCamera(true)// 设置是否使用拍照
                        .setSingle(false)// 设置是否单选
                        .canPreview(true)// 是否点击放大图片查看，默认为true
                        .setMaxSelectCount(9)// 图片的最大选择数量，小于等于0时，不限数量。
                        .start(this, REQUEST_CODE);// 打开相册
                break;
            case R.id.btn_clip://单选并剪裁
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setCrop(true)// 设置是否使用图片剪切功能。
                        .setCropRatio(1.0f)// 图片剪切的宽高比，默认1.0f。宽固定为手机屏幕的宽。
                        .setSingle(true)//设置是否单选
                        .canPreview(true)//是否点击放大图片查看，默认为true
                        .start(this, REQUEST_CODE);// 打开相册
                break;
            case R.id.btn_take_and_clip:// 拍照并剪裁
                ImageSelector.builder()
                        .useCamera(true) // 设置是否使用拍照
                        .setCrop(true)// 设置是否使用图片剪切功能。
                        .setCropRatio(1.0f)// 图片剪切的宽高比,默认1.0f。宽固定为手机屏幕的宽。
                        .setActionType(ActionType.TAKE_PHOTO)// 仅拍照，不打开相册
                        .setPermissionTip(new PermissionTip())
                        .start(this, REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            ArrayList<Uri> imageContentUris = data.getParcelableArrayListExtra(ImageSelector.SELECT_RESULT);
            mImageAdapter.refresh(imageContentUris);
        }
    }
}
