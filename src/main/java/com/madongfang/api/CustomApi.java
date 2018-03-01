package com.madongfang.api;

public class CustomApi {

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	public Integer getReserveDay() {
		return reserveDay;
	}

	public void setReserveDay(Integer reserveDay) {
		this.reserveDay = reserveDay;
	}

	public Integer getLimitPrice() {
		return limitPrice;
	}

	public void setLimitPrice(Integer limitPrice) {
		this.limitPrice = limitPrice;
	}

	public Boolean getFreeRecharge() {
		return freeRecharge;
	}

	public void setFreeRecharge(Boolean freeRecharge) {
		this.freeRecharge = freeRecharge;
	}

	public String getHeadImgUrl() {
		return headImgUrl;
	}

	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}

	private Integer id;
	
	private String type;
	
	private String nickname;
	
	private Integer balance;
	
	private Integer reserveDay;
	
	private Integer limitPrice;
	
	private Boolean freeRecharge;
	
	private String headImgUrl;
}
