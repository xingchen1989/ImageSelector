package com.xingchen.imageselector.adapter;

import android.annotation.SuppressLint;
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
import com.xingchen.imageselector.entry.MediaData;
import com.xingchen.imageselector.entry.MediaFolder;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private ArrayList<MediaFolder> mediaFolders = new ArrayList<>();
    private OnFolderListener folderListener;
    private final Context context;

    public interface OnFolderListener {
        void onFolderSelect(MediaFolder folder);
    }

    public FolderAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        View view = mInflater.inflate(R.layout.adapter_folder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        MediaFolder mediaFolder = mediaFolders.get(position);
        List<MediaData> mediaList = mediaFolder.getMediaList();
        holder.tvFolderName.setText(mediaFolder.getFolderName());
        holder.ivFolderSelect.setSelected(mediaFolder.isSelected());
        holder.tvFolderSize.setText(String.format("%1$s项", mediaFolder.getMediaList().size()));
        if (!mediaList.isEmpty()) {
            Glide.with(context).load(mediaList.get(0).getContentUri()).into(holder.ivFolderImage);
        }
        holder.itemView.setOnClickListener(v -> {
            this.refresh(mediaFolder);
            if (folderListener != null) {
                folderListener.onFolderSelect(mediaFolder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaFolders == null ? 0 : mediaFolders.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(ArrayList<MediaFolder> folders) {
        mediaFolders = folders;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(MediaFolder folder) {
        for (MediaFolder item : mediaFolders) {
            item.setSelected(item == folder);
        }
        notifyDataSetChanged();
    }

    public void setOnFolderListener(OnFolderListener folderListener) {
        this.folderListener = folderListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName;
        TextView tvFolderSize;
        ImageView ivFolderImage;
        ImageView ivFolderSelect;

        public ViewHolder(View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tv_name);
            tvFolderSize = itemView.findViewById(R.id.tv_size);
            ivFolderImage = itemView.findViewById(R.id.iv_image);
            ivFolderSelect = itemView.findViewById(R.id.iv_select);
        }
    }
}
