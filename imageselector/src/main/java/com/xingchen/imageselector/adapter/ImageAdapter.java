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
import com.xingchen.imageselector.entry.Image;
import com.xingchen.imageselector.entry.RequestConfig;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_IMAGE = 2;
    private ArrayList<Image> mSelectImages = new ArrayList<>(); //保存选中的图片
    private ArrayList<Image> mTotalImages = new ArrayList<>();//数据源，包含当前显示的所有图片
    private OnImageSelectListener mSelectListener;//图片选择监听器
    private OnItemClickListener mItemClickListener;//Item点击监听器
    private RequestConfig config;
    private Context mContext;

    public ImageAdapter(Context mContext, RequestConfig config) {
        this.mContext = mContext;
        this.config = config;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        if (viewType == TYPE_IMAGE) {
            View view = mInflater.inflate(R.layout.adapter_images_item, parent, false);
            viewHolder = new ViewHolder(view);
        } else if (viewType == TYPE_CAMERA) {
            View view = mInflater.inflate(R.layout.adapter_camera_item, parent, false);
            viewHolder = new ViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_IMAGE) {
            final Image image = getImage(position);
            Glide.with(mContext).load(image.getContentUri()).into(holder.ivImage);
            holder.ivTypeGif.setVisibility(image.isGif() ? View.VISIBLE : View.GONE);
            setItemSelectState(holder, mSelectImages.contains(image));
            holder.ivSelect.setOnClickListener(v -> checkedImage(holder, image));
            holder.itemView.setOnClickListener(v -> {
                if (config.canPreview && mItemClickListener != null) {
                    int adapterPosition = holder.getBindingAdapterPosition();
                    mItemClickListener.OnItemClick(image, config.useCamera ? adapterPosition - 1 : adapterPosition);
                } else {
                    checkedImage(holder, image);
                }
            });
        } else if (getItemViewType(position) == TYPE_CAMERA) {
            holder.itemView.setOnClickListener(v -> {
                if (mItemClickListener != null) {
                    mItemClickListener.OnCameraClick();
                }
            });
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

    private Image getImage(int position) {
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
    private void selectImage(Image image) {
        mSelectImages.add(image);
        if (mSelectListener != null) {
            mSelectListener.OnImageSelect(image, true, mSelectImages.size());
        }
    }

    /**
     * 取消选中图片并回调监听
     *
     * @param image
     */
    private void unSelectImage(Image image) {
        mSelectImages.remove(image);
        if (mSelectListener != null) {
            mSelectListener.OnImageSelect(image, false, mSelectImages.size());
        }
    }

    /**
     * 清除当前选中的图片，并且更新视图
     */
    private void clearImageSelect() {
        if (mTotalImages != null) {
            for (Image image : mSelectImages) {
                mSelectImages.remove(image);
                int index = mTotalImages.indexOf(image);
                int changeIndex = config.useCamera ? index + 1 : index;
                if (index != -1) notifyItemChanged(changeIndex);
            }
        }
    }

    /**
     * 处理图片选中状态
     *
     * @param holder
     * @param image
     */
    private void checkedImage(ViewHolder holder, Image image) {
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
    public void refresh(ArrayList<Image> imageList) {
        mTotalImages = imageList;
        notifyDataSetChanged();
    }

    /**
     * 获取第一个可见项
     *
     * @param firstVisibleItem
     * @return
     */
    public Image getFirstVisibleImage(int firstVisibleItem) {
        if (mTotalImages != null && !mTotalImages.isEmpty()) {
            if (config.useCamera) {
                return mTotalImages.get(firstVisibleItem > 0 ? firstVisibleItem - 1 : 0);
            } else {
                return mTotalImages.get(firstVisibleItem);
            }
        }
        return null;
    }

    public ArrayList<Image> getTotalImages() {
        return mTotalImages;
    }

    public ArrayList<Image> getSelectImages() {
        return mSelectImages;
    }

    public void setSelectListener(OnImageSelectListener mSelectListener) {
        this.mSelectListener = mSelectListener;
    }

    public void setItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnImageSelectListener {
        void OnImageSelect(Image image, boolean isSelect, int selectCount);
    }

    public interface OnItemClickListener {
        void OnItemClick(Image image, int position);

        void OnCameraClick();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
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
    }
}