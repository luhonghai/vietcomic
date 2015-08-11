/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.mobile.shared.data;

import java.io.Serializable;

/**
 * DOCME
 * 
 * @Creator LongNguyen
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */
public class Device implements Serializable, Mirrorable {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 5792388422615392786L;
	private String regId;

	/**
	 * Constructor
	 */
	public Device() {

	}

	/**
	 * Constructor
	 * @param regId
	 */
	public Device(String regId) {
		this.regId = regId;
	}

	/**
	 * get Register id
	 * @return
	 */
	public String getId() {
		return regId;
	}

	/**
	 * set Register id
	 * @param regId
	 */
	public void setId(String regId) {
		this.regId = regId;
	}
}
