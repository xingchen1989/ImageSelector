package com.xingchen.imageselector.entry;

import java.util.ArrayList;

/**
 * 媒体文件夹实体类
 */
public class MediaFolder {
    private boolean isSelected;//当前是否选中
    private String folderName;//文件夹名称
    private ArrayList<MediaData> mediaList;//文件夹包含的媒体

    public MediaFolder(String folderName) {
        this.folderName = folderName;
    }

    public MediaFolder(String folderName, ArrayList<MediaData> mediaList) {
        this.mediaList = mediaList;
        this.folderName = folderName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public MediaFolder setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        return this;
    }

    public String getFolderName() {
        return folderName == null ? "" : folderName;
    }

    public MediaFolder setFolderName(String folderName) {
        this.folderName = folderName == null ? "" : folderName;
        return this;
    }

    public ArrayList<MediaData> getMediaList() {
        if (mediaList == null) {
            return new ArrayList<>();
        }
        return mediaList;
    }

    public MediaFolder setMediaList(ArrayList<MediaData> mediaList) {
        this.mediaList = mediaList;
        return this;
    }

    /**
     * 向该文件夹添加媒体
     *
     * @param image
     */
    public void addMediaData(MediaData image) {
        if (mediaList == null) {
            mediaList = new ArrayList<>();
        }
        mediaList.add(image);
    }
}
