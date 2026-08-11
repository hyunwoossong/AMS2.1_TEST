package com.trx.evalleyvs.eai.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SimpleTimeZone;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.trx.evalleyvs.eai.log.LoggerFactory;
import com.trx.evalleyvs.eai.util.CommonUtil;


/**
 *  Description :
 *  Modification Information
 *
 *     수정일         수정자                   수정내용
 *   -------    --------    ---------------------------
 *   2007. 12. 10   soogie         최초 생성
 *
 *  @author soogie
 *  @since 2007. 12. 10
 *  @version 1.0
 *  @see
 *
 *  Copyright (C) 2007 by TRAXON All right reserved.
 */
public class CommonUtil {
	private static Logger logger = LoggerFactory.getLogger("MSB_ADMIN");


	private static String filePath = "D:\\data\\logs\\";

	/**
	 * MSB  TransactionID 관련
	 */
	public static String getTransactionID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 문자열을 헥스 스트링으로 변환
	 * @param s
	 * @return
	 */
	public static String stringToHex(String s) {
		String result = "";
		for (int i = 0; i < s.length(); i++) {
		result += String.format("%02X ", (int) s.charAt(i));
		}
		return result;
	}

	/**
	 * 문자열 관련
	 * 테이블내에 들어갈 글자가 긴 경우 maxNum 만큼만 남기고 "..."으로 처리
	 * @param   title   문자열소스
	 *		  maxNum  문자열 길이 허용 한계
	 *		  re_level	indentation이 필요한 경우 depth
	 */
	public static String getTitleLimit(String title,int maxNum,int re_level) {
		int blankLen = 0;
		if(re_level != 0){
			blankLen = (re_level +1)*2;
		}
		int tLen =title.length();
		int count = 0;
		char c;
		int s=0;
		for(s=0;s<tLen;s++){
			c = title.charAt(s);
			if((int)(count) > (int)(maxNum-blankLen)) break;
			if(c>127) count +=2;
			else count ++;
		}
		return (tLen >s)? title.substring(0,s)+"..." : title;
	}

	/**
	 * 문자열 관련
	 * 테이블내에 들어갈 글자가 긴 경우 maxNum 만큼만 남기고 "..."으로 처리
	 * @param title
	 * @param maxNum
	 * @return
	 */
	public static String getTitleLimit(String title,int maxNum) {
		int tLen =title.length();
		int count = 0;
		char c;
		int s=0;
		for(s=0;s<tLen;s++){
			c = title.charAt(s);
			if(count > maxNum) break;
			if(c>127) count +=2;
			else count ++;
		}
		return (tLen >s)? title.substring(0,s)+"..." : title;
	}

	/**
	 * 문자열 관련
	 * Convert시 String add(-해당문자앞 또는 뒤에 add)
	 * strRAdd("abcd", "B", "R", 4) - " abcd"
	 * @param	String str	   문자열
	 *			String strAdd	추가문자열
	 *			String  align	정렬방식
	 *			int size		 길이
	 * @return	String	문자열
	 */
	public static String strAdd(String str, String  strAdd, String  align, int size) {
		if  (align.equals( "L")) return strLAdd(str , strAdd , size );
		else if  (align.equals( "R" )) return strRAdd(str, strAdd, size);
		else  return str ;
	}

	/**
	 * Convert시 String add(왼쪽정렬- 해당문자뒤에 add)
	 * strLAdd("abcd", "B", 4) - "abcd "
	 * @param	String str	   문자열
	 *			String strAdd	추가문자열
	 *			String  align	정렬방식
	 *			int size		 길이
	 * @return	String	문자열
	 */
	public static String strLAdd(String str, String strAdd, int size) {
		StringBuffer  strBuf = new StringBuffer() ;
		strBuf.append(str) ;
		for (int i = 0 ; i < size - str.length();  i++){
			if (strAdd.equals("B"))  strBuf.append(" ") ;
			else  strBuf.append(strAdd) ;
		}
		return strBuf.toString() ;
	}

