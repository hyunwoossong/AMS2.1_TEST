package com.trx.evalleyvs.eai.cargo.util;

/** 
 *  Description : 
 *  Modification Information 
 * 
 *     수정일         수정자                   수정내용 
 *   -------    --------    --------------------------- 
 *   2008. 1. 4   soogie         최초 생성 
  * 
 *  @author soogie
 *  @since 2008. 1. 4
 *  @version 1.0 
 *  @see 
 * 
 *  Copyright (C) 2007 by TRAXON All right reserved. 
 */
public class PatternUtil {

	/**
	 * 정규식 만들기
	 * 사용예 ) String regex = ValidationUtil.makeRegExAndOr("\\p{Upper})","/\\p{Upper}{3}");
	 * 			String regex = ValidationUtil.makeRegExAndOr("\\p{Digit}{2}(\\p{Upper}{3})?", regex);
	 * 			결과 => (/\\p{Upper}|//\\p{Upper}{3}|/\\p{Upper}/\\p{Upper}{3})
	 * 					(/\\p{Digit}{2}(\\p{Upper}{3})?|/(/\\p{Upper}|//\\p{Upper}{3}|/\\p{Upper}/\\p{Upper}{3})|/\\p{Digit}{2}(\\p{Upper}{3})?(/\\p{Upper}|//\\p{Upper}{3}|/\\p{Upper}/\\p{Upper}{3}))
	 * 					(/A|/B|/AB) 형태
	 * @param patternA
	 * @param patternB
	 * @return
	 */
	public static String makeRegExAndOrWithSlant(String patternA, String patternB) {
		// 순서중요
		// /AB가 제일 먼저 와야 함
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append("/").append(patternA).append(patternB);
		sb.append("|");
		sb.append("/").append(patternA);
		sb.append("|");
		sb.append("/").append(patternB);
		sb.append(")");
		
		return sb.toString();
	}
	

}
