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

import com.madongfang.api.CurrentChargeApi;
import com.madongfang.api.DeviceApi;
import com.madongfang.api.PlugApi;
import com.madongfang.entity.Device;
import com.madongfang.entity.Plug;
import com.madongfang.entity.PlugPK;
import com.madongfang.repository.DeviceRepository;
import com.madongfang.repository.PlugRepository;

@Service
@Transactional(isolation=Isolation.SERIALIZABLE)
public class PlugService {

	public List<PlugApi> getPlugs(String deviceCode) {
		List<PlugApi> plugs = new LinkedList<PlugApi>();
		
		for (Plug plug : plugRepository.findByDeviceCode(deviceCode)) {
			PlugApi plugApi = new PlugApi();
			
			plugApi.setId(plug.getId());
			if ("E".equals(plug.getStatus())) // 插座故障
			{
				plugApi.setStatus("E");
			}
			else
			{
				if (plug.getInUse())
				{
					plugApi.setStatus("U");
				}
				else 
				{
					plugApi.setStatus("F");
				}
			}
			plugApi.setPower(plug.getPower());
			plugs.add(plugApi);
		}
		
		return plugs;
	}
	
	public List<CurrentChargeApi> getCurrentCharges(int customId) {
		List<CurrentChargeApi> currentCharges = new LinkedList<CurrentChargeApi>();
		
		for (Plug plug : plugRepository.findByCustomIdAndInUseTrueOrderByStartTimeDesc(customId)) {
			CurrentChargeApi currentChargeApi = new CurrentChargeApi();
			
			DeviceApi deviceApi = new DeviceApi();
			Device device = deviceRepository.findByCode(plug.getDeviceCode());
			if (device != null)
			{
				if (device.getArea() != null)
				{
					deviceApi.setArea(device.getArea().getName());
				}
				deviceApi.setCode(device.getCode());
				deviceApi.setLocation(device.getName());
			}
			
			currentChargeApi.setDevice(deviceApi);
			currentChargeApi.setLimitPrice(plug.getLimitPrice());
			int moneyConsumption = plug.getAttachPrice() + (plug.getConsumePower() * plug.getUnitPrice() / 1000);
			if (moneyConsumption > plug.getLimitPrice()) // 当前充电已经透支
			{
				moneyConsumption = plug.getLimitPrice();
			}
			currentChargeApi.setMoneyConsumption(moneyConsumption);
			currentChargeApi.setPlugId(plug.getId());
			currentChargeApi.setPower(plug.getPower());
			currentChargeApi.setStartTime(plug.getStartTime());
			currentChargeApi.setUpdateTime(plug.getUpdateTime());
			
			currentCharges.add(currentChargeApi);
		}
		
		return currentCharges;
	}
	
	public void setInusePlugUpdateTimeNow(String deviceCode, int plugId) {
		Plug plug = plugRepository.findOne(new PlugPK(plugId, deviceCode));
		if (plug == null || !plug.getInUse())
		{
			logger.warn("设备插座不存在或插座没有在使用:deviceCode={},plugId={}", deviceCode, plugId);
			return;
		}
		
		plug.setUpdateTime(new Date());
		plugRepository.save(plug);
		return;
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PlugRepository plugRepository;
	
	@Autowired
	private DeviceRepository deviceRepository;
}
