package com.myproject.mobileplayer.utils;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;


import com.myproject.mobileplayer.bean.MediaItem;

import java.util.ArrayList;

/**
 * Created by lxj on 2017/3/7.
 */
public class CursorUtil {
    private static final String TAG = "CursorUtil";
    /**
     * 打印cursor的每列数据
     * @param cursor
     */
    public static void printCursor(Cursor cursor) {
        if(cursor==null)return;
        Log.e(TAG, "获取到了 "+ cursor.getCount() +"条数据!");
        //遍历每条记录
        while(cursor.moveToNext()){
            //打印当前记录的所有列的数据
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                //获取字段名
                String columnName = cursor.getColumnName(i);
                //获取字段的值
                String columnValue = cursor.getString(i);
                Log.e(TAG, columnName+ " : "+columnValue);
            }
            Log.e(TAG, "-----------------华丽的分隔符------------------");
        }
    }

    /**
     * 将cursor中的数据取出来存到一个集合中
     * @param cursor
     * @return
     */
    public static ArrayList<MediaItem> cursor2list(Cursor cursor){
        if(cursor==null)return null;

        ArrayList<MediaItem> list = new ArrayList<>();
        while (cursor.moveToNext()){
            //将每条记录的数据传给mediaItem
            MediaItem mediaItem = new MediaItem();
            mediaItem.name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
            mediaItem.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
            mediaItem.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            mediaItem.path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            mediaItem.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));
            list.add(mediaItem);
        }

        return list;
    }
}
