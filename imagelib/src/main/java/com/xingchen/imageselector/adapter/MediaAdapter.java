package com.xingchen.imageselector.adapter;

import static com.xingchen.imageselector.model.MediaModel.mSelectMedias;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.xingchen.imageselector.databinding.AdapterCameraItemBinding;
import com.xingchen.imageselector.databinding.AdapterImageItemBinding;
import com.xingchen.imageselector.databinding.AdapterVideoItemBinding;
import com.xingchen.imageselector.entry.MediaData;
import com.xingchen.imageselector.entry.RequestConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {
    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_IMAGE = 2;
    private static final int TYPE_VIDEO = 3;
    private final Context context;
    private final RequestConfig requestConfig;
    private ItemActionListener actionListener;//图片操作监听器
    private ArrayList<MediaData> mediaSources = new ArrayList<>();//媒体数据源
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

    public MediaAdapter(Context context, RequestConfig requestConfig) {
        this.context = context;
        this.requestConfig = requestConfig;
    }

    @Override
    public int getItemCount() {
        return mediaSources == null ? 0 : mediaSources.size();
    }

    @Override
    public int getItemViewType(int position) {
        String mimeType = mediaSources.get(position).getMimeType();
        if (requestConfig.useCamera && position == 0) {
            return TYPE_CAMERA;
        } else if (mimeType.startsWith("video")) {
            return TYPE_VIDEO;
        } else {
            return TYPE_IMAGE;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        ViewBinding viewBinding = null;
        if (viewType == TYPE_IMAGE) {
            viewBinding = AdapterImageItemBinding.inflate(mInflater, parent, false);
        } else if (viewType == TYPE_VIDEO) {
            viewBinding = AdapterVideoItemBinding.inflate(mInflater, parent, false);
        } else if (viewType == TYPE_CAMERA) {
            viewBinding = AdapterCameraItemBinding.inflate(mInflater, parent, false);
        }
        return new ViewHolder(Objects.requireNonNull(viewBinding));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (holder.binding instanceof AdapterCameraItemBinding) {
            holder.itemView.setOnClickListener(view -> onCameraClick());
        } else if (holder.binding instanceof AdapterVideoItemBinding) {
            holder.bindVideoItem(position, (AdapterVideoItemBinding) holder.binding);
        } else if (holder.binding instanceof AdapterImageItemBinding) {
            holder.bindImageItem(position, (AdapterImageItemBinding) holder.binding);
        }
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
            notifyItemChanged(mediaSources.indexOf(mediaData));
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
        } else if (requestConfig.isSingle) {
            //如果是单选，就先清空已经选中的图片，再选中当前图片
            clearSelectImage(mSelectMedias);
            selectImage(mediaData, position);
        } else if (requestConfig.maxCount <= 0 || mSelectMedias.size() < requestConfig.maxCount) {
            //如果不限制图片的选中数量，或者图片的选中数量还没有达到最大限制，就直接选中当前图片。
            selectImage(mediaData, position);
        } else {
            //图片选择数量达到最大张数时提示用户
            String tip = String.format("您最多只能选择%1$s张照片", requestConfig.maxCount);
            Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击图片
     *
     * @param mediaData
     * @param position
     */
    private void onItemClick(MediaData mediaData, int position) {
        if (requestConfig.canPreview && actionListener != null) {
            actionListener.OnMediaClick(mediaData, position);
        } else {
            onItemSelect(mediaData, position);
        }
    }

    /**
     * 点击相机
     */
    private void onCameraClick() {
        if (actionListener != null) {
            actionListener.OnCameraClick();
        }
    }

    /**
     * 刷新列表
     *
     * @param mediaSources
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refresh(ArrayList<MediaData> mediaSources) {
        this.mediaSources = mediaSources;
        notifyDataSetChanged();
    }

    /**
     * 获取第一个可见项
     *
     * @param position
     * @return
     */
    public MediaData getFirstVisibleImage(int position) {
        if (position < 0 || mediaSources.isEmpty()) {
            return null;
        }
        if (requestConfig.useCamera) {
            return mediaSources.get(position > 0 ? position - 1 : 0);
        } else {
            return mediaSources.get(position);
        }
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
        private final ViewBinding binding;

        ViewHolder(@NonNull ViewBinding viewBinding) {
            super(viewBinding.getRoot());
            binding = viewBinding;
        }

        private void bindImageItem(int position, AdapterImageItemBinding binding) {
            MediaData mediaData = mediaSources.get(position);
            Glide.with(context).load(mediaData.getContentUri()).into(binding.ivMedia);
            binding.ivTypeGif.setVisibility(mediaData.isGif() ? View.VISIBLE : View.GONE);
            binding.ivSelect.setOnClickListener(view -> onItemSelect(mediaData, position));
            binding.getRoot().setOnClickListener(view -> onItemClick(mediaData, position));
            binding.ivSelect.setSelected(mediaData.isSelected());
        }

        private void bindVideoItem(int position, AdapterVideoItemBinding binding) {
            MediaData mediaData = mediaSources.get(position);
            Glide.with(context).load(mediaData.getContentUri()).into(binding.ivMedia);
            binding.tvDuration.setText(dateFormat.format(new Date(mediaData.getDuration())));
            binding.ivSelect.setOnClickListener(view -> onItemSelect(mediaData, position));
            binding.getRoot().setOnClickListener(view -> onItemClick(mediaData, position));
            binding.ivSelect.setSelected(mediaData.isSelected());
        }
    }
}