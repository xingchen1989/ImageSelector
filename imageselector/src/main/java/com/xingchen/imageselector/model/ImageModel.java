package com.xingchen.imageselector.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.xingchen.imageselector.R;
import com.xingchen.imageselector.entry.Image;
import com.xingchen.imageselector.entry.ImageFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageModel {
    public static void loadImage(final Context context, final DataCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (ImageModel.class) {
                    ArrayList<Image> imageList = scanImages(context);
                    if (callback != null) {
                        callback.onSuccess(splitFolder(context, imageList));
                    }
                }
            }
        }).start();
    }

    /**
     * 扫描图片
     *
     * @return
     */
    private static ArrayList<Image> scanImages(Context context) {
        ArrayList<Image> images = new ArrayList<>();
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
                images.add(new Image(time, mimeType, name, path, uri));
            }
            mCursor.close();
        }
        return images;
    }

    /**
     * 根据图片路径，获取图片文件夹名称
     *
     * @param path
     * @return
     */
    private static String getFolderName(String path) {
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
    private static ImageFolder getFolder(Image image, List<ImageFolder> folders) {
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
    private static ArrayList<ImageFolder> splitFolder(Context context, ArrayList<Image> images) {
        ArrayList<ImageFolder> folders = new ArrayList<>();
        folders.add(new ImageFolder(context.getString(R.string.selector_all_image), images));
        for (Image image : images) {
            ImageFolder folder = getFolder(image, folders);
            folder.addImage(image);
        }
        return folders;
    }

    public interface DataCallback {
        void onSuccess(ArrayList<ImageFolder> imageFolders);
    }
}
