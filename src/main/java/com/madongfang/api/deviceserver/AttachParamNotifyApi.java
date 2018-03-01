package com.madongfang.api.deviceserver;

public class AttachParamNotifyApi extends NotifyApi {

	public AttachParamNotifyApi() {
		super();
	}

	public AttachParamNotifyApi(String type, String nonce, String deviceId) {
		super(type, nonce);
		this.deviceId = deviceId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	private String deviceId;
}