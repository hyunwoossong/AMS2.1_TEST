package com.trx.evalleyvs.eai.util;

/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.StringTokenizer;









import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trx.Msgprocess.CreateAck;
import com.trx.evalleyvs.eai.cargo.util.ValidationUtil;
import com.trx.evalleyvs.eai.exception.SystemException;
import com.trx.evalleyvs.eai.message.MessageXMLResourceManager;

/*
 * EDIFACT <-> RFC2351 Converter
 */
public class RFC2351Util {
	
	private static final Logger logger = LoggerFactory.getLogger(RFC2351Util.class);
	static MessageXMLResourceManager mxr = MessageXMLResourceManager.getInstance("MessageResource.xml");
	private static String KE_SITA_CODE = "SELFMKE";
	private static String MSB_SITA_CODE = "MSBFMKE";
    
	
	//FWB, FHL, FZB, FSU, FYT 제외한 문서 = VHEG (MSB 1.0)
	public static final String RA_R_SEND_PATTERN_MSB1 = new StringBuffer()
	.append("V.\r\n")
	.append("VHEG.WA/E1KRCGQ/I1KECG/P{0}\r\n")
	.append("VGZ\r\n")
	.append("\r\n")
	.append("QK {1}\r\n")
	.append(".{2} {3} {4} {5}\r\n")
	.append("{6}")
	.toString();
		
		
	//FWB, FHL, FZB, FSU, FYT = VLEG (MSB 1.0)
	public static final String NO_R_SEND_PATTERN_MSB1 = new StringBuffer()
	.append("V.\r\n")
	.append("VLEG.WA/E1KRCGQ/I1KECG/P{0}\r\n")
	.append("VGZ\r\n")
	.append("\r\n")
	.append("QK {1}\r\n")
	.append(".{2} {3} {4} {5}\r\n")
	.append("{6}")
	.toString();
	
	//FWB, FHL, FZB, FSU, FYT 제외한 문서 = VHEG
	public static final String RA_R_SEND_PATTERN = new StringBuffer()
	.append("QK {1}\r\n")
	.append(".{2} {3} {4} {5}\r\n")
	.append("{6}")
	.toString();
	
	
	//FWB, FHL, FZB, FSU, FYT = VLEG
	public static final String NO_R_SEND_PATTERN = new StringBuffer()
	.append("QK {1}\r\n")
	.append(".{2} {3} {4} {5}\r\n")
	.append("{6}")
	.toString();
	
	
	/*
	 * RFC2351 -> EDIFACT ( MSB 1.0 )
	 */
	public static HashMap<String,Object> convertRecvDataKEMatip(String message) {
		StringBuffer sb = new StringBuffer();
		HashMap map = new HashMap();
		String counter = "";
		String recipientId = "";
		StringTokenizer st = new StringTokenizer(message, "\r\n");

		int inx = 0;
		/** 변환전
		 *  V.
         *  VLEG.WA/E1KECG/I1KRCGR/P0001
         *  VGZ
         *  
		 *  QD MSBFMKE
			.SELFMKE 041458 RKRAGT85CYBERLOGITEC/SEL01 -FRAKE150304004
			FMA
			ACK/OK HOUSE CREATED  -18083073174.ADA90605
			FHL/4
			MBI/180-83073174FRAICN/T97K5645.0
		 */
		/**
		 *  변환 후
		 *  UNA:+.? 'UNB+IATA:1+RKRAIR08KAL:PIMA+RKRAGT85CYBERLOGITEC/SEL01:PIMA:FRAKE150304004+150304:1458+0000+0'UNH+0000+CIMFMA:0+0001'FMA
			ACK/OK HOUSE CREATED  -18083073174.ADA90605
			FHL/4
			MBI/180-83073174FRAICN/T97K5645.0
			'UNT+3+0000'UNZ+1+0000'
		 */
		while (st.hasMoreElements())
		{
			String token = st.nextElement().toString();

			if (inx == 1) {
				counter = ValidationUtil.extractPatternData(token, "\\p{Digit}{4}");
				map.put("COUNTER", counter);
			}
			else if (inx == 4) {
				String[] items = token.trim().split(" ");
				
				
				recipientId =  items[2];
				if (items.length > 2) 
				{
					map.put("TGT_PIMA", recipientId); 
				}
				// MSB_OUT_REVERSE_KEY 를 추가한다. 
				if(items.length > 3)
				{
					// 최종값의 ACK를 반영 한다.
					if (items[items.length - 1] != null)
					{	
						map.put("MATIP_OUT_REVERSE_KEY", items[items.length - 1].replaceAll("-", "")); 
					}
				}
				map.put("DATE", ValidationUtil.extractPatternData(token, "\\p{Digit}{6}"));
				
				map.put("TRANSACTION_ID" , recipientId +"Q" + counter);
			}else if (inx == 5) {
				map.put("SMI", ValidationUtil.extractPatternData(token, "\\p{Upper}{3}"));
				map.put("VERSION", CommonUtil.nvl(ValidationUtil.extractPatternData(token, "\\p{Digit}{1,3}"), "0"));
				sb.append(token + "\r\n");
			}else if (inx == 6 && map.get("SMI").equals("FVA")){
				//AVR/ICNPVG/14MAR
				String[] arr_fvr = null;
				String fvr_org = "";
				String fvr_dest = "";
				String fvr_req_d = "";
				arr_fvr = token.split("/");		
				map.put("FVR_ORG", arr_fvr[1].substring(0,3));
				map.put("FVR_DEST", arr_fvr[1].substring(3,arr_fvr[1].length()));
				map.put("FVR_REQ_D", arr_fvr[2]);
			}
			if (inx > 5) {
				sb.append(token + "\r\n");
			}

			inx = inx + 1;
		}
		map.put("BODY", sb.toString());
		map.put("SRC_PIMA", "RKRAIR08KAL");

		return map;
	}
	
