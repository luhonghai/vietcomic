/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cmg.android.plmobile.R;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class AndroidCommonUtils {
    private final static Logger LOG = Logger.getLogger(ContentUtils.class);

    public static final String PENSIONER_LETTER = "pensioner_letter";

    public static final String EMPLOYEE_LETTER = "employee_letter";

    public static final int PDF_THUMBNAIL_MAX_WIDTH = 240;

    public static final int PDF_THUMBNAIL_MAX_HEIGHT = 320;

    /**
     * get XML url
     *
     * @param letter
     * @param context
     * @return
     */
    public static String getXMLDataURL(String letter, Context context) {
        if (letter.equals(PENSIONER_LETTER)) {
            return context.getResources().getString(R.string.xml_url_pensioner);
        } else if (letter.equals(EMPLOYEE_LETTER)) {
            return context.getResources().getString(R.string.xml_url_employee);
        }
        return "";
    }

    /**
     * clear view
     *
     * @param view
     */
    public static void unbindDrawables(View view) {
        try {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        } catch (Exception ex) {
            LOG.error("Error when unbindDrawables", ex);
        }
    }

    /**
     * hide key board
     *
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                .getWindowToken(), 0);
    }

    /**
     * handle the landscape view and portrait view
     *
     * @param context
     * @param imageView
     * @param coverView
     * @param height
     * @param width
     */
    @Deprecated
    public static void switchOrientation(Context context, ImageView imageView,
                                         ImageView coverView, int height, int width) {
        int orient = context.getResources().getConfiguration().orientation;
        switch (orient) {
            case Configuration.ORIENTATION_LANDSCAPE:
                imageView.setLayoutParams(new FrameLayout.LayoutParams(height / 3,
                        4 * height / 9));
                coverView.setLayoutParams(new FrameLayout.LayoutParams(height / 3,
                        4 * height / 9));
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                imageView.setLayoutParams(new FrameLayout.LayoutParams(width / 3,
                        4 * width / 9));
                coverView.setLayoutParams(new FrameLayout.LayoutParams(width / 3,
                        4 * width / 9));
                break;
            default:
        }
    }

    /**
     * @param frame
     * @param height
     * @param width
     */
    public static void setSizeCoverFlowFrame(FrameLayout frame, int height,
                                             int width) {
        frame.setLayoutParams(new RelativeLayout.LayoutParams(width,
                2 * height / 5));
    }

    public static ThumbnailSize generateThumbnailSize(Context context, int w,
                                                      int h) {
        return generateThumbnailSize(context, w, h, .4f);
    }

    /**
     * @param context
     * @param w
     * @param h
     * @param rate
     * @return
     */
    public static ThumbnailSize generateThumbnailSize(Context context, int w,
                                                      int h, float rate) {
        int orient = context.getResources().getConfiguration().orientation;
        ThumbnailSize ts = new ThumbnailSize();
        switch (orient) {
            case Configuration.ORIENTATION_LANDSCAPE:
                ts.w = Math.round(rate * h);
                // if (ts.w > PDF_THUMBNAIL_MAX_WIDTH) {
                // ts.w = PDF_THUMBNAIL_MAX_WIDTH;
                // }
                ts.h = 4 * ts.w / 3;
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                ts.w = Math.round(rate * w);
                // if (ts.w > PDF_THUMBNAIL_MAX_WIDTH) {
                // ts.w = PDF_THUMBNAIL_MAX_WIDTH;
                // }
                ts.h = 4 * ts.w / 3;
                break;
            default:
        }
        return ts;
    }

    public static int generateTextSize(Context context, int size) {
        return Math.round(size
                * context.getResources().getDisplayMetrics().density);
    }

    public static String getLatestScreenShootURL(Context context) {
        File file = getLatestScreenshoot(context);
        if (file != null)
            try {
                return file.toURI().toURL().toString();
            } catch (MalformedURLException e) {
                return "";
            }
        return "";
    }


    private static File getLatestScreenshoot(Context context) {
        String folder = FileUtils.getFolderPath(FileUtils.SCREENSHOOTS_FOLDER, context);
        File folderScreenshoots = new File(folder);
        if (folderScreenshoots.exists() && folderScreenshoots.isDirectory()) {
            File[] files = folderScreenshoots.listFiles();
            if (files != null && files.length > 0) {
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File o1, File o2) {

                        if (o1.lastModified() > o2.lastModified()) {
                            return -1;
                        } else if (o1.lastModified() < o2.lastModified()) {
                            return +1;
                        } else {
                            return 0;
                        }
                    }

                });
            }
            return files[0];
        }
        return null;
    }

    public static String getLatestScreenShootPath(Context context) {
        File file = getLatestScreenshoot(context);
        if (file != null)
            return file.getAbsolutePath();
        return "";

    }

    public static void takeScreenShot(Activity activity) {
        try {
            View view = activity.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap b1 = view.getDrawingCache();
            Rect frame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int statusBarHeight = frame.top;
            int width = -1;
            int height = -1;
            Display display = activity.getWindowManager().getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                Point size = new Point();
                display.getSize(size);
                width = size.x;
                height = size.y;
            } else {
                width = display.getWidth();
                height = display.getHeight();
            }
            Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
                    - statusBarHeight);
            view.destroyDrawingCache();
            b1.recycle();
            savePic(b, activity.getApplicationContext());
        } catch (Exception ignored) {

        }
    }

    private static void savePic(Bitmap b, Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String folder = FileUtils.getFolderPath(FileUtils.SCREENSHOOTS_FOLDER, context);
        File folderScreenshoots = new File(folder);
        if (!folderScreenshoots.exists() && !folderScreenshoots.isDirectory()) {
            folderScreenshoots.mkdirs();
        }

        File[] files = folderScreenshoots.listFiles();
        if (files != null && files.length > 3) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2) {

                    if (o1.lastModified() > o2.lastModified()) {
                        return -1;
                    } else if (o1.lastModified() < o2.lastModified()) {
                        return +1;
                    } else {
                        return 0;
                    }
                }

            });

            files[files.length - 1].delete();
        }

        String fileName = FileUtils.getFilePath(FileUtils.SCREENSHOOTS_FOLDER, sdf.format(new Date(System.currentTimeMillis())) + ".png", context);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex) {

                }
            }
            if (b != null) {
                b.recycle();
            }
        }

    }

    /**
     * @author LUHONGHAI
     */
    public static class ThumbnailSize {
        public int w;
        public int h;
    }
}
