package com.trx.evalleyvs.eai.util;

import java.util.HashMap;
import java.util.List;

/** 
 *  Description : 
 *  Modification Information 
 * 
 *     수정일         수정자                   수정내용 
 *   -------    --------    --------------------------- 
 *   2007. 12. 11   soogie         최초 생성 
 * 
 *  @author soogie
 *  @since 2007. 12. 11
 *  @version 1.0 
 *  @see 
 * 
 *  Copyright (C) 2007 by TRAXON All right reserved. 
 */
public class CheckIf {
	/**
	 * <pre>
	 *  해당 String 이  NULL 이면 공백을 리턴한다.  
	 * </pre>
	 *
	 * @param targetObject 파라메터
	 *
	 * @return 공백 스트링 또는 원본 스트링 
	 */
	public static String isNullToBlank(String targetObject) {
		return (String)isNullToDefault( targetObject, "") ;
	}
	
	/**
	 * <pre>
	 *  해당 Object가  NULL 이면 defaultVal 로 지정한 Object 를  리턴한다.  
	 * </pre>
	 *
	 * @param targetObject 파라메터
	 *
	 * @return defaultVal 또는 원본 Object 
	 */   
	public static Object isNullToDefault(Object targetObject, Object defaultVal) {
		return isNull(targetObject) ?  defaultVal :  targetObject ;    
	}
	
	/**
	 * <pre>
	 *  해당 수치가 0 인지 판별한다. 
	 * </pre>
	 *
	 * @param srcVal 파라메터
	 *
	 * @return 0 여부
	 */
	public static boolean isZero(double srcVal) {
		return ( srcVal == 0.0D ) ? true : false ;
	} 
	/**
	 * <pre>
	 *  해당 수치가 0 인지 판별한다. 
	 * </pre>
	 *
	 * @param srcVal 파라메터
	 *
	 * @return 0 여부
	 */
	public static boolean isZero(String srcVal) {
		if (!isNull(srcVal) && Double.parseDouble(srcVal) == 0.0D ) { 
			return true ; 
		} else {
			return false; 
		}
		
	}
	
	
	/**
	 * <pre>
	 *   Object 가 NULL 인지 체크한다. 
	 * </pre>
	 *
	 * @param targetObject 파라메터
	 *
	 * @return NULL여부
	 */
	public static boolean isNull(Object targetObject) {
		if ( targetObject == null ) {
			return true;
		} else if (targetObject instanceof Number ) {
			return false; 
		} else {
			return false;
		}
	}
	
	/**
	 * <pre>
	 *  Object[] 가 NULL 인지 체크한다. 
	 * </pre>
	 *
	 * @param targetArray 파라메터
	 *
	 * @return NULL여부
	 */
	public static boolean isNull(Object[] targetArray) {
		if ((targetArray == null) || (targetArray.length <= 0)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * <pre>
	 *  String 이 NULL 인지 체크한다. 
	 * </pre>
	 *
	 * @param targetObject 파라메터
	 *
	 * @return NULL여부
	 */
	public static boolean isNull(String targetObject) {
		if ((targetObject == null) || targetObject.equals("")) {
			return true ;
		} else {
			return false;
		}   
	}
	/**
	 * <pre>
	 *  List가  NULL 인지 체크한다. 
	 * </pre>
	 *
	 * @param targetList 파라메터
	 *
	 * @return NULL여부
	 */
	public static boolean isNull(List targetList) {
		if ((targetList == null) || (targetList.size() <= 0)) {
			return true;
		}  else return false ; 
	}
	
	/**
	 * <pre>
	 *  HashMap 이 NULL 인지 체크한다. 
	 * </pre>
	 *
	 * @param targetMap 파라메터
	 *
	 * @return NULL여부 
	 */
	public static boolean isNull(HashMap targetMap) {
		if ((targetMap == null) || (targetMap.size() <= 0)) {
			return true;
		}  else return false ; 
	}
	
}
