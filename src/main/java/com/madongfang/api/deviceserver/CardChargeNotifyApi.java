package com.madongfang.api.deviceserver;

public class CardChargeNotifyApi extends NotifyApi {

	public CardChargeNotifyApi() {
		super();
	}

	public CardChargeNotifyApi(String type, String nonce, String deviceId, int plugId, String cardId) {
		super(type, nonce);
		this.deviceId = deviceId;
		this.plugId = plugId;
		this.cardId = cardId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getPlugId() {
		return plugId;
	}

	public void setPlugId(int plugId) {
		this.plugId = plugId;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	private String deviceId;
	
	private int plugId;
	
	private String cardId;
}
