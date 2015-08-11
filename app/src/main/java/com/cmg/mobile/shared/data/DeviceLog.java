package com.cmg.mobile.shared.data;

import java.io.Serializable;

public class DeviceLog implements Serializable, Mirrorable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2888333995785476738L;

	private String id;

	private String imei;

	private String name;

	private String ram;
	
	public DeviceLog(){
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRam() {
		return ram;
	}

	public void setRam(String ram) {
		this.ram = ram;
	}

}
