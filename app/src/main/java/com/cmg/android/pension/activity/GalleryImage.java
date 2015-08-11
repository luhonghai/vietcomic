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
import android.view.MotionEvent;
import android.widget.ImageView;

import com.cmg.mobile.shared.data.Newsletter;

import java.util.List;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class GalleryImage extends ImageView {
    public static final long DOUBLE_TAP_TIME = 300;
    private final Context context;
    private final List<Newsletter> newsLetter;
    private final int position;
    private long lastDownEventTime = 0;

    /**
     * Constructor
     *
     * @param context
     * @param newsLetter
     * @param position
     */
    public GalleryImage(Context context, List<Newsletter> newsLetter,
                        int position) {
        super(context);
        this.context = context;
        this.newsLetter = newsLetter;
        this.position = position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getEventTime() - lastDownEventTime < DOUBLE_TAP_TIME) {
                ((FullImageActivity) context).setData(newsLetter.get(position));
            } else {
                lastDownEventTime = ev.getEventTime();
            }
        }

        return super.onTouchEvent(ev);
    }

}
