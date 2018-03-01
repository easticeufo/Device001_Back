package com.madongfang;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.madongfang.service.WechatDataService;
import com.madongfang.util.WechatUtil;
import com.madongfang.util.WechatUtil.AccessTokenThread;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

/**
 * Application Lifecycle Listener implementation class StartStopListener
 *
 */
@Component
public class StartStopListener implements ServletContextListener {
	
    /**
     * Default constructor. 
     */
    public StartStopListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce)  { 
         // TODO Auto-generated method stub
    	/* mysql相关资源释放，防止tomcat卸载应用时的内存泄漏 */
    	Enumeration<Driver> drivers = DriverManager.getDrivers();
        Driver d = null;
        while (drivers.hasMoreElements()) 
        {
            try {
                d = drivers.nextElement();
                DriverManager.deregisterDriver(d);
                System.out.println(String.format("ContextFinalizer:Driver %s deregistered", d));
            } catch (SQLException ex) {
                System.out.println(String.format("ContextFinalizer:Error deregistering driver %s", d) + ":" + ex);
            }
        }
        AbandonedConnectionCleanupThread.checkedShutdown();
        
        /* 微信获取access_token线程停止 */
        accessTokenThread.stopThread();
        
        System.out.println("web stopped");
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce)  { 
         // TODO Auto-generated method stub
    	
    	/* 启动获取微信“对话服务->基础支持”中的access_token的线程 */
    	accessTokenThread = wechatUtil.getAccessTokenThread();
    	accessTokenThread.setAccessTokenCallback(new WechatUtil.AccessTokenCallback() {
			
			@Override
			public void refreshAccessToken(String accessToken, String jsapiTicket, Integer expiresIn) {
				// TODO Auto-generated method stub
				wechatDataService.saveAccessToken(accessToken, jsapiTicket, expiresIn);
			}
		});
    	accessTokenThread.start();
    	
    	logger.info("web started");
    }
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private AccessTokenThread accessTokenThread;
    
    @Autowired
    private WechatUtil wechatUtil;
    
    @Autowired
    private WechatDataService wechatDataService;
}
