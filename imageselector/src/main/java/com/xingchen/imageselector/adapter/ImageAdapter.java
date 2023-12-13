package com.xingchen.imageselector.adapter;

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
    private ItemActionListener mItemActionListener;//图片操作监听器
    private ArrayList<ImageData> mTotalImages = new ArrayList<>();//图片数据源
    private ArrayList<ImageData> mSelectImages = new ArrayList<>();//保存选中的图片

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
            holder.bindCamera();
        } else {
            ImageData image = getImage(position);
            holder.bindImageData(mContext, image);
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
     * 设置图片选中和未选中的效果
     */
    private void setItemSelectState(ViewHolder holder, boolean isSelect) {
        if (isSelect) {
            holder.ivSelect.setImageResource(R.drawable.icon_image_select);
        } else {
            holder.ivSelect.setImageResource(R.drawable.icon_image_un_select);
        }
    }

    /**
     * 选中图片并回调监听
     *
     * @param image
     */
    private void selectImage(ImageData image) {
        mSelectImages.add(image);
//        if (mImageSelectListener != null) {
//            mImageSelectListener.OnImageSelect(image, true, mSelectImages.size());
//        }
    }

    /**
     * 取消选中图片并回调监听
     *
     * @param image
     */
    private void unSelectImage(ImageData image) {
        mSelectImages.remove(image);
//        if (mImageSelectListener != null) {
//            mImageSelectListener.OnImageSelect(image, false, mSelectImages.size());
//        }
    }

    /**
     * 清除当前选中的图片，并且更新视图
     */
    private void clearImageSelect() {
        for (ImageData image : mSelectImages) {
            mSelectImages.remove(image);
            int index = mTotalImages.indexOf(image);
            int changeIndex = config.useCamera ? index + 1 : index;
            if (index != -1) notifyItemChanged(changeIndex);
        }
    }

    /**
     * 处理图片选中状态
     *
     * @param holder
     * @param image
     */
    private void checkedImage(ViewHolder holder, ImageData image) {
        if (mSelectImages.contains(image)) {
            //如果图片已经选中，就取消选中
            unSelectImage(image);
            setItemSelectState(holder, false);
        } else if (config.isSingle) {
            //如果是单选，就先清空已经选中的图片，再选中当前图片
            clearImageSelect();
            selectImage(image);
            setItemSelectState(holder, true);
        } else if (config.maxCount <= 0 || mSelectImages.size() < config.maxCount) {
            //如果不限制图片的选中数量，或者图片的选中数量还没有达到最大限制，就直接选中当前图片。
            selectImage(image);
            setItemSelectState(holder, true);
        } else {
            //图片选择数量达到最大张数时提示用户
            Toast.makeText(mContext, String.format("您最多只能选择%1$s张照片", config.maxCount), Toast.LENGTH_SHORT).show();
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
        if (mTotalImages != null && !mTotalImages.isEmpty()) {
            if (config.useCamera) {
                return mTotalImages.get(position > 0 ? position - 1 : 0);
            } else {
                return mTotalImages.get(position);
            }
        }
        return null;
    }

    public ArrayList<ImageData> getTotalImages() {
        return mTotalImages;
    }

    public ArrayList<ImageData> getSelectImages() {
        return mSelectImages;
    }

    public void setItemActionListener(ItemActionListener mItemClickListener) {
        this.mItemActionListener = mItemClickListener;
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

        private void bindCamera() {
            itemView.setOnClickListener(v -> {
                if (mItemActionListener != null) {
                    mItemActionListener.OnCameraClick();
                }
            });
        }

        public void bindImageData(Context context, ImageData data) {
            Glide.with(context).load(data.getContentUri()).into(ivImage);
            ivTypeGif.setVisibility(data.isGif() ? View.VISIBLE : View.GONE);
            ivSelect.setSelected(data.isSelected());
            ivSelect.setOnClickListener(view -> {
                data.setSelected(!data.isSelected());
                if (mItemActionListener != null) {
                    mItemActionListener.OnImageSelect(data, mSelectImages.size());
                }
            });
            itemView.setOnClickListener(v -> {
//                if(){
//
//                }
            });
//            setItemSelectState(holder, mSelectImages.contains(image));
//            holder.ivSelect.setOnClickListener(v -> checkedImage(holder, image));
//            holder.itemView.setOnClickListener(v -> {
//                if (config.canPreview && mItemClickListener != null) {
//                    int adapterPosition = holder.getBindingAdapterPosition();
//                    mItemClickListener.OnItemClick(image, config.useCamera ? adapterPosition - 1 : adapterPosition);
//                } else {
//                    checkedImage(holder, image);
//                }
//            });
        }
    }
}