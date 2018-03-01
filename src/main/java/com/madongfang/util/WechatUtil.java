package com.madongfang.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@Component
public class WechatUtil {

	public final static String SCOPE_BASE = "snsapi_base";
	public final static String SCOPE_USERINFO = "snsapi_userinfo";
	
	/**
	 * @param userAgent http头中User-Agent字段的值
	 * @return 是否为微信浏览器
	 */
	public boolean isWechatBrowser(String userAgent) {
		return (userAgent.toLowerCase().indexOf("micromessenger") != -1);
	}
	
	/**
	 * 获取微信Oauth2登陆重定向的网址
	 * @param scope
	 * @param redirectUri
	 * @param state
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String oauth2Redirect(String scope, String redirectUri, String state) throws UnsupportedEncodingException
	{
		redirectUri = URLEncoder.encode(redirectUri, "UTF-8");

		if (state == null)
		{
			return String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s#wechat_redirect"
					, appId, redirectUri, scope);
		}
		else
		{
			state = URLEncoder.encode(state, "UTF-8");
			return String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s#wechat_redirect"
					, appId, redirectUri, scope, state);
		}
	}
	
	public String getOpenId(String code) throws JsonParseException, JsonMappingException, IOException
	{
		AccessData accessData = getAccessData(code);
    	if (accessData.access_token == null)
    	{
    		logger.warn("get access_token failed! 错误代码："+accessData.errcode + ", 错误原因：" + accessData.errmsg);
    		return null;
    	}
    	
    	return accessData.openid;
	}
	
	public UserInfo getUserInfo(String code) throws IOException {
		
		AccessData accessData = getAccessData(code);
    	if (null == accessData.access_token)
    	{
    		System.out.println("get access_token failed! 错误代码："+accessData.errcode + ", 错误原因：" + accessData.errmsg);
    		return null;
    	}
    	
		/* 拉取用户信息 */
		String jsonString = httpUtil.getToString(String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN"
			, accessData.access_token, accessData.openid));
		logger.debug("wechat userinfo={}", jsonString);
		ObjectMapper mapper = new ObjectMapper();
		UserInfo userInfo = mapper.readValue(jsonString, UserInfo.class);
    	if (null == userInfo.getOpenid())
    	{
    		logger.warn("getUserInfo failed! 错误代码："+userInfo.errcode + ", 错误原因：" + userInfo.errmsg);
    		return null;
    	}
    	return userInfo;
	}
	
	/**
	 * 微信支付统一下单
	 * @param body 商品简单描述
	 * @param attach 附加数据，在查询API和支付通知中原样返回
	 * @param tradeNumber 商户系统内部订单号，要求32个字符内、且在同一个商户号下唯一
	 * @param totalFee 订单总金额，单位为分
	 * @param ip 终端IP
	 * @param url 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
	 * @param openid 微信用户在商户对应appid下的唯一标识
	 * @return
	 * @throws IOException 
	 */
	public Map<String, Object> unifiedOrder(String body, String attach, String tradeNumber, int totalFee, String ip, String url, String openid) throws IOException 
	{		
		UnifiedOrderRequest unifiedOrderRequest = new UnifiedOrderRequest();
		
		unifiedOrderRequest.appid = appId;
		unifiedOrderRequest.mch_id = mchId;
		unifiedOrderRequest.nonce_str = commonUtil.getRandomStringByLength(32);
		unifiedOrderRequest.body = body;
		unifiedOrderRequest.attach = attach;
		unifiedOrderRequest.out_trade_no = tradeNumber;
		unifiedOrderRequest.total_fee = totalFee;
		unifiedOrderRequest.spbill_create_ip = ip;
		unifiedOrderRequest.notify_url = url;
		unifiedOrderRequest.trade_type = "JSAPI";
		unifiedOrderRequest.openid = openid;

		unifiedOrderRequest.sign = getSign(unifiedOrderRequest, key);

		XmlMapper xmlMapper = new XmlMapper();
		
		String xmlString = xmlMapper.writeValueAsString(unifiedOrderRequest);
		logger.debug("unifiedOrder xml:\n" + xmlString);
		xmlString = httpUtil.postToString("https://api.mch.weixin.qq.com/pay/unifiedorder", xmlString);
		logger.debug("unifiedOrderReturn xml:\n" + xmlString);
		UnifiedOrderResponse unifiedOrderResponse = xmlMapper.readValue(xmlString, UnifiedOrderResponse.class);
		
		if ("SUCCESS".equals(unifiedOrderResponse.return_code) && "SUCCESS".equals(unifiedOrderResponse.result_code))
		{
			TreeMap<String, Object> brandWCPayRequest = new TreeMap<String, Object>();
			brandWCPayRequest.put("appId", appId);
			brandWCPayRequest.put("timeStamp", String.valueOf((new Date()).getTime() / 1000));
			brandWCPayRequest.put("nonceStr", commonUtil.getRandomStringByLength(32));
			brandWCPayRequest.put("package", String.format("prepay_id=%s", unifiedOrderResponse.prepay_id));
			brandWCPayRequest.put("signType", "MD5");
			brandWCPayRequest.put("paySign", getSign(brandWCPayRequest, key));

			return brandWCPayRequest;
		}
		else
		{
			logger.error(String.format("unifiedorder failed:%s", unifiedOrderResponse.return_msg));
			throw new IOException("微信支付下单失败:" + unifiedOrderResponse.return_msg);
		}
	}
	
	public PaymentResult getPaymentResult(String notifyXml) throws JsonParseException, JsonMappingException, IOException 
	{		
		PaymentResult paymentResult = new PaymentResult();
		PaymentNotifyReturn paymentNotifyReturn = new PaymentNotifyReturn();
		XmlMapper xmlMapper = new XmlMapper();
		@SuppressWarnings("unchecked")
		TreeMap<String, Object> paymentNotifyMap = xmlMapper.readValue(notifyXml, TreeMap.class);
		
		String notifySign = (String)paymentNotifyMap.get("sign");
		paymentNotifyMap.remove("sign");
		if (!getSign(paymentNotifyMap, key).equals(notifySign))
		{
			paymentResult.success = false;
			paymentNotifyReturn.return_code = "FAIL";
			paymentNotifyReturn.return_msg = "签名失败";
		}
		else if (!"SUCCESS".equals(paymentNotifyMap.get("return_code")) || !"SUCCESS".equals(paymentNotifyMap.get("result_code")))
		{
			paymentResult.success = false;
			paymentNotifyReturn.return_code = "FAIL";
			paymentNotifyReturn.return_msg = "支付失败";
		}
		else
		{
			paymentResult.success = true;
			paymentNotifyReturn.return_code = "SUCCESS";
			paymentNotifyReturn.return_msg = "OK";
		}
		paymentResult.returnXml = xmlMapper.writeValueAsString(paymentNotifyReturn);
		
		if (paymentResult.success)
		{
			if ("Y".equals(paymentNotifyMap.get("is_subscribe")))
			{
				paymentResult.isSubscribe = true;
			}
			else {
				paymentResult.isSubscribe = false;
			}
			paymentResult.openid = (String)paymentNotifyMap.get("openid");
			paymentResult.attach = (String)paymentNotifyMap.get("attach");
			paymentResult.tradeNumber = (String)paymentNotifyMap.get("out_trade_no");
			paymentResult.transactionId = (String)paymentNotifyMap.get("transaction_id");
			paymentResult.totalFee = Integer.valueOf((String)paymentNotifyMap.get("total_fee"));
			paymentResult.paymentTime = (String)paymentNotifyMap.get("time_end");
		}
		
		return paymentResult;
	}
	
	public boolean refund(String tradeNumber, int totalFee, String refundNumber, int refundFee) throws KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, JsonProcessingException, IOException 
	{
		if (certFilePath == null)
		{
			logger.error("缺少商户证书路径");
			return false;
		}

		XmlMapper xmlMapper = new XmlMapper();
		
		RefundRequest refundRequest = new RefundRequest();
		refundRequest.appid = appId;
		refundRequest.mch_id = mchId;
		refundRequest.nonce_str = commonUtil.getRandomStringByLength(32);
		refundRequest.out_trade_no = tradeNumber;
		refundRequest.out_refund_no = refundNumber;
		refundRequest.total_fee = totalFee;
		refundRequest.refund_fee = refundFee;
		refundRequest.op_user_id = mchId;
		refundRequest.sign = getSign(refundRequest, key);
		
		String xml = httpUtil.postToStringWithCert("https://api.mch.weixin.qq.com/secapi/pay/refund", 
				xmlMapper.writeValueAsString(refundRequest), certFilePath, mchId);
		RefundResponse refundResponse = xmlMapper.readValue(xml, RefundResponse.class);
		
		if ("SUCCESS".equals(refundResponse.return_code) && "SUCCESS".equals(refundResponse.result_code))
		{
			logger.info(String.format("refund success: tradeNumber=%s, refundNumber=%s", tradeNumber, refundNumber));
			return true;
		}
		else
		{
			logger.warn(String.format("refund failed:%s:%s", refundResponse.return_msg, refundResponse.err_code_des));
			return false;
		}
	}
	
	public boolean refund(String tradeNumber, int totalFee, String refundNumber, int refundFee, String refundReason) throws KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, JsonProcessingException, IOException 
	{
		if (certFilePath == null)
		{
			logger.error("缺少商户证书路径");
			return false;
		}

		XmlMapper xmlMapper = new XmlMapper();
		
		RefundRequest refundRequest = new RefundRequest();
		refundRequest.appid = appId;
		refundRequest.mch_id = mchId;
		refundRequest.nonce_str = commonUtil.getRandomStringByLength(32);
		refundRequest.out_trade_no = tradeNumber;
		refundRequest.out_refund_no = refundNumber;
		refundRequest.total_fee = totalFee;
		refundRequest.refund_fee = refundFee;
		refundRequest.op_user_id = mchId;
		refundRequest.refund_desc = refundReason;
		refundRequest.sign = getSign(refundRequest, key);
		
		String xml = httpUtil.postToStringWithCert("https://api.mch.weixin.qq.com/secapi/pay/refund", 
				xmlMapper.writeValueAsString(refundRequest), certFilePath, mchId);
		RefundResponse refundResponse = xmlMapper.readValue(xml, RefundResponse.class);
		
		if ("SUCCESS".equals(refundResponse.return_code) && "SUCCESS".equals(refundResponse.result_code))
		{
			logger.info(String.format("refund success: tradeNumber=%s, refundNumber=%s", tradeNumber, refundNumber));
			return true;
		}
		else
		{
			logger.warn(String.format("refund failed:%s:%s", refundResponse.return_msg, refundResponse.err_code_des));
			return false;
		}
	}
	
	public boolean transfer(String transferNumber, int amount, String openid, String description) throws KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, JsonProcessingException, IOException 
	{
    	if (certFilePath == null)
		{
			logger.error("缺少商户证书路径");
			return false;
		}
    	
    	XmlMapper xmlMapper = new XmlMapper();
    	
    	TransferRequest transferRequest = new TransferRequest();
    	transferRequest.mch_appid = appId;
    	transferRequest.mchid = mchId;
    	transferRequest.nonce_str = commonUtil.getRandomStringByLength(32);
    	transferRequest.partner_trade_no = transferNumber;
    	transferRequest.openid = openid;
    	transferRequest.check_name = "NO_CHECK";
    	transferRequest.amount = amount; // 单位“分”
    	transferRequest.desc = description;
    	transferRequest.spbill_create_ip = InetAddress.getLocalHost().getHostAddress();
    	transferRequest.sign = getSign(transferRequest, key);
    	
    	logger.debug("spbill_create_ip=" + transferRequest.spbill_create_ip);

		String xml = httpUtil.postToStringWithCert("https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers", 
				xmlMapper.writeValueAsString(transferRequest), certFilePath, mchId);
		TransferResponse transferResponse = xmlMapper.readValue(xml, TransferResponse.class);
		
		if ("SUCCESS".equals(transferResponse.return_code) && "SUCCESS".equals(transferResponse.result_code))
		{
			logger.info("transfer success: transferNumber=" + transferNumber);
			return true;
		}
		else
		{
			logger.warn(String.format("transfer failed:%s:%s", transferResponse.return_msg, transferResponse.err_code_des));
			return false;
		}
	}
	
	public AccessTokenThread getAccessTokenThread() {
		return accessTokenThread;
	}
	
	public boolean isSubscribed(String openid) throws IOException
	{
		return isSubscribed(openid, accessTokenThread.getAccessToken());
	}
	
	public boolean isSubscribed(String openid, String accessToken) throws IOException
	{
		if (accessToken == null)
		{
			logger.error("accessToken is null");
			return false;
		}
		
		String jsonString = httpUtil.getToString(String.format("https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s"
			, accessToken, openid));
		
		ObjectMapper mapper = new ObjectMapper();
		Subscribe subscribe = mapper.readValue(jsonString, Subscribe.class);
    	if (null == subscribe.subscribe)
    	{
    		logger.warn("isSubscribed failed:errcode={},errmsg={}", subscribe.errcode, subscribe.errmsg);
    		return false;
    	}
    	
    	if (0 == subscribe.subscribe)
    	{
    		return false;
    	}
    	else 
    	{
			return true;
		}
	}
	
	public void sendTempleteMessage(String openId, String templateId, String url, Object data) throws IOException
	{
		sendTempleteMessage(openId, templateId, url, data, accessTokenThread.getAccessToken());
	}
	
	public void sendTempleteMessage(String openId, String templateId, String url, Object data, String accessToken) throws IOException {
		if (accessToken == null)
		{
			logger.error("accessToken is null");
			return;
		}
		
		TempleteMessageRequest messageRequest = new TempleteMessageRequest();
		messageRequest.touser = openId;
		messageRequest.template_id = templateId;
		messageRequest.url = url;
		messageRequest.data = data;
		
		ObjectMapper mapper = new ObjectMapper();
		String body = mapper.writeValueAsString(messageRequest);
		String json = httpUtil.postToString(String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken), body);
		TempleteMessageResponse messageResponse = mapper.readValue(json, TempleteMessageResponse.class);
		if (0 != messageResponse.errcode)
		{
			logger.warn("sendTempleteMessage failed:errmsg={}", messageResponse.errmsg);
		}
		
		return;
	}
	
	public JsSdkConfig getJsSdkConfig(String url) {
		return getJsSdkConfig(accessTokenThread.getJsapiTicket(), url);
	}
	
	public JsSdkConfig getJsSdkConfig(String jsapiTicket, String url) {
		if (jsapiTicket == null)
		{
			logger.error("jsapiTicket is null");
			return null;
		}
		
		String nonceStr = commonUtil.getRandomStringByLength(16);
        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        
        String str = "jsapi_ticket=" + jsapiTicket + "&noncestr=" + nonceStr 
        		+ "&timestamp=" + timestamp + "&url=" + url;
        String signature = commonUtil.sha1(str);
        
        logger.debug("str={}, signature={}", str, signature);
        
        JsSdkConfig jsSdkConfig = new JsSdkConfig();
        jsSdkConfig.setAppId(appId);
        jsSdkConfig.setNonceStr(nonceStr);
        jsSdkConfig.setSignature(signature);
        jsSdkConfig.setTimestamp(timestamp);
        
        return jsSdkConfig;
	}

	public interface AccessTokenCallback {
		public void refreshAccessToken(String accessToken, String jsapiTicket, Integer expiresIn);
	}
	
	public class AccessTokenThread extends Thread {
		
		public String getAccessToken() {
			return accessToken;
		}

		public void setAccessTokenCallback(AccessTokenCallback accessTokenCallback) {
			this.accessTokenCallback = accessTokenCallback;
		}

		public String getJsapiTicket() {
			return jsapiTicket;
		}

		public void setJsapiTicket(String jsapiTicket) {
			this.jsapiTicket = jsapiTicket;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int expireTime = 0;
			int timeCount = 0;
			
			while (!exit)
			{
				try {
					if (timeCount > expireTime - 60 * 20)
					{
						try {
							ObjectMapper mapper = new ObjectMapper();
							
							String json = httpUtil.getToString(String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", 
									appId, appSecret));
							
							BaseAccessToken baseAccessToken = mapper.readValue(json, BaseAccessToken.class);
							
							if (baseAccessToken.access_token != null)
							{
								accessToken = baseAccessToken.access_token;
								expireTime = baseAccessToken.expires_in;
								logger.debug("accessToken={}, expires_in={}", 
										baseAccessToken.access_token, baseAccessToken.expires_in);
								
								timeCount = 0;
								
								json = httpUtil.getToString(String.format("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi", baseAccessToken.access_token));
								
								JsapiTicketInfo jsapiTicketInfo = mapper.readValue(json, JsapiTicketInfo.class);
								jsapiTicket = jsapiTicketInfo.ticket;
								
								if (accessTokenCallback != null)
								{
									accessTokenCallback.refreshAccessToken(accessToken, jsapiTicket, expireTime);
								}
							}
							else if (baseAccessToken.errcode != null)
							{
								logger.error("get base access token failed:" + baseAccessToken.errmsg);
							}
							else {
								logger.error("get base access token return error:json={}", json);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							logger.error("AccessTokenThread catch IOException:", e);
						}
					}
					
					sleep(60 * 1000);
					timeCount += 60;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("AccessTokenThread Interrupted!"); // 线程退出
				}
			}
			
			System.out.println("AccessTokenThread stopped!");
		}
		
		public void stopThread() {
			exit = true;
			interrupt();
			try {
				join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private volatile boolean exit = false; 
		
		private String accessToken = null;
		
		private String jsapiTicket = null;
		
		private AccessTokenCallback accessTokenCallback = null;

		private AccessTokenThread() {
			super();
		}
	}
	
	/**
	 * 微信用户信息
	 * 
	 * @author madongfang
	 *
	 */
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class UserInfo{
		private String openid;
		private String nickname;
		private Integer sex;
		private String language;
		private String province;
		private String city;
		private String country;
		private String headimgurl;
		private String unionid;
		private String errcode;
		private String errmsg;
		public String getOpenid() {
			return openid;
		}
		public void setOpenid(String openid) {
			this.openid = openid;
		}
		public String getNickname() {
			return nickname;
		}
		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
		public Integer getSex() {
			return sex;
		}
		public void setSex(Integer sex) {
			this.sex = sex;
		}
		public String getLanguage() {
			return language;
		}
		public void setLanguage(String language) {
			this.language = language;
		}
		public String getProvince() {
			return province;
		}
		public void setProvince(String province) {
			this.province = province;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getCountry() {
			return country;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		public String getHeadimgurl() {
			return headimgurl;
		}
		public void setHeadimgurl(String headimgurl) {
			this.headimgurl = headimgurl;
		}
		public String getUnionid() {
			return unionid;
		}
		public void setUnionid(String unionid) {
			this.unionid = unionid;
		}
		public String getErrcode() {
			return errcode;
		}
		public void setErrcode(String errcode) {
			this.errcode = errcode;
		}
		public String getErrmsg() {
			return errmsg;
		}
		public void setErrmsg(String errmsg) {
			this.errmsg = errmsg;
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			if (openid == null)
			{
				return String.format("获取用户信息失败(errcode:%s,errmsg:%s)", errcode, errmsg);
			}
			else
			{
				StringBuilder sb = new StringBuilder("微信用户信息：");
				sb.append("\nopenid:");
				sb.append(openid);
				sb.append("\nnickname:");
				sb.append(nickname);
				sb.append("\nsex:");
				sb.append(sex);
				sb.append("\nlanguage:");
				sb.append(language);
				sb.append("\nprovince:");
				sb.append(province);
				sb.append("\ncity:");
				sb.append(city);
				sb.append("\ncountry:");
				sb.append(country);
				sb.append("\nheadimgurl:");
				sb.append(headimgurl);
				sb.append("\nunionid:");
				sb.append(unionid);
				return sb.toString();
			}
		}
	}
	
	/**
	 * 微信支付通知的结果
	 * 
	 * @author madongfang
	 *
	 */
	public static class PaymentResult{
		private String returnXml;
		private boolean success;
		private String openid;
		private boolean isSubscribe; // 用户是否关注公众账号
		private String attach;
		private String tradeNumber; // 商户订单号
		private String transactionId; // 微信支付订单号
		private Integer totalFee;
		private String paymentTime;
		public String getReturnXml() {
			return returnXml;
		}
		public void setReturnXml(String returnXml) {
			this.returnXml = returnXml;
		}
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getOpenid() {
			return openid;
		}
		public void setOpenid(String openid) {
			this.openid = openid;
		}
		public boolean isSubscribe() {
			return isSubscribe;
		}
		public void setSubscribe(boolean isSubscribe) {
			this.isSubscribe = isSubscribe;
		}
		public String getAttach() {
			return attach;
		}
		public void setAttach(String attach) {
			this.attach = attach;
		}
		public String getTradeNumber() {
			return tradeNumber;
		}
		public void setTradeNumber(String tradeNumber) {
			this.tradeNumber = tradeNumber;
		}
		public String getTransactionId() {
			return transactionId;
		}
		public void setTransactionId(String transactionId) {
			this.transactionId = transactionId;
		}
		public Integer getTotalFee() {
			return totalFee;
		}
		public void setTotalFee(Integer totalFee) {
			this.totalFee = totalFee;
		}
		public String getPaymentTime() {
			return paymentTime;
		}
		public void setPaymentTime(String paymentTime) {
			this.paymentTime = paymentTime;
		}
		
	}
	
	public static class TempleteMessageUnit {
		
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getColor() {
			return color;
		}
		public void setColor(String color) {
			this.color = color;
		}
		private String value = "";
		private String color = "#173177";
	}

	public static class TempleteMessage4{
		
		public TempleteMessageUnit getFirst() {
			return first;
		}
		public TempleteMessageUnit getKeyword1() {
			return keyword1;
		}
		public TempleteMessageUnit getKeyword2() {
			return keyword2;
		}
		public TempleteMessageUnit getKeyword3() {
			return keyword3;
		}
		public TempleteMessageUnit getKeyword4() {
			return keyword4;
		}
		public TempleteMessageUnit getRemark() {
			return remark;
		}
		private TempleteMessageUnit first = new TempleteMessageUnit();
		private TempleteMessageUnit keyword1 = new TempleteMessageUnit();
		private TempleteMessageUnit keyword2 = new TempleteMessageUnit();
		private TempleteMessageUnit keyword3 = new TempleteMessageUnit();
		private TempleteMessageUnit keyword4 = new TempleteMessageUnit();
		private TempleteMessageUnit remark = new TempleteMessageUnit();
	}

	public static class TempleteMessage5{

		public TempleteMessageUnit getFirst() {
			return first;
		}
		public TempleteMessageUnit getKeyword1() {
			return keyword1;
		}
		public TempleteMessageUnit getKeyword2() {
			return keyword2;
		}
		public TempleteMessageUnit getKeyword3() {
			return keyword3;
		}
		public TempleteMessageUnit getKeyword4() {
			return keyword4;
		}
		public TempleteMessageUnit getKeyword5() {
			return keyword5;
		}
		public TempleteMessageUnit getRemark() {
			return remark;
		}
		private TempleteMessageUnit first = new TempleteMessageUnit();
		private TempleteMessageUnit keyword1 = new TempleteMessageUnit();
		private TempleteMessageUnit keyword2 = new TempleteMessageUnit();
		private TempleteMessageUnit keyword3 = new TempleteMessageUnit();
		private TempleteMessageUnit keyword4 = new TempleteMessageUnit();
		private TempleteMessageUnit keyword5 = new TempleteMessageUnit();
		private TempleteMessageUnit remark = new TempleteMessageUnit();
	}
	
	public static class JsSdkConfig {
		public String getAppId() {
			return appId;
		}
		public void setAppId(String appId) {
			this.appId = appId;
		}
		public String getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}
		public String getNonceStr() {
			return nonceStr;
		}
		public void setNonceStr(String nonceStr) {
			this.nonceStr = nonceStr;
		}
		public String getSignature() {
			return signature;
		}
		public void setSignature(String signature) {
			this.signature = signature;
		}
		private String appId;
		private String timestamp;
		private String nonceStr;
		private String signature;
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AccessTokenThread accessTokenThread = new AccessTokenThread();
	
	@Autowired
	private HttpUtil httpUtil;
	@Autowired
	private CommonUtil commonUtil;
	
	@Value("${wechat.appId}")
	private String appId;
	@Value("${wechat.appSecret}")
	private String appSecret;
	@Value("${wechat.mchId}")
	private String mchId;
	@Value("${wechat.key}")
	private String key; // 微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	@Value("${wechat.certFilePath:#{null}}")
	private String certFilePath; // 商户证书路径，退款申请API和企业付款API中需要使用，微信商户平台(pay.weixin.qq.com)-->账户中心-->账户设置-->API安全-->证书下载
	
	private AccessData getAccessData(String code) throws JsonParseException, JsonMappingException, IOException {
		/* 获取Access token */
		String jsonString = httpUtil.getToString(String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code"
				, appId, appSecret, code));
		
		ObjectMapper mapper = new ObjectMapper();
		
    	return mapper.readValue(jsonString, AccessData.class);
	}
	
	/**
	 * 微信支付签名算法生成签名
	 * 
	 * @param obj
	 * @param key
	 * @return
	 */
	private String getSign(Object obj, String key) {
		TreeMap<String, Object> paramMap = new TreeMap<String, Object>();
		
		Class<? extends Object> type = obj.getClass();
		
		try {
			Field[] fields= type.getDeclaredFields();
			for (Field field: fields)
			{
				if (field.get(obj) == null)
				{
					continue;
				}
				paramMap.put(field.getName(), field.get(obj));
			}
			
			return getSign(paramMap, key);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("catch Exception:", e);
			return null;
		}
	}
	
	/**
	 * 微信支付签名算法生成签名
	 * 
	 * @param paramMap
	 * @param key
	 * @return
	 */
	private String getSign(TreeMap<String, Object> paramMap, String key){
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> param : paramMap.entrySet()) {
			sb.append(param.getKey());
			sb.append("=");
			sb.append(param.getValue());
			sb.append("&");
		}
		sb.append("key=");
		sb.append(key);
		logger.debug("wechat sign string=" + sb.toString());
		
		return commonUtil.md5(sb.toString());
	}
	
	@SuppressWarnings("unused")
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class AccessData{
		public String access_token;
		public Integer expires_in; 
		public String refresh_token;
		public String openid;
		public String scope;
		public String errcode;
		public String errmsg;
	}
	
	/**
	 * 微信统一下单API请求参数
	 * @author madongfang
	 *
	 */
	@SuppressWarnings("unused")
	@JacksonXmlRootElement(localName="xml")
	private static class UnifiedOrderRequest{
		public String appid;
		public String mch_id;
		public String sign;
		public String body;
		public String nonce_str;
		public String attach;
		public String out_trade_no;
		public Integer total_fee;
		public String spbill_create_ip;
		public String notify_url;
		public String trade_type;
		public String openid;
	}
	
	/**
	 * 微信统一下单API返回结果
	 * @author madongfang
	 *
	 */
	@SuppressWarnings("unused")
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class UnifiedOrderResponse{
		public String return_code;
		public String return_msg;
		public String appid;
		public String mch_id;
		public String device_info;
		public String nonce_str;
		public String sign;
		public String result_code;
		public String err_code;
		public String err_code_des;
		public String trade_type;
		public String prepay_id;
		public String code_url;
	}
	
	/**
	 * 微信支付结果通知参数
	 * @author madongfang
	 *
	 */
	@SuppressWarnings("unused")
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class PaymentNotify {
		public String return_code;
		public String return_msg;
		public String appid;
		public String mch_id;
		public String device_info;
		public String nonce_str;
		public String sign;
		public String result_code;
		public String err_code;
		public String err_code_des;
		public String openid;
		public String is_subscribe;
		public String trade_type;
		public String bank_type;
		public String total_fee;
		public String settlement_total_fee;
		public String fee_type;
		public String cash_fee;
		public String cash_fee_type;
		public String transaction_id;
		public String out_trade_no;
		public String attach;
		public String time_end;
	}
	
	/**
	 * 微信支付结果通知，商户处理后同步返回给微信的参数
	 * 
	 * @author madongfang
	 *
	 */
	@JacksonXmlRootElement(localName="xml")
	private static class PaymentNotifyReturn {
		@JacksonXmlCData
		public String return_code;
		@JacksonXmlCData
		public String return_msg;
	}
	
	/**
	 * 微信退款API请求参数
	 * 
	 * @author madongfang
	 *
	 */
	@SuppressWarnings("unused")
	@JacksonXmlRootElement(localName="xml")
	private static class RefundRequest {
		public String appid;
		public String mch_id;
		public String nonce_str;
		public String sign;
		public String out_trade_no; // 商户订单号
		public String out_refund_no; // 商户退款单号
		public Integer total_fee; // 订单金额
		public Integer refund_fee; // 退款金额
		public String op_user_id; // 操作员帐号, 默认为商户号
		public String refund_desc; // 退款原因
	}

	/**
	 * 微信退款API返回结果
	 * 
	 * @author madongfang
	 *
	 */
	@SuppressWarnings("unused")
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class RefundResponse {
		public String return_code;
		public String return_msg;
		public String result_code;
		public String err_code;
		public String err_code_des;
		public String appid;
		public String mch_id;
		public String device_info;
		public String nonce_str;
		public String sign;
		public String transaction_id;
		public String out_trade_no;
		public String out_refund_no;
		public String refund_id;
		public String refund_channel;
		public Integer refund_fee;
		public Integer total_fee;
		public Integer cash_fee;
		public Integer cash_refund_fee;
		public Integer coupon_refund_fee;
		public Integer coupon_refund_count;
	}
	
	/**
	 * 微信企业付款API请求参数
	 * 
	 * @author madongfang
	 *
	 */
	@SuppressWarnings("unused")
	@JacksonXmlRootElement(localName="xml")
	private static class TransferRequest{
		public String mch_appid;
		public String mchid;
		public String nonce_str;
		public String sign;
		public String partner_trade_no;
		public String openid;
		public String check_name;
		public Integer amount;
		public String desc;
		public String spbill_create_ip;
	}

	/**
	 * 微信企业付款API返回参数
	 * 
	 * @author madongfang
	 *
	 */
	@SuppressWarnings("unused")
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class TransferResponse{
		public String return_code;
		public String return_msg;
		public String mch_appid;
		public String mchid;
		public String device_info;
		public String nonce_str;
		public String result_code;
		public String err_code;
		public String err_code_des;
		public String partner_trade_no;
		public String payment_no;
		public String payment_time;
	}
	
	/**
	 * 基础支持中获取access_token的返回参数
	 * 
	 * @author madongfang
	 *
	 */
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class BaseAccessToken{
		public String access_token; // 获取到的凭证
		public Integer expires_in; // 凭证有效时间，单位：秒
		public Integer errcode;
		public String errmsg;
	}
	
	@SuppressWarnings("unused")
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class JsapiTicketInfo{
		public String ticket; // 获取到的jsapi_ticket
		public Integer expires_in; // 凭证有效时间，单位：秒
		public Integer errcode;
		public String errmsg;
	}
	
	/**
	 * 对话服务->用户管理->获取用户基本信息
	 * 
	 * @author madongfang
	 *
	 */
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class Subscribe
	{
		public Integer subscribe;
		public Integer errcode;
		public String errmsg;
	}
	
	@SuppressWarnings("unused")
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class TempleteMessageRequest{
		public String touser;
		public String template_id;
		public String url;
		public Object data;
	}

	@SuppressWarnings("unused")
	@JsonIgnoreProperties(ignoreUnknown=true)
	private static class TempleteMessageResponse{
		public Integer errcode;
		public String errmsg;
		public Long msgid;
	}
}
