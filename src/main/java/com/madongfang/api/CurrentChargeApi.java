package com.madongfang.api;

import java.util.Date;

public class CurrentChargeApi {

	public Integer getPlugId() {
		return plugId;
	}

	public void setPlugId(Integer plugId) {
		this.plugId = plugId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Integer getLimitPrice() {
		return limitPrice;
	}

	public void setLimitPrice(Integer limitPrice) {
		this.limitPrice = limitPrice;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getMoneyConsumption() {
		return moneyConsumption;
	}

	public void setMoneyConsumption(Integer moneyConsumption) {
		this.moneyConsumption = moneyConsumption;
	}

	public Integer getPower() {
		return power;
	}

	public void setPower(Integer power) {
		this.power = power;
	}

	public DeviceApi getDevice() {
		return device;
	}

	public void setDevice(DeviceApi device) {
		this.device = device;
	}

	private Integer plugId;
	
	private Date startTime;
	
	private Integer limitPrice;
	
	private Date updateTime;
	
	private Integer moneyConsumption; // 已花费的钱
	
	private Integer power; // 功率
	
	private DeviceApi device;
}
