package com.halosolutions.mangaworld;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.halosolutions.mangaworld.BuildConfig;
import com.halosolutions.mangaworld.R;
import com.halosolutions.mangaworld.adapter.ComicBookCursorAdapter;
import com.halosolutions.mangaworld.comic.ComicBook;
import com.halosolutions.mangaworld.fragment.AllComicFragment;
import com.halosolutions.mangaworld.fragment.DownloadedComicFragment;
import com.halosolutions.mangaworld.fragment.FavoriteComicFragment;
import com.halosolutions.mangaworld.fragment.HotComicFragment;
import com.halosolutions.mangaworld.fragment.NewComicFragment;
import com.halosolutions.mangaworld.fragment.WatchedComicFragment;
import com.halosolutions.mangaworld.sqlite.ext.ComicBookDBAdapter;
import com.halosolutions.mangaworld.util.SimpleAppLog;
import com.rey.material.app.ToolbarManager;
import com.rey.material.drawable.ThemeDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.SnackBar;
import com.rey.material.widget.TabPageIndicator;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends BaseActivity implements ToolbarManager.OnToolbarGroupChangedListener,
		SearchView.OnQueryTextListener,
		SearchView.OnSuggestionListener{

	private DrawerLayout dl_navigator;
	private FrameLayout fl_drawer;
	private ListView lv_drawer;
	private CustomViewPager vp;
	private TabPageIndicator tpi;

	private DrawerAdapter mDrawerAdapter;
	private PagerAdapter mPagerAdapter;
	
	private Toolbar mToolbar;
    private ToolbarManager mToolbarManager;
    private SnackBar mSnackBar;

	private SearchView searchView;

	private ComicBookDBAdapter dbAdapter;

	private ComicBookCursorAdapter adapter;

	private Tab[] mItems = new Tab[]{Tab.HOT,Tab.NEW,Tab.FAVORITE, Tab.DOWNLOADED, Tab.WATCHED, Tab.ALL };

	private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
				
		dl_navigator = (DrawerLayout)findViewById(R.id.main_dl);
		fl_drawer = (FrameLayout)findViewById(R.id.main_fl_drawer);
		lv_drawer = (ListView)findViewById(R.id.main_lv_drawer);
		mToolbar = (Toolbar)findViewById(R.id.main_toolbar);
		vp = (CustomViewPager)findViewById(R.id.main_vp);
		tpi = (TabPageIndicator)findViewById(R.id.main_tpi);
        mSnackBar = (SnackBar)findViewById(R.id.main_sn);

        mToolbarManager = new ToolbarManager(getDelegate(), mToolbar, R.id.tb_group_main, R.style.ToolbarRippleStyle, R.anim.abc_fade_in, R.anim.abc_fade_out);
        mToolbarManager.setNavigationManager(new ToolbarManager.ThemableNavigationManager(R.array.navigation_drawer, getSupportFragmentManager(), mToolbar, dl_navigator) {
            @Override
            public void onNavigationClick() {
				SimpleAppLog.debug("onNavigationClick");
                if(mToolbarManager.getCurrentGroup() != R.id.tb_group_main)
                    mToolbarManager.setCurrentGroup(R.id.tb_group_main);
                else
                    dl_navigator.openDrawer(GravityCompat.START);
            }

            @Override
            public boolean isBackState() {
                return super.isBackState() || mToolbarManager.getCurrentGroup() != R.id.tb_group_main;
            }

            @Override
            protected boolean shouldSyncDrawerSlidingProgress() {
                return super.shouldSyncDrawerSlidingProgress() && mToolbarManager.getCurrentGroup() == R.id.tb_group_main;
            }

        });
        mToolbarManager.registerOnToolbarGroupChangedListener(this);
		
		mDrawerAdapter = new DrawerAdapter(this);
		lv_drawer.setAdapter(mDrawerAdapter);
		
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mItems);
		vp.setAdapter(mPagerAdapter);
		tpi.setViewPager(vp);
		tpi.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mToolbar.setTitle(mItems[position].name);
				mDrawerAdapter.setSelected(mItems[position]);
				mSnackBar.dismiss();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}

		});
		mToolbar.setTitle(Tab.HOT.name);
        mDrawerAdapter.setSelected(Tab.HOT);
		vp.setCurrentItem(0);

        ViewUtil.setBackground(getWindow().getDecorView(), new ThemeDrawable(R.array.bg_window));
        ViewUtil.setBackground(mToolbar, new ThemeDrawable(R.array.bg_toolbar));
		dbAdapter = new ComicBookDBAdapter(this);
		setSupportActionBar(mToolbar);


		mAdView = (AdView) findViewById(R.id.adView);
		if (mAdView != null && BuildConfig.IS_FREE) {
			mAdView.setVisibility(View.VISIBLE);
			AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice("7898660F3293A11BB56ED538658F9B0F")
					.build();
			mAdView.loadAd(adRequest);
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        mToolbarManager.createMenu(R.menu.menu_main);
		SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
		searchView = (SearchView) MenuItemCompat.getActionView(mToolbar.getMenu().findItem(R.id.action_search));
		if (null != searchView) {
			searchView.setIconified(true);
			try {
				dbAdapter.open();
			} catch (Exception e) {
				SimpleAppLog.error("Could not open database", e);
			}
			searchView.setQueryHint("Tìm kiếm truyện");
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(true);
			searchView.setOnQueryTextListener(this);
			searchView.setOnSuggestionListener(this);
			try {
				adapter = new ComicBookCursorAdapter(this, dbAdapter.cursorSearch(""), R.layout.comic_item_lite);
				searchView.setSuggestionsAdapter(adapter);
			} catch (Exception e) {
				SimpleAppLog.error("Could not set suggestion adapter",e);
			}
//			SearchView.SearchAutoComplete autoCompleteTextView = (SearchView.SearchAutoComplete)
//					searchView.findViewById(R.id.search_src_text);
//
//			if (autoCompleteTextView != null) {
//				try {
//					autoCompleteTextView.setDropDownBackgroundResource(R.color.colorPrimary);
//				} catch (Exception e) {}
//			}
		} else {
			SimpleAppLog.error("No search view");
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdView != null)
			mAdView.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAdView != null)
			mAdView.pause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbAdapter != null) {
			dbAdapter.close();
		}
		if (mAdView != null)
			mAdView.destroy();
	}

	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mToolbarManager.onPrepareMenu();

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		SimpleAppLog.debug("onOptionsItemSelected " + item.getItemId());
        switch (item.getItemId()){
			case android.R.id.home:
				if (dl_navigator.isDrawerOpen(GravityCompat.START)) {
					dl_navigator.closeDrawer(fl_drawer);
				} else {
					dl_navigator.openDrawer(GravityCompat.START);
				}
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onToolbarGroupChanged(int oldGroupId, int groupId) {
        mToolbarManager.notifyNavigationStateChanged();
    }

    public SnackBar getSnackBar(){
        return mSnackBar;
    }

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	private Runnable updateQueryRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				updateQuery();
			} catch (Exception e) {
				SimpleAppLog.error("could not update suggestion query",e);
			}
		}
	};

	private void updateQuery() throws Exception {
		try {
			dbAdapter.close();
			dbAdapter.open();
		} catch (Exception e) {

		}
		final Cursor c = dbAdapter.cursorSearch(searchText);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				searchView.getSuggestionsAdapter().changeCursor(c);
			}
		});
	}

	private Handler updateQueryHandler = new Handler();

	private String searchText;

	@Override
	public boolean onQueryTextChange(String s) {
		searchText = s;
		updateQueryHandler.removeCallbacks(updateQueryRunnable);
		updateQueryHandler.postDelayed(updateQueryRunnable, 200);
		return true;
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		//Toast.makeText(this, "Select position: " + position, Toast.LENGTH_LONG).show();
		//startActivity(new Intent(this, DetailActivity.class));
		return true;
	}

	@Override
	public boolean onSuggestionClick(int position) {
		if (searchView != null) {
			ComicBook comicBook = dbAdapter.toObject(
					(Cursor) searchView.getSuggestionsAdapter().getItem(position));
			Gson gson = new Gson();
			Intent intent = new Intent(this, DetailActivity.class);
			intent.putExtra(ComicBook.class.getName(), gson.toJson(comicBook));
			startActivity(intent);
		}
		return true;
	}

	public enum Tab {
	    HOT("Truyện HOT"),
		NEW("Truyện mới"),
        ALL("Toàn bộ"),
        FAVORITE("Yêu thích"),
		DOWNLOADED("Đã tải"),
		WATCHED("Đã xem");

	    private final String name;       

	    private Tab(String s) {
	        name = s;
	    }

	    public boolean equalsName(String otherName){
	        return (otherName != null) && name.equals(otherName);
	    }

	    public String toString(){
	       return name;
	    }

	}
	
	class DrawerAdapter extends BaseAdapter implements View.OnClickListener {

		private Tab mSelectedTab;
		private int mTextColorLight;
        private int mBackgroundColorLight;

        public DrawerAdapter(Context context){
            mTextColorLight = context.getResources().getColor(R.color.abc_primary_text_material_light);
            mBackgroundColorLight = ThemeUtil.colorPrimary(context, 0);
        }

        public void setSelected(Tab tab){
			if(tab != mSelectedTab){
				mSelectedTab = tab;
				notifyDataSetInvalidated();
			}
		}
		
		public Tab getSelectedTab(){
			return mSelectedTab;
		}
		
		@Override
		public int getCount() {
			return mItems.length;
		}

		@Override
		public Object getItem(int position) {
			return mItems[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
                v = LayoutInflater.from(MainActivity.this).inflate(R.layout.row_drawer, null);
                v.setOnClickListener(this);
            }
            v.setTag(position);
			Tab tab = (Tab)getItem(position);
			((TextView)v).setText(tab.toString());
			
			if(tab == mSelectedTab) {
                v.setBackgroundColor(mBackgroundColorLight);
                ((TextView)v).setTextColor(0xFFFFFFFF);
            }
			else {
                v.setBackgroundResource(0);
                ((TextView)v).setTextColor(mTextColorLight);
            }
			
			return v;
		}

        @Override
        public void onClick(View v) {
            int position = (Integer)v.getTag();
            vp.setCurrentItem(position);
            dl_navigator.closeDrawer(fl_drawer);
        }
    }
	
	private static class PagerAdapter extends FragmentStatePagerAdapter {
		
		Fragment[] mFragments;
		Tab[] mTabs; 
				
		private static final Field sActiveField;
		
		static {
			Field f = null;
			try {
				Class<?> c = Class.forName("android.support.v4.app.FragmentManagerImpl");
				f = c.getDeclaredField("mActive");
				f.setAccessible(true);   
			} catch (Exception e) {}
			
			sActiveField = f;
		}
		
        public PagerAdapter(FragmentManager fm, Tab[] tabs) {
            super(fm);    
            mTabs = tabs;
            mFragments = new Fragment[mTabs.length];
       
            
            //dirty way to get reference of cached fragment
            try{
    			ArrayList<Fragment> mActive = (ArrayList<Fragment>)sActiveField.get(fm);
    			if(mActive != null){
    				for(Fragment fragment : mActive){
    					if(fragment instanceof HotComicFragment)
    						setFragment(Tab.HOT, fragment);
                        else if(fragment instanceof AllComicFragment)
                            setFragment(Tab.ALL, fragment);
                        else if(fragment instanceof FavoriteComicFragment)
                            setFragment(Tab.FAVORITE, fragment);
						else if(fragment instanceof NewComicFragment)
							setFragment(Tab.NEW, fragment);
						else if(fragment instanceof WatchedComicFragment)
							setFragment(Tab.WATCHED, fragment);
						else if(fragment instanceof DownloadedComicFragment)
							setFragment(Tab.DOWNLOADED, fragment);
    				}
    			}
    		}
    		catch(Exception e){}
        }
        
        private void setFragment(Tab tab, Fragment f){
        	for(int i = 0; i < mTabs.length; i++)
        		if(mTabs[i] == tab){
        			mFragments[i] = f;
        			break;
        		}
        }
        
		@Override
        public Fragment getItem(int position) {
			if(mFragments[position] == null){
	        	switch (mTabs[position]) {
					case HOT:
						mFragments[position] = new HotComicFragment();
						break;
                    case ALL:
                        mFragments[position] = new AllComicFragment();
                        break;
                    case FAVORITE:
                        mFragments[position] = new FavoriteComicFragment();
                        break;
					case NEW:
						mFragments[position] = new NewComicFragment();
						break;
					case WATCHED:
						mFragments[position] = new WatchedComicFragment();
						break;
					case DOWNLOADED:
						mFragments[position] = new DownloadedComicFragment();
						break;
				}
			}
						
			return mFragments[position];		
        }
				
		@Override
		public CharSequence getPageTitle(int position) {
			return mTabs[position].toString().toUpperCase();
		}

		@Override
        public int getCount() {
            return mFragments.length;
        }
    }
}
