package com.madongfang.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("E")
public class ExceptionCustomRecord extends CustomRecord {

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public StartCustomRecord getStartCustomRecord() {
		return startCustomRecord;
	}

	public void setStartCustomRecord(StartCustomRecord startCustomRecord) {
		this.startCustomRecord = startCustomRecord;
	}

	private String reason;
	
	@OneToOne(mappedBy="customRecord")
	private StartCustomRecord startCustomRecord;
}
