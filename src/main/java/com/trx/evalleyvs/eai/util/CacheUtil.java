package com.trx.evalleyvs.eai.util;


public class CacheUtil {
	
	private static CacheUtil obj;
	
	private CacheUtil() {}

	private static void init(){
		obj = new CacheUtil();
	}    
    
	/**
	 * AjaxUtil 객체 반환
	 * @return
	 */
	public static CacheUtil getInstance() {
		if (obj == null) {
			init();
		}
		return obj;
	}
	
	/**
	 * Active smi 여부반환
	 * @param smi
	 * @return
	 */
	public boolean isActiveSmi(String smi) {
		return true;
	}

	/**
	 * Active version 여부반환
	 * @param version
	 * @return
	 */
	public boolean isActiveVersion(String version) {
		return true;
	}
}
