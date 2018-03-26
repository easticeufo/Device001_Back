package com.madongfang.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.alipay.api.AlipayApiException;
import com.madongfang.api.DeviceApi;
import com.madongfang.api.DevicePromptApi;
import com.madongfang.api.ReturnApi;
import com.madongfang.api.deviceserver.StatusReturnApi;
import com.madongfang.api.deviceserver.AttachParamReturnApi;
import com.madongfang.api.deviceserver.CardChargeReturnApi;
import com.madongfang.api.deviceserver.ParamReturnApi;
import com.madongfang.api.deviceserver.StatusNotifyApi.PlugStatus;
import com.madongfang.entity.Area;
import com.madongfang.entity.Custom;
import com.madongfang.entity.Device;
import com.madongfang.entity.DevicePrompt;
import com.madongfang.entity.ErrorRecord;
import com.madongfang.entity.ExceptionCustomRecord;
import com.madongfang.entity.Plug;
import com.madongfang.entity.PlugPK;
import com.madongfang.entity.StartCustomRecord;
import com.madongfang.entity.StopCustomRecord;
import com.madongfang.exception.HttpBadRequestException;
import com.madongfang.exception.HttpInternalServerErrorException;
import com.madongfang.exception.HttpNotAcceptableException;
import com.madongfang.exception.HttpNotFoundException;
import com.madongfang.repository.AreaRepository;
import com.madongfang.repository.CustomRepository;
import com.madongfang.repository.DevicePromptRepository;
import com.madongfang.repository.DeviceRepository;
import com.madongfang.repository.ErrorRecordRepository;
import com.madongfang.repository.ExceptionCustomRecordRepository;
import com.madongfang.repository.PlugRepository;
import com.madongfang.repository.StartCustomRecordRepository;
import com.madongfang.repository.StopCustomRecordRepository;
import com.madongfang.util.AlipayUtil;
import com.madongfang.util.CommonUtil;
import com.madongfang.util.DeviceControlUtil;
import com.madongfang.util.CommonUtil.NearCoordinate;
import com.madongfang.util.WechatUtil.TempleteMessage4;
import com.madongfang.util.WechatUtil;

@Service
@Transactional(isolation=Isolation.SERIALIZABLE)
public class DeviceService {

	public DeviceApi addDevice(DeviceApi deviceApi) {
		ReturnApi returnApi = new ReturnApi();
		
		Device device = deviceRepository.findOne(deviceApi.getId());
		if (device != null)
		{
			returnApi.setReturnCode(-1);
			returnApi.setReturnMsg("该设备已经添加");
			throw new HttpBadRequestException(returnApi);
		}
		
		String areaName = "新增设备";
		Area area = areaRepository.findByName(areaName);
		if (area == null)
		{
			area = new Area();
			area.setCardPassword("FFFFFFFFFFFF");
			area.setName(areaName);
			areaRepository.save(area);
		}
		
		device = new Device();
		device.setAliveTime(null);
		device.setArea(area);
		device.setAttachPrice(deviceApi.getAttachPrice());
		device.setCode(deviceApi.getCode());
		device.setFactor(deviceApi.getFactor());
		device.setFloatChargeTime(deviceApi.getFloatChargeTime());
		device.setId(deviceApi.getId());
		device.setMaxDevicePower(deviceApi.getMaxDevicePower());
		device.setMaxPlugPower(deviceApi.getMaxPlugPower());
		device.setMinPrice(deviceApi.getMinPrice());
		device.setName(deviceApi.getLocation());
		device.setPlugNumber(deviceApi.getPlugNumber());
		device.setStatus("N");
		if (deviceApi.getTrialCount() != null)
		{
			device.setTrialCount(deviceApi.getTrialCount());
		}
		else
		{
			device.setTrialCount(30); // 默认试用次数为30次
		}
		device.setUnitPrice(deviceApi.getUnitPrice());
		deviceRepository.save(device);
		
		for (int i = 1; i <= deviceApi.getPlugNumber(); i++)
		{
			Plug plug = new Plug();
			plug.setConsumePower(0);
			plug.setCustom(null);
			plug.setDeviceCode(device.getCode());
			plug.setId(i);
			plug.setInUse(false);
			plug.setLimitPrice(0);
			plug.setPower(0);
			plug.setStartTime(null);
			plug.setUpdateTime(null);
			plugRepository.save(plug);
		}
		
		return deviceApi;
	}
	
	public List<DeviceApi> getDevices() {
		List<DeviceApi> devices = new LinkedList<DeviceApi>();
		
		for (Device device : deviceRepository.findAll()) {
			DeviceApi deviceApi = new DeviceApi();
			deviceApi.setArea(device.getArea().getName());
			deviceApi.setAttachPrice(device.getAttachPrice());
			deviceApi.setCode(device.getCode());
			deviceApi.setFactor(device.getFactor());
			deviceApi.setFloatChargeTime(device.getFloatChargeTime());
			deviceApi.setId(device.getId());
			deviceApi.setLocation(device.getName());
			deviceApi.setMaxDevicePower(device.getMaxDevicePower());
			deviceApi.setMaxPlugPower(device.getMaxPlugPower());
			deviceApi.setMinPrice(device.getMinPrice());
			deviceApi.setPlugNumber(device.getPlugNumber());
			deviceApi.setUnitPrice(device.getUnitPrice());
			
			devices.add(deviceApi);
		}
		
		return devices;
	}
	
