package com.trx.validate;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.bind.ValidationException;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.trx.Msgprocess.CreateAck;
import com.trx.evalleyvs.eai.cargo.CargoValidator;
import com.trx.evalleyvs.eai.message.MessageXMLResourceManager;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;

public class AckHeaderCheck {

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


	final static String AMS_STEP = "800";

	static MessageXMLResourceManager mxr = MessageXMLResourceManager.getInstance("MessageResource.xml");

	public String ackheaderCheck(@Body String msg, Exchange exchange) throws Exception {
		boolean flag = false;
		boolean air_flag = false;
		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		String carr_code;
		String fnaMsg;

		try{

			sqlMapClient.startTransaction();
			logger.info(" === ACK Header Check START === ");


			flag = getMessage(msg);

			fhlmap = getACKInfoString(msg);
			exchange.getIn().setHeader("ACK_PARSE", fhlmap);

			//carr_code= (String) fhlmap.get("TGT_PIMA");

		}catch(IndexOutOfBoundsException e){
			logger.info(" === ACK Header Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(fhlmap.get("MSG_CTRL_ID").toString(), "AMS-MAIN","ACK Header Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === ACK Header Check NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(fhlmap.get("MSG_CTRL_ID").toString(), "AMS-MAIN","ACK Header Check NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === ACK Header Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(fhlmap.get("MSG_CTRL_ID").toString(), "AMS-MAIN","ACK Header Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === ACK Header Check Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	mainExceptionManager.process(fhlmap.get("MSG_CTRL_ID").toString(), "AMS-MAIN","ACK Header Check Exception",e, msg);

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
	    	fhlmap.put("ERRORMSG", "ACK HEADER CHECK ERROR");

	    	//EDI HISTORY INSERT[S]
			HashMap logMap = new HashMap();
			//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
			logMap.put("AMS_TRX_ROUTE", fhlmap.get("MSG_CTRL_ID"));
			logMap.put("AMS_STEP", AMS_STEP);
			logMap.put("HISTORY_STATUS", "ACK Header Check Error");
			commonSql.insertHistory(logMap);
			//EDI HISTORY INSERT[E]
	    	flag = false;
			//throw e;
	    }finally{


	    	//EDI HISTORY INSERT[S]
			HashMap logMap = new HashMap();

			logMap.put("AMS_TRX_ROUTE", fhlmap.get("MSG_CTRL_ID"));
			logMap.put("AMS_STEP", AMS_STEP);
			logMap.put("HISTORY_STATUS", "ACK Header Check");
			commonSql.insertHistory(logMap);
			//EDI HISTORY INSERT[E]
			logger.info(" === ACK Header Check Success === ");


	    	exchange.getIn().setHeader("ACK CHECK_FLAG",Boolean.valueOf(flag));
	    	exchange.getIn().setHeader("ACK HEADER_CHECK_FLAG",Boolean.valueOf(flag));

	    	/*byte[] bytes = msg.getBytes("UTF-8");

			fhlmap.put("MSB_TRX_ROUTE", msb_out_reverse_key);
			fhlmap.put("ORG_MSG", bytes);
	    	int result = 0;
	    	result = commonSql.insertTrace(exchange, fhlmap);*/
	    	logger.info(" === ACK Header Check End === ");
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
	public static HashMap<String, Object> getACKInfoString(String msg){
		HashMap<String,Object> map = new HashMap<String,Object>();
		String[] fhlinfoDt = null;
		String body = "";
		msg = msg.replaceAll("UNA:\\+.\\? '", "");
		msg = msg.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("<br>", "\r\n");
		/**
		StringBuffer strMsg = new StringBuffer();
		StringTokenizer st = new StringTokenizer(msg, "\r\n");
		while (st.hasMoreElements())
		{
			String token = st.nextElement().toString();
			strMsg.append(token+"\r\n");
		}
		System.out.println("strMsg="+strMsg);
		msg = strMsg.toString();
		**/
		String[] arrFHL = msg.split("\r\n");
		StringBuffer strBuf =  new StringBuffer();
		StringBuffer ackBuf = new StringBuffer();

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
						arrHeader2 = arrHeader[3].split(":");
						if(arrHeader.length > 2){
							srcPima = arrHeader[2].substring(0,arrHeader[2].indexOf(":PIMA"));
						}
						if(arrHeader.length > 3){
							tgtPima = arrHeader[3].substring(0,arrHeader[3].indexOf(":PIMA"));

							if(arrHeader2.length >= 3){
								map.put("MSG_CTRL_ID", arrHeader2[2]);
							}else{
								map.put("MSG_CTRL_ID", "TRAXONRE");
							}

						}
					    map.put("SRC_PIMA", srcPima);
					    map.put("TGT_PIMA", tgtPima);

					    if(arrHeader.length > 4){
					    	arrEdiInpTime = arrHeader[4].split(":");
					    	ediInpD = arrEdiInpTime[0];
						    ediInpT = arrEdiInpTime[1];
					    }
					    map.put("EDI_INP_D", ediInpD);
					    map.put("EDI_INP_T", ediInpT);
					}else if(s.indexOf("ACK/") !=-1){
						map.put("ACK_LINE", s);

						if(map.get("SMI").equals("FNA")){

							for(int a = i; a < arrFHL.length; a++){
								if(arrFHL[a].startsWith("MBI/") || arrFHL[a].startsWith("FHL/")){
									break;
								}else{
									ackBuf.append(arrFHL[a]);
									map.put("ACK_DESCRIPTION", ackBuf.toString());
								}
							}
						}

						String[] arrHawb =map.get("ACK_LINE").toString().split("\\.");
							if(arrHawb.length > 1){
								map.put("HAWB", arrHawb[1]);
							}

					}else if (s.startsWith("MBI/")
									|| (lineNum == 2
								    && !map.get("SMI").equals("FMA")
									&& !map.get("SMI").equals("FNA"))) {
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
						map.put("MAWB", mawb);
						map.put("MAWB_NO", mawb.replaceAll("-", ""));
					}else if(s.startsWith("HBS/")){
							map.put("HBS", s);
							String[] arrHawb =null;
							arrHawb = s.split("/");
							if(arrHawb.length > 0){
								map.put("HAWB", arrHawb[1]);
							}


					} else if (s.indexOf("FHL/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))) {
						String[] arrOriginSmi =null;
						arrOriginSmi = s.split("/");
						if(arrOriginSmi.length > 0){
							map.put("ORIGIN_SMI", arrOriginSmi[0]);
						}
						ackLineNum = lineNum+1;
					} else if (lineNum == ackLineNum){
						if(s.indexOf("-") != -1){
							map.put("MBI", s);
							String[] arrMawb =null;
							arrMawb = s.split("/");
							String mawb= "";
							if(arrMawb.length > 0){
								if(arrMawb[0].length() > 11){
									mawb = arrMawb[0].substring(0,12);
								}
							}
							map.put("MAWB", mawb.replaceAll("-", ""));
						}
					}  else if (s.indexOf("FVR/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							 || s.indexOf("FVR") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							 || s.indexOf("FVA/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							 || s.indexOf("FVA") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))) {
						String[] arrOriginSmi =null;
						arrOriginSmi = s.split("/");
						if(arrOriginSmi.length > 0){
							map.put("ORIGIN_SMI", arrOriginSmi[0]);
						}
					}
					strBuf.append(s+"\r\n");
				}


				map.put("ACK_EDI", strBuf.toString());

				/**
				 * BODY 추출 = FHL/4 MBI/057-25088906ICNSOF/T1K158.0
				 * HBS/14KS69APR29/ICNSOF/1/K158.0//BANKNOTE SORTER
				 * TXT/.COM.BANKNOTE SORTER UNITS /.FLT.20140429-AF267-WOOJ--
				 * OCI/KR/EXP/M/E001-010151402522630-- /KR/EXP/M/S001-1-158.0-N-
				 * SHP/KISAN ELECTRONICS CO. LTD. /273-1 SOUNG SOO 2GA SOUNG
				 * DONG GU /SEOUL /KR/133 120 CNE/ET HRISTO TSONKOVSKI
				 * /HRIZANTEMA STR 20A /SOFIA /BG/1612 CVD/KRW/PP/NVD/NCV/XXX
				 **/
				fhlinfoDt = map.get("ACK_EDI").toString().split("'");
				if(fhlinfoDt.length > 2){
					body = fhlinfoDt[2];
				}
				map.put("BODY", body);

		}

		return map;
	}
}
