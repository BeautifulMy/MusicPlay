package com.myproject.mobileplayer.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    private int layoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getlayoutId());
        ButterKnife.bind(this);
        setListener();
        setdata();

    }

    public abstract int getlayoutId() ;

    protected  void setListener(){}

    protected abstract void setdata();
}
