/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity.content;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.cmg.android.caching.ImageLoaderHelper;
import com.cmg.android.plmobile.ContentActivity;
import com.cmg.android.plmobile.R;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class ShareActivity extends ContentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ImageView qrcode = (ImageView) findViewById(R.id.share_qrcode);
        ImageLoaderHelper.getImageLoader(this).displayImage(
                getResources().getString(R.string.share_qrcode_url), qrcode);

//        TextView address = (TextView) findViewById(R.id.txtLink);
//        String shareUrl = getResources().getString(
//                R.string.share_pensionline_url);
//        address.setText(Html.fromHtml("<a href=\"" + shareUrl + "\">here</a>"));
//        address.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.share_activity_menu, menu);
        MenuItem actionItem = menu.findItem(R.id.action_share);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem
                .getActionProvider();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Newsletters");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getResources().getString(R.string.share_pensionline_url));
        actionProvider.setShareIntent(shareIntent);
        return true;
    }
}
