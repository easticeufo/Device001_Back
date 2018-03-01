package com.madongfang.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.DiscriminatorValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.madongfang.api.BillRecordApi;
import com.madongfang.api.ChargeRecordApi;
import com.madongfang.api.DeviceApi;
import com.madongfang.api.PaymentRecordApi;
import com.madongfang.api.ReturnApi;
import com.madongfang.entity.Custom;
import com.madongfang.entity.CustomRecord;
import com.madongfang.entity.Device;
import com.madongfang.entity.PayCustomRecord;
import com.madongfang.entity.StartCustomRecord;
import com.madongfang.entity.StopCustomRecord;
import com.madongfang.exception.HttpInternalServerErrorException;
import com.madongfang.exception.HttpNotFoundException;
import com.madongfang.repository.CustomRecordRepository;
import com.madongfang.repository.CustomRepository;
import com.madongfang.repository.PayCustomRecordRepository;
import com.madongfang.repository.StopCustomRecordRepository;

@Service
public class CustomRecordService {

	public ChargeRecordApi getLastChargeRecord(int customId) {
		StopCustomRecord stopCustomRecord = stopCustomRecordRepository.findFirstByCustomIdOrderByTimeDesc(customId);
		if (stopCustomRecord == null)
		{
			throw new HttpNotFoundException(new ReturnApi(-1, "无记录"));
		}
		
		StartCustomRecord startCustomRecord = stopCustomRecord.getStartCustomRecord();
		if (startCustomRecord == null)
		{
			logger.error("充电结束记录无对应的充电开始记录！");
			throw new HttpInternalServerErrorException(new ReturnApi(-2, "充电结束记录无对应的充电开始记录"));
		}
		
		ChargeRecordApi chargeRecordApi = new ChargeRecordApi();
		chargeRecordApi.setAmount(-startCustomRecord.getAmount() - stopCustomRecord.getAmount());
		DeviceApi deviceApi = new DeviceApi();
		if (startCustomRecord.getDevice() != null)
		{
			if (startCustomRecord.getDevice().getArea() != null)
			{
				deviceApi.setArea(startCustomRecord.getDevice().getArea().getName());
			}
			deviceApi.setCode(startCustomRecord.getDevice().getCode());
			deviceApi.setLocation(startCustomRecord.getDevice().getName());
		}
		chargeRecordApi.setDevice(deviceApi);
		chargeRecordApi.setPlugId(startCustomRecord.getPlugId());
		chargeRecordApi.setStartTime(startCustomRecord.getTime());
		chargeRecordApi.setStopTime(stopCustomRecord.getTime());
		
		return chargeRecordApi;
	}
	
	public List<ChargeRecordApi> getChargeRecords(int customId, Pageable pageable) {
		List<ChargeRecordApi> chargeRecords = new ArrayList<ChargeRecordApi>(pageable.getPageSize());
		for (StopCustomRecord stopCustomRecord : stopCustomRecordRepository.findByCustomId(customId, pageable)) 
		{
			StartCustomRecord startCustomRecord = stopCustomRecord.getStartCustomRecord();
			if (startCustomRecord == null)
			{
				logger.error("充电结束记录无对应的充电开始记录！");
				continue;
			}
			
			ChargeRecordApi chargeRecordApi = new ChargeRecordApi();
			DeviceApi deviceApi = new DeviceApi();
			Device device = startCustomRecord.getDevice();
			if (device != null)
			{
				if (device.getArea() != null)
				{
					deviceApi.setArea(device.getArea().getName());
				}
				deviceApi.setCode(device.getCode());
				deviceApi.setLocation(device.getName());
			}
			
			chargeRecordApi.setAmount(-startCustomRecord.getAmount() - stopCustomRecord.getAmount());
			chargeRecordApi.setDevice(deviceApi);
			chargeRecordApi.setPlugId(startCustomRecord.getPlugId());
			chargeRecordApi.setStartTime(startCustomRecord.getTime());
			chargeRecordApi.setStopTime(stopCustomRecord.getTime());
			
			chargeRecords.add(chargeRecordApi);
		}
		
		return chargeRecords;
	}
	
	public List<PaymentRecordApi> getPaymentRecords(int customId, Pageable pageable) {
		List<PaymentRecordApi> paymentRecords = new LinkedList<PaymentRecordApi>();
		
		for (PayCustomRecord payCustomRecord : payCustomRecordRepository.findByCustomId(customId, pageable)) {
			PaymentRecordApi paymentRecordApi = new PaymentRecordApi();
			
			paymentRecordApi.setAmount(payCustomRecord.getAmount());
			paymentRecordApi.setBalance(payCustomRecord.getBalance());
			paymentRecordApi.setTime(payCustomRecord.getTime());
			
			paymentRecords.add(paymentRecordApi);
		}
		
		return paymentRecords;
	}
	
	public List<BillRecordApi> getBillRecords(int customId, Pageable pageable) {
		List<BillRecordApi> billRecords = new LinkedList<BillRecordApi>();
		
		for (CustomRecord customRecord : customRecordRepository.findByCustomId(customId, pageable)) {
			BillRecordApi billRecordApi = new BillRecordApi();
			
			billRecordApi.setAmount(customRecord.getAmount());
			billRecordApi.setBalance(customRecord.getBalance());
			billRecordApi.setTime(customRecord.getTime());
			String type = customRecord.getClass().getAnnotation(DiscriminatorValue.class).value();
			billRecordApi.setType(type);
			
			billRecords.add(billRecordApi);
		}
		
		return billRecords;
	}
	
	public List<BillRecordApi> getBillRecords(String cardId, Date beginTime, Date endTime) {
		Custom custom = customRepository.findByTypeAndNickname("C", cardId);
		if (custom == null)
		{
			throw new HttpNotFoundException(new ReturnApi(-1, "该充电卡不存在"));
		}
		
		List<BillRecordApi> billRecords = new LinkedList<BillRecordApi>();
		
		for (CustomRecord customRecord : customRecordRepository.findByCustomIdAndTimeBetweenOrderByTimeDesc(custom.getId(), beginTime, endTime)) {
			BillRecordApi billRecordApi = new BillRecordApi();
			
			billRecordApi.setAmount(customRecord.getAmount());
			billRecordApi.setBalance(customRecord.getBalance());
			billRecordApi.setTime(customRecord.getTime());
			String type = customRecord.getClass().getAnnotation(DiscriminatorValue.class).value();
			billRecordApi.setType(type);
			
			billRecords.add(billRecordApi);
		}
		
		return billRecords;
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CustomRecordRepository customRecordRepository;
	
	@Autowired
	private StopCustomRecordRepository stopCustomRecordRepository;
	
	@Autowired
	private PayCustomRecordRepository payCustomRecordRepository;
	
	@Autowired
	private CustomRepository customRepository;
	
}
