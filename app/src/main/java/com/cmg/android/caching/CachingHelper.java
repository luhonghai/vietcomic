/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.caching;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class CachingHelper {
    private final Map<String, SoftReference<FrameLayout>> cache = new HashMap<String, SoftReference<FrameLayout>>();
    private final List<FrameLayout> queue = Collections
            .synchronizedList(new LinkedList<FrameLayout>());
    final static int SIZELIMIT = 8;

    /**
     * put value, key to HashMap
     *
     * @param value
     * @param id
     */
    public void put(FrameLayout value, String id) {
        synchronized (cache) {
            if (cache.containsKey(id)) {
                push(cache.get(id).get());
                cache.remove(id);
            }
            cache.put(id, new SoftReference<FrameLayout>(value));
        }

    }

    /**
     * queue value
     *
     * @param value
     */
    private void push(FrameLayout value) {
        // recycleTitle(value);
        queue.add(value);
        if (queue.size() > SIZELIMIT) {
            recycleTitle(queue.get(0));
            queue.remove(0);
        }
    }

    /**
     * clear queue
     */
    public void clear() {
        synchronized (cache) {
            if (!cache.isEmpty()) {
                Iterator<String> keys = cache.keySet().iterator();
                while (keys.hasNext()) {
                    recycleTitle(cache.get(keys.next()).get());
                }
            }
            cache.clear();
        }
        Iterator<FrameLayout> fms = queue.iterator();
        while (fms.hasNext()) {
            recycleTitle(fms.next());
        }
        queue.clear();
    }

    /**
     * get size
     *
     * @return
     */
    public int getSize() {
        return cache.size();
    }

    /**
     * clear cache
     *
     * @param frmTitle
     */
    public static void recycleTitle(final FrameLayout frmTitle) {
        if (frmTitle == null) {
            return;
        }
        if (frmTitle.getChildCount() > 0) {
            TextView txtTitle = (TextView) frmTitle.getChildAt(0);
            if (txtTitle.getBackground() != null) {
                if (txtTitle.getBackground().getTransparentRegion() != null) {
                    txtTitle.getBackground().getTransparentRegion().setEmpty();
                }
                txtTitle.getBackground().setCallback(null);
            }
        }
        if (frmTitle != null && frmTitle.getChildCount() > 0) {
            frmTitle.removeAllViews();
        }
        if (frmTitle != null && frmTitle.getBackground() != null) {
            frmTitle.getBackground().setCallback(null);
        }
    }

    /**
     * clear thumbnail bitmap
     *
     * @param image
     */
    public static void recycleThumbnail(final ImageView image) {
        Drawable drawable = image.getDrawable();
        if (drawable != null) {
            drawable.setCallback(null);
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                bitmap.recycle();
            }

        }
    }
}
