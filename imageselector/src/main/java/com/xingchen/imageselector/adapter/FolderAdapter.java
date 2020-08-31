package com.xingchen.imageselector.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xingchen.imageselector.R;
import com.xingchen.imageselector.entry.Image;
import com.xingchen.imageselector.entry.ImageFolder;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<ImageFolder> mFolders = new ArrayList<>();
    private OnFolderSelectListener mOnFolderSelectListener;

    public FolderAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageFolder folder = mFolders.get(position);
        ArrayList<Image> images = folder.getImageList();
        holder.tvFolderName.setText(folder.getFolderName());
        holder.tvFolderSize.setText(mContext.getString(R.string.selector_image_num, images.size()));
        if (!images.isEmpty()) {
            Glide.with(mContext).load(images.get(0).getContentUri()).into(holder.ivImage);
        }
    }

    @Override
    public int getItemCount() {
        return mFolders == null ? 0 : mFolders.size();
    }

    /**
     * 刷新列表
     *
     * @param folders
     */
    public void refresh(ArrayList<ImageFolder> folders) {
        mFolders = folders;
        notifyDataSetChanged();
    }

    public interface OnFolderSelectListener {
        void OnFolderSelect(ImageFolder folder);
    }

    public void setOnFolderSelectListener(OnFolderSelectListener mOnFolderSelectListener) {
        this.mOnFolderSelectListener = mOnFolderSelectListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivSelect;
        TextView tvFolderName;
        TextView tvFolderSize;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivSelect = itemView.findViewById(R.id.iv_select);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name);
            tvFolderSize = itemView.findViewById(R.id.tv_folder_size);
        }
    }
}
