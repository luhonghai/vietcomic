/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.caching;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.cmg.android.plmobile.R;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Newsletter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


import java.util.List;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public final class ImageLoaderHelper {

    /**
     * Constructor
     */
    private ImageLoaderHelper() {

    }



    /**
     * load image to disk cache
     *
     * @param images
     */
    public static void silentLoadImageToDiscCache(final List<String> images) {
        SimpleAppLog.info("init loading disc cache");

    }

    /**
     * Constructor
     *
     * @param context
     * @return
     */
    public static ImageLoader getImageLoader(Context context) {
        try {
            if (ImageLoader.getInstance().isInited()) {
                return ImageLoader.getInstance();
            }
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.icon_pdf)
                    .showImageForEmptyUri(R.drawable.icon_pdf)
                    .showImageOnFail(R.drawable.icon_pdf).cacheInMemory()
                    .cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                    context).defaultDisplayImageOptions(defaultOptions).build();
            ImageLoader.getInstance().init(config);
            return ImageLoader.getInstance();
        } catch (Exception ex) {
            SimpleAppLog.error("Error when get image loader instance", ex);
            return null;
        }
    }

    /**
     * @param activity
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int calculateImageSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();

        int height = display.getHeight();
        long roundedHeightSize = Math.round((0.2132 * height) + 27.177);
        SimpleAppLog.info("Height: " + height);
        SimpleAppLog.info("Round Height: " + roundedHeightSize);

        int width = display.getWidth();
        long roundedWidthSize = Math.round((0.4264 * width) - 6.9355);
        SimpleAppLog.info("Width: " + width);
        SimpleAppLog.info("Round Width: " + roundedWidthSize);

        return (int) ((roundedHeightSize + roundedWidthSize) / 2);
    }

    /**
     * @param mContext
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getWidth(Context mContext) {
        int width = 0;
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        } else {
            width = display.getWidth(); // deprecated
        }
        return width;
    }

    /**
     * @param mContext
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getHeight(Context mContext) {
        int height = 0;
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12) {
            Point size = new Point();
            display.getSize(size);
            height = size.y;
        } else {
            height = display.getHeight(); // deprecated
        }
        return height;
    }


}
