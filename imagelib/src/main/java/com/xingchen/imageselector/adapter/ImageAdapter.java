package com.xingchen.imageselector.adapter;

import static com.xingchen.imageselector.model.ImageModel.mSelectImages;
import static com.xingchen.imageselector.model.ImageModel.mTotalImages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.entry.ImageData;
import com.xingchen.imageselector.entry.RequestConfig;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_IMAGE = 2;
    private final Context mContext;
    private final RequestConfig config;
    private ItemActionListener mActionListener;//图片操作监听器

    public ImageAdapter(Context mContext, RequestConfig config) {
        this.mContext = mContext;
        this.config = config;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        if (viewType == TYPE_CAMERA) {
            return new ViewHolder(mInflater.inflate(R.layout.adapter_camera_item, parent, false));
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.adapter_images_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CAMERA) {
            holder.bindCameraItem();
        } else {
            ImageData image = getImage(position);
            holder.bindImageItem(mContext, image, position);
        }
    }

    @Override
    public int getItemCount() {
        return config.useCamera ? mTotalImages.size() + 1 : mTotalImages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (config.useCamera && position == 0) {
            return TYPE_CAMERA;
        } else {
            return TYPE_IMAGE;
        }
    }

    private ImageData getImage(int position) {
        return mTotalImages.get(config.useCamera ? position - 1 : position);
    }

    /**
     * 选中图片并回调监听
     *
     * @param imageData
     */
    private void selectImage(ImageData imageData, int position) {
        mSelectImages.add(imageData);
        imageData.setSelected(true);
        notifyItemChanged(position);
        if (mActionListener != null) {
            mActionListener.OnImageSelect(imageData, mSelectImages.size());
        }
    }

    /**
     * 取消选中图片并回调监听
     *
     * @param imageData
     */
    private void unSelectImage(ImageData imageData, int position) {
        mSelectImages.remove(imageData);
        imageData.setSelected(false);
        notifyItemChanged(position);
        if (mActionListener != null) {
            mActionListener.OnImageSelect(imageData, mSelectImages.size());
        }
    }

    /**
     * 清除当前选中的图片，并且更新视图
     */
    private void clearSelectImage(ArrayList<ImageData> selectImages) {
        for (ImageData imageData : selectImages) {
            selectImages.remove(imageData);
            imageData.setSelected(false);
            int index = mTotalImages.indexOf(imageData);
            notifyItemChanged(config.useCamera ? index + 1 : index);
        }
    }

    /**
     * 处理图片选中状态
     *
     * @param imageData
     */
    private void onItemSelect(ImageData imageData, int position) {
        if (mSelectImages.contains(imageData)) {
            //如果图片已经选中，就取消选中
            unSelectImage(imageData, position);
        } else if (config.isSingle) {
            //如果是单选，就先清空已经选中的图片，再选中当前图片
            clearSelectImage(mSelectImages);
            selectImage(imageData, position);
        } else if (config.maxCount <= 0 || mSelectImages.size() < config.maxCount) {
            //如果不限制图片的选中数量，或者图片的选中数量还没有达到最大限制，就直接选中当前图片。
            selectImage(imageData, position);
        } else {
            //图片选择数量达到最大张数时提示用户
            Toast.makeText(mContext, String.format("您最多只能选择%1$s张照片", config.maxCount), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击图片
     *
     * @param imageData
     * @param position
     */
    private void onItemClick(ImageData imageData, int position) {
        if (config.canPreview && mActionListener != null) {
            mActionListener.OnImageClick(imageData, position);
        } else {
            onItemSelect(imageData, position);
        }
    }

    /**
     * 刷新列表
     *
     * @param imageList
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refresh(ArrayList<ImageData> imageList) {
        mTotalImages = imageList;
        notifyDataSetChanged();
    }

    /**
     * 获取第一个可见项
     *
     * @param position
     * @return
     */
    public ImageData getFirstVisibleImage(int position) {
        if (position < 0 || mTotalImages.isEmpty()) {
            return null;
        }
        if (config.useCamera) {
            return mTotalImages.get(position > 0 ? position - 1 : 0);
        } else {
            return mTotalImages.get(position);
        }
    }

    public ArrayList<ImageData> getTotalImages() {
        return mTotalImages;
    }

    public ArrayList<ImageData> getSelectImages() {
        return mSelectImages;
    }

    public void setActionListener(ItemActionListener mItemClickListener) {
        this.mActionListener = mItemClickListener;
    }

    public interface ItemActionListener {
        void OnCameraClick();

        void OnImageClick(ImageData image, int position);

        void OnImageSelect(ImageData image, int selectCount);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCamera;
        ImageView ivImage;
        ImageView ivSelect;
        ImageView ivTypeGif;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivCamera = itemView.findViewById(R.id.iv_camera);
            ivSelect = itemView.findViewById(R.id.iv_select);
            ivTypeGif = itemView.findViewById(R.id.iv_type_gif);
        }

        private void bindCameraItem() {
            itemView.setOnClickListener(v -> {
                if (mActionListener != null) {
                    mActionListener.OnCameraClick();
                }
            });
        }

        public void bindImageItem(Context context, ImageData imageData, int position) {
            Glide.with(context).load(imageData.getContentUri()).into(ivImage);
            ivTypeGif.setVisibility(imageData.isGif() ? View.VISIBLE : View.GONE);
            ivSelect.setSelected(imageData.isSelected());
            ivSelect.setOnClickListener(view -> onItemSelect(imageData, position));
            itemView.setOnClickListener(view -> onItemClick(imageData, position));
        }
    }
}