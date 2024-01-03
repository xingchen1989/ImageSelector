package com.xingchen.imageselector.adapter;

import static com.xingchen.imageselector.model.MediaModel.mSelectMedias;
import static com.xingchen.imageselector.model.MediaModel.mTotalMedias;

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
import com.xingchen.imageselector.entry.MediaData;
import com.xingchen.imageselector.entry.RequestConfig;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {
    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_IMAGE = 2;
    private final Context context;
    private final RequestConfig config;
    private ItemActionListener actionListener;//图片操作监听器

    public MediaAdapter(Context context, RequestConfig config) {
        this.context = context;
        this.config = config;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        if (viewType == TYPE_CAMERA) {
            return new ViewHolder(mInflater.inflate(R.layout.adapter_camera_item, parent, false));
        } else {
            return new ViewHolder(mInflater.inflate(R.layout.adapter_media_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CAMERA) {
            holder.bindCameraItem();
        } else {
            MediaData image = getImage(position);
            holder.bindImageItem(image, position);
        }
    }

    @Override
    public int getItemCount() {
        return config.useCamera ? mTotalMedias.size() + 1 : mTotalMedias.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (config.useCamera && position == 0) {
            return TYPE_CAMERA;
        } else {
            return TYPE_IMAGE;
        }
    }

    private MediaData getImage(int position) {
        return mTotalMedias.get(config.useCamera ? position - 1 : position);
    }

    /**
     * 选中图片并回调监听
     *
     * @param mediaData
     */
    private void selectImage(MediaData mediaData, int position) {
        mSelectMedias.add(mediaData);
        mediaData.setSelected(true);
        notifyItemChanged(position);
        if (actionListener != null) {
            actionListener.OnMediaSelect(mediaData, mSelectMedias.size());
        }
    }

    /**
     * 取消选中图片并回调监听
     *
     * @param mediaData
     */
    private void unSelectImage(MediaData mediaData, int position) {
        mSelectMedias.remove(mediaData);
        mediaData.setSelected(false);
        notifyItemChanged(position);
        if (actionListener != null) {
            actionListener.OnMediaSelect(mediaData, mSelectMedias.size());
        }
    }

    /**
     * 清除当前选中的图片，并且更新视图
     */
    private void clearSelectImage(ArrayList<MediaData> selectImages) {
        for (MediaData mediaData : selectImages) {
            selectImages.remove(mediaData);
            mediaData.setSelected(false);
            int index = mTotalMedias.indexOf(mediaData);
            notifyItemChanged(config.useCamera ? index + 1 : index);
        }
    }

    /**
     * 处理图片选中状态
     *
     * @param mediaData
     */
    private void onItemSelect(MediaData mediaData, int position) {
        if (mSelectMedias.contains(mediaData)) {
            //如果图片已经选中，就取消选中
            unSelectImage(mediaData, position);
        } else if (config.isSingle) {
            //如果是单选，就先清空已经选中的图片，再选中当前图片
            clearSelectImage(mSelectMedias);
            selectImage(mediaData, position);
        } else if (config.maxCount <= 0 || mSelectMedias.size() < config.maxCount) {
            //如果不限制图片的选中数量，或者图片的选中数量还没有达到最大限制，就直接选中当前图片。
            selectImage(mediaData, position);
        } else {
            //图片选择数量达到最大张数时提示用户
            Toast.makeText(context, String.format("您最多只能选择%1$s张照片", config.maxCount), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击图片
     *
     * @param mediaData
     * @param position
     */
    private void onItemClick(MediaData mediaData, int position) {
        if (config.canPreview && actionListener != null) {
            actionListener.OnMediaClick(mediaData, position);
        } else {
            onItemSelect(mediaData, position);
        }
    }

    /**
     * 刷新列表
     *
     * @param imageList
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refresh(ArrayList<MediaData> imageList) {
        mTotalMedias = imageList;
        notifyDataSetChanged();
    }

    /**
     * 获取第一个可见项
     *
     * @param position
     * @return
     */
    public MediaData getFirstVisibleImage(int position) {
        if (position < 0 || mTotalMedias.isEmpty()) {
            return null;
        }
        if (config.useCamera) {
            return mTotalMedias.get(position > 0 ? position - 1 : 0);
        } else {
            return mTotalMedias.get(position);
        }
    }

    public ArrayList<MediaData> getTotalImages() {
        return mTotalMedias;
    }

    public ArrayList<MediaData> getSelectImages() {
        return mSelectMedias;
    }

    public void setActionListener(ItemActionListener mItemClickListener) {
        this.actionListener = mItemClickListener;
    }

    public interface ItemActionListener {
        void OnCameraClick();

        void OnMediaClick(MediaData media, int position);

        void OnMediaSelect(MediaData media, int selectCount);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCamera;
        ImageView ivMedia;
        ImageView ivSelect;
        ImageView ivTypeGif;

        public ViewHolder(View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.iv_media);
            ivCamera = itemView.findViewById(R.id.iv_camera);
            ivSelect = itemView.findViewById(R.id.iv_select);
            ivTypeGif = itemView.findViewById(R.id.iv_type_gif);
        }

        private void bindCameraItem() {
            itemView.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.OnCameraClick();
                }
            });
        }

        private void bindImageItem(MediaData mediaData, int position) {
            Glide.with(context).load(mediaData.getContentUri()).into(ivMedia);
            ivTypeGif.setVisibility(mediaData.isGif() ? View.VISIBLE : View.GONE);
            ivSelect.setSelected(mediaData.isSelected());
            ivSelect.setOnClickListener(view -> onItemSelect(mediaData, position));
            itemView.setOnClickListener(view -> onItemClick(mediaData, position));
        }
    }
}