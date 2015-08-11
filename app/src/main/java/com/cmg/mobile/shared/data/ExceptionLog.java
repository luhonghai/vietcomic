package com.cmg.mobile.shared.data;

import java.io.Serializable;
import java.util.Date;

public class ExceptionLog implements Serializable, Mirrorable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -563773982567273991L;

	private String id;
	
	private String sessionName;

	private String description;

	private Date dateCreated;
	
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}
