package com.madongfang.api;

public class CardRechargeApi {

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	private Integer amount; // 充值金额，单位为分
}
