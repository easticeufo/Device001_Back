package com.madongfang.api.deviceserver;

public class ParamNotifyApi extends NotifyApi {

	public ParamNotifyApi() {
		super();
	}

	public ParamNotifyApi(String type, String nonce, String deviceId) {
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
