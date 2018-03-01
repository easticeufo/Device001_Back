package com.madongfang.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Payment {

	public int getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(int payAmount) {
		this.payAmount = payAmount;
	}

	public int getGiftAmount() {
		return giftAmount;
	}

	public void setGiftAmount(int giftAmount) {
		this.giftAmount = giftAmount;
	}

	@Id
	private int payAmount; // 用户支付金额，单位为分
	
	private int giftAmount; // 支付赠送金额，单位为分
}
