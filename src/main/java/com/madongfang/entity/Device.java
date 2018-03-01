package com.madongfang.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="Device_T")
public class Device implements Serializable{

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getPlugNumber() {
		return plugNumber;
	}

	public void setPlugNumber(Integer plugNumber) {
		this.plugNumber = plugNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Integer unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Integer getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Integer minPrice) {
		this.minPrice = minPrice;
	}

	public Integer getAttachPrice() {
		return attachPrice;
	}

	public void setAttachPrice(Integer attachPrice) {
		this.attachPrice = attachPrice;
	}

	public Integer getOverdraft() {
		return overdraft;
	}

	public void setOverdraft(Integer overdraft) {
		this.overdraft = overdraft;
	}

	public Integer getFactor() {
		return factor;
	}

	public void setFactor(Integer factor) {
		this.factor = factor;
	}

	public Integer getMaxPlugPower() {
		return maxPlugPower;
	}

	public void setMaxPlugPower(Integer maxPlugPower) {
		this.maxPlugPower = maxPlugPower;
	}

	public Integer getMaxDevicePower() {
		return maxDevicePower;
	}

	public void setMaxDevicePower(Integer maxDevicePower) {
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getAliveTime() {
		return aliveTime;
	}

	public void setAliveTime(Date aliveTime) {
		this.aliveTime = aliveTime;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(length=32)
	private String id;
	
	@Column(length=32, unique=true)
	private String code;
	
	@Column(name="type")
	private Integer plugNumber;
	
	@Column(name="location")
	private String name;
	
	@Column(name="priceIn")
	private Integer unitPrice; // 单位电价，单位：分/度 计时设备单位:小时/元
	
	private Integer minPrice; // 最低电费，单位：分，当实际消耗的电量计算所得的电费小于最低电费时，按照最低电费计算电费
	
	private Integer attachPrice; // 附加收费，单位：分，每次充电的附加使用费，即表示在电费基础上多收取的费用
	
	private Integer overdraft; // 透支系数，1000表示不透支
	
	private Integer factor; // 校准因子
	
	private Integer maxPlugPower;
	
	private Integer maxDevicePower;
	
	private int floatChargeTime; // 浮充充电时间，单位：分钟
	
	private Integer trialCount; // 试用次数，负数表示正式版本，可以无限使用
	
	@Column(length=1)
	private String status;
	
	private Date aliveTime;
	
	private Double latitude; // 纬度
	
	private Double longitude; // 经度
	
	@Column(length=32, name="devType")
	private String type; // 设备类型：power-计电量类型 time-计时类型
	
	@ManyToOne
	@JoinColumn(name="areaId")
	private Area area;
}
