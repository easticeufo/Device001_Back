package com.madongfang.controller;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.madongfang.api.LoginApi;
import com.madongfang.api.ReturnApi;
import com.madongfang.entity.Custom;
import com.madongfang.entity.Manager;
import com.madongfang.exception.HttpUnauthorizedException;
import com.madongfang.service.CustomService;
import com.madongfang.service.ManagerService;
import com.madongfang.util.AlipayUtil;
import com.madongfang.util.WechatUtil;

@Controller
@RequestMapping(value="/api/login")
public class LoginController {

	@GetMapping
	public String oauthLogin(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(name="code", required=false)String code, 
			@RequestParam(name="auth_code", required=false)String authCode,
			@RequestParam(name="state", required=false)String state,
			@RequestParam(name="device", required=false)String deviceCode, 
			@RequestParam(name="card", required=false)String cardId, 
			@RequestParam(name="menu", required=false)String menu,
			Model model) 
	{
		String basePath = request.getScheme()+"://"+request.getServerName()+request.getContextPath();
		logger.debug("basePath={}", basePath);
		
		if (cardId != null && cardId.length() == 10) // 将10位十进制内码(正码)转化为8位16进制的卡号
		{
			cardId = String.format("%08X", Long.valueOf(cardId));
		}
		
		if (code != null || authCode != null) // 微信或支付宝OAuth登录后重定向处理
		{
			try {
				cardId = null;
				deviceCode = null;
				menu = null;
				if (state.startsWith("card"))
				{
					cardId = state.substring("card".length());
				}
				else if (state.startsWith("device"))
				{
					deviceCode = state.substring("device".length());
				}
				else if (state.startsWith("menu"))
				{
					menu = state.substring("menu".length());
				}
				
				Custom custom = null;
				Manager manager = null;
				if (code != null) // 微信扫一扫
				{
					WechatUtil.UserInfo userInfo = wechatUtil.getUserInfo(code);
					if (userInfo == null)
					{
						logger.error("微信登录获取userInfo失败");
						model.addAttribute("errorInfo", "微信登录获取userInfo失败");
						return "error";
					}
					if (cardId != null) // 微信扫卡上的二维码登陆
					{
						custom = customService.cardLogin(cardId, userInfo.getOpenid());
						manager = managerService.getManager(userInfo.getOpenid());
					}
					else
					{
						custom = customService.wechatLogin(userInfo);
					}
				}
				else // 支付宝扫一扫
				{
					AlipayUtil.UserInfo userInfo = alipayUtil.getUserInfo(authCode);
					if (cardId != null) // 支付宝扫卡上的二维码登陆
					{
						custom = customService.cardLogin(cardId, userInfo.getUserId());
					}
					else
					{
						custom = customService.alipayLogin(userInfo);
					}
				}
				
				if (custom == null)
				{
					if (manager != null && manager.getLevel() == 4)
					{
						response.sendRedirect(basePath+"_Manager/api/login?card="+cardId);
						return null;
					}
					else
					{
						model.addAttribute("errorInfo", "无效的卡号");
						return "error";
					}
				}
				request.getSession().setAttribute("custom", custom);
				String redirect = basePath + "/index.html#?";
				if (deviceCode != null) // 扫描设备上的二维码跳转
				{
					redirect += String.format("/devices/%s/plugs", deviceCode);
				}
				else if (cardId != null) // 扫描卡上的二维码跳转
				{
					redirect += "/mine";
				}
				else if (menu != null) // 扫描卡上的二维码跳转
				{
					redirect += ("/" + menu); // 点击菜单选项跳转
				}
				else // 程序调试跳转
				{
					redirect += "/devices";
				}
				logger.debug("redirect address:" + redirect);
				response.sendRedirect(redirect);
				return null; // 这里不能使用spring自带的"redirect:"进行重定向，spring的重定向功能会在url中加入session导致angularjs页面访问出错
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("catch Exception:", e);
				model.addAttribute("errorInfo", e.getMessage());
				return "error";
			}
		}
		else // 用户扫码时的url不带code或authCode参数，重定向到OAuth登录连接
		{
			try {
				if (deviceCode != null) // 扫设备上的二维码登陆
				{
					state = "device" + deviceCode;
				}
				else if (cardId != null) // 扫充电卡上的二维码登陆
				{
					state = "card" + cardId;
				}
				else if (menu != null) // 点击菜单选项登陆
				{
					state = "menu" + menu;
				}
				else // 其他二维码形式
				{
					state = "other";
				}
				
				String site = "redirect:";
				String userAgent = request.getHeader("User-Agent");
				if (wechatUtil.isWechatBrowser(userAgent))
				{
					site += wechatUtil.oauth2Redirect(WechatUtil.SCOPE_USERINFO, basePath + "/api/login", state);
				}
				else if (alipayUtil.isAlipayBrowser(userAgent))
				{
					if (alipayUtil.isSandBox())
					{
						model.addAttribute("errorInfo", "本系统暂时不支持支付宝，敬请期待！");
						site = "error";
					}
					else 
					{
						site += alipayUtil.oauth2Redirect(AlipayUtil.SCOPE_USER, basePath + "/api/login", state);
					}
				}
				else 
				{
					model.addAttribute("errorInfo", "请使用微信或支付宝扫一扫登陆!");
					site = "error";
				}

				return  site;
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				logger.error("catch Exception:", e);
				model.addAttribute("errorInfo", e.getMessage());
				return "error";
			}
		}
	}
	
	@PostMapping
	public @ResponseBody ReturnApi codeLogin(HttpSession session, @RequestBody LoginApi loginApi) 
	{
		ReturnApi returnApi = new ReturnApi(0, "OK");
		
		Custom custom = customService.getCustom(loginApi.getUsername());
		if (custom == null)
		{
			returnApi.setReturnCode(-1);
			returnApi.setReturnMsg("不存在的用户");
		}
		else if (custom.getLoginCode() == null || !custom.getLoginCode().equalsIgnoreCase(loginApi.getLoginCode()))
		{
			returnApi.setReturnCode(-2);
			returnApi.setReturnMsg("登陆码错误");
		}
		else if (custom.getGenerateTime() == null || (new Date().getTime() - custom.getGenerateTime().getTime()) > 60 * 1000)
		{
			returnApi.setReturnCode(-3);
			returnApi.setReturnMsg("登陆码超期");
		}
		else // 登陆成功
		{
			session.setAttribute("custom", custom);
		}
		
		if (returnApi.getReturnCode() < 0)
		{
			throw new HttpUnauthorizedException(returnApi);
		}
		else
		{
			return returnApi;
		}
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private WechatUtil wechatUtil;
	
	@Autowired
	private AlipayUtil alipayUtil;
	
	@Autowired
	private CustomService customService;
	
	@Autowired
	private ManagerService managerService;
}
