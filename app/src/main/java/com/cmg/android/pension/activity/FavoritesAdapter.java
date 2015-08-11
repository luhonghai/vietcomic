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
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.cmgpdf.CmgPDFActivity;
import com.cmg.android.cmgpdf.view.TwoWayView;
import com.cmg.android.common.Environment;
import com.cmg.android.common.ViewWrapperInfo;
import com.cmg.android.pension.view.CoverView;
import com.cmg.android.plmobile.MainActivity;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.FileUtils;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.util.ContentGenerater;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * Created by Hai Lu on 10/24/13.
 */
public class FavoritesAdapter extends BaseAdapter {
    private static Logger log = Logger.getLogger(FavoritesAdapter.class);
    private final Context mContext;

    private final List<Newsletter> mLetter;

    private ViewHolder view;

    static class ViewHolder {
        private ImageView imgView;
        private FrameLayout downloadView;
        private FrameLayout previewBarHolder;
        private TextView txtViewTitle;
        private TextView txtDate;
        private FrameLayout frmTitle;
        private OnImageClick onImageClick;
        private ScrollingMovementMethod smmTitle;
        private ImageView coverView;
    }

    public FavoritesAdapter(Context c, List<Newsletter> list) {
        mContext = c;
        this.mLetter = list;
    }

    /**
     * control image click event
     *
     * @author LongNguyen
     */
    class OnImageClick implements View.OnClickListener {
        private int position;

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, NewsletterDetailActivity.class);
            intent.putExtra(Newsletter.NEWSLETTER_ID, mLetter.get(position)
                    .getId());
            mContext.startActivity(intent);
        }

    }

    public void recycle() {
        if (view == null)
            return;
        if (view.coverView != null && view.coverView.getDrawable() != null) {
            ((CoverView) view.coverView).recycle();
            view.coverView.getDrawable().setCallback(null);
            view.coverView = null;
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
        final Newsletter newsletter = mLetter.get(position);
        if (convertView == null) {
            view = new ViewHolder();

            convertView = inflator.inflate(R.layout.newsletter_list_favorites_cell,
                    null);
            view.txtDate = (TextView) convertView
                    .findViewById(R.id.newsletter_date);


            view.txtViewTitle = (TextView) convertView
                    .findViewById(R.id.newsletter_title);
            view.txtViewTitle.setMovementMethod(view.smmTitle);
            view.imgView = (ImageView) convertView
                    .findViewById(R.id.img_pdf_newsletter);
            view.onImageClick = new OnImageClick();
            view.imgView.setOnClickListener(view.onImageClick);

            view.coverView = (ImageView) convertView
                    .findViewById(R.id.cover_image);
            view.previewBarHolder = (FrameLayout) convertView.findViewById(R.id.PreviewBarHolder);
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
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        view.onImageClick.position = position;
        view.txtViewTitle.setText(newsletter.getTitle());
        ImageLoaderHelper.getImageLoader(mContext).displayImage(
                mLetter.get(position).getImageUrl(), view.imgView);
        if (newsletter.getBookmarkPages().size() > 0) {
            view.previewBarHolder.setVisibility(View.VISIBLE);
        } else {
            view.previewBarHolder.setVisibility(View.INVISIBLE);
        }
        view.txtDate.setText(newsletter.getDate());

        ((CoverView) view.coverView).setFavorMode(true);
        ((CoverView) view.coverView).setDownloaded(newsletter.checkDownloaded());
        ((CoverView) view.coverView).setStatus(Newsletter.IS_FAVOR);

        log.info("init two way view. Newsletter " + newsletter.getTitle() + " bookmark pages size: " + newsletter.getBookmarkPages());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        ThumbnailPagerAdapter thumbnailPagerAdapter = new ThumbnailPagerAdapter(mContext, newsletter);
        return convertView;
    }


    /**
     * get count
     */
    @Override
    public int getCount() {
        return mLetter.size();
    }

    /**
     * get item
     */
    @Override
    public Object getItem(int position) {
        return mLetter.get(position);
    }

    /**
     * get id
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }


}
