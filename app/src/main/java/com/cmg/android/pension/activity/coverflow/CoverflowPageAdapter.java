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
import com.cmg.android.plmobile.R;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.AndroidCommonUtils.ThumbnailSize;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.util.ContentGenerater;
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
public class CoverflowPageAdapter extends FancyCoverFlowAdapter {
    private final Context mContext;
    private final Newsletter mLetter;
    private List<String> imagePages;
    private CarouselHolder view;
    private final CachingHelper cache;
    private int width;
    private int height;

    /**
     * @author LongNguyen
     */
    static class CarouselHolder {
        private ImageView imgView;
        private TextView txtView;
        private FrameLayout frmTitle;
    }

    /**
     * Constructor
     *
     * @param c
     * @param newsletter
     */
    public CoverflowPageAdapter(Context c, Newsletter newsletter) {
        mContext = c;
        mLetter = newsletter;
        cache = new CachingHelper();
        imagePages = ContentGenerater.createListOfPage(newsletter);
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

        }
    }

    /**
     * get Cover Flow size
     */
    public int getCount() {
        return mLetter.getPage();
    }

    /**
     * get item position
     */
    public Object getItem(int position) {
        return position;
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
        width = ImageLoaderHelper.getWidth(mContext);
        height = ImageLoaderHelper.getHeight(mContext);
        final ThumbnailSize ts = AndroidCommonUtils.generateThumbnailSize(
                mContext, width, height, .3f);
        final ImageSize is = new ImageSize(ts.w, ts.h);
        LayoutInflater inflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            view = new CarouselHolder();
            convertView = inflator.inflate(R.layout.newsletter_carousel_page,
                    null);
            view.txtView = (TextView) convertView.findViewById(R.id.page_title);
            FrameLayout frmCache = (FrameLayout) convertView
                    .findViewById(R.id.frm_page_title);
            cache.put(frmCache, mLetter.getId());
            view.frmTitle = frmCache;

            view.imgView = (ImageView) convertView
                    .findViewById(R.id.page_image);
            view.imgView.setLayoutParams(new FrameLayout.LayoutParams(ts.w,
                    ts.h));
            view.imgView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            convertView.setTag(view);
        } else {
            view = (CarouselHolder) convertView.getTag();
        }
        if (imagePages.size() > 0) {
            ImageLoaderHelper.getImageLoader(mContext).displayImage(
                    imagePages.get(position), view.imgView);
            // view.imgView.setVisibility(View.INVISIBLE);
            // ImageSize is = new ImageSize(w, h);
            // ImageLoaderHelper.getImageLoader(mContext).loadImage(
            // imagePages.get(position), is,
            // new SimpleImageLoadingListener() {
            // @Override
            // public void onLoadingComplete(String imageUri,
            // View inView, Bitmap loadedImage) {
            // view.imgView.setImageBitmap(Bitmap
            // .createScaledBitmap(loadedImage, w, h, true));
            // //view.imgView.setVisibility(View.VISIBLE);
            // }
            // });

            view.txtView.setText("Page " + (position + 1));
        }
        convertView
                .setLayoutParams(new FancyCoverFlow.LayoutParams(ts.w, ts.h));
        return convertView;
    }
}
