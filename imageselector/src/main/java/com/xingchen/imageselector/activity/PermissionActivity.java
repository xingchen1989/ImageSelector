package com.xingchen.imageselector.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gyf.immersionbar.ImmersionBar;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.utils.ActionType;
import com.xingchen.imageselector.utils.ImageSelector;

public class PermissionActivity extends AppCompatActivity {
    private static final int REQ_PERMISSION_CAMERA = 0x00000011;
    private static final int REQ_PERMISSION_VIDEO = 0x00000012;
    private static final int REQ_PERMISSION_READ = 0x00000013;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        ImmersionBar.with(this).init();
        initPermissionLogic();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION_READ) {
            processPermissionResult(grantResults, "请在系统设置中打开存储权限");
        } else if (requestCode == REQ_PERMISSION_VIDEO) {
            processPermissionResult(grantResults, "请在系统设置中打开存储权限");
        } else if (requestCode == REQ_PERMISSION_CAMERA) {
            processPermissionResult(grantResults, "请在系统设置中打开相机权限");
        }
    }

    public static void openActivity(Activity activity, RequestConfig config, int requestCode) {
        Intent intent = new Intent(activity, PermissionActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        activity.startActivityForResult(intent, requestCode);
    }

    private void processPermissionResult(int[] grantResults, String message) {
        if (checkGrantResults(grantResults)) {
            initPermissionLogic();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initPermissionLogic() {
        RequestConfig config = (RequestConfig) getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
        if (config.actionType == ActionType.PICK_PHOTO) {// 选照片
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            if (checkPermissions(permissions, REQ_PERMISSION_READ)) {
                startMediaActivity(config, 1);
            }
        } else if (config.actionType == ActionType.TAKE_PHOTO) {// 拍照
            String[] permissions = new String[]{Manifest.permission.CAMERA};
            if (checkPermissions(permissions, REQ_PERMISSION_CAMERA)) {
                startMediaActivity(config, 2);
            }
        } else if (config.actionType == ActionType.PICK_VIDEO) {// 选视频
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            if (checkPermissions(permissions, REQ_PERMISSION_VIDEO)) {
                startMediaActivity(config, 3);
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

    private void showExceptionDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.selector_hint)
                .setMessage(R.string.selector_permissions_hint)
                .setNegativeButton(R.string.selector_cancel, (dialog, which) -> {
                    dialog.cancel();
                    finish();
                }).setPositiveButton(R.string.selector_confirm, (dialog, which) -> {
                    dialog.cancel();
                    Uri uri = Uri.parse("package:" + getPackageName());
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                    startActivity(intent);
                }).show();
    }
}