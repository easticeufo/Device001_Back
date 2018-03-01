package com.madongfang.api;

public class ManufacturerInfoApi {

	public String getWebTitle() {
		return webTitle;
	}

	public void setWebTitle(String webTitle) {
		this.webTitle = webTitle;
	}

	public String getWechatQrcodeAddress() {
		return wechatQrcodeAddress;
	}

	public void setWechatQrcodeAddress(String wechatQrcodeAddress) {
		this.wechatQrcodeAddress = wechatQrcodeAddress;
	}

	private String webTitle;
	
	private String wechatQrcodeAddress;
}
