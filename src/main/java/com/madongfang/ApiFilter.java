package com.madongfang;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madongfang.api.ReturnApi;

/**
 * Servlet Filter implementation class ApiFilter
 */
@Component
public class ApiFilter implements Filter {

    /**
     * Default constructor. 
     */
    public ApiFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		// place your code here
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		
		httpResponse.setContentType("application/json");
		httpResponse.setCharacterEncoding("UTF-8");
		
		String uri = httpRequest.getRequestURI();
		String query = httpRequest.getQueryString();
		if (query != null)
		{
			logger.debug("ApiFilter: method=" + httpRequest.getMethod() + ", url=" + uri + "?" + query);
		}
		else
		{
			logger.debug("ApiFilter: method=" + httpRequest.getMethod() + ", url=" + uri);
		}
		
		/* 验证用户登陆 */
		boolean authenticated = false; // 是否已登录认证
		String auth = httpRequest.getHeader("Authorization");
		logger.debug("http auth={}", auth);
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String authString = username + ":" + password + sdf.format(new Date());
    	String authStringEnc = new String(Base64.getEncoder().encode(authString.getBytes()));
		HttpSession session = httpRequest.getSession(false);
		String apiString = uri.substring(httpRequest.getContextPath().length());
		if ("OPTIONS".equals(httpRequest.getMethod()) 
				|| apiString.equals("/api/login")
				|| apiString.equals("/api/info/manufacturer")
				|| apiString.equals("/api/wechat/jsSdkConfig")
				|| apiString.startsWith("/api/payment/wechat/notify")
				|| apiString.startsWith("/api/payment/alipay/notify")
				|| apiString.startsWith("/api/notify")) // 不需要登陆验证的命令
		{
			authenticated = true;
		}
		else // 普通用户登录
		{
			if (session != null && session.getAttribute("custom") != null)
			{
				authenticated = true;
			}
			
			if (auth != null && auth.equals("Basic " + authStringEnc))
			{
				authenticated = true;
			}
		}
		
		if (!authenticated)
		{
			ReturnApi returnApi = new ReturnApi(-1, "未登陆，请先登陆！");
			httpResponse.setStatus(401);
			response.getWriter().write(new ObjectMapper().writeValueAsString(returnApi));
			return;
		}
		
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${api.base64.username}")
	private String username;
	
	@Value("${api.base64.password}")
	private String password;
}
