package com.xingchen.imageselector.entry;

import android.net.Uri;

/**
 * 媒体实体类
 */
public class MediaData {
    private String mimeType;//媒体类型
    private String mediaName;//媒体名称
    private String mediaPath;//媒体路径
    private Uri contentUri;//媒体uri
    private long addedTime;//添加时间
    private long duration;//视频时长
    private boolean isSelected;//是否选中

    public MediaData(String mimeType, String mediaName, String mediaPath, Uri contentUri, long addedTime) {
        this(mimeType, mediaName, mediaPath, contentUri, addedTime, 0);
    }

    public MediaData(String mimeType, String mediaName, String mediaPath, Uri contentUri, long addedTime, long duration) {
        this.addedTime = addedTime;
        this.duration = duration;
        this.mimeType = mimeType;
        this.mediaName = mediaName;
        this.mediaPath = mediaPath;
        this.contentUri = contentUri;
    }

    public String getMediaName() {
        return mediaName == null ? "" : mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName == null ? "" : mediaName;
    }

    public String getMimeType() {
        return mimeType == null ? "" : mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType == null ? "" : mimeType;
    }

    public String getMediaPath() {
        return mediaPath == null ? "" : mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath == null ? "" : mediaPath;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }

    public long getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * 是否是Gif图片
     *
     * @return
     */
    public boolean isGif() {
        return "image/gif".equals(mimeType);
    }
}
