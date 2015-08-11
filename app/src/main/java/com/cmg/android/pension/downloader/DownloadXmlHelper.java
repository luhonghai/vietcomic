/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.downloader;

import android.content.Context;

import com.cmg.android.pension.database.DatabaseHandler;
import com.cmg.android.plmobile.R;
import com.cmg.android.util.AndroidCommonUtils;
import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.data.NewsletterCategory;
import com.cmg.mobile.shared.exception.NewsletterParserException;
import com.cmg.mobile.shared.util.FileHelper;
import com.cmg.mobile.shared.util.XmlParser;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Download xml controller
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class DownloadXmlHelper {
    private static Logger logger = Logger.getLogger(DownloadXmlHelper.class);

    private Context context;
    private String fileName;
    private String pathXml;
    private boolean isDownload = false;

    /**
     * Constructor
     *
     * @param c
     * @param p
     * @param f
     */
    public DownloadXmlHelper(Context c, String p, String f) {
        this.context = c;
        this.pathXml = p;
        this.fileName = f;
    }

    /**
     * Constructor
     *
     * @param c
     */
    public DownloadXmlHelper(Context c) {
        this.context = c;
    }

    /**
     * Download newsletters
     *
     * @param context
     * @return
     * @throws FileNotFoundException
     * @throws NewsletterParserException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static List<Newsletter> downloadNewsletters(final Context context)
            throws FileNotFoundException, NewsletterParserException,
            ParserConfigurationException, SAXException, IOException {
        DownloadXmlHelper dxu = new DownloadXmlHelper(context,
                AndroidCommonUtils.getXMLDataURL(
                        AndroidCommonUtils.PENSIONER_LETTER, context),
                AndroidCommonUtils.PENSIONER_LETTER);
        dxu.setDownload(true);
        dxu.downloadFileXml();
        List<Newsletter> pensioners = dxu
                .xmlParser(NewsletterCategory.PENSIONER);

        dxu = new DownloadXmlHelper(context, AndroidCommonUtils.getXMLDataURL(
                AndroidCommonUtils.EMPLOYEE_LETTER, context),
                AndroidCommonUtils.EMPLOYEE_LETTER);
        dxu.setDownload(true);
        dxu.downloadFileXml();
        List<Newsletter> employees = dxu.xmlParser(NewsletterCategory.EMPLOYEE);
        if (pensioners != null && pensioners.size() > 0) {
            pensioners.addAll(employees);
            return pensioners;
        } else {
            return employees;
        }
    }

    /**
     * Download XML file
     */
    public void downloadFileXml() {
        logger.info("Begin download xml " + pathXml + " File Name: " + fileName);
        try {
            FileHelper.downloadFile(pathXml,
                    context.openFileOutput(fileName, 0), isDownload);
        } catch (Exception e) {
            logger.error("Error when download file", e);
        }
    }

    /**
     * Parse XML to list Newsletter object
     *
     * @param catId
     * @return
     * @throws NewsletterParserException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public List<Newsletter> xmlParser(int catId)
            throws NewsletterParserException, ParserConfigurationException,
            SAXException, IOException, FileNotFoundException {

        DatabaseHandler database = new DatabaseHandler(context);
        database.addCategory(NewsletterCategory.PENSIONER,
                NewsletterCategory.STR_PENSIONER);
        database.addCategory(NewsletterCategory.EMPLOYEE,
                NewsletterCategory.STR_EMPLOYEE);
        String xmlRes = FileHelper.readFileContent(context
                .openFileInput(fileName));
        //Log.i("XML ",xmlRes);
        XmlParser parser = new XmlParser(xmlRes, catId, context.getResources()
                .getString(R.string.newsletter_image_url));
        List<Newsletter> list = null;
        list = parser.parse();
        if (list != null && list.size() > 0) {
            for (Newsletter n : list) {
                database.addNewsletter(n);
            }
        }
        return list;
    }

    /**
     * @return the isDownload
     */
    public boolean isDownload() {
        return isDownload;
    }

    /**
     * @param isDownload the isDownload to set
     */

    public void setDownload(boolean isDownload) {
        this.isDownload = isDownload;
    }
}
