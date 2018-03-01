package com.madongfang.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("D")
public class StartCustomRecord extends CustomRecord {

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Integer getPlugId() {
		return plugId;
	}

	public void setPlugId(Integer plugId) {
		this.plugId = plugId;
	}

	public String getPaymentTradeNumber() {
		return paymentTradeNumber;
	}

	public void setPaymentTradeNumber(String paymentTradeNumber) {
		this.paymentTradeNumber = paymentTradeNumber;
	}

	public CustomRecord getCustomRecord() {
		return customRecord;
	}

	public void setCustomRecord(CustomRecord customRecord) {
		this.customRecord = customRecord;
	}

	@ManyToOne
	@JoinColumn(name="deviceCode", referencedColumnName="code")
	private Device device;
	
	private Integer plugId;
	
	private String paymentTradeNumber; // 开始充电订单号
	
	@OneToOne
	private CustomRecord customRecord; // 和开始记录对应的停止记录或异常记录
}
