package com.madongfang.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.madongfang.entity.DevicePrompt;

public interface DevicePromptRepository extends JpaRepository<DevicePrompt, Integer> {

	@Query("select dp From DevicePrompt dp where dp.device.code=?1 and dp.startTime<=?2 and dp.stopTime>=?2 order by dp.startTime asc")
	public List<DevicePrompt> findByDeviceCodeAndTime(String deviceCode, Date time);
}
