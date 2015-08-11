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
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cmg.android.plmobile.R;
import com.cmg.android.plmobile.SplashScreen;
import com.cmg.android.util.SimpleAppLog;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class SplashPanel extends SurfaceView implements SurfaceHolder.Callback {
    private static final int ANIMATION_SPEED = 20;
    private static final int FPS = 80;
    private final Context context;
    private static final String TAG = SplashPanel.class.getSimpleName();
    private AnimatedImage animatedImage;
    private MainThread thread;
    private long lastDraw = 0;

    /**
     * Constructor
     *
     * @param context
     */
    public SplashPanel(Context context) {
        super(context);
        this.context = context;
        init();
    }

    /**
     * initial data
     */
    void init() {
        getHolder().addCallback(this);

        animatedImage = new AnimatedImage(BitmapFactory.decodeResource(
                getResources(), R.drawable.pl_splash), 0, 0, new Speed(
                ANIMATION_SPEED, 0));
        // ImageLoaderHelper.getImageLoader(context).loadImage(
        // "drawable://" + R.drawable.pl_splash,
        // new SimpleImageLoadingListener() {
        // @Override
        // public void onLoadingComplete(String imageUri, View view,
        // Bitmap loadedImage) {
        // animatedImage = new AnimatedImage(loadedImage, 0, 0,
        // new Speed(ANIMATION_SPEED, 0));
        // }
        // });

        thread = new MainThread(getHolder(), this);
        setFocusable(true);
        SimpleAppLog.info("init animation");
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
    public SplashPanel(Context context, AttributeSet attrs) {
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
     * draw canvas
     *
     * @param canvas
     */
    public void render(Canvas canvas) {
        // canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
        // R.drawable.pl_splash_fix), 0, 0, null);
        canvas.drawColor(Color.WHITE);
        if (animatedImage != null) {
            animatedImage.draw(canvas);
        }
    }

    /**
     * draw image by X
     */
    public void update() {
        if (animatedImage != null) {
            animatedImage.update();
            if (animatedImage.getX() <= -(animatedImage.getMax() - getWidth())) {
                thread.setRunning(false);
                Intent intent = new Intent(SplashScreen.SPLASH_SCREEN_MESSAGE);
                intent.putExtra(SplashScreen.MESSAGE_ACTION,
                        SplashScreen.COMPLETE_ANIMATION);
                context.sendBroadcast(intent);
            }
        }
    }

    /**
     * thread control splash screen
     *
     * @author LongNguyen
     */
    private class MainThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private SplashPanel panel;
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
         * @param p
         */
        public MainThread(SurfaceHolder sh, SplashPanel p) {
            super();
            this.surfaceHolder = sh;
            this.panel = p;
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
                    if (panel != null && canvas != null) {
                        this.panel.render(canvas);
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
                                if (panel != null && canvas != null) {
                                    lastDraw = now;
                                    this.panel.update();
                                    this.panel.render(canvas);
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
