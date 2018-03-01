package com.madongfang.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.alipay.api.AlipayApiException;
import com.madongfang.api.OrderApi;
import com.madongfang.api.PaymentOptionApi;
import com.madongfang.api.ReturnApi;
import com.madongfang.entity.Custom;
import com.madongfang.exception.HttpInternalServerErrorException;
import com.madongfang.service.DeviceService;
import com.madongfang.service.PaymentService;
import com.madongfang.util.AlipayUtil;
import com.madongfang.util.CommonUtil;
import com.madongfang.util.WechatUtil;
import com.madongfang.util.WechatUtil.TempleteMessage4;

@Controller
@RequestMapping(value="/api/payment")
public class PaymentController {

	@GetMapping("/options")
	public @ResponseBody List<PaymentOptionApi> getPaymentOptions()
	{
		return paymentService.getPaymentOptions();
	}
	
	@PostMapping("/wechat/order")
	public @ResponseBody Map<String, Object> wechatOrder(HttpServletRequest request, 
			@RequestBody OrderApi orderApi, @SessionAttribute Custom custom)
	{
		String basePath = request.getScheme()+"://"+request.getServerName()+request.getContextPath();
		try {
			String tradeNumber = "W" + custom.getId() + "_" + System.currentTimeMillis() + "_";
			tradeNumber += commonUtil.getRandomStringByLength(32 - tradeNumber.length());
			
			return wechatUtil.unifiedOrder("云支付供电站", String.valueOf(custom.getId()), 
					tradeNumber, orderApi.getAmount(), request.getRemoteAddr(), 
					basePath + "/api/payment/wechat/notify", custom.getUserOpenId());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("wechatOrder Exception:", e);
			throw new HttpInternalServerErrorException(new ReturnApi(-1, e.getMessage()));
		}
	}
	
