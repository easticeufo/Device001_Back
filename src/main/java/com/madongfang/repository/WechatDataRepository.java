package com.madongfang.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.WechatData;

public interface WechatDataRepository extends JpaRepository<WechatData, Integer> {

	public WechatData findFirstByIdNotNull();
}
