package com.madongfang.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.Custom;

public interface CustomRepository extends JpaRepository<Custom, Integer> {

	public Custom findByTypeAndUserOpenId(String type, String userOpenId);
	
	public Custom findByTypeAndNickname(String type, String nickname);
	
	public Custom findByPhoneNumber(String phoneNumber);
}
