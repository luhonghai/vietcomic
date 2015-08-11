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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cmg.android.caching.CachingHelper;
import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.common.Environment;
import com.cmg.android.pension.activity.coverflow.CoverflowAdapter;
import com.cmg.android.pension.data.NewsletterHelper;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.pension.view.ProgressButton;
import com.cmg.android.plmobile.R;
import com.cmg.android.preference.Preference;
import com.cmg.android.task.UpdateNewStatusAsync;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.ExceptionHandler;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Newsletter;


import java.util.List;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class NewsletterFragment extends ProgressSherlockFragment {
    public static final String DATE_FORMAT = "MMMM dd, yyyy";
    private View mContentView;
    private Handler mHandler;
    private NewsletterAdapter viewAdapter;
    private String viewType;
    private String sortType;
    private String search;
    private int letterType;
    private List<Newsletter> newsletters;
    private GridView gridView;
    private ListView listView;
    // private CoverFlow coverFlow;

    private FancyCoverFlow fancyCoverFlow;
    private DatabaseHandler db;
    private CoverflowAdapter adapter;
    private FrameLayout frame;
    private View footer;
    @SuppressWarnings("unused")
    private ImageView girlImage;

    /**
     * thread initial
     */
    private Runnable mShowContentRunnable = new Runnable() {
        @Override
        public void run() {
            init();
            SimpleAppLog.info("init fragment");
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
        newsletters = db.getAllNewslettersByCategory(letterType, search);
        NewsletterHelper.sortNewsletter(newsletters, sortType, Preference
                .getInstance(getActivity().getApplicationContext())
                .isContrastOrder());
        LayoutInflater inflator = LayoutInflater.from(getActivity());
        footer = inflator.inflate(R.layout.footer_view, null);

        if (newsletters != null && newsletters.size() > 0) {
            if (viewType.equalsIgnoreCase(Preference.GRID_VIEW)) {
                viewAdapter = new NewsletterAdapter(getActivity(), newsletters,
                        viewType);
                gridView = (GridView) mContentView.findViewById(R.id.grid_view);

                gridView.setAdapter(viewAdapter);
                gridView.setOnItemClickListener(new ItemClick(newsletters));

            } else if (viewType.equalsIgnoreCase(Preference.LIST_VIEW)) {
                viewAdapter = new NewsletterAdapter(getActivity(), newsletters,
                        viewType);
                listView = (ListView) mContentView
                        .findViewById(R.id.newsletter_list_view);
                if (listView.findViewById(R.id.footer_view) != null) {
                    listView.removeFooterView(listView
                            .findViewById(R.id.footer_view));
                }
                listView.addFooterView(footer);
                listView.setAdapter(viewAdapter);
                listView.setOnItemClickListener(new ItemClick(newsletters));
            } else if (viewType.equalsIgnoreCase(Preference.CAROUSEL_VIEW)) {
                if (adapter == null) {
                    adapter = new CoverflowAdapter(getActivity(), newsletters);
                }
                if (fancyCoverFlow == null) {

                    fancyCoverFlow = new FancyCoverFlow(getActivity());
                    fancyCoverFlow.setAdapter(adapter);
                    fancyCoverFlow.setUnselectedAlpha(1f);
                    fancyCoverFlow.setUnselectedSaturation(0.5f);
                    fancyCoverFlow.setUnselectedScale(0.6f);
                    fancyCoverFlow.setSpacing(10);
                    fancyCoverFlow.setMaxRotation(45);
                    fancyCoverFlow.setScaleDownGravity(0.5f);
                    fancyCoverFlow
                            .setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
                    fancyCoverFlow.setGravity(Gravity.TOP);
                    fancyCoverFlow.setOnItemClickListener(new ItemClick(
                            newsletters));
                }
                if (frame == null) {
                    frame = (FrameLayout) mContentView
                            .findViewById(R.id.frame_carousel);
                }
                if (frame.getChildCount() > 0) {
                    frame.removeAllViews();
                }
                frame.addView(fancyCoverFlow);
            }
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
        viewType = Preference
                .getInstance(getActivity().getApplicationContext())
                .getViewType();
        sortType = Preference
                .getInstance(getActivity().getApplicationContext())
                .getSortType();
        search = Preference.getInstance(getActivity().getApplicationContext())
                .getStrSearch();
        letterType = bundle.getString(NewsletterFragmentAdapter.LETTER_ARG)
                .equals(AndroidCommonUtils.PENSIONER_LETTER) ? 1 : 2;

        if (viewType.equalsIgnoreCase(Preference.GRID_VIEW)) {
            mContentView = inflater.inflate(R.layout.newsletter_grid_layout,
                    container, false);
            girlImage = (ImageView) mContentView.findViewById(R.id.girl_image);


        } else if (viewType.equalsIgnoreCase(Preference.LIST_VIEW)) {
            mContentView = inflater.inflate(R.layout.newsletter_list_layout,
                    container, false);

        } else if (viewType.equalsIgnoreCase(Preference.CAROUSEL_VIEW)) {
            mContentView = inflater.inflate(
                    R.layout.newsletter_carousel_layout, container, false);

        }
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
        if (gridView != null) {
            View[] tmp = new View[gridView.getCount()];
            int count = gridView.getCount();
            ImageView girlFooter = (ImageView) gridView
                    .findViewById(R.id.girl_image);
            if (girlFooter != null) {
                if (girlFooter.getDrawable() != null) {
                    girlFooter.getDrawable().setCallback(null);
                }
            }
            for (int i = 0; i < count; i++) {
                tmp[i] = gridView.getChildAt(i);
                if (tmp[i] != null) {
                    ImageView image = (ImageView) tmp[i]
                            .findViewById(R.id.img_pdf_newsletter);

                    if (image.getDrawable() != null) {
                        image.getDrawable().setCallback(null);
                    }

                    ImageView imageCover = (ImageView) tmp[i]
                            .findViewById(R.id.cover_image);
                    if (imageCover.getDrawable() != null) {
                        imageCover.getDrawable().setCallback(null);
                    }

                    TextView txtTitle = (TextView) tmp[i]
                            .findViewById(R.id.newsletter_title);
                    if (txtTitle != null && txtTitle.getBackground() != null) {
                        if (txtTitle.getBackground().getTransparentRegion() != null) {
                            txtTitle.getBackground().getTransparentRegion()
                                    .setEmpty();
                        }
                        txtTitle.getBackground().setCallback(null);
                    }
                    FrameLayout frmTitle = (FrameLayout) tmp[i]
                            .findViewById(R.id.newsletter_title_container);

                    if (frmTitle != null && frmTitle.getChildCount() > 0) {
                        frmTitle.removeAllViews();
                    }

                    if (frmTitle != null && frmTitle.getBackground() != null) {
                        frmTitle.getBackground().setCallback(null);
                        frmTitle.setVisibility(View.GONE);
                    }

                    FrameLayout frm = (FrameLayout) tmp[i]
                            .findViewById(R.id.frm_progress_download);
                    if (frm != null && frm.getChildCount() > 0) {
                        for (int j = 0; j < frm.getChildCount(); j++) {
                            View child = frm.getChildAt(j);
                            if (child != null
                                    && child instanceof ProgressButton) {
                                if (((ProgressButton) child)
                                        .getMessageReceiver()
                                        .isOrderedBroadcast()) {
                                    try {
                                        getActivity().unregisterReceiver(
                                                ((ProgressButton) child)
                                                        .getMessageReceiver());
                                    } catch (Exception ex) {
                                        // Silent
                                    }
                                }
                                ((ProgressButton) child).recycle();
                                if (child.getBackground() != null) {
                                    child.getBackground().setCallback(null);
                                }
                                child.setVisibility(View.GONE);
                                child = null;
                            }
                        }
                        frm.removeAllViews();
                    }

                    if (frm != null && frm.getBackground() != null) {
                        frm.getBackground().setCallback(null);
                        frm.setVisibility(View.GONE);
                        frm = null;
                    }
                }

            }

            unbindDrawables(gridView);
            gridView = null;
        }
        if (listView != null) {
            int count = listView.getCount();
            for (int i = 0; i < count; i++) {
                View v = listView.getChildAt(i);
                if (v != null) {
                    FrameLayout frm = (FrameLayout) v
                            .findViewById(R.id.frm_progress_download);
                    if (frm != null && frm.getChildCount() > 0) {
                        for (int j = 0; j < frm.getChildCount(); j++) {
                            View child = frm.getChildAt(j);
                            if (child != null
                                    && child instanceof ProgressButton) {
                                if (((ProgressButton) child)
                                        .getMessageReceiver()
                                        .isOrderedBroadcast()) {
                                    try {
                                        getActivity().unregisterReceiver(
                                                ((ProgressButton) child)
                                                        .getMessageReceiver());
                                    } catch (Exception ex) {

                                    }
                                }
                                ((ProgressButton) child).recycle();
                                if (child.getBackground() != null) {
                                    child.getBackground().setCallback(null);
                                }
                                child.setVisibility(View.GONE);
                                child = null;
                            }
                        }
                        frm.removeAllViews();
                    }

                    if (frm != null && frm.getBackground() != null) {
                        frm.getBackground().setCallback(null);
                        frm.setVisibility(View.GONE);
                    }
                    frm = null;
                }
            }
            unbindDrawables(listView);
            listView = null;
        }
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
            viewAdapter = null;
        }

        if (fancyCoverFlow != null) {
            View[] tmp = new View[fancyCoverFlow.getCount()];
            int count = fancyCoverFlow.getCount();
            for (int i = 0; i < count; i++) {
                tmp[i] = fancyCoverFlow.getChildAt(i);
                if (tmp[i] != null) {
                    // CachingHelper.recycleThumbnail((ImageView) tmp[i]
                    // .findViewById(R.id.carousel_image));
                    // CachingHelper.recycleThumbnail((ImageView) tmp[i]
                    // .findViewById(R.id.cover_image));
                    CachingHelper.recycleTitle((FrameLayout) tmp[i]
                            .findViewById(R.id.frm_carousel_title));
                }
            }
            frame.removeAllViews();
            frame = null;
            fancyCoverFlow = null;
        }

        if (adapter != null) {
            adapter.clear();
            adapter.recycle();
            adapter = null;
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
    class ItemClick implements OnItemClickListener {
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
