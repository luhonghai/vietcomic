package com.halosolutions.vietcomic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmg.android.cmgpdf.AsyncTask;
import com.google.gson.Gson;
import com.halosolutions.vietcomic.comic.ComicBook;
import com.halosolutions.vietcomic.fragment.AllComicFragment;
import com.halosolutions.vietcomic.fragment.FavoriteComicFragment;
import com.halosolutions.vietcomic.fragment.HotComicFragment;
import com.halosolutions.vietcomic.fragment.NewComicFragment;
import com.halosolutions.vietcomic.service.BroadcastHelper;
import com.halosolutions.vietcomic.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.vietcomic.util.SimpleAppLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rey.material.app.ThemeManager;
import com.rey.material.app.ToolbarManager;
import com.rey.material.drawable.NavigationDrawerDrawable;
import com.rey.material.drawable.ThemeDrawable;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.ProgressView;
import com.rey.material.widget.TabPageIndicator;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by cmg on 18/08/15.
 */
public class DetailActivity extends BaseActivity implements ToolbarManager.OnToolbarGroupChangedListener{

    private CustomViewPager vp;
    private TabPageIndicator tpi;
    private PagerAdapter mPagerAdapter;

    private Toolbar mToolbar;
    private ToolbarManager mToolbarManager;

    private ComicBookDBAdapter dbAdapter;

    private Tab[] mItems = new Tab[]{Tab.HOT,Tab.NEW,Tab.FAVORITE, Tab.ALL };

    private ComicBook selectedBook;

    private BroadcastHelper broadcastHelper;

    private DisplayImageOptions displayImageOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        mToolbar = (Toolbar)findViewById(R.id.main_toolbar);

        vp = (CustomViewPager)findViewById(R.id.main_vp);
        tpi = (TabPageIndicator)findViewById(R.id.main_tpi);

        mToolbarManager = new ToolbarManager(getDelegate(), mToolbar, R.id.tb_group_main, R.style.ToolbarRippleStyle, R.anim.abc_fade_in, R.anim.abc_fade_out);
        mToolbarManager.registerOnToolbarGroupChangedListener(this);

