package com.madongfang.api.deviceserver;

public class StartApi {
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Integer getPlugId() {
		return plugId;
	}

	public void setPlugId(Integer plugId) {
		this.plugId = plugId;
	}

	public Integer getLimitPower() {
		return limitPower;
	}

	public void setLimitPower(Integer limitPower) {
		this.limitPower = limitPower;
	}

	public Integer getLimitTime() {
		return limitTime;
	}

	public void setLimitTime(Integer limitTime) {
		this.limitTime = limitTime;
	}

	private String deviceId;
	
	private Integer plugId;
	
	private Integer limitPower;
	
	private Integer limitTime;
}
