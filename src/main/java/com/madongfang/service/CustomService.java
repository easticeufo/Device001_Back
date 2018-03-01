package com.madongfang.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.madongfang.api.CardApi;
import com.madongfang.api.CardRechargeApi;
import com.madongfang.api.CustomApi;
import com.madongfang.api.ReturnApi;
import com.madongfang.api.deviceserver.CardBalanceReturnApi;
import com.madongfang.entity.Custom;
import com.madongfang.entity.Manager;
import com.madongfang.entity.PayCustomRecord;
import com.madongfang.exception.HttpBadRequestException;
import com.madongfang.exception.HttpNotFoundException;
import com.madongfang.repository.CustomRepository;
import com.madongfang.repository.ManagerRepository;
import com.madongfang.repository.PayCustomRecordRepository;
import com.madongfang.util.AlipayUtil;
import com.madongfang.util.CommonUtil;
import com.madongfang.util.WechatUtil;

@Service
@Transactional(isolation=Isolation.SERIALIZABLE)
public class CustomService {

	public CardApi getCard(String cardId)
	{
		Custom custom = customRepository.findByTypeAndNickname("C", cardId);
		if (custom == null)
		{
			throw new HttpNotFoundException(new ReturnApi(-1, "该充电卡不存在"));
		}
		
		return convertToCardApi(custom);
	}
	
	public CardApi addCard(CardApi cardApi) {
		ReturnApi returnApi = new ReturnApi();
		
		Custom custom = customRepository.findByTypeAndNickname("C", cardApi.getId());
		if (custom != null)
		{
			returnApi.setReturnCode(-1);
			returnApi.setReturnMsg("该充电卡已经添加");
			throw new HttpBadRequestException(returnApi);
		}
		
		custom = new Custom();
		custom.setBalance(cardApi.getBalance());
		custom.setGenerateTime(null);
		custom.setLimitPrice(200);
		custom.setLoginCode(null);
		custom.setNickname(cardApi.getId());
		custom.setPhoneNumber(null);
		custom.setReserveDay(0);
		custom.setType("C");
		custom.setUserOpenId(cardApi.getId());
		
		customRepository.save(custom);
		
		return convertToCardApi(custom);
	}
	
	public CardApi rechargeCard(String cardId, CardRechargeApi cardRechargeApi) {
		Custom custom = customRepository.findByTypeAndNickname("C", cardId);
		if (custom == null)
		{
			throw new HttpBadRequestException(new ReturnApi(-1, "该充电卡不存在"));
		}

		Date now = new Date();
		String tradeNumber = "C" + custom.getId() + "_" + now.getTime() + "_";
		tradeNumber += commonUtil.getRandomStringByLength(32 - tradeNumber.length());
		
		custom.setBalance(custom.getBalance() + cardRechargeApi.getAmount());
		
		PayCustomRecord payCustomRecord = new PayCustomRecord();
		payCustomRecord.setAmount(cardRechargeApi.getAmount());
		payCustomRecord.setBalance(custom.getBalance());
		payCustomRecord.setCustom(custom);
		payCustomRecord.setTime(now);
		payCustomRecord.setTradeNumber(tradeNumber);
		payCustomRecordRepository.save(payCustomRecord);
		
		return convertToCardApi(custom);
	}
	
	public Custom wechatLogin(WechatUtil.UserInfo userInfo) {
		Custom custom = customRepository.findByTypeAndUserOpenId("W", userInfo.getOpenid());
		if (custom != null) // 已有用户登陆
		{
			custom.setNickname(userInfo.getNickname());
			custom.setUnionid(userInfo.getUnionid());
			custom.setHeadImgUrl(userInfo.getHeadimgurl());
		}
		else // 新用户登陆
		{
			custom = new Custom();
			custom.setBalance(0);
			custom.setGenerateTime(null);
			custom.setLimitPrice(200);
			custom.setLoginCode(null);
			custom.setNickname(userInfo.getNickname());
			custom.setPhoneNumber(null);
			custom.setReserveDay(0);
			custom.setType("W");
			custom.setUserOpenId(userInfo.getOpenid());
			custom.setUnionid(userInfo.getUnionid());
			custom.setHeadImgUrl(userInfo.getHeadimgurl());
		}
		
		customRepository.save(custom);
		
		return custom;
	}
	
