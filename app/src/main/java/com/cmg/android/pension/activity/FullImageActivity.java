/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.common.Environment;
import com.cmg.android.pension.activity.coverflow.CoverFlow;
import com.cmg.android.pension.activity.coverflow.CoverflowPageAdapter;
import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.pension.downloader.task.DownloadAsync;
import com.cmg.android.pension.view.DownloadImage;
import com.cmg.android.pension.view.ProgressButton;
import com.cmg.android.plmobile.MainActivity;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.ContentUtils;
import com.cmg.android.util.ExceptionHandler;
import com.cmg.android.util.FileUtils;
import com.cmg.mobile.shared.data.Newsletter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
public class FullImageActivity extends SherlockActivity {
    private ImageView imageView;
    private Newsletter newsletter = null;
    private String pdfLocal = "";
    private boolean isDownloaded = false;
    private FrameLayout frame;
    @SuppressWarnings("unused")
    private List<Newsletter> list;
    private ProgressButton btnDownload;
    private DownloadImage imgDownload;
    private Menu menu;
    private DatabaseHandler db;
    private static final Map<String, Class<? extends Activity>> EXTENSION_TO_ACTIVITY = new HashMap<String, Class<? extends Activity>>();

//    static {
//        EXTENSION_TO_ACTIVITY.put("pdf", PdfViewerFragmentActivity.class);
//        EXTENSION_TO_ACTIVITY.put("djvu", DjvuViewerFragmentActivity.class);
//        EXTENSION_TO_ACTIVITY.put("djv", DjvuViewerFragmentActivity.class);
//    }

    /**
     * @param extension
     * @return
     */
    public static Class<?> getBookReader(String extension) {
        return EXTENSION_TO_ACTIVITY.get(extension);
    }

