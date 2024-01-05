package com.xingchen.imageselector.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.xingchen.imageselector.entry.MediaData;
import com.xingchen.imageselector.entry.MediaFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaModel {
    private static MediaModel mInstance = new MediaModel();//数据model单例

    public static ArrayList<MediaData> mTotalMedias = new ArrayList<>();//媒体数据源

    public static ArrayList<MediaData> mSelectMedias = new ArrayList<>();//保存选中的媒体

    public interface DataCallback {
        void onSuccess(ArrayList<MediaFolder> mediaFolders);
    }

    public static synchronized MediaModel getInstance() {
        if (mInstance == null) {
            mInstance = new MediaModel();
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
                mTotalMedias.clear();
                mSelectMedias.clear();
                scanImages(context, context.getContentResolver());
                callback.onSuccess(splitFolder("全部图片", mTotalMedias));
            }
        }).start();
    }

    /**
     * 异步加载视频
     *
     * @param context
     * @param callback
     */
    public void asyncLoadVideo(Context context, DataCallback callback) {
        new Thread(() -> {
            if (callback != null) {
                mTotalMedias.clear();
                mSelectMedias.clear();
                scanVideos(context, context.getContentResolver());
                callback.onSuccess(splitFolder("全部视频", mTotalMedias));
            }
        }).start();
    }

    /**
     * 返回第一个被选中数据
     *
     * @return
     */
    public int getFirstSelect() {
        if (mSelectMedias.isEmpty()) return 0;
        else return mTotalMedias.indexOf(mSelectMedias.get(0));
    }

    /**
     * 根据媒体路径，获取媒体文件夹名称
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
     * 获取媒体所属的文件夹
     *
     * @param mediaData
     * @param mediaFolders
     * @return
     */
    private MediaFolder getMediaFolder(MediaData mediaData, List<MediaFolder> mediaFolders) {
        String category = mediaData.getCategory();
        for (MediaFolder mediaFolder : mediaFolders) {
            if (category.equals(mediaFolder.getFolderName())) {
                return mediaFolder;
            }
        }
        MediaFolder newFolder = new MediaFolder(category);
        mediaFolders.add(newFolder);
        return newFolder;
    }

    /**
     * 把媒体按文件夹拆分，第一个文件夹保存所有的媒体
     *
     * @param mediaList
     * @return
     */
    private ArrayList<MediaFolder> splitFolder(String title, ArrayList<MediaData> mediaList) {
        ArrayList<MediaFolder> mediaFolders = new ArrayList<>();
        mediaFolders.add(new MediaFolder(title, mediaList).setSelected(true));
        for (MediaData mediaData : mediaList) {
            MediaFolder mediaFolder = getMediaFolder(mediaData, mediaFolders);
            mediaFolder.addMediaData(mediaData);
        }
        return mediaFolders;
    }

    /**
     * 扫描图片
     *
     * @return
     */
    private void scanImages(Context context, ContentResolver contentResolver) {
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
        };
        Cursor mCursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC"
        );
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                //获取图片的id
                long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                //获取图片时间
                long addTime = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                //获取图片路径
                String mediaPath = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                //获取图片类型
                String type = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                //获取图片uri
                Uri mediaUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                //在这里可以处理每张图片的ID、名称和URI
                mTotalMedias.add(new MediaData(getFolderName(mediaPath), type, mediaUri, addTime));
            }
            mCursor.close();
        }
    }

    /**
     * 扫描视频
     *
     * @param contentResolver
     */
    private void scanVideos(Context context, ContentResolver contentResolver) {
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.MIME_TYPE,
        };
        Cursor mCursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC"
        );
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                //获取视频的id
                long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                //获取视频时长
                long duration = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                //获取视频时间
                long addTime = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                //获取视频路径
                String mediaPath = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                //获取视频类型
                String type = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                //获取视频uri
                Uri mediaUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                //在这里可以处理每个视频的ID、名称和URI
                mTotalMedias.add(new MediaData(getFolderName(mediaPath), type, mediaUri, addTime, duration));
            }
            mCursor.close();
        }
    }
}
