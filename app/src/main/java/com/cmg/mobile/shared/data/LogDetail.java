package com.cmg.mobile.shared.data;

import java.io.Serializable;
import java.util.Date;



public class LogDetail implements Serializable, Mirrorable {
	public enum Type {
		DOWNLOAD_NEWLETTER,
		OPEN_NEWSLETTER,
		READ_NEWSLETTER_PAGE
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5032204196288896124L;

	private String id;
	
	private int fileID;
	
	private int pageNumber;
	
	private String sessionName;
	
	private String typeAction;
	
	private String timeSpent;
	
	private Date timestamp;
	
	private String description;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getFileID() {
		return fileID;
	}

	public void setFileID(int fileID) {
		this.fileID = fileID;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getTypeAction() {
		return typeAction;
	}

	public void setTypeAction(String typeAction) {
		this.typeAction = typeAction;
	}

	public String getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(String timeSpent) {
		this.timeSpent = timeSpent;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public static String logType(Type logtype) {
		switch (logtype) {
		case DOWNLOAD_NEWLETTER: return "Download newsletter";
		case OPEN_NEWSLETTER: return "Open newsletter";
		case READ_NEWSLETTER_PAGE: return "Read newsletter page";
		default:
			return "Unknown type";
		}
	}
}
