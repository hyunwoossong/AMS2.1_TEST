package com.trx.validate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.bind.ValidationException;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.trx.Msgprocess.CreateAck;
import com.trx.evalleyvs.eai.cargo.CargoValidator;
import com.trx.evalleyvs.eai.message.MessageXMLResourceManager;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;
import com.ibatis.sqlmap.client.SqlMapClient;

@Component
public class HeaderCheck {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(HeaderCheck.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	CommonSql commonSql;

	@Autowired
	CommonUtil commonUtil;

	@Autowired
	CreateAck createack = new CreateAck();

	@Autowired
	MainExceptionManager mainExceptionManager;

/*	@Value("#{prop['ktnet.write.path']}")
	//@Value("${ktnet.write.path}")
	private String write_path;*/

	final static String AMS_STEP = "200";
	final static String IKAMS_STEP = "200";

	static MessageXMLResourceManager mxr = MessageXMLResourceManager.getInstance("MessageResource.xml");

	public String headerCheck(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, @Body String msg, Exchange exchange) throws Exception {
		boolean flag = false;
		boolean air_flag = false;
		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		String carr_code;
		String fnaMsg = "";

		try{

			//msg = msg.trim();
			sqlMapClient.startTransaction();
			logger.info(" === Header Check START === ");
			//System.out.println(msg.length());
			
			fhlmap = getFHLInfoString(msg);
			fhlmap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
			exchange.getIn().setHeader("EDI_PARSE", fhlmap);
			
			if(msg.length() > 4000) {
				flag = true;
				logger.info(" === MSG LENGTH 4000 초과 === ");
			}else{
				flag = getMessage(msg);
			}
			
//			flag = getMessage(msg);

			exchange.getIn().setHeader("VERSION",fhlmap.get("VERSION"));
			exchange.getIn().setHeader("SMI",fhlmap.get("SMI"));
			exchange.getIn().setHeader("BODY",fhlmap.get("BODY"));
			exchange.getIn().setHeader("SRC_PIMA",fhlmap.get("SRC_PIMA"));

		}catch(IndexOutOfBoundsException e){
			logger.info(" === Header Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","Header Check IndexOutOfBoundsException",e, msg);
    		throw e;
		}catch(NullPointerException e){
			logger.info(" === Header Check NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","Header Check NullPointerException",e, msg);
    		throw e;
		}catch(SQLException e){
			logger.info(" === Header Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","Header Check SQLException",e, msg);
    		throw e;
		}catch(Exception e){
			logger.info(" === Header Check Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	flag = false;
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","Header Check Exception",e, msg);

	    	String errorCode = new String();
	    	String errorMsg = new String();
	    	if (CommonUtil.isExit(e.toString(), ":::")) {
				errorCode = e.toString().substring(e.toString().indexOf(":::")-4, e.toString().indexOf(":::"));
				errorMsg = e.toString().substring(e.toString().indexOf(":::")+3);
			}else {
				errorCode = "9999";
				errorMsg = e.toString();
			}
	    	errorMsg = CommonUtil.nullChk(errorMsg,"");
	    	
	    	if (fhlmap.isEmpty() || CommonUtil.nullChk(fhlmap.get("BODY")).isEmpty()) {
	    		// No FHL Parsing Info
	    		throw e;
	    	} else {
    			// FNA Create
    			fhlmap.put("ERRORCODE", errorCode);
    			fhlmap.put("ERRORMSG",errorMsg);
    			
    			/** FNA 생성 [s] **/
    			fnaMsg = createack.FNAack(fhlmap);
    			fhlmap.put("AMS_ACK", fnaMsg);
    			fhlmap.put("AMS_SMI", "FNA");
    			/** FNA 생성 [e] **/
	    	}
	    	
			//EDI HISTORY INSERT[S]
			HashMap logMap = new HashMap();
			logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
			logMap.put("AMS_STEP", "209");
			logMap.put("IKAMS_STEP", "209");
			logMap.put("HISTORY_STATUS", "Header Check Error");
			commonSql.insertHistory(logMap);
			
	    }finally{
	    	
	    	if (!flag) {
	    		
				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "Header Check Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", "209");
				logMap.put("IKAMS_STEP", "209");
				commonSql.insertHistory(logMap);
				//FNA 체크
				if(fnaMsg.length() > 0){
					msg = fnaMsg;
		    	}
				
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
				commonSql.AckupdateTrace(logMap, fhlmap);
				logger.info(" === Header Check ERROR === ");
	    	} else {
	    		
	    		String lengthMsg = "";
	    		
	    		HashMap<String,Integer> colMap = new HashMap<String,Integer>();
	    		colMap.put("EDI_INP_D", 8);
	    		colMap.put("EDI_INP_T", 6);
	    		colMap.put("HAWB", 30);
	    		colMap.put("MSG_CTRL_ID", 14);
	    		colMap.put("SEND_COMP", 100);
	    		colMap.put("SMI", 4);
	    		colMap.put("SRC_PIMA", 35);
	    		colMap.put("TGT_PIMA", 2);
	    		colMap.put("VERSION", 3);
	    		for (String colKey : colMap.keySet()) {
	    			if (CommonUtil.nullChk(fhlmap.get(colKey)).length() > colMap.get(colKey)) {
	    				String colName = new String(colKey);
	    				switch (colName) {
	    					case "EDI_INP_D": colName = "HEADER INPUT DATE"; break;
	    					case "EDI_INP_T": colName = "HEADER INPUT TIME"; break;
	    					case "HAWB": colName = "HEADER HAWB NO"; break;
	    					case "MSG_CTRL_ID": colName = "HEADER MESSAGE CONTROL ID"; break;
	    					case "SEND_COMP": colName = "HEADER SEND COMPANY"; break;
	    					case "SMI": colName = "HEADER SMI"; break;
	    					case "SRC_PIMA": colName = "HEADER AGENT PIMA"; break;
	    					case "TGT_PIMA": colName = "HEADER AIRLINE CODE"; break;
	    					case "VERSION": colName = "HEADER VERSION"; break;
	    				}
		    			lengthMsg = colName + " EXCEEDS " + String.valueOf(colMap.get(colKey)) + " BYTES";
	    				
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", lengthMsg);
							fhlmap.put("ERRORMSG_KOR", colName + " 항목 길이가 " + String.valueOf(colMap.get(colKey)) + " 자를 초과함");
						}
	    				flag = false;
	    				
//	    				String substrVal = CommonUtil.nullChk(fhlmap.get(colKey)).substring(0, colMap.get(colKey));
	    				fhlmap.put(colKey, "");
	    			}
	    		}
	    		
	    		// TM_TRACE 컬럼 길이 초과값 존재
	    		if (!flag) {
	    			
					//FNA 생성
			    	fnaMsg = createack.FNAack(fhlmap);
			    	fhlmap.put("AMS_ACK", fnaMsg);
			    	fhlmap.put("AMS_SMI", "FNA");
			    	
					HashMap logMap = new HashMap();
					logMap.put("HISTORY_STATUS", lengthMsg);
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
					logMap.put("AMS_STEP", "209");
					logMap.put("IKAMS_STEP", "209");
					
					logger.info(" === Header Check ERROR === ");
					logger.info(" === " + lengthMsg + " === ");
					commonSql.insertHistory(logMap);
					commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
					commonSql.AckupdateTrace(logMap, fhlmap);
	    		} else {
	    			
	    			//EDI HISTORY INSERT[S]
	    			HashMap logMap = new HashMap();
	    			logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
	    			logMap.put("AMS_STEP", AMS_STEP);
	    			logMap.put("IKAMS_STEP", IKAMS_STEP);
	    			logMap.put("HISTORY_STATUS", "Header Check Success");
	    			
	    			commonSql.insertHistory(logMap);
	    			commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
	    			//EDI HISTORY INSERT[E]
	    			logger.info(" === Header Check Success === ");
	    		}
	    	}

	    	exchange.getIn().setHeader("CHECK_FLAG",Boolean.valueOf(flag));
	    	exchange.getIn().setHeader("HEADER_CHECK_FLAG",Boolean.valueOf(flag));

	    	logger.info(" === Header Check End === ");
	    	try{
	    		sqlMapClient.commitTransaction();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}finally{
	    		sqlMapClient.endTransaction();
	    	}
	    }
		return msg;
	}