	public List<DeviceApi> getNearDevices(double longitude, double latitude, int distance)
	{
		List<DeviceApi> devices = new LinkedList<>();
		NearCoordinate coordinate = commonUtil.getNearCoordinate(longitude, latitude, distance);
		
		for (Device device : deviceRepository.findByLongitudeBetweenAndLatitudeBetween(coordinate.getMinlng(), coordinate.getMaxlng(), coordinate.getMinlat(), coordinate.getMaxlat())) 
		{
			DeviceApi deviceApi = new DeviceApi();
			deviceApi.setArea(device.getArea().getName());
			deviceApi.setCode(device.getCode());
			deviceApi.setDistance(commonUtil.getDistance(longitude, latitude, device.getLongitude(), device.getLatitude()));
			deviceApi.setLatitude(device.getLatitude());
			deviceApi.setLocation(device.getName());
			deviceApi.setLongitude(device.getLongitude());
			
			if (deviceApi.getDistance() <= distance)
			{
				devices.add(deviceApi);
			}
		}
		
		devices.sort(new Comparator<DeviceApi>() {

			@Override
			public int compare(DeviceApi dev1, DeviceApi dev2) {
				// TODO Auto-generated method stub
				return dev1.getDistance() - dev2.getDistance();
			}
		});
		
		return devices;
	}
	
	public DeviceApi getDevice(String deviceCode) {
		Device device = deviceRepository.findByCode(deviceCode);
		
		if (device == null)
		{
			throw new HttpNotFoundException(new ReturnApi(-1, "设备不存在"));
		}
		
		DeviceApi deviceApi = new DeviceApi();
		deviceApi.setArea(device.getArea().getName());
		deviceApi.setServiceNumber(device.getArea().getServiceNumber());
		deviceApi.setAttachPrice(device.getAttachPrice());
		deviceApi.setCode(device.getCode());
		deviceApi.setFactor(device.getFactor());
		deviceApi.setFloatChargeTime(device.getFloatChargeTime());
		deviceApi.setId(device.getId());
		deviceApi.setLocation(device.getName());
		deviceApi.setMaxDevicePower(device.getMaxDevicePower());
		deviceApi.setMaxPlugPower(device.getMaxPlugPower());
		deviceApi.setMinPrice(device.getMinPrice());
		deviceApi.setPlugNumber(device.getPlugNumber());
		deviceApi.setUnitPrice(device.getUnitPrice());
		deviceApi.setTrialCount(device.getTrialCount());
		deviceApi.setType(device.getType());
		if (device.getAliveTime() == null 
				|| (new Date()).getTime() - device.getAliveTime().getTime() >= 60 * 60 * 1000) // 一小时都没有收到设备上报的状态时认为离线
		{
			deviceApi.setIsOnline(false);
		}
		else 
		{
			deviceApi.setIsOnline(true);
		}
		
		return deviceApi;
	}
	
	public ReturnApi activateDevice(String deviceCode) {
		Device device = deviceRepository.findByCode(deviceCode);
		if (device == null)
		{
			throw new HttpNotFoundException(new ReturnApi(-1, "设备不存在"));
		}
		
		device.setTrialCount(-1);
		deviceRepository.save(device);
		
		return new ReturnApi(0, "OK");
	}
	
	public List<DevicePromptApi> getPrompts(String deviceCode)
	{
		List<DevicePromptApi> devicePrompts = new LinkedList<DevicePromptApi>();
		
		for (DevicePrompt devicePrompt : devicePromptRepository.findByDeviceCodeAndTime(deviceCode, new Date())) 
		{
			DevicePromptApi devicePromptApi = new DevicePromptApi();
			devicePromptApi.setContent(devicePrompt.getContent());
			devicePromptApi.setDeviceCode(deviceCode);
			devicePromptApi.setId(devicePrompt.getId());
			devicePromptApi.setStartTime(devicePrompt.getStartTime());
			devicePromptApi.setStopTime(devicePrompt.getStopTime());
			devicePromptApi.setTitle(devicePrompt.getTitle());
			
			devicePrompts.add(devicePromptApi);
		}
		
		return devicePrompts;
	}
	
	public void startChargeThread(String deviceCode, int plugId, String customType, String userOpenId,
			String tradeNumber, int limitPrice)
	{
		new StartChargeThread(deviceCode, plugId, customType, userOpenId, tradeNumber, limitPrice).start();
	}
	
