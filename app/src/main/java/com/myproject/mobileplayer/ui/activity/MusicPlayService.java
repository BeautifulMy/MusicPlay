package com.myproject.mobileplayer.ui.activity;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.myproject.mobileplayer.bean.MediaItem;
import com.myproject.mobileplayer.event.MusicEvent;
import com.myproject.mobileplayer.notification.MusicNotification;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by Administrator on 2017/3/10.
 */

public class MusicPlayService extends Service {
    private ArrayList<MediaItem> list;
    private int index;

    //播放模式：顺序播放，随机播放，单曲循环
    public static final int MODE_ORDER = 1;//顺序播放
    public static final int MODE_RANDOM = 2;//随机播放
    public static final int MODE_REPEAT = 3;//单曲播放
    private int playMode = MODE_ORDER;//播放模式,默认是顺序播放
    public static final int MSG_UPDATE_MUSIC_PROGRESS = 1;//更新音乐的播放进度
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_MUSIC_PROGRESS:
                    notifyMusicUpdate();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override

    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
//首先获取Action
            String action = intent.getStringExtra("action");
            if ("pre".equals(action)) {
                //说明是从通知栏的pre按钮点击进入service的，
                playPre();
            } else if ("next".equals(action)) {
                //说明是从通知栏的next按钮点击进入Service的
                playNext();
            } else if ("notification".equals(action)) {
                //说明是从点击整个通知进来的,Service仍然是一直在播放音乐的，我们只需要
                //再次更新UI即可
                //需要让activity更新UI
                EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_SHOW_MUSIC_INFO
                        , mediaItem));

                notifyMusicUpdate();
            }
            else {
                list = (ArrayList<MediaItem>) intent.getSerializableExtra("video");
                index = intent.getIntExtra("position", 0);
                //开始播放音乐
                playMusic();
        }
        }


        return START_STICKY;

    }

    //通知音乐进度更新了
    private void notifyMusicUpdate() {
        EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_UPDATE_MUSIC_PROGRESS, mediaPlayer.getCurrentPosition()));
        //开启定时循环发送消息
        handler.sendEmptyMessageDelayed(MSG_UPDATE_MUSIC_PROGRESS, 200);
    }

    MediaPlayer mediaPlayer;
    MediaItem mediaItem;

    /**
     * 播放音乐的方法
     */
    public void playMusic() {
        if (mediaPlayer != null) {
            //需要先释放之前的歌曲资源
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaItem = list.get(index);
        try {
            mediaPlayer.setDataSource(mediaItem.path);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(preparedListener);
            //设置歌曲播放完成的监听器
            mediaPlayer.setOnCompletionListener(completionListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //播放下一首歌曲
    private void playNext() {
        if (index < (list.size() - 1)) {
            index++;
            playMusic();
        } else {
            //说明是最后一个
            Toast.makeText(this, "当前已经是最后一首了！", Toast.LENGTH_SHORT).show();
        }
    }
    @Subscribe
    public void onEvent(MusicEvent event){
        switch (event.what){
            case MusicEvent.EVENT_TOOGLE_MUSIC_PLAY://切换音乐的播放和暂停
                tooglePlay();
                break;
            case MusicEvent.EVENT_SeekBar_Change://进度条拖拽的事件
                mediaPlayer.seekTo((Integer) event.obj);
                //进度改变后，要重新给UI循环发送事件
                handler.sendEmptyMessage(MSG_UPDATE_MUSIC_PROGRESS);
                break;
            case MusicEvent.EVENT_Stop_Update_Progress:
                handler.removeMessages(MSG_UPDATE_MUSIC_PROGRESS);
                break;
            case MusicEvent.EVENT_Play_Pre://要播放前一首
                playPre();
                break;
            case MusicEvent.EVENT_Play_Next://要播放下一首
                playNext();
                break;
            case MusicEvent.EVENT_TOOTLE_PLAYMODE://切换播放模式的事件
                tooglePlayMode();
                break;
        }
    }

    /**
     * 切换当前的播放模式
     */
    private void tooglePlayMode() {
        switch (playMode){
            case MODE_ORDER://顺序播放->随机播放
                playMode = MODE_RANDOM;
                break;
            case MODE_RANDOM://随机播放->单曲播放
                playMode = MODE_REPEAT;
                break;
            case MODE_REPEAT://单曲播放->顺序播放
                playMode = MODE_ORDER;
                break;
        }
        //通知Activity给我更新一下播放模式的背景图片
        EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_UPDATE_PLAYMODE_BG,playMode));
    }


    //准备完成的监听器
    MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            //开始播放歌曲
            mediaPlayer.start();

            //开始显示通知
            MusicNotification.create(MusicPlayService.this).sendNotification(mediaItem);

            //需要让activity更新UI
            EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_SHOW_MUSIC_INFO
                    , mediaItem));

            notifyMusicUpdate();
        }
    };
    //歌曲播放完成的监听器
    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //歌曲播放完成，那么就不应该再发送消息了
            handler.removeMessages(MSG_UPDATE_MUSIC_PROGRESS);

            //那么下一首怎么播放呢？应该按照当前的播放模式来处理
            autoPlayByMode();
        }
    };

    /**
     * 根据当前的播放模式来播放下一首歌曲
     */
    private void autoPlayByMode() {
        switch (playMode) {
            case MODE_ORDER://顺序播放
                //如果当前是最后一首，应该重来拉过，就是播放第1首
                if (index == (list.size() - 1)) {
                    index = 0;
                    playMusic();
                } else {
                    playNext();
                }
                break;
            case MODE_RANDOM://随机播放
                //在歌曲列表中随机选择一首
                index = new Random().nextInt(list.size());
                playMusic();
                break;
            case MODE_REPEAT://单曲循环
                playMusic();
                break;
        }
    }

    //播放前一首歌曲
    private void playPre() {
        if (index > 0) {
            index--;
            playMusic();
        } else {
            //说明当前是第一首了
            Toast.makeText(this, "当前是第一首！", Toast.LENGTH_SHORT).show();
        }
    }

    //切换音乐的播放和暂停
    private void tooglePlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            //暂停之后就不要再发送消息了
            handler.removeMessages(MSG_UPDATE_MUSIC_PROGRESS);
            //移除通知
            MusicNotification.create(MusicPlayService.this).removeNotification();
        } else {
            mediaPlayer.start();
            //开始播放再重新发送消息
            handler.sendEmptyMessage(MSG_UPDATE_MUSIC_PROGRESS);
            //发送通知
            MusicNotification.create(MusicPlayService.this).sendNotification(mediaItem);
        }
        //通过Activity当前的音乐播放是暂停还是播放中
        EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_MUSIC_STATE_CHANGE, mediaPlayer.isPlaying()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //资源的释放
        EventBus.getDefault().unregister(this);
        handler.removeCallbacksAndMessages(null);
        mediaPlayer.release();
    }
}
