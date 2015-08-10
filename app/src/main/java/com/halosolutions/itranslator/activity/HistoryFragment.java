/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.halosolutions.itranslator.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.halosolutions.itranslator.BuildConfig;
import com.halosolutions.itranslator.R;
import com.halosolutions.itranslator.adapter.HistoryAdapter;
import com.halosolutions.itranslator.constant.Constant;
import com.halosolutions.itranslator.sqlite.History;
import com.halosolutions.itranslator.sqlite.ext.HistoryDBAdapter;
import com.halosolutions.itranslator.utilities.DataParser;
import com.halosolutions.itranslator.utilities.FilePath;
import com.halosolutions.itranslator.utilities.GlobalUsage;
import com.halosolutions.itranslator.utilities.SimpleAppLog;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by longnguyen on 7/10/15.
 *
 */
public class HistoryFragment extends Fragment {
    public static String TAG = "HistoryFragment";
    private SwipeListView swipeListView;
    private List<History> listHistory;
    private FilePath filePath;
    private DataParser parser;
    private HistoryAdapter adapter;

    private HistoryDBAdapter historyDBAdapter;

    private AdView mAdView;

    public static HistoryFragment newInstance(){
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        swipeListView = null;
        historyDBAdapter.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) mAdView.destroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        filePath = new FilePath(getActivity());
        parser = new DataParser();
        historyDBAdapter = new HistoryDBAdapter(getActivity());
        try {
            historyDBAdapter.open();
        } catch (SQLException e) {
            SimpleAppLog.error("Could not open database",e);
        }
        try {
            listHistory = historyDBAdapter.toCollection(historyDBAdapter.getAll());
        } catch (Exception e) {
            SimpleAppLog.error("Could not list history",e);
        }

        adapter = new HistoryAdapter(getActivity(), listHistory, new HistoryAdapter.OnDimissItem() {
            @Override
            public void onDimissItem(int pos) {
                swipeListView.dismiss(pos);
            }
        });

        swipeListView = (SwipeListView)v.findViewById(R.id.listHistory);

        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                swipeListView.closeOpenedItems();
            }

            @Override
            public void onStartClose(int position, boolean right) {
            }

            @Override
            public void onClickFrontView(int position) {
                GlobalUsage.sourceTxt = listHistory.get(position).getPhase();
                ((MainActivity)getActivity()).getViewPager().setCurrentItem(0);
                ((MainActivity)getActivity()).getDrawerAdapter().setSelected(MainActivity.Tab.LANGUAGE);
            }

            @Override
            public void onClickBackView(int position) {
                swipeListView.closeOpenedItems();
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    try {
                        historyDBAdapter.delete(listHistory.get(position));
                    } catch (Exception e) {
                        SimpleAppLog.error("Could not delete history item: " + position,e);
                    }
                }
                updateListView();
            }

        });
        swipeListView.setAdapter(adapter);
        reload();

        mAdView = (AdView) v.findViewById(R.id.adView);
        if (mAdView != null && BuildConfig.IS_FREE) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        return v;
    }

    public void updateListView(){
        adapter.list.clear();
        try {
            adapter.list.addAll(historyDBAdapter.toCollection(historyDBAdapter.getAll()));
        } catch (Exception e) {
            SimpleAppLog.error("Could not update list view",e);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) mAdView.resume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            updateListView();
        }
    }

    private void reload() {
        swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        swipeListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        swipeListView.setSwipeActionRight(SwipeListView.SWIPE_ACTION_REVEAL);
        swipeListView.setOffsetLeft(convertDpToPixel(310.0f));
        //swipeListView.setOffsetRight(convertDpToPixel(310.0f));
        swipeListView.setAnimationTime(0);
        swipeListView.setSwipeOpenOnLongPress(true);
    }

    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdView != null) mAdView.pause();
    }
}