	public StartChargeResult startCharge(String deviceCode, int plugId, int customId, int limitPrice) 
	{
		ReturnApi returnApi = new ReturnApi();
		
		/* 校验参数合法性 */
		Device device = deviceRepository.findByCode(deviceCode);
		Plug plug = plugRepository.findOne(new PlugPK(plugId, deviceCode));
		if (device == null || plug == null)
		{
			logger.warn("设备或插座不存在");
			returnApi.setReturnCode(-2);
			returnApi.setReturnMsg("不存在的设备或插座");
			throw new HttpBadRequestException(returnApi);
		}
		boolean freeCharge = false; // 是否免费充电
		if (device.getUnitPrice() == 0)
		{
			freeCharge = true;
		}
		if (device.getTrialCount() != null && device.getTrialCount() == 0)
		{
			returnApi.setReturnCode(-7);
			returnApi.setReturnMsg("设备试用次数为0，请联系厂家激活为正式版本！");
			throw new HttpNotAcceptableException(returnApi);
		}
		
		if (device.getOverdraft() == null || device.getOverdraft() < 1000)
		{
			device.setOverdraft(1000);
			deviceRepository.save(device);
		}
		
		if (plug.getInUse())
		{
			logger.warn("插座已被使用");
			returnApi.setReturnCode(-1);
			returnApi.setReturnMsg("插座已被使用");
			throw new HttpBadRequestException(returnApi);
		}
		
		Custom custom = customRepository.findOne(customId);
		if (custom == null)
		{
			logger.warn("用户不存在");
			returnApi.setReturnCode(-3);
			returnApi.setReturnMsg("不存在的用户");
			throw new HttpBadRequestException(returnApi);
		}
		
		if (!freeCharge)
		{
			if (limitPrice < (device.getAttachPrice() + device.getMinPrice()) && !"time".equals(device.getType()))
			{
				returnApi.setReturnCode(-5);
				returnApi.setReturnMsg("充电限价过低");
				throw new HttpBadRequestException(returnApi);
			}
			
			if (custom.getBalance() == 0 || custom.getBalance() < limitPrice)
			{
				returnApi.setReturnCode(-6);
				returnApi.setReturnMsg("余额不足");
				throw new HttpBadRequestException(returnApi);
			}
		}
		
		/* 开始充电 */
		custom.setLimitPrice(limitPrice);
		if (!freeCharge)
		{
			custom.setBalance(custom.getBalance() - limitPrice);
		}
		
		Date now = new Date();
		plug.setInUse(true);
		plug.setAttachPrice(device.getAttachPrice());
		plug.setConsumePower(0);
		plug.setCustom(custom);
		plug.setLimitPrice(limitPrice);
		plug.setMinPrice(device.getMinPrice());
		plug.setOverdraft(device.getOverdraft());
		plug.setPower(0);
		plug.setStartTime(now);
		plug.setUnitPrice(device.getUnitPrice());
		plug.setUpdateTime(null);
		if (freeCharge)
		{
			plug.setLimitPower(12 * 1000);
			plug.setLimitTime(12 * 60);
		}
		else if ("time".equals(device.getType()))
		{
			plug.setLimitPower(12 * 1000);
			plug.setLimitTime(plug.getUnitPrice() * limitPrice / 100);
		}
		else
		{
			plug.setLimitPower((limitPrice*plug.getOverdraft()/1000 - plug.getAttachPrice()) * 1000 / plug.getUnitPrice());
			plug.setLimitTime(12 * 60);
		}
		plugRepository.save(plug);
		
		StartCustomRecord startCustomRecord = new StartCustomRecord();
		if (freeCharge)
		{
			startCustomRecord.setAmount(0);
		}
		else
		{
			startCustomRecord.setAmount(-limitPrice);
		}
		startCustomRecord.setBalance(custom.getBalance());
		startCustomRecord.setCustom(custom);
		startCustomRecord.setCustomRecord(null);
		startCustomRecord.setDevice(device);
		startCustomRecord.setPlugId(plugId);
		startCustomRecord.setTime(now);
		startCustomRecordRepository.save(startCustomRecord);
		
		StartChargeResult startChargeResult = new StartChargeResult();
		startChargeResult.setCustomBalance(custom.getBalance());
		if (device.getArea() != null)
		{
			startChargeResult.setDeviceArea(device.getArea().getName());
		}
		startChargeResult.setDeviceId(device.getId());
		startChargeResult.setDeviceLocation(device.getName());
		startChargeResult.setLimitPower(plug.getLimitPower());
		startChargeResult.setLimitTime(plug.getLimitTime());
		startChargeResult.setStartTime(now);
		
		return startChargeResult;
	}
	
	public ReturnApi customStopCharge(String deviceCode, int plugId, int customId)
	{
		/* 校验参数合法性 */
		Device device = deviceRepository.findByCode(deviceCode);
		Plug plug = plugRepository.findOne(new PlugPK(plugId, deviceCode));
		if (device == null || plug == null)
		{
			logger.warn("设备或插座不存在");
			throw new HttpBadRequestException(new ReturnApi(-1, "不存在的设备或插座"));
		}
		
		Custom custom = customRepository.findOne(customId);
		if (custom == null)
		{
			logger.warn("用户不存在");
			throw new HttpBadRequestException(new ReturnApi(-2, "不存在的用户"));
		}
		
		if (plug.getCustom().getId() != customId)
		{
			logger.warn("无权停止");
			throw new HttpBadRequestException(new ReturnApi(-3, "该插座的充电不是您开启的，无权停止"));
		}
		
		if (!deviceControlUtil.stopCharge(device.getId(), plugId))
		{
			logger.warn("停止命令发送异常");
			throw new HttpInternalServerErrorException(new ReturnApi(-4, "停止命令发送异常"));
		}
		
		return new ReturnApi(0, "OK");
	}
	
