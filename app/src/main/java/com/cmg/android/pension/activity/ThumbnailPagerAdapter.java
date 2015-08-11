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
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.cmgpdf.util.Utility;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.FileUtils;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.data.NewsletterCategory;
import com.cmg.mobile.shared.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * Created by Hai Lu on 10/24/13.
 */
public class ThumbnailPagerAdapter extends BaseAdapter {
    private static final String TAG = ThumbnailPagerAdapter.class
            .getSimpleName();

    private static final String DEFAULT_CACHE_FOLDER = ".thumbnail";

    private Context mContext;
    private final List<Integer> pages;
    private final Newsletter newsletter;
    private Point thumbnailSize;

    public ThumbnailPagerAdapter(Context context, Newsletter newsletter) {
        mContext = context;
        pages = newsletter.getBookmarkPages();
        this.newsletter = newsletter;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Object getItem(int pPosition) {
        return null;
    }

    @Override
    public long getItemId(int pPosition) {
        return pPosition;
    }

    private Point getThumbnailSize(String filePath) {
        if (thumbnailSize == null) {
            BufferedReader br = null;
            File file = new File(filePath);
            if (!file.exists())
                return null;
            try {
                br = new BufferedReader(new FileReader(file));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append('\n');
                    line = br.readLine();
                }
                String rawText = sb.toString();
                SimpleAppLog.debug("found thumbnail size: " + rawText);
                if (rawText.length() > 0) {
                    rawText = rawText.trim();
                    String[] p = rawText.split("\\|");
                    if (p != null && p.length == 2) {
                        thumbnailSize = new Point();
                        thumbnailSize.x = Integer.parseInt(p[0]);
                        thumbnailSize.y = Integer.parseInt(p[1]);
                    }
                }
            } catch (Exception e) {

            } finally {
                try {
                    if (br != null)
                        br.close();
                } catch (Exception e) {
                    // silent
                }
            }
        }
        return thumbnailSize;

    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.preview_pager_item_layout,
                    parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        if (mPreviewSize != null) {
//            holder.previewPageImageView
//                    .setLayoutParams(new FrameLayout.LayoutParams(
//                            mPreviewSize.x, mPreviewSize.y));
//        }
        holder.previewPageNumber.setText(String.valueOf(pages.get(position)));
        holder.previewPageFrameLayout.setBackgroundColor(Color.TRANSPARENT);

        String pdfDir = FileUtils.getPdfFolder(newsletter.getCategoryId() == NewsletterCategory.PENSIONER ? NewsletterCategory.STR_PENSIONER : NewsletterCategory.STR_EMPLOYEE, mContext);
        String pdfName = StringUtils.getFileName(newsletter.getFileUrl());
        String mPath = pdfDir + File.separator + DEFAULT_CACHE_FOLDER + File.separator;
        String fileUri = Uri.fromFile(new File(pdfDir + File.separator + pdfName)).toString();
        StringBuffer sb = new StringBuffer();
        sb.append(fileUri);
        sb.append(File.separator);
        sb.append(String.valueOf(pages.get(position) - 1));

        String mCachedBitmapFilePath = mPath + Utility.md5(sb.toString()) + ".png";

        final Point p = getThumbnailSize(mPath + Utility.md5(fileUri) + ".s");
        if (p != null) {
            SimpleAppLog.debug("Set point x=" + p.x + " y=" + p.y);
            final ImageView imageView = holder.previewPageImageView;
            holder.previewPageImageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setLayoutParams(new FrameLayout.LayoutParams(
                            p.x, p.y));
                }
            });
        } else {
            SimpleAppLog.debug("Could not found point");
        }

        ImageLoaderHelper.getImageLoader(mContext).displayImage(Uri.fromFile(new File(mCachedBitmapFilePath)).toString(), holder.previewPageImageView);
        return convertView;
    }

    static class ViewHolder {
        ImageView previewPageImageView = null;
        TextView previewPageNumber = null;
        FrameLayout previewPageFrameLayout = null;

        ViewHolder(View view) {
            this.previewPageImageView = (ImageView) view
                    .findViewById(R.id.PreviewPageImageView);
            this.previewPageNumber = (TextView) view
                    .findViewById(R.id.PreviewPageNumber);
            this.previewPageFrameLayout = (FrameLayout) view
                    .findViewById(R.id.PreviewPageFrameLayout);
        }
    }
}
