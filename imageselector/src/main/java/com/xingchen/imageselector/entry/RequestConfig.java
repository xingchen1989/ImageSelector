package com.xingchen.imageselector.entry;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @Author teach liang
 * @Description 封装请求参数
 * @Date 2019/9/23
 */
public class RequestConfig implements Parcelable {

    public boolean isCrop = false; // 是否剪切
    public boolean isSingle = false; // 是否单选
    public boolean canPreview = true; // 是否可以点击图片预览
    public boolean enableCamera = true;  // 是否支持拍照
    public boolean onlyTakePhoto = false; // 仅拍照，不打开相册。true时，useCamera也必定为true。
    public int maxSelectCount = 0; //图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
    public float cropRatio = 1.0f; // 图片剪切的宽高比，宽固定为手机屏幕的宽。
    public ArrayList<String> selected; //接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，重新打开选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。

    public RequestConfig() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isCrop ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isSingle ? (byte) 1 : (byte) 0);
        dest.writeByte(this.canPreview ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enableCamera ? (byte) 1 : (byte) 0);
        dest.writeByte(this.onlyTakePhoto ? (byte) 1 : (byte) 0);
        dest.writeInt(this.maxSelectCount);
        dest.writeFloat(this.cropRatio);
        dest.writeStringList(this.selected);
    }

    private RequestConfig(Parcel in) {
        this.isCrop = in.readByte() != 0;
        this.isSingle = in.readByte() != 0;
        this.canPreview = in.readByte() != 0;
        this.enableCamera = in.readByte() != 0;
        this.onlyTakePhoto = in.readByte() != 0;
        this.maxSelectCount = in.readInt();
        this.cropRatio = in.readFloat();
        this.selected = in.createStringArrayList();
    }

    public static final Creator<RequestConfig> CREATOR = new Creator<RequestConfig>() {
        @Override
        public RequestConfig createFromParcel(Parcel source) {
            return new RequestConfig(source);
        }

        @Override
        public RequestConfig[] newArray(int size) {
            return new RequestConfig[size];
        }
    };
}
