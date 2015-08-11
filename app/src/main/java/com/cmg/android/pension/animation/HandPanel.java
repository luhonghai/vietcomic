/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.animation;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cmg.android.plmobile.R;

import org.apache.log4j.Logger;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class HandPanel extends SurfaceView implements SurfaceHolder.Callback {
    private static Logger log = Logger.getLogger(SplashPanel.class);
    private static final int ANIMATION_SPEED = 5;
    private static final int FPS = 30;
    @SuppressWarnings("unused")
    private final Context context;
    private static final String TAG = SplashPanel.class.getSimpleName();
    private AnimatedImage animatedImage;
    private HandThread thread;
    private long lastDraw = 0;

    /**
     * Constructor
     *
     * @param context
     */
    public HandPanel(Context context) {
        super(context);
        this.context = context;
        init();
    }

    /**
     * initial data
     */
    void init() {
        //this.setBackgroundColor(Color.TRANSPARENT);
        //this.setZOrderOnTop(true); //necessary
        //getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);
        animatedImage = new AnimatedImage(BitmapFactory.decodeResource(
                getResources(), R.drawable.help_hand), 120, 220, new Speed(
                ANIMATION_SPEED, 5));
        thread = new HandThread(getHolder(), this);
        setFocusable(true);
        log.info("init animation");
    }

    /**
     * start thread
     */
    public void start() {
        thread.setSkip(false);
    }

    /**
     * clear image
     */
    public void recycle() {
        if (animatedImage != null) {
            animatedImage.recycle();
            animatedImage = null;
        }
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     */
    public HandPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface is being destroyed");
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
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
        Log.d(TAG, "Thread was shut down cleanly");
    }

    /**
     * draw image
     *
     * @param canvas
     */
    public void render(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        animatedImage.draw(canvas);
    }

    /**
     * draw image by Y
     */
    public void update() {
        animatedImage.updateHandImage();
        if (animatedImage.getY() == 50) {
            thread.setRunning(false);
        }
    }

    /**
     * thread control hand image
     *
     * @author LongNguyen
     */
    private class HandThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private HandPanel handPanel;
        private boolean running;
        private boolean skip = true;

        /**
         * set running
         *
         * @param r
         */
        public void setRunning(boolean r) {
            this.running = r;
        }

        /**
         * Constructor
         *
         * @param sh
         * @param h
         */
        public HandThread(SurfaceHolder sh, HandPanel h) {
            super();
            this.surfaceHolder = sh;
            this.handPanel = h;
        }

        /**
         * set skip
         *
         * @param skip
         */
        public void setSkip(boolean skip) {
            this.skip = skip;
        }

        @Override
        public void run() {
            Canvas canvas = this.surfaceHolder.lockCanvas();
            try {
                synchronized (surfaceHolder) {
                    if (handPanel != null && canvas != null) {
                        this.handPanel.render(canvas);
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            while (running) {
                if (!skip) {
                    long now = System.currentTimeMillis();
                    if (lastDraw == 0 || (now - lastDraw) > (1000 / FPS)) {
                        canvas = null;
                        try {
                            canvas = this.surfaceHolder.lockCanvas();
                            synchronized (surfaceHolder) {
                                if (handPanel != null && canvas != null) {
                                    lastDraw = now;
                                    this.handPanel.update();
                                    this.handPanel.render(canvas);
                                }
                            }
                        } finally {
                            if (canvas != null) {
                                surfaceHolder.unlockCanvasAndPost(canvas);
                            }
                        }
                    }
                }
            }
        }

    }
}