	public Custom alipayLogin(AlipayUtil.UserInfo userInfo) {
		Custom custom = customRepository.findByTypeAndUserOpenId("A", userInfo.getUserId());
		if (custom != null) // 已有用户登陆
		{
			custom.setNickname(userInfo.getNickname());
			custom.setHeadImgUrl(userInfo.getAvatar());
		}
		else // 新用户登陆
		{
			custom = new Custom();
			custom.setBalance(0);
			custom.setGenerateTime(null);
			custom.setLimitPrice(200);
			custom.setLoginCode(null);
			custom.setNickname(userInfo.getNickname());
			custom.setPhoneNumber(null);
			custom.setReserveDay(0);
			custom.setType("A");
			custom.setUserOpenId(userInfo.getUserId());
			custom.setHeadImgUrl(userInfo.getAvatar());
		}
		
		customRepository.save(custom);
		
		return custom;
	}
	
	public Custom cardLogin(String cardId, String userOpenId)
	{
		Custom custom = customRepository.findByTypeAndNickname("C", cardId);
		if (custom != null)
		{
			custom.setUserOpenId(userOpenId);
			customRepository.save(custom);
		}
		return custom;
	}
	
	public Custom getCustom(String phoneNumber)
	{
		return customRepository.findByPhoneNumber(phoneNumber);
	}
	
	public CustomApi getCustom(Integer id)
	{
		Custom custom = customRepository.findOne(id);
		if (custom == null)
		{
			throw new HttpNotFoundException(new ReturnApi(-1, "不存在的客户"));
		}
		
		CustomApi customApi = new CustomApi();
		customApi.setBalance(custom.getBalance());
		customApi.setId(id);
		customApi.setLimitPrice(custom.getLimitPrice());
		customApi.setNickname(custom.getNickname());
		customApi.setReserveDay(custom.getReserveDay());
		customApi.setType(custom.getType());
		customApi.setHeadImgUrl(custom.getHeadImgUrl());
		Manager manager = managerRepository.findByOpenId(custom.getUserOpenId());
		if ("C".equals(custom.getType()) && manager != null && manager.getLevel() == 4)
		{
			customApi.setFreeRecharge(true);
		}
		else 
		{
			customApi.setFreeRecharge(false);
		}
		
		return customApi;
	}
	
	public CardBalanceReturnApi getCardBalance(String cardId)
	{
		CardBalanceReturnApi cardBalanceReturnApi = new CardBalanceReturnApi();
		Custom custom = customRepository.findByTypeAndNickname("C", cardId);
		if (custom == null)
		{
			cardBalanceReturnApi.setReturnCode(-1);
			cardBalanceReturnApi.setReturnMsg("卡号无效");
			return cardBalanceReturnApi;
		}
		
		cardBalanceReturnApi.setReturnCode(0);
		cardBalanceReturnApi.setReturnMsg("OK");
		cardBalanceReturnApi.setBalance(custom.getBalance());
		
		return cardBalanceReturnApi;
	}
	
	public int getCustomBalance(int customId) {
		Custom custom = customRepository.findOne(customId);
		return custom.getBalance();
	}
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private CustomRepository customRepository;
	

	@Autowired
	private ManagerRepository managerRepository;
	
	@Autowired
	private PayCustomRecordRepository payCustomRecordRepository;
	
	private CardApi convertToCardApi(Custom custom) {
		CardApi cardApi = new CardApi();
		cardApi.setBalance(custom.getBalance());
		cardApi.setId(custom.getNickname());
		
		return cardApi;
	}
}
