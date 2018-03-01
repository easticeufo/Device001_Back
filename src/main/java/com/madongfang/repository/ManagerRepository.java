package com.madongfang.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.Manager;

public interface ManagerRepository extends JpaRepository<Manager, Integer> {

	public Manager findByOpenId(String openId);
}
