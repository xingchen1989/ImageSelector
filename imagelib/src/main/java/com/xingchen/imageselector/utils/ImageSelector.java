package com.xingchen.imageselector.utils;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.xingchen.imageselector.activity.PermissionActivity;
import com.xingchen.imageselector.entry.PermissionTip;
import com.xingchen.imageselector.entry.RequestConfig;

/**
 * Depiction:
 * Date:2020/8/29
 */
public class ImageSelector {
    /**
     * 是否是来自于相机拍照的图片，
     * 只有本次调用相机拍出来的照片，返回时才为true。
     * 当为true时，图片返回当结果有且只有一张图片。
     */
    public static final String IS_CAMERA_IMAGE = "is_camera_image";

    /**
     * 是否单选
     */
    public static final String IS_SINGLE = "is_single";

    /**
     * 初始位置
     */
    public static final String POSITION = "position";

    /**
     * 最大的图片选择数
     */
    public static final String MAX_COUNT = "max_count";

    /**
     * 图片选中器的配置
     */
    public static final String KEY_CONFIG = "key_config";

    /**
     * 图片选择的结果
     */
    public static final String SELECT_RESULT = "select_result";

    public static final int REQ_IMAGE_CODE = 0x00000010;

    public static final int REQ_CAMERA_CODE = 0x00000011;

    public static final int REQ_VIDEO_CODE = 0x00000012;

    public static ImageSelectorBuilder builder() {
        return new ImageSelectorBuilder();
    }

    public static class ImageSelectorBuilder {
        private final RequestConfig config;

        private ImageSelectorBuilder() {
            config = new RequestConfig();
        }

        /**
         * 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
         *
         * @param maxSelectCount
         * @return
         */
        public ImageSelectorBuilder setMaxSelectCount(int maxSelectCount) {
            config.maxCount = maxSelectCount;
            return this;
        }

        /**
         * 图片剪切的宽高比，宽固定为手机屏幕的宽。
         *
         * @param ratio
         * @return
         */
        public ImageSelectorBuilder setCropRatio(float ratio) {
            config.cropRatio = ratio;
            return this;
        }

        /**
         * 是否使用图片剪切功能。默认false。如果使用了图片剪切功能，相册只能单选。
         *
         * @param isCrop
         * @return
         */
        public ImageSelectorBuilder setCrop(boolean isCrop) {
            config.isCrop = isCrop;
            return this;
        }

        /**
         * 是否单选
         *
         * @param isSingle
         * @return
         */
        public ImageSelectorBuilder setSingle(boolean isSingle) {
            config.isSingle = isSingle;
            return this;
        }

        /**
         * 是否可以点击预览，默认为true
         *
         * @param canPreview
         * @return
         */
        public ImageSelectorBuilder canPreview(boolean canPreview) {
            config.canPreview = canPreview;
            return this;
        }

        /**
         * 是否使用拍照功能。
         *
         * @param enableCamera 默认为true
         * @return
         */
        public ImageSelectorBuilder useCamera(boolean enableCamera) {
            config.useCamera = enableCamera;
            return this;
        }

        /**
         * 是否仅拍照，不打开相册。true时，useCamera也必定为true
         *
         * @param actionType
         * @return
         */
        public ImageSelectorBuilder setActionType(ActionType actionType) {
            config.actionType = actionType;
            return this;
        }

        /**
         * 设置请求权限时的额外说明
         *
         * @param permissionTip
         * @return
         */
        public ImageSelectorBuilder setPermissionTip(PermissionTip permissionTip) {
            config.permissionTip = permissionTip;
            return this;
        }

        /**
         * 打开相册
         *
         * @param activity
         * @param requestCode
         */
        public void start(Activity activity, int requestCode) {
            PermissionActivity.openActivity(activity, config, requestCode);
        }

        /**
         * 打开相册
         *
         * @param fragment
         * @param requestCode
         */
        public void start(Fragment fragment, int requestCode) {
            PermissionActivity.openActivity(fragment, config, requestCode);
        }
    }
}
