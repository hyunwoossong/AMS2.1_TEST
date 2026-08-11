package com.trx.Msgprocess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.trx.evalleyvs.eai.exception.SystemException;
import com.trx.evalleyvs.eai.message.MessageXMLResourceManager;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;

public class CreateAck {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateAck.class);
	static MessageXMLResourceManager mxr = MessageXMLResourceManager.getInstance("MessageResource.xml");

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	SqlMapClient sqlMapClient_msb;

	@Autowired
	CommonSql commonSql;

	@Autowired
	CommonUtil commonUtil;

	@Produce
	ProducerTemplate producer;

	@Autowired
	MainExceptionManager mainExceptionManager;

	@Value("#{prop['ack.write.path']}")
	//@Value("${ktnet.write.path}")
	private String write_path;

	final static String AMS_STEP = "999";
	final static String IKAMS_STEP = "999";

	/**
	 * CREAT FMA MESSAGE
	 */
	public String FMAack(HashMap<String, Object> fhlmap) throws Exception, SystemException{
		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		String ams_out_reverse_key = fhlmap.get("AMS_TRX_ROUTE").toString();
		String msg = fhlmap.get("EDI").toString();
		String ranchar = commonUtil.randomchar();
		String fileName ="";
		String traxon_reverse = "";
		if(CommonUtil.nullChk(CommonUtil.nullChk(fhlmap.get("IKAMS_SMI"))).equals("FMA")){
			fileName = "IKAMS_FMA_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
			traxon_reverse = "RKRCCS88TRAXON";
		}else{
			fileName = "AMS_FMA_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
			traxon_reverse = "RKRCCS77TRAXON";
		}

		// [UNB, IATA:1, RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA, AF:PIMA,
		// 140428:1820, AIRCISFHL, 0]
		String header = (String) fhlmap.get("HEADER");
		String[] header_check;
		String[] unh_check;
		header_check = header.split("'");
		// UNH+SELWOO+CIMFHL:4+SELWOO
		unh_check = header_check[1].split("\\+");
		header_check = header_check[0].split("\\+");

		// MBI/057-25088906ICNSOF/T1K158.0
		String full_mbi = (String) fhlmap.get("MBI");
		String mbi = (String) fhlmap.get("MBI_NO");

		// HBS/14KS69APR29/ICNSOF/1/K158.0//BANKNOTE SORTER
		String hbs = CommonUtil.nullChk(fhlmap.get("HBS_NO"));

		String smi = (String) fhlmap.get("SMI");
		String version = (String) fhlmap.get("VERSION");
		boolean flag = true;
		try{
			/**
			 * FMA ACK 생성(파일명 : ) UNA:+.?
			 * 'UNB+IATA:1+RKRCCS88GLSKR:PIMA+RKRAGT82AMSHNF01/SEL01:PIMA:XE96ZUIO6874+140410:0836+KTNETIMP+0'UNH+HFI07140039+CIMFMA:0+8319'FM
			 * A ACK/MESSAGE SENT OK -18060749824.HFI07140039 FHL/4
			 * MBI/180-60749824SFOICN/T13K393.0
			 * 'UNT+3+HFI07140039'UNZ+1+KTNETIMP'
			 */
			sb.append("UNA:+.? 'UNB+IATA:1+" + traxon_reverse + ":PIMA+"
					+ header_check[2] + "+" + YYMMddFormat.format(now) + ":"
					+ hhmmFormat.format(now) + "+" + header_check[5]
					+ "+0'UNH+" + unh_check[1] + "+CIMFMA:0+" + CommonUtil.nvl(fhlmap.get("REFERENCE_NUMBER"), "0000") + "'FMA");
			// 0000 은 REFERENCE_NUMBER 값.
			sb.append("\r\n");
			if(CommonUtil.nullChk(fhlmap.get("IKAMS_SMI")).equals("FMA")){
				if(CommonUtil.nullChk(fhlmap.get("KTNET_SEND_FLAG")).equals("KTNETIMP") || CommonUtil.nullChk(fhlmap.get("KAMS_KTNET_MANIFEST")).equals("KTMANIFEST")){
					sb.append("ACK/MFCS MESSAGE SENT OK -" + mbi.replaceAll("-", "") + "."
						+ hbs);
				}else if(CommonUtil.nullChk(fhlmap.get("KAMS_KCNET_MANIFEST")).equals("KCMANIFEST")){
					sb.append("ACK/KCNET MESSAGE SENT OK -" + mbi.replaceAll("-", "") + "."
							+ hbs);
				}else{
					sb.append("ACK/IKAMS MESSAGE SENT OK -" + mbi.replaceAll("-", "") + "."
							+ hbs);
				}
			}else{
				sb.append("ACK/MESSAGE SENT OK -" + mbi.replaceAll("-", "") + "."
						+ hbs);
			}
			sb.append("\r\n");
			sb.append(smi + "/" + version);
			sb.append("\r\n");
			sb.append(full_mbi);
			sb.append("\r\n");
			sb.append("'UNT+3+" + unh_check[1] + "'UNZ+1+" + header_check[5]
					+ "'");
		}catch(Exception e){
			if(CommonUtil.nullChk(CommonUtil.nullChk(fhlmap.get("IKAMS_SMI"))).equals("FMA")){
				logger.info(" === IKAMS ACK CREATE ERROR === \r\n", e);
				logger.error("ERROR==>"+e.toString(), e);
				mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "IKAMS ACK Create Exception", e, msg);
				flag = false;
			}else{
				logger.info(" === AMS ACK CREATE ERROR === \r\n", e);
				logger.error("ERROR==>"+e.toString(), e);
				mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "AMS ACK Create Exception", e, msg);
				flag = false;
			}
		}finally{
			if(!flag){
				//exceptin table로 insert
				if(CommonUtil.nullChk(CommonUtil.nullChk(fhlmap.get("IKAMS_SMI"))).equals("FMA")){
					HashMap logMap = new HashMap();
					logMap.put("HISTORY_STATUS", "AMS ACK CREATE");
					logMap.put("AMS_TRX_ROUTE", fhlmap.get("AMS_TRX_ROUTE"));
					logMap.put("AMS_STEP", AMS_STEP);
					commonSql.insertHistory(logMap);
				}else{
					HashMap logMap = new HashMap();
					logMap.put("HISTORY_STATUS", "IKAMS ACK CREATE");
					logMap.put("AMS_TRX_ROUTE", fhlmap.get("AMS_TRX_ROUTE"));
					logMap.put("AMS_STEP", IKAMS_STEP);
					commonSql.insertHistory(logMap);
				}

			}else{
				HashMap ackMap = new HashMap();
				ackMap.put("ACKFILE", fileName);
				producer.sendBodyAndHeaders("direct:AMSACKqueuesend",sb.toString(), ackMap);
			}
		}

		return sb.toString();
	}

	/**
	 * CREATE FNA MESSAGE
	 */
	public String FNAack(HashMap<String, Object> fhlmap) throws Exception, SystemException{

		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		String ams_out_reverse_key = fhlmap.get("AMS_TRX_ROUTE").toString();
		String msg = fhlmap.get("EDI").toString();
		String returnMsg = "";
		String fileName = "";
		String traxon_reverse = "";
		// [UNB, IATA:1, RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA, AF:PIMA,
		// 140428:1820, AIRCISFHL, 0]
		String ranchar = commonUtil.randomchar();
		if(CommonUtil.nullChk(fhlmap.get("IKAMS_SMI")).equals("FNA")){
			fileName = "IKAMS_FNA_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
			traxon_reverse = "RKRCCS88TRAXON";
		}else{
			fileName = "AMS_FNA_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
			traxon_reverse = "RKRCCS77TRAXON";
		}

		String orgin_smi = "";
		orgin_smi = CommonUtil.nullChk(fhlmap.get("ORIGIN_SMI"));
		String header = (String) fhlmap.get("HEADER");
		String[] header_check;
		String[] unh_check;
		header_check = header.split("'");
		// UNH+SELWOO+CIMFHL:4+SELWOO
		unh_check = header_check[1].split("\\+");
		header_check = header_check[0].split("\\+");

		// FHL/4,FWB/16
		String smi = (String) fhlmap.get("SMI");
		String version = (String) fhlmap.get("VERSION");

		// MBI/057-25088906ICNSOF/T1K158.0
		String full_mbi = CommonUtil.nullChk(fhlmap.get("MBI"));
		String mbi = CommonUtil.nullChk(fhlmap.get("MBI"));
		String[] mbi_check;
		mbi_check = mbi.split("/");
		if (smi.equals("FHL") || orgin_smi.equals("FHL")) {
			if(mbi_check[1].length() > 11){
				mbi = mbi_check[1].substring(0, 12);
			}
		} else if (smi.equals("FVR")){
			mbi = "";
		}else{
			if(mbi_check[0].length() > 11){
				mbi = mbi_check[0].substring(0, 12);
			}
		}

		// HBS/14KS69APR29/ICNSOF/1/K158.0//BANKNOTE SORTER
		String hbs = CommonUtil.nullChk(fhlmap);
		String[] hbs_check;
		hbs_check = hbs.split("/");
		if (smi.equals("FHL")) {
			hbs = hbs_check[1];
		}

		/**
		 * FNA ACK 생성 UNA:+.?
		 * 'UNB+IATA:1+RKRCCS88GLSKR:PIMA+RHKAIR01CHICPXH:PIMA:1+140818:0934+0000+0'UNH+0000+CIMFNA:0'FN
		 * A ACK/RKRAGT82EGISAIR - UNKNOWN PARTICIPANT IDENTIFIER FHL/4
		 * 180-45449961ICNLIM/T1K209
		 * BKD/UA1716/18AUG/SFOIAH/T1K209/E1350/E1942
		 * 'UNT+3+0000'UNZ+1+0000'
		 */
		StringBuffer sb = new StringBuffer();
		boolean flag = true;
		try{
			sb.append("UNA:+.? 'UNB+IATA:1+" + traxon_reverse + ":PIMA+"
					+ header_check[2] + "+" + YYMMddFormat.format(now) + ":"
					+ hhmmFormat.format(now) + "+" +  header_check[5]
					+ "+0'UNH+" + unh_check[1] + "+CIMFNA:0+" + CommonUtil.nvl(fhlmap.get("REFERENCE_NUMBER"), "0000") + "'FNA");
			sb.append("\r\n");
			if(CommonUtil.nullChk(fhlmap.get("IKAMS_SMI")).equals("FNA")){
				sb.append("ACK/" + fhlmap.get("IKAMS_ERRMSG"));
			}else{
				sb.append("ACK/" + fhlmap.get("ERRORMSG"));
			}
			sb.append("\r\n");
			sb.append(fhlmap.get("BODY"));
			sb.append("'UNT+3+" + unh_check[1] + "'UNZ+1+" + header_check[5] + "'");
			//sb.append("'UNT+3+0000" + "'UNZ+1+0000'");

			returnMsg = sb.toString();

		}catch(Exception e){
			if(CommonUtil.nullChk(fhlmap.get("IKAMS_SMI")).equals("FNA")){
				logger.info(" === IKAMS ACK CREATE ERROR === \r\n", e);
				logger.error("ERROR==>"+e.toString(), e);
				mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "IKAMS ACK Create Exception", e, msg);
				flag = false;
			}else{
				logger.info(" === AMS ACK CREATE ERROR === \r\n", e);
				logger.error("ERROR==>"+e.toString(), e);
				mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "IKAMS ACK Create Exception", e, msg);
				flag = false;
			}
		}finally{
			if(!flag){
				if(CommonUtil.nullChk(fhlmap.get("IKAMS_SMI")).equals("FNA")){
					//exception table로 insert
					HashMap logMap = new HashMap();
					logMap.put("HISTORY_STATUS", "IKAMS ACK CREATE FAIL");
					logMap.put("AMS_TRX_ROUTE", fhlmap.get("AMS_TRX_ROUTE"));
					logMap.put("AMS_STEP", IKAMS_STEP);

					commonSql.insertHistory(logMap);
				}else{
					//exception table로 insert
					HashMap logMap = new HashMap();
					logMap.put("HISTORY_STATUS", "AMS ACK CREATE FAIL");
					logMap.put("AMS_TRX_ROUTE", fhlmap.get("AMS_TRX_ROUTE"));
					logMap.put("AMS_STEP", AMS_STEP);

					commonSql.insertHistory(logMap);
				}
			}else{
				HashMap ackMap = new HashMap();
				ackMap.put("ACKFILE", fileName);
				producer.sendBodyAndHeaders("direct:AMSACKqueuesend",sb.toString(), ackMap);
			}
		}
		return returnMsg;
	}

	// 미 연계 항공사 FNA
	public String NonService(HashMap<String, Object> fhlmap) throws Exception {

		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		String returnMsg = "";
		String ams_out_reverse_key = fhlmap.get("AMS_TRX_ROUTE").toString();
		String msg = fhlmap.get("EDI").toString();
		String ranchar = commonUtil.randomchar();
		String fileName = "AMS_FNA_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
		// [UNB, IATA:1, RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA, AF:PIMA,
		// 140428:1820, AIRCISFHL, 0]
		String orgin_smi = "";
		orgin_smi = CommonUtil.nullChk(fhlmap.get("ORIGIN_SMI"));
		String header = (String) fhlmap.get("HEADER");
		String[] header_check;
		String[] unh_check;
		header_check = header.split("'");
		// UNH+SELWOO+CIMFHL:4+SELWOO
		unh_check = header_check[1].split("\\+");
		header_check = header_check[0].split("\\+");

		// FHL/4,FWB/16
		String smi = (String) fhlmap.get("SMI");
		String version = (String) fhlmap.get("VERSION");

		// MBI/057-25088906ICNSOF/T1K158.0
		String full_mbi = CommonUtil.nullChk(fhlmap.get("MBI"));
		String mbi = CommonUtil.nullChk(fhlmap.get("MBI"));
		String[] mbi_check;
		mbi_check = mbi.split("/");
		if (smi.equals("FHL") || orgin_smi.equals("FHL")) {
			if(mbi_check[1].length() > 11){
				mbi = mbi_check[1].substring(0, 12);
			}
		} else if (smi.equals("FVR")){
			mbi = "";
		}else{
			if(mbi_check[0].length() > 11){
				mbi = mbi_check[0].substring(0, 12);
			}
		}

		// HBS/14KS69APR29/ICNSOF/1/K158.0//BANKNOTE SORTER
		String hbs = CommonUtil.nullChk(fhlmap.get("HBS"));
		String[] hbs_check;
		hbs_check = hbs.split("/");
		if (smi.equals("FHL")) {
			hbs = hbs_check[1];
		}

		/**
		 * FNA ACK 생성 UNA:+.?
		 * 'UNB+IATA:1+RKRCCS88GLSKR:PIMA+RHKAIR01CHICPXH:PIMA:1+140818:0934+0000+0'UNH+0000+CIMFNA:0'FN
		 * A ACK/RKRAGT82EGISAIR - UNKNOWN PARTICIPANT IDENTIFIER FHL/4
		 * 180-45449961ICNLIM/T1K209
		 * BKD/UA1716/18AUG/SFOIAH/T1K209/E1350/E1942
		 * 'UNT+3+0000'UNZ+1+0000'
		 */
		StringBuffer sb = new StringBuffer();
		boolean flag = true;
		try{
			sb.append("UNA:+.? 'UNB+IATA:1+" + "RKRCCS77TRAXON" + ":PIMA+"
					+ header_check[2] + "+" + YYMMddFormat.format(now) + ":"
					+ hhmmFormat.format(now) + "+" +  header_check[5]
					+ "+0'UNH+" + unh_check[1] + "+CIMFNA:0+" + CommonUtil.nvl(fhlmap.get("REFERENCE_NUMBER"), "0000") + "'FNA");
			sb.append("\r\n");
			sb.append("ACK/AMS SERVICE IS NOT AVAILABLE FOR THIS AIRLINE");
			sb.append("\r\n");
			sb.append(fhlmap.get("BODY"));
			sb.append("'UNT+3+" + unh_check[1] + "'UNZ+1+" + header_check[5] + "'");
			//sb.append("'UNT+3+0000" + "'UNZ+1+0000'");
			returnMsg = sb.toString();
		}catch(Exception e){
			logger.info(" === AMS ACK CREATE ERROR === \r\n", e);
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "AMS ACK Create Exception", e, msg);
			flag = false;
		}finally{
			if(!flag){
				//exception table로 insert
				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "AMS ACK CREATE");
				logMap.put("AMS_TRX_ROUTE", fhlmap.get("AMS_TRX_ROUTE"));
				logMap.put("AMS_STEP", AMS_STEP);

				commonSql.insertHistory(logMap);

			}else{
				HashMap ackMap = new HashMap();
				ackMap.put("ACKFILE", fileName);
				producer.sendBodyAndHeaders("direct:AMSACKqueuesend",sb.toString(), ackMap);
			}
		}
		return returnMsg;
	}

	/**
	 * MSB에서 내려온 ACK CREAT
	 */
	public HashMap<String, Object> Msb_Ack(HashMap<String, Object> fhlmap) throws Exception, SystemException{
		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		HashMap<String,Object> route_code = new HashMap<String,Object>();
		StringBuffer sb = new StringBuffer();
		String ranchar = commonUtil.randomchar();
		String fileName = "";
		String ams_in_reverse_key ="";
		String test = "";
		String replace_reverse_key = "";
		String ams_out_reverse_key = CommonUtil.nullChk(fhlmap.get("ORG_ROUTE")).toString();
		String msg = fhlmap.get("ACK_EDI").toString();
		ams_in_reverse_key = getOriginRouteReceive(CommonUtil.nullChk(fhlmap.get("HEADER")).toString());
		//ams_in_reverse_key = msg.substring(msg.indexOf(":PIMA"), msg.lastIndexOf(":PIMA"));
		if(ams_in_reverse_key.length() > 0){
			msg = msg.replace(ams_in_reverse_key, ams_out_reverse_key);
		}else{
			logger.info(" === ACK REVERSEKEY NONE ===");
		}

		//CX 항공사로 ACK 발생 시 EZC PIMA -> RKR로 변경
		if(fhlmap.get("TGT_PIMA").toString().startsWith("EZCAGT")){

			List result = sqlMapClient_msb.queryForList("TM_TRACE.ezcpimachange", fhlmap);

			if(result.size() > 0){

				for (int i = 0; i < result.size(); i++) {
					route_code = (HashMap) result.get(i);
					fhlmap.put("PIMA", (String) CommonUtil.nullChk(route_code.get("PIMA")));
				}

				msg = msg.replace(fhlmap.get("TGT_PIMA").toString(), fhlmap.get("PIMA").toString());
			}
		}
		// [UNB, IATA:1, RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA, AF:PIMA,
		// 140428:1820, AIRCISFHL, 0]

		boolean flag = true;

		try{
			if(ams_out_reverse_key.length() > 0){
				if (msg.startsWith("UNB")) {
					msg = "UNA:+.? '" + msg;
					if (msg.endsWith("'\r\n")){
						msg = msg.substring(0, msg.length() - "\r\n".length());
					}
				}
			}else{
				if(msg.lastIndexOf(":PIMA:") != -1){
					msg = msg.replace(":PIMA:", ":PIMA");
				}
				if (msg.startsWith("UNB")) {
					msg = "UNA:+.? '" + msg;
					if (msg.endsWith("'\r\n")){
						msg = msg.substring(0, msg.length() - "\r\n".length());
					}
				}
			}
			sb.append(msg);
			if(fhlmap.get("SRC_PIMA").equals("RKRCCS77TRAXON")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("AMS_SMI", "FMA");
				}else if(fhlmap.get("SMI").equals("FNA")){
					fhlmap.put("AMS_SMI", "FNA");
				}
			}
			if(fhlmap.get("SRC_PIMA").equals("RKRCCS88GLSKR") || fhlmap.get("SRC_PIMA").equals("RKRCCS77TRAXON")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("MSB_SMI", "FMA");
					fileName = "MSB_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("MSB_SMI", "FNA");
					fileName = "MSB_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("MSB_ACK", sb.toString());
				if(fhlmap.get("SRC_PIMA").equals("RKRCCS77TRAXON")){
					fhlmap.put("ACK_NAME", "AMS");
				}else{
					fhlmap.put("ACK_NAME", "MSB");
				}

			}else if(fhlmap.get("SRC_PIMA").equals("RKRAIR08KAL")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("AIR_SMI", "FMA");
					fileName = "KE_AIR_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("AIR_SMI", "FNA");
					fileName = "KE_AIR_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("KE_AIR_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "KE AIR");
			}else if(fhlmap.get("SRC_PIMA").equals("RKRCCS77KTNET")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("KT_ACK_STATUS", "KTNET_OK");
					fileName = "KTNET_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("KT_ACK_STATUS", "KTNET_FAIL");
					fileName = "KTNET_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("KTNET_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "KTNET");
			//OZ 항공사 ACK
			}else if(fhlmap.get("SRC_PIMA").equals("RKRCCS77AIR")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("KT_AIR_STATUS", "KT_AIR_FMA");
					fileName = "KT_AIR_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("KT_AIR_STATUS", "KT_AIR_FNA");
					fileName = "KT_AIR_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("KTNET_AIR_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "KTNET AIR");
			}else if(fhlmap.get("SRC_PIMA").equals("RKRCCS77MFCS")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("KT_MFCS_STATUS", "MFCS_OK");
					fileName = "KTNET_MFCS_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("KT_MFCS_STATUS", "MFCS_FAIL");
					fileName = "KTNET_MFCS_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("KTNET_MFCS_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "KTNET MFCS");
			}else if(fhlmap.get("SRC_PIMA").equals("RKRCCS77FHL")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("KT_FHL_STATUS", "KT_FHL_OK");
					fileName = "KTNET_FHL_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("KT_FHL_STATUS", "KT_FHL_FAIL");
					fileName = "KTNET_FHL_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("KTNET_FHL_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "KTNET FHL");
			}else if(fhlmap.get("SRC_PIMA").equals("RKRCCS77AMS")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("KT_AMS_STATUS", "KT_AMS_OK");
					fileName = "KTNET_AMS_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("KT_AMS_STATUS", "KT_AMS_FAIL");
					fileName = "KTNET_AMS_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("KTNET_AMS_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "KTNET AMS");
			}else if(fhlmap.get("SRC_PIMA").equals("KCNECP")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("KC_ACPS_STATUS", "KC_ACPS_OK");
					fileName = "KCNET_ACPS_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("KC_ACPS_STATUS", "KC_ACPS_FAIL");
					fileName = "KCNET_ACPS_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("KCNET_ACPS_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "KCNET ACPS");
			}else if(fhlmap.get("SRC_PIMA").equals("KCNECF5")){
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("KC_MFCS_STATUS", "KC_MFCS_OK");
					fileName = "KCNET_MFCS_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("KC_MFCS_STATUS", "KC_MFCS_FAIL");
					fileName = "KCNET_MFCS_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("KCNET_MFCS_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "KCNET MFCS");
			}else{
				if(fhlmap.get("SMI").equals("FMA")){
					fhlmap.put("OAL_AIR_SMI", "FMA");
					fileName = "OAL_AIR_FMA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}else{
					fhlmap.put("OAL_AIR_SMI", "FNA");
					fileName = "OAL_AIR_FNA_"+fhlmap.get("MAWB")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
				}
				fhlmap.put("OAL_AIR_ACK", sb.toString());
				fhlmap.put("ACK_NAME", "OAL AIR");
			}
		}catch(Exception e){
			logger.info(" === AMS ACK CREATE ERROR === \r\n", e);
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "AMS ACK Create Exception", e, msg);
			flag = false;
		}finally{
			if(!flag){

				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "AMS ACK CREATE ERROR");
				logMap.put("AMS_TRX_ROUTE", fhlmap.get("TRX_ROUTE"));
				logMap.put("AMS_STEP", "1109");

				commonSql.insertHistory(logMap);
			}else{
				logger.info(" === " + CommonUtil.nullChk(fhlmap.get("ACK_NAME")) + " ACK TRX ROUTE : " + CommonUtil.nullChk(fhlmap.get("TRX_ROUTE")) + " ===");
				HashMap ackMap = new HashMap();
				ackMap.put("ACKFILE", fileName);
				producer.sendBodyAndHeaders("direct:AMSACKqueuesend",sb.toString(), ackMap);
			}
		}

		return fhlmap;
	}

	/**
	 * ikams sucess flag FMA
	 */
	public HashMap<String, Object> ikamsFMA(HashMap<String, Object> fhlmap) throws Exception, SystemException{
		HashMap<String,Object> kams = new HashMap<String,Object>();
		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		String ams_out_reverse_key = fhlmap.get("MSG_CTRL_ID").toString();
		String msg = fhlmap.get("ORG_MSG").toString();
		String ranchar = commonUtil.randomchar();

		kams = getFHLInfoString(msg);
		// [UNB, IATA:1, RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA, AF:PIMA,
		// 140428:1820, AIRCISFHL, 0]
		String header = (String) kams.get("HEADER");
		String[] header_check;
		String[] unh_check;
		header_check = header.split("'");
		// UNH+SELWOO+CIMFHL:4+SELWOO
		unh_check = header_check[1].split("\\+");
		header_check = header_check[0].split("\\+");

		// MBI/057-25088906ICNSOF/T1K158.0
		String mbi = (String) fhlmap.get("MAWB_NO");
		String m_pre = fhlmap.get("MAWB_NO").toString().substring(0,3);
		String m_suf = fhlmap.get("MAWB_NO").toString().substring(3);
		// HBS/14KS69APR29/ICNSOF/1/K158.0//BANKNOTE SORTER
		String hbs = CommonUtil.nullChk(fhlmap.get("HAWB_NO"));
		String fileName = "IKAMS_KE_FMA_"+m_pre+"-"+m_suf+"_"+fhlmap.get("HAWB_NO")+"_"+ranchar+".txt";
		boolean flag = true;
		try{

			sb.append("UNA:+.? 'UNB+IATA:1+" + "RKRAIR08KCUS" + ":PIMA+"
					+ header_check[2] + "+" + YYMMddFormat.format(now) + ":"
					+ hhmmFormat.format(now) + "+" + header_check[5]
					+ "+0'UNH+" + unh_check[1] + "+CIMFMA:0+" + CommonUtil.nvl(kams.get("REFERENCE_NUMBER"), "0000") + "'FMA");
			// 0000 은 REFERENCE_NUMBER 값.
			sb.append("\r\n");
			sb.append("ACK/KCUS RCVD OK -" + mbi.replaceAll("-", "") + "."
					+ hbs);
			sb.append("\r\n");
			sb.append("FHL/4 ");
			sb.append("\r\n");
			sb.append("MBI/"+m_pre+"-"+m_suf+fhlmap.get("M_ORG")+fhlmap.get("M_DST")+"/T"+fhlmap.get("M_PC_Q")+"K"+fhlmap.get("M_WT_M"));
			sb.append("\r\n");
			sb.append("'UNT+3+" + unh_check[1] + "'UNZ+1+" + header_check[5]
					+ "'");
			fhlmap.put("KAMS_ACK", sb.toString());
		}catch(Exception e){
			logger.info(" === IKAMS ACK CREATE ERROR === \r\n", e);
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "IKAMS ACK Create Exception", e, msg);
			flag = false;
		}finally{
			if(!flag){

				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "IKAMS ACK CREATE ERROR");
				logMap.put("AMS_TRX_ROUTE", fhlmap.get("MSG_CTRL_ID"));
				logMap.put("IKAMS_STEP", "1100");

				commonSql.insertHistory(logMap);
			}else{
				logger.info(" === IKAMS KE FMA ACK TRX ROUTE : " + ams_out_reverse_key + " ===");
				HashMap ackMap = new HashMap();
				ackMap.put("ACKFILE", fileName);
				producer.sendBodyAndHeaders("direct:AMSACKqueuesend",sb.toString(), ackMap);
			}
		}

		return fhlmap;
	}

	/**
	 * ikams sucess flag FNA
	 */
	public HashMap<String, Object> ikamsFNA(HashMap<String, Object> fhlmap) throws Exception, SystemException{
		HashMap<String,Object> kams = new HashMap<String,Object>();
		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		String ams_out_reverse_key = fhlmap.get("MSG_CTRL_ID").toString();
		String msg = fhlmap.get("ORG_MSG").toString();
		String ranchar = commonUtil.randomchar();

		kams = getFHLInfoString(msg);
		// [UNB, IATA:1, RKRAGT82AMSWOO01/SEL01:PIMA:SELKOA, AF:PIMA,
		// 140428:1820, AIRCISFHL, 0]
		String header = (String) kams.get("HEADER");
		String[] header_check;
		String[] unh_check;
		header_check = header.split("'");
		// UNH+SELWOO+CIMFHL:4+SELWOO
		unh_check = header_check[1].split("\\+");
		header_check = header_check[0].split("\\+");

		// MBI/057-25088906ICNSOF/T1K158.0
		String mbi = (String) fhlmap.get("MAWB_NO");
		String m_pre = fhlmap.get("MAWB_NO").toString().substring(0,3);
		String m_suf = fhlmap.get("MAWB_NO").toString().substring(3);
		// HBS/14KS69APR29/ICNSOF/1/K158.0//BANKNOTE SORTER
		String hbs = CommonUtil.nullChk(fhlmap.get("HAWB_NO"));

		String ack_body = (String) kams.get("BODY");
		String failmsg;
		if(fhlmap.get("ACK_TYPE").equals("IMP")){
			fhlmap = IKAMS_IMP_ERROR_DESC(fhlmap);
		}else{
			fhlmap = IKAMS_EXP_ERROR_DESC(fhlmap);
		}
		String fileName = "IKAMS_KE_FNA_"+m_pre+"-"+m_suf+"_"+fhlmap.get("HAWB_NO")+"_"+ranchar+".txt";
		boolean flag = true;
		try{

			sb.append("UNA:+.? 'UNB+IATA:1+" + "RKRAIR08KCUS" + ":PIMA+"
					+ header_check[2] + "+" + YYMMddFormat.format(now) + ":"
					+ hhmmFormat.format(now) + "+" + header_check[5]
					+ "+0'UNH+" + unh_check[1] + "+CIMFNA:0+" + CommonUtil.nvl(kams.get("REFERENCE_NUMBER"), "0000") + "'FNA");
			// 0000 은 REFERENCE_NUMBER 값.
			sb.append("\r\n");
			sb.append("ACK/"+CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG")));
			sb.append("\r\n");
			sb.append(ack_body);
			sb.append("'UNT+3+" + unh_check[1] + "'UNZ+1+" + header_check[5] + "'");
			//sb.append("'UNT+3+0000'UNZ+1+0000'");
			fhlmap.put("KAMS_ACK", sb.toString());
		}catch(Exception e){
			logger.info(" === IKAMS ACK CREATE ERROR === \r\n", e);
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "IKAMS ACK Create Exception", e, msg);
			flag = false;
		}finally{
			if(!flag){

				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "IKAMS ACK CREATE ERROR");
				logMap.put("AMS_TRX_ROUTE", fhlmap.get("MSG_CTRL_ID"));
				logMap.put("IKAMS_STEP", "1100");

				commonSql.insertHistory(logMap);
			}else{
				logger.info(" === IKAMS KE FNA ACK TRX ROUTE : " + ams_out_reverse_key + " ===");
				HashMap ackMap = new HashMap();
				ackMap.put("ACKFILE", fileName);
				producer.sendBodyAndHeaders("direct:AMSACKqueuesend",sb.toString(), ackMap);
			}
		}

		return fhlmap;
	}

	public HashMap<String, Object> IKAMS_EXP_ERROR_DESC(HashMap<String, Object> fhlmap){

		if(CommonUtil.nullChk(fhlmap.get("FAIL_MSG")).equals("R")){
			fhlmap.put("IKAMS_ERRMSG", "FLT DEPARTED. NO FHL NEEDED R");
			fhlmap.put("IKAMS_ERRMSG_KOR", "적하신고 마감된 경우이나 상태는 Offload 와 동일(변경 불가 - 정정신고로 해결)");
		}else if(CommonUtil.nullChk(fhlmap.get("FAIL_MSG")).equals("M")){
			fhlmap.put("IKAMS_ERRMSG", "AWB OR FHL RCVD. NO MORE FHL NEEDED M");
			fhlmap.put("IKAMS_ERRMSG_KOR", "이미 적하목록 신고가 시작된 경우로 변경하기 불가능 - 대부분 정정신고로 해결");
		}else if(CommonUtil.nullChk(fhlmap.get("FAIL_MSG")).equals("D")){
			fhlmap.put("IKAMS_ERRMSG", "AWB DELETED. NEED CTC WITH COUNTER D");
			fhlmap.put("IKAMS_ERRMSG_KOR", "항공사 담당자가 해당 빌을 삭제한 경우이고 카운터에 문의 하여 접수 불가능 한지 확인 해야 함");
		}else if(CommonUtil.nullChk(fhlmap.get("FAIL_MSG")).equals("A")){
			fhlmap.put("IKAMS_ERRMSG", "AWB OR FHL RCVD. NO MORE FHL NEEDED A");
			fhlmap.put("IKAMS_ERRMSG_KOR", "카운터 접수마감(단, 긴급한 경우 카운터에서 상태를 초기화 시킬 수 있음 - 전화 통화)");
		}else if(CommonUtil.nullChk(fhlmap.get("FAIL_MSG")).equals("5AG")){
			fhlmap.put("IKAMS_ERRMSG", "CUSTOMS DECLARATION CLOSED 5AG");
			fhlmap.put("IKAMS_ERRMSG_KOR", "수출 적하목록 신고 마감");
		}else{
			fhlmap.put("IKAMS_ERRMSG", "FLT DEPARTED. NO FHL NEEDED");
			fhlmap.put("IKAMS_ERRMSG_KOR", "이륙한 경우이고 Release 와 구분 무의미 - 정정신고로 해결");
		}

		return fhlmap;

	}

	public HashMap<String, Object> IKAMS_IMP_ERROR_DESC(HashMap<String, Object> fhlmap){

		if(CommonUtil.nullChk(fhlmap.get("FAIL_MSG")).equals("AIF")){
			fhlmap.put("IKAMS_ERRMSG", "CUSTOMS DECLARATION CLOSED AIF");
			fhlmap.put("IKAMS_ERRMSG_KOR", "이미 수입 적하목록 신고가 시작된 경우(부분 재전송)");
		}else if(CommonUtil.nullChk(fhlmap.get("FAIL_MSG")).equals("A85")){
			fhlmap.put("IKAMS_ERRMSG", "CUSTOMS DECLARATION CLOSED A85");
			fhlmap.put("IKAMS_ERRMSG_KOR", "이미 수입 적하목록 신고가 시작된 경우(최초 문서 발생)");
		}else if(CommonUtil.nullChk(fhlmap.get("FAIL_MSG")).equals("5UZ")){
			fhlmap.put("IKAMS_ERRMSG", "CUSTOMS DECLARATION CLOSED 5UZ");
			fhlmap.put("IKAMS_ERRMSG_KOR", "하기신고 승인까지 받은 경우임");
		}else{
			fhlmap.put("IKAMS_ERRMSG", "AWB MODIFIED. NEED CTC WITH COUNTER");
			fhlmap.put("IKAMS_ERRMSG_KOR", "항공사 담당자가 해당 빌을 업데이트 한 경우, 카운터에 문의 하여 접수 불가능 한지 확인 해야 함");
		}

		return fhlmap;

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
					} else if (s.startsWith("MBI/")
								|| (lineNum == 2
								    && !map.get("SMI").equals("FMA")
									&& !map.get("SMI").equals("FNA"))) {
						map.put("MBI", s);
						String[] arrMawb =null;
						arrMawb = s.split("/");
						String mawb= "";
						if(map.get("SMI").equals("FWB")
							|| map.get("SMI").equals("FSR")
							|| map.get("SMI").equals("FSA")
							|| map.get("SMI").equals("FFR")
							|| map.get("SMI").equals("FFA")
							|| map.get("SMI").equals("FSU")){
							if(arrMawb.length > 0){
								if(arrMawb[0].length() > 11){
									mawb = arrMawb[0].substring(0,12);
								}
							}
						}else if(map.get("SMI").equals("FHL") || s.indexOf("MBI/") != -1){
							if(arrMawb.length > 1){
								if(arrMawb[1].length() > 11){
									mawb = arrMawb[1].substring(0,12);
								}
							}
						}else if(map.get("SMI").equals("FVR")){
							String[] arrFvr = null;
							arrFvr = s.split("/");
							if(arrFvr.length > 2){
								map.put("FVR_ORG", arrFvr[1].substring(0,3));
								map.put("FVR_DEST", arrFvr[1].substring(3,arrFvr[1].length()));
								map.put("FVR_REQ_D", arrFvr[2]);
							}

						}
						map.put("MAWB", mawb.replaceAll("-", ""));
					} else if (s.startsWith("HBS/")) {
						if(s.indexOf("ACK/") != -1){
							String[] arrHawb =null;
							if(s.indexOf("\\.") != -1){
								arrHawb = s.split("\\.");
								if(arrHawb.length > 1){
									map.put("HAWB", arrHawb[1]);
								}
							}
						}
						if(s.indexOf("HBS/") != -1){
							map.put("HBS", s);
							String[] arrHawb =null;
							arrHawb = s.split("/");
							if(arrHawb.length > 0){
								map.put("HAWB", arrHawb[1]);
							}
						}

					} else if (s.indexOf("FHL/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							|| s.indexOf("FWB/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							|| s.indexOf("FFR/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							|| s.indexOf("FFA/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							|| s.indexOf("FSR/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							|| s.indexOf("FSR") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							|| s.indexOf("FSA/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							|| s.indexOf("FSA") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))
							|| s.indexOf("FSU/") != -1 && (map.get("SMI").equals("FMA") || map.get("SMI").equals("FNA"))) {
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


				map.put("EDI", strBuf.toString());

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

		}

		return map;
	}

	/**
	 * ORIGIN ROUTE 추출(SEND 기준)
	 * @param msg
	 * @return
	 */
	public static String getOriginRoute(String strHeader) {
		String[] arrHeader = null;
		String strOrgRoute = "";
		strOrgRoute = strHeader.substring(strHeader.indexOf(":PIMA"),strHeader.length());
		arrHeader = strOrgRoute.split("\\+");
		strOrgRoute = arrHeader[0].replaceAll(":","");
		strOrgRoute = strOrgRoute.replaceAll("PIMA","");
		return strOrgRoute;
	}

	/**
	 * ORIGIN ROUTE 추출(RECEIVE 기준)
	 * @param msg
	 * @return
	 */
	public static String getOriginRouteReceive(String strHeader) {
		String[] arrHeader = null;
		String strOrgRoute = "";
		strOrgRoute = strHeader.substring(strHeader.lastIndexOf(":PIMA"),strHeader.length());
		arrHeader = strOrgRoute.split("\\+");
		strOrgRoute = arrHeader[0].replaceAll(":","");
		strOrgRoute = strOrgRoute.replaceAll("PIMA","");
		return strOrgRoute;
	}



}
