package com.madongfang.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.madongfang.util.WechatUtil;
import com.madongfang.util.WechatUtil.JsSdkConfig;

@RestController
@RequestMapping(value="/api/wechat")
public class WechatController {

	@GetMapping(value="/jsSdkConfig")
	public JsSdkConfig getJsSdkConfig(HttpServletRequest request)
	{
		String basePath = request.getScheme()+"://"+request.getServerName()+request.getContextPath();
		String url = basePath + "/index.html";
		
		logger.debug("url={}", url);
		
		return wechatUtil.getJsSdkConfig(url);
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private WechatUtil wechatUtil;
}
