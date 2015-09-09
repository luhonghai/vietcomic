package com.halosolutions.mangaworld;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmg.android.cmgpdf.AsyncTask;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.halosolutions.mangaworld.BuildConfig;
import com.halosolutions.mangaworld.R;
import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.fragment.detail.AllChapterComicFragment;
import com.halosolutions.mangaworld.fragment.detail.SameAuthorComicFragment;
import com.halosolutions.mangaworld.fragment.detail.SameCategoriesComicFragment;
import com.halosolutions.mangaworld.service.BroadcastHelper;
import com.halosolutions.mangaworld.service.ComicUpdateService;
import com.halosolutions.mangaworld.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.mangaworld.util.AndroidHelper;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rey.material.app.ThemeManager;
import com.rey.material.app.ToolbarManager;
import com.rey.material.drawable.NavigationDrawerDrawable;
import com.rey.material.drawable.ThemeDrawable;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.TabPageIndicator;

import org.apache.commons.lang3.StringUtils;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by cmg on 18/08/15.
 */
public class DetailActivity extends BaseActivity implements ToolbarManager.OnToolbarGroupChangedListener,
        View.OnClickListener {

    private CustomViewPager vp;
    private TabPageIndicator tpi;
    private PagerAdapter mPagerAdapter;

    private Toolbar mToolbar;
    private ToolbarManager mToolbarManager;

    private ComicBookDBAdapter dbAdapter;

    private Tab[] mItems = new Tab[]{Tab.ALL_CHAPTER,Tab.SAME_CATEGORIES,Tab.SAME_AUTHOR};

    private ComicBook selectedBook;

    private BroadcastHelper broadcastHelper;

    private DisplayImageOptions displayImageOptions;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Gson gson = new Gson();
            selectedBook = gson.fromJson(bundle.getString(ComicBook.class.getName()), ComicBook.class);
        } else {
            selectedBook = new ComicBook();
        }
        mToolbar = (Toolbar)findViewById(R.id.main_toolbar);

        vp = (CustomViewPager)findViewById(R.id.main_vp);
        tpi = (TabPageIndicator)findViewById(R.id.main_tpi);
        findViewById(R.id.imgExpandView).setOnClickListener(this);
        mToolbarManager = new ToolbarManager(getDelegate(), mToolbar, R.id.tb_group_main, R.style.ToolbarRippleStyle, R.anim.abc_fade_in, R.anim.abc_fade_out);
        mToolbarManager.registerOnToolbarGroupChangedListener(this);

        if (vp != null) {
            mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems, selectedBook);
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

        try {
            dbAdapter.open();
        } catch (Exception e) {
            SimpleAppLog.error("Could not open data base",e);
        }
        broadcastHelper = new BroadcastHelper(this);

        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.comic_thumbnail_default) // resource or drawable
                .showImageOnFail(R.drawable.comic_thumbnail_error) // resource or drawable
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        loadComicInfo();

        broadcastHelper.registerOnComicBookUpdated(new BroadcastHelper.OnComicBookUpdated() {
            @Override
            public void onUpdated(ComicBook comicBook) {
                if (selectedBook != null && comicBook != null
                        && comicBook.getBookId() != null
                        && comicBook.getBookId().length() > 0
                        && comicBook.getBookId().equals(selectedBook.getBookId())) {
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

        if (selectedBook != null) {
            Gson gson = new Gson();
            Intent updateBook = new Intent(this, ComicUpdateService.class);
            updateBook.putExtra(ComicBook.class.getName(), gson.toJson(selectedBook));
            startService(updateBook);
        }

        mAdView = (AdView) findViewById(R.id.adView);
        if (mAdView != null && BuildConfig.IS_FREE) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("7898660F3293A11BB56ED538658F9B0F")
                    .build();
            mAdView.loadAd(adRequest);
        }
    }

    private void loadComicInfo() {
        if (selectedBook != null) {
            TextView txtTitle = (TextView) findViewById(R.id.toolbar_title);
            if (txtTitle != null && txtTitle.getTag() == null) {
                txtTitle.setText(selectedBook.getName());
                txtTitle.setTag(new Object());
            }
            final ImageView imgThumbnail = (ImageView) findViewById(R.id.thumbnail);
            if (imgThumbnail != null && imgThumbnail.getTag() == null) {
                if (AndroidHelper.isLowerThanApiLevel11()) {
                    imgThumbnail.setBackgroundDrawable(null);
                }
                String thumbnail = selectedBook.getThumbnail();
                ImageLoader.getInstance().displayImage(thumbnail,
                        imgThumbnail,
                        displayImageOptions);
                imgThumbnail.setTag(new Object());
            }
            ((HtmlTextView) findViewById(R.id.txtComicOtherName)).setHtmlFromString(getString(R.string.comic_other_name,
                    selectedBook.getOtherName()), new HtmlTextView.RemoteImageGetter());
            ((HtmlTextView) findViewById(R.id.txtComicAuthor)).setHtmlFromString(getString(R.string.comic_author,
                    selectedBook.getAuthor()), new HtmlTextView.RemoteImageGetter());
            ((HtmlTextView) findViewById(R.id.txtComicSource)).setHtmlFromString(getString(R.string.comic_source,
                    selectedBook.getSource()), new HtmlTextView.RemoteImageGetter());
            ((HtmlTextView) findViewById(R.id.txtComicStatus)).setHtmlFromString(getString(R.string.comic_status,
                    selectedBook.getStatus()), new HtmlTextView.RemoteImageGetter());
            ((HtmlTextView) findViewById(R.id.txtComicCategories)).setHtmlFromString(getString(R.string.comic_categories,
                    StringUtils.join(selectedBook.getCategories(), ", "))
                    , new HtmlTextView.RemoteImageGetter());
            if (selectedBook.getDescription() != null && selectedBook.getDescription().length() > 0) {
                HtmlTextView txtDescription = ((HtmlTextView) findViewById(R.id.txtComicDescription));
//                if (txtDescription.getText().toString().length() == 0) {
//                    YoYo.with(Techniques.Tada).delay(300).duration(1500).playOn(findViewById(R.id.imgExpandView));
//                }
                txtDescription.setHtmlFromString(selectedBook.getDescription()
                                , new HtmlTextView.RemoteImageGetter());

            }
            LinearLayout llRateStar = (LinearLayout) findViewById(R.id.llRateStar);
            if (llRateStar != null && llRateStar.getTag() == null) {
                AndroidHelper.showRateStar(this, llRateStar, selectedBook.getRate());
                llRateStar.setTag(new Object());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null)
            mAdView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null)
            mAdView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbAdapter != null) {
            dbAdapter.close();
        }
        if (broadcastHelper != null)
            broadcastHelper.unregister();
        if (mAdView != null)
            mAdView.destroy();
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
                            updateSelectedBook();
                            return null;
                        }
                    }.execute();
                }
                updateFavoriteMenuItem(item);
                break;
            case R.id.menu_feedback:
                Intent intent = new Intent(this, FeedbackActivity.class);
                if (selectedBook != null) {
                    Gson gson = new Gson();
                    intent.putExtra(ComicBook.class.getName(), gson.toJson(selectedBook));
                }
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSelectedBook() {
        try {
            if (selectedBook != null && dbAdapter != null)
                if (dbAdapter.update(selectedBook)) {
                    broadcastHelper.sendComicUpdate(selectedBook);
                }
        } catch (Exception e) {
            SimpleAppLog.error("Could not update comic book", e);
        }
    }

    @Override
    public void onToolbarGroupChanged(int oldGroupId, int groupId) {
        mToolbarManager.notifyNavigationStateChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgExpandView:
                final ImageView img = (ImageView) findViewById(R.id.imgExpandView);
                img.setVisibility(View.INVISIBLE);
                img.setEnabled(false);
                final View rlBookInfo = findViewById(R.id.rlBookInfo);
                int visibility = rlBookInfo.getVisibility();
                if (visibility == View.VISIBLE) {
                    rlBookInfo.setTag(rlBookInfo.getHeight());
                    if (AndroidHelper.isLowerThanApiLevel12()) {
                        rlBookInfo.setVisibility(View.GONE);
                        img.setImageDrawable(getResources().getDrawable(R.drawable.app_icon_up_grey));
                        img.setEnabled(true);
                        img.setVisibility(View.VISIBLE);
                    } else {
                        rlBookInfo.animate()
                                .translationY(rlBookInfo.getHeight())
                                .alpha(0.0f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        rlBookInfo.setVisibility(View.GONE);
                                        img.setImageDrawable(getResources().getDrawable(R.drawable.app_icon_up_grey));
                                        img.setEnabled(true);
                                        img.setVisibility(View.VISIBLE);
                                    }
                                });
                    }

                } else if (visibility == View.GONE) {
                    if (AndroidHelper.isLowerThanApiLevel12()) {
                        rlBookInfo.setVisibility(View.VISIBLE);
                        img.setImageDrawable(getResources().getDrawable(R.drawable.app_icon_down_grey));
                        img.setVisibility(View.VISIBLE);
                        img.setEnabled(true);
                    } else {
                        rlBookInfo.animate()
                                .translationY(0)
                                .alpha(1.0f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        rlBookInfo.setVisibility(View.VISIBLE);
                                        rlBookInfo.setAlpha(0.0f);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        img.setImageDrawable(getResources().getDrawable(R.drawable.app_icon_down_grey));
                                        img.setVisibility(View.VISIBLE);
                                        img.setEnabled(true);
                                    }
                                });
                    }
                }
                break;
        }
    }

    public enum Tab {
        ALL_CHAPTER("Toàn bộ tập"),
        SAME_CATEGORIES("Cùng thể loại"),
        SAME_AUTHOR("Cùng tác giả");
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
        Gson gson;
        ComicBook comicBook;

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

        public PagerAdapter(FragmentManager fm, Tab[] tabs, ComicBook comicBook) {
            super(fm);
            gson = new Gson();
            mTabs = tabs;
            mFragments = new Fragment[mTabs.length];
            this.comicBook = comicBook;


            //dirty way to get reference of cached fragment
            try{
                ArrayList<Fragment> mActive = (ArrayList<Fragment>)sActiveField.get(fm);
                if(mActive != null){
                    for(Fragment fragment : mActive){
                        if(fragment instanceof SameAuthorComicFragment)
                            setFragment(Tab.SAME_AUTHOR, fragment);
                        else if(fragment instanceof SameCategoriesComicFragment)
                            setFragment(Tab.SAME_CATEGORIES, fragment);
                        else if(fragment instanceof AllChapterComicFragment)
                            setFragment(Tab.ALL_CHAPTER, fragment);
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
                Fragment fragment = null;
                switch (mTabs[position]) {
                    case SAME_AUTHOR:
                        fragment = new SameAuthorComicFragment();
                        break;
                    case SAME_CATEGORIES:
                        fragment = new SameCategoriesComicFragment();
                        break;
                    case ALL_CHAPTER:
                        fragment = new AllChapterComicFragment();
                        break;
                }
                Bundle bundle = new Bundle();
                bundle.putString(ComicBook.class.getName(), gson.toJson(comicBook));
                fragment.setArguments(bundle);
                mFragments[position] = fragment;
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
