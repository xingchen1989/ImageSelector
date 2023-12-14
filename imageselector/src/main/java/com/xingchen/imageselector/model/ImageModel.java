package com.xingchen.imageselector.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.xingchen.imageselector.R;
import com.xingchen.imageselector.entry.ImageData;
import com.xingchen.imageselector.entry.ImageFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageModel {
    private static ImageModel mInstance = new ImageModel();//数据model单例

    public static ArrayList<ImageData> mTotalImages = new ArrayList<>();//图片数据源

    public static ArrayList<ImageData> mSelectImages = new ArrayList<>();//保存选中的图片

    public interface DataCallback {
        void onSuccess(ArrayList<ImageFolder> imageFolders);
    }

    public static synchronized ImageModel getInstance() {
        if (mInstance == null) {
            mInstance = new ImageModel();
        }
        return mInstance;
    }

    /**
     * 异步加载图片
     *
     * @param context
     * @param callback
     */
    public void asyncLoadImage(Context context, DataCallback callback) {
        new Thread(() -> {
            if (callback != null) {
                resetImageData();
                scanImages(context);
                callback.onSuccess(splitFolder(context, mTotalImages));
            }
        }).start();
    }

    /**
     * 扫描图片
     *
     * @return
     */
    private void scanImages(Context context) {
        Cursor mCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED},
                null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        //读取扫描到的图片
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                // 获取图片的id
                long id = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
                //获取图片时间
                long time = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                //获取图片名称
                String name = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                //获取图片类型
                String mimeType = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                //获取图片路径
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                //获取图片uri
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                if (checkImageValid(path))
                    mTotalImages.add(new ImageData(time, mimeType, name, path, uri));
            }
            mCursor.close();
        }
    }

    private void resetImageData() {
        mTotalImages.clear();
        mSelectImages.clear();
    }

    /**
     * 检查图片是否有效
     *
     * @param path
     * @return
     */
    private boolean checkImageValid(String path) {
        if (!TextUtils.isEmpty(path)) {
            return new File(path).exists();
        }
        return false;
    }

    /**
     * 根据图片路径，获取图片文件夹名称
     *
     * @param path
     * @return
     */
    private String getFolderName(String path) {
        if (!TextUtils.isEmpty(path)) {
            String[] strings = path.split(File.separator);
            if (strings.length >= 2) {
                return strings[strings.length - 2];
            }
        }
        return "";
    }

    /**
     * 获取图片所属的文件夹
     *
     * @param image
     * @param folders
     * @return
     */
    private ImageFolder getFolder(ImageData image, List<ImageFolder> folders) {
        String name = getFolderName(image.getPath());
        for (ImageFolder folder : folders) {
            if (name.equals(folder.getFolderName())) {
                return folder;
            }
        }
        ImageFolder newFolder = new ImageFolder(name);
        folders.add(newFolder);
        return newFolder;
    }

    /**
     * 把图片按文件夹拆分，第一个文件夹保存所有的图片
     *
     * @param images
     * @return
     */
    private ArrayList<ImageFolder> splitFolder(Context context, ArrayList<ImageData> images) {
        ArrayList<ImageFolder> folders = new ArrayList<>();
        folders.add(new ImageFolder(context.getString(R.string.selector_all_image), images));
        for (ImageData image : images) {
            ImageFolder folder = getFolder(image, folders);
            folder.addImage(image);
        }
        return folders;
    }
}
