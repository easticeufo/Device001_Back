package com.madongfang.api;

public class DeviceApi {

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPlugNumber() {
		return plugNumber;
	}

	public void setPlugNumber(int plugNumber) {
		this.plugNumber = plugNumber;
	}

	public int getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(int minPrice) {
		this.minPrice = minPrice;
	}

	public int getAttachPrice() {
		return attachPrice;
	}

	public void setAttachPrice(int attachPrice) {
		this.attachPrice = attachPrice;
	}

	public int getFactor() {
		return factor;
	}

	public void setFactor(int factor) {
		this.factor = factor;
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

	public int getFloatChargeTime() {
		return floatChargeTime;
	}

	public void setFloatChargeTime(int floatChargeTime) {
		this.floatChargeTime = floatChargeTime;
	}

	public Integer getTrialCount() {
		return trialCount;
	}

	public void setTrialCount(Integer trialCount) {
		this.trialCount = trialCount;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public Boolean getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(Boolean isOnline) {
		this.isOnline = isOnline;
	}

	public String getServiceNumber() {
		return serviceNumber;
	}

	public void setServiceNumber(String serviceNumber) {
		this.serviceNumber = serviceNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String code;
	
	private String id;
	
	private int plugNumber;
	
	private int unitPrice; // 单位电价，单位：分/度，计时设备单位:小时/元
	
	private int minPrice; // 最低电费，单位：分，当实际消耗的电量计算所得的电费小于最低电费时，按照最低电费计算电费
	
	private int attachPrice; // 附加收费，单位：分，每次充电的附加使用费，即表示在电费基础上多收取的费用
	
	private int factor; // 校准因子
	
	private int maxPlugPower;
	
	private int maxDevicePower;
	
	private int floatChargeTime; // 浮充充电时间，单位：分钟
	
	private Integer trialCount;
	
	private String area;
	
	private String location;
	
	private Double latitude; // 纬度
	
	private Double longitude; // 经度
	
	private Integer distance; // 充电站和当前位置的距离，单位米
	
	private Boolean isOnline;
	
	private String serviceNumber;
	
	private String type;
}
