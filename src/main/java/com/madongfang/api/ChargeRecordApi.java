package com.madongfang.api;

import java.util.Date;

public class ChargeRecordApi {

	public int getPlugId() {
		return plugId;
	}

	public void setPlugId(int plugId) {
		this.plugId = plugId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getStopTime() {
		return stopTime;
	}

	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public DeviceApi getDevice() {
		return device;
	}

	public void setDevice(DeviceApi device) {
		this.device = device;
	}

	private int plugId;
	
	private Date startTime;
	
	private Date stopTime;
	
	private int amount;
	
	private DeviceApi device;
}
