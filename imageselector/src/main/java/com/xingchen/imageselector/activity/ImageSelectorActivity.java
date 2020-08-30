package com.xingchen.imageselector.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.xingchen.imageselector.R;
import com.xingchen.imageselector.adapter.ImageAdapter;
import com.xingchen.imageselector.entry.Image;
import com.xingchen.imageselector.entry.ImageFolder;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.model.ImageModel;
import com.xingchen.imageselector.utils.ImageSelector;
import com.xingchen.imageselector.utils.VersionUtils;

import java.util.ArrayList;

public class ImageSelectorActivity extends AppCompatActivity {
    private static final int PERMISSION_READ_EXTERNAL_REQUEST_CODE = 0x00000011;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;

    private TextView tvConfirm;
    private TextView tvFolderName;
    private TextView tvPreview;
    private FrameLayout btnConfirm;
    private FrameLayout btnPreview;
    private RecyclerView rvImage;
    private RecyclerView rvFolder;
    private GridLayoutManager mLayoutManager;
    private ImageAdapter mAdapter;
    private RequestConfig config;

    /**
     * 启动图片选择器
     *
     * @param activity
     * @param requestCode
     * @param config
     */
    public static void openActivity(Activity activity, int requestCode, RequestConfig config) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动图片选择器
     *
     * @param fragment
     * @param requestCode
     * @param config
     */
    public static void openActivity(Fragment fragment, int requestCode, RequestConfig config) {
        Intent intent = new Intent(fragment.getActivity(), ImageSelectorActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector);
        setStatusBarColor();//设置状态栏颜色
        initData();//初始化数据
        initView();//初始化视图
        initImageList();//初始化图片列表
        setSelectImageCount(0);
        if (config != null && config.onlyTakePhoto) {
//            checkPermissionAndCamera();
        } else {
            checkPermissionAndLoadImages();
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
        rvImage = findViewById(R.id.rv_image);
        tvPreview = findViewById(R.id.tv_preview);
        tvConfirm = findViewById(R.id.tv_confirm);
        tvFolderName = findViewById(R.id.tv_folder_name);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnPreview = findViewById(R.id.btn_preview);
    }

    /**
     * 初始化图片列表
     */
    private void initImageList() {
        SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) rvImage.getItemAnimator();
        if (simpleItemAnimator != null) {//解决notifyItemChanged()闪烁
            simpleItemAnimator.setSupportsChangeAnimations(false);
        }
        Configuration configuration = getResources().getConfiguration();// 判断屏幕方向
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(this, 3);
        } else {
            mLayoutManager = new GridLayoutManager(this, 5);
        }
        rvImage.setLayoutManager(mLayoutManager);
        mAdapter = new ImageAdapter(this, config);
        mAdapter.setSelectListener(new ImageAdapter.OnImageSelectListener() {
            @Override
            public void OnImageSelect(Image image, boolean isSelect, int selectCount) {
                setSelectImageCount(selectCount);
            }
        });
        mAdapter.setItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Image image, int position) {
                PreviewActivity.openActivity(ImageSelectorActivity.this, ImageSelector.RESULT_CODE,
                        position, config, mAdapter.getSelectImages(), mAdapter.getTotalImages());
            }

            @Override
            public void OnCameraClick() {

            }
        });
        rvImage.setAdapter(mAdapter);
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
     * 设置选中的文件夹，同时刷新图片列表
     *
     * @param folder
     */
    private void setFolder(ImageFolder folder) {
        if (folder != null) {
            tvFolderName.setText(folder.getFolderName());
            mAdapter.refresh(folder.getImageList());
        }
    }

    /**
     * 设置选中的数量
     *
     * @param count
     */
    private void setSelectImageCount(int count) {
        if (count == 0) {
            btnConfirm.setEnabled(false);
            btnPreview.setEnabled(false);
            tvConfirm.setText(R.string.selector_send);
            tvPreview.setText(R.string.selector_preview);
        } else {
            btnConfirm.setEnabled(true);
            btnPreview.setEnabled(true);
            tvPreview.setText(String.format(getString(R.string.selector_preview) + "(%1$s)", count));
            if (config.isSingle) {
                tvConfirm.setText(R.string.selector_send);
            } else if (config.maxSelectCount > 0) {
                tvConfirm.setText(String.format(getString(R.string.selector_send) + "(%1$s/%2$s)", count, config.maxSelectCount));
            } else {
                tvConfirm.setText(String.format(getString(R.string.selector_send) + "(%1$s)", count));
            }
        }
    }

    /**
     * 加载图片并且更新视图
     */
    private void loadImageAndUpdateView() {
        ImageModel.loadImage(this, new ImageModel.DataCallback() {
            @Override
            public void onSuccess(final ArrayList<ImageFolder> imageFolders) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setFolder(imageFolders.get(0));
                    }
                });
            }
        });
    }

    /**
     * 发生没有权限等异常时，显示一个提示dialog.
     */
    private void showExceptionDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.selector_hint)
                .setMessage(R.string.selector_permissions_hint)
                .setNegativeButton(R.string.selector_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).setPositiveButton(R.string.selector_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }).show();
    }

    /**
     * 检查权限并加载SD卡里的图片。
     */
    private void checkPermissionAndLoadImages() {
        int hasReadExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadExternalPermission == PackageManager.PERMISSION_GRANTED) {//有权限，加载图片。
            loadImageAndUpdateView();
        } else {//没有权限，申请权限。
            ActivityCompat.requestPermissions(ImageSelectorActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_READ_EXTERNAL_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImageAndUpdateView();//允许权限，加载图片。
            } else {
                showExceptionDialog();//拒绝权限，弹出提示框。
            }
        } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                openCamera();//允许权限，有调起相机拍照。
            } else {
                showExceptionDialog(); //拒绝权限，弹出提示框。
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.RESULT_CODE) {
            if (data != null && data.getBooleanExtra(ImageSelector.IS_CONFIRM, false)) {
                //如果用户在预览页点击了确定，就直接把用户选中的图片返回给用户。
//                confirm();
            } else {
                //否则，就刷新当前页面。
                mAdapter.notifyDataSetChanged();
                setSelectImageCount(mAdapter.getSelectImages().size());
            }
        }
    }
}
