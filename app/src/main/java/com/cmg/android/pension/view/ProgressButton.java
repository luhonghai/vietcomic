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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import com.cmg.android.pension.activity.FullImageActivity;
import com.cmg.android.pension.downloader.task.DownloadAsync;
import com.cmg.android.plmobile.R;
import com.cmg.mobile.shared.data.Newsletter;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class ProgressButton extends CompoundButton {

    private int mMax;
    private int mProgress;
    private Drawable mShadowDrawable;
    private Paint bgCirclePaint;
    private Paint innerCirclePaint;
    private Paint mCirclePaint;
    private Paint mProgressPaint;
    private Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect mTempRect = new Rect();
    private RectF mTempRectF = new RectF();
    private Point point1Draw = new Point();
    private Point point2Draw = new Point();
    private Point point3Draw = new Point();
    private Path path = new Path();
    private int mDrawableSize;
    private int mInnerSize;
    private final Context context;
    private Newsletter newsletter;
    private int lastProgress = -1;
    private final Resources res = getResources();

    /**
     * Constructor
     *
     * @param context
     */
    public ProgressButton(Context context) {
        super(context);
        this.context = context;
        init(this.context, null, 0);
    }

    /**
     * clear cache
     */
    public void recycle() {
        if (mShadowDrawable != null) {
            mShadowDrawable.setCallback(null);
            mShadowDrawable = null;
        }
        if (newsletter != null) {
            newsletter = null;
        }
        bgCirclePaint = null;
        innerCirclePaint = null;
        mCirclePaint = null;
        mProgressPaint = null;
        mTempRect = null;
        mTempRectF = null;
        point1Draw = null;
        point2Draw = null;
        point3Draw = null;
        path = null;
        paint2 = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     * @param newsletter
     */
    public ProgressButton(Context context, AttributeSet attrs,
                          Newsletter newsletter) {
        super(context, attrs);
        //setNewsletter(newsletter);
        this.context = context;
        init(context, attrs, 0);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     * @param defStyle
     * @param newsletter
     */
    public ProgressButton(Context context, AttributeSet attrs, int defStyle,
                          Newsletter newsletter) {
        super(context, attrs, defStyle);
        //setNewsletter(newsletter);
        this.context = context;
        init(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {

    }

    /**
     * Initial value
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        mMax = 100;
        mProgress = -1;

        int alphaMax = 255;
        int circleColor = res.getColor(R.color.pin_progress_default_circle_color);
        int progressColor = res.getColor(R.color.pin_progress_default_progress_color);

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton, defStyle, 0);

            mMax = a.getInteger(R.styleable.ProgressButton_max, mMax);
            mProgress = a.getInteger(R.styleable.ProgressButton_progress, mProgress);

            circleColor = a.getColor(R.styleable.ProgressButton_circleColor, circleColor);
            progressColor = a.getColor(R.styleable.ProgressButton_progressColor, progressColor);

            a.recycle();
        }

        // Other initialization

        mShadowDrawable = res.getDrawable(R.drawable.pin_progress_shadow);
        mShadowDrawable.setCallback(this);

        mDrawableSize = mShadowDrawable.getIntrinsicWidth();
        mInnerSize = res.getDimensionPixelSize(R.dimen.pin_progress_inner_size);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(circleColor);
        mCirclePaint.setAntiAlias(true);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setAntiAlias(true);

        bgCirclePaint = new Paint();
        bgCirclePaint.setAntiAlias(true);
        bgCirclePaint.setColor(res.getColor(R.color.pin_progress_default_progress_background));
        bgCirclePaint.setAlpha(alphaMax);

        innerCirclePaint = new Paint();
        innerCirclePaint.setAntiAlias(true);
        innerCirclePaint.setColor(res.getColor(R.color.pin_progress_default_progress_filled));
        innerCirclePaint.setAlpha(alphaMax);
    }

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


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(mDrawableSize, widthMeasureSpec),
                resolveSize(mDrawableSize, heightMeasureSpec));
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mShadowDrawable != null && mShadowDrawable.isStateful()) {
            mShadowDrawable.setState(getDrawableState());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int rectLeft = 0;
        int rectTop = 0;
        float tempRectLeft = -0.5f;
        float tempRectTop = -0.5f;
        int radius = 32;
        float strokeWidth = 2;

        mTempRect.set(rectLeft, rectTop, mDrawableSize, mDrawableSize);
        mTempRect.offset((getWidth() - mDrawableSize) / 2,
                (getHeight() - mDrawableSize) / 2);

        mTempRectF.set(tempRectLeft, tempRectTop, mInnerSize + 0.5f, mInnerSize + 0.5f);
        mTempRectF.offset((getWidth() - mInnerSize) / 2,
                (getHeight() - mInnerSize) / 2);

        // Draw background cycle

        canvas.drawCircle(mTempRect.width() / 2, mTempRect.height() / 2, radius,
                bgCirclePaint);

        if (mProgress != -1) {
            // Draw progress
            // canvas.drawArc(mTempRectF, 0, 360, true, mCirclePaint);
            canvas.drawArc(mTempRectF, -90, 360 * mProgress / mMax, true, mProgressPaint);
        }
        // Draw inner cycle
        radius = 25;
        canvas.drawCircle(mTempRect.width() / 2, mTempRect.height() / 2, radius,
                innerCirclePaint);
        // Draw arrow
        paint2.setStrokeWidth(strokeWidth);
        paint2.setColor(Color.WHITE);
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);
        paint2.setAntiAlias(true);
        paint2.setColor(Color.WHITE);
        if (mProgress != -1) {
            point1Draw.set(mTempRect.width() / 3 - 3,
                    mTempRect.height() / 2 + 6);
            point2Draw.set(mTempRect.width() / 2 + 14,
                    mTempRect.height() / 2 + 6);
            point3Draw.set(mTempRect.width() / 2, mTempRect.height() / 2 + 18);

            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(point1Draw.x, point1Draw.y);
            path.lineTo(point2Draw.x, point2Draw.y);
            path.lineTo(point3Draw.x, point3Draw.y);
            path.lineTo(point1Draw.x, point1Draw.y);
            path.close();

            canvas.drawPath(path, paint2);

            canvas.drawRect(point1Draw.x + 6, point1Draw.y - 13,
                    point2Draw.x - 6, point2Draw.y + 1, paint2);
            canvas.drawRect(point1Draw.x + 6, point1Draw.y - 19,
                    point2Draw.x - 6, point2Draw.y - 16, paint2);
            canvas.drawRect(point1Draw.x + 6, point1Draw.y - 24,
                    point2Draw.x - 6, point2Draw.y - 22, paint2);

            paint2.setColor(res
                    .getColor(R.color.pin_progress_default_progress_filled));

            canvas.drawRect(point1Draw.x - 1, point1Draw.y - 25,
                    point2Draw.x + 1, point2Draw.y - 25 + (37 * mProgress / mMax), paint2);
        } else {
            canvas.drawRect(mTempRect.width() / 2 - 13,
                    mTempRect.height() / 2 - 4, mTempRect.width() / 2 + 13,
                    mTempRect.height() / 2 + 4, paint2);
            canvas.drawRect(mTempRect.width() / 2 - 4,
                    mTempRect.height() / 2 - 13, mTempRect.width() / 2 + 4,
                    mTempRect.height() / 2 + 13, paint2);
        }
        mShadowDrawable.setBounds(mTempRect);
        mShadowDrawable.draw(canvas);
    }

    /**
     * A {@link Parcelable} representing the {@link ProgressButton}'s state.
     */
    public static class SavedState extends BaseSavedState {
        private int mProgress;
        private int mMax;

        /**
         * Constructor
         *
         * @param superState
         */
        public SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor
         *
         * @param in
         */
        private SavedState(Parcel in) {
            super(in);
            mProgress = in.readInt();
            mMax = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mProgress);
            out.writeInt(mMax);
        }

        /**
         * Object to save status
         */
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isSaveEnabled()) {
            SavedState ss = new SavedState(superState);
            ss.mMax = mMax;
            ss.mProgress = mProgress;
            return ss;
        }
        return superState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mMax = ss.mMax;
        mProgress = ss.mProgress;
    }

    /**
     * get Message from server
     *
     * @return
     */
    public BroadcastReceiver getMessageReceiver() {
        return mHandleMessageReceiver;
    }

    /**
     * hide the image
     */
    void hide() {
        setVisibility(View.GONE);
        if (context instanceof FullImageActivity) {
            ((FullImageActivity) context).setDownloadable();
        }
    }

    /**
     * get Newsletter object
     *
     * @return
     */
    public Newsletter getNewsletter() {
        return newsletter;
    }

    /**
     * set Newsletter object
     *
     * @param newsletter
     */
    public void setNewsletter(Newsletter newsletter) {
        this.newsletter = newsletter;
    }

    /**
     * receive message from server
     */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String progressValue = intent.getExtras().getString(
                    DownloadAsync.PROGRESS_VALUE);
            String newsletterId = intent.getExtras().getString(
                    Newsletter.NEWSLETTER_ID);
            if (newsletter != null && newsletterId != null
                    && newsletterId.length() > 0 && progressValue != null
                    && progressValue.length() > 0
                    && newsletterId.equals(newsletter.getId())) {
                int progress = Integer.parseInt(progressValue);
                if (progress == 100) {
                    hide();
                    return;
                }
                setProgress(progress);
            }
        }
    };

}
