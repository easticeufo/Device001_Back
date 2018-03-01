package com.madongfang.api;

public class OrderApi {

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	private Integer amount; // 订单金额
	
	private String returnUrl; // 支付宝前台回跳地址，在微信下单时该参数无用
}
