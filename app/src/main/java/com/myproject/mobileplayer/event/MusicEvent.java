package com.myproject.mobileplayer.event;

/**
 * Created by lxj on 2017/3/10.
 * MusicEvent包含2个因素，a:这个是什么事件  b:这个事件附带的数据
 */

public class MusicEvent {
    public static final int EVENT_SHOW_MUSIC_INFO = 1;//显示歌曲信息的事件
    public static final int EVENT_UPDATE_MUSIC_PROGRESS = 2;//更新音乐播放进度的事件
    public static final int EVENT_TOOGLE_MUSIC_PLAY = 3;//切换音乐播放和暂停的事件
    public static final int EVENT_MUSIC_STATE_CHANGE = 4;//音乐播放状态更改的事件
    public static final int EVENT_SeekBar_Change = 5;//Seekbar进度拖拽的事件
    public static final int EVENT_Stop_Update_Progress = 6;//告诉Service不要在发消息了的事件
    public static final int EVENT_Play_Pre = 7;//告诉Service播放前一首的事件
    public static final int EVENT_Play_Next = 8;//告诉Service播放下一首的事件
    public static final int EVENT_TOOTLE_PLAYMODE = 9;//告诉Service切换播放模式的事件
    public static final int EVENT_UPDATE_PLAYMODE_BG = 10;//告诉Activity更新播放模式的背景图片

    public int what;
    public Object obj;

    public MusicEvent(int what, Object obj) {
        this.what = what;
        this.obj = obj;
    }



}
