/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.http;

import com.cmg.android.http.exception.UploaderException;
import com.cmg.mobile.shared.common.FileCommon;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator Hai Lu
 * @Last changed: $LastChangedDate$
 */
public class FileUploader {
    private static final Logger log = Logger.getLogger(FileUploader.class);

    /**
     * @param entity
     * @param uploadUrl
     * @return
     */
    public static String upload(HttpEntity entity, String uploadUrl) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(uploadUrl);
        try {
            log.info("Upload file to " + uploadUrl);
            httppost.setEntity(entity);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            if (entity != null) {
                String responseString = EntityUtils.toString(resEntity, "UTF-8");
                log.info("responseString: " + responseString);
                return responseString;
            }
        } catch (ClientProtocolException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * @param is
     * @param paras
     * @param uploadUrl
     * @return
     * @throws UploaderException
     */
    public static String upload(InputStream is, Map<String, String> paras, String uploadUrl) throws UploaderException {
        if (!paras.containsKey(FileCommon.PARA_FILE_NAME)) {
            throw new UploaderException("Missing parameter PARA_FILE_NAME");
        }
        SimpleMultipartEntity entity = new SimpleMultipartEntity();
        Iterator<String> keys = paras.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key != FileCommon.PARA_FILE_NAME && key != FileCommon.PARA_FILE_TYPE)
                entity.addPart(key, paras.get(key));
        }
        if (paras.containsKey(FileCommon.PARA_FILE_TYPE)) {
            entity.addPart(FileCommon.PARA_FILE_NAME, paras.get(FileCommon.PARA_FILE_NAME), is, paras.get(FileCommon.PARA_FILE_TYPE));
        } else {
            entity.addPart(FileCommon.PARA_FILE_NAME, paras.get(FileCommon.PARA_FILE_NAME), is);
        }

        return upload(entity, uploadUrl);
    }

    /**
     * @param file
     * @param paras
     * @param uploadUrl
     * @return
     * @throws UploaderException
     * @throws FileNotFoundException
     */
    public static String upload(File file, Map<String, String> paras, String uploadUrl) throws UploaderException, FileNotFoundException {
        return upload(new FileInputStream(file), paras, uploadUrl);
    }

    /**
     * @param paras
     * @param uploadUrl
     * @return
     * @throws UploaderException
     * @throws FileNotFoundException
     */
    public static String upload(Map<String, String> paras, String uploadUrl) throws UploaderException, FileNotFoundException {
        if (!paras.containsKey(FileCommon.PARA_FILE_PATH)) {
            throw new UploaderException("Missing parameter PARA_FILE_PATH");
        }
        return upload(new FileInputStream(paras.get(FileCommon.PARA_FILE_PATH)), paras, uploadUrl);
    }
}
