package com.cmg.mobile.shared.data;

import java.io.Serializable;
import java.util.Date;

public class Session implements Serializable, Mirrorable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5133054455421716755L;

	private String id;

	private String sessionName;

	private String imei;

	private Date dateCreated;
	
	public Session(){
		dateCreated = new Date(System.currentTimeMillis());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
}
