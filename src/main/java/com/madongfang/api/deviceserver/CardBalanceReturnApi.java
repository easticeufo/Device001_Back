package com.madongfang.api.deviceserver;

import com.madongfang.api.ReturnApi;

public class CardBalanceReturnApi extends ReturnApi {

	public CardBalanceReturnApi() {
		super();
	}

	public CardBalanceReturnApi(int returnCode, String returnMsg, int balance) {
		super(returnCode, returnMsg);
		this.balance = balance;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	private int balance;
}
