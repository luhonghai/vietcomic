/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.util.StringUtils;


import java.io.File;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class FileUtils {

    public static final String PDF_FOLDER = "newsletters";
    public static final String SCREENSHOOTS_FOLDER = "screenshoots";

    public static String getFilePath(String folderName, String fileName,
                                     Context context) {
        return getFolderPath(folderName, context) + File.separator + fileName;
    }

    public static String getFolderPath(String folderName, Context context) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(Environment.getExternalStorageDirectory().toString())
                    .append(File.separator).append(folderName);
            return sb.toString();
        } catch (Exception ex) {
            SimpleAppLog.error("Error when get PDF folder path", ex);
            return "";
        }
    }

    /**
     * get PDF folder path
     *
     * @param type
     * @param context
     * @return
     */
    public static String getPdfFolder(String type, Context context) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(Utilities.getPackageInfo(context).applicationInfo.dataDir)
                    .append(File.separator).append(PDF_FOLDER)
                    .append(File.separator).append(type);
            return sb.toString();
        } catch (Exception ex) {
            SimpleAppLog.error("Error when get PDF folder path", ex);
            return "";
        }
    }

    /**
     * get PDF file path
     *
     * @param newsletter
     * @param context
     * @return
     */
    public static String getPdfFile(Newsletter newsletter, Context context) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(Utilities.getPackageInfo(context).applicationInfo.dataDir)
                    .append(File.separator).append(PDF_FOLDER)
                    .append(File.separator).append(newsletter.getType())
                    .append(File.separator)
                    .append(StringUtils.getFileName(newsletter.getFileUrl()));
            return sb.toString();
        } catch (Exception ex) {
            SimpleAppLog.error("Error when get PDF file path", ex);
            return "";
        }
    }

    /**
     * get XML folder path
     *
     * @param context
     * @return
     * @throws NameNotFoundException
     */
    public static String getXMLFolder(Context context)
            throws NameNotFoundException {
        return context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0).applicationInfo.dataDir
                + File.separator + "files" + File.separator;
    }

    /**
     * get download folder path
     *
     * @return
     */
    public static String getDownloadFolder() {
        try {
            return Environment.getExternalStorageDirectory() + File.separator
                    + "download" + File.separator;
        } catch (Exception ex) {
            SimpleAppLog.error("Error when get download folder", ex);
            return "";
        }
    }
}
