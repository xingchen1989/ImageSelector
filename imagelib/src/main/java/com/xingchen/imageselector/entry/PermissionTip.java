package com.xingchen.imageselector.entry;

import java.io.Serializable;

public class PermissionTip implements Serializable {
    private String cameraTitle;
    private String storageTitle;
    private String cameraContent;
    private String storageContent;
    private String cameraDetailMsg;
    private String storageDetailMsg;

    public PermissionTip() {
        this.cameraTitle = "相机权限使用说明";
        this.storageTitle = "存储空间权限使用说明";
        this.cameraContent = "用于设置头像、即时通讯以及意见反馈等目的。";
        this.storageContent = "用于读取/修改/删除/保存/上传/下载图片、文件等信息，以便于选取照片、设置头像以及意见反馈等目的。";
        this.cameraDetailMsg = "您需要授予我们相机权限，以便我们可以使用拍照功能，用于设置头像、即时通讯以及意见反馈等目的。";
        this.storageDetailMsg = "您需要授予我们存储权限，以便我们可以访问存储空间，用于读取/修改/删除/保存/上传/下载图片、文件等信息，以便于选取照片、设置头像以及意见反馈等目的。";
    }

    public String getCameraTitle() {
        return cameraTitle;
    }

    public PermissionTip setCameraTitle(String cameraTitle) {
        this.cameraTitle = cameraTitle;
        return this;
    }

    public String getStorageTitle() {
        return storageTitle;
    }

    public PermissionTip setStorageTitle(String storageTitle) {
        this.storageTitle = storageTitle;
        return this;
    }

    public String getCameraContent() {
        return cameraContent;
    }

    public PermissionTip setCameraContent(String cameraContent) {
        this.cameraContent = cameraContent;
        return this;
    }

    public String getStorageContent() {
        return storageContent;
    }

    public PermissionTip setStorageContent(String storageContent) {
        this.storageContent = storageContent;
        return this;
    }

    public String getCameraDetailMsg() {
        return cameraDetailMsg;
    }

    public void setCameraDetailMsg(String cameraDetailMsg) {
        this.cameraDetailMsg = cameraDetailMsg;
    }

    public String getStorageDetailMsg() {
        return storageDetailMsg;
    }

    public PermissionTip setStorageDetailMsg(String storageDetailMsg) {
        this.storageDetailMsg = storageDetailMsg;
        return this;
    }
}
