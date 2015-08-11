/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity.coverflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmg.android.caching.CachingHelper;
import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.pension.view.CoverView;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.AndroidCommonUtils.ThumbnailSize;
import com.cmg.mobile.shared.data.Newsletter;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.List;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class CoverflowAdapter extends FancyCoverFlowAdapter {

    private final Context mContext;
    private final List<Newsletter> mLetter;
    @SuppressWarnings("unused")
    private static int index = 0;
    private CarouselHolder view;
    private final CachingHelper cache;
    private int height;
    private int width;

    /**
     * @author LongNguyen
     */
    static class CarouselHolder {
        private ImageView imgView;
        private TextView txtView;
        private ImageView coverView;
        private FrameLayout frmTitle;
    }

    /**
     * clear cache
     */
    public void recycle() {

        try {

            if (view != null) {
                if (view.imgView != null) {
                    if (view.imgView.getDrawable() != null) {
                        view.imgView.getDrawable().setCallback(null);
                    }
                }
                if (view.coverView != null) {
                    if (view.coverView.getDrawable() != null) {
                        view.coverView.getDrawable().setCallback(null);
                    }
                }
                if (view.txtView != null) {
                    if (view.txtView.getBackground() != null) {
                        if (view.txtView.getBackground().getTransparentRegion() != null) {
                            view.txtView.getBackground().getTransparentRegion()
                                    .setEmpty();
                        }
                        view.txtView.getBackground().setCallback(null);
                    }

                    if (view.frmTitle != null
                            && view.frmTitle.getChildCount() > 0) {
                        view.frmTitle.removeAllViews();
                    }

                    if (view.frmTitle != null
                            && view.frmTitle.getBackground() != null) {
                        view.frmTitle.getBackground().setCallback(null);
                        view.frmTitle.setVisibility(View.GONE);
                    }
                }
            }
            if (cache != null) {
                cache.clear();
            }
        } catch (Exception ex) {
            // Silent
        }
    }

    /**
     * Constructor
     *
     * @param c
     * @param list
     */
    public CoverflowAdapter(Context c, List<Newsletter> list) {
        mContext = c;
        this.mLetter = list;
        cache = new CachingHelper();
    }

    /**
     * get Cover Flow size
     */
    public int getCount() {
        return mLetter.size();
    }

    /**
     * get position of object
     */
    public Object getItem(int position) {
        if (mLetter == null || mLetter.size() == 0) {
            return null;
        }
        return mLetter.get(position);
    }

    /**
     * get item id
     */
    public long getItemId(int position) {
        return position;
    }

    // public float getScale(boolean focused, int offset) {
    // /* Formula: 1 / (2 ^ offset) */
    // return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
    // }

    @Override
    public View getCoverFlowItem(int position, View convertView,
                                 ViewGroup parent) {
        LayoutInflater inflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        width = ImageLoaderHelper.getWidth(mContext);
        height = ImageLoaderHelper.getHeight(mContext);
        final ThumbnailSize ts = AndroidCommonUtils.generateThumbnailSize(
                mContext, width, height, .4f);
        final ImageSize is = new ImageSize(ts.w, ts.h);
        if (convertView == null) {
            view = new CarouselHolder();
            convertView = inflator.inflate(R.layout.newsletter_carousel_cell,
                    null);
            FrameLayout frmCached = (FrameLayout) convertView
                    .findViewById(R.id.frm_carousel_title);
            if (mLetter != null && mLetter.size() > 0) {
                cache.put(frmCached, mLetter.get(position).getId());
            }
            view.frmTitle = frmCached;

            view.txtView = (TextView) convertView
                    .findViewById(R.id.carousel_title);
            view.imgView = (ImageView) convertView
                    .findViewById(R.id.carousel_image);
            view.imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            view.coverView = (ImageView) convertView
                    .findViewById(R.id.cover_image);

            view.imgView.setLayoutParams(new FrameLayout.LayoutParams(ts.w,
                    ts.h));
            view.coverView.setLayoutParams(new FrameLayout.LayoutParams(ts.w,
                    ts.h));
            // AndroidCommonUtils.switchOrientation(mContext, view.imgView,
            // view.coverView, height, width);
            convertView.setTag(view);
        } else {
            view = (CarouselHolder) convertView.getTag();
            // CachingHelper.recycleThumbnail((ImageView) convertView
            // .findViewById(R.id.carousel_image));
            // CachingHelper.recycleTitle((FrameLayout) convertView
            // .findViewById(R.id.frm_carousel_title));
        }
        convertView
                .setLayoutParams(new FancyCoverFlow.LayoutParams(ts.w, ts.h));

        if (mLetter.size() > 0) {
            if (!mLetter.get(position).checkDownloaded()) {
                if (mLetter.get(position).getIsNew() == 1) {
                    ((CoverView) view.coverView).setStatus(Newsletter.IS_NEW);
                } else {
                    ((CoverView) view.coverView).setStatus(Newsletter.NOT_NEW);
                }
                view.coverView.setVisibility(View.VISIBLE);
            } else {
                view.coverView.setVisibility(View.GONE);
            }
            ImageLoaderHelper.getImageLoader(mContext).displayImage(
                    mLetter.get(position).getImageUrl(), view.imgView);
            // view.imgView.setVisibility(View.INVISIBLE);
            // ImageLoaderHelper.getImageLoader(mContext).loadImage(
            // mLetter.get(position).getImageUrl(), is,
            // new SimpleImageLoadingListener() {
            // @Override
            // public void onLoadingComplete(String imageUri,
            // View inView, Bitmap loadedImage) {
            // final Bitmap scaledBm = Bitmap.createScaledBitmap(
            // loadedImage, ts.w, ts.h, true);
            // view.imgView.setImageBitmap(scaledBm);
            // // view.imgView.setVisibility(View.VISIBLE);
            // }
            // });
            view.txtView.setText(mLetter.get(position).getTitle());

        }
        // imageCache.put(convertView, position);
        return convertView;
    }

}