	public String KtnetmainStart(@Header("KTNET_ROUTE") String ktnet_reverse_key, @Body String msg, Exchange exchange) throws Exception {
		HashMap<String,Object> ktmap = new HashMap<String,Object>();
		try{
			sqlMapClient.startTransaction();
			logger.info(" === KTNET IMP MESSAGE PARSER START === ");

			ktmap = KTgetMessage(ktnet_reverse_key, msg);
			exchange.getIn().setHeader("KTNET_EDI", ktmap);
			//EDI HISTORY INSERT[S]
			HashMap logMap = new HashMap();
			logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
			//logMap.put("MSB_STATUS", "Header Check");

			//commonSql.insertHistory(logMap);
			//EDI HISTORY INSERT[E]
			logger.info(" === KTNET IMP MESSAGE PARSER Success === ");

		}catch(Exception e){
			logger.info(" === KTNET IMP MESSAGE PARSER Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);

	    	String errorCode = new String();
	    	String errorMsg = new String();
	    	if (CommonUtil.isExit(e.toString(), ":::")) {
				errorCode = e.toString().substring(e.toString().indexOf(":::")-4, e.toString().indexOf(":::"));
				errorMsg = e.toString().substring(e.toString().indexOf(":::")+3);
			}else {
				errorCode = "9999";
				errorMsg = e.toString();
			}
	    	errorMsg = CommonUtil.nullChk(errorMsg,"");
	    	//System.out.println("errorCode="+errorCode);
	    	//System.out.println("errorMsg="+errorMsg);
	    	//EDI HISTORY INSERT[S]
			HashMap logMap = new HashMap();
			logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
			//logMap.put("MSB_STATUS", "Header Check ERROR");
			//logMap.put("MSB_STEP", msb_step);
			//commonSql.insertHistory(logMap);
			//EDI HISTORY INSERT[E]
			//throw e;
	    }finally{
	    	logger.info(" === KTNET IMP MESSAGE PARSER End === ");
	    	try{
	    		sqlMapClient.commitTransaction();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}finally{
	    		sqlMapClient.endTransaction();
	    	}
	    }
		return msg;
	}

	/**
	 * 파일리스트 정렬
	 *
	 * @param files
	 * @return
	 */
	public ArrayList<String> sortFileList(File[] files) {
		ArrayList<String> arrayList = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			arrayList.add(files[i].getName());
			// System.out.println(arrayList.get(i));
		}
		Collections.sort(arrayList);// 정렬

		return arrayList;
	}

