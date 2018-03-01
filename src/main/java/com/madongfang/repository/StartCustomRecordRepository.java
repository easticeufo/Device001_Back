package com.madongfang.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.StartCustomRecord;

public interface StartCustomRecordRepository extends JpaRepository<StartCustomRecord, Integer> {
	
	public StartCustomRecord findFirstByDeviceCodeAndPlugIdAndCustomRecordIsNullOrderByTimeDesc(String deviceCode, Integer plugId);
	
	public StartCustomRecord findFirstByPaymentTradeNumber(String paymentTradeNumber);
}
