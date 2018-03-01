package com.madongfang.api;

import java.util.Date;

public class PaymentRecordApi {

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	private Date time;
	
	private Integer amount;
	
	private Integer balance;
}
