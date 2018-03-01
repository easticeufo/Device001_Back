package com.madongfang.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.madongfang.entity.WechatData;
import com.madongfang.repository.WechatDataRepository;

@Service
public class WechatDataService {

	public void saveAccessToken(String accessToken, String jsapiTicket, Integer expiresIn) {
		WechatData wechatData = wechatDataRepository.findFirstByIdNotNull();
		if (wechatData == null)
		{
			wechatData = new WechatData();
		}
		
		wechatData.setAccessToken(accessToken);
		wechatData.setExpiresIn(expiresIn);
		wechatData.setRefreshTime(new Date());
		wechatData.setJsapiTicket(jsapiTicket);
		wechatDataRepository.save(wechatData);
	}
	
	@Autowired
	private WechatDataRepository wechatDataRepository;
}