	@PostMapping("/alipay/order")
	public @ResponseBody Map<String, Object> alipayOrder(HttpServletRequest request, 
			@RequestBody OrderApi orderApi, @SessionAttribute Custom custom)
	{
		String basePath = request.getScheme()+"://"+request.getServerName()+request.getContextPath();
		
		try {
			String tradeNumber = "A" + custom.getId() + "_" + System.currentTimeMillis() + "_";
			tradeNumber += commonUtil.getRandomStringByLength(32 - tradeNumber.length());
			
			String returnUrl = "";
			try {
				if (orderApi.getReturnUrl() != null)
				{
					returnUrl = URLEncoder.encode(orderApi.getReturnUrl(), "UTF-8");
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				logger.error("catch Exception:", e);
			}
			
			logger.debug("URLEncoder returnUrl={}", returnUrl);
			
			String paymentForm = alipayUtil.order(tradeNumber, orderApi.getAmount(), "云支付供电站", 
					basePath + "/api/payment/alipay/return?returnUrl=" + returnUrl, 
					basePath + "/api/payment/alipay/notify", String.valueOf(custom.getId()));
			
			Map<String, Object> htmlMap = new HashMap<String, Object>();
			htmlMap.put("html", paymentForm);
			
			return htmlMap;
		} catch (AlipayApiException e) {
			// TODO Auto-generated catch block
			logger.error("alipayOrder Exception:", e);
			throw new HttpInternalServerErrorException(new ReturnApi(-1, e.getMessage()));
		}
	}
	
	@PostMapping("/wechat/notify")
	public @ResponseBody String wechatNotify(HttpServletRequest request, @RequestBody String notifyXml)
	{
		logger.debug("notifyXml={}", notifyXml);
		
		String basePath = request.getScheme() + "://" + request.getServerName() + 
				":" + request.getServerPort() + request.getContextPath();
		logger.debug("basePath={}", basePath);
		
		try {
			WechatUtil.PaymentResult paymentResult = wechatUtil.getPaymentResult(notifyXml);
			if (paymentResult.isSuccess())
			{
				int amount = paymentResult.getTotalFee();
				int customId = Integer.valueOf(paymentResult.getAttach());
				String tradeNumber = paymentResult.getTradeNumber();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date time = sdf.parse(paymentResult.getPaymentTime());
				String openid = paymentResult.getOpenid();
				
				logger.debug("wechat notify: amount={}, customId={}, tradeNumber={}, time={}, ReturnXml={}", 
						amount, customId, tradeNumber, time, paymentResult.getReturnXml());
				PaymentService.RechargeResult rechargeResult = paymentService.recharge(customId, amount, tradeNumber, time, openid);
				
				if (rechargeResult != null && "W".equals(rechargeResult.getCustomType())) // 给微信用户推送充值成功消息
				{
					TempleteMessage4 rechargeMessage = new TempleteMessage4();
					rechargeMessage.getFirst().setValue("您的充值已完成");
					sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
					rechargeMessage.getKeyword1().setValue(sdf.format(time));
					rechargeMessage.getKeyword2().setValue(String.format("%.2f元", (float)amount / 100));
					rechargeMessage.getKeyword3().setValue(String.format("%.2f元", (float)rechargeResult.getGiftAmount() / 100));
					rechargeMessage.getKeyword4().setValue(String.format("%.2f元", (float)rechargeResult.getCustomBalance() / 100));
					rechargeMessage.getRemark().setValue("感谢您使用充值服务");
					try {
						wechatUtil.sendTempleteMessage(rechargeResult.getUserOpenId(), 
								wechatTemplateIdRecharge, basePath + "/api/login?menu=mine", rechargeMessage);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						logger.error("catch Exception:", e);
					}
				}
			}
			
			return paymentResult.getReturnXml();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			logger.error("wechatNotify Exception:", e);
			return "error";
		}
	}
	
	@PostMapping("/alipay/notify")
	public @ResponseBody String alipayNotify(@RequestParam Map<String, String> params)
	{
		logger.debug("requestParams={}", params);
		
		try {
			if (alipayUtil.checkSignature(params))
			{
				String tradeStatus = params.get("trade_status");
				if (tradeStatus != null && (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")))
				{
					int amount = (int)(Float.valueOf(params.get("total_amount")) * 100);
					int customId = Integer.valueOf(params.get("passback_params"));
					String tradeNumber = params.get("out_trade_no");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date time = sdf.parse(params.get("gmt_payment"));
					String userId = params.get("buyer_id");
					
					logger.debug("alipay notify: amount={}, customId={}, tradeNumber={}, time={}", 
							amount, customId, tradeNumber, time);
					paymentService.recharge(customId, amount, tradeNumber, time, userId);
				}
				
				return "success";
			}
			else
			{
				logger.error("alipayUtil.checkSignature false!");
				return "failure";
			}
		} catch (AlipayApiException | ParseException e) {
			// TODO Auto-generated catch block
			logger.error("alipayNotify Exception:", e);
			return "failure";
		}
	}
	
	@PostMapping("/wechat/notify/startCharge")
	public @ResponseBody String wechatNotifyStartCharge(HttpServletRequest request, @RequestBody String notifyXml)
	{
		logger.debug("notifyXml={}", notifyXml);
		
		String basePath = request.getScheme() + "://" + request.getServerName() + 
				":" + request.getServerPort() + request.getContextPath();
		logger.debug("basePath={}", basePath);
		
		try {
			WechatUtil.PaymentResult paymentResult = wechatUtil.getPaymentResult(notifyXml);
			if (paymentResult.isSuccess())
			{
				int limitPrice = paymentResult.getTotalFee();
				String[] args = paymentResult.getAttach().split(":");
				String deviceCode = args[0];
				int plugId = Integer.valueOf(args[1]);
				String tradeNumber = paymentResult.getTradeNumber();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date time = sdf.parse(paymentResult.getPaymentTime());
				
				logger.debug("wechat notify: limitPrice={}, tradeNumber={}, time={}, ReturnXml={}", 
						limitPrice, tradeNumber, time, paymentResult.getReturnXml());
				
				deviceService.startChargeThread(deviceCode, plugId, "W", paymentResult.getOpenid(), tradeNumber, limitPrice);
			}
			
			return paymentResult.getReturnXml();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			logger.error("wechatNotify Exception:", e);
			return "error";
		}
	}
	
	@PostMapping("/alipay/notify/startCharge")
	public @ResponseBody String alipayNotifyStartCharge(@RequestParam Map<String, String> params)
	{
		logger.debug("requestParams={}", params);
		
		try {
			if (alipayUtil.checkSignature(params))
			{
				String tradeStatus = params.get("trade_status");
				if (tradeStatus != null && (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")))
				{
					int limitPrice = (int)(Float.valueOf(params.get("total_amount")) * 100);
					String[] args = params.get("passback_params").split(":");
					String deviceCode = args[0];
					int plugId = Integer.valueOf(args[1]);
					String tradeNumber = params.get("out_trade_no");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date time = sdf.parse(params.get("gmt_payment"));
					String userId = params.get("buyer_id");
					
					logger.debug("alipay notify: limitPrice={}, tradeNumber={}, time={}", 
							limitPrice, tradeNumber, time);
					
					deviceService.startChargeThread(deviceCode, plugId, "A", userId, tradeNumber, limitPrice);
				}
				
				return "success";
			}
			else
			{
				logger.error("alipayUtil.checkSignature false!");
				return "failure";
			}
		} catch (AlipayApiException | ParseException e) {
			// TODO Auto-generated catch block
			logger.error("alipayNotify Exception:", e);
			return "failure";
		}
	}
	
	@GetMapping("/alipay/return")
	public String alipayReturn(@RequestParam String returnUrl, Model model) // 支付宝付款后，前端页面先会跳到这里再重定向到前端页面，解决回跳页面带有"?"参数导致angularjs路由解析错误的问题
	{
		logger.debug("returnUrl={}", returnUrl);
		
		if (returnUrl.length() == 0)
		{
			model.addAttribute("errorInfo", "returnUrl错误!");
			return "error";
		}
		else
		{
			return "redirect:" + returnUrl;
		}
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${wechat.templateId.recharge}")
	private String wechatTemplateIdRecharge;

	@Autowired
	private WechatUtil wechatUtil;
	
	@Autowired
	private AlipayUtil alipayUtil;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private DeviceService deviceService;
}
