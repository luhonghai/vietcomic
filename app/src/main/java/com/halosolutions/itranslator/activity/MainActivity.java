package com.halosolutions.itranslator.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.halosolutions.itranslator.BuildConfig;
import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.utilities.FilePath;
import com.rey.material.app.ToolbarManager;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.SnackBar;

import java.lang.reflect.Field;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends ActionBarActivity implements ToolbarManager.OnToolbarGroupChangedListener{

    private DrawerLayout dl_navigator;
    private FrameLayout fl_drawer;
    private ListView lv_drawer;
    private CustomViewPager vp;

    private DrawerAdapter mDrawerAdapter;
    private PagerAdapter mPagerAdapter;

    private Toolbar mToolbar;
    private ToolbarManager mToolbarManager;
    private SnackBar mSnackBar;

    private Tab[] mItems = new Tab[]{Tab.LANGUAGE, Tab.HISTORY};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        dl_navigator = (DrawerLayout)findViewById(R.id.main_dl);
        fl_drawer = (FrameLayout)findViewById(R.id.main_fl_drawer);
        lv_drawer = (ListView)findViewById(R.id.main_lv_drawer);
        mToolbar = (Toolbar)findViewById(R.id.main_toolbar);
        vp = (CustomViewPager)findViewById(R.id.main_vp);
        mSnackBar = (SnackBar)findViewById(R.id.main_sn);

        mToolbarManager = new ToolbarManager(this, mToolbar, 0, R.style.ToolbarRippleStyle, R.anim.abc_fade_in, R.anim.abc_fade_out);
        mToolbarManager.setNavigationManager(new ToolbarManager.BaseNavigationManager(R.style.NavigationDrawerDrawable, this, mToolbar, dl_navigator) {
            @Override
            public void onNavigationClick() {
                if(mToolbarManager.getCurrentGroup() != 0)
                    mToolbarManager.setCurrentGroup(0);
                else
                    dl_navigator.openDrawer(Gravity.START);
            }

            @Override
            public boolean isBackState() {
                return super.isBackState() || mToolbarManager.getCurrentGroup() != 0;
            }

            @Override
            protected boolean shouldSyncDrawerSlidingProgress() {
                return super.shouldSyncDrawerSlidingProgress() && mToolbarManager.getCurrentGroup() == 0;
            }

        });
        mToolbarManager.registerOnToolbarGroupChangedListener(this);

        mDrawerAdapter = new DrawerAdapter();
        lv_drawer.setAdapter(mDrawerAdapter);

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems);
        vp.setAdapter(mPagerAdapter);

        mDrawerAdapter.setSelected(Tab.LANGUAGE);
        vp.setCurrentItem(0);
        FilePath filePath = new FilePath(this);
        filePath.checkUsage();
        filePath.initLanguage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbarManager.createMenu(R.menu.menu_main);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mToolbarManager.onPrepareMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.tb_setting:
                Intent i = new Intent(this, PrefActivity.class);
                startActivity(i);
                break;
            case R.id.menu_feedback:
                Intent activity = new Intent();
                activity.setClass(this, FeedbackActivity.class);
                this.startActivity(activity);
                break;
        }
        return true;
    }

    @Override
    public void onToolbarGroupChanged(int oldGroupId, int groupId) {
        mToolbarManager.notifyNavigationStateChanged();
    }

    public SnackBar getSnackBar(){
        return mSnackBar;
    }

    public CustomViewPager getViewPager(){return vp;}

    public DrawerAdapter getDrawerAdapter(){return mDrawerAdapter;}

    public enum Tab {
        LANGUAGE ("Home Page"),
        HISTORY ("History");
        private final String name;

        private Tab(String s) {
            name = s;
        }

        public boolean equalsName(String otherName){
            return (otherName != null) && name.equals(otherName);
        }

        public String toString(){
            return name;
        }

    }

    class DrawerAdapter extends BaseAdapter implements View.OnClickListener {

        private Tab mSelectedTab;

        public void setSelected(Tab tab){
            if(tab != mSelectedTab){
                mSelectedTab = tab;
                notifyDataSetInvalidated();
            }
        }

        public Tab getSelectedTab(){
            return mSelectedTab;
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null) {
                v = LayoutInflater.from(MainActivity.this).inflate(R.layout.row_drawer, null);
                v.setOnClickListener(this);
            }

            v.setTag(position);
            Tab tab = (Tab)getItem(position);
            ((TextView)v).setText(tab.toString());

            if(tab == mSelectedTab) {
                v.setBackgroundColor(ThemeUtil.colorPrimary(MainActivity.this, 0));
                ((TextView)v).setTextColor(0xFFFFFFFF);
            }
            else {
                v.setBackgroundResource(0);
                ((TextView)v).setTextColor(0xFFFFFFFF);
            }

            return v;
        }

        @Override
        public void onClick(View v) {
            int position = (Integer)v.getTag();
            vp.setCurrentItem(position);
            dl_navigator.closeDrawer(fl_drawer);
            mDrawerAdapter.setSelected(mItems[position]);
        }
    }

    private static class PagerAdapter extends FragmentStatePagerAdapter {

        Fragment[] mFragments;
        Tab[] mTabs;

        private static final Field sActiveField;

        static {
            Field f = null;
            try {
                Class<?> c = Class.forName("android.support.v4.app.FragmentManagerImpl");
                f = c.getDeclaredField("mActive");
                f.setAccessible(true);
            } catch (Exception e) {}

            sActiveField = f;
        }

        public PagerAdapter(FragmentManager fm, Tab[] tabs) {
            super(fm);
            mTabs = tabs;
            mFragments = new Fragment[mTabs.length];


            //dirty way to get reference of cached fragment
            try{
                ArrayList<Fragment> mActive = (ArrayList<Fragment>)sActiveField.get(fm);
                if(mActive != null){
                    for(Fragment fragment : mActive){
                        if(fragment instanceof LanguageFragment)
                            setFragment(Tab.LANGUAGE, fragment);
                        else if(fragment instanceof HistoryFragment)
                            setFragment(Tab.HISTORY, fragment);
                    }
                }
            }
            catch(Exception e){}
        }

        private void setFragment(Tab tab, Fragment f){
            for(int i = 0; i < mTabs.length; i++)
                if(mTabs[i] == tab){
                    mFragments[i] = f;
                    break;
                }
        }

        @Override
        public Fragment getItem(int position) {
            if(mFragments[position] == null){
                switch (mTabs[position]) {
                    case LANGUAGE:
                        mFragments[position] = LanguageFragment.newInstance();
                        break;
                    case HISTORY:
                        mFragments[position] = HistoryFragment.newInstance();
                        break;
                }
            }

            return mFragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs[position].toString().toUpperCase();
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    }

    @Override
    public void onBackPressed() {
        if (BuildConfig.IS_FREE) {
            SweetAlertDialog d = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
            d.setCustomImage(R.drawable.pro_stamp);
            d.setTitleText("Switch to PRO version?");
            d.setContentText("Full supported languages, no ads and more ...");
            d.setConfirmText("Go PRO");
            d.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    String appPro = "com.halosolutions.itranslator";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPro)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPro)));
                    }
                }
            });
            d.setCancelText("Just close");
            d.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    MainActivity.this.finish();
                }
            });
            d.show();
            return;
        } else {
            super.onBackPressed();
        }
    }
}
