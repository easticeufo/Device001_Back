package com.madongfang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.madongfang.entity.Manager;
import com.madongfang.repository.ManagerRepository;

@Service
public class ManagerService {

	public Manager getManager(String openId) {
		return managerRepository.findByOpenId(openId);
	}
	
	@Autowired
	private ManagerRepository managerRepository;
}
