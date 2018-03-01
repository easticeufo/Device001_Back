package com.madongfang.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(DeviceManagerPK.class)
public class DeviceManager {

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

	public Integer getSplitPercent() {
		return splitPercent;
	}

	public void setSplitPercent(Integer splitPercent) {
		this.splitPercent = splitPercent;
	}

	@Id
	@Column(length=32)
	private String deviceCode;
	
	@Id
	private Integer managerId;
	
	private Integer splitPercent; // 分账百分比，同一台设备的所有管理员的比例总和不能超过100
}