	// /////////////////////////////////////////////
	// EDI 파일 HEADER FORMAT CHECK
	// /////////////////////////////////////////////
	public boolean getMessage(String source) throws ValidationException {
		String header = "";
		String body = "";
		String footer = "";
		String version = "";
		String accessRef = "";
		String edifact = "";
		boolean chkFlag = false;
		synchronized (this) {
			if (source.startsWith("UNB")) {
				source = "UNA:+.? '" + source;
			}

			//logger.info("Origin EDI==>[\r\n"+source+"\r\n]");

			String str = null;
			StringTokenizer token = new StringTokenizer(source, "'");
			while (token.hasMoreTokens()) {
				str = token.nextToken();
				if (str.startsWith("UNA"))
					header += str;
				else if (str.startsWith("UNB")) {
					header += "'" + str;

				} else if (str.startsWith("UNH")) {
					header += "'" + str;
					String[] items = str.split("\\+");
					if (items != null && items.length > 2) {
						version = items[2].split(":")[1];
					}
					if (items != null && items.length > 3) {
						accessRef = items[3];
					}
				} else if (str.startsWith("UNT"))
					footer += "'" + str; // Message Trailer
				// else if (str.startsWith("UNE")) footer += "'" + str; //
				// Functional Group Trailer (not supported by TRAXON)
				else if (str.startsWith("UNZ")) {
					footer += "'" + str + "'"; // Interchange Trailer
				} else
					body += "'" + str;
			}
			// logger.debug(body);
			if (body.endsWith("'\r\n"))
				body = body.substring(0, body.length() - "'\r\n".length());
			edifact = header + body.replace('\n', '+') + footer;

			//logger.info("Header Data= \n[" + header + "]");
			//logger.info("Transformed EDIFACT Data================" + edifact + "=============");
			chkFlag = validate(edifact);
			if (chkFlag == false || source.length() == 0) {
				throw new ValidationException(
						//mxr.getProperty("validation.message.header.exception"));
						mxr.getProperty("validation.edifact.exception"));
			}
			return chkFlag;
		}

	}

