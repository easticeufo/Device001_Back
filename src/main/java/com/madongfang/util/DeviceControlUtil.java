package com.madongfang.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madongfang.api.ReturnApi;
import com.madongfang.api.deviceserver.StartApi;
import com.madongfang.api.deviceserver.StopApi;

@Component
public class DeviceControlUtil {

	public boolean startCharge(String deviceId, int plugId, int limitPower, int limitTime) {
		StartApi startApi = new StartApi();
		startApi.setDeviceId(deviceId);
		startApi.setLimitPower(limitPower);
		startApi.setLimitTime(limitTime);
		startApi.setPlugId(plugId);
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			HttpUtil.HttpResponse response = httpUtil.postWithBasicAuth(baseUrl + "/api/start", 
					objectMapper.writeValueAsString(startApi), username, password);
			
			if (response.getStatusCode() == 200)
			{
				ReturnApi returnApi = objectMapper.readValue(response.getBody(), ReturnApi.class);
				if (returnApi.getReturnCode() == 0)
				{
					return true;
				}
			}
			
			logger.warn("DeviceControlUtil start response={}", response.getBody());
			return false;
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("catch Exception:", e);
			return false;
		}
		
	}
	
	public boolean stopCharge(String deviceId, int plugId)
	{
		StopApi stopApi = new StopApi();
		stopApi.setDeviceId(deviceId);
		stopApi.setPlugId(plugId);
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			HttpUtil.HttpResponse response = httpUtil.postWithBasicAuth(baseUrl + "/api/stop", 
					objectMapper.writeValueAsString(stopApi), username, password);
			
			if (response.getStatusCode() == 200)
			{
				ReturnApi returnApi = objectMapper.readValue(response.getBody(), ReturnApi.class);
				if (returnApi.getReturnCode() == 0)
				{
					return true;
				}
			}
			
			logger.warn("DeviceControlUtil start response={}", response.getBody());
			return false;
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("catch Exception:", e);
			return false;
		}
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private HttpUtil httpUtil;
	
	@Value("${deviceServer.baseUrl}")
	private String baseUrl;
	
	@Value("${deviceServer.username}")
	private String username;
	
	@Value("${deviceServer.password}")
	private String password;
}
