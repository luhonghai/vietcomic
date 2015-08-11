/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.cmg.android.plmobile.R;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class CoverView extends ImageView {

    private static final int NEW_ITEM = 1;
    private Paint paint = new Paint();
    private RectF rectF = new RectF();
    private Bitmap newIcon;
    private Bitmap favorBitmap;
    private int status;
    private boolean favorMode = false;
    private boolean isDownloaded = false;

    /**
     * get Status
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * set Status
     */
    public void setStatus(int new_status) {
        status = new_status;
        postInvalidate();
    }

    /**
     * Constructor
     *
     * @param context
     */
    public CoverView(Context context) {
        super(context);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     */
    public CoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Draw canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int alpha = 190;
        int strokeWidth = 3;
        float rectLeft = 4;
        float rectTop = 0;
        float rectRight = getWidth() - 5;
        float rectBottom = getHeight() - 3;
        float roundRectX = 20;
        float roundRectY = 20;
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(strokeWidth);

        rectF.set(rectLeft, rectTop, rectRight, rectBottom);
        if (!isDownloaded) {
            paint.setAlpha(alpha);
            canvas.drawRoundRect(rectF, roundRectX, roundRectY, paint);
        }
        if (status == NEW_ITEM) {
            init();
            if (favorMode) {
                canvas.drawBitmap(favorBitmap, getWidth() - favorBitmap.getWidth(), 0,
                        null);
            } else {
                canvas.drawBitmap(newIcon, getWidth() - newIcon.getWidth(), 0,
                        paint);
            }
        }
    }

    /**
     * initial image
     */
    public void init() {
        if (favorMode) {
            if (favorBitmap == null)
                favorBitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_menu_star_on);
        } else {
            if (newIcon == null)
                newIcon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ribbon);
        }
    }

    /**
     * clear cache
     */
    public void recycle() {
        if (newIcon != null) {
            newIcon.recycle();
            newIcon = null;
        }
        if (favorBitmap != null) {
            favorBitmap.recycle();
            favorBitmap = null;
        }
    }

    public boolean isFavorMode() {
        return favorMode;
    }

    public void setFavorMode(boolean favorMode) {
        this.favorMode = favorMode;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }
}
