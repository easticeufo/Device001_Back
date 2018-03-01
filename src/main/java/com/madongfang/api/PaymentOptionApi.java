package com.madongfang.api;

public class PaymentOptionApi {

	public Integer getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(Integer payAmount) {
		this.payAmount = payAmount;
	}

	public Integer getGiftAmount() {
		return giftAmount;
	}

	public void setGiftAmount(Integer giftAmount) {
		this.giftAmount = giftAmount;
	}

	private Integer payAmount; // 用户支付金额，单位为分
	
	private Integer giftAmount; // 支付赠送金额，单位为分
}
