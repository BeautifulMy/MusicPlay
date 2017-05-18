package com.myproject.mobileplayer.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.myproject.mobileplayer.R;
import com.myproject.mobileplayer.bean.MediaItem;
import com.myproject.mobileplayer.utils.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.VideoView;

import static com.myproject.mobileplayer.R.id.fl_loading;

/**
 * Created by Administrator on 2017/3/8.
 */

public class VideoPlayActivity extends BaseActivity {

    @Bind(R.id.videoView)
    VideoView videoView;
    @Bind(R.id.fl_overlay)
    FrameLayout flOverlay;
    @Bind(fl_loading)
    LinearLayout flLoading;
    @Bind(R.id.tv_name)
    TextView tvName;
    @Bind(R.id.iv_battery)
    ImageView ivBattery;
    @Bind(R.id.tv_system_time)
    TextView tvSystemTime;
    @Bind(R.id.btn_voice)
    ImageView btnVoice;
    @Bind(R.id.sb_voice)
    SeekBar sbVoice;
    @Bind(R.id.ll_top)
    LinearLayout llTop;
    @Bind(R.id.tv_current_position)
    TextView tvCurrentPosition;
    @Bind(R.id.sb_progress)
    SeekBar sbProgress;
    @Bind(R.id.tv_total_position)
    TextView tvTotalPosition;
    @Bind(R.id.iv_exit)
    ImageView ivExit;
    @Bind(R.id.iv_play_pre)
    ImageView ivPlayPre;
    @Bind(R.id.iv_play_pause)
    ImageView ivPlayPause;
    @Bind(R.id.iv_play_next)
    ImageView ivPlayNext;
    @Bind(R.id.iv_fullscreen)
    ImageView ivFullscreen;
    @Bind(R.id.ll_bottom)
    LinearLayout llBottom;