    /**
     * select menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * create menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.share_activity_menu, menu);
        this.menu = menu;
        initShareMenu();
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * create view
     */
    @SuppressWarnings({"deprecation", "unused"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Environment.isEnableAnalytics(getResources())) {
        }

        db = new DatabaseHandler(this);
        list = new ArrayList<Newsletter>();
        setContentView(R.layout.newsletter_detail);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle localBundle = getIntent().getExtras();
        // get intent data
        newsletter = db.getById(localBundle.getString(Newsletter.NEWSLETTER_ID));
        list = db.getAllNewslettersByCategory(newsletter.getCategoryId(), "");

        setData(newsletter);
        CoverflowPageAdapter adapter = new CoverflowPageAdapter(this,
                newsletter);
        CoverFlow coverFlow;
        coverFlow = new CoverFlow(this);
        //coverFlow.setAdapter(adapter);
        coverFlow.setSpacing(-20);
        coverFlow.setSelection(1, true);
        coverFlow.setAnimationDuration(1000);
        coverFlow.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int itemNo,
                                    long arg3) {
                File file = new File(pdfLocal);
                if (file.exists()) {
                    Uri uri = Uri.fromFile(file);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    Bundle localBundle = new Bundle();
                    localBundle.putParcelable("Newsletter", (Parcelable) newsletter);
                    intent.putExtras(localBundle);

                    Bundle currentPage = new Bundle();
                    currentPage.putInt("page", itemNo + 1);
                    intent.putExtras(currentPage);

                    String uriString = uri.toString();
                    String extension = uriString.substring(uriString.lastIndexOf('.') + 1);
                    intent.setClass(FullImageActivity.this, EXTENSION_TO_ACTIVITY.get(extension));
                    startActivity(intent);
                }
            }
        });

        frame = (FrameLayout) findViewById(R.id.frame_carousel);
        frame.addView(coverFlow);
        if (!isDownloaded) {
            frame.setVisibility(View.INVISIBLE);
        } else {
            frame.setVisibility(View.VISIBLE);
        }

        final FullImageActivity currentContext = this;
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDownloaded) {
                    File file = new File(pdfLocal);
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        Bundle localBundle = new Bundle();
                        localBundle.putParcelable("Newsletter", (Parcelable) newsletter);
                        intent.putExtras(localBundle);
                        String uriString = uri.toString();
                        String extension = uriString.substring(uriString.lastIndexOf('.') + 1);
                        intent.setClass(currentContext, EXTENSION_TO_ACTIVITY.get(extension));

                        if (Environment.isEnableAnalytics(getResources())) {
                        }

                        startActivity(intent);
                    }
                }
            }
        });
    }

    /**
     * check if PDF can be download
     */
    public void setDownloadable() {
        isDownloaded = true;
    }

    /**
     * download image via URL
     *
     * @param url
     * @return
     */
    public static Bitmap downloadImage(String url) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url)
                    .getContent());
        } catch (Exception e) {
            //Silent
        }

        return bitmap;
    }

    /**
     * destroy view
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }

        if (imgDownload != null && imgDownload.getMessageReceiver() != null) {
            try {
                unregisterReceiver(imgDownload.getMessageReceiver());
            } catch (Exception ex) {

            }
        }
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().clearMemoryCache();
        }
    }

    /**
     * control download action
     *
     * @author LongNguyen
     */
    class OnDownloadButtonClick implements OnClickListener {
        private final ProgressButton btn;
        private final Newsletter newsletter;
        private final Context context;

        /**
         * Constructor
         *
         * @param btn
         * @param newsletter
         * @param context
         */
        public OnDownloadButtonClick(ProgressButton btn, Newsletter newsletter,
                                     Context context) {
            this.btn = btn;
            this.newsletter = newsletter;
            this.context = context;
        }

        /**
         * onclick function
         */
        @Override
        public void onClick(View v) {
            if (btn.getProgress() < 1) {
                btn.setProgress(0);
                DownloadAsync da = new DownloadAsync(newsletter, context);
                da.execute();
            }
        }

    }

    /**
     * initial data
     *
     * @param newsletter
     */
    public void setData(Newsletter newsletter) {
        this.newsletter = newsletter;
        if (btnDownload != null) {
            try {
                unregisterReceiver(btnDownload.getMessageReceiver());
            } catch (Exception ex) {

            }
        }
        DatabaseHandler db = new DatabaseHandler(this);
        isDownloaded = db.checkIsDownloaded(newsletter.getId());
        imageView = (ImageView) findViewById(R.id.full_image_view);
        pdfLocal = FileUtils.getPdfFile(newsletter, this);
        ImageLoaderHelper.getImageLoader(this).displayImage(newsletter.getImageUrl(), imageView);
        TextView titleView = (TextView) findViewById(R.id.txtTitle);
        titleView.setText(newsletter.getTitle());

        TextView dateView = (TextView) findViewById(R.id.txtDate);
        dateView.setText(newsletter.getDate());

        TextView desView = (TextView) findViewById(R.id.txtDescription);
        desView.setMovementMethod(new ScrollingMovementMethod());
        desView.setText(newsletter.getSummary());
        FrameLayout frameDownload = (FrameLayout) findViewById(R.id.frm_progress_download);
        frameDownload.removeAllViews();

        if (!isDownloaded) {

            imgDownload = new DownloadImage(this, newsletter);
            frameDownload.addView(imgDownload);
            registerReceiver(imgDownload.getMessageReceiver(),
                    new IntentFilter(DownloadAsync.DISPLAY_MESSAGE_ACTION + "@"
                            + newsletter.getId()));
        }
        if (menu != null) {
            initShareMenu();
        }
    }

    /**
     * initial Share menu
     */
    public void initShareMenu() {
        MenuItem actionItem = menu.findItem(R.id.action_share);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();

        try {
            actionProvider.setShareIntent(ContentUtils
                    .createShareIntent(newsletter));
        } catch (Exception e) {
            //Silent
        }
    }

    /**
     * set frame visible
     */
    public void setVisible() {
        frame.setVisibility(View.VISIBLE);
    }
}
