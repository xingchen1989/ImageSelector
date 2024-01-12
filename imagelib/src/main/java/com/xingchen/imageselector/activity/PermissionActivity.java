package com.xingchen.imageselector.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.gyf.immersionbar.ImmersionBar;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.databinding.ActivityPermissionTipBinding;
import com.xingchen.imageselector.entry.PermissionTip;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ActionType;
import com.xingchen.imageselector.utils.ImageSelector;

public class PermissionActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 0x00000011;
    private static final int REQUEST_VIDEO = 0x00000012;
    private static final int REQUEST_IMAGE = 0x00000013;
    private static final int REQUEST_APPLY = 0x00000014;
    private ActivityPermissionTipBinding binding;
    private RequestConfig requestConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionTipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        initPermissionLogic();
    }

    private void initView() {
        ImmersionBar.with(this).init();
        requestConfig = (RequestConfig) getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) setResult(RESULT_OK, data);
        else if (requestCode == REQUEST_APPLY) initPermissionLogic();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionTip permissionTip = requestConfig.permissionTip;
        if (checkGrantResults(grantResults)) initPermissionLogic();
        else if (requestCode == REQUEST_IMAGE)
            showExceptionDialog(permissionTip.getStorageDetailMsg());
        else if (requestCode == REQUEST_VIDEO)
            showExceptionDialog(permissionTip.getStorageDetailMsg());
        else if (requestCode == REQUEST_CAMERA)
            showExceptionDialog(permissionTip.getCameraDetailMsg());
    }

    public static void openActivity(Activity activity, RequestConfig config, int requestCode) {
        Intent intent = new Intent(activity, PermissionActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void openActivity(Fragment fragment, RequestConfig config, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), PermissionActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        fragment.startActivityForResult(intent, requestCode);
    }

    private void initPermissionLogic() {
        PermissionTip permissionTip = requestConfig.permissionTip;
        if (requestConfig.actionType == ActionType.PICK_PHOTO) {// 选照片
            binding.tvTitle.setText(permissionTip.getStorageTitle());
            binding.tvContent.setText(permissionTip.getStorageContent());
            if (checkPermissions(getImagePermission(), REQUEST_IMAGE)) {
                startMediaActivity(requestConfig, 1);
            }
        } else if (requestConfig.actionType == ActionType.PICK_VIDEO) {// 选视频
            binding.tvTitle.setText(permissionTip.getStorageTitle());
            binding.tvContent.setText(permissionTip.getStorageContent());
            if (checkPermissions(getVideoPermission(), REQUEST_VIDEO)) {
                startMediaActivity(requestConfig, 2);
            }
        } else if (requestConfig.actionType == ActionType.TAKE_PHOTO) {// 拍照
            binding.tvTitle.setText(permissionTip.getCameraTitle());
            binding.tvContent.setText(permissionTip.getCameraContent());
            String[] permissions = new String[]{Manifest.permission.CAMERA};
            if (checkPermissions(permissions, REQUEST_CAMERA)) {
                startMediaActivity(requestConfig, 3);
            }
        }
    }

    private void startMediaActivity(RequestConfig config, int requestCode) {
        if (config.actionType == ActionType.PICK_VIDEO) {
            SelectorActivity.openActivity(this, config, requestCode);
        } else if (config.isCrop) {
            ClipImageActivity.openActivity(this, config, requestCode);
        } else {
            SelectorActivity.openActivity(this, config, requestCode);
        }
    }

    private boolean checkGrantResults(int... grantResults) {
        boolean hasPermission = true;
        for (int grantResult : grantResults) {
            hasPermission = hasPermission && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        return hasPermission;
    }

    private boolean checkPermissions(String[] permissions, int requestCode) {
        boolean hasPermission = true;
        for (String permission : permissions) {
            int checkResult = ContextCompat.checkSelfPermission(this, permission);
            hasPermission = hasPermission && checkResult == PackageManager.PERMISSION_GRANTED;
        }
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
        return hasPermission;
    }

    private String[] getImagePermission() {
        if (Build.VERSION.SDK_INT >= 33 && getApplicationInfo().targetSdkVersion >= 33) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    private String[] getVideoPermission() {
        if (Build.VERSION.SDK_INT >= 33 && getApplicationInfo().targetSdkVersion >= 33) {
            return new String[]{Manifest.permission.READ_MEDIA_VIDEO};
        } else {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    private void showExceptionDialog(String message) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage(message)
                .setNegativeButton(R.string.selector_cancel, (dialog, which) -> {
                    dialog.cancel();
                    finish();
                })
                .setPositiveButton(R.string.selector_confirm, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    startActivityForResult(intent.setData(uri), REQUEST_APPLY);
                    dialog.cancel();
                }).show();
    }
}