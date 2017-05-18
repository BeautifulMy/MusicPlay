package com.myproject.mobileplayer.ui.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.ToxicBakery.viewpager.transforms.CubeInTransformer;
import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.myproject.mobileplayer.R;
import com.myproject.mobileplayer.adapter.MainPagerAdapter;
import com.myproject.mobileplayer.ui.fragment.FragmentFactory;
import com.myproject.mobileplayer.ui.fragment.MediaListFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/3/8.
 */

public class MainActivity extends BaseActivity {
    @Bind(R.id.tablayout)
    TabLayout tablayout;
    @Bind(R.id.viewpager)
    ViewPager viewpager;

    @Override
    public int getlayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void setdata() {
        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mainPagerAdapter.addFragment(FragmentFactory.create(getbundle(MediaListFragment.TYPE_VIDEO)));
        mainPagerAdapter.addFragment(FragmentFactory.create(getbundle(MediaListFragment.TYPE_MUSIC)));
        viewpager.setPageTransformer(true, new CubeOutTransformer());
        viewpager.setAdapter(mainPagerAdapter);
        tablayout.setupWithViewPager(viewpager);

//        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
//        adapter.addFragment(FragmentFactory.create(getbundle(MediaListFragment.TYPE_VIDEO)));
//        adapter.addFragment(FragmentFactory.create(getbundle(MediaListFragment.TYPE_MUSIC)));
//        viewpager.setPageTransformer(true, new CubeOutTransformer());
//        viewpager.setAdapter(adapter);
//        //设置ViewPager页面滑动特效
//
//        //2.绑定TabLayout和ViewPager
//        tablayout.setupWithViewPager(viewpager);
    }


    public Bundle getbundle(String type) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        return bundle;
    }
}
