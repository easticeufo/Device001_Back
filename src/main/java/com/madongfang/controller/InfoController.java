package com.madongfang.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.madongfang.api.ManufacturerInfoApi;

@RestController
@RequestMapping(value="/api/info")
public class InfoController {

	@GetMapping(value="/manufacturer")
	public ManufacturerInfoApi getManufacturerInfo(HttpServletRequest request)
	{
		String basePath = request.getScheme() + "://" + request.getServerName() + 
				":" + request.getServerPort() + request.getContextPath();
		
		ManufacturerInfoApi manufacturerInfoApi = new ManufacturerInfoApi();
		manufacturerInfoApi.setWebTitle(webTitle);
		manufacturerInfoApi.setWechatQrcodeAddress(basePath + "/api/images/" + qrcodeFile);
		return manufacturerInfoApi;
	}
	
	@Value("${manufacturer.webTitle}")
	private String webTitle;
	
	@Value("${manufacturer.qrcodeFile}")
	private String qrcodeFile;
}
