package com.madongfang.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;

@Component
public class AlipayUtil {

	public final static String SCOPE_BASE = "auth_base";
	public final static String SCOPE_USER = "auth_user";
	
	@PostConstruct
	public void init() {
		alipayClient = new DefaultAlipayClient(gateway, appId, privateKey, "json", CHARSET, publicKey);
	}
	
	public boolean isAlipayBrowser(String userAgent) 
	{
		return (userAgent.toLowerCase().indexOf("alipay") != -1);
	}
	
	public boolean isSandBox() 
	{
		if ("https://openapi.alipaydev.com/gateway.do".equals(gateway))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public String oauth2Redirect(String scope, String redirectUri, String state) throws UnsupportedEncodingException
	{
		redirectUri = URLEncoder.encode(redirectUri, CHARSET);
		
		String oauth2Uri;
		if (isSandBox())
		{
			oauth2Uri = String.format("https://openauth.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=%s&redirect_uri=%s"
				, appId, scope, redirectUri);
		}
		else
		{
			oauth2Uri = String.format("https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=%s&redirect_uri=%s"
				, appId, scope, redirectUri);
		}
		
		if (state != null)
		{
			state = URLEncoder.encode(state, CHARSET);
			state = URLEncoder.encode(state, CHARSET); // 支付宝在回调时会自动对state进行转义，所以这里进行第二次转义，防止state参数获取错误
			
			oauth2Uri += ("&state=" + state);
		}
		
		return oauth2Uri;
	}
	
	public String getUserId(String authCode) throws AlipayApiException
	{
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
		request.setCode(authCode);
		request.setGrantType("authorization_code");
		
	    AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(request);
	    if (oauthTokenResponse.isSuccess())
	    {
	    	return oauthTokenResponse.getUserId();
	    }
	    else
	    {
	    	logger.warn("AlipayOperator getUserId failed:" + oauthTokenResponse.getMsg() + oauthTokenResponse.getSubMsg());
	    	return null;
	    }
	}
	
	public UserInfo getUserInfo(String authCode) throws AlipayApiException
	{
		AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
		AlipayUserInfoShareResponse userinfoShareResponse;
		
		userinfoShareResponse = alipayClient.execute(request, getAccessToken(authCode));
		if (userinfoShareResponse.isSuccess())
		{
			UserInfo userInfo = new UserInfo();
			userInfo.userId = userinfoShareResponse.getUserId();
			userInfo.nickname = userinfoShareResponse.getNickName();
			userInfo.avatar = userinfoShareResponse.getAvatar();
			return userInfo;
		} 
		else 
		{
			logger.warn("AlipayOperator getUserInfo failed:"+userinfoShareResponse.getMsg()+userinfoShareResponse.getSubMsg());
	    	return null;
		}
	}
	
	public String order(String tradeNumber, int totalAmount, String subject, String returnUrl, String notifyUrl, String passbackParams) throws AlipayApiException
	{
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
		alipayRequest.setReturnUrl(returnUrl); //设置回跳地址
		alipayRequest.setNotifyUrl(notifyUrl); //设置通知地址
		
		try {
			passbackParams = URLEncoder.encode(passbackParams, CHARSET);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error("Catch Exception:", e);
			passbackParams = "";
		}
		
		String param = String.format("{" +
		    "\"out_trade_no\":\"%s\"," +
		    "\"total_amount\":%.2f," +
		    "\"subject\":\"%s\"," +
		    "\"seller_id\":\"%s\"," +
		    "\"product_code\":\"QUICK_WAP_PAY\"," +
		    "\"passback_params\":\"%s\"" +
		    "}", tradeNumber, (float)totalAmount/100, subject, pid, passbackParams);
		alipayRequest.setBizContent(param); //填充业务参数
		
		return alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成支付页面
	}
	
	public boolean refund(String tradeNumber, String refundNumber, int refundAmount, String refundReason) throws AlipayApiException {
		AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
		String param = String.format("{" +
			    "\"out_trade_no\":\"%s\"," +
			    "\"out_request_no\":\"%s\"," +
			    "\"refund_amount\":%.2f," + 
			    "\"refund_reason\":\"%s\"" +
			    "}", tradeNumber, refundNumber, (float)refundAmount/100, refundReason);
		request.setBizContent(param);
		
		AlipayTradeRefundResponse response = alipayClient.execute(request);
		
		return response.isSuccess();
	}
	
	public boolean checkSignature(Map<String, String> params) throws AlipayApiException 
	{
		return AlipaySignature.rsaCheckV1(params, publicKey, CHARSET);
	}
	
	public static class UserInfo {
		
		public String getUserId() {
			return userId;
		}
		
		public void setUserId(String userId) {
			this.userId = userId;
		}
		
		public String getNickname() {
			return nickname;
		}
		
		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
		
		public String getAvatar() {
			return avatar;
		}

		public void setAvatar(String avatar) {
			this.avatar = avatar;
		}

		private String userId;
		
		private String nickname;
		
		private String avatar;
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final static String CHARSET = "UTF-8";
	
	@Value("${alipay.gateway}")
	private String gateway;
	
	@Value("${alipay.appId}")
	private String appId;
	
	@Value("${alipay.privateKey}")
	private String privateKey;
	
	@Value("${alipay.publicKey}")
	private String publicKey;
	
	@Value("${alipay.pid}")
	private String pid;
	
	private AlipayClient alipayClient;
	
	private String getAccessToken(String authCode) throws AlipayApiException
	{
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
		request.setCode(authCode);
		request.setGrantType("authorization_code");
		
	    AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(request);
	    if (oauthTokenResponse.isSuccess())
	    {
	    	return oauthTokenResponse.getAccessToken();
	    }
	    else
	    {
	    	logger.warn("AlipayOperator getAccessToken failed:" + oauthTokenResponse.getMsg() + oauthTokenResponse.getSubMsg());
	    	return null;
	    }
	}
}
