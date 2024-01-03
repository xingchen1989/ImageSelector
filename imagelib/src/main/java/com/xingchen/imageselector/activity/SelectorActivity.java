package com.xingchen.imageselector.activity;

import static com.xingchen.imageselector.utils.DateTimeUtil.getImageTime;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gyf.immersionbar.ImmersionBar;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.adapter.FolderAdapter;
import com.xingchen.imageselector.adapter.MediaAdapter;
import com.xingchen.imageselector.databinding.ActivityMediaSelectorBinding;
import com.xingchen.imageselector.entry.MediaData;
import com.xingchen.imageselector.entry.MediaFolder;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.model.MediaModel;
import com.xingchen.imageselector.utils.ActionType;
import com.xingchen.imageselector.utils.ImageSelector;

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
    private static final int REQ_PERMISSION_CAMERA = 0x00000011;
    private ActivityMediaSelectorBinding binding;
    private RequestConfig requestConfig;
    private FolderAdapter folderAdapter;
    private MediaAdapter mediaAdapter;
    private Runnable hideRunnable;
    private Handler hideHandler;
    private Uri cameraUri;
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
        Intent intent = new Intent(fragment.getContext(), SelectorActivity.class);
        intent.putExtra(ImageSelector.KEY_CONFIG, config);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaSelectorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initData();//初始化数据
        initView();//初始化视图
        initLogic();//初始化逻辑
        initListener();//初始化监听事件
        setSelectCount(0);//初始化选中的数量为0
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.REQ_IMAGE_CODE) {
            setSelectCount(mediaAdapter.getSelectImages().size());
            mediaAdapter.notifyDataSetChanged();
            if (resultCode == RESULT_OK) {
                confirmSelect();
            }
        } else if (requestCode == ImageSelector.REQ_CAMERA_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<Uri> imageContentUris = new ArrayList<>();
//                addPictureToAlbum(mCameraUri);
                imageContentUris.add(cameraUri);
                saveImageAndFinish(imageContentUris, true);
            }
        } else if (requestCode == ImageSelector.REQ_VIDEO_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<Uri> videoContentUris = new ArrayList<>();
                videoContentUris.add(data.getData());
                saveImageAndFinish(videoContentUris, false);
            } else finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION_CAMERA) {
            boolean hasPermission = true;
            for (int grantResult : grantResults) {
                hasPermission = hasPermission && grantResult == PackageManager.PERMISSION_GRANTED;
            }
            if (hasPermission) openDeviceCamera();
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        hideHandler = new Handler(Looper.myLooper());
        requestConfig = (RequestConfig) getIntent().getSerializableExtra(ImageSelector.KEY_CONFIG);
        hideRunnable = () -> ObjectAnimator.ofFloat(binding.tvTime, "alpha", 1, 0).start();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ImmersionBar.with(this).titleBar(R.id.cl_title).init();

        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        mediaAdapter = new MediaAdapter(this, requestConfig);
        mediaAdapter.setActionListener(new MyMediaListener());
        binding.rvMedia.setLayoutManager(mLayoutManager);
        binding.rvMedia.setItemAnimator(null);
        binding.rvMedia.setAdapter(mediaAdapter);

        binding.rvFolder.setLayoutManager(new LinearLayoutManager(SelectorActivity.this));
        folderAdapter = new FolderAdapter(this);
        folderAdapter.setOnFolderListener(new MyFolderListener());
        binding.rvFolder.setAdapter(folderAdapter);

        binding.tvTitle.setText(requestConfig.getMediaTitle());
    }

    /**
     * 初始化逻辑
     */
    private void initLogic() {
        if (requestConfig.actionType == ActionType.PICK_PHOTO) {
            listImageFiles();
        } else if (requestConfig.actionType == ActionType.PICK_VIDEO) {
            listVideoFiles();
        } else if (requestConfig.actionType == ActionType.TAKE_PHOTO) {
            openDeviceCamera();
        }
    }

    /**
     * 初始化监听事件
     */
    private void initListener() {
        binding.ivBack.setOnClickListener(v -> finish());
        binding.viewMask.setOnClickListener(v -> closeFolder());
        binding.tvFolder.setOnClickListener(v -> animationFolder());
        binding.btnPreview.setOnClickListener(v -> previewImage());
        binding.btnConfirm.setOnClickListener(v -> confirmSelect());
        binding.rvMedia.addOnScrollListener(new MyScrollListener());
    }

    /**
     * 刷新文件夹列表
     *
     * @param folders
     */
    private void refreshFolders(ArrayList<MediaFolder> folders) {
        folderAdapter.refresh(folders);
    }

    /**
     * 设置选中的文件夹，同时刷新图片列表
     *
     * @param folder
     */
    private void refreshMedias(MediaFolder folder) {
        mediaAdapter.refresh(folder.getMediaList());
        binding.tvFolder.setText(folder.getFolderName());
    }

    /**
     * 打开文件夹
     */
    private void openFolder() {
        isFolderOpen = true;
        binding.viewMask.setVisibility(View.VISIBLE);
        float[] params = {binding.rvFolder.getHeight(), 0};
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.rvFolder, "translationY", params);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                binding.rvFolder.setVisibility(View.VISIBLE);
            }
        });
        animator.start();
    }

    /**
     * 收起文件夹
     */
    private void closeFolder() {
        isFolderOpen = false;
        binding.viewMask.setVisibility(View.INVISIBLE);
        float[] params = {0, binding.rvFolder.getHeight()};
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.rvFolder, "translationY", params);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                binding.rvFolder.setVisibility(View.GONE);
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
     * 设置选中的数量
     *
     * @param count
     */
    private void setSelectCount(int count) {
        String confirm = getString(R.string.selector_send);
        String preview = getString(R.string.selector_preview);
        binding.btnConfirm.setEnabled(count != 0);
        binding.btnPreview.setEnabled(count != 0);
        if (requestConfig.isSingle) {
            binding.tvConfirm.setText(R.string.selector_send);
            binding.tvPreview.setText(R.string.selector_preview);
        } else if (requestConfig.maxCount <= 0) {
            binding.tvPreview.setText(String.format(preview + "(%1$s)", count));
            binding.tvConfirm.setText(String.format(confirm + "(%1$s)", count));
        } else {
            int maxCount = requestConfig.maxCount;
            binding.tvPreview.setText(String.format(preview + "(%1$s)", count));
            binding.tvConfirm.setText(String.format(confirm + "(%1$s/%2$s)", count, maxCount));
        }
    }

    /**
     * 预览所选图片
     */
    private void previewImage() {
        this.previewImage(MediaModel.getInstance().getFirstSelect());
    }

    /**
     * 预览所选图片
     *
     * @param position
     */
    private void previewImage(int position) {
        int index = requestConfig.useCamera ? position - 1 : position;
        PreviewActivity.openActivity(this, requestConfig, index, ImageSelector.REQ_IMAGE_CODE);
    }

    /**
     * 确认所选的图片
     */
    private void confirmSelect() {
        ArrayList<Uri> imageContentUris = new ArrayList<>();
        for (MediaData image : mediaAdapter.getSelectImages()) {
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
        MediaModel.getInstance().asyncLoadImage(this, imageFolders -> runOnUiThread(() -> {
            refreshMedias(imageFolders.get(0));
            refreshFolders(imageFolders);
        }));
    }

    /**
     * 加载视频并且更新视图
     */
    private void listVideoFiles() {
        MediaModel.getInstance().asyncLoadVideo(this, videoFolders -> runOnUiThread(() -> {
            refreshMedias(videoFolders.get(0));
            refreshFolders(videoFolders);
        }));
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
        startActivityForResult(intent, ImageSelector.REQ_VIDEO_CODE);
    }

    /**
     * 打开相机拍照
     */
    private void openDeviceCamera() {
        int checkResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (checkResult != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, permissions, REQ_PERMISSION_CAMERA);
        } else {
            File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), createImageName());
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String authority = getPackageName() + ".imageSelectorProvider";
            cameraUri = FileProvider.getUriForFile(this, authority, photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            startActivityForResult(intent, ImageSelector.REQ_CAMERA_CODE);
        }
    }

    /**
     * 向相册添加图片
     *
     * @param inputUri
     */
    private void addPictureToAlbum(Uri inputUri) {
        try {
            //创建ContentValues对象，准备插入数据
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, createImageName());
            //插入数据，返回所插入数据对应的Uri
            Uri outUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            ParcelFileDescriptor parcelFdInput = getContentResolver().openFileDescriptor(inputUri, "r");
            ParcelFileDescriptor parcelFdOutput = getContentResolver().openFileDescriptor(outUri, "w");
            InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(parcelFdInput);
            OutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(parcelFdOutput);
            byte[] bytes = new byte[1024];
            while ((inputStream.read(bytes)) != -1) outputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyFolderListener implements FolderAdapter.OnFolderListener {
        @Override
        public void onFolderSelect(MediaFolder folder) {
            refreshMedias(folder);
            closeFolder();
        }
    }

    private class MyMediaListener implements MediaAdapter.ItemActionListener {
        @Override
        public void OnCameraClick() {
            openDeviceCamera();
        }

        @Override
        public void OnMediaClick(MediaData media, int position) {
            previewImage(position);
        }

        @Override
        public void OnMediaSelect(MediaData media, int selectCount) {
            setSelectCount(selectCount);
        }
    }

    private class MyScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    // 手指触屏拉动准备滚动，只触发一次        顺序: 1
                    hideHandler.removeCallbacks(hideRunnable);
                    ObjectAnimator.ofFloat(binding.tvTime, "alpha", 0, 1).start();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    // 持续滚动开始，只触发一次                顺序: 2
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    // 整个滚动事件结束，只触发一次            顺序: 4
                    hideHandler.postDelayed(hideRunnable, 1500);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            try {
                GridLayoutManager layoutManager = (GridLayoutManager) binding.rvMedia.getLayoutManager();
                int position = Objects.requireNonNull(layoutManager).findFirstVisibleItemPosition();
                MediaData firstImage = mediaAdapter.getFirstVisibleImage(position);
                binding.tvTime.setText(getImageTime(SelectorActivity.this, firstImage.getAddedTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
