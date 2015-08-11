/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.mobile.shared.data;

/** 
 * DOCME
 * 
 * @Creator Hai Lu
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */

/*
 *  Define the Cloneable object DTO <-> JDO by JSON parser (Jackson or Gson API)
 */
public interface Mirrorable {
	/**
	 *  Get ID of object
	 * @return
	 */
	public String getId();
	/**
	 * Set ID to JDO clone able object
	 * @param id
	 */
	public void setId(String id);
}
