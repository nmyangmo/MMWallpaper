package com.yangmo.mmwallpaper.adapter;

/**
 * Created by dzkj on 2018/1/18.
 */

import java.util.List;

import android.support.v4.app.Fragment;   //注意打包是用support.v4的
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yangmo.mmwallpaper.fragment.ImageFlowFragment;

public class FragmentViewPagerAdapter extends FragmentPagerAdapter {

    private FragmentManager fragmetnmanager;  //创建FragmentManager
    private int num;

    public FragmentViewPagerAdapter(FragmentManager fm,int num) {
        super(fm);
        this.fragmetnmanager=fm;
        this.num=num;
    }

    @Override
    public Fragment getItem(int arg0) {
        // TODO Auto-generated method stub
        return ImageFlowFragment.newInstance("","");
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return num; //总共有多少个fragment
    }
}
