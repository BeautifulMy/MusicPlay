package com.myproject.mobileplayer.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;


import com.myproject.mobileplayer.R;
import com.myproject.mobileplayer.bean.MediaItem;
import com.myproject.mobileplayer.ui.activity.MusicPlayActivity;
import com.myproject.mobileplayer.ui.activity.MusicPlayService;
import com.myproject.mobileplayer.utils.StringUtils;

import java.util.Random;
import java.util.UUID;

/**
 * Created by lxj on 2017/3/10.
 */

public class MusicNotification {

    private static MusicNotification mInstance = null;
    private Context context;
    private MusicNotification(Context context){
        this.context = context;
        managerCompat = NotificationManagerCompat.from(context);
    }
    public static MusicNotification create(Context context){
        if(mInstance==null){
            mInstance = new MusicNotification(context);
        }
         return mInstance;
    }


    private int ID = (int) System.currentTimeMillis();
    private NotificationManagerCompat managerCompat;
    MediaItem mediaItem;
    /**
     * 发送一个通知在通知栏中
     */
    public void sendNotification( MediaItem item){
        mediaItem = item;
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.icon_notification)
                .setTicker("正在播放："+ StringUtils.formatMedia(item.name))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)//设置不可以被手指滑动掉
                .setContent(getRemoteViews())//设置自定义的内容视图
                .build();

        managerCompat.notify(ID,notification);
    }
    //获取通知的自定义的视图
    private RemoteViews getRemoteViews() {
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.notification_music);
        //设置歌曲名称
        remoteView.setTextViewText(R.id.notification_title,StringUtils.formatMedia(mediaItem.name));
        //设置艺术家名称
        remoteView.setTextViewText(R.id.notification_artist,mediaItem.artist);
        //设置点击事件
        remoteView.setOnClickPendingIntent(R.id.notification_pre,getPreIntent());
        //给下一首按钮设置点击事件
        remoteView.setOnClickPendingIntent(R.id.notification_next,getNextIntent());
        //给整个通知的布局设置点击
        remoteView.setOnClickPendingIntent(R.id.notification,getNotificationIntent());


        return remoteView;
    }

    private PendingIntent getNotificationIntent() {
        Intent intent = new Intent(context, MusicPlayActivity.class);
        intent.putExtra("action","notification");
        PendingIntent pi = PendingIntent.getActivity(context,3,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    //当点击下一个按钮的时候执行的Intent对象
    private PendingIntent getNextIntent() {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.putExtra("action","next");
        PendingIntent pi = PendingIntent.getService(context,2,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    //当点击前一个按钮的时候执行的Intent对象
    private PendingIntent getPreIntent() {
        Intent intent = new Intent(context, MusicPlayService.class);
        intent.putExtra("action","pre");
        PendingIntent pi = PendingIntent.getService(context,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    /**
     * 移除一个通知
     */
    public void removeNotification(){
        managerCompat.cancel(ID);
    }
}
