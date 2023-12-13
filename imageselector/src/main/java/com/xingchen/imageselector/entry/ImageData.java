package com.xingchen.imageselector.entry;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 图片实体类
 */
public class ImageData implements Parcelable {
    private String mimeType;//图片类型
    private String name;//图片名称
    private String path;//图片路径
    private Uri contentUri;//图片uri
    private long addedTime;//图片首次添加时间
    private boolean isSelected;//是否选中

    public ImageData(long addedTime, String mimeType, String name, String path, Uri contentUri) {
        this.addedTime = addedTime;
        this.mimeType = mimeType;
        this.name = name;
        this.path = path;
        this.contentUri = contentUri;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public String getMimeType() {
        return mimeType == null ? "" : mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType == null ? "" : mimeType;
    }

    public String getPath() {
        return path == null ? "" : path;
    }

    public void setPath(String path) {
        this.path = path == null ? "" : path;
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
        dest.writeString(this.name);
        dest.writeString(this.mimeType);
        dest.writeParcelable(this.contentUri, flags);
    }

    private ImageData(Parcel in) {
        this.addedTime = in.readLong();
        this.name = in.readString();
        this.mimeType = in.readString();
        this.contentUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<ImageData> CREATOR = new Creator<ImageData>() {
        @Override
        public ImageData createFromParcel(Parcel source) {
            return new ImageData(source);
        }

        @Override
        public ImageData[] newArray(int size) {
            return new ImageData[size];
        }
    };
}
