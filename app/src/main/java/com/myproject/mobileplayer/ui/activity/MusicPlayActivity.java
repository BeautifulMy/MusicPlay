package com.myproject.mobileplayer.ui.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;

import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.myproject.mobileplayer.R;
import com.myproject.mobileplayer.bean.MediaItem;
import com.myproject.mobileplayer.event.MusicEvent;
import com.myproject.mobileplayer.utils.DateUtil;
import com.myproject.mobileplayer.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/8.
 */

public class MusicPlayActivity extends BaseActivity {

    @Bind(R.id.iv_back)
    ImageView ivBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_anim)
    ImageView ivAnim;
    @Bind(R.id.tv_music_time)
    TextView tvMusicTime;
    @Bind(R.id.sb_music)
    SeekBar sbMusic;
    @Bind(R.id.iv_playmode)
    ImageView ivPlaymode;
    @Bind(R.id.iv_music_pre)
    ImageView ivMusicPre;
    @Bind(R.id.iv_music_play)
    ImageView ivMusicPlay;
    @Bind(R.id.iv_music_next)
    ImageView ivMusicNext;
    private AnimationDrawable drawable;

    @Override
    public int getlayoutId() {
        return R.layout.activity_music_player;
    }

    @Override
    protected void setListener() {
        super.setListener();
        sbMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //告诉Service暂时不要发消息了
                EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_Stop_Update_Progress
                        , null));
            }

            //手指抬起，停止拖拽
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_SeekBar_Change
                        , sbMusic.getProgress()));
            }
        });
    }

    @Override
    protected void setdata() {
        EventBus.getDefault().register(this);
        //执行帧动画
        drawable = (AnimationDrawable) ivAnim.getBackground();
        drawable.start();
        //将list和index数据传递给service
        Intent intent = getIntent();
        intent.setClassName(this, MusicPlayService.class.getName());
        startService(intent);

    }
    MediaItem mediaItem = null;

    //设置在主线程执行onEvent方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MusicEvent event) {
        switch (event.what) {
            case MusicEvent.EVENT_SHOW_MUSIC_INFO://显示歌曲信息
                mediaItem = (MediaItem) event.obj;
                showMusicInfo(mediaItem);
                break;
            case MusicEvent.EVENT_UPDATE_MUSIC_PROGRESS://更新歌曲播放进度
                updateMusicProgress((Integer) event.obj);
                break;
            case MusicEvent.EVENT_MUSIC_STATE_CHANGE://歌曲播放状态改变的事件
                boolean isPlaying = (boolean) event.obj;
                updatePlayBtnBg(isPlaying);
                break;
            case MusicEvent.EVENT_UPDATE_PLAYMODE_BG://更新播放模式的事件
                int playMode = (int) event.obj;
                updatePlayModeBg(playMode);
                break;
        }
    }
    //更新播放模式的背景图片
    private void updatePlayModeBg(int playMode) {
        switch (playMode) {
            case MusicPlayService.MODE_ORDER:
                ivPlaymode.setBackgroundResource(R.drawable.selector_btn_playmode_order);
                break;
            case MusicPlayService.MODE_RANDOM:
                ivPlaymode.setBackgroundResource(R.drawable.selector_btn_playmode_random);
                break;
            case MusicPlayService.MODE_REPEAT:
                ivPlaymode.setBackgroundResource(R.drawable.selector_btn_playmode_single);
                break;
        }
    }

    //更新播放按钮的背景图片
    private void updatePlayBtnBg(boolean isPlaying) {
        ivMusicPlay.setBackgroundResource(isPlaying ? R.drawable.selector_btn_audio_pause
                : R.drawable.selector_btn_audio_play);
    }

    //显示音乐信息的方法
    private void showMusicInfo(MediaItem mediaItem) {
        tvTitle.setText(StringUtils.formatMedia(mediaItem.name));
        tvMusicTime.setText("00:00/" + DateUtil.formateDuration(mediaItem.duration));
        ivMusicPlay.setBackgroundResource(R.drawable.selector_btn_audio_pause);
        sbMusic.setMax((int) mediaItem.duration);
        sbMusic.setProgress(0);
    }
    //更新音乐的播放进度
    private void updateMusicProgress(int progress) {
        String total = DateUtil.formateDuration(mediaItem.duration);
        tvMusicTime.setText(DateUtil.formateDuration(progress) + "/" + total);
        sbMusic.setProgress(progress);
    }
    @OnClick({R.id.iv_back, R.id.iv_playmode, R.id.iv_music_pre, R.id.iv_music_play, R.id.iv_music_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_playmode:
                EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_TOOTLE_PLAYMODE, null));
                break;
            case R.id.iv_music_pre:
                EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_Play_Pre, null));
                break;
            case R.id.iv_music_play:
                EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_TOOGLE_MUSIC_PLAY, null));
                break;
            case R.id.iv_music_next:
                EventBus.getDefault().post(new MusicEvent(MusicEvent.EVENT_Play_Next, null));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消事件的订阅
        EventBus.getDefault().unregister(this);
    }



}
