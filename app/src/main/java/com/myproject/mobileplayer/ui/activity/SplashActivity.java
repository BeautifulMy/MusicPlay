package com.myproject.mobileplayer.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.myproject.mobileplayer.R;

import butterknife.Bind;

public class SplashActivity extends BaseActivity {


    @Bind(R.id.iv_icon)
    ImageView ivIcon;
    @Bind(R.id.tv_tips)
    TextView tvTips;

    @Override
    protected void setdata() {
        //添加一个全局的布局监听器
        ivIcon.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ivIcon.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                execHorizontaAnim();
                enterMain();
            }
        });

    }

    private void enterMain() {
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                        finish();
                    }
                }, 2100);

    }

    private void execHorizontaAnim() {
        int windowWidth = getWindowManager().getDefaultDisplay().getWidth();
        int translationX = windowWidth / 2 + tvTips.getWidth();
        ivIcon.setTranslationX(-translationX);
        tvTips.setTranslationX(translationX);
        ViewCompat.animate(ivIcon).translationX(0).setDuration(800).setInterpolator(new OvershootInterpolator()).start();
        ViewCompat.animate(tvTips).translationX(0).setDuration(800).setInterpolator(new OvershootInterpolator()).start();
    }

    @Override
    public int getlayoutId() {
        return R.layout.activity_splash;
    }


}
