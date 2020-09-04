package com.xingchen.imageselector.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.xingchen.imageselector.R;
import com.xingchen.imageselector.adapter.FolderAdapter;
import com.xingchen.imageselector.adapter.ImageAdapter;
import com.xingchen.imageselector.entry.Image;
import com.xingchen.imageselector.entry.ImageFolder;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.model.ImageModel;
import com.xingchen.imageselector.utils.DateUtils;
import com.xingchen.imageselector.utils.ImageSelector;
import com.xingchen.imageselector.utils.VersionUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImageSelectorActivity extends AppCompatActivity {

    private static final int PERMISSION_READ_EXTERNAL_REQUEST_CODE = 0x00000011;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;

    private View viewMask;
    private TextView tvTime;
    private TextView tvConfirm;
    private TextView tvFolderName;
    private TextView tvPreview;
    private ImageView ivBack;
    private FrameLayout btnConfirm;
    private FrameLayout btnPreview;
    private RelativeLayout btnFolder;
    private RecyclerView rvImage;
    private RecyclerView rvFolder;
    private ImageAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;

    private boolean isFolderOpen;
    private Uri mCameraUri;
    private RequestConfig config;
    private Handler mHideHandler = new Handler();
    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            if (tvTime.getAlpha() == 1) {
                ObjectAnimator.ofFloat(tvTime, "alpha", 1, 0).setDuration(300).start();
            }
        }
    };

    /**
     * 启动图片选择器
     *
     * @param activity
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
        initListener();//初始化监听事件
        initImageList();//初始化图片列表
        initFolderList();//初始化文件夹列表
        setSelectImageCount(0);
        if (config != null && config.onlyTakePhoto) {
            checkPermissionAndCamera();
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
        ivBack = findViewById(R.id.iv_back);
        rvImage = findViewById(R.id.rv_image);
        rvFolder = findViewById(R.id.rv_folder);
        tvTime = findViewById(R.id.tv_time);
        tvPreview = findViewById(R.id.tv_preview);
        tvConfirm = findViewById(R.id.tv_confirm);
        tvFolderName = findViewById(R.id.tv_folder_name);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnPreview = findViewById(R.id.btn_preview);
        btnFolder = findViewById(R.id.btn_folder);
        viewMask = findViewById(R.id.view_mask);
    }

    /**
     * 初始化监听事件
     */
    private void initListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFolder();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSelect();
            }
        });
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Image> totalImages = new ArrayList<>(mImageAdapter.getSelectImages());
                PreviewActivity.openActivity(ImageSelectorActivity.this, ImageSelector.SELECTOR_RESULT_CODE,
                        0, config, mImageAdapter.getSelectImages(), totalImages);
            }
        });
        btnFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFolderOpen) {
                    closeFolder();
                } else {
                    openFolder();
                }
            }
        });
        rvImage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        // 手指触屏拉动准备滚动，只触发一次        顺序: 1
                        mHideHandler.removeCallbacks(mHide);
                        ObjectAnimator.ofFloat(tvTime, "alpha", 0, 1).setDuration(300).start();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        // 持续滚动开始，只触发一次                顺序: 2
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // 整个滚动事件结束，只触发一次            顺序: 4
                        mHideHandler.postDelayed(mHide, 1500);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                changeTime();
            }
        });
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
        GridLayoutManager mLayoutManager = (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) ?
                new GridLayoutManager(this, 3) : new GridLayoutManager(this, 5);
        mImageAdapter = new ImageAdapter(this, config);
        mImageAdapter.setSelectListener(new ImageAdapter.OnImageSelectListener() {
            @Override
            public void OnImageSelect(Image image, boolean isSelect, int selectCount) {
                setSelectImageCount(selectCount);
            }
        });
        mImageAdapter.setItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Image image, int position) {
                PreviewActivity.openActivity(ImageSelectorActivity.this, ImageSelector.SELECTOR_RESULT_CODE,
                        position, config, mImageAdapter.getSelectImages(), mImageAdapter.getTotalImages());
            }

            @Override
            public void OnCameraClick() {
                checkPermissionAndCamera();
            }
        });
        rvImage.setLayoutManager(mLayoutManager);
        rvImage.setAdapter(mImageAdapter);
    }

    /**
     * 初始化文件夹列表
     */
    private void initFolderList() {
        rvFolder.setLayoutManager(new LinearLayoutManager(ImageSelectorActivity.this));
        mFolderAdapter = new FolderAdapter(ImageSelectorActivity.this);
        mFolderAdapter.setOnFolderSelectListener(new FolderAdapter.OnFolderSelectListener() {
            @Override
            public void OnFolderSelect(ImageFolder folder) {
                refreshImages(folder);
                closeFolder();
            }
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
            tvFolderName.setText(folder.getFolderName());
            mImageAdapter.refresh(folder.getImageList());
            if (tvTime.getAlpha() == 0) {
                mHideHandler.postDelayed(mHide, 1500);
                ObjectAnimator.ofFloat(tvTime, "alpha", 0, 1).setDuration(300).start();
            }
        }
    }

    /**
     * 打开文件夹
     */
    private void openFolder() {
        isFolderOpen = true;
        viewMask.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY", rvFolder.getHeight(), 0).setDuration(300);
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
        viewMask.setVisibility(View.INVISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY", 0, rvFolder.getHeight()).setDuration(300);
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
    private void loadImageAndUpdateView() {
        ImageModel.getInstance().loadImage(this, false, new ImageModel.DataCallback() {
            @Override
            public void onSuccess(final ArrayList<ImageFolder> imageFolders) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshImages(imageFolders.get(0));
                        refreshFolders(imageFolders);
                    }
                });
            }
        });
    }

    /**
     * 改变时间条显示的时间（显示图片列表中的第一个可见图片的时间）
     */
    private void changeTime() {
        GridLayoutManager layoutManager = (GridLayoutManager) rvImage.getLayoutManager();
        if (layoutManager != null) {
            Image image = mImageAdapter.getFirstVisibleImage(layoutManager.findFirstVisibleItemPosition());
            if (image != null) {
                String time = DateUtils.getImageTime(this, image.getAddedTime());
                tvTime.setText(time);
            }
        }
    }

    /**
     * 判断SDCard是否存在
     *
     * @return
     */
    private boolean isSDCardExists() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                !Environment.isExternalStorageRemovable();
    }

    /**
     * 创建图片文件
     *
     * @return
     */
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = String.format("JPEG_%s.jpg", timeStamp);
        if (isSDCardExists() && getExternalCacheDir() != null) {
            return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName);
        } else {
            return new File(getFilesDir(), imageFileName);
        }
    }

    /**
     * 打开相机拍照
     *
     * @return
     */
    private Uri openCamera() {
        Uri imageUri = null;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            /*获取当前系统的android版本号*/
            File photoFile = createImageFile();
            if (android.os.Build.VERSION.SDK_INT < 24) {
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
        } else {
            Toast.makeText(this, "设备不支持", Toast.LENGTH_SHORT).show();
        }
        return imageUri;
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

    private void checkPermissionAndCamera() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //有调起相机拍照。
            mCameraUri = openCamera();
        } else {
            //没有权限，申请权限。
            ActivityCompat.requestPermissions(ImageSelectorActivity.this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
        }
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

    /**
     * 向相册添加图片
     *
     * @param inputUri
     */
    private void addPictureToAlbum(Uri inputUri) {
        //创建ContentValues对象，准备插入数据
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, String.format("JPEG_%s.jpg", timeStamp));
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mCameraUri = openCamera();//允许权限，有调起相机拍照。
            } else {
                showExceptionDialog(); //拒绝权限，弹出提示框。
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.SELECTOR_RESULT_CODE) {
            if (data != null && data.getBooleanExtra(ImageSelector.IS_CONFIRM, false)) {
                //如果用户在预览页点击了确定，就直接把用户选中的图片返回给用户。
                confirmSelect();
            } else {
                //否则，就刷新当前页面。
                mImageAdapter.notifyDataSetChanged();
                setSelectImageCount(mImageAdapter.getSelectImages().size());
            }
        } else if (requestCode == ImageSelector.CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<Uri> imageContentUris = new ArrayList<>();
                addPictureToAlbum(mCameraUri);
                imageContentUris.add(mCameraUri);
                saveImageAndFinish(imageContentUris, true);
            } else {
                if (config.onlyTakePhoto) {
                    finish();
                }
            }
        }
    }

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
}
