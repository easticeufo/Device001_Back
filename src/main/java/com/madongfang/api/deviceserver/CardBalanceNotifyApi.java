package com.madongfang.api.deviceserver;

public class CardBalanceNotifyApi extends NotifyApi {

	public CardBalanceNotifyApi() {
		super();
	}

	public CardBalanceNotifyApi(String type, String nonce, String deviceId, String cardId) {
		super(type, nonce);
		this.deviceId = deviceId;
		this.cardId = cardId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	private String deviceId;
	
	private String cardId;
}
