package com.xingchen.imageselector.activity;

import static com.xingchen.imageselector.utils.DateTimeUtils.getImageTime;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xingchen.imageselector.R;
import com.xingchen.imageselector.adapter.FolderAdapter;
import com.xingchen.imageselector.adapter.ImageAdapter;
import com.xingchen.imageselector.entry.Image;
import com.xingchen.imageselector.entry.ImageFolder;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.model.ImageModel;
import com.xingchen.imageselector.utils.ActionType;
import com.xingchen.imageselector.utils.ImageSelector;
import com.xingchen.imageselector.utils.VersionUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SelectorActivity extends AppCompatActivity {
    private View contentMask;
    private TextView tvAddTime;
    private TextView tvConfirm;
    private TextView tvFolder;
    private TextView tvPreview;
    private ImageView ivBack;
    private FrameLayout btnConfirm;
    private FrameLayout btnPreview;
    private RecyclerView rvImage;
    private RecyclerView rvFolder;
    private ImageAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;
    private Runnable mHideRunnable;
    private Handler mHideHandler;
    private RequestConfig config;
    private Uri mCameraUri;
    private boolean isFolderOpen;

    /**
     * 启动图片选择器
     *
     * @param activity
     * @param config
     */
    public static void openActivity(Activity activity, Serializable config, int requestCode) {
        Intent intent = new Intent(activity, SelectorActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 启动图片选择器
     *
     * @param fragment
     * @param config
     */
    public static void openActivity(Fragment fragment, Serializable config, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), SelectorActivity.class);
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
        initLogic();//初始化逻辑
        initListener();//初始化监听事件
        initImageList();//初始化图片列表
        initFolderList();//初始化文件夹列表
        setSelectCount(0);//初始化选中的数量为0
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        GridLayoutManager layoutManager = (GridLayoutManager) rvImage.getLayoutManager();
        if (layoutManager != null && mImageAdapter != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutManager.setSpanCount(3);
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layoutManager.setSpanCount(5);
            }
            mImageAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.SELECTOR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //如果用户在预览页点击了确定，就直接把用户选中的图片返回给用户。
                confirmSelect();
            } else {
                //否则，就刷新当前页面。
                mImageAdapter.notifyDataSetChanged();
                setSelectCount(mImageAdapter.getSelectImages().size());
            }
        } else if (requestCode == ImageSelector.CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<Uri> imageContentUris = new ArrayList<>();
                addPictureToAlbum(mCameraUri);
                imageContentUris.add(mCameraUri);
                saveImageAndFinish(imageContentUris, true);
            } else if (config.actionType == ActionType.TAKE_PHOTO) finish();
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        config = (RequestConfig) getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
        mHideHandler = new Handler(Looper.myLooper());
        mHideRunnable = () -> ObjectAnimator.ofFloat(tvAddTime, "alpha", 1, 0).start();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        rvImage = findViewById(R.id.rv_image);
        rvFolder = findViewById(R.id.rv_folder);
        tvFolder = findViewById(R.id.tv_folder);
        tvAddTime = findViewById(R.id.tv_time);
        tvPreview = findViewById(R.id.tv_preview);
        tvConfirm = findViewById(R.id.tv_confirm);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnPreview = findViewById(R.id.btn_preview);
        contentMask = findViewById(R.id.view_mask);
    }

    /**
     * 初始化逻辑
     */
    private void initLogic() {
        if (config.actionType == ActionType.TAKE_PHOTO) {
            openDeviceCamera();
        } else if (config.actionType == ActionType.PICK_VIDEO) {
            openVideoFiles();
        } else {
            listImageFiles();
        }
    }

    /**
     * 初始化监听事件
     */
    private void initListener() {
        ivBack.setOnClickListener(v -> finish());
        contentMask.setOnClickListener(v -> closeFolder());
        btnPreview.setOnClickListener(v -> previewImage());
        btnConfirm.setOnClickListener(v -> confirmSelect());
        tvFolder.setOnClickListener(v -> animationFolder());
        rvImage.addOnScrollListener(new MyScrollListener());
    }

    /**
     * 初始化图片列表
     */
    private void initImageList() {
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        mImageAdapter = new ImageAdapter(this, config);
        mImageAdapter.setSelectListener((image, isSelect, selectCount) -> setSelectCount(selectCount));
        mImageAdapter.setItemClickListener(new MyItemClickListener());
        rvImage.setLayoutManager(mLayoutManager);
        rvImage.setAdapter(mImageAdapter);
    }

    /**
     * 初始化文件夹列表
     */
    private void initFolderList() {
        rvFolder.setLayoutManager(new LinearLayoutManager(SelectorActivity.this));
        mFolderAdapter = new FolderAdapter(SelectorActivity.this);
        mFolderAdapter.setOnFolderSelectListener(folder -> {
            refreshImages(folder);
            closeFolder();
        });
        rvFolder.setAdapter(mFolderAdapter);
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
    private void refreshImages(ImageFolder folder) {
        if (folder != null) {
            tvFolder.setText(folder.getFolderName());
            mImageAdapter.refresh(folder.getImageList());
        }
    }

    /**
     * 打开文件夹
     */
    private void openFolder() {
        isFolderOpen = true;
        contentMask.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY", rvFolder.getHeight(), 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                rvFolder.setVisibility(View.VISIBLE);
            }
        });
        animator.start();
    }

    /**
     * 收起文件夹
     */
    private void closeFolder() {
        isFolderOpen = false;
        contentMask.setVisibility(View.INVISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY", 0, rvFolder.getHeight());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rvFolder.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    /**
     * 收折目录列表
     */
    private void animationFolder() {
        if (isFolderOpen) closeFolder();
        else openFolder();
    }

    /**
     * 刷新文件夹列表
     *
     * @param folders
     */
    private void refreshFolders(ArrayList<ImageFolder> folders) {
        mFolderAdapter.refresh(folders);
    }

    /**
     * 设置选中的数量
     *
     * @param count
     */
    private void setSelectCount(int count) {
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
            } else if (config.maxCount > 0) {
                tvConfirm.setText(String.format(getString(R.string.selector_send) + "(%1$s/%2$s)", count, config.maxCount));
            } else {
                tvConfirm.setText(String.format(getString(R.string.selector_send) + "(%1$s)", count));
            }
        }
    }

    /**
     * 预览所选图片
     */
    private void previewImage() {
        ArrayList<Image> totalImages = new ArrayList<>(mImageAdapter.getSelectImages());
        PreviewActivity.openActivity(this, ImageSelector.SELECTOR_REQUEST_CODE,
                0, config, mImageAdapter.getSelectImages(), totalImages);
    }

    /**
     * 确认所选的图片
     */
    private void confirmSelect() {
        ArrayList<Uri> imageContentUris = new ArrayList<>();
        for (Image image : mImageAdapter.getSelectImages()) {
            imageContentUris.add(image.getContentUri());
        }
        saveImageAndFinish(imageContentUris, false);
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

    /**
     * 加载图片并且更新视图
     */
    private void listImageFiles() {
        ImageModel.getInstance().asyncLoadImage(this, imageFolders -> runOnUiThread(() -> {
            refreshImages(imageFolders.get(0));
            refreshFolders(imageFolders);
        }));
    }

    /**
     * 创建图片文件
     *
     * @return
     */
    private File createImageFile() {
        return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), createImageName());
    }

    /**
     * 创建图片名称
     *
     * @return
     */
    private String createImageName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return String.format("JPEG_%s.jpg", dateFormat.format(new Date()));
    }

    /**
     * 打开视频列表
     */
    private void openVideoFiles() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, ImageSelector.VIDEO_REQUEST_CODE);
    }

    /**
     * 打开相机拍照
     *
     * @return
     */
    private void openDeviceCamera() {
        try {
            Uri imageUri = null;
            File photoFile = createImageFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT < 24) {
                imageUri = Uri.fromFile(photoFile);
            } else {
                //方式一，需要读写权限
                        /*ContentValues contentValues = new ContentValues(1);
                        contentValues.put(MediaStore.Images.Media.DATA, mTmpFile.getAbsolutePath());
                        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);*/

                // 方式2 置入一个不设防的VmPolicy（不设置的话 7.0以上一调用拍照功能就崩溃了）
                        /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                        imageUri = Uri.fromFile(mTmpFile);*/

                //方式3，使用 FileProvider 类进行授权，可以不申请读写权限
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".imageSelectorProvider", photoFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, ImageSelector.CAMERA_REQUEST_CODE);
            mCameraUri = imageUri;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向相册添加图片
     *
     * @param inputUri
     */
    private void addPictureToAlbum(Uri inputUri) {
        //创建ContentValues对象，准备插入数据
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, createImageName());
        //插入数据，返回所插入数据对应的Uri
        Uri outUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (outUri != null) {
            try {
                ParcelFileDescriptor parcelFdInput = getContentResolver().openFileDescriptor(inputUri, "r");
                ParcelFileDescriptor parcelFdOutput = getContentResolver().openFileDescriptor(outUri, "w");
                InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(parcelFdInput);
                OutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(parcelFdOutput);
                byte[] bytes = new byte[1024];
                while ((inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MyItemClickListener implements ImageAdapter.OnItemClickListener {
        @Override
        public void OnItemClick(Image image, int position) {
            PreviewActivity.openActivity(SelectorActivity.this, ImageSelector.SELECTOR_REQUEST_CODE,
                    position, config, mImageAdapter.getSelectImages(), mImageAdapter.getTotalImages());
        }

        @Override
        public void OnCameraClick() {

        }
    }

    private class MyScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    // 手指触屏拉动准备滚动，只触发一次        顺序: 1
                    mHideHandler.removeCallbacks(mHideRunnable);
                    ObjectAnimator.ofFloat(tvAddTime, "alpha", 0, 1).start();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    // 持续滚动开始，只触发一次                顺序: 2
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    // 整个滚动事件结束，只触发一次            顺序: 4
                    mHideHandler.postDelayed(mHideRunnable, 1500);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            GridLayoutManager layoutManager = (GridLayoutManager) rvImage.getLayoutManager();
            int position = Objects.requireNonNull(layoutManager).findFirstVisibleItemPosition();
            Image firstVisibleImage = mImageAdapter.getFirstVisibleImage(position);
            if (firstVisibleImage != null) {
                tvAddTime.setText(getImageTime(SelectorActivity.this, firstVisibleImage.getAddedTime()));
            }
        }
    }
}