	/*
	 * RFC2351 -> EDIFACT
	 */
	public static HashMap<String,Object> convertRecvData(String message) throws Exception, SystemException {
		StringBuffer sb = new StringBuffer();
		HashMap map = new HashMap();
		String counter = "";
		String recipientId = "";
		StringTokenizer st = new StringTokenizer(message, "\r\n");

		int inx = 0;
		/** 변환전
		 *  QD MSBFMKE
			.SELFMKE 041458 RKRAGT85CYBERLOGITEC/SEL01 -FRAKE150304004
			FMA
			ACK/OK HOUSE CREATED  -18083073174.ADA90605
			FHL/4
			MBI/180-83073174FRAICN/T97K5645.0
		 */
		/**
		 *  변환 후
		 *  UNA:+.? 'UNB+IATA:1+RKRAIR08KAL:PIMA+RKRAGT85CYBERLOGITEC/SEL01:PIMA:FRAKE150304004+150304:1458+0000+0'UNH+0000+CIMFMA:0+0001'FMA
			ACK/OK HOUSE CREATED  -18083073174.ADA90605
			FHL/4
			MBI/180-83073174FRAICN/T97K5645.0
			'UNT+3+0000'UNZ+1+0000'
		 */
		while(st.hasMoreElements())
		{
			String token = st.nextElement().toString();

			if(inx == 1){
				String[] items = token.trim().split(" ");
				
				
				recipientId =  items[2];
				if(items.length > 2) 
				{
					map.put("TGT_PIMA", recipientId); 
				}
				// MSB_OUT_REVERSE_KEY 를 추가한다. 
				if(items.length > 3)
				{
					// 최종값의 ACK를 반영 한다.
					if (items[items.length - 1] != null)
					{	
						map.put("MATIP_OUT_REVERSE_KEY", items[items.length - 1].replaceAll("-", "")); 
					}
				}
				map.put("DATE", ValidationUtil.extractPatternData(token, "\\p{Digit}{6}"));
			}else if (inx == 2) {
				map.put("SMI", ValidationUtil.extractPatternData(token, "\\p{Upper}{3}"));
				map.put("VERSION", CommonUtil.nvl(ValidationUtil.extractPatternData(token, "\\p{Digit}{1,3}"), "0"));
				sb.append(token + "\r\n");
			}else if (inx == 3 && map.get("SMI").equals("FVA")){
				//AVR/ICNPVG/14MAR
				String[] arr_fvr = null;
				String fvr_org = "";
				String fvr_dest = "";
				String fvr_req_d = "";
				arr_fvr = token.split("/");		
				map.put("FVR_ORG", arr_fvr[1].substring(0,3));
				map.put("FVR_DEST", arr_fvr[1].substring(3,arr_fvr[1].length()));
				map.put("FVR_REQ_D", arr_fvr[2]);
			}
			
			if (inx > 2) {
				sb.append(token + "\r\n");
			}
			inx = inx + 1;
		}
		map.put("BODY", sb.toString());
		map.put("SRC_PIMA", "RKRAIR08KAL");

		return map;
	}
	
