package com.myproject.mobileplayer.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myproject.mobileplayer.R;
import com.myproject.mobileplayer.adapter.MediaAdapter;
import com.myproject.mobileplayer.bean.MediaItem;
import com.myproject.mobileplayer.db.mediaQueryHandler;
import com.myproject.mobileplayer.ui.activity.MusicPlayActivity;
import com.myproject.mobileplayer.ui.activity.VideoPlayActivity;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/3/8.
 */

public class MediaListFragment extends Fragment {
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_MUSIC = "music";
    private mediaQueryHandler queryHandler;
    private MediaAdapter mediaAdapter;
    private  String type;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_media_list, null);
        ButterKnife.bind(this, view);
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        type = bundle.getString("type");

        setupRecycle();
        queryHandler = new mediaQueryHandler(getActivity().getContentResolver());
        if (type ==TYPE_VIDEO){
            loadVideo();
        }else{
            loadMusic();
        }

    }

    private void loadMusic() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String []Projection = {MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ARTIST};
        String orderBy = MediaStore.Audio.Media.DISPLAY_NAME;
        queryHandler.startQuery(1,mediaAdapter,uri,
                Projection,null,null,orderBy
        );

    }

    private void loadVideo() {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[]projection={MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.ARTIST
        };
        String order =  MediaStore.Video.Media.DISPLAY_NAME;
        queryHandler.startQuery(0,mediaAdapter,uri,projection,null,null,order);

    }

    private void setupRecycle() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mediaAdapter = new MediaAdapter();
        recyclerView.setAdapter(mediaAdapter);
        mediaAdapter.setOnItemClickListener(new MediaAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(ArrayList<MediaItem> item, int position) {
                Intent intent = new Intent(getContext(), type == TYPE_VIDEO ? VideoPlayActivity.class : MusicPlayActivity.class);
                intent.putExtra("video",item);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
