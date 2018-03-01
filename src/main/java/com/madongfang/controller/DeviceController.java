package com.madongfang.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.alipay.api.AlipayApiException;
import com.madongfang.api.ChargeStartApi;
import com.madongfang.api.ChargeStartReturnApi;
import com.madongfang.api.DeviceApi;
import com.madongfang.api.DevicePromptApi;
import com.madongfang.api.PlugApi;
import com.madongfang.api.ReturnApi;
import com.madongfang.entity.Custom;
import com.madongfang.exception.HttpInternalServerErrorException;
import com.madongfang.service.DeviceService;
import com.madongfang.service.PlugService;
import com.madongfang.util.AlipayUtil;
import com.madongfang.util.CommonUtil;
import com.madongfang.util.DeviceControlUtil;
import com.madongfang.util.WechatUtil;
import com.madongfang.util.WechatUtil.TempleteMessage4;
import com.madongfang.util.WechatUtil.TempleteMessage5;

@RestController
@RequestMapping(value="/api/devices")
public class DeviceController {

	@PostMapping
	public DeviceApi addDevice(@RequestBody DeviceApi deviceApi) {
		return deviceService.addDevice(deviceApi);
	}
	
	@GetMapping
	public List<DeviceApi> getDevices() {
		return deviceService.getDevices();
	}
	
	@GetMapping(params={"longitude", "latitude", "distance"})
	public List<DeviceApi> getNearDevices(@RequestParam double longitude, @RequestParam double latitude, @RequestParam int distance)
	{	
		return deviceService.getNearDevices(longitude, latitude, distance);
	}
	
	@GetMapping(value="/{deviceCode}")
	public DeviceApi getDevice(@PathVariable String deviceCode)
	{
		return deviceService.getDevice(deviceCode);
	}
	
	@PostMapping(value="/{deviceCode}/activation")
	public ReturnApi activateDevice(@PathVariable String deviceCode)
	{
		return deviceService.activateDevice(deviceCode);
	}
	
	@GetMapping(value="/{deviceCode}/plugs")
	public List<PlugApi> getPlugs(@PathVariable String deviceCode) {
		return plugService.getPlugs(deviceCode);
	}
	
