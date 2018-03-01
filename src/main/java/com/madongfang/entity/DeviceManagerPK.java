package com.madongfang.entity;

import java.io.Serializable;

public class DeviceManagerPK implements Serializable {

	public DeviceManagerPK() {
		super();
	}

	public DeviceManagerPK(String deviceCode, Integer managerId) {
		super();
		this.deviceCode = deviceCode;
		this.managerId = managerId;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public Integer getManagerId() {
		return managerId;
	}

	public void setManagerId(Integer managerId) {
		this.managerId = managerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deviceCode == null) ? 0 : deviceCode.hashCode());
		result = prime * result + ((managerId == null) ? 0 : managerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceManagerPK other = (DeviceManagerPK) obj;
		if (deviceCode == null) {
			if (other.deviceCode != null)
				return false;
		} else if (!deviceCode.equals(other.deviceCode))
			return false;
		if (managerId == null) {
			if (other.managerId != null)
				return false;
		} else if (!managerId.equals(other.managerId))
			return false;
		return true;
	}

	private static final long serialVersionUID = 1L;

	private String deviceCode;
	
	private Integer managerId;
}
