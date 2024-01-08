package com.xingchen.imageselector.entry;

import android.net.Uri;

import java.io.Serializable;

/**
 * 媒体实体类
 */
public class MediaData implements Serializable {
    private String mimeType;//媒体类型
    private String category;//媒体目录
    private Uri contentUri;//媒体uri
    private long addedTime;//添加时间
    private long duration;//视频时长
    private boolean isSelected;//是否选中

    public MediaData(String mimeType) {
        this("", mimeType, null, 0);
    }

    public MediaData(String category, String mimeType, Uri contentUri, long addedTime) {
        this(category, mimeType, contentUri, addedTime, 0);
    }

    public MediaData(String category, String mimeType, Uri contentUri, long addedTime, long duration) {
        this.addedTime = addedTime;
        this.duration = duration;
        this.mimeType = mimeType;
        this.category = category;
        this.contentUri = contentUri;
    }

    public String getMimeType() {
        return mimeType == null ? "" : mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType == null ? "" : mimeType;
    }

    public String getCategory() {
        return category == null ? "" : category;
    }

    public void setCategory(String category) {
        this.category = category == null ? "" : category;
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
