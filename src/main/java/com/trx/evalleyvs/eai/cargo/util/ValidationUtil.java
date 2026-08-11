package com.trx.evalleyvs.eai.cargo.util;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.trx.evalleyvs.eai.log.LoggerFactory;
import com.trx.evalleyvs.eai.message.MessageXMLResourceManager;
import com.trx.evalleyvs.eai.util.CommonUtil;

/** 
 *  Description : Validation을 위한 유틸클래스
 *  Modification Information 
 * 
 *     수정일         수정자                   수정내용 
 *   -------    --------    --------------------------- 
 *   2007. 12. 26   soogie         최초 생성 
  * 
 *  @author soogie
 *  @since 2007. 12. 26
 *  @version 1.0 
 *  @see 
 * 
 *  Copyright (C) 2007 by TRAXON All right reserved. 
 */
public class ValidationUtil {
	
	/**
	 * Double 의 regular expressions
	 */
	public static String pDouble = "[0-9\\.]";
	public static String pText = "[A-Z0-9\\s\\.]";

	public static String pWeightCode = "[0-9\\.]{1,7}";
	public static String pVolumeAmount = "[0-9\\.]{1,9}";

	private static Logger logger = LoggerFactory.getLogger("MSB");
	private static MessageXMLResourceManager mxr = MessageXMLResourceManager.getInstance("MessageResource.xml");
	
	/**
	 * source가 pattern에 적합한지 체크
	 * @param source
	 * @param pattern
	 * @return
	 */
	public static boolean validPattern(String source, String pattern) 
	{
		boolean isValid = false;
		
		// 여기중요 ::: nvl 사용하면 
		// '\r\n'이 trim 되어 pattern missmatch 발생
		source = (source == null) ? "" : source;
		
		pattern = CommonUtil.nvl(pattern);
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(source);
		
		while(m.find()){
			if (m.group().equals(source)) {
				isValid = true;
			}
		}
		if(isValid==false){
			System.out.println("source=["+source+"]");
			System.out.println("pattern=["+pattern+"]");
		}
		return isValid;
	}

	/**
	 * 패턴에 맞는 첫번째 String 만 반환
	 * @param source
	 * @param pattern
	 * @return
	 */
	public static String extractPatternData(String source, String pattern) 
	{
		String result = "";
		
		// 여기중요 ::: nvl 사용하면 
		// '\r\n'이 trim 되어 pattern missmatch 발생
		source = (source == null) ? "" : source;
		pattern = CommonUtil.nvl(pattern);
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(source);
		
		//CommonUtil.writelog("validPattern.source=["+source+"]");
		if (m.find()) {
			result = m.group();
		}
		return result;
	}

	/**
	 * 패턴에 맞는 모든 String 반환
	 * @param source
	 * @param pattern
	 * @return
	 */
	public static String[] extractPatternDataList(String source, String pattern) 
	{
		Vector vt = new Vector();
		
		// 여기중요 ::: nvl 사용하면 
		// '\r\n'이 trim 되어 pattern missmatch 발생
		source = (source == null) ? "" : source;
		pattern = CommonUtil.nvl(pattern);
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(source);
		
		//CommonUtil.writelog("validPattern.source=["+source+"]");
		while (m.find()) {
			vt.add(m.group());
		}
		
		return (String[])vt.toArray(new String[vt.size()]);
	}

}