        if (vp != null) {
            mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems);
            vp.setAdapter(mPagerAdapter);
            tpi.setViewPager(vp);
            tpi.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            });
            vp.setCurrentItem(0);
        }
        ViewUtil.setBackground(getWindow().getDecorView(), new ThemeDrawable(R.array.bg_window));
        ViewUtil.setBackground(mToolbar, new ThemeDrawable(R.array.bg_toolbar));
        dbAdapter = new ComicBookDBAdapter(this);
        setSupportActionBar(mToolbar);
        final NavigationDrawerDrawable drawable =
                (new com.rey.material.drawable.NavigationDrawerDrawable.Builder(this.mToolbar.getContext(),
                        ThemeManager.getInstance().getCurrentStyle(R.array.navigation_drawer)))
                        .build();
        mToolbar.setNavigationIcon(drawable);
        drawable.switchIconState(NavigationDrawerDrawable.STATE_ARROW, false);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Gson gson = new Gson();
            selectedBook = gson.fromJson(bundle.getString(ComicBook.class.getName()), ComicBook.class);
        } else {
            selectedBook = new ComicBook();
        }
        try {
            dbAdapter.open();
        } catch (Exception e) {
            SimpleAppLog.error("Could not open data base",e);
        }
        broadcastHelper = new BroadcastHelper(this);

        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.comic_thumbnail_default) // resource or drawable
                .showImageForEmptyUri(R.drawable.comic_thumbnail_default) // resource or drawable
                .showImageOnFail(R.drawable.comic_thumbnail_error) // resource or drawable
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        loadComicInfo();

        broadcastHelper.registerOnComicBookUpdated(new BroadcastHelper.OnComicBookUpdated() {
            @Override
            public void onUpdated(ComicBook comicBook) {
                if (selectedBook != null && comicBook.getBookId().equals(selectedBook.getBookId())) {
                    selectedBook = comicBook;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadComicInfo();
                        }
                    });
                }
            }
        });
    }

    private void loadComicInfo() {
        if (selectedBook != null) {
            ((TextView) findViewById(R.id.toolbar_title)).setText(selectedBook.getName());
            RelativeLayout rlComicRate = (RelativeLayout) findViewById(R.id.rlComicRate);
            if (rlComicRate != null) {
                float rate = selectedBook.getRate();
                if (rate > 0.0) {
                    String strRate;
                    if (rate == 10.0) {
                        strRate = "10";
                    } else {
                        strRate = new DecimalFormat("#.##").format(rate);
                    }
                    findViewById(R.id.rlComicRate).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.txtComicRate)).setText(strRate);
                } else {
                    findViewById(R.id.rlComicRate).setVisibility(View.GONE);
                }
            }
            final ImageView imgThumbnail = (ImageView) findViewById(R.id.thumbnail);
            String thumbnail = selectedBook.getThumbnail();
            ImageLoader.getInstance().displayImage(thumbnail,
                    imgThumbnail,
                    displayImageOptions);
            ((TextView) findViewById(R.id.txtComicAuthor)).setText(getString(R.string.comic_author,
                    selectedBook.getAuthor()));
            ((TextView) findViewById(R.id.txtComicSource)).setText(getString(R.string.comic_source,
                    selectedBook.getSource()));
            ((TextView) findViewById(R.id.txtComicStatus)).setText(getString(R.string.comic_status,
                    selectedBook.getStatus()));
            ((TextView) findViewById(R.id.txtComicCategories)).setText(getString(R.string.comic_categories,
                    StringUtils.join(selectedBook.getCategories(), ", ")));
            ((ProgressView) findViewById(R.id.progressDescription)).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbarManager.createMenu(R.menu.menu_detail);
        updateFavoriteMenuItem(mToolbar.getMenu().findItem(R.id.action_favorite));
        return true;
    }

    private void updateFavoriteMenuItem(MenuItem favorite) {
        if (selectedBook != null) {
            if (selectedBook.isFavorite()) {
                favorite.setIcon(getResources().getDrawable(R.drawable.app_icon_menu_love_red));
            } else {
                favorite.setIcon(getResources().getDrawable(R.drawable.app_icon_menu_love_white));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbAdapter != null) {
            dbAdapter.close();
        }
        if (broadcastHelper != null)
            broadcastHelper.unregister();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mToolbarManager.onPrepareMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SimpleAppLog.debug("onOptionsItemSelected " + item.getItemId());
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_favorite:
                if (selectedBook != null) {
                    selectedBook.setIsFavorite(!selectedBook.isFavorite());
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                if (selectedBook != null && dbAdapter != null)
                                    if (dbAdapter.update(selectedBook)) {
                                        broadcastHelper.sendComicUpdate(selectedBook);
                                    }
                            } catch (Exception e) {
                                SimpleAppLog.error("Could not update comic book", e);
                            }
                            return null;
                        }
                    }.execute();
                }
                updateFavoriteMenuItem(item);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onToolbarGroupChanged(int oldGroupId, int groupId) {
        mToolbarManager.notifyNavigationStateChanged();
    }

    public enum Tab {
        HOT("Truyện HOT"),
        NEW("Truyện mới"),
        ALL("Toàn bộ"),
        FAVORITE("Yêu thích");
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
                        if(fragment instanceof HotComicFragment)
                            setFragment(Tab.HOT, fragment);
                        else if(fragment instanceof AllComicFragment)
                            setFragment(Tab.ALL, fragment);
                        else if(fragment instanceof FavoriteComicFragment)
                            setFragment(Tab.FAVORITE, fragment);
                        else if(fragment instanceof NewComicFragment)
                            setFragment(Tab.NEW, fragment);
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
                    case HOT:
                        mFragments[position] = new HotComicFragment();
                        break;
                    case ALL:
                        mFragments[position] = new AllComicFragment();
                        break;
                    case FAVORITE:
                        mFragments[position] = new FavoriteComicFragment();
                        break;
                    case NEW:
                        mFragments[position] = new NewComicFragment();
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
}
