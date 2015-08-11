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
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmg.android.caching.CachingHelper;
import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.cmgpdf.CmgPDFActivity;
import com.cmg.android.common.Environment;
import com.cmg.android.common.ViewWrapperInfo;
import com.cmg.android.pension.activity.coverflow.CoverflowPageAdapter;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.pension.downloader.task.DownloadAsync;
import com.cmg.android.pension.view.DownloadImage;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.ExceptionHandler;
import com.cmg.android.util.FileUtils;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.util.ContentGenerater;
import com.pagesuite.flowtext.FlowTextView;


import java.io.File;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class NewsletterDetailFragment extends ProgressSherlockFragment {
    private static final int BEEP_DELAY_TIME = 400;

    private View mContentView;
    private Handler mHandler;
    private DatabaseHandler db;
    private Newsletter newsletter;
    private ImageView imageView;
    private DownloadImage imageDownload;
    private String pdfPath;
    private FrameLayout frame;
    private FancyCoverFlow fancyCoverFlow;
    private CoverflowPageAdapter adapter;
    private boolean isOnline;
    private boolean isLandscape = false;
    private int height;
    private int width;
    private TextView newsletterStatus;
    private final Handler statusHandler = new Handler();
    private boolean isDownloading = false;
    private int beep = 1;
    private Runnable statusRunnale = new Runnable() {
        @Override
        public void run() {
            if (newsletterStatus == null)
                return;
            if (beep > 3)
                beep = 1;
            String strBeep = "";
            switch (beep) {
                case 1:
                    strBeep = ".";
                    break;
                case 2:
                    strBeep = "..";
                    break;
                case 3:
                    strBeep = "...";
                    break;
                default:
                    strBeep = ".";
                    break;
            }
            newsletterStatus.setText(getResources().getText(R.string.status_downloading) + strBeep);
            beep++;
            statusHandler.postDelayed(statusRunnale, BEEP_DELAY_TIME);
        }
    };

    private Runnable mShowContentRunnable = new Runnable() {

        @Override
        public void run() {
            init();
        }

    };

    @SuppressWarnings("unused")
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
     * initial data
     */
    void init() {
        Bundle bundle = getArguments();
        String newsletterId = bundle.getString(Newsletter.NEWSLETTER_ID);
        SimpleAppLog.info("Start read newsletter detail id: " + newsletterId);
        db = new DatabaseHandler(getActivity());
        newsletter = db.getById(newsletterId);
        if (newsletter != null) {
            //initView();
            setData();
            setContentShown(true);
        } else {
            setContentEmpty(true);
            setContentShown(true);
        }

    }

    /**
     * control click function on newsletter
     *
     * @author LongNguyen
     */
    class OnClickViewNewsletter implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (newsletter.checkDownloaded()) {
                File file = new File(pdfPath);
                if (file.exists()) {
                    Uri uri = Uri.fromFile(file);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                    Bundle bundle = new Bundle();
                    bundle.putString(ViewWrapperInfo.DETAIL_CLASS,
                            NewsletterDetailActivity.class.getName());
                    bundle.putInt(ViewWrapperInfo.ITEM_PAGE, -1);
                    bundle.putString(ViewWrapperInfo.ITEM_ID_KEY,
                            Newsletter.NEWSLETTER_ID);
                    bundle.putString(ViewWrapperInfo.ITEM_ID_VALUE,
                            newsletter.getId());
                    bundle.putString(ViewWrapperInfo.SHARE_SUBJECT,
                            newsletter.getTitle());
                    bundle.putString(ViewWrapperInfo.SHARE_TEXT,
                            ContentGenerater.generateShareInfo(newsletter));
                    intent.putExtras(bundle);

                    intent.setClass(getActivity(), CmgPDFActivity.class);


                    startActivity(intent);
                }
            } else {
                isOnline = isOnline();
                if (isOnline) {

                    if (!isDownloading) {
                        isDownloading = true;
                        statusHandler.post(statusRunnale);
                    }
                    imageDownload.startDownload();
                } else {
                    Toast.makeText(
                            NewsletterDetailFragment.this.getActivity(),
                            "Make sure you are connected to 3G or Wi-Fi to download file",
                            Toast.LENGTH_LONG).show();
                }
            }

        }

    }

    /**
     * control item click on coverflow view
     *
     * @author LongNguyen
     */
    class OnClickCoverFollowItem implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            File file = new File(pdfPath);
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Bundle bundle = new Bundle();
                bundle.putInt(ViewWrapperInfo.ITEM_PAGE, arg2 + 1);
                bundle.putString(ViewWrapperInfo.DETAIL_CLASS,
                        NewsletterDetailActivity.class.getName());
                bundle.putString(ViewWrapperInfo.ITEM_ID_KEY,
                        Newsletter.NEWSLETTER_ID);
                bundle.putString(ViewWrapperInfo.ITEM_ID_VALUE,
                        newsletter.getId());
                bundle.putString(ViewWrapperInfo.SHARE_SUBJECT,
                        newsletter.getTitle());
                bundle.putString(ViewWrapperInfo.SHARE_TEXT,
                        ContentGenerater.generateShareInfo(newsletter));
                intent.putExtras(bundle);

                startActivity(intent);
            }

        }

    }

    /**
     * intial view
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public void initView() {
        height = ImageLoaderHelper.getHeight(getActivity());
        width = ImageLoaderHelper.getWidth(getActivity());
        if (fancyCoverFlow == null) {
            SimpleAppLog.debug("Start init coverflow");
            adapter = new CoverflowPageAdapter(getActivity(), newsletter);
            fancyCoverFlow = new FancyCoverFlow(getActivity());
            fancyCoverFlow.setAdapter(adapter);
            fancyCoverFlow.setUnselectedAlpha(1f);
            fancyCoverFlow.setUnselectedSaturation(0.5f);
            fancyCoverFlow.setUnselectedScale(0.6f);
            fancyCoverFlow.setSpacing(10);
            fancyCoverFlow.setMaxRotation(45);
            fancyCoverFlow.setScaleDownGravity(0.5f);
            fancyCoverFlow.setGravity(Gravity.BOTTOM);
            fancyCoverFlow
                    .setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);

            fancyCoverFlow.setOnItemClickListener(new OnClickCoverFollowItem());
        }

        if (!isLandscape) {
            frame = (FrameLayout) mContentView
                    .findViewById(R.id.frame_carousel);
            if (frame.getChildCount() > 0) {
                frame.removeAllViews();
            }
            // AndroidCommonUtils.setSizeCoverFlowFrame(frame, height, width);
            frame.addView(fancyCoverFlow);
            SimpleAppLog.info("Add fancy cover flow to view");
            if (!newsletter.checkDownloaded()) {
                SimpleAppLog.info("Newsletter was not downloaded");
                frame.setVisibility(View.INVISIBLE);
            } else {
                SimpleAppLog.info("Newsletter was downloaded");
                frame.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * set data into view
     */
    public void setData() {
        SimpleAppLog.debug("Start setData coverflow");
        height = ImageLoaderHelper.getHeight(getActivity());
        width = ImageLoaderHelper.getWidth(getActivity());
        imageView = (ImageView) mContentView.findViewById(R.id.full_image_view);
        pdfPath = FileUtils.getPdfFile(newsletter, getActivity()
                .getApplicationContext());
        int orient = getActivity().getResources().getConfiguration().orientation;

        ImageLoaderHelper.getImageLoader(getActivity()).displayImage(
                newsletter.getImageUrl(), imageView);
        TextView titleView = (TextView) mContentView
                .findViewById(R.id.txtTitle);
        titleView.setText(newsletter.getTitle());
        TextView dateView = (TextView) mContentView.findViewById(R.id.txtDate);
        dateView.setText(newsletter.getDate());
        FlowTextView desView = (FlowTextView) mContentView
                .findViewById(R.id.txtDescription);

        desView.setText(newsletter.getSummary() + "\n");
        desView.setTextSize(AndroidCommonUtils.generateTextSize(
                this.getActivity(), 16));
        desView.invalidate();

        FrameLayout frameDownload = (FrameLayout) mContentView
                .findViewById(R.id.frm_progress_download);


        newsletterStatus = (TextView) mContentView.findViewById(R.id.statusNewsletter);

        frameDownload.removeAllViews();
        imageDownload = new DownloadImage(
                getActivity().getApplicationContext(), newsletter);
        if (newsletter.checkDownloaded()) {
            imageDownload.setProgress(DownloadImage.DRAW_DOWNLOADED_ANI);
            newsletterStatus.setText(getResources().getText(R.string.status_open));
        } else {
            imageDownload.setProgress(DownloadImage.DRAW_DOWNLOADING_ANI);
            newsletterStatus.setText(getResources().getText(R.string.status_download));
        }
        switch (orient) {
            case Configuration.ORIENTATION_LANDSCAPE:
                imageView.setLayoutParams(new FrameLayout.LayoutParams(height / 3,
                        4 * height / 9));
                frameDownload.setLayoutParams(new FrameLayout.LayoutParams(
                        height / 3, 4 * height / 9));
                desView.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                imageView.setLayoutParams(new FrameLayout.LayoutParams(width / 3,
                        4 * width / 9));
                frameDownload.setLayoutParams(new FrameLayout.LayoutParams(
                        width / 3, 4 * width / 9));
                desView.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, height / 2));
                break;
            default:
        }
        imageView.setOnClickListener(new OnClickViewNewsletter());
        imageDownload.startDrawCircle();
        imageDownload.setOnClickListener(new OnClickViewNewsletter());
        frameDownload.addView(imageDownload);
        if (!mHandleMessageReceiver.isInitialStickyBroadcast()) {
            getActivity().registerReceiver(
                    mHandleMessageReceiver,
                    new IntentFilter(DownloadAsync.DISPLAY_MESSAGE_ACTION + "@"
                            + newsletter.getId()));
        }
        if (!imageDownload.getMessageReceiver().isInitialStickyBroadcast()) {
            getActivity().registerReceiver(
                    imageDownload.getMessageReceiver(),
                    new IntentFilter(DownloadAsync.DISPLAY_MESSAGE_ACTION + "@"
                            + newsletter.getId()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.newsletter_detail, container,
                false);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(
                getActivity()));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int orient = getResources().getConfiguration().orientation;
        switch (orient) {
            case Configuration.ORIENTATION_LANDSCAPE:
                isLandscape = true;
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                break;
            default:
        }
        setContentView(mContentView);
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
     * destroy view, database connection
     */
    void destroy() {
        // mHandler.removeCallbacks(null);
        if (imageDownload != null) {
            try {
                imageDownload.recycle();
                getActivity().unregisterReceiver(
                        imageDownload.getMessageReceiver());
            } catch (Exception ex) {
                // Silent
            }
        }
        if (fancyCoverFlow != null) {
            View[] tmp = new View[fancyCoverFlow.getCount()];
            int count = fancyCoverFlow.getCount();
            for (int i = 0; i < count; i++) {
                tmp[i] = fancyCoverFlow.getChildAt(i);
                if (tmp[i] != null) {
                    // CachingHelper.recycleThumbnail((ImageView) tmp[i]
                    // .findViewById(R.id.page_image));
                    CachingHelper.recycleTitle((FrameLayout) tmp[i]
                            .findViewById(R.id.frm_page_title));

                }
            }
            if (frame != null) {
                frame.removeAllViews();
                frame = null;
            }
            fancyCoverFlow = null;
        }
        if (adapter != null) {
            adapter.clear();
            adapter.recycle();
            adapter = null;
        }
        if (mHandleMessageReceiver != null) {
            try {
                getActivity().unregisterReceiver(mHandleMessageReceiver);
            } catch (Exception ex) {
                // Silent
            }
        }
        try {
            statusHandler.removeCallbacks(statusRunnale);
        } catch (Exception e) {
            // silent
        }
        if (db != null) {
            db.recycle();
            db = null;
        }
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
        SimpleAppLog.debug("start obtainData");
        setContentShown(false);
        mHandler = new Handler();
        mHandler.post(mShowContentRunnable);
    }

    /**
     * set view visible
     */
    @Deprecated
    public void setVisible() {
        if (!isLandscape) {
            SimpleAppLog.info("set visible frame");
            frame.setVisibility(View.VISIBLE);
        }
    }

    /**
     * check if devices have Internet connection
     *
     * @return
     */
    public boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) this.getActivity()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobileInfo = cm
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiInfo = cm
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if ((mobileInfo != null && mobileInfo.isConnected())
                    || (wifiInfo != null && wifiInfo.isConnected())) {
                return true;
            }
        } catch (Exception ex) {
            SimpleAppLog.error("Error when check is online", ex);
        }
        return false;
    }

    /**
     * control receive message from server
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String progressValue = intent.getExtras().getString(
                    DownloadAsync.PROGRESS_VALUE);
            String newsletterId = intent.getExtras().getString(
                    Newsletter.NEWSLETTER_ID);
            String startAnimation = intent.getExtras().getString(
                    "START_ANIMATION");

            if (newsletter != null && newsletterId != null
                    && newsletterId.length() > 0 && progressValue != null
                    && progressValue.length() > 0
                    && newsletterId.equals(newsletter.getId())) {
                int progress = Integer.parseInt(progressValue);
                if (progress == 100) {
                    SimpleAppLog.info("generate UI when download completed");
//                    setVisible();
                    newsletter.setDownloaded(Newsletter.IS_DOWNLOAD);
                    statusHandler.removeCallbacks(statusRunnale);
                    newsletterStatus.setText(getResources().getText(R.string.status_open));
                    isDownloading = false;
//                    ImageLoaderHelper
//                            .silentLoadImageToDiscCache(ContentGenerater
//                                    .createListOfPage(newsletter));
                    return;
                } else if (progress == DownloadImage.DRAW_DOWNLOADING_ANI) {
                    statusHandler.removeCallbacks(statusRunnale);
                    newsletterStatus.setText(getResources().getText(R.string.status_download));
                } else {
                    if (!isDownloading) {
                        isDownloading = true;
                        statusHandler.post(statusRunnale);
                    }
                }
            }
            if (startAnimation != null
                    && startAnimation.equalsIgnoreCase(newsletter.getId())) {

            }
        }
    };
}
