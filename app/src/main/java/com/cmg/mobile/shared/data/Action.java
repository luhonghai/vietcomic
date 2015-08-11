package com.cmg.mobile.shared.data;

import java.io.Serializable;

public class Action implements Serializable, Mirrorable {
	
	private static final long serialVersionUID = 7143643387667486369L;

	private String id;
	
	private int type;
	
	private String Description;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

}
