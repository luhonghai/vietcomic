/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.plmobile;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.common.Environment;
import com.cmg.android.pension.activity.NewsletterFragmentAdapter;
import com.cmg.android.preference.Preference;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.mobile.shared.data.NewsletterCategory;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCME
 * 
 * @Creator LongNguyen
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */
public class MainActivity extends ContentFragmentActivity implements
		ActionBar.TabListener, OnQueryTextListener {
	private static Logger log = Logger.getLogger(MainActivity.class);
	private NewsletterFragmentAdapter newsletterAdapter;
	private ViewPager mViewPager;
	private Menu menu;
	private String search;
	private boolean inSearch = false;
	private String sQuery;
	private long lastPressBack = -1;
	private final long DOUBLE_PRESS_BACK_TIME = 2000;
	private int tabCount = 0;
	private SearchView searchView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		log.info("onCreate main activity");
		setContentView(R.layout.newsletter_main_activity);
		final ActionBar ab = getSupportActionBar();
		// set up tabs nav

//		if (Preference.getInstance(this).isShowPensioner()) {
//			ab.addTab(ab.newTab().setText(NewsletterCategory.STR_PENSIONER)
//					.setTabListener(this));
//
//		}
//		if (Preference.getInstance(this).isShowEmployee()) {
//			ab.addTab(ab.newTab().setText(getResources().getString(R.string.newsletter_employee))
//					.setTabListener(this));
//			tabCount++;
//		}

        createTab(R.layout.tab_pensioner, R.id.tab_pensioner, NewsletterCategory.STR_PENSIONER);
        createTab(R.layout.tab_favorites, R.id.tab_favorites, NewsletterCategory.STR_FAVORITES);

//        ab.addTab(ab.newTab().setText(NewsletterCategory.STR_FAVORITES)
//                .setTabListener(this));
        updateTabIcon();
		newsletterAdapter = new NewsletterFragmentAdapter(getSupportFragmentManager(), tabCount,
				Preference.getInstance(this));
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(newsletterAdapter);
		if (tabCount > 1) {
            int pos = Preference.getInstance(
                    this.getApplicationContext()).getCategoryId() == NewsletterCategory.PENSIONER ? 0 : 1;
			ab.selectTab(ab.getTabAt(pos));
            toggleMenuItem(pos);
		}
        final Context mContext = this;
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int pos) {
				ab.selectTab(ab.getTabAt(pos));
                toggleMenuItem(pos);
                Preference.getInstance(mContext).setCategoryId(pos == 0 ?  NewsletterCategory.PENSIONER : NewsletterCategory.FAVORITES);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		int orient = getResources().getConfiguration().orientation;
		switch (orient) {
		case Configuration.ORIENTATION_LANDSCAPE:
			ab.setDisplayUseLogoEnabled(true);
			ab.setDisplayShowTitleEnabled(false);
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			ab.setDisplayUseLogoEnabled(false);
			ab.setDisplayShowTitleEnabled(true);
			break;
		default:
		}
		if (ab.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}
		List<String> images = new ArrayList<String>();
		try {
			images.add(getResources().getString(R.string.share_qrcode_url));
			ImageLoaderHelper.silentLoadImageToDiscCache(images);
		} catch (Exception ex) {
			//silent
		}
	}

    public void createTab(int view, int titleView, String title) {
        ActionBar.Tab tab = getSupportActionBar().newTab();
        tab.setTabListener(this);
        tab.setCustomView(view);
        getSupportActionBar().addTab(tab);
        tabCount++;
        View textView = findViewById(titleView);
        if (textView != null) {
            ((TextView) textView).setText(title);
        }

    }

	/**
	 * update category by position
	 * @param pos
	 */
	void updateCategory(int pos) {
		if (tabCount > 1) {
			Preference.getInstance(this.getApplicationContext()).setCategoryId(
					pos == 0 ? NewsletterCategory.PENSIONER
							: NewsletterCategory.EMPLOYEE);
		} else {
			Preference.getInstance(this.getApplicationContext()).setCategoryId(
					Preference.getInstance(this.getApplicationContext())
							.isShowPensioner() ? NewsletterCategory.PENSIONER
							: NewsletterCategory.EMPLOYEE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.newsletter_main_menu, menu);
		searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setQueryHint("Search newsletter");
		searchView.setOnQueryTextListener(this);
		search = Preference.getInstance(this.getApplicationContext()).getStrSearch();	
		sQuery = search;	
			
		if (search != null && search.length() > 0) {
			searchView.setIconified(false);
			//menu.findItem(R.id.action_search).expandActionView(); 
			//searchView.performClick();
			searchView.setQuery(search, false);
			inSearch = true;
		} else {
			inSearch = false;
		}
		searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						inSearch = hasFocus;
						if (!inSearch && sQuery != null
								&& sQuery.trim().length() == 0
								&& search != null && search.length() > 0) {
							// reset list
							search = "";
							notifyDataSetChanged();
						}
					}
				});
		this.menu = menu;
		updateMenuIcon();
		
		
		
		return super.onCreateOptionsMenu(menu);
	}

    private void toggleMenuItem(int pos) {
        if (menu == null)
            return;
        final MenuItem viewMenu = (MenuItem) menu
                .findItem(R.id.action_switch_view);
        viewMenu.setVisible(pos == 0);
    }

    private void updateTabIcon() {
        View view = findViewById(R.id.tab_pensioner_icon);
        if (view == null)
            return;
        ImageView imageView = (ImageView) view;
        if (Preference.getInstance(getApplicationContext()).getViewType()
                .equals(Preference.GRID_VIEW)) {
            imageView.setImageResource(R.drawable.tab_grid_view_icon);
        } else if (Preference.getInstance(getApplicationContext())
                .getViewType().equals(Preference.LIST_VIEW)) {
            imageView.setImageResource(R.drawable.tab_list_view_icon);
        } else if (Preference.getInstance(getApplicationContext())
                .getViewType().equals(Preference.CAROUSEL_VIEW)) {
            imageView.setImageResource(R.drawable.tab_carousel_view_icon);
        }
    }
	/**
	 * update menu icon
	 */
	private void updateMenuIcon() {
        updateTabIcon();
        if (menu == null)
            return;
		final MenuItem sortMenu = (MenuItem) menu.findItem(R.id.action_sort);
		final MenuItem viewMenu = (MenuItem) menu
				.findItem(R.id.action_switch_view);
		if (Preference.getInstance(getApplicationContext()).getViewType()
				.equals(Preference.GRID_VIEW)) {
			viewMenu.setIcon(R.drawable.ic_menu_grid_view);
		} else if (Preference.getInstance(getApplicationContext())
				.getViewType().equals(Preference.LIST_VIEW)) {
			viewMenu.setIcon(R.drawable.ic_menu_list_view);
		} else if (Preference.getInstance(getApplicationContext())
				.getViewType().equals(Preference.CAROUSEL_VIEW)) {
			viewMenu.setIcon(R.drawable.ic_menu_carousel_view);
		}
		if (Preference.getInstance(getApplicationContext()).getSortType()
				.equals(Preference.SORT_ALPHABETICALLY)) {

			sortMenu.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		} else if (Preference.getInstance(getApplicationContext())
				.getSortType().equals(Preference.SORT_BY_DATE)) {

			sortMenu.setIcon(android.R.drawable.ic_menu_today);
		} else if (Preference.getInstance(getApplicationContext())
				.getSortType().equals(Preference.SORT_BY_SIZE)) {

			sortMenu.setIcon(android.R.drawable.ic_menu_sort_by_size);
		}
	}

	/**
	 * control search results
	 */
	private void notifyDataSetChanged() {
		Preference.getInstance(this).setStrSearch(search);
		newsletterAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return false;
		case R.id.action_sort_size:
			Preference.getInstance(getApplicationContext()).setSortType(
					Preference.SORT_BY_SIZE);
			updateMenuIcon();
			notifyDataSetChanged();
			break;
		case R.id.action_sort_alpha:
			Preference.getInstance(getApplicationContext()).setSortType(
					Preference.SORT_ALPHABETICALLY);
			updateMenuIcon();
			notifyDataSetChanged();
			break;
		case R.id.action_sort_date:
			Preference.getInstance(getApplicationContext()).setSortType(
					Preference.SORT_BY_DATE);
			updateMenuIcon();
			notifyDataSetChanged();
			break;
		case R.id.action_switch_grid:
			Preference.getInstance(getApplicationContext()).setViewType(
					Preference.GRID_VIEW);
			updateMenuIcon();
			notifyDataSetChanged();
			break;
		case R.id.action_switch_list:
			Preference.getInstance(getApplicationContext()).setViewType(
					Preference.LIST_VIEW);
			updateMenuIcon();
			notifyDataSetChanged();
			break;
		case R.id.action_switch_carosuel:
			Preference.getInstance(getApplicationContext()).setViewType(
					Preference.CAROUSEL_VIEW);
			updateMenuIcon();
			notifyDataSetChanged();
			break;		
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * clear cache
	 */
	public void recycle() {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recycle();
	}

	/**
	 * choose tab function
	 */
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        int pos = tab.getPosition();
        toggleMenuItem(pos);
        Preference.getInstance(this).setCategoryId(pos == 0 ?  NewsletterCategory.PENSIONER : NewsletterCategory.FAVORITES);
		mViewPager.setCurrentItem(pos);
	}

	/**
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabUnselected(com.actionbarsherlock.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
	 */
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

	}

	/**
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabReselected(com.actionbarsherlock.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
	 */
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

	}

	@Override
	public boolean onQueryTextChange(String s) {
		sQuery = s;
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String s) {
		log.debug("search newsletter: " + s);
		search = s;
		AndroidCommonUtils.hideSoftKeyboard(this);
		notifyDataSetChanged();
		return false;
	}

	@Override
	public void onBackPressed() {
		long now = System.currentTimeMillis();
		if (lastPressBack == -1 || now - lastPressBack > DOUBLE_PRESS_BACK_TIME) {
			Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG).show();
			lastPressBack = now;
			return;
		} else {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			System.exit(0);
		}
	}
}
