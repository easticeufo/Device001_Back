package com.madongfang.api;

import java.util.Date;

public class ChargeStartReturnApi extends ReturnApi {

	public ChargeStartReturnApi() {
		super();
	}

	public ChargeStartReturnApi(int returnCode, String returnMsg) {
		super(returnCode, returnMsg);
	}

	public Boolean getWechatSubscribe() {
		return wechatSubscribe;
	}

	public void setWechatSubscribe(Boolean wechatSubscribe) {
		this.wechatSubscribe = wechatSubscribe;
	}

	public String getQrcodeAddress() {
		return qrcodeAddress;
	}

	public void setQrcodeAddress(String qrcodeAddress) {
		this.qrcodeAddress = qrcodeAddress;
	}

	public String getDeviceArea() {
		return deviceArea;
	}

	public void setDeviceArea(String deviceArea) {
		this.deviceArea = deviceArea;
	}

	public String getDeviceLocation() {
		return deviceLocation;
	}

	public void setDeviceLocation(String deviceLocation) {
		this.deviceLocation = deviceLocation;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public int getCustomBalance() {
		return customBalance;
	}

	public void setCustomBalance(int customBalance) {
		this.customBalance = customBalance;
	}

	private Boolean wechatSubscribe; // 是否关注了微信公众号
	
	private String qrcodeAddress; // 公众号二维码图片地址
	
	private String deviceArea;
	
	private String deviceLocation;
	
	private Date startTime;
	
	private int customBalance;
}
