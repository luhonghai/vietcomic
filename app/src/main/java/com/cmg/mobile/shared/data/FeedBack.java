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
import java.util.Map;

import com.cmg.mobile.shared.common.DeviceInfoCommon;

/** 
 * DOCME
 * 
 * @Creator Hai Lu
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */

public class FeedBack implements Serializable, Mirrorable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String imei;
	
	private String acc;
	
	private String description;
	
	private String appVersion;
	
	private String pathScreenShot;
	
	private String model;
	
	private String osVersion;
	
	private String osApiVersion;
	
	private String deviceName;
	
	public FeedBack(Map<String, String> params){
		this.acc = params.get(DeviceInfoCommon.ACCOUNT);
		this.description = params.get(DeviceInfoCommon.FEEDBACK_DESCRIPTION);
		this.appVersion = params.get(DeviceInfoCommon.APP_VERSION);
		this.osVersion = params.get(DeviceInfoCommon.OS_VERSION);
		this.osApiVersion = params.get(DeviceInfoCommon.OS_API_LEVEL);
		this.deviceName = params.get(DeviceInfoCommon.DEVICE_NAME);
		this.model = params.get(DeviceInfoCommon.MODEL);
		this.imei = params.get(DeviceInfoCommon.IMEI);
	}
	
	/** 
	 * @return the imei 
	 */
	public String getImei() {
		return imei;
	}

	/** 
	 * @param imei the imei to set 
	 */
	
	public void setImei(String imei) {
		this.imei = imei;
	}

	/** 
	 * @return the acc 
	 */
	public String getAcc() {
		return acc;
	}

	/** 
	 * @param acc the acc to set 
	 */
	
	public void setAcc(String acc) {
		this.acc = acc;
	}

	/** 
	 * @return the description 
	 */
	public String getDescription() {
		return description;
	}

	/** 
	 * @param description the description to set 
	 */
	
	public void setDescription(String description) {
		this.description = description;
	}

	/** 
	 * @return the appVersion 
	 */
	public String getAppVersion() {
		return appVersion;
	}

	/** 
	 * @param appVersion the appVersion to set 
	 */
	
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	/** 
	 * @return the pathScreenShot 
	 */
	public String getPathScreenShot() {
		return pathScreenShot;
	}

	/** 
	 * @param pathScreenShot the pathScreenShot to set 
	 */
	
	public void setPathScreenShot(String pathScreenShot) {
		this.pathScreenShot = pathScreenShot;
	}

	/** 
	 * @return the model 
	 */
	public String getModel() {
		return model;
	}

	/** 
	 * @param model the model to set 
	 */
	
	public void setModel(String model) {
		this.model = model;
	}

	/** 
	 * @return the osVersion 
	 */
	public String getOsVersion() {
		return osVersion;
	}

	/** 
	 * @param osVersion the osVersion to set 
	 */
	
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	/** 
	 * @return the osApiVersion 
	 */
	public String getOsApiVersion() {
		return osApiVersion;
	}

	/** 
	 * @param osApiVersion the osApiVersion to set 
	 */
	
	public void setOsApiVersion(String osApiVersion) {
		this.osApiVersion = osApiVersion;
	}

	/** 
	 * @return the deviceName 
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/** 
	 * @param deviceName the deviceName to set 
	 */
	
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	

	/**
	 * (non-Javadoc)
	 * @see com.cmg.mobile.shared.data.Mirrorable#getId() 
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/**
	 * (non-Javadoc)
	 * @see com.cmg.mobile.shared.data.Mirrorable#setId(String)
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	
}