    private ArrayList<MediaItem> list;
    private int position;
    private final int MSG_UPDATE_SYSTEM_TIME = 1;//更新系统时间
    private final int MSG_UPDATE_VIDEO = 2;//更新视频播放进度
    private final int MSG_HIDE_TOP_BOTTOM = 3;//延时消失面板
    private final int Msg_vol = 4;
    private boolean isTopButtomShow = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_SYSTEM_TIME:
                    showSystemTime();
                    break;
                case Msg_vol:
                    showSystemVolume();
                    break;
                case MSG_UPDATE_VIDEO:
                    showVideoProgress();
                    break;
                case MSG_HIDE_TOP_BOTTOM:
                    hideTopAndButtom();
                    break;
            }
        }
    };
    private AudioManager audioManager;
    private float touchX;
    private float touchY;
    private int maxVolume;


    @Override
    public int getlayoutId() {
        return R.layout.activity_video_player;
    }

    @Override
    protected void setListener() {
        super.setListener();
        //声音进度的监听
        sbVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //视频进度的监听
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(MSG_UPDATE_VIDEO);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessage(MSG_UPDATE_VIDEO);
                videoView.seekTo(sbProgress.getProgress());
            }
        });
        //视频播放完成的监听
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                handler.removeMessages(MSG_UPDATE_VIDEO);
                ivPlayPause.setBackgroundResource(R.drawable.selector_btn_play);
                sbProgress.setProgress(0);
                tvCurrentPosition.setText("00:00");
            }
        });
        //设置播放错误的监听
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_IO://文件读取的错误
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT://网络文件读取超时的错误
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN://未知错误
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED://不支持格式错误
                        //我们暂时提示一个对话框
                        AlertDialog dialog = new AlertDialog.Builder(VideoPlayActivity.this)
                                .setTitle("温馨提示")
                                .setCancelable(false)
                                .setMessage("视频文件错误，无法播放!点击确定退出播放。")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).create();
                        dialog.show();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void setdata() {
        //显示系统时间
        showSystemTime();
        //注册系统点亮变化的广播接受者
        registerBatteryReceiver();
        //显示系统音量
        showSystemVolume();

        boolean initialized = Vitamio.isInitialized(VideoPlayActivity.this);
        if (!initialized) {
            return;
        }

        llTop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                llTop.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                llTop.setTranslationY(-llTop.getHeight());
                llBottom.setTranslationY(llBottom.getHeight());
            }
        });
        Uri uri = getIntent().getData();
        if (uri!=null){
            videoView.setVideoURI(uri);
            tvName.setText(uri.getPath());
            ivPlayNext.setEnabled(false);
            ivPlayPre.setEnabled(false);
            flLoading.setVisibility(View.VISIBLE);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //隐藏加载中
                    hideLoadingLayout();

                    //当视频准备完成，再进行播放
                    videoView.start();
                    //将图片换成暂停的图标
                    ivPlayPause.setBackgroundResource(R.drawable.selector_btn_pause);
                    //显示当前的进度和总进度
                    tvTotalPosition.setText(DateUtil.formateDuration(videoView.getDuration()));
                    sbProgress.setMax((int) videoView.getDuration());

                    showVideoProgress();
                }
            });
        }else{
            list = (ArrayList<MediaItem>) getIntent().getSerializableExtra("video");
            position = (int) getIntent().getSerializableExtra("position");
            palyVideo();
        }

    }

    private void hideLoadingLayout() {
        ViewCompat.animate(flLoading).alpha(0).setDuration(1200).start();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                flLoading.setVisibility(View.GONE);
            }
        },801);
    }

    private void showSystemVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        sbVoice.setMax(maxVolume);
        sbVoice.setProgress(streamVolume);
        handler.sendEmptyMessage(Msg_vol);

    }

    private void registerBatteryReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(receiver, intentFilter);
    }

    /**
     * 显示系统电量
     */
    private void showSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = simpleDateFormat.format(new Date());
        tvSystemTime.setText(time);
        handler.sendEmptyMessageDelayed(MSG_UPDATE_SYSTEM_TIME, 1000);

    }

    /**
     *
     */
    private void palyVideo() {
        ivPlayPre.setEnabled(position != 0);
        ivPlayNext.setEnabled(position != (list.size() - 1));
        MediaItem item = list.get(position);
        videoView.setVideoPath(item.path);
        tvName.setText(com.myproject.mobileplayer.utils.StringUtils.formatMedia(item.name));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                ivPlayPause.setBackgroundResource(R.drawable.selector_btn_pause);
                //显示当前的进度和总进度
                tvTotalPosition.setText(DateUtil.formateDuration(videoView.getDuration()));
                sbProgress.setMax((int) videoView.getDuration());

                showVideoProgress();
            }
        });

        //滑动事件
        flOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchX = event.getX();
                        touchY = event.getY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        float moveX = event.getX();
                        float moveY = event.getY();
                        float dx = moveX - touchX;
                        float dy = moveY - touchY;
                        if (Math.abs(dx) < Math.abs(dy)) {
                            if (moveX > flOverlay.getWidth() / 2) {
                                touchUpdateVolume(dy);
                            } else {
                                touchUpdateLight(dy);

                            }
                        }
                        touchX = moveX;
                        touchX = moveY;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;

            }
        });

    }

    private void showVideoProgress() {
        tvCurrentPosition.setText(DateUtil.formateDuration(videoView.getCurrentPosition()));
        sbProgress.setProgress((int) videoView.getCurrentPosition()
        );
        handler.sendEmptyMessageDelayed(MSG_UPDATE_VIDEO, 200);

    }


    @OnClick({R.id.fl_overlay, R.id.btn_voice, R.id.iv_exit, R.id.iv_play_pre, R.id.iv_play_pause, R.id.iv_play_next, R.id.iv_fullscreen})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_voice://静音按钮
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                sbVoice.setProgress(0);
                break;
            case R.id.iv_exit://退出播放
                handler.removeCallbacksAndMessages(null);
                finish();
                break;
            case R.id.iv_play_pre://播放上一个
                if (position > 0) {
                    position--;
                    palyVideo();
                }
                break;
            case R.id.iv_play_pause://播放按钮
                if (videoView.isPlaying()) {
                    handler.removeMessages(MSG_UPDATE_VIDEO);
                    videoView.pause();
                    ivPlayPause.setBackgroundResource(R.drawable.selector_btn_play);
                } else {
                    handler.sendEmptyMessage(MSG_UPDATE_VIDEO);
                    videoView.start();
                    //将图片换成暂停的图标
                    ivPlayPause.setBackgroundResource(R.drawable.selector_btn_pause);
                }
                break;
            case R.id.iv_play_next:
                if (position != (list.size() - 1)) {
                    position++;
                    palyVideo();
                }
                break;
            case R.id.fl_overlay://点击显示和隐藏按钮
                if (isTopButtomShow) {
                    hideTopAndButtom();

                } else {
                    showTopAndButtom();

                }
                isTopButtomShow = !isTopButtomShow;
                break;
            case R.id.iv_fullscreen:
                break;
        }
    }

    private void showTopAndButtom() {
//先移除之前的msg
        handler.removeMessages(MSG_HIDE_TOP_BOTTOM);
        ViewCompat.animate(llTop).translationY(0).setDuration(400).start();
        ViewCompat.animate(llBottom).translationY(0).setDuration(400).start();

        //发送延时消息，过一会去消失掉
        handler.sendEmptyMessageDelayed(MSG_HIDE_TOP_BOTTOM, 4000);
    }

    private void hideTopAndButtom() {
        ViewCompat.animate(llTop).translationY(-llTop.getHeight()).setDuration(400).start();
        ViewCompat.animate(llBottom).translationY(llBottom.getHeight()).setDuration(400).start();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            if (level == 0) {
                ivBattery.setImageResource(R.mipmap.ic_battery_0);
            } else if (level <= 10 && level > 0) {
                ivBattery.setImageResource(R.mipmap.ic_battery_10);
            } else if (level <= 20 && level > 10) {
                ivBattery.setImageResource(R.mipmap.ic_battery_20);
            } else if (level <= 40 && level > 20) {
                ivBattery.setImageResource(R.mipmap.ic_battery_40);
            } else if (level <= 60 && level > 40) {
                ivBattery.setImageResource(R.mipmap.ic_battery_60);
            } else if (level <= 80 && level > 60) {
                ivBattery.setImageResource(R.mipmap.ic_battery_80);
            } else {
                ivBattery.setImageResource(R.mipmap.ic_battery_100);
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                touchX = event.getX();
//                touchY = event.getY();
//
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float moveX = event.getX();
//                float moveY = event.getY();
//                float dx = moveX - touchX;
//                float dy = moveY - touchY;
//                if (Math.abs(dx) < Math.abs(dy)) {
//                    if (moveX > flOverlay.getWidth() / 2) {
//                        touchUpdateVolume(dy);
//                    } else {
//                        touchUpdateLight(dy);
//
//                    }
//                }
//                touchX = moveX;
//                touchX = moveY;
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
        return true;
    }

    private void touchUpdateLight(float dy) {
        float alpha = flOverlay.getAlpha();
        if (dy > 0) {
            alpha += dy * 0.01;
            if (alpha > 0.8f) {
                alpha = 0.8f;
            }
            flOverlay.setAlpha(alpha);
        }else{
            alpha += dy * 0.01;
            if (alpha < 0) {
                alpha = 0;
            }
            flOverlay.setAlpha(alpha);
        }
    }

    private void touchUpdateVolume(float dy) {
        if (Math.abs(dy)>5){
            int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (dy>0){
                volume-=1;
                volume=Math.max(0,volume);
            }else{
                volume+=1;
                volume=Math.min(maxVolume,volume);
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            sbVoice.setProgress(volume);
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //如果当前点击的键是音量的上下键
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            //重新获取当前的音量，设置给Seekbar
            int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            sbVoice.setProgress(volume);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        handler.removeCallbacksAndMessages(null);
    }
}
