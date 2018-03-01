package com.madongfang.api.deviceserver;

import com.madongfang.api.ReturnApi;

public class AttachParamReturnApi extends ReturnApi {

	public AttachParamReturnApi() {
		super();
	}

	public AttachParamReturnApi(int returnCode, String returnMsg, int floatChargeTime) {
		super(returnCode, returnMsg);
		this.floatChargeTime = floatChargeTime;
	}

	public int getFloatChargeTime() {
		return floatChargeTime;
	}

	public void setFloatChargeTime(int floatChargeTime) {
		this.floatChargeTime = floatChargeTime;
	}

	private int floatChargeTime;
}
