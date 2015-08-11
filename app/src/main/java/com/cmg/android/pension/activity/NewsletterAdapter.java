/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.common.Environment;
import com.cmg.android.pension.downloader.task.DownloadAsync;
import com.cmg.android.pension.view.CoverView;
import com.cmg.android.pension.view.ProgressButton;
import com.cmg.android.plmobile.R;
import com.cmg.android.preference.Preference;
import com.cmg.android.task.UpdateNewStatusAsync;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Newsletter;


import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class NewsletterAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<Newsletter> mLetter;
    private String viewType;
    private ViewHolder view;

    /**
     * @author LongNguyen
     *         <p/>
     *         class to hold object
     */
    static class ViewHolder {
        private ImageView imgView;
        private FrameLayout downloadView;
        private TextView txtViewTitle;
        private TextView txtDate;
        private TextView txtDescription;
        private FrameLayout frmTitle;
        private OnImageClick onImageClick;
        private OnDownloadButtonClick onDownloadBtnClick;
        private ScrollingMovementMethod smmTitle;
        private ScrollingMovementMethod smmDescription;
        private ImageView coverView;
    }

    /**
     * Constructor
     *
     * @param c
     * @param list
     * @param viewType
     */
    public NewsletterAdapter(Context c, List<Newsletter> list, String viewType) {
        mContext = c;
        this.mLetter = list;
        this.setViewType(viewType);
    }

    /**
     * get count
     */
    @Override
    public int getCount() {
        return getmLetter().size();
    }

    /**
     * get item
     */
    @Override
    public Object getItem(int position) {
        return getmLetter().get(position);
    }

    /**
     * get id
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * clear cache
     */
    public void recycle() {
        if (mLetter != null) {
            mLetter.clear();
        }
        if (view != null) {
            if (view.imgView != null && view.imgView.getDrawable() != null) {
                view.imgView.getDrawable().setCallback(null);
                view.imgView = null;
            }
            if (view.coverView != null && view.coverView.getDrawable() != null) {
                ((CoverView) view.coverView).recycle();
                view.coverView.getDrawable().setCallback(null);
                view.coverView = null;
            }
            if (view.txtViewTitle != null
                    && view.txtViewTitle.getBackground() != null) {
                view.txtViewTitle.getBackground().setCallback(null);
            }
            if (view.txtDate != null && view.txtDate.getBackground() != null) {
                view.txtDate.getBackground().setCallback(null);
            }
            if (view.txtDescription != null
                    && view.txtDescription.getBackground() != null) {
                view.txtDescription.getBackground().setCallback(null);
            }
            if (view.frmTitle != null && view.frmTitle.getBackground() != null) {
                view.frmTitle.removeAllViews();
                view.frmTitle.getBackground().setCallback(null);
                view.frmTitle = null;
            }
            if (view.downloadView != null) {
                for (int i = 0; i < view.downloadView.getChildCount(); i++) {
                    View child = view.downloadView.getChildAt(i);
                    if (child != null && child instanceof ProgressButton) {
                        if (((ProgressButton) child).getMessageReceiver()
                                .isOrderedBroadcast()) {
                            try {
                                mContext.unregisterReceiver(((ProgressButton) child)
                                        .getMessageReceiver());
                            } catch (Exception ex) {
                                SimpleAppLog.error("Cannot unregister receiver", ex);
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
                if (view.downloadView.getBackground() != null) {
                    view.downloadView.getBackground().setCallback(null);
                }
                view.downloadView.removeAllViews();
                view.downloadView = null;
            }
            if (view.onImageClick != null) {
                view.onImageClick = null;
            }
            if (view.onDownloadBtnClick != null) {
                view.onDownloadBtnClick.recycle();
                view.onDownloadBtnClick = null;
            }
            view = null;
        }
    }

    /**
     * control download event
     *
     * @author LongNguyen
     */
    class OnDownloadButtonClick implements OnClickListener {
        private ProgressButton btn;
        private Newsletter newsletter;

        /**
         * clear newsletter
         */
        public void recycle() {
            newsletter = null;
            btn = null;
        }

        /**
         * @param btn
         * @param newsletter
         */
        public OnDownloadButtonClick(ProgressButton btn, Newsletter newsletter) {
            this.btn = btn;
            this.newsletter = newsletter;
        }

        @Override
        public void onClick(View v) {
            if (btn.getProgress() < 1) {
                btn.setProgress(0);

                if (Environment.isEnableAnalytics(mContext.getResources())) {
                }

                DownloadAsync da = new DownloadAsync(newsletter, mContext);
                da.execute();
            }
        }

    }

    /**
     * control image click event
     *
     * @author LongNguyen
     */
    class OnImageClick implements OnClickListener {
        private int position;

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, NewsletterDetailActivity.class);
            intent.putExtra(Newsletter.NEWSLETTER_ID, mLetter.get(position)
                    .getId());
            mContext.startActivity(intent);
        }

    }

    /**
     * get view
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int height = ImageLoaderHelper.getHeight(mContext);
        int width = ImageLoaderHelper.getWidth(mContext);
        LayoutInflater inflator = LayoutInflater.from(mContext);

        if (convertView == null) {
            view = new ViewHolder();

            if (getViewType().equalsIgnoreCase(Preference.GRID_VIEW)) {
                convertView = inflator.inflate(R.layout.newsletter_grid_cell,
                        null);
                view.frmTitle = (FrameLayout) convertView
                        .findViewById(R.id.newsletter_title_container);
            } else if (getViewType().equalsIgnoreCase(Preference.LIST_VIEW)) {
                convertView = inflator.inflate(R.layout.newsletter_list_cell,
                        null);
                view.txtDate = (TextView) convertView
                        .findViewById(R.id.newsletter_date);
                view.txtDescription = (TextView) convertView
                        .findViewById(R.id.newsletter_description);
                view.smmDescription = new ScrollingMovementMethod();
                view.txtDescription.setMovementMethod(view.smmDescription);
            } else {
                return null;
            }
            view.smmTitle = new ScrollingMovementMethod();
            view.txtViewTitle = (TextView) convertView
                    .findViewById(R.id.newsletter_title);
            view.txtViewTitle.setMovementMethod(view.smmTitle);
            view.imgView = (ImageView) convertView
                    .findViewById(R.id.img_pdf_newsletter);
            view.onImageClick = new OnImageClick();
            view.imgView.setOnClickListener(view.onImageClick);

            view.coverView = (ImageView) convertView
                    .findViewById(R.id.cover_image);
            if (getViewType().equalsIgnoreCase(Preference.LIST_VIEW)) {
                int orient = mContext.getResources().getConfiguration().orientation;
                switch (orient) {
                    case Configuration.ORIENTATION_LANDSCAPE:
                        view.imgView.setLayoutParams(new FrameLayout.LayoutParams(
                                height / 3, 4 * height / 9));
                        view.coverView
                                .setLayoutParams(new FrameLayout.LayoutParams(
                                        height / 3, 4 * height / 9));
                        break;
                    case Configuration.ORIENTATION_PORTRAIT:
                        view.imgView.setLayoutParams(new FrameLayout.LayoutParams(
                                width / 3, 4 * width / 9));
                        view.coverView
                                .setLayoutParams(new FrameLayout.LayoutParams(
                                        width / 3, 4 * width / 9));
                        break;
                    default:
                }
            }
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        view.onImageClick.position = position;
        view.txtViewTitle.setText(getmLetter().get(position).getTitle());
        ImageLoaderHelper.getImageLoader(mContext).displayImage(
                getmLetter().get(position).getImageUrl(), view.imgView);

        if (getViewType().equalsIgnoreCase(Preference.LIST_VIEW)) {
            view.txtDate.setText(getmLetter().get(position).getDate());
            String des = getmLetter().get(position).getSummary();
            if (des.length() >= 120) {
                des = des.substring(0, 120) + " ...";
            }
            view.txtDescription.setText(des);

        } else {
            final FrameLayout frmContent = (FrameLayout) convertView
                    .findViewById(R.id.frm_content);
            frmContent.post(new Runnable() {
                @Override
                public void run() {
                    int widthFrm = frmContent.getWidth();
                    int heightFrm = 4 * widthFrm / 3;
                    LayoutParams lp = (LayoutParams) frmContent
                            .getLayoutParams();
                    lp.height = heightFrm;
                    frmContent.setLayoutParams(lp);
                }
            });
        }

        if (!getmLetter().get(position).checkDownloaded()) {
            if (getmLetter().get(position).getIsNew() == 1) {
                ((CoverView) view.coverView).setStatus(Newsletter.IS_NEW);
            } else {
                ((CoverView) view.coverView).setStatus(Newsletter.NOT_NEW);
            }
            view.coverView.setVisibility(View.VISIBLE);
        } else {
            view.coverView.setVisibility(View.GONE);
        }
        return convertView;
    }

    /**
     * download image via URL
     *
     * @param url
     * @return
     */
    public static Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url)
                    .getContent());
        } catch (Exception e) {
            // Silent
        }
        return bitmap;
    }

    /**
     * get Newsletter
     *
     * @return
     */
    public List<Newsletter> getmLetter() {
        return mLetter;
    }

    /**
     * get View type
     *
     * @return
     */
    public String getViewType() {
        return viewType;
    }

    /**
     * set View type
     *
     * @param viewType
     */
    public void setViewType(String viewType) {
        this.viewType = viewType;
    }
}
