package com.xingchen.imageselector.utils;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

import com.xingchen.imageselector.activity.ClipImageActivity;
import com.xingchen.imageselector.activity.ImageSelectorActivity;
import com.xingchen.imageselector.entry.RequestConfig;
import com.xingchen.imageselector.model.ImageModel;

import java.util.ArrayList;

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
     * 图片选择的结果
     */
    public static final String SELECT_RESULT = "select_result";

    /**
     * 最大的图片选择数
     */
    public static final String MAX_SELECT_COUNT = "max_select_count";

    /**
     * 图片选中器的配置
     */
    public static final String KEY_CONFIG = "key_config";

    public static final String IS_CONFIRM = "is_confirm";

    public static final int SELECTOR_RESULT_CODE = 0x00000010;

    public static final int CAMERA_REQUEST_CODE = 0x00000011;

    /**
     * 预加载图片
     *
     * @param context
     */
    public static void preload(Context context) {
        ImageModel.getInstance().preloadAndRegisterContentObserver(context);
    }

    public static ImageSelectorBuilder builder() {
        return new ImageSelectorBuilder();
    }

    public static class ImageSelectorBuilder {

        private RequestConfig config;

        private ImageSelectorBuilder() {
            config = new RequestConfig();
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
            config.enableCamera = enableCamera;
            return this;
        }

        /**
         * 是否仅拍照，不打开相册。true时，useCamera也必定为true
         *
         * @param onlyTakePhoto
         * @return
         */
        public ImageSelectorBuilder onlyTakePhoto(boolean onlyTakePhoto) {
            config.onlyTakePhoto = onlyTakePhoto;
            return this;
        }

        /**
         * 图片的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
         *
         * @param maxSelectCount
         * @return
         */
        public ImageSelectorBuilder setMaxSelectCount(int maxSelectCount) {
            config.maxSelectCount = maxSelectCount;
            return this;
        }

        /**
         * 接收从外面传进来的已选择的图片列表。当用户原来已经有选择过图片，现在重新打开
         * 选择器，允许用户把先前选过的图片传进来，并把这些图片默认为选中状态。
         *
         * @param selected
         * @return
         */
        public ImageSelectorBuilder setSelected(ArrayList<String> selected) {
            config.selected = selected;
            return this;
        }

        /**
         * 打开相册
         *
         * @param activity
         * @param requestCode
         */
        public void start(Activity activity, int requestCode) {
            // 仅拍照，useCamera必须为true
            if (config.onlyTakePhoto) {
                config.enableCamera = true;
            }
            if (config.isCrop) {
                ClipImageActivity.openActivity(activity, requestCode, config);
            } else {
                ImageSelectorActivity.openActivity(activity, requestCode, config);
            }
        }

        /**
         * 打开相册
         *
         * @param fragment
         * @param requestCode
         */
        public void start(Fragment fragment, int requestCode) {
            // 仅拍照，useCamera必须为true
            if (config.onlyTakePhoto) {
                config.enableCamera = true;
            }
            if (config.isCrop) {
                ClipImageActivity.openActivity(fragment, requestCode, config);
            } else {
                ImageSelectorActivity.openActivity(fragment, requestCode, config);
            }
        }
    }
}
