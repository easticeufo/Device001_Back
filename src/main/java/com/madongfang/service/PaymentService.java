package com.madongfang.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.madongfang.api.PaymentOptionApi;
import com.madongfang.entity.Custom;
import com.madongfang.entity.GiftCustomRecord;
import com.madongfang.entity.PayCustomRecord;
import com.madongfang.entity.Payment;
import com.madongfang.repository.CustomRepository;
import com.madongfang.repository.GiftCustomRecordRepository;
import com.madongfang.repository.PayCustomRecordRepository;
import com.madongfang.repository.PaymentRepository;

@Service
@Transactional(isolation=Isolation.SERIALIZABLE)
public class PaymentService {

	public List<PaymentOptionApi> getPaymentOptions() {
		List<PaymentOptionApi> paymentOptions = new LinkedList<PaymentOptionApi>();
		
		for (Payment payment : paymentRepository.findAll()) {
			PaymentOptionApi paymentOptionApi = new PaymentOptionApi();
			paymentOptionApi.setGiftAmount(payment.getGiftAmount());
			paymentOptionApi.setPayAmount(payment.getPayAmount());
			
			paymentOptions.add(paymentOptionApi);
		}
		
		return paymentOptions;
	}
	
	public RechargeResult recharge(int customId, int amount, String tradeNumber, Date time, String userOpenId) {
		Custom custom = customRepository.findOne(customId);
		if (custom == null)
		{
			logger.error("用户不存在:customId={}", customId);
			return null;
		}
		
		if (payCustomRecordRepository.findFirstByTradeNumber(tradeNumber) != null)
		{
			logger.error("重复的订单通知，订单号：{}", tradeNumber);
			return null;
		}
		
		/* 充值金额 */
		custom.setBalance(custom.getBalance() + amount);
		
		PayCustomRecord payCustomRecord = new PayCustomRecord();
		payCustomRecord.setAmount(amount);
		payCustomRecord.setBalance(custom.getBalance());
		payCustomRecord.setCustom(custom);
		payCustomRecord.setTime(time);
		payCustomRecord.setTradeNumber(tradeNumber);
		payCustomRecord.setUserOpenId(userOpenId);
		payCustomRecordRepository.save(payCustomRecord);
		
		/* 赠送金额 */
		GiftCustomRecord giftCustomRecord = null;
		Payment payment = paymentRepository.findOne(amount);
		if (payment != null && payment.getGiftAmount() != 0)
		{
			custom.setBalance(custom.getBalance() + payment.getGiftAmount());
			
			giftCustomRecord = new GiftCustomRecord();
			giftCustomRecord.setAmount(payment.getGiftAmount());
			giftCustomRecord.setBalance(custom.getBalance());
			giftCustomRecord.setCustom(custom);
			giftCustomRecord.setTime(time);
			giftCustomRecord.setTradeNumber(tradeNumber);
			giftCustomRecordRepository.save(giftCustomRecord);
		}
		
		RechargeResult rechargeResult = new RechargeResult();
		rechargeResult.setCustomBalance(custom.getBalance());
		rechargeResult.setCustomType(custom.getType());
		if (giftCustomRecord != null)
		{
			rechargeResult.setGiftAmount(giftCustomRecord.getAmount());
		}
		else
		{
			rechargeResult.setGiftAmount(0);
		}
		rechargeResult.setUserOpenId(custom.getUserOpenId());
		
		return rechargeResult;
	}
	
	public static class RechargeResult
	{
		public int getGiftAmount() {
			return giftAmount;
		}

		public void setGiftAmount(int giftAmount) {
			this.giftAmount = giftAmount;
		}

		public int getCustomBalance() {
			return customBalance;
		}

		public void setCustomBalance(int customBalance) {
			this.customBalance = customBalance;
		}

		public String getCustomType() {
			return customType;
		}

		public void setCustomType(String customType) {
			this.customType = customType;
		}

		public String getUserOpenId() {
			return userOpenId;
		}

		public void setUserOpenId(String userOpenId) {
			this.userOpenId = userOpenId;
		}

		private int giftAmount;
		
		private int customBalance;
		
		private String customType;
		
		private String userOpenId;
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PaymentRepository paymentRepository;
	
	@Autowired
	private CustomRepository customRepository;
	
	@Autowired
	private PayCustomRecordRepository payCustomRecordRepository;
	
	@Autowired
	private GiftCustomRecordRepository giftCustomRecordRepository;
}
