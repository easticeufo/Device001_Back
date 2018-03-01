package com.madongfang.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.StopCustomRecord;

public interface StopCustomRecordRepository extends JpaRepository<StopCustomRecord, Integer> {

	public StopCustomRecord findFirstByCustomIdOrderByTimeDesc(Integer customId);
	
	public List<StopCustomRecord> findByCustomId(Integer customId, Pageable pageable);
}
