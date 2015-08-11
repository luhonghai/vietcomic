/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cmg.mobile.shared.data.NewsletterCategory;

import org.apache.log4j.Logger;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class Preference {
    private final static Logger LOG = Logger.getLogger(Preference.class);

    public static final String CHECKBOX_NEWSLETTER_PENSIONER = "checkbox_newsletter_pensioner";
    public static final String CHECKBOX_NEWSLETTER_EMPLOYEE = "checkbox_newsletter_employee";

    public static final String CHANGELOG_PREFERENCE = "changelog_preferences";

    public static final String CHECKBOX_SEND_REPORT_PARENT = "parent_checkbox_report_parent";
    public static final String CHECKBOX_SEND_ERRORS = "child_checkbox_send_errors";
    public static final String CHECKBOX_SEND_STATISTICAL_DATA = "child_checkbox_send_statistics";

    public static final String LIST_VIEW_CHOICE = "list_view_choice";
    public static final String LIST_SORT_CHOICE = "list_sort_choice";

    public static final String GRID_VIEW = "grid_view";
    public static final String LIST_VIEW = "list_view";
    public static final String CAROUSEL_VIEW = "carousel_view";

    public static final String SORT_BY_SIZE = "sort_by_size";
    public static final String SORT_ALPHABETICALLY = "sort_alphabetically";
    public static final String SORT_BY_DATE = "sort_by_date";

    private String viewType = LIST_VIEW;
    private String sortType = SORT_BY_DATE;
    private int id = 1;
    @SuppressWarnings("unused")
    private boolean showPensioner;
    @SuppressWarnings("unused")
    private boolean showEmployee;
    private static Preference instance;

    private boolean sendReport;
    private boolean sendError;
    private boolean sendStatisticalData;

    // unsave propeties
    private boolean contrastOrder = false;
    private String strSearch = "";
    private int categoryId = NewsletterCategory.PENSIONER;

    private String searchPdfText;

    private static boolean flag = false;

    /**
     * Constructor
     */
    public Preference() {

    }

    /**
     * Tell system need to reload preference instance
     */
    public static void changeFlag() {
        flag = true;
    }

    /**
     * @param context
     * @return
     */
    public static Preference getInstance(Context context) {
        if (instance == null || flag) {
            init(context);
            flag = false;
        }
        return instance;
    }

    /**
     * initial data
     *
     * @param context
     */
    public static void init(Context context) {
        // DatabaseHandler databaseHandler = new DatabaseHandler(context);
        // instance = databaseHandler.getPreference();
        // if (instance == null) {
        // instance = new Preference();
        // databaseHandler.updatePreference(instance);
        // }
        // databaseHandler.recycle();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (instance == null) {
            instance = new Preference();
        }
        instance.setViewType(pref.getString(LIST_VIEW_CHOICE, LIST_VIEW));
        instance.setSortType(pref.getString(LIST_SORT_CHOICE, SORT_BY_DATE));
        instance.setShowEmployee(pref.getBoolean(CHECKBOX_NEWSLETTER_EMPLOYEE,
                false));
        instance.setShowPensioner(pref.getBoolean(
                CHECKBOX_NEWSLETTER_PENSIONER, true));
        instance.setSendReport(pref.getBoolean(CHECKBOX_SEND_REPORT_PARENT,
                true));

        instance.setSendError(pref.getBoolean(CHECKBOX_SEND_ERRORS, true));
        instance.setSendStatisticalData(pref.getBoolean(
                CHECKBOX_SEND_STATISTICAL_DATA, true));
    }

    /**
     * update data
     *
     * @param context
     */
    public static void update(Context context) {
        if (instance != null) {
            // DatabaseHandler databaseHandler = new DatabaseHandler(context);
            try {
                SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = pref.edit();
                // editor.putBoolean(CHECKBOX_NEWSLETTER_EMPLOYEE,
                // instance.isShowEmployee());
                // editor.putBoolean(CHECKBOX_NEWSLETTER_PENSIONER,
                // instance.isShowPensioner());
                editor.putBoolean(CHECKBOX_SEND_REPORT_PARENT,
                        instance.isSendReport());
                editor.putBoolean(CHECKBOX_SEND_ERRORS, instance.isSendError());
                editor.putBoolean(CHECKBOX_SEND_STATISTICAL_DATA,
                        instance.isSendStatisticalData());
                editor.putString(LIST_VIEW_CHOICE, instance.getViewType());
                editor.putString(LIST_SORT_CHOICE, instance.getSortType());
                editor.commit();
                // databaseHandler.updatePreference(instance);
            } catch (Exception ex) {
                LOG.error("Cannot update preference", ex);
            }
        }
    }

    /**
     * get view type
     *
     * @return
     */
    public String getViewType() {
        return viewType;
    }

    /**
     * set view type
     *
     * @param viewType
     */
    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    /**
     * get sort type
     *
     * @return
     */
    public String getSortType() {
        return sortType;
    }

    /**
     * set sort type
     *
     * @param sortType
     */
    public void setSortType(String sortType) {
        if (this.sortType != null && sortType != null
                && this.sortType.equals(sortType)) {
            contrastOrder = !contrastOrder;
        }
        this.sortType = sortType;
    }

    /**
     * get id
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * set id
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * check show pensioner tab
     *
     * @return
     */
    public boolean isShowPensioner() {
        return true;
        //return showPensioner;
    }

    /**
     * set show pensioner
     *
     * @param showPensioner
     */
    public void setShowPensioner(boolean showPensioner) {
        this.showPensioner = showPensioner;
    }

    /**
     * check show employee tab
     *
     * @return
     */
    public boolean isShowEmployee() {
        return false;
        //return showEmployee;
    }

    /**
     * set show employee
     *
     * @param showEmployee
     */
    public void setShowEmployee(boolean showEmployee) {
        this.showEmployee = showEmployee;
    }

    /**
     * check send report
     *
     * @return
     */
    public boolean isSendReport() {
        return sendReport;
    }

    /**
     * set send report
     *
     * @param sendReport
     */
    public void setSendReport(boolean sendReport) {
        this.sendReport = sendReport;
    }

    /**
     * check send error
     *
     * @return
     */
    public boolean isSendError() {
        return sendError;
    }

    /**
     * set send error
     *
     * @param sendError
     */
    public void setSendError(boolean sendError) {
        this.sendError = sendError;
    }

    /**
     * check send statistic data
     *
     * @return
     */
    public boolean isSendStatisticalData() {
        return sendStatisticalData;
    }

    /**
     * set send statistic data
     *
     * @param sendStatisticalData
     */
    public void setSendStatisticalData(boolean sendStatisticalData) {
        this.sendStatisticalData = sendStatisticalData;
    }

    /**
     * check the arrange order
     *
     * @return
     */
    public boolean isContrastOrder() {
        return contrastOrder;
    }

    /**
     * set arrange order
     *
     * @param contrastOrder
     */
    public void setContrastOrder(boolean contrastOrder) {
        this.contrastOrder = contrastOrder;
    }

    /**
     * get String for searching
     *
     * @return
     */
    public String getStrSearch() {
        return strSearch;
    }

    /**
     * set String for searching
     *
     * @param strSearch
     */
    public void setStrSearch(String strSearch) {
        this.strSearch = strSearch;
    }

    /**
     * get category id
     *
     * @return
     */
    public int getCategoryId() {
        if (categoryId == NewsletterCategory.FAVORITES)
            return categoryId;
        if (isShowEmployee() && isShowPensioner()) {
            return categoryId;
        } else if (!isShowEmployee()) {
            return NewsletterCategory.PENSIONER;
        } else if (!isShowPensioner()) {
            return NewsletterCategory.EMPLOYEE;
        } else {
            return categoryId;
        }
    }

    /**
     * set cetegory id
     *
     * @param categoryId
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * @return the searchPdfText
     */
    public String getSearchPdfText() {
        return searchPdfText;
    }

    /**
     * @param searchPdfText the searchPdfText to set
     */

    public void setSearchPdfText(String searchPdfText) {
        this.searchPdfText = searchPdfText;
    }

}