	/**
	 * Convert시 String add(오른쪽정렬- 해당문자앞에 add)
	 * strRAdd("abcd", "B", 4) - " abcd"
	 * @param	String str	   문자열
	 *			String strAdd	추가문자열
	 *			int size		 길이
	 * @return	String	문자열
	 */
	public static String strRAdd(String str, String strAdd, int size) {
		StringBuffer  strBuf = new StringBuffer() ;
		for (int i = 0; i < size  - str.length();  i++){
			if (strAdd.equals("B") )  strBuf.append(" ");
			else strBuf.append(strAdd) ;
		}
		strBuf.append(str) ;
		return strBuf.toString() ;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param   str		 소스
	 *		  strDefault  소스가 널인경우 사용할 디폴트값
	 */
	public static String nvl(Object obj, String defaultVal) {
		String rtnVal = "";
		if (obj == null) {
			rtnVal = defaultVal;
		} else {
			rtnVal = obj.toString();
		}
		return rtnVal;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static int nvl(Object obj, Short defaultVal) {
		short rtnVal = 0;
		if (obj == null) {
			rtnVal = defaultVal.shortValue();
		} else {
			rtnVal = ((Short) obj).shortValue();
		}
		return rtnVal;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static int nvl(Object obj, Integer defaultVal) {
		int rtnVal = 0;
		if (obj == null || "".equals(obj)) {
			rtnVal = defaultVal.intValue();
		} else {
			rtnVal = new Integer(obj.toString()).intValue();
		}
		return rtnVal;
	}

	public static boolean nvl(Object obj, boolean defaultVal) {
		boolean rtnVal = true;
		if (obj == null || "".equals(obj)) {
			rtnVal = defaultVal;
		}
		else {
			rtnVal = new Boolean(obj.toString()).booleanValue();
		}
		return rtnVal;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static long nvl(Object obj, Long defaultVal) {
		long rtnVal = 0;
		if (obj == null) {
			rtnVal = defaultVal.longValue();
		} else {
			rtnVal = ((Long) obj).longValue();
		}
		return rtnVal;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static float nvl(Object obj, Float defaultVal) {
		float rtnVal = 0;
		if (obj == null) {
			rtnVal = defaultVal.floatValue();
		} else {
			rtnVal = ((Float) obj).floatValue();
		}
		return rtnVal;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static double nvl(Object obj, Double defaultVal) {
		double rtnVal = 0;
		if (obj == null) {
			rtnVal = defaultVal.doubleValue();
		} else {
			rtnVal = ((Double) obj).doubleValue();
		}
		return rtnVal;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static double nvl(Object obj, BigDecimal defaultVal) {
		double rtnVal = 0;
		if (obj == null) {
			rtnVal = defaultVal.doubleValue();
		} else {
			rtnVal = ((BigDecimal) obj).doubleValue();
		}
		return rtnVal;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static String nvl(Object obj, Object objDefault){
		if (obj == null) return objDefault.toString();
		if ((obj.toString()).trim().equals("")) return objDefault.toString();
		else return (obj.toString()).trim();
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static String nvl(Object obj){
		if (obj == null) return "";
		else return obj.toString();
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static String nvl(String str, String strDefault) {
		if (str == null) return strDefault;
		if (str.trim().equals("")) return strDefault;
		return str.trim();
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static String nvl(String str) {
		if (str == null) return "";
		return str.trim();
	}

	public static HashMap nvl(HashMap map) {
		if (map == null) return new HashMap();
		else return map;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static String[] nvl(String[] arrStr,String strDefault) {
		String[] arrBuf = {};
		if ( arrStr == null )
		{
			return arrBuf;
		}
		else
		{
			arrBuf = new String[arrStr.length];
			for (int i=0; i<arrStr.length; i++)
			{
				if (arrStr[i] == null || arrStr[i].equals("")) arrBuf[i] = strDefault;
				else	arrBuf[i] = arrStr[i].trim();
			}
		}
		return arrBuf;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static String[] nvl(String[] arrStr ) {
		String[] arrBuf = {};
		if ( arrStr == null )
		{
			return arrBuf;
		}
		else
		{
			arrBuf = new String[arrStr.length];
			for (int i=0; i<arrStr.length; i++)
			{
				if (arrStr[i] == null || arrStr[i].equals("")) arrBuf[i] = "";
				else	arrBuf[i] = arrStr[i].trim();
			}
		}
		return arrBuf;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static int nvl(int x, int nDefault) {
		if (x == 0) return nDefault;
		return x;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static int nvl(int x) {
		return x;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static double nvl(double x, double nDefault) {
		if (x == 0) return nDefault;
		return x;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static double nvl(double x) {
		return x;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static long nvl(long x, long nDefault) {
		if (x == 0) return nDefault;
		return x;
	}

	/**
	 * 문자열 관련
	 * sql의 nvl 함수와 같은 기능(숫자와 문자열 타입만 가능)
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static long nvl(long x)
	{
		return x;
	}


	/**
	 * 문자 포맷주기
	 * @param	Objecy obj (Object Type)
	 *			String keyType
	 * @return	String
	 */
	public static String getFormatData(Object obj, String keyType) {
		return getFormatData(nvl(obj), keyType);
	}
	/**
	 * 문자 포맷주기
	 * 예)우편번호 : getFormatData("123456", "Z")
	 * D : 동호정보(####-####)
	 * @param   String  s,String keyType
	 * @return  String
	 */
	public static String getFormatData(String  s, String keyType) {
		StringBuffer value = new StringBuffer();
		if (s == null) return "";
		s = s.trim();
		if (keyType.equals("D")) {
			if (s.length() != 8 || !isNumber(s)) {
				return s;
			}
			value.append(s.substring(0,4));
			value.append("-");
			value.append(s.substring(4,8));
		}
		return value.toString();
	}

	/**
	 * mask문자를 제거하고 숫자만 남긴다.
	 * @param   data	input String
	 * @return  String  output String
	 */
	public static  String exceptMask(String data) {
		String lsReturn = "", lsTemp = data.trim();
		char lcChar ;
		for ( int i = 0; i < lsTemp.length(); i ++ ){
			lcChar = lsTemp.charAt(i);
			if ( Character.isDigit(lcChar) )
				lsReturn += lcChar ;
		}
		return lsReturn;
	}

	/**
	 * 특정mask문자를 제거한다.
	 * @param   data	input String
	 * @return  String  output String
	 */
	public static  String exceptMask_Packet(String data)
	{
		String lsReturn = "", lsTemp = data.trim();
		for ( int i = 0; i < lsTemp.length(); i ++ ){
			if ( lsTemp.charAt(i) ==  '/' )		;
			else if ( lsTemp.charAt(i) ==  '-') ;
			else if ( lsTemp.charAt(i) ==  ':') ;
			else if ( lsTemp.charAt(i) ==  ',') ;
			else  lsReturn += lsTemp.charAt(i);
		 }
   		 return lsReturn;
	}

	/**
	 * 통화관련 Comma문자를 제거한다.
	 * @param   data	input String
	 * @return  String  output String
	 */
	public static  String exceptCurrency(String data){
		String lsReturn = "", lsTemp = data.trim();
		for ( int i = 0; i < lsTemp.length(); i ++){
			if ( lsTemp.charAt(i) ==  ','  )  ;
			else  lsReturn += lsTemp.charAt(i) ;
		}

		return lsReturn;
	}

	/**
	 * int를 통화로 변환
	 * @param   data	input int
	 * @return  String  output String
	 */
	public static  String intToCurrency(int data){

		String lsReturn ;
		if(data == 0){
			lsReturn = "";
		}else{
			lsReturn = java.text.NumberFormat.getInstance(Locale.KOREA).format(data);
		}

		return lsReturn;
	}

	/**
	 * String을 통화로 변환
	 * @param   data	input int
	 * @return  String  output String
	 */
	public static  String intToCurrency(String data){
		String lsReturn ;
		if(isNumber(data)){
			lsReturn = java.text.NumberFormat.getInstance(Locale.KOREA).format(Integer.parseInt(data));
		}else{
			lsReturn = "";
		}
		return lsReturn;
	}


	/**
	 * 숫자인지 검사
	 * @param	String sVal
	 * @return	boolean
	 */
	public static boolean isNumber(String sVal){
		if (sVal == null) {
			return false;
		}
		for (int i = 0 ; i < sVal.length(); i++){
			if(!Character.isDigit(sVal.charAt(i))){
				return false;
			}
		}
		return true;
	}

	/**
	 * 숫자인지 검사
	 * @param strbuf
	 * @return
	 */
	public static boolean isNumber(StringBuffer strbuf){
		if (strbuf == null) {
			return false;
		}

		for ( int i=0; i < strbuf.length() ; i++ ) {
			if(!Character.isDigit(strbuf.charAt(i))){
				return false;
			}
		}

		return true;
	}

	/**
	 * 날짜 관련
	 * 오늘과 같은 날짜이면 시간으로리턴, 다르면 날짜로 리턴
	 * @param   srcdate	 소스데이트
	 */
	public static String getDateFormat(java.util.Date srcdate){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
		java.util.Date today = new java.util.Date();
		String destdate  = "";
		String nowdate = "";
		nowdate = formatter.format(today);
		destdate  = formatter.format(srcdate);
		if (nowdate.equals(destdate)) {
			destdate = formatter2.format(srcdate);
		}
		return destdate;
	}

	/**
	 * 포맷으로 현재 날짜 및 시간 리턴
	 * usage : getCurrentDateByFormat("yyyy-MM-dd HH:mm:ss")
	 * @return
	 */
	public static String getCurrentDateByFormat(String format){
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		java.util.Date today = new java.util.Date();
		return formatter.format(today);
	}

	 /**
	  * 포맷으로 GMT 날짜 및 시간 리턴
	  * usage : getCurrentGMTDateByFormat("yyyy-MM-dd HH:mm:ss")
	  * date : 2009/3/18 적용했으나 미반영 2009/4/1 재적용
	  * @return
	  */
	 public static String getCurrentGMTDateByFormat(String format){
		 SimpleDateFormat formatter = new SimpleDateFormat(format);
	     java.util.Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
	     formatter.setCalendar(cal);
	     return formatter.format(cal.getTime());
	 }

	/**
	  * 입력된 날짜 포맷을 출력 포맷으로 변환하여 반환한다
	  *
	  * <pre>
	  * example :
	  *
	  *	  CommonUtil.writelog("오늘 날짜 : " + getDate("20061010101010"));
	  *
	  * result :
	  *
	  *	  오늘 날짜 : 2006/10/10 10:10:10
	  *
	  *
	  * </pre>
	  * @param inDate	입력 날짜스트링 20061010101010
	  * @return  String   변환된 날짜 스트링 2006/10/10 10:10:10
	  */
	public static String getDate(String inDate) throws Exception
	{
		return getDate(inDate, "yyyyMMddHHmmss", "yyyy/MM/dd HH:mm:ss");
	}

	/**
	  * 입력된 날짜 포맷을 출력 포맷으로 변환하여 반환한다
	  *
	  * <pre>
	  * example :
	  *
	  *	  CommonUtil.writelog("오늘 날짜 : " + getDate("20061010", "yyyyMMdd", "yyyy/MM/dd"));
	  *
	  * result :
	  *
	  *	  오늘 날짜 : 2006/10/10
	  *
	  *
	  * </pre>
	  * @param inDate	입력 날짜스트링 20061010
	  * @param inType  입력 날짜포맷 yyyyMMdd
	  * @param outType	출력 날짜포맷 yyyy/MM/dd
	  * @return  String   변환된 날짜 스트링 2006/10/10
	  */
	public static String getDate(String inDate, String inType, String outType) throws Exception
	{
		String outDate = "";

		try
		{
			if (inDate.length() != inType.length()) {
				return inDate;
			}
			outDate = new SimpleDateFormat(outType).format(new SimpleDateFormat(inType).parse(inDate));
		}
		catch (Exception ex)
		{
			outDate = inDate;
		}
		return outDate;
	}

	/**
	 * 날짜 관련
	 * 날짜비교
	 * @param   type	날짜포멧 ( 예 : "yyyy/MM/dd" )
	 */
	public static boolean isBeforeDate(String d1, String d2) throws Exception
	{
		java.util.Date date1 = null;
		java.util.Date date2 = null;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		date1 = formatter.parse(d1);
		date2 = formatter.parse(d2);

		return date1.before(date2);
	}

	/**
	 * 날짜 관련
	 * 날짜비교
	 * @param d1
	 * @param d2
	 * @return
	 * @throws Exception
	 */
	public static boolean isAfterDate(String d1, String d2) throws Exception
	{
		java.util.Date date1 = null;
		java.util.Date date2 = null;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		date1 = formatter.parse(d1);
		date2 = formatter.parse(d2);

		return date1.after(date2);
	}

	/**
	 * 날짜 관련
	 * 날짜 add
	 * @param d1
	 * @param field
	 * @param amount
	 * @return
	 * @throws Exception
	 */
	public static String addDate(String d1, int field, int amount) throws Exception
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(d1));
		cal.add(field, amount);

		return new SimpleDateFormat("yyyy/MM/dd").format(cal.getTime());
	}

	/**
	 * 날짜관련
	 * date1 - date2
	 * @param date1
	 * @param date2
	 * @return
	 * @throws Exception
	 */
	public static long dateDiff(String date1, String date2) throws Exception
	{
		java.util.Date d1 = null;
		java.util.Date d2 = null;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		d1 = formatter.parse(date1);
		d2 = formatter.parse(date2);

		long differ = ( d1.getTime() - d2.getTime() ) / ( 60 * 60 * 24 * 1000);
		if (differ < 0) differ *= -1;
		return differ;
	}

	/**
	  * 날짜 2개를 입력받아 시간차이를 구한다
	  *
	  * <pre>
	  * example :
	  *
	  *	  CommonUtil.writelog("시간차이 : " + getDateDiff("20061010101010.111", "20061010101013.222", "yyyyMMddHHmmss.SSS"));
	  *
	  * result :
	  *
	  *	  시간차이 : 00:00:03.111
	  *
	  *
	  * </pre>
	  * @param s1	입력 날짜스트링 20061010101010.111
	  * @param s2 입력 날짜스트링 20061010101013.222
	  * @param format	입력 날짜포맷 yyyyMMddHHmmss.SSS
	  * @return  String   변환된 날짜 스트링 00:00:03.111
	  */
	public static String getDateDiff(String s1, String s2,String format) throws Exception
	{

	 long   datedif;

	 try{

		   SimpleDateFormat fmt = new java.text.SimpleDateFormat(format);
		   if( s1.equals("") && s2.equals("")) {
			 return "";
		   }

		   if(s2.trim().equals("00")||s2.trim().equals("")) {
			 Calendar cal = Calendar.getInstance();
					   s2 = fmt.format(cal.getTime()).trim();
		   }

		   datedif = ( (fmt.parse(s1).getTime()-fmt.parse(s2).getTime()) );
		   datedif = (datedif>0) ? datedif:Math.abs(datedif);

		   double SSS   = datedif;
		   SSS   = ( SSS > 999 ) ? (SSS%1000) : SSS;

		   double ss   = Math.floor((datedif-SSS)/1000);;
		   ss   = ( ss>59 ) ? (ss%60) : ss;

		   double mm   = Math.floor((datedif-ss-SSS)/60/1000);
		   mm   = ( mm>59 ) ? (mm%60) : mm;

		   double hh   = Math.floor(( datedif-((mm*60)+ss+SSS) )/60/60/1000);
		   double day  = (hh>23) ? Math.floor(hh/24):0 ;
		   hh   = (hh>23) ? (hh%24) : hh;

		   String date = (day!=0) ? ""+((int)day)+"일 " : "";
		   String hour = ""+((int)hh);
		   String min  = ""+((int)mm);
		   String sec  = ""+((int)ss);
		   String secSSS  = ""+((int)SSS);

		   if(hh<10){ hour = "0"+hour; }
		   if(mm<10){  min = "0"+min;  }
		   if(ss<10){  sec = "0"+sec;  }
		   if(SSS<10){  secSSS = "00" + secSSS;  }
		   else if(SSS<100){  secSSS = "0" + secSSS;  }

		   return date+hour+":"+min+":"+sec + "."+secSSS;

	 }catch(Exception e){
	   return "??:??:??";
	   //1일10:10:10.333
	 }

   }

	/**
	 * 게시물 관련
	 * 파일의 크기를 소수점 두자리(.00)까지 표시
	 * @param   filesize		해당파일의 바이트크기
	 */
	public static String getFileSize(long filesize) {
		DecimalFormat df = new DecimalFormat(".##");
		String fSize="";
		if ((filesize > 1024) && (filesize < 1024 * 1024)) {
			fSize = df.format((float)filesize/1024).toString() + " KB" ;
		} else if (filesize >= 1024 * 1024) {
			fSize = df.format((float)filesize/(1024*1024)).toString() + " MB" ;
		} else {
			fSize = Long.toString(filesize) + " Bytes" ;
		}
		return fSize;
	}

	/**
	 * 넘버포멧 관련
	 * 숫자의 크기를 소수점 두자리(.000)까지 표시
	 * @param   숫자 또는 문자형
	 */
	public static String ff(int value) { return formatFloat((double)value); }
	public static String ff(long value) { return formatFloat((double)value); }
	public static String ff(float value) { return formatFloat((double)value); }
	public static String ff(String value) { return formatFloat(Double.parseDouble(value)); }
	public static String ff(double value) { return formatFloat(value); }

	public static String ff(int value, int scale, int precision) { return formatFloat((double)value, scale, precision); }
	public static String ff(long value, int scale, int precision) { return formatFloat((double)value, scale, precision); }
	public static String ff(float value, int scale, int precision) { return formatFloat((double)value, scale, precision); }
	public static String ff(String value, int scale, int precision) { return formatFloat(Double.parseDouble(value), scale, precision); }
	public static String ff(double value, int scale, int precision) { return formatFloat(value, scale, precision); }

	public static String formatFloat(int value) { return formatFloat((double)value); }
	public static String formatFloat(long value) { return formatFloat((double)value); }
	public static String formatFloat(float value) { return formatFloat((double)value); }
	public static String formatFloat(String value) { return formatFloat(Double.parseDouble(value)); }

	public static String formatFloat(double value) {
		DecimalFormat formatFloat =new DecimalFormat("###,###,###,###,##0");
		return formatFloat.format(value);
	}

	public static String formatFloat(double value, int precision, int scale) {
		int i=0;
		String fmt = "";
		if (precision > 0)
		{
			for(i=1; i < precision; i++)
			{
				if (i%3 == 0)
					fmt = "#," + fmt;
				else
					fmt = "#" + fmt;
			}
		}
		fmt += "0";
		if (scale > 0)
		{
			fmt += ".";
			for(i=0; i < scale; i++)
				fmt += "0";
		}

		DecimalFormat formatFloat=new DecimalFormat(fmt);
		return formatFloat.format(value);
   }

	/**
	 * 문자열 관련
	 * 테이블내에 들어갈 글자가 긴 경우 maxNum 만큼만 남기고 "..."으로 처리
	 * <br>이 있는 경우 사용
	 * @param	strInText   문자열소스
	 *		  intStart  	길이
	 */
	public static String getSubString(String strInText, int intMaxNum){
		int intLen =strInText.length();
		int intCount = 0;
		char charC;
		int intSum=0;
		if(intLen > 0){
			strInText.replace("<br>", "\n");
			for(intSum=0; intSum < intLen; intSum++){
				charC = strInText.charAt(intSum);
				if(intCount > intMaxNum) break;
				if(charC>127) intCount +=2;
				else intCount ++;
			}
		}else{
			strInText = "";
		}
		return (intLen > intSum)? strInText.substring(0, intSum).replace("\n", "<br>") + "..." : strInText;
	}

	/**
	 * str이 searchText를 포함하는지
	 * 포함하면 true, 포함하지 않으면 false
	 * @param str
	 * @param searchText
	 * @return
	 */
	public static boolean isExit(String str, String searchText) {
		int idx = str.indexOf(searchText);

		if (idx < 0) return false;
		else return true;
	}

//	public static void main(String args[]) {
//		CommonUtil.writelog(strLAdd("409","0",6));
//		CommonUtil.writelog(strRAdd("409","0",6));
//	}
//


	/**
	 * 날짜별 로그 생성
	 * @param sLog
	 * @param logPath
	 */
	public static void writelog(String sLog)
    {
		System.out.println(sLog);
        /**
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd");
        String sToday = formatter.format(cal.getTime()); //for log-time
        String sDate = formatter2.format(cal.getTime()); //for file-name

        String sFileName = "MSB_"+sDate +".out";
        sLog = "[" + sToday + "]:" + sLog;
        try
         {
//        	System.out.println("1 = " + new String(sLog.getBytes("utf-8"), "euc-kr"));
//            System.out.println("2 = " + new String(sLog.getBytes("utf-8"), "ksc5601"));
//            System.out.println("3 = " + new String(sLog.getBytes("utf-8"), "x-windows-949"));
//            System.out.println("4 = " + new String(sLog.getBytes("utf-8"), "iso-8859-1"));
//
//            System.out.println("5 = " + new String(sLog.getBytes("iso-8859-1"), "euc-kr"));
//            System.out.println("6 = " + new String(sLog.getBytes("iso-8859-1"), "ksc5601"));
//            System.out.println("7 = " + new String(sLog.getBytes("iso-8859-1"), "x-windows-949"));
//            System.out.println("8 = " + new String(sLog.getBytes("iso-8859-1"), "utf-8"));
//
//            System.out.println("9 = " + new String(sLog.getBytes("euc-kr"), "ksc5601"));
//            System.out.println("10 = " + new String(sLog.getBytes("euc-kr"), "utf-8"));
//            System.out.println("11 = " + new String(sLog.getBytes("euc-kr"), "x-windows-949"));
//            System.out.println("12 = " + new String(sLog.getBytes("euc-kr"), "iso-8859-1"));
//
//            System.out.println("13 = " + new String(sLog.getBytes("ksc5601"), "euc-kr"));
//            System.out.println("14 = " + new String(sLog.getBytes("ksc5601"), "utf-8"));
//            System.out.println("15 = " + new String(sLog.getBytes("ksc5601"), "x-windows-949"));
//            System.out.println("16 = " + new String(sLog.getBytes("ksc5601"), "iso-8859-1"));
//
//            System.out.println("17 = " + new String(sLog.getBytes("x-windows-949"), "euc-kr"));
//            System.out.println("18 = " + new String(sLog.getBytes("x-windows-949"), "utf-8"));
//            System.out.println("19 = " + new String(sLog.getBytes("x-windows-949"), "ksc5601"));
//            System.out.println("20 = " + new String(sLog.getBytes("x-windows-949"), "iso-8859-1"));


        //FileOutputStream flWt = new FileOutputStream(filePath+sFileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(filePath+sFileName,true));


//		bw.write(new String(sLog.getBytes("euc-kr"),"ksc5601"));
		bw.write(new String(sLog.getBytes("euc-kr"),"ksc5601"));
            bw.newLine();
            bw.close();
        }
        catch(IOException ie)
        {
            ie.printStackTrace();
        }
        */
    }

	/**
	 * 날짜별 로그 생성
	 * @param sLog
	 * @param logPath
	 */
	public static void writelog(String sLog, Exception e)
    {
		StringBuffer sb = new StringBuffer();
		sb.append(sLog.toString());
	    sb.append("\r\n");
	    StackTraceElement element[] = e.getStackTrace();
	    for (int idx = 0; idx < element.length; idx++) {
	        sb.append("\tat ");
	        sb.append(element[idx].toString());
	        sb.append("\r\n");
	    }
	    /**
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd");
        String sToday = formatter.format(cal.getTime()); //for log-time
        String sDate = formatter2.format(cal.getTime()); //for file-name

        String sFileName = "MSB_"+sDate +".out";
        sLog = "[" + sToday + "]:" + sb.toString();
        System.out.println(sLog);
        try
         {
        	BufferedWriter bw = new BufferedWriter(new FileWriter(filePath+sFileName,true));
        	bw.write(new String(sLog.getBytes("euc-kr"),"ksc5601"));
            bw.newLine();
            bw.close();
        }
        catch(IOException ie)
        {
            ie.printStackTrace();
        }
        */
    }

	// 숫자만 체크.
	public static boolean checkValidityNumber(String strNumbers){
	  for (int i=0;i < strNumbers.length();i++) {
		  char charData = strNumbers.charAt(i);
	   if ((charData < '0' ) || (charData > '9')) {
		  return false;
	   }
	  }
	  return true;
	}

	public static String changeSec(String s, String f) {
		if(f.equals("0")){
			if (s != null && s.length()<11){
				s = s+" 00:00:00";
			}
		}else if(f.equals("1")){
			if (s != null && s.length()>11){
				s = s.substring(0,11);
			}
		}
		return s;
	}

	public static String fromKr(String s) {
		try {
			if (s != null)
				return (new String(s.getBytes("KSC5601"), "8859_1"));
			return s;
		} catch (UnsupportedEncodingException e) {
			return "Encoding Error";
		}
	}

	public static String toKr(String s) {
		try {
			if (s != null) {
				byte[] byteStr = s.getBytes("8859_1");
				String ret = new String(byteStr, "KSC5601");

				int slen = s.length();

				// 한글 중간에서 짤릴 때는 ret가 공백이 됨.
				if ((slen - 1) >= 0 && (ret == null || ret.trim().equals(""))) {
					// 중간에서 짤릴때는 한자를 더 뺀다.
					String tempStr = new String(byteStr, 0, slen - 1, "KSC5601");
					// 한자리가 부족하므로, 자릿수를 맞춤.
					tempStr += " ";

					return tempStr;
				} else
					return ret;
			}
			return s;
		} catch (UnsupportedEncodingException e) {
			return "Encoding Error";
		}

	}

	/**
	 * <pre>
	 * 한글 영문 복합 문자길이 구하기2
	 * </pre>
	 *
	 * @param aStr
	 * @return strlen
	 */
	public static int charLen(String aStr) {
		int strlen = 0;

		for (int j = 0; j < aStr.length(); j++) {
			char c = aStr.charAt(j);
			if ((c < 0xac00) || (0xd7a3 < c))
				strlen++;
			else
				strlen += 2; // 한글이다..
		}
		return strlen;
	}

	/**
	 * <pre>
	 * Null인지를 체크하여 Null인경우에는 빈문자열(&quot;&quot;)을 리턴한다.
	 * </pre>
	 *
	 * @param str_value
	 *            변환할 문자
	 * @return null인 경우 "" 아닌경우는 str_value
	 */
	public static String nullChk(Object str_value) {

		if (str_value == null || str_value.toString().trim().toLowerCase().equals("null")) {
			return "";
		} else {
			return str_value.toString().trim();
		}

	}

	/**
	 * <pre>
	 * Null인지를 체크하여 Null인경우에는 디폴트 값을 리턴한다.
	 * </pre>
	 *
	 * @param str_value
	 *            변환할 문자
	 * @param default_value
	 * @return null인 경우 default_value  아닌경우는 str_value
	 */
	public static String nullChk(Object str_value, String default_value) {
		if (str_value == null || str_value.toString().trim().toLowerCase().equals("null") || str_value.toString().trim().length() == 0 ) {
			return default_value;
		} else {
			return str_value.toString().trim();
		}
	}
	/**
	 * <pre>
	 * 한글 존재 여부와 상관없이 일정한 Byte 단위로 String을 잘라주는 메소드
	 * <br>
	 *  입력된 Byte Size 보다 큰 경우에는 &quot;...&quot;을 추가 한다.
	 * </pre>
	 *
	 * @param asStr
	 *            변환할 문자열
	 * @param aiByteSize
	 *            반환받을 Byte Size
	 * @return 일정길이로 잘라진 문자열
	 */
	public static String getShortString(String asStr, int aiByteSize) {
		int iSize = 0;
		int iLen = 0;

		if (asStr == null)
			return "";

		if (asStr.getBytes().length > aiByteSize) {
			for (iSize = 0; iSize < asStr.length(); iSize++) {
				if (asStr.charAt(iSize) > 0x007F)
					iLen += 2;
				else
					iLen++;

				if (iLen > aiByteSize)
					break;
			}
			asStr = asStr.substring(0, iSize) + "...";
		}
		return asStr;
	}


	/**
	 * 문자열(s) 중에서 특정 문자열(old)을 찾아서 원하는 문자열(replacement)을 변환한다.
	 *
	 * @param s :
	 *            원본 String 문자열
	 * @param old :
	 *            찾는고 하는 문자열
	 * @return String : 바뀌 문자열
	 */
	public static String replace(String s, String old, String replacement) {
		int i = s.indexOf(old);
		StringBuffer r = new StringBuffer();

		if (i == -1)
			return s;
		r.append(s.substring(0, i) + replacement);

		if (i + old.length() < s.length())
			r.append(replace(s.substring(i + old.length(), s.length()), old,
					replacement));

		return r.toString();
	}

	/**
	 * '\n'를 <br>
	 * 로 변환
	 *
	 * @param s :
	 *            원본 String 문자열
	 * @return String : <BR>
	 */
	public static String nToBr(String s) {
		String Br;
		Br = replace(s, "\n", "<BR>");
		return Br;
	}

	/**
	 * <code><pre>
	 *  한글을 영문으로 .
	 *
	 *  @param korean 한글 문자열.
	 *  @retrun 영문 String
	 * </pre></code>
	 */
	public static String K2E(String korean) {
		String english = null;
		if (korean == null)
			return null;

		english = new String(korean);
		try {
			english = new String(new String(korean.getBytes("KSC5601"),
					"8859_1"));
		} catch (UnsupportedEncodingException e) {
			english = korean;
		}

		return english;
	}

	/**
	 * <code><pre>
	 *  영문을 한글로 .
	 *
	 *  @param english 영문 문자열.
	 *  @retrun 한글 String
	 * </pre></code>
	 */
	public static String E2K(String english) {

		String korean = null;
		if (english == null)
			return null;
		// 우선은 생략... 무슨 방침이 있겠지....
		try {
			korean = new String(new String(english.getBytes("8859_1"),
					"KSC5601"));
		} catch (UnsupportedEncodingException e) {
			korean = english;
		}

		return korean;
	}

	/**
	 * String의 sep제거<br>
	 *
	 * @param str
	 * @param sep
	 * @return sep가 제거된 문자
	 * @since 2002.02.01
	 */
	public static String ignoreSeparator(String str, String sep) {
		String str1 = "";
		java.util.StringTokenizer st = new java.util.StringTokenizer(str, sep);
		while (st.hasMoreTokens()) {
			str1 = str1 + st.nextToken();
		}
		return str1;
	}

	/**
	 * <pre>
	 *  int형으로 넘어온 iZero를 String으로 변환하여 그 앞에 sFill을 넣어
	 *        길이가 iLen이 되도록 하는 메소드
	 * </pre>
	 *
	 * @param sFill
	 * @param iZero
	 * @param iLen
	 * @return sZero
	 */
	public static String fillCharFront(String sFill, int iZero, int iLen) {
		String sTemp = "";
		String sZero = String.valueOf(iZero);

		for (int i = 0; i < (iLen - sZero.length()); i++) {
			sTemp = sTemp + sFill;
		}

		return sZero = sTemp + sZero;
	}

	// null check
	public static boolean isNull(Object str) {
		if (str == null || str.toString().toLowerCase().equals("null")
				|| str.toString().trim().equals("")) {
			return true;
		} else {

			return false;
		}
	}

	/**
	 * <code><pre>
	 *  오라클 입력 string으로 만드는 함수
	 *
	 *  @param str_Src 원문
	 *  @retrun &quot;'&quot; 으로 감싸진 안전한 문자열
	 * </pre></code>
	 */

	public static String makeSafeStr(Object str_Src) {
		String str_Return = "";

		if (!isNull(str_Src)) {
			str_Return = replace(str_Src.toString(), "'", "''");
		}

		if (str_Src != null && !str_Src.equals("NULL")) {
			str_Return = "'" + str_Return + "'";
		}
		if (str_Src == null) {
			str_Return = "''";
		}
		// else {
		// str_Return = (String)str_Src;
		// }

		return str_Return;
	}

	/**
	 * <code><pre>
	 *  Single Quote (') =&gt; Double Quote ('') 로 변환
	 *
	 *  by sky 2005.8.17
	 *  @param str_Src 원문
	 *  @retrun DoubleQuote 처리된 문자열
	 * </pre></code>
	 */

	public static String checkSingleQuote(Object str_Src) {
		String str_Return = "";

		if (!isNull(str_Src)) {
			str_Return = replace(str_Src.toString(), "'", "''");
		}
		return str_Return;
	}

	/**
	 * <code><pre>
	 *  값이 없을경우 0 리턴
	 *
	 *  @param str_Src 원문
	 *  @retrun 숫자가 아니거나 값이 없으면 0
	 * </pre></code>
	 */

	public static String makeSafeNumber(Object str_Src) {
		String str_Return = "";

		if (!isNull(str_Src)) {

			str_Return = replace(str_Src.toString(), ",", "");
			str_Return = replace(str_Return, ".0", "");

			if (!isNumber(str_Return))
				str_Return = "0";
		} else
			str_Return = "0";

		return str_Return;
	}

	public static boolean isNumber(Object str_Src) {
		if (isNull(str_Src))
			return false;
		else {
			char[] ca_Src = str_Src.toString().toCharArray();
			for (int i = 0; i < ca_Src.length; i++) {
				if (!Character.isDigit(ca_Src[i]))
					return false;
			}

			return true;
		}

	}


	/*
	 * History : 2004/06/30, fire73, Created Version : 1.0 Comment : string을 정해진
	 * 오라클 date 타입으로 변환 --------------------------------------------------
	 * str_Src : source string
	 * --------------------------------------------------
	 */
	public static String makeOracleDate(String str_Src) {

		if (str_Src != null && !str_Src.toUpperCase().equals("NULL")
				&& !str_Src.equals("")) {
			str_Src = " to_date('" + str_Src + "','yyyy-mm-dd') ";
		} else if (str_Src.toUpperCase().equals("NULL")) {
			;
		} else {
			str_Src = " sysdate ";
		}

		return str_Src;

	}

	/*
	 * History : 2004/06/30, fire73, Created Version : 1.0 Comment : string을 정해진
	 * 오라클 date 타입으로 변환 --------------------------------------------------
	 * str_Src : source string
	 * --------------------------------------------------
	 */
	public static String makeOracleDateOnly(String str_Src) {

		if (str_Src != null && !str_Src.toUpperCase().equals("NULL")
				&& !str_Src.equals("")) {
			str_Src = " to_date('" + str_Src + "','yyyy-mm-dd') ";
		} else if (str_Src.toUpperCase().equals("NULL")) {
			;
		} else {
			str_Src = " to_date('') ";
		}
		return str_Src;
	}

	/*
	 * History : 2004/08/05, fire73, Created Version : 1.0 Comment : string을 정해진
	 * 오라클 date 타입으로 변환 --------------------------------------------------
	 * str_Src : source string
	 * --------------------------------------------------
	 */
	public static String makeOracleDateTime(String str_Src) {

		if (str_Src != null && !str_Src.toUpperCase().equals("NULL")
				&& !str_Src.equals("")) {
			str_Src = " to_date('" + str_Src + "','yyyy-mm-dd hh24') ";
		} else if (str_Src.toUpperCase().equals("NULL")) {
			;
		} else {
			str_Src = " sysdate ";
		}

		return str_Src;

	}
	public static String makeOracleTime(String str_Src) {
		if (str_Src != null && !str_Src.toUpperCase().equals("NULL")
				&& !str_Src.equals("")) {
			str_Src = str_Src.substring(0,2) + ":" + str_Src.substring(2,4);
		} else if (str_Src.toUpperCase().equals("NULL")) {

			str_Src = " ";
		} else {
			str_Src = " ";
		}
		return str_Src;

	}
	/*
	 * History : 2004/06/30, fire73, Created Version : 1.0 Comment : string을 정해진
	 * 오라클 time 타입으로 변환 --------------------------------------------------
	 * str_Src : source string
	 * --------------------------------------------------
	 */
	public static String makeOracleDatePattern(String str_Src,
			String str_Pattern) {

		if (str_Src != null && !str_Src.equals("")) {
			str_Src = " to_date('" + str_Src + "','" + str_Pattern + "') ";
		} else {
			// str_Src = " to_date('" + DateUtil.getDateByPattern(str_Pattern) +
			// "','" + str_Pattern + "') ";
			// str_Src = " to_date(sysdate ,'" + str_Pattern + "') ";
			str_Src = " sysdate ";
		}

		return str_Src;

	}

	/*
	 * History : 2004/08/05, fire73, Created Version : 1.0 Comment : request로 부터
	 * parameter값을 가져올때 null exception 발생을 방지
	 * -------------------------------------------------- obj_Src : source
	 * Object --------------------------------------------------
	 */
	public static String safeGetString(Object obj_Src) {
		if (obj_Src == null)
			return "";
		else {
			// System.out.println("not null");
			return replace((String) obj_Src, "'", "''");
			// return (String) obj_Src;
		}
	}


	public static String int2str(int num) throws Exception {
		return Integer.toString(num);
	}

	public static int str2int(String str) throws Exception {
		if (str == null || str.equals("")) {
			return 0;
		}
		Integer d = new Integer(str);
		return d.intValue();
	}

	public static int str2int(Object obj) throws Exception {
		String str = nullChk(obj,"0");
		if (str == null || str.equals("")) {
			return 0;
		}
		Integer d = new Integer(str);
		return d.intValue();
	}

	public static float str2dou(String str) throws Exception {
		if (str == null || str.equals("")) {
			return 0;
		}
		Float d = new Float(str);
		return d.floatValue();
	}

	public static float str2dou1(String str) throws Exception {

		return Float.parseFloat(str);
	}

	/*
	 * History : 2004/10/19, fire73, Created Version : 1.0 Comment : 긴문장을 지정 길이로
	 * 잘라서 String[] 형태로 리턴 --------------------------------------------------
	 * s_Src : 원문 --------------------------------------------------
	 */
	public static String[] makeShotString(String s_Src, int i_Len) {
		byte[] byt_Src = s_Src.getBytes();

		String[] sa_Return;

		int i_ReturnCnt = 0;
		int i_StartIndex = 0;
		int i_EndIndex = 0;

		// System.out.print("s_Src length ==>");
		// System.out.println(byt_Src.length);

		if ((byt_Src.length % i_Len) == 0)
			i_ReturnCnt = byt_Src.length / i_Len;
		else
			i_ReturnCnt = (byt_Src.length / i_Len) + 1;

		sa_Return = new String[i_ReturnCnt];

		// System.out.print("i_ReturnCnt ==>");
		// System.out.println(i_ReturnCnt);

		for (int i = 0; i < i_ReturnCnt; i++) {
			i_StartIndex = i_EndIndex;

			if (i < i_ReturnCnt - 1)
				i_EndIndex = i_EndIndex + i_Len;
			else
				i_EndIndex = byt_Src.length - 1;

			if ((char) byt_Src[i_EndIndex] != ' '
					&& i_EndIndex != byt_Src.length - 1) {
				boolean b_Continue = true;
				for (int j = i_EndIndex; j > i_StartIndex && b_Continue; j--) {

					if ((char) byt_Src[j] == ' ') {
						i_EndIndex = j;
						b_Continue = false;
					}

				}
			}

			// System.out.print("i_EndIndex==>");
			// System.out.println(i_EndIndex);

			sa_Return[i] = new String(byt_Src, i_StartIndex, i_EndIndex
					- i_StartIndex);

			/*
			 * System.out.print("================"); System.out.print(i);
			 * System.out.println("================");
			 * System.out.println(sa_Return[i]);
			 * System.out.println("=====================================");
			 */

		}

		return sa_Return;

	}

	/**
	 * Hashtable 의 값을 Insert , Update 하기 위한 Properties 로 바꾼다. 조회한 내용으로 Copy
	 * Insert 할때 사용
	 *
	 * @param ht
	 * @return
	 * @throws Exception
	 */
	public static Properties h2Props(Hashtable ht) throws Exception {
		Properties prop = new Properties();
		Enumeration enm = ht.keys();
		while (enm.hasMoreElements()) {
			String key = (String) enm.nextElement();
			prop.put(key.toUpperCase(), ht.get(key));
			// System.out.println("KEY=" + key + " " + ht.get(key) + "\n");
		}

		return prop;
	}



	public static Properties getDefaultParamValues(Hashtable param,
			Properties prop) throws Exception {
		String key = "";
		String value = "";
		Enumeration enume = param.keys();
		while (enume.hasMoreElements()) {
			key = (String) enume.nextElement();
			value = (String) param.get(key);
			// System.out.println("Properties Key Name - " + key + "\n");
			// System.out.println("Properties Values Name - " + value + "\n");
			prop.put(key, value);
		}
		return prop;
	}

	// 금액변환 유틸 추가 by sky. 2005.09.06
	/**
	 * <pre>
	 *  금액문자열을 금액표시타입으로 변환한다.
	 *  (예) 12345678 --&gt; 12,345,678
	 * </pre>
	 *
	 * @param strMoney
	 *            금액문자열
	 * @param delimeter
	 *            금액표시 구분자
	 * @return 변경된 금액 문자열
	 */
	public static String makeMoneyType(String strMoney, String delimeter) {
		if("".equals(strMoney) || "null".equals(strMoney)) return "";
		DecimalFormat df = new DecimalFormat();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();

		dfs.setGroupingSeparator(delimeter.charAt(0));
		df.setGroupingSize(3);
		df.setDecimalFormatSymbols(dfs);

		return (df.format(Double.parseDouble(strMoney))).toString();
	}

	public static String makeMoneyType(Object objMoney, String delimeter) {
		String strMoney = nullChk(objMoney);
		if("".equals(strMoney) || "null".equals(strMoney)) return "";
		DecimalFormat df = new DecimalFormat();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();

		dfs.setGroupingSeparator(delimeter.charAt(0));
		df.setGroupingSize(3);
		df.setDecimalFormatSymbols(dfs);

		return (df.format(Double.parseDouble(strMoney))).toString();
	}

	/**
	 * <pre>
	 *  숫자를 금액표시타입으로 변환한다.
	 *  (예) 12345678 --&gt; 12,345,678
	 * </pre>
	 *
	 * @param intMoney
	 *            금액
	 * @param delimeter
	 *            금액표시 구분자
	 * @return 변경된 금액 문자열
	 */
	public static String makeMoneyType(int intMoney, String delimeter) {
		return (makeMoneyType(Integer.toString(intMoney), delimeter));
	}


	/**
	 * <pre>
	 *  다중 파일명
	 *  (예) "file1:file2:file3" --&gt; ("file_name1", "file1") ("file_name2", "file2") ("file_name3", "file3")
	 *  (예) "1:2:1" --&gt; ("file_gubn1", "1") ("file_gubn2", "2") ("file_gubn3", "1")
	 * </pre>
	 *
	 * @param list
	 *            VMRS의 list
	 * @param delimeter
	 *            파일명 구분자
	 * @return 변경된 VMRS의 list
	 */
	public static Vector getFileNames(Vector list, String delimeter) {
		Enumeration e_num = list.elements();
		Hashtable ht = (Hashtable)e_num.nextElement();

		java.util.StringTokenizer st = new java.util.StringTokenizer((String)ht.get("conts_file"),delimeter);
		int i = 1;

		while ( st.hasMoreTokens() ) {
			ht.put("file_name"+i, st.nextToken());
//				System.out.println("파일명"+i+" : "+(String)ht.get("file_name"+i));
			i++;
		}

		st = new java.util.StringTokenizer((String)ht.get("s_1"),delimeter);
		i = 1;

		while ( st.hasMoreTokens() ) {
			ht.put("file_gubn"+i, st.nextToken());
//				System.out.println("구분자"+i+" : "+(String)ht.get("file_gubn"+i));
			i++;
		}

		list.addElement(ht);

		return list;
	}

	/**
	 * <pre>
	 *  다중 첨부파일 처리 메소드
	 *  CONTS_FILE 및 S_1의 값을 넘겨 받아 구분자로 구분한다.
	 * </pre>
	 *
	 * @param file_name
	 *            CONTS_FILE
	 * @param file_gubn
	 *            S_1
	 * @param delimeter
	 *            구분기호 ":"
	 * @return (ht,ht,ht,...)로 구성된 벡터
	 */
	public static Vector getFileNames(String file_name, String file_gubn, String delimeter) {
		Vector list = new Vector();

		java.util.StringTokenizer st1 = new java.util.StringTokenizer(file_name, delimeter);
		java.util.StringTokenizer st2 = new java.util.StringTokenizer(file_gubn, delimeter);

		while ( st1.hasMoreTokens() ) {
			Hashtable ht = new Hashtable();
			ht.put("file_name", st1.nextToken());
			if( st2.hasMoreTokens() )
				ht.put("file_gubn", st2.nextToken());
			System.out.println("file_name : "+(String)ht.get("file_name"));
			System.out.println("file_gubn : "+(String)ht.get("file_gubn"));
			list.addElement(ht);
		}

		return list;
	}

	public static String resCheckScript(String arg) {
		String rValue = "";
		if(arg != null || !arg.equals("")){
			rValue = arg.replaceAll("<","&lt;");
			rValue = arg.replaceAll(">","&gt;");
		}
		return rValue;
	}

	public static String getStrLenBr(String asStr, int strSize) {
		String str_tmp="";
		if (asStr == null || asStr.equals(""))
			return "";
		if(asStr.length() > strSize){
			str_tmp = asStr.substring(0,strSize) + "<br>" + asStr.substring(strSize,asStr.length()-1);
		}else{
		    str_tmp = asStr;
		}

		return str_tmp;
	}

	//지정된 길이로 문자열 잘라주기  substring
	public static String substring(String str, int i, int j) {
		if (str==null) return "";
		return (str.length()>i) ? str.substring(i,j) : str;
    }

	public static String[] split(String str, String sep){
		return str.split("\\"+sep);
	}

	//HTML TAG 삭제 2008.03.26
	public static String stripTags(String str) {
		if (str==null) return "";
		else return str.replaceAll("/<\\/?[^>]+>/gi", "");
    }

	public static int indexof(String str, String ss){
		return str.indexOf(ss);
	}

	public static String addstring(String str, String ss){
		return str + ss;
	}

	// 소문자 -> 대문자로 변경 2009.07.28
	public static String convertUpper(String str) {
		String result="";

		for(char c : str.toCharArray()){
			//소문자일 경우 대문자로 변경
			if(Character.isLowerCase(c)){
				result+= toUpperCase(c);
			//알파벳이 아닌경우 그냥 출력
			}else{
				result+= c;
			}
		}
		return result;
	}

	//입력 받은 문자를 대문자로 변경한다.
	private static char toUpperCase(char c) {
		return Character.toUpperCase(c);
	}


	// 대문자 -> 소문자로 변경 2009.07.28
	public static String convertLower(String str) {
		String result="";

		for(char c : str.toCharArray()){
			//대문자 일경우 소문자로 변경
			if( Character.isUpperCase(c) ){
				result+= toLowerCase(c);
			//알파벳이 아닌경우 그냥 출력
			}else{
				result+= c;
			}
		}
		return result;
	}

	//입력받은 문자를 소문자로 변경한다.
	private static char toLowerCase(char c) {
		return Character.toLowerCase(c);
	}


	/**
	 * <code><pre>
	 *  값이 없을경우 0 리턴
	 *
	 *  @param str_Src 원문
	 *  @retrun 숫자가 아니거나 값이 없으면 0
	 * </pre></code>
	 */

	public static String makeSafeNumber(Object str_Src, String default_value) {
		String str_Return = "";
		default_value = makeSafeNumber(default_value);

		if (!isNull(str_Src)) {

			str_Return = replace(str_Src.toString(), ",", "");
			str_Return = replace(str_Return, ".0", "");

			if (!isNumber(str_Return))
				str_Return = default_value;
		} else
			str_Return = default_value;

		return str_Return;
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
		pattern = CommonUtil.nullChk(pattern);

		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(source);

		if (m.find()) {
			result = m.group();
		}
		return result;
	}

	/**
	 * <code><pre>
	 *  SQL Injection 방지를 위해 입력문자 조정
	 *
	 * </pre></code>
	 */
	public static String inputTxtFilter(Object src_Str){
		String str_Return = "";

		if (src_Str == null){
			return "";
		}
		else {
			str_Return = src_Str.toString();

			str_Return = str_Return.replaceAll("'", "''");
			str_Return = str_Return.replaceAll("--", "");
			str_Return = str_Return.replaceAll(";", "");
			str_Return = str_Return.replaceAll("\\&", "&#38;");
			str_Return = str_Return.replaceAll("<", "&lt;");
			str_Return = str_Return.replaceAll(">", "&gt;");
			str_Return = str_Return.replaceAll("\\%", "&#37;");
			str_Return = str_Return.replaceAll("\\$", "&#36;");
			str_Return = str_Return.replaceAll("\\|", "");
			str_Return = str_Return.replaceAll("\\(", "&#40;");
			str_Return = str_Return.replaceAll("\\)", "&#41;");
			str_Return = str_Return.replaceAll("\\+", "&#43;");

			return (str_Return);

		}
	}

	/**
	 * @param str
	 * @param cutsize
	 * @return
	 */
	public static String getHtmlString(String str,int cutsize) {
		String rStr = "";
		String[] substr = new String[cutsize];
		byte[] temp = str.getBytes();

		int count = 0;
		int str_count = 0;

		while(temp.length > cutsize){
			count = 0;
			for(int i=0; i < cutsize; i++) {
				if(temp[i]<0) count++;
			}

			if(count%2!=0)
			{
				substr[str_count] = new String(temp, 0, cutsize+1);
				temp = new String(temp, cutsize+1, temp.length-(cutsize+1)).getBytes();
			}else
			{
				substr[str_count] = new String(temp, 0, cutsize);
				temp = new String(temp, cutsize, temp.length-cutsize).getBytes();
			}

			str_count++;
		}
		substr[str_count] = new String(temp);

		for(int i=0; i<=str_count; i++)
			rStr += substr[i] + "<br />";

		return rStr;
	}

	/**
	 * HTTP request를 위한 특수문자 변환
	 * @param reqStr
	 * @return
	 */
	public static String convertHtmlRequest(String reqStr) {
		reqStr = nullChk(reqStr);
		reqStr = reqStr.replaceAll("%", "%25"); // 무조건 첫번째여야 함!!!
		reqStr = reqStr.replaceAll(" ", "%20");
		reqStr = reqStr.replaceAll("!", "%21");
		reqStr = reqStr.replaceAll("\"", "%22");
		reqStr = reqStr.replaceAll("#", "%23");
		reqStr = reqStr.replaceAll("&", "%26");
		reqStr = reqStr.replaceAll("'", "%27");
		reqStr = reqStr.replaceAll("\\(", "%28");
		reqStr = reqStr.replaceAll("\\)", "%29");
		reqStr = reqStr.replaceAll("\\*", "%2A");
		reqStr = reqStr.replaceAll("\\+", "%2B");
		reqStr = reqStr.replaceAll(",", "%2C");
		reqStr = reqStr.replaceAll("/", "%2F");
		reqStr = reqStr.replaceAll(":", "%3A");
		reqStr = reqStr.replaceAll(";", "%3B");
		reqStr = reqStr.replaceAll("<", "%3C");
		reqStr = reqStr.replaceAll("=", "%3D");
		reqStr = reqStr.replaceAll(">", "%3E");
		reqStr = reqStr.replaceAll("\\?", "%3F");
		reqStr = reqStr.replaceAll("@", "%40");
		reqStr = reqStr.replaceAll("\\\r\\\n","%0D%0A");
		return reqStr;
	}

	/**
	 * Host 연계시 특수문자 체크1
	 * @param reqStr
	 * @return
	 */
	public static String hostRequestChk1(String reqStr) {
		reqStr = nullChk(reqStr);
		reqStr = reqStr.replaceAll("%", "");
		reqStr = reqStr.replaceAll(" ", "");
		reqStr = reqStr.replaceAll("!", "");
		reqStr = reqStr.replaceAll("\"", "");
		reqStr = reqStr.replaceAll("#", "");
		reqStr = reqStr.replaceAll("&", "");
		reqStr = reqStr.replaceAll("'", "");
		reqStr = reqStr.replaceAll("\\(", "");
		reqStr = reqStr.replaceAll("\\)", "");
		reqStr = reqStr.replaceAll("\\*", "");
		reqStr = reqStr.replaceAll("\\+", "");
		reqStr = reqStr.replaceAll(",", "");
		reqStr = reqStr.replaceAll("/", "");
		reqStr = reqStr.replaceAll(":", "");
		reqStr = reqStr.replaceAll(";", "");
		reqStr = reqStr.replaceAll("<", "");
		reqStr = reqStr.replaceAll("=", "");
		reqStr = reqStr.replaceAll(">", "");
		reqStr = reqStr.replaceAll("\\?", "");
		reqStr = reqStr.replaceAll("@", "");
		reqStr = reqStr.replaceAll("\\\r\\\n","");
		reqStr = reqStr.replaceAll("0", "");
		reqStr = reqStr.replaceAll("1", "");
		reqStr = reqStr.replaceAll("2", "");
		reqStr = reqStr.replaceAll("3", "");
		reqStr = reqStr.replaceAll("4", "");
		reqStr = reqStr.replaceAll("5", "");
		reqStr = reqStr.replaceAll("6", "");
		reqStr = reqStr.replaceAll("7", "");
		reqStr = reqStr.replaceAll("8", "");
		reqStr = reqStr.replaceAll("9", "");
		return reqStr;
	}

	/**
	 * Host 연계시 특수문자 체크2
	 * @param reqStr
	 * @return
	 */
	public static String hostRequestChk2(String reqStr) {
		reqStr = nullChk(reqStr);
		reqStr = reqStr.replaceAll("%", "");
		reqStr = reqStr.replaceAll(" ", "");
		reqStr = reqStr.replaceAll("!", "");
		reqStr = reqStr.replaceAll("\"", "");
		reqStr = reqStr.replaceAll("#", "");
		reqStr = reqStr.replaceAll("&", "");
		reqStr = reqStr.replaceAll("'", "");
		reqStr = reqStr.replaceAll("\\(", "");
		reqStr = reqStr.replaceAll("\\)", "");
		reqStr = reqStr.replaceAll("\\*", "");
		reqStr = reqStr.replaceAll("\\+", "");
		reqStr = reqStr.replaceAll(",", "");
		//reqStr = reqStr.replaceAll("/", "%2F");
		reqStr = reqStr.replaceAll(":", "");
		reqStr = reqStr.replaceAll(";", "");
		reqStr = reqStr.replaceAll("<", "");
		reqStr = reqStr.replaceAll("=", "");
		reqStr = reqStr.replaceAll(">", "");
		reqStr = reqStr.replaceAll("\\?", "");
		reqStr = reqStr.replaceAll("@", "");
		reqStr = reqStr.replaceAll("\\\r\\\n","");
		return reqStr;
	}


	/**
	 * <code><pre>
	 *  입력값 Injection 방지를 위해 입력문자 조정
	 *
	 * </pre></code>
	 */
	public static String inputTxtFilterWeb(Object src_Str){
		String str_Return = "";

		if (src_Str == null || src_Str.equals("")){
			return "";
		}
		else {
			str_Return = src_Str.toString();

			str_Return = str_Return.replaceAll("'", "''");
			str_Return = str_Return.replaceAll("--", "");
			str_Return = str_Return.replaceAll(";", "");
			str_Return = str_Return.replaceAll("\\&", "&amp;");
			str_Return = str_Return.replaceAll("<", "&lt;");
			str_Return = str_Return.replaceAll(">", "&gt;");
			str_Return = str_Return.replaceAll("\\%", "&#37;");
			str_Return = str_Return.replaceAll("\\$", "&#36;");
			str_Return = str_Return.replaceAll("\\#", "&#35;");
			str_Return = str_Return.replaceAll("\\|", "");
			str_Return = str_Return.replaceAll("\\(", "&#40;");
			str_Return = str_Return.replaceAll("\\)", "&#41;");
			str_Return = str_Return.replaceAll("\\+", "&#43;");

			return (str_Return);

		}
	}



	/***
	 * 문자열을 '-' 자른후 인덱스 번호 반환
	 * @param str
	 * @param size
	 * @return
	 */
	public static String getAarryIdx(String str, int size) {

		String[] arrStr =str.split("-");
		if(arrStr.length > size){
			return arrStr[size];
		}

		return "";
	}


    public static String rtnTrim(String str) {

        if(str == null || str.equals("")) {
        	return "";
        } else {
        	return str.trim();
        }

	}

}
