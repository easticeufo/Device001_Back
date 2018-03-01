package com.madongfang.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.PayCustomRecord;

public interface PayCustomRecordRepository extends JpaRepository<PayCustomRecord, Integer> {

	public List<PayCustomRecord> findByCustomId(Integer customId, Pageable pageable);
	
	public PayCustomRecord findFirstByTradeNumber(String tradeNumber);
}
