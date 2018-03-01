package com.madongfang.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.Plug;
import com.madongfang.entity.PlugPK;

public interface PlugRepository extends JpaRepository<Plug, PlugPK> {

	public List<Plug> findByDeviceCode(String deviceCode);
	
	public List<Plug> findByCustomIdAndInUseTrueOrderByStartTimeDesc(Integer customId);
}
