package com.madongfang.api.deviceserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NotifyApi {

	public NotifyApi() {
		super();
	}

	public NotifyApi(String type, String nonce) {
		super();
		this.type = type;
		this.nonce = nonce;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
	
	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	@Override
	public String toString() {
		return "NotifyApi [type=" + type + ", sign=" + sign + ", nonce=" + nonce + "]";
	}

	private String type = "undefined";
	
	private String sign;
	
	private String nonce;
	
}
