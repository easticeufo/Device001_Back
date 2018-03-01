package com.madongfang.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CommonUtil {

	/**
	 * 随机字符生成
	 * 
	 * @param length
	 * @return
	 */
	public String getRandomStringByLength(int length) {  
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";  
        Random random = new Random();  
        StringBuffer sb = new StringBuffer();  
        for (int i = 0; i < length; i++) {  
            int number = random.nextInt(base.length());  
            sb.append(base.charAt(number));  
        }  
        return sb.toString();
    }
	
	/**
	 * 将字符串进行MD5加密
	 * 
	 * @param str
	 * @return 32位大写形式的字符串
	 */
	public String md5(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			return toHex(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			logger.error("catch Exception:", e);
			return null;
		}
	}
	
	/**
	 * 将字符串进行sha1签名
	 * 
	 * @param str
	 * @return 
	 */
	public String sha1(String str) {
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(str.getBytes("UTF-8"));
			return toHex(crypt.digest());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
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
	public String getSign(Object obj, String key) {
		TreeMap<String, String> paramMap = new TreeMap<String, String>();
		
		List<Field> fieldList = getFieldList(obj.getClass());
		
		try {
			for (Field field : fieldList) {
				field.setAccessible(true);
				if (field.get(obj) == null)
				{
					continue;
				}
				
				if (field.get(obj) instanceof Collection)
				{
					paramMap.put(field.getName(), new ObjectMapper().writeValueAsString(field.get(obj)));
				}
				else 
				{
					paramMap.put(field.getName(), field.get(obj).toString());
				}
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
	public String getSign(TreeMap<String, String> paramMap, String key){
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> param : paramMap.entrySet()) {
			sb.append(param.getKey());
			sb.append("=");
			sb.append(param.getValue());
			sb.append("&");
		}
		sb.append("key=");
		sb.append(key);
		logger.debug("sign string:" + sb.toString());
		
		return md5(sb.toString());
	}
	
	public NearCoordinate getNearCoordinate(double longitude, double latitude, int distance) {
		double dis = distance / 1000d;
		double dlng = 2 * Math.asin(Math.sin(dis/(2*EARTH_RADIUS)) / Math.cos(latitude*Math.PI/180));
		dlng = dlng * 180 / Math.PI;
		double dlat = dis / EARTH_RADIUS;
		dlat = dlat * 180 / Math.PI;
		
		return new NearCoordinate(latitude-dlat, latitude+dlat, longitude-dlng, longitude+dlng);
	}
	
	public int getDistance(double lng1, double lat1, double lng2, double lat2)
	{
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b/2), 2)));
		return (int) Math.round(s * EARTH_RADIUS * 1000d);
	}
	
	public static class NearCoordinate
	{
		public NearCoordinate() {
			super();
		}

		public NearCoordinate(double minlat, double maxlat, double minlng, double maxlng) {
			super();
			this.minlat = minlat;
			this.maxlat = maxlat;
			this.minlng = minlng;
			this.maxlng = maxlng;
		}

		public double getMinlat() {
			return minlat;
		}

		public void setMinlat(double minlat) {
			this.minlat = minlat;
		}

		public double getMaxlat() {
			return maxlat;
		}

		public void setMaxlat(double maxlat) {
			this.maxlat = maxlat;
		}

		public double getMinlng() {
			return minlng;
		}

		public void setMinlng(double minlng) {
			this.minlng = minlng;
		}

		public double getMaxlng() {
			return maxlng;
		}

		public void setMaxlng(double maxlng) {
			this.maxlng = maxlng;
		}

		@Override
		public String toString() {
			return "NearCoordinate [minlat=" + minlat + ", maxlat=" + maxlat + ", minlng=" + minlng + ", maxlng="
					+ maxlng + "]";
		}

		private double minlat;
		
		private double maxlat;
		
		private double minlng;
		
		private double maxlng;
	}
	
	private static final double EARTH_RADIUS = 6371.393; // 地球平均半径6371.393千米
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 将16位byte[] 转换为32位String
	 * 
	 * @param buffer
	 * @return
	 */
	private String toHex(byte buffer[]) {
		StringBuffer sb = new StringBuffer(buffer.length * 2);
		for (int i = 0; i < buffer.length; i++) {
			sb.append(Character.forDigit((buffer[i] & 0xF0) >> 4, 16));
			sb.append(Character.forDigit(buffer[i] & 0x0F, 16));
		}

		return sb.toString();
	}
	
	/**
	 * 递归获取当前类和其所有父类(除了"java.lang.Object")的成员变量列表
	 * 
	 * @param type
	 * @return
	 */
	private List<Field> getFieldList(Class<? extends Object> type) {
		
		Class<? extends Object> superClassType = type.getSuperclass();
		if (superClassType == null) // type == java.lang.Object
		{
			return new LinkedList<Field>();
		}
		
		List<Field> fieldList = getFieldList(superClassType);
		
		Field[] fields = type.getDeclaredFields();
		for (Field field: fields)
		{
			fieldList.add(field);
		}
		
		return fieldList;
	}
	
	private double rad(double d)
	{
		return d * Math.PI / 180;
	}
}
