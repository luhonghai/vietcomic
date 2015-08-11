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

import com.cmg.android.preference.Preference;
import com.cmg.android.util.AndroidCommonUtils;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class NewsletterFragmentAdapter extends FragmentStatePagerAdapter {
    public static final String LETTER_ARG = "letter_type";
    public static final String SEARCH_ARG = "search_arg";
    private final int tabCount;
    private final Preference pref;

    /**
     * Constructor
     *
     * @param fm
     * @param tabCount
     * @param pref
     */
    public NewsletterFragmentAdapter(FragmentManager fm, int tabCount,
                                     Preference pref) {
        super(fm);
        this.tabCount = tabCount;
        this.pref = pref;
    }

    @Override
    public Fragment getItem(int pos) {
        if (pos == 0) {
            Fragment fragment = new NewsletterFragment();
            Bundle args = new Bundle();
            if (pref.isShowEmployee() && pref.isShowPensioner()) {
                args.putString(LETTER_ARG,
                        pos == 0 ? AndroidCommonUtils.PENSIONER_LETTER
                                : AndroidCommonUtils.EMPLOYEE_LETTER);
            } else if (pref.isShowEmployee() && !pref.isShowPensioner()) {
                args.putString(LETTER_ARG, AndroidCommonUtils.EMPLOYEE_LETTER);
            } else if (!pref.isShowEmployee() && pref.isShowPensioner()) {
                args.putString(LETTER_ARG, AndroidCommonUtils.PENSIONER_LETTER);
            }

            fragment.setArguments(args);
            return fragment;
        } else {
            Fragment fragment = new FavoritesFragment();
            return fragment;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public int getItemPosition(Object object) {
        // return super.getItemPosition(object);
        return POSITION_NONE;
    }

}
