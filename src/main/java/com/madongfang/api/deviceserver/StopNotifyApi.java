package com.madongfang.api.deviceserver;

public class StopNotifyApi extends NotifyApi {

	public StopNotifyApi() {
		super();
	}

	public StopNotifyApi(String type, String nonce, String deviceId, Integer plugId, Integer remain) {
		super(type, nonce);
		this.deviceId = deviceId;
		this.plugId = plugId;
		this.remain = remain;
	}

	public String getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Integer getPlugId() {
		return plugId;
	}

	public void setPlugId(Integer plugId) {
		this.plugId = plugId;
	}

	public Integer getRemain() {
		return remain;
	}

	public void setRemain(Integer remain) {
		this.remain = remain;
	}

	public Integer getStopReason() {
		return stopReason;
	}

	public void setStopReason(Integer stopReason) {
		this.stopReason = stopReason;
	}

	public Integer getStopPower() {
		return stopPower;
	}

	public void setStopPower(Integer stopPower) {
		this.stopPower = stopPower;
	}

	public Integer getRemainTime() {
		return remainTime;
	}

	public void setRemainTime(Integer remainTime) {
		this.remainTime = remainTime;
	}

	@Override
	public String toString() {
		return super.toString() + " StopNotifyApi [deviceId=" + deviceId + ", plugId=" + plugId + ", remain=" + remain + "]";
	}

	private String deviceId;
	
	private Integer plugId;
	
	private Integer remain;
	
	private Integer stopReason;
	
	private Integer stopPower;
	
	private Integer remainTime;
}
