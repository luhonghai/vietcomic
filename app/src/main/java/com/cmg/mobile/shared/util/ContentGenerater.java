/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.mobile.shared.util;

import java.util.ArrayList;
import java.util.List;

import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.data.Version;

/**
 * DOCME
 * 
 * @Creator LongNguyen
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */
public class ContentGenerater {
	/**
	 * 
	 * @param newsletter
	 * @return
	 */
	public static String generateShareInfo(Newsletter newsletter) {
		return new StringBuffer()
		// .append("Newsletter from Pensionline: ")
		// .append(newsletter.getTitle()).append("\nDownload link: ")
				.append(newsletter.getFileUrl())
				// .append("\nSummary: " + newsletter.getSummary())
				.toString();
	}

	/**
	 * send new version message from server
	 * 
	 * @param ver
	 * @param currentVersion
	 * @return
	 */
	public static String generateNewVersionMessasge(Version ver,
			String currentVersion) {
		return new StringBuffer().append("A new version is available ")
				.append(ver.getVersionName()).append(" (Current version ")
				.append(currentVersion)
				.append("). Do you want to update new version?").toString();
	}

	/**
	 * initial URL images for pages
	 * 
	 * @return
	 */
	public static List<String> createListOfPage(Newsletter nLetter) {
		List<String> pages = new ArrayList<String>();
		String url = nLetter.getImageUrl().substring(0,
				nLetter.getImageUrl().length() - 12);
		for (int i = 0; i < nLetter.getPage(); i++) {
			String pageUrl = url + (i + 1) + "_mobile.png";
			pages.add(pageUrl);
		}
		return pages;
	}
}
