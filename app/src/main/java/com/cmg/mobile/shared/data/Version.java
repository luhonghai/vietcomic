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
import java.util.Date;

/**
 * DOCME
 * 
 * @Creator LongNguyen
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */
public class Version implements Serializable, Mirrorable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3156222511427181525L;

	private String id;
	
	private String versionName;

	private String projectName;

	private String changeLog;

	private String apkFile;

	private String readMe;

	private Date timestamp;

	/**
	 * Constructor
	 */
	public Version() {
		timestamp = new Date(System.currentTimeMillis());
	}

	/**
	 * read version from maven build
	 * @param ver
	 */
	public void swap(final Version ver) {
		projectName = ver.projectName;
		changeLog = ver.changeLog;
		apkFile = ver.apkFile;
		readMe = ver.readMe;
		timestamp = ver.timestamp;
	}

	/**
	 * get Project Name
	 * @return
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * set Project Name
	 * @param projectName
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * get Version Name
	 * @return
	 */
	public String getVersionName() {
		return versionName;
	}

	/**
	 * set Version Name
	 * @param versionName
	 */
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	/**
	 * @return the changeLog
	 */
	public String getChangeLog() {
		return changeLog;
	}

	/**
	 * @param changeLog
	 *            the changeLog to set
	 */

	public void setChangeLog(String changeLog) {
		this.changeLog = changeLog;
	}

	/**
	 * @return the apkFile
	 */
	public String getApkFile() {
		return apkFile;
	}

	/**
	 * @param apkFile
	 *            the apkFile to set
	 */

	public void setApkFile(String apkFile) {
		this.apkFile = apkFile;
	}

	/**
	 * @return the readMe
	 */
	public String getReadMe() {
		return readMe;
	}

	/**
	 * @param readMe
	 *            the readMe to set
	 */
	public void setReadMe(String readMe) {
		this.readMe = readMe;
	}

	/**
	 * get Time stamp
	 * @return
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * set Time stamp
	 * @param timestamp
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * (non-Javadoc)
	 * @see com.cmg.mobile.shared.data.Mirrorable#getId() 
	 */
	@Override
	public String getId() {
		return id;
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
