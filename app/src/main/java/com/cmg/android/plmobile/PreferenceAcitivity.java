/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.plmobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.webkit.WebView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cmg.android.cmgpdf.CmgPDFActivity;
import com.cmg.android.common.CommonIntent;
import com.cmg.android.pension.activity.NewsletterDetailActivity;
import com.cmg.android.pension.activity.content.AboutActivity;
import com.cmg.android.pension.activity.content.FeedbackActivity;
import com.cmg.android.pension.activity.content.HelpActivity;
import com.cmg.android.pension.activity.content.ShareActivity;
import com.cmg.android.preference.Preference;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.android.util.ContentUtils;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class PreferenceAcitivity extends SherlockPreferenceActivity implements
        OnSharedPreferenceChangeListener {

    // private CheckBoxPreference mCheckBoxPensioner;
    // private CheckBoxPreference mCheckBoxEmployee;
    private Context context;
    @SuppressWarnings("unused")
    private CheckBoxPreference mCheckBoxReportParent;
    @SuppressWarnings("unused")
    private CheckBoxPreference mCheckBoxErrors;
    @SuppressWarnings("unused")
    private CheckBoxPreference mCheckBoxStatisticalData;

    private ListPreference mListViewType;
    private ListPreference mListSortType;
    private android.preference.Preference preChangelog;
    private boolean isChange = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        AndroidCommonUtils.takeScreenShot(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.preferences);

        // mCheckBoxPensioner = (CheckBoxPreference) getPreferenceScreen()
        // .findPreference(Preference.CHECKBOX_NEWSLETTER_PENSIONER);
        //
        // mCheckBoxEmployee = (CheckBoxPreference) getPreferenceScreen()
        // .findPreference(Preference.CHECKBOX_NEWSLETTER_EMPLOYEE);
        preChangelog = (android.preference.Preference) getPreferenceScreen()
                .findPreference(Preference.CHANGELOG_PREFERENCE);
        preChangelog
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(
                            android.preference.Preference preference) {
                        String html = ContentUtils
                                .generateChangelogHTML(context);
                        View view = getLayoutInflater().inflate(
                                R.layout.change_log, null);
                        ((WebView) view.findViewById(R.id.webView))
                                .loadDataWithBaseURL(html, html, "text/html",
                                        null, null);
                        final AlertDialog dialogChangeLog = new AlertDialog.Builder(
                                context)
                                .setTitle(
                                        getResources().getString(
                                                R.string.title_changelog))
                                .setView(view)
                                .setNegativeButton(
                                        getResources().getString(
                                                R.string.dialog_close),
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                dialog.dismiss();
                                            }
                                        }).create();

                        dialogChangeLog.show();
                        return true;
                    }
                });
        mCheckBoxReportParent = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(Preference.CHECKBOX_SEND_REPORT_PARENT);

        mCheckBoxErrors = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(Preference.CHECKBOX_SEND_ERRORS);

        mCheckBoxStatisticalData = (CheckBoxPreference) getPreferenceScreen()
                .findPreference(Preference.CHECKBOX_SEND_STATISTICAL_DATA);

        mListSortType = (ListPreference) getPreferenceScreen().findPreference(
                Preference.LIST_SORT_CHOICE);

        mListViewType = (ListPreference) getPreferenceScreen().findPreference(
                Preference.LIST_VIEW_CHOICE);

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);

        updatePreferenceView(pref, Preference.LIST_SORT_CHOICE);
        updatePreferenceView(pref, Preference.LIST_VIEW_CHOICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.default_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isChange) {

                    String fromActivity = getIntent().getExtras().getString(CommonIntent.FROM_ACTIVITY_CLASS);
                    if (fromActivity.equalsIgnoreCase(CmgPDFActivity.class.getName())) {
                        Intent intent = new Intent(NewsletterDetailActivity.REFRESH_MAIN_ACTIVITY_MESSAGE);
                        intent.putExtra(NewsletterDetailActivity.NOTIFY_REFESH_MAIN_ACTIVITY, "true");
                        context.sendBroadcast(intent);
                        super.onBackPressed();
                        return true;
                    }
                    Class<?> fromClass = null;
                    try {
                        fromClass = Class.forName(fromActivity);
                    } catch (Exception e) {
                        fromClass = MainActivity.class;
                    }
                    Intent upIntent = new Intent(this, fromClass);
                    Bundle bundle = getIntent().getExtras();
                    upIntent.putExtras(bundle);

                    if (getIntent().getData() != null) {
                        upIntent.setData(getIntent().getData());
                    }

                    if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                        TaskStackBuilder.from(this).addNextIntent(upIntent)
                                .startActivities();
                        finish();
                    } else {
                        NavUtils.navigateUpTo(this, upIntent);
                    }
                } else {
                    super.onBackPressed();
                }
                return true;
            case R.id.action_about:
                startActivity(AboutActivity.class);
                break;
            case R.id.action_help:
                startActivity(HelpActivity.class);
                break;
            case R.id.action_share_app:
                startActivity(ShareActivity.class);
                break;
            case R.id.action_preferences:
                startActivity(PreferenceAcitivity.class);
                break;
            case R.id.action_feedback:
                startActivity(FeedbackActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startActivity(Class<?> c) {
        Intent intent = new Intent();
        Bundle bundle = getIntent().getExtras();
        //bundle.putString(CommonIntent.FROM_ACTIVITY_CLASS, getIntent().getExtras().getString(CommonIntent.FROM_ACTIVITY_CLASS));
        intent.setClass(this, c);
        intent.putExtras(bundle);
        if (getIntent().getData() != null) {
            intent.setData(getIntent().getData());
        }
        startActivity(intent);
    }

    /**
     * update preference view
     *
     * @param sharedPreferences
     * @param key
     */
    void updatePreferenceView(SharedPreferences sharedPreferences, String key) {
        // if (key.equals(Preference.CHECKBOX_NEWSLETTER_EMPLOYEE)) {
        // if (!mCheckBoxEmployee.isChecked()
        // && !mCheckBoxPensioner.isChecked()) {
        // mCheckBoxPensioner.setChecked(true);
        // }
        // } else if (key.equals(Preference.CHECKBOX_NEWSLETTER_PENSIONER)) {
        // if (!mCheckBoxEmployee.isChecked()
        // && !mCheckBoxPensioner.isChecked()) {
        // mCheckBoxEmployee.setChecked(true);
        // }
        // } else
        if (key.equals(Preference.LIST_SORT_CHOICE)) {
            String sortType = sharedPreferences.getString(
                    Preference.LIST_SORT_CHOICE, Preference.SORT_BY_DATE);
            if (sortType.equals(Preference.SORT_BY_DATE)) {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    mListSortType.setIcon(android.R.drawable.ic_menu_today);
                }
                mListSortType.setTitle(R.string.action_bar_sort_date);
            } else if (sortType.equals(Preference.SORT_BY_SIZE)) {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    mListSortType
                            .setIcon(android.R.drawable.ic_menu_sort_by_size);
                }
                mListSortType.setTitle(R.string.action_bar_sort_size);
            } else if (sortType.equals(Preference.SORT_ALPHABETICALLY)) {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    mListSortType
                            .setIcon(android.R.drawable.ic_menu_sort_alphabetically);
                }
                mListSortType.setTitle(R.string.action_bar_sort_alpha);
            }
        } else if (key.equals(Preference.LIST_VIEW_CHOICE)) {
            String viewType = sharedPreferences.getString(
                    Preference.LIST_VIEW_CHOICE, Preference.LIST_VIEW);
            if (viewType.equals(Preference.LIST_VIEW)) {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    mListViewType.setIcon(R.drawable.ic_menu_list_view);
                }
                mListViewType.setTitle(R.string.action_bar_list_view);
            } else if (viewType.equals(Preference.GRID_VIEW)) {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    mListViewType.setIcon(R.drawable.ic_menu_grid_view);
                }
                mListViewType.setTitle(R.string.action_bar_grid_view);
            } else if (viewType.equals(Preference.CAROUSEL_VIEW)) {
                if (android.os.Build.VERSION.SDK_INT >= 11) {
                    mListViewType.setIcon(R.drawable.ic_menu_carousel_view);
                }
                mListViewType.setTitle(R.string.action_bar_carousel_view);
            }
        }
    }

    /**
     * start main activity
     */
    void startMainActivity() {
        Intent upIntent = new Intent(this, MainActivity.class);

        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.from(this).addNextIntent(upIntent)
                    .startActivities();
            finish();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }


    /**
     * update the preference changes
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updatePreferenceView(sharedPreferences, key);
        Preference.changeFlag();
        if (!isChange)
            isChange = true;
    }
}