	/*
	 * EDIFACT -> RFC2351 (MSB 1.0)
	 */
	public String convertSendDataKEMatip(HashMap<String,Object> edi) throws Exception, SystemException {
	
		// 1. 변수 선언
		String result = "";
		String sendPattern = "";
		// 2. 변수 정의
		String message = CommonUtil.nvl(edi.get("BODY"));
		String smi = CommonUtil.nvl(edi.get("SMI"));
		//UNB+IATA:1+RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA+AF:PIMA+140428:1820+AIRCISFHL+0'UNH+SELWOO+CIMFHL:4+SELWOO'FHL/4
		String header = CommonUtil.nvl(edi.get("HEADER"));
		String MSB_OUT_REVERSE_KEY = CommonUtil.nvl(edi.get("MSB_OUT_REVERSE_KEY"));
		
		String[] header_check = null;		
		header_check = header.split("\\+");
		String[] pima_check = null;
		pima_check = header_check[2].split(":");
		//System.out.println(pima_check[0]); //sender pima
		//System.out.println(pima_check[2]); //agt code
		
		//System.out.println("header_check="+header_check[4]);  //yyyymmdd:hhss
		header_check = header_check[4].split(":");
		//RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA
		
		String date = CommonUtil.nvl(header_check[0], CommonUtil.getCurrentDateByFormat("yyMMdd"));
		
		String time = CommonUtil.nvl(header_check[1], CommonUtil.getCurrentDateByFormat("HHmm"));
		String dateWithTime = (date.length() > 4)? date.substring(4)+time : CommonUtil.getCurrentDateByFormat("dd")+time;

		String senderAgtCode = "";
		senderAgtCode = "-"+MSB_OUT_REVERSE_KEY;	
						
		String counter = "0000"; 
		String sendPima = CommonUtil.nvl(pima_check[0]);
		
		
		if ("FWB".equals(smi) || "FHL".equals(smi) || "FZB".equals(smi) || "FSU".equals(smi) || "FYT".equals(smi)) {
			sendPattern = NO_R_SEND_PATTERN_MSB1;
		}
		else {
			sendPattern = RA_R_SEND_PATTERN_MSB1;
		}
		
		Object[] args = new Object[] {
			counter,				// Counter
			KE_SITA_CODE,			// KAL SITA CODE
			MSB_SITA_CODE,			// MSB SITA CODE
			dateWithTime,			// 시간 (ddHHss)
			sendPima,				// Sender PIMA 
			senderAgtCode,			// Sender PIMA  + agtCode
			message,
		};
		result = MessageFormat.format(sendPattern, args);
		return result;
		
	}
	
	/*
	 * EDIFACT -> RFC2351
	 */
	public String convertSendData(HashMap<String,Object> edi) throws Exception, SystemException {
	
		// 1. 변수 선언
		String result = "";
		String sendPattern = "";
		// 2. 변수 정의
		String message = CommonUtil.nvl(edi.get("BODY"));
		String smi = CommonUtil.nvl(edi.get("SMI"));
		//UNB+IATA:1+RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA+AF:PIMA+140428:1820+AIRCISFHL+0'UNH+SELWOO+CIMFHL:4+SELWOO'FHL/4
		String header = CommonUtil.nvl(edi.get("HEADER"));
		String MSB_OUT_REVERSE_KEY = CommonUtil.nvl(edi.get("MSB_OUT_REVERSE_KEY"));
		
		String[] header_check = null;		
		header_check = header.split("\\+");
		String[] pima_check = null;
		pima_check = header_check[2].split(":");
		//System.out.println(pima_check[0]); //sender pima
		//System.out.println(pima_check[2]); //agt code
		
		//System.out.println("header_check="+header_check[4]);  //yyyymmdd:hhss
		header_check = header_check[4].split(":");
		//RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA
		
		String date = CommonUtil.nvl(header_check[0], CommonUtil.getCurrentDateByFormat("yyMMdd"));
		
		String time = CommonUtil.nvl(header_check[1], CommonUtil.getCurrentDateByFormat("HHmm"));
		String dateWithTime = (date.length() > 4)? date.substring(4)+time : CommonUtil.getCurrentDateByFormat("dd")+time;

		String senderAgtCode = "";
		senderAgtCode = "-"+MSB_OUT_REVERSE_KEY;
		
		//System.out.println("SMI===="+smi);
		/** FFR REVERSE KEY 문제시 원복 [s]*/
		/**
		if(!"FFR".equals(smi)){
			senderAgtCode = "-"+MSB_OUT_REVERSE_KEY;
		}
		**/
		/** FFR REVERSE KEY 문제시 원복 [e]*/
		
		/**
		if(pima_check[2] != null){
			if(CommonUtil.nvl(pima_check[2]).length() > 0) senderAgtCode = "-"+MSB_OUT_REVERSE_KEY;	
		}
		**/
						
		String counter = "0000"; 
		String sendPima = CommonUtil.nvl(pima_check[0]);
		
		
		if ("FWB".equals(smi) || "FHL".equals(smi) || "FZB".equals(smi) || "FSU".equals(smi) || "FYT".equals(smi)) {
			sendPattern = NO_R_SEND_PATTERN;
		}
		else {
			sendPattern = RA_R_SEND_PATTERN;
		}
		
		Object[] args = new Object[] {
			counter,				// Counter
			KE_SITA_CODE,			// KAL SITA CODE
			MSB_SITA_CODE,			// MSB SITA CODE
			dateWithTime,			// 시간 (ddHHss)
			sendPima,				// Sender PIMA 
			senderAgtCode,			// Sender PIMA  + agtCode
			message,
		};
		result = MessageFormat.format(sendPattern, args);
		return result;
		
	}
}

