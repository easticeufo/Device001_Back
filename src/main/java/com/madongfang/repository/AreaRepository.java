package com.madongfang.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.Area;

public interface AreaRepository extends JpaRepository<Area, Integer> {

	public Area findByName(String name);
}
