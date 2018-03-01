package com.madongfang.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madongfang.api.deviceserver.AttachParamNotifyApi;
import com.madongfang.api.deviceserver.AttachParamReturnApi;
import com.madongfang.api.deviceserver.CardBalanceNotifyApi;
import com.madongfang.api.deviceserver.CardBalanceReturnApi;
import com.madongfang.api.deviceserver.CardChargeNotifyApi;
import com.madongfang.api.deviceserver.CardChargeReturnApi;
import com.madongfang.api.deviceserver.NotifyApi;
import com.madongfang.api.deviceserver.ParamNotifyApi;
import com.madongfang.api.deviceserver.ParamReturnApi;
import com.madongfang.api.ReturnApi;
import com.madongfang.api.deviceserver.StatusNotifyApi;
import com.madongfang.api.deviceserver.StatusReturnApi;
import com.madongfang.api.deviceserver.StopNotifyApi;
import com.madongfang.exception.HttpNotAcceptableException;
import com.madongfang.service.CustomService;
import com.madongfang.service.DeviceService;
import com.madongfang.util.CommonUtil;
import com.madongfang.util.HttpUtil;
import com.madongfang.util.WechatUtil;
import com.madongfang.util.WechatUtil.TempleteMessage5;

@Controller
@RequestMapping("/api/notify")
public class NotifyController {

	@PostMapping
	public String dispatchNotify(HttpServletRequest request)
	{
		try {
			String httpBody = httpUtil.getBody(request);
			logger.debug("notify request body={}", httpBody);
			
			ObjectMapper objectMapper = new ObjectMapper();
			NotifyApi notifyApi = objectMapper.readValue(httpBody, NotifyApi.class);
			switch (notifyApi.getType()) {
			case "stop":
				notifyApi = objectMapper.readValue(httpBody, StopNotifyApi.class);
				break;
				
			case "status":
				notifyApi = objectMapper.readValue(httpBody, StatusNotifyApi.class);
				break;
				
			case "param":
				notifyApi = objectMapper.readValue(httpBody, ParamNotifyApi.class);
				break;
				
			case "cardBalance":
				notifyApi = objectMapper.readValue(httpBody, CardBalanceNotifyApi.class);
				break;
				
			case "cardCharge":
				notifyApi = objectMapper.readValue(httpBody, CardChargeNotifyApi.class);
				break;
				
			case "attachParam":
				notifyApi = objectMapper.readValue(httpBody, AttachParamNotifyApi.class);
				break;

			default:
				request.setAttribute("returnApi", new ReturnApi(-1, String.format("未知命令类型(type=%s)", notifyApi.getType())));
				return "forward:/api/notify/error";
			}
			
			/* 校验签名 */
			String sign = notifyApi.getSign();
			notifyApi.setSign(null);
			if (sign == null || !sign.equals(commonUtil.getSign(notifyApi, password)))
			{
				request.setAttribute("returnApi", new ReturnApi(-2, "签名错误"));
				return "forward:/api/notify/error";
			}
			
			request.setAttribute("notifyApi", notifyApi);
			return "forward:/api/notify/" + notifyApi.getType();
			
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("catch Exception:", e);
			request.setAttribute("returnApi", new ReturnApi(-3, "异常:" + e.getMessage()));
			return "forward:/api/notify/error";
		}
	}
	
