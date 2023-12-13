package com.xingchen.imageselector.entry;

import com.xingchen.imageselector.utils.ActionType;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @Author teach liang
 * @Description 封装请求参数
 * @Date 2019/9/23
 */
public class RequestConfig implements Serializable {
    public boolean isCrop = false; // 是否剪切
    public boolean isSingle = false; // 是否单选
    public boolean canPreview = true; // 是否可以预览
    public boolean useCamera = true;  // 是否支持拍照
    public int maxCount = 0; // 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
    public float cropRatio = 1.0f; // 图片剪切的宽高比，宽固定为手机屏幕的宽。
    public ActionType actionType = ActionType.PICK_PHOTO; // 默认选择照片。
    public ArrayList<String> selected; //接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，重新打开选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
}
