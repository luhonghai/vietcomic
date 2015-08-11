/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.cmg.mobile.shared.data.Newsletter;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class NewsletterDetailFragmentAdapter extends FragmentStatePagerAdapter {
    private static Logger log = Logger.getLogger(NewsletterDetailFragmentAdapter.class);
    private final List<Newsletter> newsletters;

    /**
     * Constructor
     *
     * @param fm
     * @param newsletters
     */
    public NewsletterDetailFragmentAdapter(FragmentManager fm, List<Newsletter> newsletters) {
        super(fm);
        this.newsletters = newsletters;
    }

    @Override
    public Fragment getItem(int position) {
        log.debug("start create instance NewsletterDetailFragment. pos: "
                + position);
        Fragment fragment = new NewsletterDetailFragment();
        Bundle args = new Bundle();
        args.putString(Newsletter.NEWSLETTER_ID, newsletters.get(position)
                .getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return newsletters.size();
    }

}
