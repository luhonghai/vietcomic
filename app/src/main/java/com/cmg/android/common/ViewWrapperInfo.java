/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.common;

import android.os.Bundle;

/**
 * Created by cmg on 10/23/13.
 */
public class ViewWrapperInfo {

    public static final String SHARE_TEXT = "SHARE_TEXT";

    public static final String SHARE_SUBJECT = "SHARE_SUBJECT";

    public static final String MAIN_CLASS = "MAIN_CLASS";

    public static final String DETAIL_CLASS = "DETAIL_CLASS";

    public static final String ITEM_ID_VALUE = "ITEM_ID_VALUE";

    public static final String ITEM_ID_KEY = "ITEM_ID_KEY";

    public static final String ITEM_PAGE = "ITEM_PAGE";

    private Class<?> mainClass;

    private Class<?> detailClass;

    private int page;

    private boolean valid = false;

    private String shareText;

    private String shareSubject;

    private String itemIdKey;

    private String itemIdValue;

    private final Bundle bundle;

    public ViewWrapperInfo(Bundle bundle) {
        this.bundle = bundle;
    }

    public void init() {
        if (bundle != null) {
            String strMainClass = bundle.getString(MAIN_CLASS);
            if (strMainClass != null) {
                try {
                    mainClass = Class.forName(strMainClass);
                    valid = true;
                } catch (ClassNotFoundException e) {

                }
            }

            String strDetailClass = bundle.getString(DETAIL_CLASS);

            try {
                if (strDetailClass != null) {
                    detailClass = Class.forName(strDetailClass);
                }
            } catch (ClassNotFoundException e) {

            }
            shareText = bundle.getString(SHARE_TEXT);
            shareSubject = bundle.getString(SHARE_SUBJECT);
            itemIdKey = bundle.getString(ITEM_ID_KEY);
            itemIdValue = bundle.getString(ITEM_ID_VALUE);
            page = bundle.getInt(ITEM_PAGE);
            if (shareText != null && shareSubject != null
                    && itemIdKey != null && itemIdValue != null
                    && page != 0) {
                valid = true;
            }
        }
    }

    public int getPage() {
        return page;
    }

    public Class<?> getMainClass() {
        return mainClass;
    }

    public Class<?> getDetailClass() {
        return detailClass;
    }

    public String getShareText() {
        return shareText;
    }

    public String getShareSubject() {
        return shareSubject;
    }

    public boolean isValid() {
        return valid;
    }

    public String getItemIdKey() {
        return itemIdKey;
    }

    public void setItemIdKey(String itemIdKey) {
        this.itemIdKey = itemIdKey;
    }

    public String getItemIdValue() {
        return itemIdValue;
    }

    public void setItemIdValue(String itemIdValue) {
        this.itemIdValue = itemIdValue;
    }

}
