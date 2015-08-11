/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.pension.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.common.Environment;
import com.cmg.android.pension.data.NewsletterHelper;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.plmobile.R;
import com.cmg.android.preference.Preference;
import com.cmg.android.util.ExceptionHandler;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.data.NewsletterCategory;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by Hai Lu on 10/24/13.
 */
public class FavoritesFragment extends ProgressSherlockFragment {
    private static Logger log = Logger.getLogger(FavoritesFragment.class);

    public static final String DATE_FORMAT = "MMMM dd, yyyy";

    private View mContentView;
    private Handler mHandler;

    private String sortType;
    private String search;

    private List<Newsletter> newsletters;

    private ListView listView;

    private DatabaseHandler db;
    private FrameLayout frame;
    private View footer;
    private ImageView girlImage;

    private FavoritesAdapter viewAdapter;

    /**
     * thread initial
     */
    private Runnable mShowContentRunnable = new Runnable() {
        @Override
        public void run() {
            init();
            log.info("init fragment");
        }
    };

    /**
     * clear view
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    /**
     * initial data view
     */
    @SuppressWarnings("deprecation")
    void init() {
        Preference.update(getActivity());
        db = new DatabaseHandler(getActivity().getApplicationContext());
        newsletters = db.getAllNewslettersByCategory(NewsletterCategory.FAVORITES ,search);
        for (Newsletter n : newsletters) {
            n = db.getBookmark(n);
        }
        NewsletterHelper.sortNewsletter(newsletters, sortType, Preference
                .getInstance(getActivity().getApplicationContext())
                .isContrastOrder());
        LayoutInflater inflator = LayoutInflater.from(getActivity());
        footer = inflator.inflate(R.layout.footer_view, null);

        if (newsletters != null && newsletters.size() > 0) {
            viewAdapter = new FavoritesAdapter(getActivity(), newsletters);
            listView = (ListView) mContentView
                    .findViewById(R.id.newsletter_favorites_view);
            if (listView.findViewById(R.id.footer_view) != null) {
                listView.removeFooterView(listView
                        .findViewById(R.id.footer_view));
            }
            listView.addFooterView(footer);
            listView.setAdapter(viewAdapter);
            listView.setOnItemClickListener(new ItemClick(newsletters));
            setContentShown(true);
        } else {
            setContentEmpty(true);
            setContentShown(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        sortType = Preference
                .getInstance(getActivity().getApplicationContext())
                .getSortType();
        search = Preference.getInstance(getActivity().getApplicationContext())
                .getStrSearch();

        mContentView = inflater.inflate(R.layout.newsletter_favorites_layout,
                container, false);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Setup content view
        setContentView(mContentView);
        // Setup text for empty content
        setEmptyText(R.string.empty_newsletter);

        obtainData();

    }

    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

    }

    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();

    }

    /**
     * clear cache
     */
    void recycle() {

        if (mHandler != null) {
            mHandler.removeCallbacks(mShowContentRunnable);
            mHandler = null;
        }

    }

    /**
     * destroy view, database connection, adapter
     */
    void destroy() {
        if (db != null) {
            db.recycle();
            db = null;
        }
        if (newsletters != null) {
            newsletters.clear();
            newsletters = null;
        }
        if (viewAdapter != null) {
            viewAdapter.recycle();
        }
        ImageLoaderHelper.getImageLoader(getActivity().getApplicationContext())
                .clearMemoryCache();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        recycle();
        destroy();
    }

    /**
     * get data
     */
    private void obtainData() {
        setContentShown(false);
        mHandler = new Handler();
        mHandler.post(mShowContentRunnable);
    }

    /**
     * control item click function on view
     *
     * @author LongNguyen
     */
    class ItemClick implements AdapterView.OnItemClickListener {
        private final List<Newsletter> newsletters;

        /**
         * Constructor
         */
        public ItemClick(List<Newsletter> newsletters) {
            this.newsletters = newsletters;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.widget.AdapterView.OnItemClickListener#onItemClick(android
         * .widget.AdapterView, android.view.View, int, long)
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int param,
                                long id) {
            if (param < this.newsletters.size()) {
                Intent intent = new Intent(getActivity(),
                        NewsletterDetailActivity.class);
                intent.putExtra(Newsletter.NEWSLETTER_ID,
                        this.newsletters.get(param).getId());
                getActivity().startActivity(intent);
            }
        }
    }

}
