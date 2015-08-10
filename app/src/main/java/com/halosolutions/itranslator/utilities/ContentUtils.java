/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.halosolutions.itranslator.utilities;

import android.content.Context;
import android.content.Intent;

import java.util.Iterator;
import java.util.Map;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class ContentUtils {

    public static final String KEY_SCREENSHOOT = "ADD_SCREEN_SHOOT";

    /**
     * Default constructor
     */
    public ContentUtils() {

    }


    public static String generatePreviewHtmlFeedback(Map<String, String> infos) {
        StringBuffer html = new StringBuffer();
        Iterator<String> keys = infos.keySet().iterator();
        while (keys.hasNext()) {
            String k = keys.next();
            if (!k.equals(KEY_SCREENSHOOT)) {
                html.append("<h4 style=\"color:#4acd00\">" + k + "</h4>");
                html.append("<p><label>" + infos.get(k) + "</label></p>");
                html.append("<hr>");
            }
        }

        if (infos.containsKey(KEY_SCREENSHOOT)) {
            html.append("<h4 style=\"color:#4acd00\">Screenshoot</h4>");
            html.append("<p><img style=\"width:80%;border:1px solid #4acd00;\" src='" + infos.get(KEY_SCREENSHOOT) + "' /></p>");
            html.append("<hr>");
        }

        return html.toString();
    }
}