	@PostMapping(value="/{deviceCode}/plugs/{plugId}/chargeStart")
	public ChargeStartReturnApi chargeStart(HttpServletRequest request, @SessionAttribute Custom custom, 
			@PathVariable String deviceCode, @PathVariable int plugId, 
			@RequestBody ChargeStartApi chargeStartApi)
	{
		String basePath = request.getScheme() + "://" + request.getServerName() + 
				":" + request.getServerPort() + request.getContextPath();
		logger.debug("basePath={}", basePath);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		ChargeStartReturnApi chargeStartReturnApi = new ChargeStartReturnApi(0, "OK");
		
		DeviceService.StartChargeResult result = deviceService.startCharge(deviceCode, plugId, custom.getId(), chargeStartApi.getLimitPrice());
		if ("W".equals(custom.getType())) // 微信用户需要推送开始充电消息
		{
			TempleteMessage4 startMessage = new TempleteMessage4();
			startMessage.getFirst().setValue("您的充电已开始");
			startMessage.getKeyword1().setValue(result.getDeviceArea() + result.getDeviceLocation() + plugId + "号插座");
			startMessage.getKeyword2().setValue(sdf.format(result.getStartTime()));
			startMessage.getKeyword3().setValue(String.format("%.2f元", (float)chargeStartApi.getLimitPrice() / 100));
			startMessage.getKeyword4().setValue(String.format("%.2f元", (float)result.getCustomBalance() / 100));
			startMessage.getRemark().setValue("感谢您使用充电服务");
			try {
				wechatUtil.sendTempleteMessage(custom.getUserOpenId(), 
						wechatTemplateIdStartCharge, basePath + "/api/login?menu=mine", startMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("catch Exception:", e);
			}
		}
		
		if (!deviceControlUtil.startCharge(result.getDeviceId(), plugId, result.getLimitPower(), result.getLimitTime()))
		{
			logger.warn("设备网络通信异常: deviceCode={}, plugId={}", deviceCode, plugId);
			DeviceService.StopChargeResult stopResult = deviceService.exceptionStopCharge(deviceCode, plugId, "设备网络通信异常");
			if (stopResult != null && "W".equals(stopResult.getCustomType())) // 微信用户需要推送异常停止消息
			{	
				TempleteMessage5 stopMessage = new TempleteMessage5();
				stopMessage.getFirst().setValue("充电异常结束");
				stopMessage.getKeyword1().setValue(stopResult.getDeviceArea() + stopResult.getDeviceLocation() + plugId + "号插座");
				stopMessage.getKeyword2().setValue(sdf.format(stopResult.getStopTime()));
				stopMessage.getKeyword3().setValue(String.format("%.2f元", (float)stopResult.getSpend() / 100));
				stopMessage.getKeyword4().setValue(String.format("%.2f元", (float)stopResult.getRefund() / 100));
				stopMessage.getKeyword5().setValue(String.format("%.2f元", (float)stopResult.getCustomBalance() / 100));
				stopMessage.getRemark().setValue("设备网络通信异常，请稍后再试！");
				try {
					wechatUtil.sendTempleteMessage(stopResult.getUserOpenId(), 
							wechatTemplateIdStopCharge, basePath + "/api/login?menu=mine", stopMessage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("catch Exception:", e);
				}
			}
			chargeStartReturnApi.setReturnCode(-10);
			chargeStartReturnApi.setReturnMsg("供电站网络通信异常");
			throw new HttpInternalServerErrorException(chargeStartReturnApi);
		}
		else
		{
			plugService.setInusePlugUpdateTimeNow(deviceCode, plugId);
		}
		
		chargeStartReturnApi.setCustomBalance(result.getCustomBalance());
		chargeStartReturnApi.setDeviceArea(result.getDeviceArea());
		chargeStartReturnApi.setDeviceLocation(result.getDeviceLocation());
		chargeStartReturnApi.setStartTime(result.getStartTime());
		chargeStartReturnApi.setWechatSubscribe(true);
		try {
			if ("W".equals(custom.getType()) && !wechatUtil.isSubscribed(custom.getUserOpenId())) // 未关注公众号的微信用户
			{
				chargeStartReturnApi.setQrcodeAddress(basePath + "/api/images/" + qrcodeFile);
				chargeStartReturnApi.setWechatSubscribe(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("catch Exception:", e);
		}
		
		return chargeStartReturnApi;
	}
	
	@PostMapping(value="/{deviceCode}/plugs/{plugId}/chargeStop")
	public ReturnApi chargeStop(@SessionAttribute Custom custom, @PathVariable String deviceCode, @PathVariable int plugId)
	{
		return deviceService.customStopCharge(deviceCode, plugId, custom.getId());
	}
	
	@GetMapping(value="/{deviceCode}/prompts")
	public List<DevicePromptApi> getPrompts(@PathVariable String deviceCode)
	{
		return deviceService.getPrompts(deviceCode);
	}
	
	@PostMapping(value="/{deviceCode}/plugs/{plugId}/payAndStartCharge")
	public Map<String, Object> payAndStartCharge(HttpServletRequest request, @SessionAttribute Custom custom, 
			@PathVariable String deviceCode, @PathVariable int plugId, 
			@RequestBody ChargeStartApi chargeStartApi)
	{
		String basePath = request.getScheme()+"://"+request.getServerName()+request.getContextPath();
		logger.debug("basePath={}", basePath);
		
		String attach = deviceCode + ":" + plugId;
		String userAgent = request.getHeader("User-Agent");
		if (wechatUtil.isWechatBrowser(userAgent))
		{
			try {
				String tradeNumber = "W" + custom.getId() + "_" + System.currentTimeMillis() + "_";
				tradeNumber += commonUtil.getRandomStringByLength(32 - tradeNumber.length());
				
				return wechatUtil.unifiedOrder("云支付供电站", attach, 
						tradeNumber, chargeStartApi.getLimitPrice(), request.getRemoteAddr(), 
						basePath + "/api/payment/wechat/notify/startCharge", custom.getUserOpenId());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("wechatOrder Exception:", e);
				throw new HttpInternalServerErrorException(new ReturnApi(-1, e.getMessage()));
			}
		}
		else
		{
			String tradeNumber = "A" + custom.getId() + "_" + System.currentTimeMillis() + "_";
			tradeNumber += commonUtil.getRandomStringByLength(32 - tradeNumber.length());
			
			String returnUrl = "";
			try {
				if (chargeStartApi.getAlipayReturnUrl() != null)
				{
					returnUrl = URLEncoder.encode(chargeStartApi.getAlipayReturnUrl(), "UTF-8");
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				logger.error("catch Exception:", e);
			}
			
			logger.debug("URLEncoder returnUrl={}", returnUrl);
			
			try {
				String paymentForm = alipayUtil.order(tradeNumber, chargeStartApi.getLimitPrice(), "云支付供电站", 
						basePath + "/api/payment/alipay/return?returnUrl=" + returnUrl, 
						basePath + "/api/payment/alipay/notify/startCharge", attach);
				Map<String, Object> htmlMap = new HashMap<String, Object>();
				htmlMap.put("html", paymentForm);
				
				return htmlMap;
			} catch (AlipayApiException e) {
				// TODO Auto-generated catch block
				logger.error("AlipayApiException:", e);
				throw new HttpInternalServerErrorException(new ReturnApi(-1, e.getMessage()));
			}
		}
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${wechat.templateId.startCharge}")
	private String wechatTemplateIdStartCharge;
	
	@Value("${wechat.templateId.stopCharge}")
	private String wechatTemplateIdStopCharge;
	
	@Value("${manufacturer.qrcodeFile}")
	private String qrcodeFile;
	
	@Autowired
	private WechatUtil wechatUtil;
	
	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private PlugService plugService;
	
	@Autowired
	private DeviceControlUtil deviceControlUtil;
	
	@Autowired
	private AlipayUtil alipayUtil;
	
	@Autowired
	private CommonUtil commonUtil;
}
