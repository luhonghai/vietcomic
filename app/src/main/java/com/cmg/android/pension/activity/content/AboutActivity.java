/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity.content;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

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
public class AboutActivity extends ContentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView cmg = (TextView) findViewById(R.id.cmg_site);
        cmg.setText(Html
                .fromHtml("<a href=\"https://sites.google.com/a/c-mg.com/temp/mobile/mobile-pdf-reader-license\">Claybourne McGregor</a>"));
        cmg.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
