package com.madongfang.api.deviceserver;

import java.util.List;

public class StatusNotifyApi extends NotifyApi {

	public StatusNotifyApi() {
		super();
	}

	public StatusNotifyApi(String type, String nonce, String deviceId, List<PlugStatus> plugStatusList) {
		super(type, nonce);
		this.deviceId = deviceId;
		this.plugStatusList = plugStatusList;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public List<PlugStatus> getPlugStatusList() {
		return plugStatusList;
	}

	public void setPlugStatusList(List<PlugStatus> plugStatusList) {
		this.plugStatusList = plugStatusList;
	}

	public static class PlugStatus {
		
		public PlugStatus() {
			super();
		}
		public PlugStatus(Integer remain, Integer power, Integer remainTime) {
			super();
			this.remain = remain;
			this.power = power;
			this.remainTime = remainTime;
		}

		public Integer getRemain() {
			return remain;
		}
		public void setRemain(Integer remain) {
			this.remain = remain;
		}
		public Integer getPower() {
			return power;
		}
		public void setPower(Integer power) {
			this.power = power;
		}
		public Integer getRemainTime() {
			return remainTime;
		}
		public void setRemainTime(Integer remainTime) {
			this.remainTime = remainTime;
		}
		
		private Integer remain;
		private Integer power;
		private Integer remainTime;
	}
	
	private String deviceId;
	
	private List<PlugStatus> plugStatusList;
}