	@PostMapping(value="/stop")
	public @ResponseBody ReturnApi stop(HttpServletRequest request, 
			@RequestAttribute(name="notifyApi") StopNotifyApi stopNotifyApi)
	{
		String basePath = request.getScheme() + "://" + hostName + 
				":" + request.getServerPort() + request.getContextPath();
		logger.debug("basePath={}", basePath);
		
		DeviceService.StopChargeResult stopResult = deviceService.stopCharge(stopNotifyApi.getDeviceId(), stopNotifyApi.getPlugId(), stopNotifyApi.getRemain());
		if (stopResult != null && "W".equals(stopResult.getCustomType())) // 微信用户需要推送异常停止消息
		{	
			TempleteMessage5 stopMessage = new TempleteMessage5();
			String reason = "";
			if (stopNotifyApi.getStopReason() != null)
			{
				if (stopNotifyApi.getStopReason() == 0)
				{
					reason = ":付费电量耗尽";
				}
				else if (stopNotifyApi.getStopReason() == 1)
				{
					reason = ":浮充完成";
				}
				else if (stopNotifyApi.getStopReason() == 2)
				{
					reason = ":插头拔出";
				}
				else if (stopNotifyApi.getStopReason() == 3)
				{
					reason = ":功率超限";
				}
				else if (stopNotifyApi.getStopReason() == 4)
				{
					reason = ":计时时间结束";
				}
				else 
				{
					reason = ":" + stopNotifyApi.getStopReason();
				}
			}
			stopMessage.getFirst().setValue("您的充电已完成" + reason);
			stopMessage.getKeyword1().setValue(stopResult.getDeviceArea() + stopResult.getDeviceLocation() + stopNotifyApi.getPlugId() + "号插座");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			stopMessage.getKeyword2().setValue(sdf.format(stopResult.getStopTime()));
			stopMessage.getKeyword3().setValue(String.format("%.2f元", (float)stopResult.getSpend() / 100));
			stopMessage.getKeyword4().setValue(String.format("%.2f元", (float)stopResult.getRefund() / 100));
			stopMessage.getKeyword5().setValue(String.format("%.2f元", (float)stopResult.getCustomBalance() / 100));
			stopMessage.getRemark().setValue("点击“详情”可进入本次充电选择界面");
			try {
				wechatUtil.sendTempleteMessage(stopResult.getUserOpenId(), 
						wechatTemplateIdStopCharge, basePath + "/api/login?device="+stopResult.getDeviceCode(), stopMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("catch Exception:", e);
			}
		}
		return new ReturnApi(0, "OK");
	}
	
	@PostMapping(value="/status")
	public @ResponseBody StatusReturnApi status(@RequestAttribute(name="notifyApi") StatusNotifyApi statusNotifyApi)
	{
		return deviceService.updateAndGetStatus(statusNotifyApi.getDeviceId(), statusNotifyApi.getPlugStatusList());
	}
	
	@PostMapping(value="/param")
	public @ResponseBody ParamReturnApi param(@RequestAttribute(name="notifyApi") ParamNotifyApi paramNotifyApi)
	{
		return deviceService.getParam(paramNotifyApi.getDeviceId());
	}
	
	@PostMapping(value="/cardBalance")
	public @ResponseBody CardBalanceReturnApi cardBalance(@RequestAttribute(name="notifyApi") CardBalanceNotifyApi cardBalanceNotifyApi)
	{
		return customService.getCardBalance(cardBalanceNotifyApi.getCardId());
	}
	
	@PostMapping(value="/cardCharge")
	public @ResponseBody CardChargeReturnApi cardCharge(@RequestAttribute(name="notifyApi") CardChargeNotifyApi cardChargeNotifyApi)
	{
		return deviceService.cardChargeStart(cardChargeNotifyApi.getDeviceId(), cardChargeNotifyApi.getPlugId(), cardChargeNotifyApi.getCardId());
	}
	
	@PostMapping(value="/attachParam")
	public @ResponseBody AttachParamReturnApi attachParam(@RequestAttribute(name="notifyApi") AttachParamNotifyApi attachParamNotifyApi)
	{
		return deviceService.getAttachParam(attachParamNotifyApi.getDeviceId());
	}
	
	@PostMapping(value="/error")
	public @ResponseBody ReturnApi error(@RequestAttribute(name="returnApi") ReturnApi returnApi)
	{
		throw new HttpNotAcceptableException(returnApi);
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${deviceServer.password}")
	private String password;
	
	@Value("${wechat.templateId.stopCharge}")
	private String wechatTemplateIdStopCharge;
	
	@Value("${manufacturer.hostName}")
	private String hostName;
	
	@Autowired
	private WechatUtil wechatUtil;

	@Autowired
	private HttpUtil httpUtil;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private CustomService customService;
}
