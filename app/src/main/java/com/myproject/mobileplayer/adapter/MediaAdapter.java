package com.myproject.mobileplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myproject.mobileplayer.R;
import com.myproject.mobileplayer.bean.MediaItem;
import com.myproject.mobileplayer.utils.DateUtil;
import com.myproject.mobileplayer.utils.StringUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lxj on 2017/3/7.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaHolder> {
    ArrayList<MediaItem> list = new ArrayList<>();
    private boolean isShowMusic;

    public void setShowMusic() {
        isShowMusic = true;
    }

    public void setData(ArrayList<MediaItem> data) {
        this.list.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public MediaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.adapter_media, null);
        return new MediaHolder(view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(MediaHolder holder, final int position) {
        MediaItem mediaItem = list.get(position);
        holder.tvName.setText(StringUtils.formatMedia(mediaItem.name));
        holder.tvSize.setText(Formatter.formatFileSize(holder.itemView.getContext(), mediaItem.size));
        holder.tvDuration.setText(isShowMusic ? mediaItem.artist : DateUtil.formateDuration(mediaItem.duration));

        //给itemView添加点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(list, position);
            }
        });

    }

    static class MediaHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_duration)
        TextView tvDuration;
        @Bind(R.id.tv_size)
        TextView tvSize;


        public MediaHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(ArrayList<MediaItem> item, int position);
    }
}
