package com.baibuti.biji.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.baibuti.biji.ui.adapter.AuthPageAdapter;
import com.baibuti.biji.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthActivity extends AppCompatActivity {

    @BindView(R.id.authAct_view_pager)
    ViewPager m_viewPager;

    @BindView(R.id.authAct_layout_tab)
    TabLayout m_tabs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupTabLayout();
        setTitle("登录笔迹");
    }

    private void setupTabLayout() {
        m_viewPager.setAdapter(new AuthPageAdapter(this, getSupportFragmentManager()));
        m_viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) { // 登录 / 注册 笔记
                setTitle(String.format(Locale.CHINA, "%s笔迹", getString(AuthPageAdapter.TAB_TITLES[position])));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        m_tabs.setupWithViewPager(m_viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
        }
        return true;
    }

    /**
     * 转到登录 Frag, 两个 Frag 用
     */
    public void openLogin() {
        m_viewPager.setCurrentItem(0);
    }

    /**
     * 转到注册 Frag, 两个 Frag 用
     */
    public void openRegister() {
        m_viewPager.setCurrentItem(1);
    }
}
