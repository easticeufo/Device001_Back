package com.madongfang.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.CustomRecord;

public interface CustomRecordRepository extends JpaRepository<CustomRecord, Integer> {

	public List<CustomRecord> findByCustomId(Integer customId, Pageable pageable);
	
	public List<CustomRecord> findByCustomIdAndTimeBetweenOrderByTimeDesc(Integer customId, Date beginTime, Date endTime);
}
