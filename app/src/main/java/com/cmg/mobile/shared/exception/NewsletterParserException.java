/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.mobile.shared.exception;

/**
 * DOCME
 * 
 * @Creator LongNguyen
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */
public class NewsletterParserException extends Exception {
	public static final String NO_XML_RESOURCE_FOUND = "No xml resource found";
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param message
	 */
	public NewsletterParserException(String message) {
		super(message);
	}
}
