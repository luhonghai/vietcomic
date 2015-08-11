/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.cmg.android.cmgpdf.CmgPDFActivity;
import com.cmg.android.common.Environment;
import com.cmg.android.pension.data.NewsletterHelper;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.plmobile.ContentFragmentActivity;
import com.cmg.android.plmobile.MainActivity;
import com.cmg.android.plmobile.R;
import com.cmg.android.preference.Preference;
import com.cmg.android.task.FavoritesTask;
import com.cmg.android.task.UpdateNewStatusAsync;
import com.cmg.android.util.ContentUtils;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.data.NewsletterCategory;


import java.util.List;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class NewsletterDetailActivity extends ContentFragmentActivity
        implements OnPageChangeListener {
    public static final String REFRESH_MAIN_ACTIVITY_MESSAGE = "com.cmg.android.pension.activity.NewsletterDetailActivity.REFRESH_MAIN_ACTIVITY_MESSAGE";
    public static final String NOTIFY_REFESH_MAIN_ACTIVITY = "NOTIFY_REFESH_MAIN_ACTIVITY";
   
    private ShareActionProvider actionProvider;
    private DatabaseHandler db;
    private int index = -1;
    private ViewPager viewPager;
    private NewsletterDetailFragmentAdapter detailFragmentAdapter;
    private List<Newsletter> newsletters;
    private Handler mHandler;


    private View mProgressContainer;
    private View mContentContainer;
    private View mEmptyView;
    private boolean mContentShown;
    private boolean mIsContentEmpty;

    private MenuItem actionFavorites;
    private boolean isFavorites = false;
    private Newsletter newsletter;
    private FavoritesTask favorTask;
    private Runnable changeFavarRunable;
    private Handler changeFavoritesIconHandler = new Handler();

    /**
     * run thread
     */
    private Runnable mShowContentRunnable = new Runnable() {
        @Override
        public void run() {
            init();
        }
    };

    private boolean needRefreshMainActivity = false;

    /**
     * create view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(mHandleMessageReceiver, new IntentFilter(
                REFRESH_MAIN_ACTIVITY_MESSAGE));
        registerReceiver(mHandleMessageReader, new IntentFilter(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY));
        setContentView(R.layout.newsletter_detail_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ensureContent();
        if (Preference.getInstance(this).getCategoryId() == NewsletterCategory.FAVORITES) {
            setTitle(NewsletterCategory.STR_FAVORITES);
        } else if (Preference.getInstance(this).getCategoryId() == NewsletterCategory.PENSIONER) {
            setTitle(NewsletterCategory.STR_PENSIONER);
        }

        setEmptyText(R.string.empty_newsletter);
        obtainData();
    }

    /**
     * get data to show on view
     */
    private void obtainData() {
        setContentShown(false);
        mHandler = new Handler();
        SimpleAppLog.info("post content runnable");
        mHandler.post(mShowContentRunnable);
    }

    /**
     * initial value
     */
    void init() {
        try {
            favorTask = new FavoritesTask(this.getApplicationContext());
            db = new DatabaseHandler(this);
            String newsletterId = getIntent().getStringExtra(Newsletter.NEWSLETTER_ID);//.getExtras().getString(
            //Newsletter.NEWSLETTER_ID);
            SimpleAppLog.info("NEWSLETTER ID - DETAIL: " + newsletterId);
            newsletter = db.getById(newsletterId);


            UpdateNewStatusAsync task = new UpdateNewStatusAsync(
                    this, newsletter);
            task.execute();

            isFavorites = newsletter.checkFavor();
            changeFavoritesIcon();
            int categoryId = Preference.getInstance(this.getApplicationContext()).getCategoryId();
            newsletters = db.getAllNewslettersByCategory(categoryId,
                    Preference.getInstance(this.getApplicationContext())
                            .getStrSearch());
            NewsletterHelper.sortNewsletter(newsletters, Preference
                    .getInstance(this.getApplicationContext()).getSortType(),
                    Preference.getInstance(this.getApplicationContext())
                            .isContrastOrder());
            if (newsletters != null && newsletters.size() > 0) {
                for (int i = 0; i < newsletters.size(); i++) {
                    if (newsletterId.equals(newsletters.get(i).getId())) {
                        index = i;
                        break;
                    }
                }
                SimpleAppLog.debug("Add apater to viewpager. Newsletter size: "
                        + newsletters.size() + ". Focus index: " + index);
                initShareMenu(index);
                detailFragmentAdapter = new NewsletterDetailFragmentAdapter(
                        getSupportFragmentManager(), newsletters);
                setContentShown(true);
                viewPager.setAdapter(detailFragmentAdapter);
                viewPager.setOnPageChangeListener(this);
                viewPager.setCurrentItem(index);
            } else {
                setContentShown(true);
                setContentEmpty(true);
            }
        } catch (Exception ex) {
            // Silent
        }
    }

    /**
     * The default content for a ProgressFragment has a TextView that can be
     * shown when the content is empty {@link #setContentEmpty(boolean)}. If you
     * would like to have it shown, call this method to supply the text it
     * should use.
     *
     * @param resId Identification of string from a resources
     * @see #setEmptyText(CharSequence)
     */
    public void setEmptyText(int resId) {
        setEmptyText(getString(resId));
    }

    /**
     * The default content for a ProgressFragment has a TextView that can be
     * shown when the content is empty {@link #setContentEmpty(boolean)}. If you
     * would like to have it shown, call this method to supply the text it
     * should use.
     *
     * @param text Text for empty view
     * @see #setEmptyText(int)
     */
    public void setEmptyText(CharSequence text) {
        ensureContent();
        if (mEmptyView != null) {
            if (mEmptyView instanceof TextView) {
                ((TextView) mEmptyView).setText(text);
            }
        } else {
            throw new IllegalStateException(
                    "Can't be used with a custom content view");
        }
    }

    /**
     * Control whether the content is being displayed. You can make it not
     * displayed if you are waiting for the initial data to show in it. During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown If true, the content view is shown; if false, the progress
     *              indicator. The initial value is true.
     * @see #setContentShownNoAnimation(boolean)
     */
    public void setContentShown(boolean shown) {
        setContentShown(shown, true);
    }

    /**
     * Like {@link #setContentShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     *
     * @param shown If true, the content view is shown; if false, the progress
     *              indicator. The initial value is true.
     * @see #setContentShown(boolean)
     */
    public void setContentShownNoAnimation(boolean shown) {
        setContentShown(shown, false);
    }

    /**
     * Control whether the content is being displayed. You can make it not
     * displayed if you are waiting for the initial data to show in it. During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown   If true, the content view is shown; if false, the progress
     *                indicator. The initial value is true.
     * @param animate If true, an animation will be used to transition to the new
     *                state.
     */
    private void setContentShown(boolean shown, boolean animate) {
        ensureContent();
        if (mContentShown == shown) {
            return;
        }
        mContentShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_out));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mContentContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_in));
                mContentContainer.startAnimation(AnimationUtils.loadAnimation(
                        this, android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mContentContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mContentContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Returns true if content is empty. The default content is not empty.
     *
     * @return true if content is null or empty
     * @see #setContentEmpty(boolean)
     */
    public boolean isContentEmpty() {
        return mIsContentEmpty;
    }

    /**
     * If the content is empty, then set true otherwise false. The default
     * content is not empty. You can't call this method if the content view has
     * not been initialized before {@link #setContentView(android.view.View)}
     * and content view not null.
     *
     * @param isEmpty true if content is empty else false
     * @see #isContentEmpty()
     */
    public void setContentEmpty(boolean isEmpty) {
        ensureContent();
        if (isEmpty) {
            mEmptyView.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
        }
        mIsContentEmpty = isEmpty;
    }

    /**
     * Initialization views.
     */
    private void ensureContent() {
        if (mContentContainer != null && mProgressContainer != null) {
            return;
        }
        mProgressContainer = findViewById(R.id.progress_container);
        if (mProgressContainer == null) {
            throw new RuntimeException(
                    "Your content must have a ViewGroup whose id attribute is 'R.id.progress_container'");
        }
        mContentContainer = findViewById(R.id.content_container);
        if (mContentContainer == null) {
            throw new RuntimeException(
                    "Your content must have a ViewGroup whose id attribute is 'R.id.content_container'");
        }
        mEmptyView = findViewById(android.R.id.empty);
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }

        if (viewPager == null) {
            viewPager = (ViewPager) findViewById(R.id.newsletter_detail_pager);
        }
        mContentShown = true;

    }

    /**
     * start main activity
     */
    void startMainActivity() {
        Intent upIntent = new Intent(this, MainActivity.class);

 //       if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.from(this).addNextIntent(upIntent)
                    .startActivities();
            finish();
//        } else {
//            NavUtils.navigateUpTo(this, upIntent);
//        }
    }

    /**
     * back button click
     */
    @Override
    public void onBackPressed() {
        if (needRefreshMainActivity) {
            startMainActivity();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                break;
            case R.id.action_favorites:
                isFavorites = !isFavorites;
                newsletter.setIsFavor(isFavorites ? Newsletter.IS_FAVOR : Newsletter.NOT_FAVOR);
                favorTask.execute(newsletter);
                changeFavoritesIcon();
                needRefreshMainActivity = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeFavoritesIcon() {
        if (actionFavorites != null) {
            if (changeFavarRunable != null) {
                changeFavoritesIconHandler.removeCallbacks(changeFavarRunable);
            }
            changeFavarRunable = new Runnable() {
                @Override
                public void run() {
                    if (isFavorites) {
                        actionFavorites.setIcon(R.drawable.ic_menu_star_on);
                    } else {
                        actionFavorites.setIcon(R.drawable.ic_menu_star);
                    }
                }
            };
            changeFavoritesIconHandler.post(changeFavarRunable);

        }
    }

    /**
     * create menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menu shareMenu;
        getSupportMenuInflater().inflate(R.menu.detail_activity_menu, menu);
        // shareMenu = menu;
        actionFavorites = menu.findItem(R.id.action_favorites);
        MenuItem actionItem = menu.findItem(R.id.action_share);
        actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        initShareMenu(index);
        changeFavoritesIcon();
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * initial Share Menu
     *
     * @param pos
     */
    public void initShareMenu(int pos) {
        if (pos != -1 && newsletters != null && newsletters.size() > 0) {
            try {
                actionProvider.setShareIntent(ContentUtils
                        .createShareIntent(newsletters.get(pos)));
            } catch (Exception e) {
                // Silent
            }
        }
    }

    /**
     * destroy view
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SimpleAppLog.debug("destroy newsletter detail activity");
        if (db != null) {
            db.recycle();
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mShowContentRunnable);
            mHandler = null;
        }
        mContentShown = false;
        mIsContentEmpty = false;
        mProgressContainer = null;
        mContentContainer = null;
        mEmptyView = null;
        viewPager = null;

        if (mHandleMessageReceiver != null) {
            unregisterReceiver(mHandleMessageReceiver);
        }
        if (mHandleMessageReader != null) {
            unregisterReceiver(mHandleMessageReader);
        }
        // ImageLoaderHelper.getImageLoader(this.getApplicationContext()).clearMemoryCache();
    }

    /**
     *
     */
    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
    }

    /**
     *
     */
    @Override
    public void onPageSelected(int position) {
        initShareMenu(position);
        // Intent intent = new Intent(DownloadAsync.DISPLAY_MESSAGE_ACTION +
        // "@"+ newsletters.get(position).getId());
        // intent.putExtra("START_ANIMATION",
        // newsletters.get(position).getId());
        newsletter = newsletters.get(position);
        isFavorites = newsletter.checkFavor();

        changeFavoritesIcon();
        SimpleAppLog.info("open abstract page " + newsletter.getTitle());
        UpdateNewStatusAsync task = new UpdateNewStatusAsync(this,
                newsletter);

        task.execute();
        // this.sendBroadcast(intent);
    }

    /**
     *
     */
    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * receive message from server
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey(NOTIFY_REFESH_MAIN_ACTIVITY)) {
                needRefreshMainActivity = true;
            }
        }
    };

    /**
     * receive message from server
     */
    private final BroadcastReceiver mHandleMessageReader = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY)) {
                if (intent.getExtras().getString(CmgPDFActivity.NEED_REFRESH_PREV_ACTIVITY).equalsIgnoreCase(NewsletterDetailActivity.class.getName())) {
                    isFavorites = true;
                    newsletter.setIsFavor(Newsletter.IS_FAVOR);
                    changeFavoritesIcon();
                }
            }
        }
    };


}
