package com.xingchen.imageselector.entry;

import java.io.Serializable;

public class PermissionTip implements Serializable {
    public String title;
    public String content;

    public PermissionTip() {
        this.title = "存储空间权限使用说明";
        this.content = "用于读取/修改/删除/保存/上传/下载图片、文件等信息，以便于选取照片、设置头像以及意见反馈等目的。";
    }

    public PermissionTip(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