	public StopChargeResult exceptionStopCharge(String deviceCode, int plugId, String reason) 
	{
		entityManager.clear();
		
		Plug plug = plugRepository.findOne(new PlugPK(plugId, deviceCode));
		if (plug == null || !plug.getInUse())
		{
			logger.warn("设备插座不存在或插座没有在使用:deviceCode={},plugId={}", deviceCode, plugId);
			return null;
		}
		
		Custom custom = plug.getCustom();
		if (custom == null)
		{
			logger.error("充电custom为空");
			return null;
		}

		int refund = plug.getLimitPrice();
		if (plug.getUnitPrice() == 0) // 免费充电的不退钱
		{
			refund = 0;
		}
		custom.setBalance(custom.getBalance() + refund);
		plug.setInUse(false);
		plugRepository.save(plug);
		
		Date now = new Date();
		ExceptionCustomRecord exceptionCustomRecord = new ExceptionCustomRecord();
		exceptionCustomRecord.setAmount(refund);
		exceptionCustomRecord.setBalance(custom.getBalance());
		exceptionCustomRecord.setCustom(custom);
		exceptionCustomRecord.setReason(reason);
		exceptionCustomRecord.setTime(now);
		StartCustomRecord startCustomRecord = startCustomRecordRepository.findFirstByDeviceCodeAndPlugIdAndCustomRecordIsNullOrderByTimeDesc(deviceCode, plugId);
		if (startCustomRecord != null)
		{
			startCustomRecord.setCustomRecord(exceptionCustomRecord);
		}
		else
		{
			logger.error("未找到开始充电记录：deviceCode={},plugId={}", deviceCode, plugId);
		}
		exceptionCustomRecord.setStartCustomRecord(startCustomRecord);

		exceptionCustomRecordRepository.save(exceptionCustomRecord);
		
		StopChargeResult stopChargeResult = new StopChargeResult();
		stopChargeResult.setCustomBalance(custom.getBalance());
		stopChargeResult.setCustomType(custom.getType());
		Device device = deviceRepository.findByCode(deviceCode);
		if (device != null)
		{
			if (device.getArea() != null)
			{
				stopChargeResult.setDeviceArea(device.getArea().getName());
			}
			stopChargeResult.setDeviceLocation(device.getName());
		}
		else 
		{
			logger.error("device not found:deviceCode={}", deviceCode);
		}
		stopChargeResult.setRefund(refund);
		stopChargeResult.setSpend(plug.getLimitPrice() - refund);
		stopChargeResult.setStopTime(now);
		stopChargeResult.setUserOpenId(custom.getUserOpenId());
		
		return stopChargeResult;
	}
	
	public StopChargeResult stopCharge(String deviceId, int plugId, int remain)
	{
		final int freeTime = 3 * 60 * 1000; // 免费时间，单位毫秒秒
		
		Device device = deviceRepository.findOne(deviceId);
		if (device == null)
		{
			logger.warn("设备不存在:deviceId={}", deviceId);
			return null;
		}
		
		PlugPK plugPK = new PlugPK(plugId, device.getCode());
		Plug plug = plugRepository.findOne(plugPK);
		if (plug == null || !plug.getInUse())
		{
			logger.warn("设备插座不存在或插座没有在使用:deviceId={},plugId={}", deviceId, plugId);
			return null;
		}
		boolean freeCharge = false;
		if (plug.getUnitPrice() == 0)
		{
			freeCharge = true;
		}
		
		Custom custom = plug.getCustom();
		if (custom == null)
		{
			logger.error("充电custom为空");
			return null;
		}
		
		int limitPrice = plug.getLimitPrice();
		int overdraft = 1000;
		if (plug.getOverdraft() != null)
		{
			overdraft = plug.getOverdraft();
		}
		int powerConsumption;
		if (plug.getLimitPower() == null)
		{
			powerConsumption = (limitPrice*overdraft/1000 - plug.getAttachPrice()) * 1000 / plug.getUnitPrice() - remain;
		}
		else
		{
			powerConsumption = plug.getLimitPower() - remain;
		}
		
		if (powerConsumption < 0)
		{
			logger.error("剩余电量大于本次充电的限价总电量：error powerConsumption={}", powerConsumption);
			powerConsumption = 0;
		}
		
		Date now = new Date();
		Date startTime = plug.getStartTime();
		long maxPowerConsumption = device.getMaxPlugPower() * (now.getTime() - startTime.getTime()) / (60 * 60 * 1000); // 计算理论上插座消耗的最大电量，单位瓦时
		if (powerConsumption > (maxPowerConsumption * 3 / 2)) // 消耗的电量大于1.5倍的理论最大电量
		{
			logger.warn("消耗电量过大：powerConsumption={}, maxPowerConsumption={}", powerConsumption, maxPowerConsumption);
			powerConsumption = 0;
		}
		
		int refund;
		if (freeCharge)
		{
			refund = 0;
		}
		else if (now.getTime() - startTime.getTime() <= freeTime)
		{
			refund = limitPrice;
			if (remain == 0) // 短时间内不可能把电消耗完，导致remain等于0
			{
				logger.warn("设备结束充电remain错误:powerConsumption={}", powerConsumption);
				powerConsumption = 0;
			}
		}
		else if ("time".equals(device.getType()))
		{
			refund = 0;
		}
		else 
		{
			int powerFee = powerConsumption * plug.getUnitPrice() / 1000;
			if (powerFee < plug.getMinPrice()) // 电费小于最低电费时，按照最低电费计算电费
			{
				powerFee = plug.getMinPrice();
			}
			refund = limitPrice - powerFee - plug.getAttachPrice();
			if (refund < 0)
			{
				logger.info("充电限额:{},实际电费:{}", limitPrice, powerFee);
				refund = 0;
			}
		}
		
		if (device.getTrialCount() != null && device.getTrialCount() > 0)
		{
			device.setTrialCount(device.getTrialCount() - 1); // 试用次数减一
			deviceRepository.save(device);
		}
		
		plug.setInUse(false);
		plugRepository.save(plug);
		
		StopCustomRecord stopCustomRecord = new StopCustomRecord();
		stopCustomRecord.setAmount(refund);
		stopCustomRecord.setPowerConsumption(powerConsumption);
		stopCustomRecord.setTime(now);
		StartCustomRecord startCustomRecord = startCustomRecordRepository.findFirstByDeviceCodeAndPlugIdAndCustomRecordIsNullOrderByTimeDesc(device.getCode(), plugId);
		if (startCustomRecord != null)
		{
			startCustomRecord.setCustomRecord(stopCustomRecord);
			String tradeNumber = startCustomRecord.getPaymentTradeNumber();
			if (tradeNumber != null && refund > 0) // 直接支付的用户需要退款
			{
				String refundNumber = custom.getType() + "_" + System.currentTimeMillis() + "_" + tradeNumber + "_";
				refundNumber += commonUtil.getRandomStringByLength(64 - refundNumber.length());
				
				if ("W".equals(custom.getType()))
				{
					try {
						wechatUtil.refund(tradeNumber, limitPrice, refundNumber, refund, "充电结束退款");
					} catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
							| CertificateException | IOException e) {
						// TODO Auto-generated catch block
						logger.error("微信退款异常：", e);
					}
				}
				else if ("A".equals(custom.getType()))
				{
					try {
						alipayUtil.refund(tradeNumber, refundNumber, refund, "充电结束退款");
					} catch (AlipayApiException e) {
						// TODO Auto-generated catch block
						logger.error("支付宝退款异常：", e);
					}
				}
				else 
				{
					logger.error("用户类型错误：customType={}", custom.getType());
				}
			}
			else
			{
				custom.setBalance(custom.getBalance() + refund);
			}
		}
		else
		{
			logger.error("未找到开始充电记录：deviceCode={},plugId={}", device.getCode(), plugId);
		}
		stopCustomRecord.setStartCustomRecord(startCustomRecord);
		stopCustomRecord.setBalance(custom.getBalance());
		stopCustomRecord.setCustom(custom);

