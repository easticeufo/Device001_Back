package com.madongfang.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("P")
public class StopCustomRecord extends CustomRecord {

	public Integer getPowerConsumption() {
		return powerConsumption;
	}

	public void setPowerConsumption(Integer powerConsumption) {
		this.powerConsumption = powerConsumption;
	}

	public StartCustomRecord getStartCustomRecord() {
		return startCustomRecord;
	}

	public void setStartCustomRecord(StartCustomRecord startCustomRecord) {
		this.startCustomRecord = startCustomRecord;
	}

	private Integer powerConsumption; // 本次充电消耗的总电量，单位:千分之一度
	
	@OneToOne(mappedBy="customRecord")
	private StartCustomRecord startCustomRecord;
}
