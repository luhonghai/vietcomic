/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.mobile.shared.util;

/**
 * 
 * DOCME
 * 
 * @Creator Hai Lu
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */
public final class StringUtils {
	
	/**
	 * Constructor
	 */
	private StringUtils(){
		
	}
	
	/**
	 * get File Name
	 * @param url
	 * @return
	 */
	public static String getFileName(String url) {
		if (url != null && url.length() > 0) {
			url = url.replaceAll("\\\\", "/");
			return url.substring(url.lastIndexOf("/") + 1, url.length());
		}
		return "";
	}

	/**
	 * standard file path
	 * @param root
	 * @param sub
	 * @return
	 */
	public static String mergeFilePath(String root, String sub) {
		root = root.replaceAll("\\\\", "/");
		root = root.endsWith("/") ? root : (root + "/");
		sub = sub.replaceAll("\\\\", "/");
		sub = sub.startsWith("/") ? sub.substring(1, sub.length()) : sub;
		return root + sub;
	}

}