		stopCustomRecordRepository.save(stopCustomRecord);
		
		StopChargeResult stopChargeResult = new StopChargeResult();
		stopChargeResult.setCustomBalance(custom.getBalance());
		stopChargeResult.setCustomType(custom.getType());
		if (device.getArea() != null)
		{
			stopChargeResult.setDeviceArea(device.getArea().getName());
		}
		stopChargeResult.setDeviceLocation(device.getName());
		stopChargeResult.setRefund(refund);
		stopChargeResult.setSpend(limitPrice - refund);
		stopChargeResult.setStopTime(now);
		stopChargeResult.setUserOpenId(custom.getUserOpenId());
		stopChargeResult.setDeviceCode(device.getCode());
		
		return stopChargeResult;
	}
	
	public StatusReturnApi updateAndGetStatus(String deviceId, List<PlugStatus> plugStatusList)
	{
		StatusReturnApi statusReturnApi = new StatusReturnApi();
		Device device = deviceRepository.findOne(deviceId);
		if (device == null)
		{
			logger.warn("设备不存在:deviceId={}", deviceId);
			statusReturnApi.setReturnCode(-1);
			statusReturnApi.setReturnMsg("设备不存在");
			return statusReturnApi;
		}
		Date now = new Date();
		device.setAliveTime(now);
		deviceRepository.save(device);
		
		String deviceCode = device.getCode();
		int plugNumber = device.getPlugNumber();
		List<String> statusList = new ArrayList<String>(plugNumber);
		for (int i = 0; i < plugNumber; i++)
		{
			PlugPK plugPK = new PlugPK(i+1, deviceCode);
			Plug plug = plugRepository.findOne(plugPK);
			if (plug != null && plug.getInUse() 
					&& (plug.getUpdateTime() != null || now.getTime() - plug.getStartTime().getTime() > 15 * 1000)) // 插座正在使用
			{
				statusList.add("U");
				
				try {
					PlugStatus plugStatus = plugStatusList.get(i);
					if (plugStatus.getRemain() < 0)
					{
						ErrorRecord errorRecord = new ErrorRecord();
						errorRecord.setDescription("正在使用的插座返回remain="+plugStatus.getRemain());
						errorRecord.setDeviceCode(deviceCode);
						errorRecord.setPlugId(i+1);
						errorRecord.setTime(now);
						errorRecordRepository.save(errorRecord);
						logger.warn("继电器故障：deviceCode={}, plugId={}", deviceCode, i+1);
						continue;
					}
					int limitPrice = plug.getLimitPrice();
					int attachPrice = plug.getAttachPrice();
					int unitPrice = plug.getUnitPrice();
					int overdraft = 1000;
					if (plug.getOverdraft() != null)
					{
						overdraft = plug.getOverdraft();
					}
					int powerConsumption;
					if (plug.getLimitPower() == null)
					{
						powerConsumption = (limitPrice*overdraft/1000 - attachPrice) * 1000 / unitPrice - plugStatus.getRemain();
					}
					else
					{
						powerConsumption = plug.getLimitPower() - plugStatus.getRemain();
					}
					if (plugStatus.getRemainTime() != null)
					{
						plug.setRemainTime(plugStatus.getRemainTime());
					}
					plug.setConsumePower(powerConsumption);
					plug.setPower(plugStatus.getPower());
					plug.setUpdateTime(now);
					plugRepository.save(plug);
				} catch (IndexOutOfBoundsException e) {
					// TODO: handle exception
					logger.warn("缺少插座(deviceId={},plugId={})的状态信息", deviceId, i+1);
				}
			}
			else 
			{
				statusList.add("F");
			}
		}
		
		statusReturnApi.setReturnCode(0);
		statusReturnApi.setReturnMsg("OK");
		statusReturnApi.setStatusList(statusList);
		return statusReturnApi;
	}
	
	public ParamReturnApi getParam(String deviceId)
	{
		ParamReturnApi paramReturnApi = new ParamReturnApi();
		Device device = deviceRepository.findOne(deviceId);
		if (device == null)
		{
			logger.warn("设备不存在:deviceId={}", deviceId);
			paramReturnApi.setReturnCode(-1);
			paramReturnApi.setReturnMsg("设备不存在");
			return paramReturnApi;
		}
		
		int plugNumber = device.getPlugNumber();
		List<Integer> remainList = new ArrayList<Integer>(plugNumber);
		List<Integer> remainTimeList = new ArrayList<Integer>(plugNumber);
		String deviceCode = device.getCode();
		for (int i = 0; i < plugNumber; i++)
		{
			PlugPK plugPK = new PlugPK(i+1, deviceCode);
			Plug plug = plugRepository.findOne(plugPK);
			int remain = 0;
			int remainTime = 0;
			if (plug != null && plug.getInUse()) // 插座正在使用
			{
				int limitPrice = plug.getLimitPrice();
				int attachPrice = plug.getAttachPrice();
				int unitPrice = plug.getUnitPrice();
				int overdraft = 1000;
				if (plug.getOverdraft() != null)
				{
					overdraft = plug.getOverdraft();
				}
				if (plug.getLimitPower() == null)
				{
					remain = (limitPrice * overdraft / 1000 - attachPrice) * 1000 / unitPrice - plug.getConsumePower();
				}
				else
				{
					remain = plug.getLimitPower() - plug.getConsumePower();
				}
				if (plug.getRemainTime() != null)
				{
					remainTime = plug.getRemainTime();
				}
			}
			
			remainList.add(remain);
			remainTimeList.add(remainTime);
		}
		
		paramReturnApi.setReturnCode(0);
		paramReturnApi.setReturnMsg("OK");
		
		if (device.getArea() != null)
		{
			paramReturnApi.setCardPassword(device.getArea().getCardPassword());
		}
		paramReturnApi.setFactor(device.getFactor());
		paramReturnApi.setMaxDevicePower(device.getMaxDevicePower());
		paramReturnApi.setMaxPlugPower(device.getMaxPlugPower());
		paramReturnApi.setRemainList(remainList);
		paramReturnApi.setRemainTimeList(remainTimeList);
		
		return paramReturnApi;
	}
	
	public AttachParamReturnApi getAttachParam(String deviceId)
	{
		AttachParamReturnApi attachParamReturnApi = new AttachParamReturnApi();
		Device device = deviceRepository.findOne(deviceId);
		if (device == null)
		{
			logger.warn("设备不存在:deviceId={}", deviceId);
			attachParamReturnApi.setReturnCode(-1);
			attachParamReturnApi.setReturnMsg("设备不存在");
			return attachParamReturnApi;
		}
		
		attachParamReturnApi.setReturnCode(0);
		attachParamReturnApi.setReturnMsg("OK");
		attachParamReturnApi.setFloatChargeTime(device.getFloatChargeTime());
		
		return attachParamReturnApi;
	}
	
	public CardChargeReturnApi cardChargeStart(String deviceId, int plugId, String cardId)
	{
		CardChargeReturnApi cardChargeReturnApi = new CardChargeReturnApi();
		
		/* 校验参数合法性 */
		Device device = deviceRepository.findOne(deviceId);
		if (device == null)
		{
			logger.warn("无效的设备");
			cardChargeReturnApi.setReturnCode(-1);
			cardChargeReturnApi.setReturnMsg("无效的设备或插座");
			return cardChargeReturnApi;
		}
		boolean freeCharge = false; // 是否免费充电
		if (device.getUnitPrice() == 0)
		{
			freeCharge = true;
		}
		if (device.getTrialCount() != null && device.getTrialCount() == 0)
		{
			cardChargeReturnApi.setReturnCode(-7);
			cardChargeReturnApi.setReturnMsg("设备试用次数为0");
			return cardChargeReturnApi;
		}
		if (device.getOverdraft() == null || device.getOverdraft() < 1000)
		{
			device.setOverdraft(1000);
			deviceRepository.save(device);
		}
		
		PlugPK plugPK = new PlugPK(plugId, device.getCode());
		Plug plug = plugRepository.findOne(plugPK);
		if (plug == null || "E".equals(plug.getStatus()))
		{
			logger.warn("无效的插座");
			cardChargeReturnApi.setReturnCode(-1);
			cardChargeReturnApi.setReturnMsg("无效的设备或插座");
			return cardChargeReturnApi;
		}
		
		Custom custom = customRepository.findByTypeAndNickname("C", cardId);
		if (custom == null)
		{
			logger.warn("无效的卡号");
			cardChargeReturnApi.setReturnCode(-6);
			cardChargeReturnApi.setReturnMsg("无效的卡号");
			return cardChargeReturnApi;
		}
		int limitPrice = custom.getLimitPrice();
		
		if (plug.getInUse()) // 插座正在使用
		{
			if (plug.getCustom().getId() == custom.getId()) // 插座已经被该卡成功开启充电
			{
				cardChargeReturnApi.setReturnCode(0);
				cardChargeReturnApi.setReturnMsg("OK");
				cardChargeReturnApi.setLimitPower((plug.getLimitPrice()*plug.getOverdraft()/1000 - plug.getAttachPrice()) * 1000 / plug.getUnitPrice());
				return cardChargeReturnApi;
			}
			else 
			{
				logger.warn("插座已被其他人使用");
				cardChargeReturnApi.setReturnCode(-2);
				cardChargeReturnApi.setReturnMsg("插座已被其他人使用");
				return cardChargeReturnApi;
			}
		}
		
		int limitPower = 12 * 1000;
		int limitTime = 12 * 60;
		
		if (!freeCharge)
		{
			if (custom.getBalance() == 0 || (custom.getBalance() < (device.getAttachPrice() + device.getMinPrice()) && !"time".equals(device.getType())))
			{
				logger.warn("卡内余额不足");
				cardChargeReturnApi.setReturnCode(-4);
				cardChargeReturnApi.setReturnMsg("卡内余额不足");
				return cardChargeReturnApi;
			}
			
			/* 开始充电 */
			if (limitPrice < (device.getAttachPrice() + device.getMinPrice())) // 充电限额不能小于最低电费和附加收费之和
			{
				limitPrice = device.getAttachPrice() + device.getMinPrice();
			}
			if (limitPrice > custom.getBalance()) // 余额不足时，充电限额为当前余额
			{
				limitPrice = custom.getBalance();
			}
			custom.setBalance(custom.getBalance() - limitPrice);
			
			if ("time".equals(device.getType()))
			{
				limitTime = device.getUnitPrice() * limitPrice / 100;
			}
			else
			{
				limitPower = (limitPrice*device.getOverdraft()/1000 - device.getAttachPrice()) * 1000 / device.getUnitPrice();
			}
		}
		
		Date now = new Date();
		plug.setInUse(true);
		plug.setAttachPrice(device.getAttachPrice());
		plug.setConsumePower(0);
		plug.setCustom(custom);
		plug.setLimitPrice(limitPrice);
		plug.setMinPrice(device.getMinPrice());
		plug.setOverdraft(device.getOverdraft());
		plug.setPower(0);
		plug.setStartTime(now);
		plug.setUnitPrice(device.getUnitPrice());
		plug.setUpdateTime(now);
		plug.setLimitPower(limitPower);
		plug.setLimitTime(limitTime);
		plugRepository.save(plug);
		
		StartCustomRecord startCustomRecord = new StartCustomRecord();
		if (freeCharge)
		{
			startCustomRecord.setAmount(0);
		}
		else 
		{
			startCustomRecord.setAmount(-limitPrice);
		}
		startCustomRecord.setBalance(custom.getBalance());
		startCustomRecord.setCustom(custom);
		startCustomRecord.setCustomRecord(null);
		startCustomRecord.setDevice(device);
		startCustomRecord.setPlugId(plugId);
		startCustomRecord.setTime(now);
		startCustomRecordRepository.save(startCustomRecord);
		
		cardChargeReturnApi.setReturnCode(0);
		cardChargeReturnApi.setReturnMsg("OK");
		cardChargeReturnApi.setLimitPower(limitPower);
		cardChargeReturnApi.setLimitTime(limitTime);
		
		return cardChargeReturnApi;
	}
	
	public static class StartChargeResult
	{
		public String getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}

		public String getDeviceArea() {
			return deviceArea;
		}

		public void setDeviceArea(String deviceArea) {
			this.deviceArea = deviceArea;
		}

		public String getDeviceLocation() {
			return deviceLocation;
		}

		public void setDeviceLocation(String deviceLocation) {
			this.deviceLocation = deviceLocation;
		}

		public int getLimitPower() {
			return limitPower;
		}

		public void setLimitPower(int limitPower) {
			this.limitPower = limitPower;
		}

		public Date getStartTime() {
			return startTime;
		}

		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}

		public int getCustomBalance() {
			return customBalance;
		}

		public void setCustomBalance(int customBalance) {
			this.customBalance = customBalance;
		}

		public int getLimitTime() {
			return limitTime;
		}

		public void setLimitTime(int limitTime) {
			this.limitTime = limitTime;
		}

		private String deviceId;
		
		private String deviceArea;
		
		private String deviceLocation;
		
		private int limitPower;
		
		private Date startTime;
		
		private int customBalance;
		
		private int limitTime;
	}
	
	public static class StopChargeResult
	{
		public String getDeviceArea() {
			return deviceArea;
		}

		public void setDeviceArea(String deviceArea) {
			this.deviceArea = deviceArea;
		}

		public String getDeviceLocation() {
			return deviceLocation;
		}

		public void setDeviceLocation(String deviceLocation) {
			this.deviceLocation = deviceLocation;
		}

		public Date getStopTime() {
			return stopTime;
		}

		public void setStopTime(Date stopTime) {
			this.stopTime = stopTime;
		}

		public int getSpend() {
			return spend;
		}

		public void setSpend(int spend) {
			this.spend = spend;
		}

		public int getRefund() {
			return refund;
		}

		public void setRefund(int refund) {
			this.refund = refund;
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

		public String getDeviceCode() {
			return deviceCode;
		}

		public void setDeviceCode(String deviceCode) {
			this.deviceCode = deviceCode;
		}

		private String deviceArea;
		
		private String deviceLocation;
		
		private Date stopTime;
		
		private int spend;
		
		private int refund;
		
		private int customBalance;
		
		private String customType;
		
		private String userOpenId;
		
		private String deviceCode;
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${wechat.templateId.startCharge}")
	private String wechatTemplateIdStartCharge;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private PlugRepository plugRepository;
	
	@Autowired
	private AreaRepository areaRepository;
	
	@Autowired
	private CustomRepository customRepository;
	
	@Autowired
	private StartCustomRecordRepository startCustomRecordRepository;
	
	@Autowired
	private StopCustomRecordRepository stopCustomRecordRepository;
	
	@Autowired
	private ExceptionCustomRecordRepository exceptionCustomRecordRepository;
	
	@Autowired
	private DevicePromptRepository devicePromptRepository;
	
	@Autowired
	private ErrorRecordRepository errorRecordRepository;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private WechatUtil wechatUtil;
	
	@Autowired
	private AlipayUtil alipayUtil;
	
	@Autowired
	private DeviceControlUtil deviceControlUtil;
	
	private class StartChargeThread extends Thread {

		public StartChargeThread(String deviceCode, int plugId, String customType, String userOpenId,
				String tradeNumber, int limitPrice) {
			super();
			this.deviceCode = deviceCode;
			this.plugId = plugId;
			this.customType = customType;
			this.userOpenId = userOpenId;
			this.tradeNumber = tradeNumber;
			this.limitPrice = limitPrice;
		}

		@Override
		public void run() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			/* 校验参数合法性 */
			Device device = deviceRepository.findByCode(deviceCode);
			Plug plug = plugRepository.findOne(new PlugPK(plugId, deviceCode));
			if (device == null || plug == null)
			{
				logger.warn("设备或插座不存在");
				refund("不存在的设备或插座");
				return;
			}
			
			if (startCustomRecordRepository.findFirstByPaymentTradeNumber(tradeNumber) != null)
			{
				logger.error("重复的订单通知，订单号：{}", tradeNumber);
				return;
			}
			
			boolean freeCharge = false; // 是否免费充电
			if (device.getUnitPrice() == 0)
			{
				freeCharge = true;
			}
			if (device.getTrialCount() != null && device.getTrialCount() == 0)
			{
				logger.warn("设备试用次数为0，请联系厂家激活为正式版本！");
				refund("设备试用次数为0，请联系厂家激活为正式版本！");
				return;
			}
			
			if (device.getOverdraft() == null || device.getOverdraft() < 1000)
			{
				device.setOverdraft(1000);
				deviceRepository.save(device);
			}
			
			if (plug.getInUse())
			{
				logger.warn("插座已被使用");
				refund("插座已被使用");
				return;
			}
			
			Custom custom = customRepository.findByTypeAndUserOpenId(customType, userOpenId);
			if (custom == null)
			{
				logger.warn("用户不存在");
				refund("用户不存在");
				return;
			}
			
			if (!freeCharge && !"time".equals(device.getType()))
			{
				if (limitPrice < (device.getAttachPrice() + device.getMinPrice()))
				{
					logger.warn("充电限价过低");
					refund("充电限价过低");
					return;
				}
			}
			
			/* 开始充电 */
			custom.setLimitPrice(limitPrice);
			customRepository.save(custom);
			
			Date now = new Date();
			plug.setStartTime(now);
			plug.setInUse(true);
			plug.setAttachPrice(device.getAttachPrice());
			plug.setConsumePower(0);
			plug.setCustom(custom);
			plug.setLimitPrice(limitPrice);
			plug.setMinPrice(device.getMinPrice());
			plug.setOverdraft(device.getOverdraft());
			plug.setPower(0);
			plug.setUnitPrice(device.getUnitPrice());
			plug.setUpdateTime(null);
			int limitPower = 12 * 1000;
			int limitTime = 12 * 60;
			if (!freeCharge)
			{
				if ("time".equals(device.getType()))
				{
					limitTime = plug.getUnitPrice() * limitPrice / 100;
				}
				else
				{
					limitPower = (limitPrice*plug.getOverdraft()/1000 - plug.getAttachPrice()) * 1000 / plug.getUnitPrice();
				}
			}
			plug.setLimitPower(limitPower);
			plug.setLimitTime(limitTime);
			plugRepository.save(plug);
			
			if (!deviceControlUtil.startCharge(device.getId(), plugId, limitPower, limitTime))
			{
				logger.warn("设备网络通信异常");
				plug.setInUse(false);
				plugRepository.save(plug);
				refund("设备网络通信异常");
				return;
			}
			
			StartCustomRecord startCustomRecord = new StartCustomRecord();
			if (freeCharge)
			{
				startCustomRecord.setAmount(0);
			}
			else
			{
				startCustomRecord.setAmount(-limitPrice);
			}
			startCustomRecord.setBalance(custom.getBalance());
			startCustomRecord.setCustom(custom);
			startCustomRecord.setCustomRecord(null);
			startCustomRecord.setDevice(device);
			startCustomRecord.setPlugId(plugId);
			startCustomRecord.setTime(now);
			startCustomRecord.setPaymentTradeNumber(tradeNumber);
			startCustomRecordRepository.save(startCustomRecord);
			
			if ("W".equals(customType)) // 微信用户需要推送开始充电消息
			{
				TempleteMessage4 startMessage = new TempleteMessage4();
				startMessage.getFirst().setValue("您的充电已开始");
				startMessage.getKeyword1().setValue(device.getArea().getName() + device.getName() + plugId + "号插座");
				startMessage.getKeyword2().setValue(sdf.format(now));
				startMessage.getKeyword3().setValue(String.format("%.2f元", (float)limitPrice / 100));
				startMessage.getKeyword4().setValue(String.format("%.2f元", (float)custom.getBalance() / 100));
				startMessage.getRemark().setValue("感谢您使用充电服务");
				try {
					wechatUtil.sendTempleteMessage(custom.getUserOpenId(), 
							wechatTemplateIdStartCharge, "", startMessage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("catch Exception:", e);
				}
			}
		}
		
		private String deviceCode;
		
		private int plugId;
		
		private String customType;
		
		private String userOpenId;
		
		private String tradeNumber;
		
		private int limitPrice;
		
		private void refund(String reason)
		{
			String refundNumber = customType + "_" + System.currentTimeMillis() + "_" + tradeNumber + "_";
			refundNumber += commonUtil.getRandomStringByLength(64 - refundNumber.length());
			
			if ("W".equals(customType))
			{
				try {
					wechatUtil.refund(tradeNumber, limitPrice, refundNumber, limitPrice, reason);
				} catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
						| CertificateException | IOException e) {
					// TODO Auto-generated catch block
					logger.error("微信退款异常：", e);
				}
			}
			else if ("A".equals(customType))
			{
				try {
					alipayUtil.refund(tradeNumber, refundNumber, limitPrice, reason);
				} catch (AlipayApiException e) {
					// TODO Auto-generated catch block
					logger.error("支付宝退款异常：", e);
				}
			}
			else {
				logger.error("用户类型错误：customType={}", customType);
			}
		}
	}
}
