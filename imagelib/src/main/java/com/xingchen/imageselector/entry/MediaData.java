package com.xingchen.imageselector.entry;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 媒体实体类
 */
public class MediaData implements Parcelable {
    private String mimeType;//媒体类型
    private String mediaName;//媒体名称
    private String mediaPath;//媒体路径
    private Uri contentUri;//媒体uri
    private long addedTime;//媒体首次添加时间
    private boolean isSelected;//是否选中

    public MediaData(long addedTime, String mimeType, String mediaName, String mediaPath, Uri contentUri) {
        this.addedTime = addedTime;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.addedTime);
        dest.writeString(this.mediaName);
        dest.writeString(this.mimeType);
        dest.writeParcelable(this.contentUri, flags);
    }

    private MediaData(Parcel in) {
        this.addedTime = in.readLong();
        this.mediaName = in.readString();
        this.mimeType = in.readString();
        this.contentUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<MediaData> CREATOR = new Creator<MediaData>() {
        @Override
        public MediaData createFromParcel(Parcel source) {
            return new MediaData(source);
        }

        @Override
        public MediaData[] newArray(int size) {
            return new MediaData[size];
        }
    };
}
