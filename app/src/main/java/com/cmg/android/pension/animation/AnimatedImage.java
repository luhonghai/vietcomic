/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class AnimatedImage {
    private static final int MAX_PADDING_LEFT = 20;
    private Bitmap bitmap;
    private int x;
    private int y;
    private Speed speed;
    private boolean moving = false;
    private Paint drawPaint;

    /**
     * Constructor
     *
     * @param b
     * @param x
     * @param y
     * @param s
     */
    public AnimatedImage(Bitmap b, int x, int y, Speed s) {
        this.bitmap = b;
        this.x = x;
        this.y = y;
        this.speed = s;

        init();
    }

    /**
     * Constructor
     *
     * @param b
     * @param x
     * @param y
     */
    public AnimatedImage(Bitmap b, int x, int y) {
        this.bitmap = b;
        this.x = x;
        this.y = y;
        this.speed = new Speed();
        init();
    }

    /**
     * clear bitmap
     */
    public void recycle() {
        if (this.bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    /**
     * initial data
     */
    void init() {
        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setFilterBitmap(true);
        drawPaint.setDither(true);
    }

    /**
     * draw function
     *
     * @param canvas
     */
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, drawPaint);
    }

    /**
     * get Max value
     *
     * @return
     */
    public int getMax() {
        return bitmap.getWidth() - MAX_PADDING_LEFT;
    }

    /**
     * draw image by x
     */
    public void update() {
        if (!moving) {
            x -= (speed.getXv() * speed.getxDirection());
            if (x <= -getMax()) {
                moving = true;
            }
        } else {
            Log.i("Stop thread", "Stop Thread");
        }
    }

    /**
     * draw hand image
     */
    public void updateHandImage() {
        if (!moving) {
            x -= (speed.getXv() * speed.getxDirection());
            y -= (speed.getYv() * speed.getyDirection());
            if (y <= 50) {
                moving = true;
            }
        } else {
            Log.i("Stop thread", "Stop Thread");
        }
    }

    /**
     * get bitmap
     *
     * @return
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * get X
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     * set X
     *
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * get Y
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     * set Y
     *
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * get speed
     *
     * @return
     */
    public Speed getSpeed() {
        return speed;
    }

    /**
     * set speed
     *
     * @param speed
     */
    public void setSpeed(Speed speed) {
        this.speed = speed;
    }

    /**
     * check if image is moving
     *
     * @return
     */
    public boolean isMoving() {
        return moving;
    }

    /**
     * set status "moving"
     *
     * @param moving
     */
    public void setMoving(boolean moving) {
        this.moving = moving;
    }
}