	// /////////////////////////////////////////////
	// KTNET EDI 파일 HEADER FORMAT CHECK
	//producer.sendBodyAndHeaders("direct:mq_send",msg, map);
	//@Produce
	//ProducerTemplate producer;
	// /////////////////////////////////////////////

	public HashMap<String, Object> KTgetMessage(String ktnet_reverse_key, String source) throws Exception {
		HashMap<String,Object> map = new HashMap<String,Object>();

		try {
			String[] ktnet = source.split("UNB\\+");
			map.put("KTNET_ROUTE", ktnet_reverse_key);

			for(int i=0; i<ktnet.length; i++){
	    		if(ktnet[i].length()>0){
		    		byte[] bytes = (CommonUtil.nullChk("UNB+"+ktnet[i].toString())).getBytes("UTF-8");
		    		map.put("DIVISION_MSG", bytes);
		    		int result = 0;
			    	result = commonSql.insertKTTrace(map);
	    		}
			}
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;
	}

	protected boolean validate(String edifact) {
		boolean isValid = false;
		String name = "";
		synchronized (this) {
			CargoValidator validator = new CargoValidator(new StringReader(
					edifact));
			isValid = validator.validate();

			//logger.info("validate수행후 결과는:" + isValid);
		}
		return isValid;
	}


	// /////////////////////////////////////////////
	// FHL String을 HashMap으로 변환
	// /////////////////////////////////////////////
	public static HashMap<String, Object> getFHLInfoString(String msg){
		HashMap<String,Object> map = new HashMap<String,Object>();
		String[] fhlinfoDt = null;
		String body = "";

		msg = msg.replaceAll("UNA:\\+.\\? '", "");
		msg = msg.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("<br>", "\r\n");

		String[] arrFHL = msg.split("\r\n");
		StringBuffer strBuf =  new StringBuffer();
		if (msg.length() > 0) {
				String[] header_check;
				String[] header_check_unh;
				int lineNum = 0;
				int ackLineNum = 0;
				for(int i=0;i<arrFHL.length;i++){
					lineNum++;
					String s = "";
					s = arrFHL[i];
					s = s.replace("\r", "");
			    	s = s.replace("\n", "");
					if (s.indexOf("UNB+") != -1) {

						header_check = s.split("'");
						if(header_check.length > 1){
							header_check_unh = header_check[1].split("\\+");
							if(header_check_unh.length > 3){
								map.put("REFERENCE_NUMBER", header_check_unh[3]);
							}
						}
						if(header_check.length > 2){
							header_check = header_check[2].split("/");
						}
						if(header_check.length > 0){
							map.put("SMI", header_check[0]);
						}
						if(header_check.length > 1){
							map.put("VERSION", header_check[1]);
						}else{
							map.put("VERSION", "0");
						}

						map.put("HEADER", s);
						String[] arrHeader = null;
						String[] arrHeader2 = null;
						String srcPima = "";
						String tgtPima = "";
						String msg_ctrl_id ="";
						String ediInpD = "";
						String ediInpT = "";
						String[] arrEdiInpTime = null;
						arrHeader = s.split("\\+");
						arrHeader2 = arrHeader[2].split(":");
						if(arrHeader.length > 2){
							srcPima = arrHeader[2].substring(0,arrHeader[2].indexOf(":PIMA"));
							if(arrHeader2.length > 2){
								msg_ctrl_id = arrHeader2[2];
							}
						}
						if(arrHeader.length > 3){
							tgtPima = arrHeader[3].substring(0,arrHeader[3].indexOf(":PIMA"));
						}

					    map.put("SRC_PIMA", srcPima);
					    map.put("TGT_PIMA", tgtPima);
					    map.put("MSG_CTRL_ID", msg_ctrl_id);

					    if(arrHeader.length > 4){
					    	arrEdiInpTime = arrHeader[4].split(":");
					    	ediInpD = arrEdiInpTime[0];
						    ediInpT = arrEdiInpTime[1];
					    }
					    map.put("EDI_INP_D", ediInpD);
					    map.put("EDI_INP_T", ediInpT);
					    if(arrHeader.length > 5){
					    	map.put("SEND_COMP", arrHeader[5]);
					    }
					    if(arrHeader.length > 7){
					    	map.put("CSTM_CODE", arrHeader[7]);
					    	if(map.get("CSTM_CODE").toString().length() < 7){

					    		if(map.get("CSTM_CODE").toString().length() == 6){
					    			map.put("CSTM_COD_C_3", arrHeader[7].substring(3,6));
					    		}else{
					    			map.put("CSTM_COD_C_3", "XXX");
					    		}
					    	}else{
					    		map.put("CSTM_COD_C_3", "XXX");
					    	}
					    }


					} else if (s.startsWith("MBI/")) {
						map.put("MBI", s);
						String[] arrMawb =null;
						arrMawb = s.split("/");
						String mawb= "";
						if(map.get("SMI").equals("FHL") || s.indexOf("MBI/") != -1){
							if(arrMawb.length > 1){
								if(arrMawb[1].length() > 11){
									mawb = arrMawb[1].substring(0,12);
								}
							}
						}
						map.put("MAWB", mawb.replaceAll("-", ""));
						map.put("MAWB_NO", mawb);
					} else if (s.startsWith("HBS/")) {
						if(s.indexOf("HBS/") != -1){
							map.put("HBS", s);
							String[] arrHawb =null;
							arrHawb = s.split("/");
							if(arrHawb.length > 0){
								map.put("HAWB", arrHawb[1]);
							}
						}

					}

					/*if(s.indexOf("/SHP/T/") !=-1){
						for(int oci=i; oci<arrFHL[i].length(); oci++){
							if(arrFHL[oci].indexOf("/CNE/T/") !=-1 || arrFHL[oci].startsWith("SHP/")){
								break;
							}else{
								System.out.println("SHP : "+ arrFHL[oci].toString());
							}
						}
					}else if(s.indexOf("/CNE/T/") !=-1){
						for(int oci=i; oci<arrFHL[i].length(); oci++){
							if(arrFHL[oci].startsWith("SHP/")){
								break;
							}else{
								System.out.println("CNE : " + arrFHL[oci].toString());
							}
						}
					}else{
						strBuf.append(s+"\r\n");
					}*/
					strBuf.append(s+"\r\n");
				}

				map.put("EDI", strBuf.toString());
				String edi = map.get("EDI").toString();

				if (edi.endsWith("'\r\n")){
					edi = edi.substring(0, edi.length() - "'\r\n".length());
					map.put("EDI", edi+"'");
				}
				/**
				 * BODY 추출 = FHL/4 MBI/057-25088906ICNSOF/T1K158.0
				 * HBS/14KS69APR29/ICNSOF/1/K158.0//BANKNOTE SORTER
				 * TXT/.COM.BANKNOTE SORTER UNITS /.FLT.20140429-AF267-WOOJ--
				 * OCI/KR/EXP/M/E001-010151402522630-- /KR/EXP/M/S001-1-158.0-N-
				 * SHP/KISAN ELECTRONICS CO. LTD. /273-1 SOUNG SOO 2GA SOUNG
				 * DONG GU /SEOUL /KR/133 120 CNE/ET HRISTO TSONKOVSKI
				 * /HRIZANTEMA STR 20A /SOFIA /BG/1612 CVD/KRW/PP/NVD/NCV/XXX
				 **/
				fhlinfoDt = map.get("EDI").toString().split("'");
				if(fhlinfoDt.length > 2){
					body = fhlinfoDt[2];
				}

				map.put("BODY", body);
				//KTNET MESSAGE SIZE CHECK
				//줄바꿈 제거 하여 체크
				body = body.replaceAll("\r\n", "").replaceAll("\r", "").replaceAll("\n", "").replaceAll("<br>", "");
				/*
				String[] arrBody = null;
				arrBody = body.split("\r\n");
				StringBuffer strBufBody = new StringBuffer();
				for(int i=0;i<arrBody.length;i++){
					strBufBody.append(arrBody[i]);
				}
				**/
				map.put("KT_MSG_SIZE", body.length());

				if(body.indexOf("OCI/KR/EXP/M/") != -1){
					map.put("OCI_TYPE", "EXP");
					map.put("OCI_FLAG", "M");
				}else if(body.indexOf("OCI/KR/EXP/I/") != -1){
					map.put("OCI_TYPE", "EXP");
					map.put("OCI_FLAG", "I");
				}else if(body.indexOf("OCI/KR/EXP/A/") != -1){
					map.put("OCI_TYPE", "EXP");
					map.put("OCI_FLAG", "A");
				}else if(body.indexOf("OCI/KR/EXP/E/") != -1){
					map.put("OCI_TYPE", "EXP");
					map.put("OCI_FLAG", "E");
				}else if(body.indexOf("OCI/KR/EXP/X/") != -1){
					map.put("OCI_TYPE", "EXP");
					map.put("OCI_FLAG", "X");
				}else if(body.indexOf("OCI/KR/IMP/M/") != -1){
					map.put("OCI_TYPE", "IMP");
					map.put("OCI_FLAG", "M");
				}else if(body.indexOf("OCI/KR/IMP/I/") != -1){
					map.put("OCI_TYPE", "IMP");
					map.put("OCI_FLAG", "I");
				}

		}


		return map;
	}

	public String smiCheck(@Body String msg, Exchange exchange) throws Exception {
		HashMap<String,Object> map = new HashMap<String,Object>();

		try{
			msg = msg.replaceAll("UNA:\\+.\\? '", "");
			msg = msg.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("<br>", "\r\n");

			String[] arrFHL = msg.split("\r\n");
			if (msg.length() > 0) {
				String[] header_check;
				for(int i=0;i<arrFHL.length;i++){
					String s = "";
					s = arrFHL[i];
					s = s.replace("\r", "");
			    	s = s.replace("\n", "");
					if (s.indexOf("UNB+") != -1) {

						header_check = s.split("'");
						if(header_check.length > 2){
							header_check = header_check[2].split("/");
						}
						if(header_check.length > 0){
							map.put("HEADER_SMI", header_check[0]);
						}
						if(header_check.length > 1){
							map.put("HEADER_VERSION", header_check[1]);
						}else{
							map.put("HEADER_VERSION", "0");
						}
					}
				}
				exchange.getIn().setHeader("HEADER_SMI",map.get("HEADER_SMI"));
			}

		}catch(IndexOutOfBoundsException e){
			logger.info(" === Header SMI Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
		}catch(NullPointerException e){
			logger.info(" === Header SMI Check GET NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
		}catch (Exception e) {
			logger.info(" === Header SMI Check GET ERROR === ");
			logger.info("ERROR==> " + e.toString());
			e.printStackTrace();
		}

		return msg;
	}

}
