/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.data;

import com.cmg.android.preference.Preference;
import com.cmg.mobile.shared.data.Newsletter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class NewsletterHelper {
    public static final String NEWSLETTER_DATE_FORMAT = "MMMM dd, yyyy";

    /**
     * sort newsletter
     *
     * @param newsletters
     * @param sortType
     * @param isContrastOrder
     */
    public static void sortNewsletter(final List<Newsletter> newsletters,
                                      final String sortType, final boolean isContrastOrder) {
        final SimpleDateFormat sdf = new SimpleDateFormat(
                NEWSLETTER_DATE_FORMAT, Locale.US);
        Collections.sort(newsletters, new Comparator<Newsletter>() {
            @Override
            public int compare(Newsletter lhs, Newsletter rhs) {

                if (sortType.equals(Preference.SORT_ALPHABETICALLY)) {
                    if (isContrastOrder) {
                        return rhs.getTitle().compareTo(lhs.getTitle());
                    } else {
                        return lhs.getTitle().compareTo(rhs.getTitle());
                    }

                } else if (sortType.equals(Preference.SORT_BY_DATE)) {
                    try {
                        Date date1 = sdf.parse(lhs.getDate());
                        Date date2 = sdf.parse(rhs.getDate());
                        if (isContrastOrder) {
                            return date2.compareTo(date1);
                        } else {
                            return date1.compareTo(date2);
                        }

                    } catch (ParseException e) {
                        return 0;
                    }
                } else if (sortType.equals(Preference.SORT_BY_SIZE)) {
                    if (lhs.getSize() == rhs.getSize()) {
                        return 0;
                    }
                    if (isContrastOrder) {
                        return (lhs.getSize() > rhs.getSize() ? 1 : -1);
                    } else {
                        return (lhs.getSize() < rhs.getSize() ? 1 : -1);
                    }

                }
                return 0;
            }
        });
    }
}
