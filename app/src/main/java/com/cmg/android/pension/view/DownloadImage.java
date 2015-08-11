/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.cmg.android.pension.downloader.task.DownloadAsync;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.SimpleAppLog;
import com.cmg.mobile.shared.data.Newsletter;


/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class DownloadImage extends View {
    private Paint paint = new Paint();
    private Paint textPaint = new Paint();
    private int mMax;
    private int mProgress;
    private int lastProgress = -1;
    private RectF rectF = new RectF();
    private final Context context;
    private final Newsletter newsletter;
    private boolean beep = false;
    private int beepCount = 1;
    private String strBeep = "Downloading";
    private ThreadCircle thread;
    private Bitmap bitmap;

    public static final int DRAW_DOWNLOADING_ANI = -1;
    public static final int DRAW_DOWNLOADED_ANI = -2;
    public static final long DOUBLE_TAP_TIME = 300;
    public static final int MAX_VALUE = 100;
    public static final int MIN_VALUE = 0;
    public static final int sleepTime = 500;

    /**
     * Returns the maximum download progress value.
     */
    public int getMax() {
        return mMax;
    }

    /**
     * Sets the maximum download progress value. Defaults to 100.
     */
    public void setMax(int max) {
        mMax = max;
        invalidate();
    }

    /**
     * Returns the current download progress from 0 to max.
     */
    public int getProgress() {
        return mProgress;
    }

    /**
     * Sets the current download progress (between 0 and max).
     *
     * @see #setMax(int)
     */
    public void setProgress(int progress) {

        if (lastProgress != progress) {
            mProgress = progress;

            lastProgress = progress;
            postInvalidate();
        }
    }

    /**
     * Constructor
     *
     * @param context
     * @param newsletter
     */
    public DownloadImage(Context context, Newsletter newsletter) {
        super(context);
        this.context = context;
        this.newsletter = newsletter;
        init();
    }

    /**
     * initial value
     */
    private void init() {
        mMax = MAX_VALUE;
        mProgress = DRAW_DOWNLOADING_ANI;
        bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.help_hand);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @SuppressWarnings("unused")
    @Override
    protected void onDraw(Canvas canvas) {
        int imgWidth = getWidth();
        int imgHeight = getHeight();
        int alphaMax = 255;
        int alphaMin = 200;
        int textSize = 28;
        float strokeWidth = 3;
        float xText = 10;
        float yText = 50;
        float rectLeft = 5;
        float rectTop = 4;
        float xRoundRect = 15;
        float yRoundRect = 15;
        int widthBm = 60;
        int heightBm = 60;
        if (bitmap != null) {
            widthBm = bitmap.getWidth();
            heightBm = bitmap.getHeight();
        }

        textPaint.setColor(Color.BLACK);
        textPaint.setAlpha(alphaMax);
        textPaint.setTextSize(AndroidCommonUtils.generateTextSize(context, 16));

        paint.setColor(Color.WHITE);
        paint.setAlpha(alphaMin);
        paint.setStrokeWidth(strokeWidth);

        if (mProgress >= 0) {
            rectF.set(rectLeft, rectTop, imgWidth - 5, imgHeight
                    * (MAX_VALUE - mProgress) / mMax);
            canvas.drawRoundRect(rectF, xRoundRect, yRoundRect, paint);
//            if (mProgress % 5 == 0) {
//                beep = true;
//            }
//
//            if (beep) {
//                switch (beepCount) {
//                    case 1:
//                        strBeep = "Downloading .";
//                        beepCount++;
//                        break;
//                    case 2:
//                        strBeep = "Downloading ..";
//                        beepCount++;
//                        break;
//                    default:
//                        strBeep = "Downloading ...";
//                        beepCount = 1;
//                        break;
//                }
//                beep = false;
//            }
//            canvas.drawText(strBeep, xText, yText, textPaint);
        } else {
            rectF.set(rectLeft, rectTop, imgWidth - 5, imgHeight);
            // rectF.offset(100, 100);

            if (mProgress == DRAW_DOWNLOADED_ANI) {
//                canvas.drawText("Open", imgWidth / 4, imgHeight / 9 + 10,
//                        textPaint);
            } else {
                canvas.drawRoundRect(rectF, xRoundRect, yRoundRect, paint);
//                canvas.drawText("Download", imgWidth / 7, imgHeight / 9 + 10,
//                        textPaint);
            }

            Paint circlePaint = new Paint() {
                {
                    setColor(Color.GREEN);
                    setStyle(Paint.Style.STROKE);
                    setStrokeCap(Paint.Cap.ROUND);
                    setAntiAlias(true);
                }
            };
            circlePaint.setStrokeWidth(strokeWidth);
            rectLeft = 10;
            rectTop = 10;

            final Path path = new Path();
            float startAngle;
            float xStart;
            float xEnd;
            float yStart;
            float yEnd;
            float angle = 300;

            switch (beepCount) {
                case 1:
                    xStart = imgWidth / 3 + 2 * widthBm / 5 - 12;
                    yStart = 3 * imgHeight / 5 + 12;
                    xEnd = imgWidth / 3 + 2 * widthBm / 5 + 6;
                    yEnd = 3 * imgHeight / 5 - 3;
                    angle = 260;
                    circlePaint.setAlpha(255);
                    rectF.set(rectLeft, rectTop, getWidth() - 35, getHeight() - 35);
                    startAngle = getSemicircle(xStart, yStart, xEnd, yEnd, rectF);
                    beepCount++;
                    break;
                case 2:
                    xStart = imgWidth / 3 + 2 * widthBm / 5 - 12;
                    yStart = 3 * imgHeight / 5 + 18;
                    xEnd = imgWidth / 3 + 3 * widthBm / 5 - 18;
                    yEnd = 3 * imgHeight / 5 - 16;
                    angle = 300;
                    circlePaint.setAlpha(220);
                    rectF.set(rectLeft, rectTop, getWidth() - 15, getHeight() - 15);
                    startAngle = getSemicircle(xStart, yStart, xEnd, yEnd, rectF);
                    beepCount++;
                    break;
                case 3:
                    xStart = imgWidth / 3 + 2 * widthBm / 5 - 14;
                    yStart = 3 * imgHeight / 5 + 20;
                    xEnd = imgWidth / 3 + 3 * widthBm / 5 - 16;
                    yEnd = 3 * imgHeight / 5 - 24;
                    angle = 305;
                    circlePaint.setAlpha(200);
                    rectF.set(rectLeft, rectTop, getWidth() - 25, getHeight() - 25);
                    startAngle = getSemicircle(xStart, yStart, xEnd, yEnd, rectF);
                    beepCount++;
                    break;
                case 4:
                    xStart = imgWidth / 3 + 2 * widthBm / 5 - 16;
                    yStart = 3 * imgHeight / 5 + 22;
                    xEnd = imgWidth / 3 + 3 * widthBm / 5 - 14;
                    yEnd = 3 * imgHeight / 5 - 40;
                    angle = 311;
                    circlePaint.setAlpha(180);
                    startAngle = getSemicircle(xStart, yStart, xEnd, yEnd, rectF);
                    beepCount++;
                    break;
                default:
                    xStart = imgWidth / 3 + 2 * widthBm / 5 - 12;
                    yStart = 3 * imgHeight / 5 + 12;
                    xEnd = imgWidth / 3 + 2 * widthBm / 5 + 6;
                    yEnd = 3 * imgHeight / 5 - 3;
                    angle = 260;
                    circlePaint.setAlpha(255);
                    rectF.set(rectLeft, rectTop, getWidth() - 35, getHeight() - 35);
                    startAngle = getSemicircle(xStart, yStart, xEnd, yEnd, rectF);
                    beepCount = 1;
                    break;
            }

            path.addArc(rectF, startAngle, angle);
            canvas.drawPath(path, circlePaint);
            if (bitmap != null && !bitmap.isRecycled()) {
                canvas.drawBitmap(bitmap, imgWidth / 3, 3 * imgHeight / 5,
                        paint);
            }
        }

    }

    /**
     * draw semi-circle
     */
    public static float getSemicircle(float xStart, float yStart, float xEnd,
                                      float yEnd, RectF ovalRectOUT) {

        float centerX = xStart + ((xEnd - xStart) / 2);
        float centerY = yStart + ((yEnd - yStart) / 2);

        double xLen = (xEnd - xStart);
        double yLen = (yEnd - yStart);
        float radius = (float) (Math.sqrt(xLen * xLen + yLen * yLen) / 2);

        RectF oval = new RectF((float) (centerX - radius),
                (float) (centerY - radius), (float) (centerX + radius),
                (float) (centerY + radius));

        ovalRectOUT.set(oval);

        double radStartAngle = 0;
        // if (direction == Side.LEFT) {
        radStartAngle = Math.atan2(yStart - centerY, xStart - centerX);
        // } else {
        // radStartAngle = Math.atan2(yEnd - centerY, xEnd - centerX);
        // }
        float startAngle = (float) Math.toDegrees(radStartAngle);

        return startAngle;

    }

    /**
     * stop thread
     */
    public void shutDownCircle() {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
    }

    /**
     * draw circle using thread
     */
    public void startDrawCircle() {
        thread = new ThreadCircle(this);
        thread.start();
    }

    /**
     * class control thread
     */
    class ThreadCircle extends Thread {
        private final DownloadImage downloadImage;
        private boolean running = true;

        /**
         * @return
         */
        public boolean isRunning() {
            return running;
        }

        /**
         * @param running
         */
        public void setRunning(boolean running) {
            this.running = running;
        }

        /**
         * @param downloadImage
         */
        public ThreadCircle(DownloadImage downloadImage) {
            this.downloadImage = downloadImage;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // Silent
                }
                downloadImage.postInvalidate();
            }

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // if (mProgress < 1) {
            // setProgress(0);
            // DownloadAsync da = new DownloadAsync(newsletter, context);
            // da.execute();
            // }
        }

        return super.onTouchEvent(ev);
    }

    /**
     * get Message from server
     */
    public BroadcastReceiver getMessageReceiver() {
        return mHandleMessageReceiver;
    }

    /**
     * receive Message from server
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String progressValue = intent.getExtras().getString(
                        DownloadAsync.PROGRESS_VALUE);
                String newsletterId = intent.getExtras().getString(
                        Newsletter.NEWSLETTER_ID);
                if (newsletter != null && newsletterId != null
                        && newsletterId.length() > 0 && progressValue != null
                        && progressValue.length() > 0
                        && newsletterId.equals(newsletter.getId())) {
                    int progress = Integer.parseInt(progressValue);
                    if (progress == MAX_VALUE) {
                        // this.setVisibility(View.GONE);
                        // DownloadImage.this.setVisibility(View.GONE);
                        mProgress = DRAW_DOWNLOADED_ANI;
                        startDrawCircle();
                        return;
                    } else if (progress == DRAW_DOWNLOADING_ANI) {
                        mProgress = DRAW_DOWNLOADING_ANI;
                        startDrawCircle();
                    }
                    setProgress(progress);
                }
            } catch (Exception ex) {
                SimpleAppLog.error("Error when on receive message", ex);
            }
        }
    };

    /**
     * call download animation
     */
    public void startDownload() {
        if (mProgress == DRAW_DOWNLOADING_ANI) {
            shutDownCircle();
            setProgress(MIN_VALUE);
            DownloadAsync da = new DownloadAsync(newsletter, context);
            da.execute();
        }
    }

    /**
     * clear cache
     */
    public void recycle() {
        shutDownCircle();
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

}
