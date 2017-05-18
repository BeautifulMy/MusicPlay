package com.myproject.mobileplayer.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.DrmInitData;
import android.net.Uri;

import com.myproject.mobileplayer.adapter.MediaAdapter;
import com.myproject.mobileplayer.bean.MediaItem;
import com.myproject.mobileplayer.utils.CursorUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/8.
 */

public class mediaQueryHandler extends AsyncQueryHandler {
    public mediaQueryHandler(ContentResolver cr) {
        super(cr);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

        super.onQueryComplete(token, cookie, cursor);
        ArrayList<MediaItem> mediaItems = CursorUtil.cursor2list(cursor);
        if (cookie!=null){
           MediaAdapter mediaAdapter = (MediaAdapter) cookie;
            if (token==1){
                mediaAdapter.setShowMusic();
            }
            mediaAdapter.setData(mediaItems);
        }
    }


}
