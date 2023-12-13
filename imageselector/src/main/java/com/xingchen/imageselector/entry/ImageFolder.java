package com.xingchen.imageselector.entry;

import java.util.ArrayList;

/**
 * 图片文件夹实体类
 */
public class ImageFolder {
    private boolean enableCamera; // 是否可以调用相机拍照，只有“全部”文件夹才可以拍照。
    private String folderName;// 文件夹名称
    private ArrayList<ImageData> imageList;// 文件夹包含的图片

    public ImageFolder(String folderName) {
        this.folderName = folderName;
    }

    public ImageFolder(String folderName, ArrayList<ImageData> imageList) {
        this.folderName = folderName;
        this.imageList = imageList;
    }

    public boolean isEnableCamera() {
        return enableCamera;
    }

    public void setEnableCamera(boolean enableCamera) {
        this.enableCamera = enableCamera;
    }

    public String getFolderName() {
        return folderName == null ? "" : folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName == null ? "" : folderName;
    }

    public ArrayList<ImageData> getImageList() {
        if (imageList == null) {
            return new ArrayList<>();
        }
        return imageList;
    }

    public void setImageList(ArrayList<ImageData> imageList) {
        this.imageList = imageList;
    }

    /**
     * 向该文件夹添加图片
     *
     * @param image
     */
    public void addImage(ImageData image) {
        if (image != null) {
            if (imageList == null) {
                imageList = new ArrayList<>();
            }
            imageList.add(image);
        }
    }

    @Override
    public String toString() {
        return "Folder{" + "name='" + folderName + '\'' + ", images=" + imageList + '}';
    }
}
