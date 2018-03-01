package com.madongfang.api.deviceserver;

import java.util.List;

import com.madongfang.api.ReturnApi;

public class ParamReturnApi extends ReturnApi {

	public ParamReturnApi() {
		super();
	}

	public ParamReturnApi(int returnCode, String returnMsg, int maxPlugPower, int maxDevicePower, 
			String cardPassword, int factor, List<Integer> remainList) {
		super(returnCode, returnMsg);
		this.maxPlugPower = maxPlugPower;
		this.maxDevicePower = maxDevicePower;
		this.cardPassword = cardPassword;
		this.factor = factor;
		this.remainList = remainList;
	}

	public int getMaxPlugPower() {
		return maxPlugPower;
	}

	public void setMaxPlugPower(int maxPlugPower) {
		this.maxPlugPower = maxPlugPower;
	}

	public int getMaxDevicePower() {
		return maxDevicePower;
	}

	public void setMaxDevicePower(int maxDevicePower) {
		this.maxDevicePower = maxDevicePower;
	}

	public String getCardPassword() {
		return cardPassword;
	}

	public void setCardPassword(String cardPassword) {
		this.cardPassword = cardPassword;
	}

	public int getFactor() {
		return factor;
	}

	public void setFactor(int factor) {
		this.factor = factor;
	}

	public List<Integer> getRemainList() {
		return remainList;
	}

	public void setRemainList(List<Integer> remainList) {
		this.remainList = remainList;
	}

	public List<Integer> getRemainTimeList() {
		return remainTimeList;
	}

	public void setRemainTimeList(List<Integer> remainTimeList) {
		this.remainTimeList = remainTimeList;
	}

	private int maxPlugPower;
	
	private int maxDevicePower;
	
	private String cardPassword;
	
	private int factor;
	
	private List<Integer> remainList;
	
	private List<Integer> remainTimeList;
}
