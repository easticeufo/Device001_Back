package com.madongfang.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@IdClass(PlugPK.class)
public class Plug {

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public Boolean getInUse() {
		return inUse;
	}

	public void setInUse(Boolean inUse) {
		this.inUse = inUse;
	}

	public Integer getLimitPrice() {
		return limitPrice;
	}

	public void setLimitPrice(Integer limitPrice) {
		this.limitPrice = limitPrice;
	}

	public Custom getCustom() {
		return custom;
	}

	public void setCustom(Custom custom) {
		this.custom = custom;
	}

	public Integer getPower() {
		return power;
	}

	public void setPower(Integer power) {
		this.power = power;
	}

	public Integer getConsumePower() {
		return consumePower;
	}

	public void setConsumePower(Integer consumePower) {
		this.consumePower = consumePower;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getLimitPower() {
		return limitPower;
	}

	public void setLimitPower(Integer limitPower) {
		this.limitPower = limitPower;
	}

	public Integer getLimitTime() {
		return limitTime;
	}

	public void setLimitTime(Integer limitTime) {
		this.limitTime = limitTime;
	}

	public Integer getRemainTime() {
		return remainTime;
	}

	public void setRemainTime(Integer remainTime) {
		this.remainTime = remainTime;
	}

	@Id
	private Integer id;
	
	@Id
	@Column(length=32)
	private String deviceCode;
	
	@Column(name="`use`")
	private Boolean inUse;
	
	private Integer limitPrice;
	
	@ManyToOne
	@JoinColumn(name="customId")
	private Custom custom;
	
	private Integer power;
	
	@Column(name="consume")
	private Integer consumePower; // 表示插座当前充电已经消耗的电量，单位为千分之一度
	
	private Date startTime;
	
	private Date updateTime;
	
	private Integer unitPrice; // 单位电价，单位：分/度 或 分钟/元
	
	private Integer minPrice; // 最低电费，单位：分，当实际消耗的电量计算所得的电费小于最低电费时，按照最低电费计算电费
	
	private Integer attachPrice; // 附加收费，单位：分，每次充电的附加使用费，即表示在电费基础上多收取的费用
	
	private Integer overdraft; // 透支系数，1000表示不透支
	
	private String status; // 插座状态，E表示故障，其他情况为正常
	
	private Integer limitPower; // 充电电量限制
	
	private Integer limitTime; // 充电时间限制
	
	private Integer remainTime;
}
