package com.madongfang.api;

public class CardApi {

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	public Integer getQrCodeId() {
		return qrCodeId;
	}

	public void setQrCodeId(Integer qrCodeId) {
		this.qrCodeId = qrCodeId;
	}

	private String id; // 卡号
	
	private Integer balance; // 金额，单位为分
	
	private Integer qrCodeId;
}
