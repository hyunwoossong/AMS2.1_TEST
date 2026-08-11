package com.trx.validate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.trx.Msgprocess.CreateAck;
import com.trx.db.DbinsertTrace;
import com.trx.evalleyvs.eai.message.MessageXMLResourceManager;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;

public class ValidationDoc {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(CargoImpCheck.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	CommonSql commonSql;

	@Autowired
	ValidationBiz bizcheck;

	@Autowired
	CreateAck createack = new CreateAck();

	@Autowired
	MainExceptionManager mainExceptionManager;

	@Autowired
	DbinsertTrace dbinserttrace = new DbinsertTrace();

	final static String AMS_STEP = "600";
	static MessageXMLResourceManager mxr = MessageXMLResourceManager.getInstance("MessageResource.xml");



	// /////////////////////////////////////////////
	// FHL String을 HashMap으로 변환
	// /////////////////////////////////////////////
	public  HashMap<String, Object> validationDoc(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, @Body String msg, Exchange exchange) throws Exception{

		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("EDI_PARSE");
		boolean flag = true;
		String body = fhlmap.get("BODY").toString();
		String fhlbody = body.toString().replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("<br>", "\r\n");
		String fnaMsg = "";
		String[] arrFHL = fhlbody.split("\r\n");
		String mbiline = "";
		String hbsline = "";
		String txtline = "";
		String htsline = "";
		String ociline = "";
		String ocietcline = "";
		String ocishpline = "";
		String ocicneline = "";
		String ocinfyline = "";
//		String ocicnline = "";
		String shpline = "";
		String cneline = "";
		String cvdline = "";
		StringBuffer mbi = new StringBuffer();
		StringBuffer hbs = new StringBuffer();
		StringBuffer txt = new StringBuffer();
		StringBuffer hts = new StringBuffer();
		StringBuffer oci = new StringBuffer();
		StringBuffer ociShp = new StringBuffer();
		StringBuffer ociCne = new StringBuffer();
		StringBuffer ociNfy = new StringBuffer();
		StringBuffer ocicn = new StringBuffer();
		StringBuffer ocietc = new StringBuffer();
		StringBuffer shp = new StringBuffer();
		StringBuffer cne = new StringBuffer();
		StringBuffer cvd = new StringBuffer();
		try{
			sqlMapClient.startTransaction();
			logger.info(" === AMS Validation Check START === ");

			if (fhlbody.length() > 0) {
					for(int i=0;i<arrFHL.length;i++){
						String s = "";
						s = arrFHL[i];
						s = s.replace("\r", "");
				    	s = s.replace("\n", "");
				    	if(arrFHL[i].startsWith("MBI")){
				    		for(int oct=i; oct<arrFHL[i].length(); oct++){
				    			if(arrFHL[oct].startsWith("HBS/")){
				    				break;
				    			}else{
				    				mbi.append(arrFHL[oct]+"\r\n");
				    				fhlmap.put("MBI_LINE", mbi.toString());
				    				mbiline = fhlmap.get("MBI_LINE").toString();
				    				if (mbiline.endsWith("\r\n")){
				    					mbiline = mbiline.substring(0, mbiline.length() - "\r\n".length());
				    					fhlmap.put("MBI_LINE", mbiline);
				    				}
				    			}
				    		}
				    		flag = MBICheck(fhlmap);
				    		if(flag == false){
				    			fhlmap.put("AMS_FLAG", flag);
				    		}
				    	}else if(arrFHL[i].startsWith("HBS")){
				    		for(int oct=i; oct<arrFHL[i].length(); oct++){
				    			if(arrFHL[oct].startsWith("TXT/")){
				    				break;
				    			}else{
				    				hbs.append(arrFHL[oct]+"\r\n");
				    				fhlmap.put("HBS_LINE", hbs.toString());
				    				hbsline = fhlmap.get("HBS_LINE").toString();
				    				if (hbsline.endsWith("\r\n")){
				    					hbsline = hbsline.substring(0, hbsline.length() - "\r\n".length());
				    					fhlmap.put("HBS_LINE", hbsline);
				    				}
				    			}
				    		}
				    		flag = HBSCheck(fhlmap);
				    		if(flag == false){
				    			fhlmap.put("AMS_FLAG", flag);
				    		}
				    	}else if(arrFHL[i].startsWith("TXT")){
				    		for(int oct=i; oct<arrFHL.length; oct++){
				    			if(arrFHL[oct].startsWith("HTS") || arrFHL[oct].startsWith("OCI/") || arrFHL[oct].startsWith("SHP/")){
				    				break;
				    			}else{
				    				txt.append(arrFHL[oct]+"\r\n");
				    				fhlmap.put("TXT_LINE", txt.toString());
				    				txtline = fhlmap.get("TXT_LINE").toString();
				    				if(txtline.endsWith("\r\n")){
				    					txtline = txtline.substring(0, txtline.length() - "\r\n".length());
				    					fhlmap.put("TXT_LINE", txtline);
				    				}
				    			}
				    		}
				    		flag = TXTCheck(fhlmap);
				    		if(flag == false){
				    			fhlmap.put("AMS_FLAG", flag);
				    		}
				    	}else if(arrFHL[i].startsWith("HTS")){
				    		for(int oct=i; oct<arrFHL.length; oct++){
				    			if(arrFHL[oct].startsWith("OCI/") || arrFHL[oct].startsWith("SHP/")){
				    				break;
				    			}else{
				    				hts.append(arrFHL[oct]+"\r\n");
				    				fhlmap.put("HTS_LINE", hts.toString());
				    				htsline = fhlmap.get("HTS_LINE").toString();
				    				if(htsline.endsWith("\r\n")){
				    					htsline = htsline.substring(0, htsline.length() - "\r\n".length());
				    					fhlmap.put("HTS_LINE", htsline);
				    				}
				    			}
				    		}
				    		flag = HTSCheck(fhlmap);
				    		if(flag == false){
				    			fhlmap.put("AMS_FLAG", flag);
				    		}
				    	}else if(arrFHL[i].startsWith("OCI/")){

				    		//국내세관용만
				    		// 20241105 FHL OCI CIMP 수정 - ACAS F/U
				    		for(int z=i; z<arrFHL.length; z++){

				    			String oci_str = "";
					    		oci_str = CommonUtil.nullChk(arrFHL[z]);

					    		if (oci_str.startsWith("SHP/")) {
					    			break;
					    		}

								if (oci_str.indexOf("/KR/EXP/") != -1
								|| oci_str.indexOf("/KR/IMP/") != -1) { //한국세관만 OCI에 담는다
									oci.append(oci_str+"\r\n");

								}else if(oci_str.indexOf("'UNT") !=-1){//UNT부분은 그냥 제외

								}else {
									ocietc.append(oci_str+"\r\n");// 한국세관 제외, unt제외한 나머지 (해외세관 및 다른 STATUS 포함)
								}

//					    		if(oci_str.indexOf("/SHP/T/")  != -1
//					    		 ||oci_str.indexOf("/SHP/KC/") != -1
//					    		 ||oci_str.indexOf("/SHP/U/")  != -1
//					    		 ||oci_str.indexOf("/SHP/CP/") != -1
//					    		 ||oci_str.indexOf("/SHP/CT/") != -1
//					    		 ||oci_str.indexOf("/SHP/E/")  != -1
//					    		 ||oci_str.indexOf("/SHP/")  != -1
//					    		 ||oci_str.indexOf("/CNE/T/")  != -1
//					    		 ||oci_str.indexOf("/CNE/KC/") != -1
//					    		 ||oci_str.indexOf("/CNE/U/")  != -1
//					    		 ||oci_str.indexOf("/CNE/CP/") != -1
//							     ||oci_str.indexOf("/CNE/CT/")  != -1
//					    		 ||oci_str.indexOf("/CNE/E/")  != -1
//					    		 ||oci_str.indexOf("/CNE/")  != -1
//					    		 ||oci_str.indexOf("/NFY/T/")  != -1
//					    		 ||oci_str.indexOf("/NFY/KC/") != -1
//					    		 ||oci_str.indexOf("/NFY/U/")  != -1
//					    		 ||oci_str.indexOf("/NFY/CP/") != -1
//							     ||oci_str.indexOf("/NFY/CT/")  != -1
//					    		 ||oci_str.indexOf("/NFY/E/")  != -1
//					    		 ||oci_str.indexOf("/NFY/")  != -1
//					    		 ||oci_str.startsWith("SHP/")){
//					    			break;
//					    		}else{
//					    			oci.append(oci_str+"\r\n");
//					    		}
				    		}
				    		fhlmap.put("OCI_LINE", oci.toString());
				    		fhlmap.put("OCI_LINE_CHK_A", oci.toString());
				    		ociline = fhlmap.get("OCI_LINE").toString();

				    		fhlmap.put("OCI_ETC_LINE", ocietc.toString());
				    		ocietcline = fhlmap.get("OCI_ETC_LINE").toString();

		    				if (ociline.endsWith("\r\n")){
		    					ociline = ociline.substring(0, ociline.length() - "\r\n".length());
		    					fhlmap.put("OCI_LINE", ociline);
		    				}

		    				if (ocietcline.endsWith("\r\n")){
		    					ocietcline = ocietcline.substring(0, ocietcline.length() - "\r\n".length());
		    					fhlmap.put("OCI_ETC_LINE", ocietcline);
		    				}
		    				if (ocietcline.startsWith("OCI")){//oci status 삭제 후 validation (edi conversion 할 때 다시 추가)
		    					ocietcline = ocietcline.substring(3);
		    					fhlmap.put("OCI_ETC_LINE", ocietcline);
		    				}

		    				if(CommonUtil.nullChk(ocietcline) != null && CommonUtil.nullChk(ocietcline).length() > 0){//ocietcline(한국세관 제외) 이 있을 때 CIMP VALIDATION
		    					flag = OCICheck_Origin2(fhlmap);
		    		    		if(flag == false){
		    		    			fhlmap.put("AMS_FLAG", flag);
		    		    		}
		    				}

		    				if(CommonUtil.nullChk(ociline) != null && CommonUtil.nullChk(ociline).length() > 0){
		    					/*fhlmap.put("OCI_CHECK", "C");
					    		flag = OCICheck(fhlmap);*/

					    		if(CommonUtil.nullChk(ociline).indexOf("OCI/KR/EXP") != -1
					    		|| CommonUtil.nullChk(ociline).indexOf("OCI/KR/IMP") != -1){
					    			fhlmap.put("OCI_CHECK", "C");
		    						flag = OCICheck(fhlmap);
		    					}else{
		    						flag = OCICheck_Origin(fhlmap);
		    					}
					    		if(flag == false){
					    			fhlmap.put("AMS_FLAG", flag);
					    		}
		    				}
				    		//20180518 중국 세관용만
		    				// 20241105 FHL OCI CIMP 수정 - ACAS F/U
				    		for(int z=i; z<arrFHL.length; z++){

				    			String oci_str = "";
					    		oci_str = CommonUtil.nullChknotrim(arrFHL[z]);
					    		if(oci_str.indexOf("/SHP/T/")  != -1
					    		 ||oci_str.indexOf("/SHP/KC/") != -1
					    		 ||oci_str.indexOf("/SHP/U/")  != -1
					    		 ||oci_str.indexOf("/SHP/CP/") != -1
							     ||oci_str.indexOf("/SHP/CT/")  != -1
					    		 ||oci_str.indexOf("/SHP/E/")  != -1
//					    		 ||oci_str.indexOf("/SHP/")  != -1
					    		 ){

					    			ociShp.append(oci_str+"\r\n");

					    		}else if(oci_str.indexOf("/CNE/T/")  != -1
					    		 ||oci_str.indexOf("/CNE/KC/") != -1
					    		 ||oci_str.indexOf("/CNE/U/")  != -1
			    				 ||oci_str.indexOf("/CNE/CP/") != -1
					    		 ||oci_str.indexOf("/CNE/CT/")  != -1
					    		 ||oci_str.indexOf("/CNE/E/")  != -1
//					    		 ||oci_str.indexOf("/CNE/")  != -1
					    		 ){

					    			ociCne.append(oci_str+"\r\n");

					    		}else if(oci_str.indexOf("/NFY/T/")  != -1
					    		 ||oci_str.indexOf("/NFY/KC/") != -1
					    		 ||oci_str.indexOf("/NFY/U/")  != -1
			    				 ||oci_str.indexOf("/NFY/CP/") != -1
							     ||oci_str.indexOf("/NFY/CT/")  != -1
					    		 ||oci_str.indexOf("/NFY/E/")  != -1
//					    		 ||oci_str.indexOf("/NFY/")  != -1
					    		 ){

					    			ociNfy.append(oci_str+"\r\n");

					    		}
				    		}
		    				fhlmap.put("OCI_SHP_LINE", ociShp.toString());
		    				fhlmap.put("OCI_CNE_LINE", ociCne.toString());
		    				fhlmap.put("OCI_NFY_LINE", ociNfy.toString());
		    				ocishpline = fhlmap.get("OCI_SHP_LINE").toString();
		    				if (ocishpline.endsWith("\r\n")){
		    					ocishpline = ocishpline.substring(0, ocishpline.length() - "\r\n".length());
		    					fhlmap.put("OCI_SHP_INPUT", ocishpline);
		    				}
		    				ocicneline = fhlmap.get("OCI_CNE_LINE").toString();
		    				if (ocicneline.endsWith("\r\n")){
		    					ocicneline = ocicneline.substring(0, ocicneline.length() - "\r\n".length());
		    					fhlmap.put("OCI_CNE_INPUT", ocicneline);
		    				}
		    				ocinfyline = fhlmap.get("OCI_NFY_LINE").toString();
		    				if (ocinfyline.endsWith("\r\n")){
		    					ocinfyline = ocinfyline.substring(0, ocinfyline.length() - "\r\n".length());
		    					fhlmap.put("OCI_NFY_INPUT", ocinfyline);
		    				}
		    				if(CommonUtil.nullChk(ocishpline) != null && CommonUtil.nullChk(ocishpline).length() > 0){
			    				flag = OCIShpCheck(fhlmap);
					    		if(flag == false){
					    			fhlmap.put("AMS_FLAG", flag);
					    		}
		    				}
				    		if(CommonUtil.nullChk(ocicneline) != null && CommonUtil.nullChk(ocicneline).length() > 0){
					    		flag = OCICneCheck(fhlmap);
					    		if(flag == false){
					    			fhlmap.put("AMS_FLAG", flag);
					    		}
				    		}
				    		if(CommonUtil.nullChk(ocinfyline) != null && CommonUtil.nullChk(ocinfyline).length() > 0){
					    		flag = OCINfyCheck(fhlmap);
								if(flag == false){
									fhlmap.put("AMS_FLAG", flag);
					    		}
				    		}
//				    		for(int z=i; z<arrFHL.length; z++){
//
//				    			String oci_str = "";
//					    		oci_str = CommonUtil.nullChknotrim(arrFHL[z]);
//					    		if(oci_str.indexOf("/SHP/T/")  != -1
//					    		 ||oci_str.indexOf("/SHP/KC/") != -1
//					    		 ||oci_str.indexOf("/SHP/U/")  != -1
//					    		 ||oci_str.indexOf("/SHP/CP/") != -1
//							     ||oci_str.indexOf("/SHP/CT/")  != -1
//					    		 ||oci_str.indexOf("/SHP/E/")  != -1
//					    		 ||oci_str.indexOf("/SHP/")  != -1
//					    		 ||oci_str.indexOf("/CNE/T/")  != -1
//					    		 ||oci_str.indexOf("/CNE/KC/") != -1
//					    		 ||oci_str.indexOf("/CNE/U/")  != -1
//					    		 ||oci_str.indexOf("/CNE/CP/") != -1
//							     ||oci_str.indexOf("/CNE/CT/")  != -1
//					    		 ||oci_str.indexOf("/CNE/E/")  != -1
//					    		 ||oci_str.indexOf("/CNE/")  != -1
//					    		 ||oci_str.indexOf("/NFY/T/")  != -1
//					    		 ||oci_str.indexOf("/NFY/KC/") != -1
//					    		 ||oci_str.indexOf("/NFY/U/")  != -1
//					    		 ||oci_str.indexOf("/NFY/CP/") != -1
//							     ||oci_str.indexOf("/NFY/CT/")  != -1
//					    		 ||oci_str.indexOf("/NFY/E/")  != -1
//					    		 ||oci_str.indexOf("/NFY/")  != -1){
//
//					    			ocicn.append(oci_str+"\r\n");
//
//					    		}
//				    		}
//				    		fhlmap.put("OCI_CN_LINE", ocicn.toString());
//				    		ocicnline = fhlmap.get("OCI_CN_LINE").toString().replace("OCI/", "/");
				    	}else if(arrFHL[i].startsWith("SHP")){
				    		for(int oct=i; oct<arrFHL.length; oct++){
				    			if(arrFHL[oct].startsWith("CNE/")){
				    				break;
				    			}else{
				    				shp.append(arrFHL[oct]+"\r\n");
				    				fhlmap.put("SHP_LINE", shp.toString());
				    				shpline = fhlmap.get("SHP_LINE").toString();
				    				if(shpline.endsWith("\r\n")){
				    					shpline = shpline.substring(0, shpline.length() - "\r\n".length());
				    					fhlmap.put("SHP_LINE", shpline);
				    				}
				    			}
				    		}
				    		flag = SHPCheck(fhlmap);
				    		if(flag == false){
				    			fhlmap.put("AMS_FLAG", flag);
				    		}
				    	}else if(arrFHL[i].startsWith("CNE")){
							for(int oct=i; oct<arrFHL.length; oct++){
								if(arrFHL[oct].startsWith("CVD")||arrFHL[oct].startsWith("'UNT")){
									break;
								}else{
									cne.append(arrFHL[oct]+"\r\n");
									fhlmap.put("CNE_LINE", cne.toString());
									cneline = fhlmap.get("CNE_LINE").toString();
									if(cneline.endsWith("\r\n")){
										cneline = cneline.substring(0, cneline.length() - "\r\n".length());
										fhlmap.put("CNE_LINE", cneline);
									}
								}
							}
							flag = CNECheck(fhlmap);
							if(flag == false){
								fhlmap.put("AMS_FLAG", flag);
				    		}
						}else if(arrFHL[i].startsWith("CVD")){
							for(int oct=i; oct<arrFHL.length; oct++){
								if(arrFHL[oct].startsWith("'UNT")){
									break;
								}else{
									cvd.append(arrFHL[oct]+"\r\n");
									fhlmap.put("CVD_LINE", cvd.toString());
									cvdline = fhlmap.get("CVD_LINE").toString();
									if(cvdline.endsWith("\r\n")){
										cvdline = cvdline.substring(0, cvdline.length() - "\r\n".length());
										fhlmap.put("CVD_LINE", cvdline);
									}
								}
							}
							flag = CVDCheck(fhlmap);
							if(flag == false){
								fhlmap.put("AMS_FLAG", flag);
				    		}
						}
					}

					// 20241105 FHL OCI CIMP 수정 - ACAS F/U
//					if(Pattern.matches("(((\\/[A-Z]{2}\\/(SHP)\\/[A-Z]{1,2}\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)*)?"
//							+ "(((\\/[A-Z]{2}\\/(CNE)\\/[A-Z]{1,2}\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)*)?"
//							+ "(((\\/[A-Z]{2}\\/(NFY)\\/[A-Z]{1,2}\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)*)?", ocicnline)){
//
//					}else{
//
//						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"),"").equals("")){
//							fhlmap.put("ERRORMSG", "CN OCI SHP CNE NFY SEQUENCE ERROR");
//							fhlmap.put("ERRORMSG_KOR", "CN OCI SHP/CNE/NFY 순서 입력 오류");
//						}
//						flag = false;
//						fhlmap.put("AMS_FLAG", flag);
//						logger.info(" === CN OCI SEQUENCE ERROR  === ");
//					}


					if(fhlbody.indexOf("SHP/") !=-1){
						if(fhlbody.indexOf("CNE/") == -1){
							if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"),"").equals("")){
								fhlmap.put("ERRORMSG", "CNE LINE IS NULL");
								fhlmap.put("ERRORMSG_KOR", "SHP 정보 입력 시 CNE 라인 필수");
							}
							flag = false;
							fhlmap.put("AMS_FLAG", flag);
						}
					}

					if(fhlmap.get("AMS_FLAG") == null){
						fhlmap.put("AMS_FLAG", flag);
					}
			}

			if(!(Boolean)fhlmap.get("AMS_FLAG")){

				if(CommonUtil.nullChk(exchange.getIn().getHeader("Pima_Check_Flag")).equals("false")){

					HashMap logMap = new HashMap();
					//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
					logMap.put("HISTORY_STATUS", "AMS Validation Check Error");
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
					logMap.put("AMS_STEP", "301");

					commonSql.insertHistory(logMap);
					commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
					commonSql.insertFHL(ams_out_reverse_key, logMap, fhlmap);

					if(CommonUtil.nullChk(ocishpline) != null && CommonUtil.nullChk(ocishpline).length() > 0
							||CommonUtil.nullChk(ocicneline) != null && CommonUtil.nullChk(ocicneline).length() > 0
							|| CommonUtil.nullChk(ocinfyline) != null && CommonUtil.nullChk(ocinfyline).length() > 0){

							commonSql.insertOCICN(ams_out_reverse_key, logMap, fhlmap);
					}

					flag = false;
					logger.info(" === Validation Check Error === ");

				}else if(CommonUtil.nullChk(exchange.getIn().getHeader("Air_Check_Flag")).equals("false")){

					HashMap logMap = new HashMap();
					logMap.put("HISTORY_STATUS", "AMS Validation Check Error");
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
					logMap.put("AMS_STEP", "401");

					commonSql.insertHistory(logMap);
					commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
					commonSql.insertFHL(ams_out_reverse_key, logMap, fhlmap);

					if(CommonUtil.nullChk(ocishpline) != null && CommonUtil.nullChk(ocishpline).length() > 0
							||CommonUtil.nullChk(ocicneline) != null && CommonUtil.nullChk(ocicneline).length() > 0
							|| CommonUtil.nullChk(ocinfyline) != null && CommonUtil.nullChk(ocinfyline).length() > 0){

							commonSql.insertOCICN(ams_out_reverse_key, logMap, fhlmap);
					}

					flag = false;
					logger.info(" === Validation Check Error === ");

				}else{

					fhlmap.put("AMS_SMI", "FNA");
					fnaMsg = createack.FNAack(fhlmap);
					fhlmap.put("AMS_ACK", fnaMsg); //AMS ACK로 변경
					//EDI HISTORY INSERT[S]
					HashMap logMap = new HashMap();
					//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
					logMap.put("HISTORY_STATUS", "AMS Validation Check Error");
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
					logMap.put("AMS_STEP", "609");
					commonSql.insertHistory(logMap);
					commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
					commonSql.insertFHL(ams_out_reverse_key, logMap, fhlmap);

					if(CommonUtil.nullChk(ocishpline) != null && CommonUtil.nullChk(ocishpline).length() > 0
							||CommonUtil.nullChk(ocicneline) != null && CommonUtil.nullChk(ocicneline).length() > 0
							|| CommonUtil.nullChk(ocinfyline) != null && CommonUtil.nullChk(ocinfyline).length() > 0){

							commonSql.insertOCICN(ams_out_reverse_key, logMap, fhlmap);
					}

					flag = false;
					logger.info(" === Validation Check Error === ");

				}


			}else{
				//EDI HISTORY INSERT[S]
				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "AMS Validation Check Success");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);

				if(CommonUtil.nullChk(exchange.getIn().getHeader("Pima_Check_Flag")).equals("false")){
					logMap.put("AMS_STEP", "301");
				}else if(CommonUtil.nullChk(exchange.getIn().getHeader("Air_Check_Flag")).equals("false")){
					logMap.put("AMS_STEP", "401");
				}else{
					logMap.put("AMS_STEP", AMS_STEP);
				}

				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
				if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("A")
						|| CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("E")
						|| CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("M")
						|| CommonUtil.nullChk(fhlmap.get("VERSION")).equals("2")
						|| (CommonUtil.nullChk(fhlmap.get("VERSION")).equals("4") && CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals(""))
						|| (CommonUtil.nullChk(fhlmap.get("VERSION")).equals("4") && CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals(""))){

					commonSql.insertFHL(ams_out_reverse_key, logMap, fhlmap);

					if(CommonUtil.nullChk(ocishpline) != null && CommonUtil.nullChk(ocishpline).length() > 0
							||CommonUtil.nullChk(ocicneline) != null && CommonUtil.nullChk(ocicneline).length() > 0
							|| CommonUtil.nullChk(ocinfyline) != null && CommonUtil.nullChk(ocinfyline).length() > 0){

							commonSql.insertOCICN(ams_out_reverse_key, logMap, fhlmap);
					}
				}
				//EDI HISTORY INSERT[E]
				flag = true;
				logger.info(" === AMS Validation Check Success === ");
			}

			exchange.getIn().setHeader("EDI_PARSE", fhlmap);
			exchange.getIn().setHeader("AMS_Validation_Check_Flag", flag);
		} catch(IndexOutOfBoundsException e){
			logger.info(" === AMS Validation Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Validation Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === AMS Validation Check NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Validation Check NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === AMS Validation Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Validation Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === AMS Validation Check Error === ");
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
	    	fhlmap.put("ERRORCODE", errorCode);
	    	fhlmap.put("ERRORMSG", (errorMsg.length()>65)?errorMsg.substring(0,65):errorMsg);
	    	flag = false;
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Validation Check Exception",e, msg);
	    }finally{
	    	exchange.getIn().setHeader("CHECK_FLAG",Boolean.valueOf(flag));
	    	//TRACE TABLE INSERT[E]
			logger.info(" === AMS Validation Check End ===");
			try{
	    		sqlMapClient.commitTransaction();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}finally{
	    		sqlMapClient.endTransaction();
	    	}
	    }
		return fhlmap;
	}

	//MBI LINE
	public boolean MBICheck(HashMap<String, Object> map) {
		// TODO Auto-generated method stub

		boolean flag = true;
		boolean valuflag = true;
		try{
			String[] mbiline = map.get("MBI_LINE").toString().replaceFirst("MBI\\/", "/").split("/");

			if(mbiline.length==3){
				if(Pattern.matches("[0-9]{3}\\-[0-9]{8}[A-Z]{6}",mbiline[1])){
					map.put("MBI_NO", mbiline[1].substring(0,3)+mbiline[1].substring(4,12));
					map.put("MBI_FULL", mbiline[1].substring(0,12));
					map.put("MBI_PRE", mbiline[1].substring(0,3));
					map.put("MBI_SUF", mbiline[1].substring(4,12));
					map.put("M_ORG", mbiline[1].substring(12,15));
					map.put("M_DST", mbiline[1].substring(15));
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "MAWB NO OR ORG DST ERROR");
						map.put("ERRMSG_KOR", "MAWB 번호 또는 출/도착지 코드가 잘못 입력 되었습니다");
					}
					flag = false;
				}

				//if(Pattern.matches("[T]{1}[0-9]{1,10}[KL]{1}[0-9.]{1,7}",mbiline[2].trim())){
				//if(Pattern.matches("[T]{1}[0-9]{1,10}[KL]{1}([0-9]{1,6}([.]{1}[0-9]{1,2})|[0-9]{1,7})?", mbiline[2].trim())){
				if(Pattern.matches("[T]{1}[0-9]{1,10}[KL]{1}([0-9]{1,6}([.]{1}[0-9]{1})|[0-9]{1,7})?",mbiline[2].trim())){
					if(mbiline[2].indexOf("K")!=-1){
						String[] PC_WT = mbiline[2].split("K");
						map.put("M_PC_CODE", "T");
						map.put("M_PC", Integer.parseInt(PC_WT[0].substring(1)));
						map.put("M_PC_T", PC_WT[0]);
						map.put("M_WT_CODE", "K");
						map.put("M_WT", Float.valueOf(PC_WT[1]).floatValue());
					}else if(mbiline[2].indexOf("L")!=-1){
						String[] PC_WT = mbiline[2].split("L");
						map.put("M_PC_CODE", "T");
						map.put("M_PC", Integer.parseInt(PC_WT[0].substring(1)));
						map.put("M_WT_CODE", "L");
						map.put("M_WT", Float.valueOf(PC_WT[1]).floatValue());
					}
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "MAWB PC OR WT ITEM ERROR");
						map.put("ERRMSG_KOR", "MAWB 수량 또는 중량이 잘못 입력 되었습니다");
					}
					flag = false;
				}

				// PC/WT 값 체크
				if(flag == true){
					valuflag = mawbCheckValue(map);
				}

			}else{
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "MAWB LINE FORMAT ERROR");
					map.put("ERRMSG_KOR", "MAWB 정보 입력 오류");
				}
				flag = false;
			}


		}catch(Exception ee){
			ee.printStackTrace();
			map.put("ERRORMSG", "FHL MBI FORMAT ERROR");
			flag = false;
		}
		if(flag == false || valuflag == false){
			flag = false;
		}
		return flag;
	}

	//mawb no 중량 체크
	public boolean mawbCheckValue(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		boolean flag = true;

		try{
			if(Float.valueOf(map.get("M_WT").toString())<=0){
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "MAWB WEIGHT INSERT ERROR");
					map.put("ERRMSG_KOR", "총 중량이 0 또는 작은 값은 입력 할 수 없습니다");
				}
				flag = false;
			}else if(Integer.parseInt(map.get("M_PC").toString())<=0){
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "MAWB PIECES  INSERT ERROR");
					map.put("ERRMSG_KOR", "총 수량이 0 또는 작은 값은 입력 할 수 없습니다");
				}
				flag = false;
			}
		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "FHL CHECK VALUE EXCEPTION");
				map.put("ERRMSG_KOR", "");
			}
			flag = false;
		}
		return flag;
	}

	//HBS LINE
	public boolean HBSCheck(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		boolean flag = true;
		boolean valuflag = true;

		try{
			String[] hbs = map.get("HBS_LINE").toString().replaceFirst("HBS\\/", "/").split("\r\n");
			String[] hbsline = hbs[0].split("/");

			if(hbsline.length==7&&CommonUtil.nullChk(map.get("VERSION"),"").equals("4")){

					if(Pattern.matches("[0-9A-Z]{1,12}", hbsline[1])){
						map.put("HBS_NO", hbsline[1].trim());
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "HAWB NO ERROR");
							map.put("ERRORMSG_KOR", "HAWB NO 입력 오류-" + hbsline[1].trim());
						}
						flag = false;
					}

				// HAWB ORG_DST CHECK
				if (Pattern.matches("[A-Z]{6}", hbsline[2])) {
					map.put("H_ORG", hbsline[2].substring(0,3));
					map.put("H_DST", hbsline[2].substring(3));
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "HAWB ORG OR DST ERROR");
						map.put("ERRORMSG_KOR", "HAWB 출발지 또는 도착지 코드가 잘못 입력 되었습니다");
					}
					flag = false;
				}
				// HAWB PIECES CHECK
				if (Pattern.matches("[0-9]{1,4}", hbsline[3])) {
					map.put("H_PC", hbsline[3]);
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "HAWB PIECES ERROR");
						map.put("ERRORMSG_KOR", "HAWB 수량 입력 오류 - " + hbsline[3]);
					}
					flag = false;
				}
				// HAWB WEIGHT CHECK
				//if (Pattern.matches("[KL]{1}[0-9.]{1,7}", hbsline[4])) {
				//if (Pattern.matches("[KL]{1}([0-9]{1,6}([.]{1}[0-9]{1,2})|[0-9]{1,7})?", hbsline[4])){
				if (Pattern.matches("[KL]{1}([0-9]{1,6}([.]{1}[0-9]{1})|[0-9]{1,7})?", hbsline[4])) {
					map.put("H_WT_CODE", hbsline[4].substring(0,1));
					map.put("H_WT", Float.valueOf(hbsline[4].substring(1)).floatValue());
					map.put("H_WT_LINE", hbsline[4]);

				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "HAWB WEIGHT ERROR");
						map.put("ERRORMSG_KOR", "HAWB 중량 입력 오류 - " + hbsline[4]);
					}
					flag = false;
				}
				// HAWB SLAC CHECK
				if (Pattern.matches("[0-9]{0,5}", hbsline[5])) {
					if(!hbsline[5].equals("")){
						map.put("H_SLAC", Integer.parseInt(hbsline[5]));
					}
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "HAWB SLAC ERROR");
						map.put("ERRORMSG_KOR", "INNER PICE 입력 오류");
					}
					flag = false;
				}

				// PC/WT 값 체크
				if(flag == true){
					valuflag = hawbCheckValue(map);
				}

				//hawb goods
				 if (Pattern.matches("[A-Z0-9\\s\\-\\.]{1,15}", hbsline[6])) {
					if(hbsline[6].replace(" ", "").length() == 0 || hbsline[6].replace(".", "").length() == 0 || hbsline[6].replace("-", "").length() == 0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("H_COMMODITY", hbsline[6]);
							map.put("ERRORMSG", "HAWB COMMODITY INSERT ERROR");
							map.put("ERRORMSG_KOR", "형식이 맞지 않는 품목이 입력 되었습니다." + hbsline[6]);
						}
						map.put("H_COMMODITY", hbsline[6]);
						flag = false;
					}else{
						map.put("H_COMMODITY", hbsline[6].trim());
					}
				 }else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "HAWB COMMODITY ERROR");
							map.put("ERRORMSG_KOR", "HAWB 품목 입력 오류");
						}
						flag = false;
					}

				if(hbs.length==2){

					if(!Pattern.matches("((/[A-Z]{3}){1,9})", hbs[1])){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "HAWB SPECIAL HANDLING CODE ERROR");
							map.put("ERRORMSG_KOR", "HAWB 스페셜 핸들링 코드 오류");
						}
						flag = false;
					}else{
						String[] spc_code = hbs[1].split("/");

						if(spc_code.length==1){
							map.put("HS_SPECIALHANDLINGCODE1", null);
						}else if(spc_code.length==2){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
						}else if(spc_code.length==3){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
							map.put("HS_SPECIALHANDLINGCODE2", CommonUtil.nullChk(spc_code[2]));
						}else if(spc_code.length==4){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
							map.put("HS_SPECIALHANDLINGCODE2", CommonUtil.nullChk(spc_code[2]));
							map.put("HS_SPECIALHANDLINGCODE3", CommonUtil.nullChk(spc_code[3]));
						}else if(spc_code.length==5){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
							map.put("HS_SPECIALHANDLINGCODE2", CommonUtil.nullChk(spc_code[2]));
							map.put("HS_SPECIALHANDLINGCODE3", CommonUtil.nullChk(spc_code[3]));
							map.put("HS_SPECIALHANDLINGCODE4", CommonUtil.nullChk(spc_code[4]));
						}else if(spc_code.length==6){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
							map.put("HS_SPECIALHANDLINGCODE2", CommonUtil.nullChk(spc_code[2]));
							map.put("HS_SPECIALHANDLINGCODE3", CommonUtil.nullChk(spc_code[3]));
							map.put("HS_SPECIALHANDLINGCODE4", CommonUtil.nullChk(spc_code[4]));
							map.put("HS_SPECIALHANDLINGCODE5", CommonUtil.nullChk(spc_code[5]));
						}else if(spc_code.length==7){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
							map.put("HS_SPECIALHANDLINGCODE2", CommonUtil.nullChk(spc_code[2]));
							map.put("HS_SPECIALHANDLINGCODE3", CommonUtil.nullChk(spc_code[3]));
							map.put("HS_SPECIALHANDLINGCODE4", CommonUtil.nullChk(spc_code[4]));
							map.put("HS_SPECIALHANDLINGCODE5", CommonUtil.nullChk(spc_code[5]));
							map.put("HS_SPECIALHANDLINGCODE6", CommonUtil.nullChk(spc_code[6]));
						}else if(spc_code.length==8){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
							map.put("HS_SPECIALHANDLINGCODE2", CommonUtil.nullChk(spc_code[2]));
							map.put("HS_SPECIALHANDLINGCODE3", CommonUtil.nullChk(spc_code[3]));
							map.put("HS_SPECIALHANDLINGCODE4", CommonUtil.nullChk(spc_code[4]));
							map.put("HS_SPECIALHANDLINGCODE5", CommonUtil.nullChk(spc_code[5]));
							map.put("HS_SPECIALHANDLINGCODE6", CommonUtil.nullChk(spc_code[6]));
							map.put("HS_SPECIALHANDLINGCODE7", CommonUtil.nullChk(spc_code[7]));
						}else if(spc_code.length==9){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
							map.put("HS_SPECIALHANDLINGCODE2", CommonUtil.nullChk(spc_code[2]));
							map.put("HS_SPECIALHANDLINGCODE3", CommonUtil.nullChk(spc_code[3]));
							map.put("HS_SPECIALHANDLINGCODE4", CommonUtil.nullChk(spc_code[4]));
							map.put("HS_SPECIALHANDLINGCODE5", CommonUtil.nullChk(spc_code[5]));
							map.put("HS_SPECIALHANDLINGCODE6", CommonUtil.nullChk(spc_code[6]));
							map.put("HS_SPECIALHANDLINGCODE7", CommonUtil.nullChk(spc_code[7]));
							map.put("HS_SPECIALHANDLINGCODE8", CommonUtil.nullChk(spc_code[8]));
						}else if(spc_code.length==10){
							map.put("HS_SPECIALHANDLINGCODE1", CommonUtil.nullChk(spc_code[1]));
							map.put("HS_SPECIALHANDLINGCODE2", CommonUtil.nullChk(spc_code[2]));
							map.put("HS_SPECIALHANDLINGCODE3", CommonUtil.nullChk(spc_code[3]));
							map.put("HS_SPECIALHANDLINGCODE4", CommonUtil.nullChk(spc_code[4]));
							map.put("HS_SPECIALHANDLINGCODE5", CommonUtil.nullChk(spc_code[5]));
							map.put("HS_SPECIALHANDLINGCODE6", CommonUtil.nullChk(spc_code[6]));
							map.put("HS_SPECIALHANDLINGCODE7", CommonUtil.nullChk(spc_code[7]));
							map.put("HS_SPECIALHANDLINGCODE8", CommonUtil.nullChk(spc_code[8]));
							map.put("HS_SPECIALHANDLINGCODE9", CommonUtil.nullChk(spc_code[9]));
						}
					}
				}
				//hbs spc 추가 20170403
			}else if(hbsline.length>=6 && map.get("VERSION").equals("2")){

				if(Pattern.matches("[0-9A-Z]{1,12}", hbsline[1])){
					map.put("HBS_NO", hbsline[1].trim());
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "HAWB NO ERROR");
						map.put("ERRORMSG_KOR", "HAWB NO 입력 오류-" + hbsline[1].trim());
					}
					flag = false;
				}

				// HAWB ORG_DST CHECK
				if (Pattern.matches("[A-Z]{6}", hbsline[2])) {
					map.put("H_ORG", hbsline[2].substring(0,3));
					map.put("H_DST", hbsline[2].substring(3));
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "HAWB ORG OR DST ERROR");
						map.put("ERRORMSG_KOR", "HAWB 출발지 또는 도착지 코드가 잘못 입력 되었습니다");
					}
					flag = false;
				}
				// HAWB PIECES CHECK
				if (Pattern.matches("[0-9]{1,4}", hbsline[3])) {
					map.put("H_PC", hbsline[3]);
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "HAWB PICES ERROR");
						map.put("ERRORMSG_KOR", "HAWB 수량 입력 오류 - " + hbsline[3]);
					}
					flag = false;
				}
				// HAWB WEIGHT CHECK
				if (Pattern.matches("[KL]{1}[0-9.]{1,7}", hbsline[4])) {

					map.put("H_WT_CODE", hbsline[4].substring(0,1));
					map.put("H_WT", Float.valueOf(hbsline[4].substring(1)).floatValue());

				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "HAWB WEIGHT ERROR");
						map.put("ERRORMSG_KOR", "HAWB 중량 입력 오류 - " + hbsline[4]);
					}
					flag = false;
				}

				// PC/WT 값 체크
				if(flag == true){
					valuflag = hawbCheckValue(map);
				}

				//hawb goods
				 if (Pattern.matches("A-Z0-9\\s\\-\\.]{1,15}", hbsline[5])) {
					 if(hbsline[5].replace(" ", "").length() == 0 || hbsline[5].replace(".", "").length() == 0 || hbsline[5].replace("-", "").length() == 0){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("H_COMMODITY", hbsline[5]);
								map.put("ERRORMSG", "HAWB COMMODITY INSERT ERROR");
								map.put("ERRORMSG_KOR", "형식이 맞지 않는 품목이 입력 되었습니다." + hbsline[5]);
							}
							map.put("H_COMMODITY", hbsline[5]);
							flag = false;
						}else{
							map.put("H_COMMODITY", hbsline[5].trim());
						}
				 }else{
					 if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "HAWB COMMODITY ERROR");
							map.put("ERRORMSG_KOR", "HAWB 품목 입력 오류");
						}
						flag = false;
				 }

				//hbs spc 추가 20170403
				 if(hbsline.length >= 7){

					 if(Pattern.matches("([A-Z]{3}){1,2}", hbsline[6])){

						 map.put("HS_SPECIALHANDLINGCODE1", hbsline[6].substring(0,3));

						 if(hbsline[6].length() >=6){
							 map.put("HS_SPECIALHANDLINGCODE2", hbsline[6].substring(3,hbsline[6].length()));
						 }
					 }else{
						 if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){

								map.put("ERRORMSG", "FHL HBS SPC ERROR");
								map.put("ERRORMSG_KOR", "HAWB SPC 입력 오류");
							}
							flag = false;
						 }
				 }
			}else{

				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL HBS FORMAT ERROR");
					map.put("ERRORMSG_KOR", "HAWB 정보 입력 오류");
				}
				flag = false;
			}

		}catch(Exception ee){
			ee.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "FHL HBS FORMAT ERROR");
				map.put("ERRORMSG_KOR", "HAWB 정보 입력 오류");
			}
			flag = false;
		}
		if(flag == false || valuflag == false){
			flag = false;
		}
		return flag;
	}

	//hawb no 중량 체크
	public boolean hawbCheckValue(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		boolean flag = true;

		try{
			if(Integer.parseInt(map.get("H_PC").toString())<=0){
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "HWB PIECES INSERT ERROR");
					map.put("ERRORMSG_KOR", "HWB 수량이 0 또는 작은 값은 입력 할 수 없습니다");
				}
				flag = false;
			}else if(Float.valueOf(map.get("H_WT").toString())<=0){
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "HWB WEIGHT INSERT ERROR");
					map.put("ERRORMSG_KOR", "HWB 중량이 0 또는 작은 값은 입력 할 수 없습니다");
				}
				flag = false;
			}
		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "FHL CHECK VALUE EXCEPTION");
				map.put("ERRORMSG_KOR", "");
			}
			flag = false;
		}
		return flag;
	}

	//TXT 항목
	public boolean TXTCheck(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		boolean flag = true;

		String[] txt_line = map.get("TXT_LINE").toString().split("\r\n");

		if(txt_line.length>=1){ //  && txt_line.length<=9
			String fltline = "";
			String dcsline = "";
			String snmline = "";
			String sccline = "";
			String sarline = "";
			String cnmline = "";
			String cccline = "";
			String carline = "";
			String com1 = "";
			String com2 = "";

			for(int a=0; a<txt_line.length; a++){
				if(txt_line[a].startsWith("TXT/.COM.")){
					if (Pattern.matches("[TXT]{3}\\/.[COM]{3}\\.[A-Z0-9\\s\\-\\.]{1,60}", txt_line[a])) {
						com2 = txt_line[a];
						com1 = txt_line[a];
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "FHL COM LINE");
							map.put("ERRORMSG_KOR", "품목 상세정보 입력 오류");
						}
						flag = false;
					}
				}else if(txt_line[a].startsWith("/.FLT.")){
					fltline = txt_line[a];
				}else if(txt_line[a].startsWith("/.DCS.") && map.get("BODY").toString().indexOf("OCI/KR/IMP/")!=-1){
					dcsline = txt_line[a];
				}else if(txt_line[a].startsWith("/.SNM.")){
					snmline += txt_line[a];
				}else if(txt_line[a].startsWith("/.SCC.")){
					sccline = txt_line[a];
				}else if(txt_line[a].startsWith("/.SAR.")){
					sarline += txt_line[a];
				}else if(txt_line[a].startsWith("/.CNM.")){
					cnmline += txt_line[a];
				}else if(txt_line[a].startsWith("/.CCC.")){
					cccline = txt_line[a];
				}else if(txt_line[a].startsWith("/.CAR.")){
					carline += txt_line[a];
				}else if(txt_line[a].indexOf("TXT/.COM.") == -1 || txt_line[a].startsWith("/.COM.")){
					if(Pattern.matches("/.[COM]{3}\\.[A-Z0-9\\s\\-\\.]{1,60}", txt_line[a])) {
						//KTNET 전송시 품목 붙여서 보내는 부분 적용
						com2 = com2 + txt_line[a].replace("/.COM.", "");
						//ACE FHL/4 전송시 /.COM. 부분 저장
						com1 = com1 + "\r\n" + txt_line[a].replace("/.COM.", "/");
						map.put("COM1", com1);
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "FHL COM LINE");
							map.put("ERRORMSG_KOR", "품목 상세정보 입력 오류");
						}
						flag = false;
					}
				}

				if (a == 0) {
					map.put("T_FREETEXT", txt_line[a].replace("TXT/", "").replace("/", ""));
				}else if(a == 1){
					map.put("T_FREETEXT1", txt_line[a].replace("TXT/", "").replace("/", ""));
				}else if(a == 2){
					map.put("T_FREETEXT2", txt_line[a].replace("TXT/", "").replace("/", ""));
				}else if(a == 3){
					map.put("T_FREETEXT3", txt_line[a].replace("TXT/", "").replace("/", ""));
				}else if(a == 4){
					map.put("T_FREETEXT4", txt_line[a].replace("TXT/", "").replace("/", ""));
				}else if(a == 5){
					map.put("T_FREETEXT5", txt_line[a].replace("TXT/", "").replace("/", ""));
				}else if(a == 6){
					map.put("T_FREETEXT6", txt_line[a].replace("TXT/", "").replace("/", ""));
				}else if(a == 7){
					map.put("T_FREETEXT7", txt_line[a].replace("TXT/", "").replace("/", ""));
				}else if(a == 8){
					map.put("T_FREETEXT8", txt_line[a].replace("TXT/", "").replace("/", ""));
				}
			}
			try{
				//KTNET 전송 시 품목 항목 붙여서 보내기 위한 값 셋팅
				if (Pattern.matches("[TXT]{3}\\/.[COM]{3}\\.[A-Z0-9\\s\\-\\.]{1,200}", com2)) {
					String Comodity = com2.replace("TXT/.COM.", "");
					if(Comodity.replace(" ", "").length() == 0 || Comodity.replace(".", "").length() == 0 || Comodity.replace("-", "").length() == 0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "COMMODITY INSERT ERROR");
							map.put("ERRORMSG_KOR", "형식이 맞지 않는 품목 상세정보가 입력 되었습니다");
						}
						flag = false;
					}
					map.put("TXT_COMODITY", Comodity);
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "FHL COM LINE");
						map.put("ERRORMSG_KOR", "품목 상세정보 입력 오류");
					}

					flag = false;
				}
			}catch(Exception e){
				e.printStackTrace();
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL COM LINE");
					map.put("ERRORMSG_KOR", "품목 상세정보 입력 오류");
				}
				flag = false;
			}
			try{
				//FLT Line
				if(!fltline.equals("")){

					String[] flt_line = fltline.split("-");

					if(Pattern.matches("[/.FLT.]{6}[0-9]{0,8}\\-[A-Z0-9]{0,7}\\-[A-Z0-9]{0,4}\\-[A-Z0-9]{0,8}\\-[A-Z0-9.]{0,8}", fltline)){

					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "FLT LINE FORMAT ERROR");
							map.put("ERRORMSG_KOR", "FLT 포멧 입력 오류");
						}
						flag = false;
					}

					// FLIGHTDATE CHECK
					if(Pattern.matches("[/.FLT.]{6}[0-9]{8}", flt_line[0])){
						String flt = flt_line[0].replace("/.FLT.", "");
						map.put("FLT", flt);
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "FLIGHT DATE INPUT ERROR");
							map.put("ERRORMSG_KOR", "입/출항일 입력 오류");
						}
						flag = false;
					}
					//FLT 편명 ex) KE123(화물기)/KE1234(여객기)/KE12345(화물기)/KE1234A(여객기)
					//FLT가 3자리 일경우 KE123 - > KE0123
					//숫자+알파벳은 숫자4자리+알파벳 1자리 만 가능(알파벳은 맨뒤에 와야함)
					if(Pattern.matches("[A-Z0-9]{2}[0-9]{3,5}", flt_line[1]) || Pattern.matches("[A-Z0-9]{2}[0-9]{3,4}[A-Z]{1}", flt_line[1])){
						String flt_air = flt_line[1].substring(0,2);
						String flt_num = "";

						//20170529 최재준 추가
						//FLT CARR CODE 숫자만 들어 왔을 경우 ERROR
						if(Pattern.matches("[0-9]{2}", flt_air)){

							map.put("FLT_AIRLINECODE", flt_air);
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "INVALID FLT CARR CODE");
								map.put("ERRORMSG_KOR", "FLT CARR CODE 입력 오류"+flt_line[1].substring(0,2));
							}
							flag = false;
						}else{
							map.put("FLT_AIRLINECODE", flt_air);
						}

						if(flt_line[1].substring(2).trim().length() < 4){
							flt_num = "0"+flt_line[1].substring(2);
							map.put("FLT_NUM", flt_num);
						}else if(flt_line[1].substring(2).trim().length() == 4){
							flt_num = flt_line[1].substring(2);
							map.put("FLT_NUM", flt_num);
						}else if(flt_line[1].substring(2).trim().length() == 5){
							flt_num = flt_line[1].substring(2);
							map.put("FLT_NUM", flt_num);
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "INVALID FLT NO");
								map.put("ERRORMSG_KOR", "FLT NO 입력 오류"+flt_line[1].substring(2));
							}
							flag = false;
						}
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "FLIGHT INPUT ERROR");
							map.put("ERRORMSG_KOR", "비행편명 입력 오류");
						}
						flag = false;
					}


					//DECONSOL CODE
					if(Pattern.matches("[A-Z0-9]{4}", flt_line[2])){
						map.put("FLT_DECONSOL", flt_line[2]);
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "DECONSOL INPUT ERROR");
							map.put("ERRORMSG_KOR", "세관 부호 코드 입력 오류");
						}
						flag = false;
					}

					//LOCATION CODE
					if (flt_line.length == 4 && Pattern.matches("[A-Z0-9]{0,8}", flt_line[3])) {
						map.put("FLT_LOCATION1", flt_line[3]);
					}else if(flt_line.length == 5 && Pattern.matches("[A-Z0-9.]{0,8}", flt_line[4])){

						if(flt_line[4].replace(".", "").length() == 0){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "WAREHOUSE CODE INPUT ERROR");
								map.put("ERRORMSG_KOR", "WAREHOUSE CODE 입력 오류"+flt_line[4]);
							}
							flag = false;
						}else{
							map.put("FLT_LOCATION1", flt_line[3]);
							map.put("FLT_LOCATION2", flt_line[4]);
						}
					}
					//20180516 중국 세관 OCI 추가로 국내 OCI INDEX 부분 수정
				}else if(fltline.equals("") && (map.get("BODY").toString().indexOf("OCI/KR/EXP/") !=-1 || map.get("BODY").toString().indexOf("OCI/KR/IMP/") !=-1)){
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "FLT LINE MANDATORY");
						map.put("ERRORMSG_KOR", "FLAG M or I 일 경우 편명 정보 입력 필수");
					}
					flag = false;
				}
			}catch(Exception e){
				e.printStackTrace();
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL TXT FLT LINE ERROR");
					map.put("ERRORMSG_KOR", "편명 정보 입력 오류");
				}
				flag = false;
			}

			//DECONSOL LINE
			try{

				if(!dcsline.equals("")){
					if (Pattern.matches("[/]{1}[.DCS.]{5}[A-Z0-9\\s\\-\\.]{1,60}", dcsline)) {
						if(dcsline.indexOf("(") !=-1 || dcsline.indexOf(")") !=-1){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "DCS LINE INVALID CHARACTER INPUT ERROR");
								map.put("ERRORMSG_KOR", "");
							}
							flag = false;
						}else{
							String dcs_name = dcsline.replace("/.DCS.", "");

							if (dcs_name.replace(" ", "").length() == 0 || dcs_name.replace(".", "").length() == 0|| dcs_name.replace("-", "").length() == 0) {
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "DCS INSERT ERROR");
									map.put("ERRORMSG_KOR", "DECONSOL NAME 항목 입력 오류");
								}
								flag = false;
							}else{
								map.put("DECONSOL_NAME", dcs_name);
							}
						}
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "DCS LINE ERROR");
							map.put("ERRORMSG_KOR", "DECONSOL NAME 항목 입력 오류");
						}
						flag = false;
					}
				}else if(dcsline.equals("") && map.get("BODY").toString().indexOf("OCI/KR/IMP/") !=-1){
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "DCS LINE MANDATORY");
						map.put("ERRORMSG_KOR", "수입 건 DCS 정보 입력 필수");
					}
					flag = false;
				}

			}catch(Exception e){
				e.printStackTrace();
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL TEXT DCS LINE ERROR");
					map.put("ERRORMSG_KOR", "TEXT 라인 입력 오류");
				}
				flag = false;
			}

			//SNM LINE(송하인 이름)
			// 첫번째 SNM 라인 60자
			// 두번째 SNM 라인 40자
			try{

				if(!snmline.equals("")){

					String[] snm_line = snmline.split("/.SNM.");

					if(snm_line.length >=2){

						String snm_name =  CommonUtil.nullChk(snm_line[1]);

						if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,60}", snm_name)){

							if (snm_name.replace(" ", "").length() == 0 || snm_name.replace(".", "").length() == 0|| snm_name.replace("-", "").length() == 0) {
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "SNM INSERT ERROR");
									map.put("ERRORMSG_KOR", "SNM NAME 송하인 이름 항목 입력 오류");
								}
								flag = false;
							}else{
								map.put("SNM_NAME", snm_name.trim());
							}

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "SNM LINE ERROR");
								map.put("ERRORMSG_KOR", "SNM LINE 60자 초과 항목 입력 오류");
							}
							flag = false;
						}

						if(snm_line.length == 3){

							String snm_name2 = CommonUtil.nullChk(snm_line[2]);

							if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,40}", snm_name2)){

								if (snm_name2.replace(" ", "").length() == 0 || snm_name2.replace(".", "").length() == 0|| snm_name2.replace("-", "").length() == 0) {
									if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
										map.put("ERRORMSG", "SNM INSERT ERROR");
										map.put("ERRORMSG_KOR", "SNM NAME 송하인 이름 항목 입력 오류");
									}
									flag = false;
								}else{
									map.put("SNM_NAME", map.get("SNM_NAME") + snm_name2.trim());
								}

							}else{
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "SNM LINE ERROR");
									map.put("ERRORMSG_KOR", "SNM LINE 40자 초과 항목 입력 오류");
								}
								flag = false;
							}
						}
					}


				}
			}catch(Exception e){
				e.printStackTrace();
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL TEXT SNM LINE ERROR");
					map.put("ERRORMSG_KOR", "TEXT SNM 라인 입력 오류");
				}
				flag = false;
			}

			//SCC LINE(송하인 도시코드)
			try{

				if(!sccline.equals("")){
					if(Pattern.matches("[/]{1}(.SCC.)[A-Z0-9\\s\\-\\.]{5}", sccline)){
						String scc_code = sccline.replace("/.SCC.","");

						if (scc_code.replace(" ", "").length() == 0 || scc_code.replace(".", "").length() == 0|| scc_code.replace("-", "").length() == 0) {
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "SCC INSERT ERROR");
								map.put("ERRORMSG_KOR", "SCC CODE 송하인 식별자 코드 항목 입력 오류");
							}
							flag = false;
						}else{
							map.put("SCC_CODE", scc_code);
						}

					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "SCC LINE ERROR");
							map.put("ERRORMSG_KOR", "SCC LINE 항목 입력 오류");
						}
						flag = false;
					}
				}

			}catch(Exception e){
				e.printStackTrace();
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL TEXT SCC LINE ERROR");
					map.put("ERRORMSG_KOR", "TEXT SCC 라인 입력 오류");
				}
				flag = false;
			}

			//SAR LINE(송하인 주소)
			//60/60/30 자리 총 3번 반복 가능
			try{

				if(!sarline.equals("")){

					String [] sar_line = sarline.split("/.SAR.");

					if(sar_line.length >= 2){
						String sar_addr = CommonUtil.nullChk(sar_line[1]).trim();

						if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,60}", sar_addr)){

							if (sar_addr.replace(" ", "").length() == 0 || sar_addr.replace(".", "").length() == 0|| sar_addr.replace("-", "").length() == 0) {
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "SAR INSERT ERROR");
									map.put("ERRORMSG_KOR", "SAR ADDRESS 송하인 주소 항목 입력 오류");
								}
								flag = false;
							}else{
								map.put("SAR_ADDR", sar_addr);
							}

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "SAR LINE ERROR");
								map.put("ERRORMSG_KOR", "SAR LINE 60자리 항목 입력 오류");
							}
							flag = false;
						}

						if(sar_line.length >=3){

							String sar_addr2 = CommonUtil.nullChk(sar_line[2]).trim();

							if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,60}", sar_addr2)){

								if (sar_addr2.replace(" ", "").length() == 0 || sar_addr2.replace(".", "").length() == 0|| sar_addr2.replace("-", "").length() == 0) {
									if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
										map.put("ERRORMSG", "SAR INSERT ERROR");
										map.put("ERRORMSG_KOR", "SAR ADDRESS 송하인 주소 항목 입력 오류");
									}
									flag = false;
								}else{
									map.put("SAR_ADDR", sar_addr + sar_addr2);
								}

							}else{
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "SAR LINE ERROR");
									map.put("ERRORMSG_KOR", "SAR LINE 60자리  항목 입력 오류");
								}
								flag = false;
							}

							if(sar_line.length ==4){

								String sar_addr3 = CommonUtil.nullChk(sar_line[3]).trim();

								if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,30}", sar_addr3)){

									if (sar_addr3.replace(" ", "").length() == 0 || sar_addr3.replace(".", "").length() == 0|| sar_addr3.replace("-", "").length() == 0) {
										if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
											map.put("ERRORMSG", "SAR INSERT ERROR");
											map.put("ERRORMSG_KOR", "SAR ADDRESS 송하인 주소 항목 입력 오류");
										}
										flag = false;
									}else{
										map.put("SAR_ADDR", map.get("SAR_ADDR")+ sar_addr3);
									}

								}else{
									if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
										map.put("ERRORMSG", "SAR LINE ERROR");
										map.put("ERRORMSG_KOR", "SAR LINE 30 자리 항목 입력 오류");
									}
									flag = false;
								}
							}
						}
					}
				}

			}catch(Exception e){
				e.printStackTrace();
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL TEXT SAR LINE ERROR");
					map.put("ERRORMSG_KOR", "TEXT SAR 라인 입력 오류");
				}
				flag = false;
			}

			//CNM LINE(수하인 이름)
			// 첫번째 SNM 라인 60자
			// 두번째 SNM 라인 40자
			try{

				if(!cnmline.equals("")){
					String[] cnm_line = cnmline.split("/.CNM.");

					if(cnm_line.length >= 2){

						String cnm_name =  CommonUtil.nullChk(cnm_line[1]).trim();

						if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,60}", cnm_name)){

							if (cnm_name.replace(" ", "").length() == 0 || cnm_name.replace(".", "").length() == 0|| cnm_name.replace("-", "").length() == 0) {
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "CNM INSERT ERROR");
									map.put("ERRORMSG_KOR", "CNM NAME 수하인 이름 항목 입력 오류");
								}
								flag = false;
							}else{
								map.put("CNM_NAME", cnm_name);
							}

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "CNM LINE ERROR");
								map.put("ERRORMSG_KOR", "CNM LINE 60자 항목 입력 오류");
							}
							flag = false;
						}

						if(cnm_line.length ==3){
							String cnm_name2 =  CommonUtil.nullChk(cnm_line[2]).trim();

							if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,40}", cnm_name2)){

								if (cnm_name2.replace(" ", "").length() == 0 || cnm_name2.replace(".", "").length() == 0|| cnm_name2.replace("-", "").length() == 0) {
									if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
										map.put("ERRORMSG", "CNM INSERT ERROR");
										map.put("ERRORMSG_KOR", "CNM NAME 수하인 이름 항목 입력 오류");
									}
									flag = false;
								}else{
									map.put("CNM_NAME", cnm_name + cnm_name2);
								}

							}else{
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "CNM LINE ERROR");
									map.put("ERRORMSG_KOR", "CNM LINE 40자 항목 입력 오류");
								}
								flag = false;
							}

						}
					}
				}
			}catch(Exception e){
					e.printStackTrace();
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "FHL TEXT CNM LINE ERROR");
						map.put("ERRORMSG_KOR", "TEXT CNM 라인 입력 오류");
					}
					flag = false;
			}

			//CCC LINE(수하인 도시코드)
			try{

				if(!cccline.equals("")){
					if(Pattern.matches("[/]{1}(.CCC.)[A-Z0-9\\s\\-\\.]{5}", cccline)){
						String ccc_code = cccline.replace("/.CCC.","");

						if (ccc_code.replace(" ", "").length() == 0 || ccc_code.replace(".", "").length() == 0|| ccc_code.replace("-", "").length() == 0) {
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "CCC INSERT ERROR");
								map.put("ERRORMSG_KOR", "CCC NAME 수하인 이름 항목 입력 오류");
							}
							flag = false;
						}else{
							map.put("CCC_CODE", ccc_code);
						}

					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "CCC LINE ERROR");
							map.put("ERRORMSG_KOR", "CCC LINE 항목 입력 오류");
						}
						flag = false;
					}
				}

			}catch(Exception e){
				e.printStackTrace();
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL TEXT CCC LINE ERROR");
					map.put("ERRORMSG_KOR", "TEXT CCC 라인 입력 오류");
				}
				flag = false;
			}

			//SAR LINE(송하인 주소)
			//60/60/30 자리 총 3번 반복 가능
			try{

				if(!carline.equals("")){

					String [] car_line = carline.split("/.CAR.");

					if(car_line.length >= 2){
						String car_addr = CommonUtil.nullChk(car_line[1]).trim();

						if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,60}", car_addr)){

							if (car_addr.replace(" ", "").length() == 0 || car_addr.replace(".", "").length() == 0|| car_addr.replace("-", "").length() == 0) {
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "CAR INSERT ERROR");
									map.put("ERRORMSG_KOR", "CAR ADDRESS 송하인 주소 항목 입력 오류");
								}
								flag = false;
							}else{
								map.put("CAR_ADDR", car_addr);
							}

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "CAR LINE ERROR");
								map.put("ERRORMSG_KOR", "CAR LINE 60자 항목 입력 오류");
							}
							flag = false;
						}

						if(car_line.length >=3){

							String car_addr2 = CommonUtil.nullChk(car_line[2]).trim();

							if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,60}", car_addr2)){

								if (car_addr2.replace(" ", "").length() == 0 || car_addr2.replace(".", "").length() == 0|| car_addr2.replace("-", "").length() == 0) {
									if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
										map.put("ERRORMSG", "CAR INSERT ERROR");
										map.put("ERRORMSG_KOR", "CAR ADDRESS 송하인 주소 항목 입력 오류");
									}
									flag = false;
								}else{
									map.put("CAR_ADDR", car_addr + car_addr2);
								}

							}else{
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "CAR LINE ERROR");
									map.put("ERRORMSG_KOR", "CAR LINE 60자 항목 입력 오류");
								}
								flag = false;
							}

							if(car_line.length ==4){

								String car_addr3 = CommonUtil.nullChk(car_line[3]).trim();

								if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,30}", car_addr3)){

									if (car_addr3.replace(" ", "").length() == 0 || car_addr3.replace(".", "").length() == 0|| car_addr3.replace("-", "").length() == 0) {
										if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
											map.put("ERRORMSG", "CAR INSERT ERROR");
											map.put("ERRORMSG_KOR", "CAR ADDRESS 송하인 주소 항목 입력 오류");
										}
										flag = false;
									}else{
										map.put("CAR_ADDR", map.get("CAR_ADDR")+ car_addr3);
									}

								}else{
									if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
										map.put("ERRORMSG", "CAR LINE ERROR");
										map.put("ERRORMSG_KOR", "CAR LINE 30자 항목 입력 오류");
									}
									flag = false;
								}
							}
						}
					}
				}

			}catch(Exception e){
				e.printStackTrace();
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL TEXT CAR LINE ERROR");
					map.put("ERRORMSG_KOR", "TEXT CAR 라인 입력 오류");
				}
				flag = false;
			}

		}else{
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "FHL TXT LINE ERROR");
				map.put("ERRORMSG_KOR", "품목 편명 정보 입력 오류");
			}
			flag = false;
		}
		return flag;
	}

	//HTS 항목
	public boolean HTSCheck(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		boolean flag = true;
		try{
			if(!Pattern.matches("(HTS(/[A-Z0-9]{6,18}[\r\n]*){1,9})?", map.get("HTS_LINE").toString())){
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL HTS LINE ERROR");
					map.put("ERRORMSG_KOR", "HTS LINE 에러");
				}
				flag = false;
			}else{
				String [] hts_line = map.get("HTS_LINE").toString().replace("HTS/", "").replace("/", "").split("\r\n");
				if(hts_line.length>=1 && hts_line.length<=9){
					if(hts_line.length==1){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
					}else if(hts_line.length==2){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
						map.put("HARMONISED1", CommonUtil.nullChk(hts_line[1]));
					}else if(hts_line.length==3){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
						map.put("HARMONISED1", CommonUtil.nullChk(hts_line[1]));
						map.put("HARMONISED2", CommonUtil.nullChk(hts_line[2]));
					}else if(hts_line.length==4){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
						map.put("HARMONISED1", CommonUtil.nullChk(hts_line[1]));
						map.put("HARMONISED2", CommonUtil.nullChk(hts_line[2]));
						map.put("HARMONISED3", CommonUtil.nullChk(hts_line[3]));
					}else if(hts_line.length==5){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
						map.put("HARMONISED1", CommonUtil.nullChk(hts_line[1]));
						map.put("HARMONISED2", CommonUtil.nullChk(hts_line[2]));
						map.put("HARMONISED3", CommonUtil.nullChk(hts_line[3]));
						map.put("HARMONISED4", CommonUtil.nullChk(hts_line[4]));
					}else if(hts_line.length==6){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
						map.put("HARMONISED1", CommonUtil.nullChk(hts_line[1]));
						map.put("HARMONISED2", CommonUtil.nullChk(hts_line[2]));
						map.put("HARMONISED3", CommonUtil.nullChk(hts_line[3]));
						map.put("HARMONISED4", CommonUtil.nullChk(hts_line[4]));
						map.put("HARMONISED5", CommonUtil.nullChk(hts_line[5]));
					}else if(hts_line.length==7){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
						map.put("HARMONISED1", CommonUtil.nullChk(hts_line[1]));
						map.put("HARMONISED2", CommonUtil.nullChk(hts_line[2]));
						map.put("HARMONISED3", CommonUtil.nullChk(hts_line[3]));
						map.put("HARMONISED4", CommonUtil.nullChk(hts_line[4]));
						map.put("HARMONISED5", CommonUtil.nullChk(hts_line[5]));
						map.put("HARMONISED6", CommonUtil.nullChk(hts_line[6]));
					}else if(hts_line.length==8){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
						map.put("HARMONISED1", CommonUtil.nullChk(hts_line[1]));
						map.put("HARMONISED2", CommonUtil.nullChk(hts_line[2]));
						map.put("HARMONISED3", CommonUtil.nullChk(hts_line[3]));
						map.put("HARMONISED4", CommonUtil.nullChk(hts_line[4]));
						map.put("HARMONISED5", CommonUtil.nullChk(hts_line[5]));
						map.put("HARMONISED6", CommonUtil.nullChk(hts_line[6]));
						map.put("HARMONISED7", CommonUtil.nullChk(hts_line[7]));
					}else if(hts_line.length==9){
						map.put("HARMONISED", CommonUtil.nullChk(hts_line[0]));
						map.put("HARMONISED1", CommonUtil.nullChk(hts_line[1]));
						map.put("HARMONISED2", CommonUtil.nullChk(hts_line[2]));
						map.put("HARMONISED3", CommonUtil.nullChk(hts_line[3]));
						map.put("HARMONISED4", CommonUtil.nullChk(hts_line[4]));
						map.put("HARMONISED5", CommonUtil.nullChk(hts_line[5]));
						map.put("HARMONISED6", CommonUtil.nullChk(hts_line[6]));
						map.put("HARMONISED7", CommonUtil.nullChk(hts_line[7]));
						map.put("HARMONISED8", CommonUtil.nullChk(hts_line[8]));
					}

				}
			}

		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "FHL HTS LINE ERROR");
				map.put("ERRORMSG_KOR", "HTS LINE 에러");
			}
			flag = false;
		}
		return flag;
	}

	//OCI 항목
	public boolean OCICheck(HashMap<String, Object> map) throws Exception{
		// TODO Auto-generated method stub

		boolean flag = true;
		int result = 0;
		try{
			String oci_chk = map.get("OCI_LINE_CHK_A").toString().replace("OCI", "");
			String [] First_OCI = map.get("OCI_LINE").toString().replace("OCI", "").split("\r\n");
			String lineStr = "";
			String EPN_Check = "";
			//20161206 최재준 추가(OCI SEQ 중복 값 체크)
			String SEQ_Check = "";
			String SEQ_Check2 = "";
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

			if(First_OCI.length == 1){
				//수입
				for(int i=0; i<First_OCI.length; i++){
					HashMap ocimap = new HashMap();
					ocimap.put("LINE", CommonUtil.nullChk(First_OCI[i]));
					list.add(ocimap);
					map.put("LINE", ocimap.get("LINE"));
				}

			}else{
				//수출
				for(int i=0; i<First_OCI.length; i++){
					map.put("OCI_LINE"+i,First_OCI[i]);
					String str = map.get("OCI_LINE"+i).toString();
					HashMap ocimap = new HashMap();
					if((i%2) == 0){
						lineStr = str;
					}else{
						lineStr += str;
						ocimap.put("LINE", lineStr);
						list.add(ocimap);
						map.put("LINE", ocimap.get("LINE"));
					}
				}
			}

			for(int i=0; i<list.size();i++){
				HashMap returnMap = new HashMap();
				returnMap = (HashMap)list.get(i);
				map.put("LINE",returnMap.get("LINE"));
				String [] oci_line = map.get("LINE").toString().split("/");
				if(Pattern.matches("[KR]{2}", oci_line[1])){
					map.put("OCI_ISO_CODE", oci_line[1].toString());
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "OCI ISO CODE INPUT ERROR");
						map.put("ERRORMSG_KOR", "OCI ISO CODE ERROR");
					}
					flag = false;
				}
				if(Pattern.matches("EXP|IMP", oci_line[2])){
					map.put("OCI_TYPE", oci_line[2].toString());
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "OCI TYPE INPUT ERROR");
						map.put("ERRORMSG_KOR", "OCI TYPE ERROR");
					}
					flag = false;
				}

				if(Pattern.matches("[MIAE]{1}", oci_line[3])){
					map.put("OCI_FLAG", oci_line[3].toString());
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "OCI FLAG INPUT ERROR");
						map.put("ERRORMSG_KOR", "OCI FLAG ERROR");
					}
					flag = false;
				}

				if(CommonUtil.nullChk(map.get("OCI_TYPE")).equals("EXP")){
					String[] OCILine1 = oci_line[oci_line.length-5].split("-");
					map.put("OCI_SUP_INFO1", oci_line[oci_line.length-5]);

					if(OCILine1.length == 2){
						if(Pattern.matches("[ER]{1}[0-9]{3}", OCILine1[0])){
							map.put("OCI_CARGO_TYPE",CommonUtil.nullChk(OCILine1[0].substring(0, 1), ""));
							map.put("OCI_SEQ",Integer.parseInt(CommonUtil.nullChk(OCILine1[0].substring(1),"")));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI CARGO TYPE INPUT ERROR");
								map.put("ERRORMSG_KOR", "화물타입 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9A-Z]{3,19}",OCILine1[1])){
							map.put("OCI_EPN_NO",CommonUtil.nullChk(OCILine1[1],""));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN TYPE NUMBER INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출 신고 번호 입력 오류");
							}
							flag = false;
						}

						map.put("OCI_EPN_SPACK_CODE", "");
						map.put("OCI_EPN_SPACK_PC","0");
						map.put("OCI_SHIP_CODE", "");
					}else if(OCILine1.length == 3){
						if(Pattern.matches("[ER]{1}[0-9]{3}", OCILine1[0])){
							map.put("OCI_CARGO_TYPE",CommonUtil.nullChk(OCILine1[0].substring(0, 1), ""));
							map.put("OCI_SEQ",Integer.parseInt(CommonUtil.nullChk(OCILine1[0].substring(1),"")));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI CARGO TYPE INPUT ERROR");
								map.put("ERRORMSG_KOR", "화물타입 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9A-Z]{3,19}",OCILine1[1])){
							map.put("OCI_EPN_NO",CommonUtil.nullChk(OCILine1[1],""));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN TYPE NUMBER INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출 신고 번호 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[A-Z]{0,1}",OCILine1[2])){
							map.put("OCI_EPN_SPACK_CODE",CommonUtil.nullChk(OCILine1[2],""));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI SPACK CODE INPUT ERROR");
								map.put("ERRORMSG_KOR", "동시포장코드 입력 오류");
							}
							flag = false;
						}
						map.put("OCI_EPN_SPACK_PC","0");
						map.put("OCI_SHIP_CODE", "");
					}else{

						if(OCILine1.length <= 1){

							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN NUMBER INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출 신고 번호 입력 오류");
							}
							flag = false;
						}else{

							if(Pattern.matches("[ER]{1}[0-9]{3}", OCILine1[0])){
								map.put("OCI_CARGO_TYPE",CommonUtil.nullChk(OCILine1[0].substring(0, 1), ""));
								map.put("OCI_SEQ",Integer.parseInt(CommonUtil.nullChk(OCILine1[0].substring(1),"")));
							}else{
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "OCI CARGO TYPE INPUT ERROR");
									map.put("ERRORMSG_KOR", "화물타입 입력 오류");
								}
								flag = false;
							}


							if(Pattern.matches("[0-9A-Z]{3,19}",OCILine1[1])){
								map.put("OCI_EPN_NO",CommonUtil.nullChk(OCILine1[1],""));
							}else{
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "OCI EPN NUMBER INPUT ERROR");
									map.put("ERRORMSG_KOR", "수출 신고 번호 입력 오류");
								}
								flag = false;
							}

							if(Pattern.matches("[A-Z]{0,1}",OCILine1[2])){
								map.put("OCI_EPN_SPACK_CODE",CommonUtil.nullChk(OCILine1[2],""));
							}else{
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "OCI SPACK CODE INPUT ERROR");
									map.put("ERRORMSG_KOR", "동시포장코드 입력 오류");
								}
								flag = false;
							}

							if(OCILine1[3].trim().equals(null)||OCILine1[3].trim().length()==0){
								map.put("OCI_EPN_SPACK_PC","0");
							}else{
								if(Pattern.matches("[0-9.]{0,4}",OCILine1[3])){
									map.put("OCI_EPN_SPACK_PC",Integer.parseInt(CommonUtil.nullChk(OCILine1[3].trim(),"")));
								}else{
									if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
										map.put("ERRORMSG", "OCI SPACK PC INPUT ERROR");
										map.put("ERRORMSG_KOR", "동시포장 갯수 입력 오류");
									}
									flag = false;
								}
							}

						if(OCILine1.length > 4){
							if(Pattern.matches("[A-Z0-9]{2}",OCILine1[4])){
								map.put("OCI_SHIP_CODE", OCILine1[4]);
							}else{
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "OCI SHIPPER TARE CODE INPUT ERROR");
									map.put("ERRORMSG_KOR", "선적포장코드 입력 오류");
								}
								flag = false;
							}
						}
						if(OCILine1.length >= 6){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI INPUT ERROR");
								map.put("ERRORMSG_KOR", "OCI LINE 입력 횟수 초과");
							}
							flag = false;
						}
					}
				}

						//20161206 최재준 추가
						//OCI SEQ 중복 체크(OCI 두번째 라인)
						if(SEQ_Check.indexOf(","+CommonUtil.nullChk(OCILine1[0].substring(1).toString(),"")+",") !=-1){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN SEQ OVERLAP ERROR");
								map.put("ERRORMSG_KOR", "EPN SEQ 중복 오류");
							}
							flag = false;
						}else{
							SEQ_Check += ","+CommonUtil.nullChk(OCILine1[0].substring(1).toString(),"")+",";
						}

						//if(OCILine1.length < 1){
							//EPN NO 중복 체크
							if(EPN_Check.indexOf(","+CommonUtil.nullChk(OCILine1[1],"")+",")!=-1){
								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "OCI EPN NO OVERLAP ERROR");
									map.put("ERRORMSG_KOR", "EPN NO 중복 오류");
								}
								flag = false;
							}else{
								EPN_Check += ","+CommonUtil.nullChk(OCILine1[1],"")+",";
							}
						//}

					String [] oci_line2 = map.get("LINE").toString().split("/");
					String[] OCILine2 = oci_line2[oci_line2.length-1].split("-");
					map.put("OCI_SUP_INFO2", oci_line2[oci_line2.length-1]);

					if(OCILine2.length==3){
						if(Pattern.matches("[S]{1}[0-9]{3}", OCILine2[0])){

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EVEN CARGO TYPE INPUT ERROR");
								map.put("ERRORMSG_KOR", "화물 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9]{1,10}",OCILine2[1])){
							map.put("OCI_EPN_PC",Integer.parseInt(CommonUtil.nullChk(OCILine2[1],"")));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN PC INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출신고 수량 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9]{1,13}([.]{1}[0-9]{1,3})?",OCILine2[2])){
							map.put("OCI_EPN_WT",Float.valueOf(CommonUtil.nullChk(OCILine2[2].trim(),"")).floatValue());
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN WT INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출신고 중량 입력 오류");
							}
							flag = false;
						}
						map.put("OCI_EPN_SPLIT_YN","N");
						map.put("OCI_EPN_SPLIT_SEQ",0);
					}else if(OCILine2.length==4){
						if(Pattern.matches("[S]{1}[0-9]{3}", OCILine2[0])){

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EVEN CARGO TYPE INPUT ERROR");
								map.put("ERRORMSG_KOR", "화물 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9]{1,10}",OCILine2[1])){
							map.put("OCI_EPN_PC",Integer.parseInt(CommonUtil.nullChk(OCILine2[1],"")));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN PC INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출신고 수량 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9]{1,13}([.]{1}[0-9]{1,3})?",OCILine2[2])){
							map.put("OCI_EPN_WT",Float.valueOf(CommonUtil.nullChk(OCILine2[2].trim(),"")).floatValue());
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN WT INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출신고 중량 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[A-Z]{0,1}",OCILine2[3])){
							map.put("OCI_EPN_SPLIT_YN",CommonUtil.nullChk(OCILine2[3],""));

							if(OCILine2[3].equals("N")){
								map.put("OCI_EPN_SPLIT_SEQ",0);
							}

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI SPLIT CODE INPUT ERROR");
								map.put("ERRORMSG_KOR", "분할 여부 코드  입력 오류");
							}
							flag = false;
						}

					}else if(OCILine2.length==5){

						if(Pattern.matches("[S]{1}[0-9]{3}", OCILine2[0])){

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EVEN CARGO TYPE INPUT ERROR");
								map.put("ERRORMSG_KOR", "화물 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9]{1,10}",OCILine2[1])){
							map.put("OCI_EPN_PC",Integer.parseInt(CommonUtil.nullChk(OCILine2[1],"")));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN PC INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출신고 수량 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9]{1,13}([.]{1}[0-9]{1,3})?",OCILine2[2])){
							map.put("OCI_EPN_WT",Float.valueOf(CommonUtil.nullChk(OCILine2[2].trim(),"")).floatValue());
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI EPN WT INPUT ERROR");
								map.put("ERRORMSG_KOR", "수출신고 중량 입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[A-Z]{0,1}",OCILine2[3])){
							map.put("OCI_EPN_SPLIT_YN",CommonUtil.nullChk(OCILine2[3],""));

							if(OCILine2[3].equals("N")){
								map.put("OCI_EPN_SPLIT_SEQ",0);
							}

						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI SPLIT CODE INPUT ERROR");
								map.put("ERRORMSG_KOR", "분할 여부 코드  입력 오류");
							}
							flag = false;
						}
						if(Pattern.matches("[0-9]{0,1}",OCILine2[4])){
							map.put("OCI_EPN_SPLIT_SEQ",Integer.parseInt(CommonUtil.nullChk(OCILine2[4].trim(),"")));
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI SPLIT SEQ INPUT ERROR");
								map.put("ERRORMSG_KOR", "분할 차수 코드  입력 오류");
							}
							flag = false;
						}
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "OCI INPUT ERROR");
							map.put("ERRORMSG_KOR", "OCI LINE2 입력 회수 초과");
						}
						flag = false;
					}

					//20161206 최재준 추가
					//OCI SEQ 중복 체크(OCI 두번째 라인)
					if(SEQ_Check2.indexOf(","+CommonUtil.nullChk(OCILine2[0].substring(1).toString(),"")+",") !=-1){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "OCI EPN SEQ OVERLAP ERROR");
							map.put("ERRORMSG_KOR", "EPN SEQ 2번째 라인 중복 오류");
						}
						flag = false;
					}else{
						SEQ_Check2 += ","+CommonUtil.nullChk(OCILine2[0].substring(1).toString(),"")+",";
					}
					//EPN 값 INSERT
					/*if(map.get("OCI_CHECK").equals("P")){
						result = dbinserttrace.ikamsExpEpnInsert(map);
						logger.info(" === IKAMS EXP EPN INSERT === ");
					}*/

				}else if(CommonUtil.nullChk(map.get("OCI_TYPE")).equals("IMP")){
					String org_code = "ICN,SEL,PUS,GMP";

					if(oci_line.length <= 5){
						if((map.get("OCI_FLAG").equals("M") && Pattern.matches("[ITP]{1}[0-9]{3}\\-[A-Z]{0,2}", oci_line[4]))
						|| (map.get("OCI_FLAG").equals("M") && Pattern.matches("[ITP]{1}[0-9]{4}\\-[A-Z]{0,2}", oci_line[4]))
						||(map.get("OCI_FLAG").equals("I") && Pattern.matches("[ITP]{1}[0-9]{3}\\-[A-Z]{0,2}", oci_line[4]))
						|| (map.get("OCI_FLAG").equals("I") && Pattern.matches("[ITP]{1}[0-9]{4}\\-[A-Z]{0,2}", oci_line[4]))){
							String[] ociIMPline = oci_line[4].split("-");
							map.put("OCI_SUP_INFO1", oci_line[4]);
							map.put("OCI_SEQ", CommonUtil.nullChk(ociIMPline[0].substring(1),""));
							map.put("OCI_HSN", CommonUtil.nullChk(ociIMPline[0].substring(1),""));

							if(ociIMPline[0].substring(0,1).equals("P")){
								map.put("OCI_CARGO_TYPE", "T");
								map.put("OCI_GOOD_STS_CODE", "3");
							}else{
								map.put("OCI_CARGO_TYPE", CommonUtil.nullChk(ociIMPline[0].substring(0,1),""));
								map.put("OCI_GOOD_STS_CODE", "");
							}

							if(ociIMPline.length==2){
								map.put("OCI_GOODSCODE", CommonUtil.nullChk(ociIMPline[1],""));
							}else{
								map.put("OCI_GOODSCODE", "");
							}

							if(CommonUtil.nullChk(map.get("OCI_HSN")).equals("0000")){

								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
									map.put("ERRORMSG", "OCI IMP HSN INPUT ERROR");
									map.put("ERRORMSG_KOR", "수입 OCI HSN 항목 입력 오류");
								}
								flag = false;
							}
						}else{
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "OCI IMP INPUT ERROR");
								map.put("ERRORMSG_KOR", "수입 OCI 항목 입력 오류");
							}
							flag = false;
						}
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "OCI IMP INPUT ERROR");
							map.put("ERRORMSG_KOR", "수입 OCI 항목 입력 오류");
						}
						flag = false;
					}

					//20170629 최재준 추가
					//환적(T) 일때 MAWB ORIGIN만 체크
					if(CommonUtil.nullChk(map.get("OCI_CARGO_TYPE")).equals("T")){

						if(org_code.indexOf(map.get("M_ORG").toString()) !=-1){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "IMP TRANS FHL ORG INPUT ERROR");
								map.put("ERRORMSG_KOR", "수입 환적 FHL ORG 입력 오류");
							}
							flag = false;
						}
					}else{

						if(org_code.indexOf(map.get("M_ORG").toString()) !=-1 || org_code.indexOf(map.get("H_ORG").toString()) !=-1){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "IMP FHL ORG INPUT ERROR");
								map.put("ERRORMSG_KOR", "수입 FHL ORG 입력 오류");
							}
							flag = false;
						}
					}
				}
				if(map.get("OCI_CHECK").equals("C") && (map.get("OCI_FLAG").equals("A") || map.get("OCI_FLAG").equals("E") || map.get("OCI_FLAG").equals("M"))){
					result = commonSql.insertFHLOCI(map);
				}
			}

			//20180525 중국세관 용 OCI 오류 체크 정규식 추가
			if(Pattern.matches("(\\/(KR)\\/(EXP||IMP)\\/[A-Z]{1}\\/[A-Z0-9\\s\\-\\.]{1,35}\r\n)+", oci_chk)){

				logger.info(" === OCI A Validation Check Success === ");
			}else{

				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL OCI LINE INPUT ERROR");
					map.put("ERRORMSG_KOR", "");
				}
				flag = false;
			}

		}catch(SQLException e){
			throw new SQLException();
		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "FHL OCI LINE ERROR");
				map.put("ERRORMSG_KOR", "");
			}
			flag = false;
		}
		return flag;
	}

	public boolean OCICheck_Origin(HashMap<String, Object> map) throws Exception{
		// TODO Auto-generated method stub

		boolean flag = true;
		try{
			map.put("OCI_CIMP_LINE", map.get("OCI_LINE"));
			if(map.get("OCI_LINE").toString().startsWith("OCI/")){
				map.put("OCI_LINE2", map.get("OCI_LINE").toString().replace("OCI/", "/"));
			}

			if(!Pattern.matches("((/)([A-Z]{2})?(/)([A-Z]{3})?(/)([A-Z]{1,2})?(/)([A-Z0-9\\s\\-\\.]{1,35})?(\r\n)?)+", map.get("OCI_LINE2").toString())){

				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "FHL OCI LINE ERROR");
					map.put("ERRORMSG_KOR", "");
				}
				flag = false;
			}
			/*String[] oci_line = map.get("OCI_LINE2").toString().split("\r\n");

			for(int i=0; i<oci_line.length; i++){

				String[] oci_line2 = oci_line[i].trim().split("/");

				if(CommonUtil.nullChk(oci_line2[4],"").replace(" ", "").length()==0){
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "FHL OCI LINE ERROR");
						map.put("ERRORMSG_KOR", "");
					}
					flag = false;
				}
			}	*/
		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "FHL OCI LINE ERROR");
				map.put("ERRORMSG_KOR", "");
			}
			flag = false;
		}
		return flag;
	}

	// 20241105 FHL OCI CIMP 수정 - ACAS F/U
	public boolean OCICheck_Origin2(HashMap<String, Object> map) throws Exception{
		// OCI CIMP Validation 추가

		boolean flag = true;
		int result = 0;
		int oci_etc_seq =0;
		int j=0;
		try{
			String[] oci_etc_line = map.get("OCI_ETC_LINE").toString().split("\r\n");//oci_etc_line 개행별로 쪼개기

			for(int i=0; i<oci_etc_line.length; i++) {//oci_etc_line[i]를 /(slant) 별로 쪼개기 oci는 slant 4개 필수 항목
				oci_etc_seq=++j;

				map.put("OCI_ETC_SEQ",oci_etc_seq);//개행별로 SEQ 추가
				String[] oci_etc =	oci_etc_line[i].split("/");//oci_etc는 개행된 ocietc를 /별로 쪼개기

				if(oci_etc.length!=5) {//oci는 slant가 4개 필수, 4개가 아니면 위 for 문 자체를 끝내고 errormsg 호출
					//error 표시
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "OMITTED SLANT IN OCI");
						map.put("ERRORMSG_KOR", "OCI 한 개행에 / 4개 필수");
					}
					flag = false;
				}

				if ((oci_etc[1].length()==0||oci_etc[1].equals(""))
					&& (oci_etc[2].length()==0||oci_etc[2].equals(""))
					&& (oci_etc[3].length()==0||oci_etc[3].equals(""))) {
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG","FHL30 - At least one of Ref. 6.3., Ref. 6.5. or Ref. 6.7. must be included");
						map.put("ERRORMSG_KOR", "");
					}
					flag = false;
				}

				if(oci_etc[1].length()==0||oci_etc[1].equals("")) {
					map.put("OCI_ETC_ISO_CODE","");
				}else if(Pattern.matches("[A-Z]{2}",oci_etc[1])){
					map.put("OCI_ETC_ISO_CODE",oci_etc[1]);
				}else {
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG","OCI ISO CODE ERROR - "+oci_etc[1]);
						map.put("ERRORMSG_KOR", "");
					}
					flag = false;
				}

				if(oci_etc[2].length()==0||oci_etc[2].equals("")) {
					map.put("OCI_ETC_INFO_ID","");
				}else if(Pattern.matches("[A-Z]{3}",oci_etc[2])){
					map.put("OCI_ETC_INFO_ID",oci_etc[2]);
				}else {
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG","OCI INFO ID ERROR - "+oci_etc[2]);
						map.put("ERRORMSG_KOR", "");
					}
					flag = false;
				}

				if(oci_etc[3].length()==0||oci_etc[3].equals("")) {
					map.put("OCI_ETC_CUS_INFO_ID","");
				}else if(Pattern.matches("[A-Z]{1,2}",oci_etc[3])){
					map.put("OCI_ETC_CUS_INFO_ID",oci_etc[3]);
				}else {
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG","OCI CUS INFO ID ERROR - "+oci_etc[3]);
						map.put("ERRORMSG_KOR", "");
					}
					flag = false;
				}

				if(oci_etc[4].length()==0||oci_etc[4].equals("")) {//필수조건
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "OCI SUP INFO IS REQUIRED");
						map.put("ERRORMSG_KOR", "OCI 추가 세관 정보 입력 필수");
					}
					flag = false;
				}else if(Pattern.matches("[A-Z0-9\\s\\-\\.]{1,35}",oci_etc[4])){// \\s 는 스페이스
					map.put("OCI_ETC_SUP_INFO",oci_etc[4]);
				}else {
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "OCI SUP INFO ERROR - "+oci_etc[4]);
						map.put("ERRORMSG_KOR", "");
					}
					flag = false;
				}

				result = commonSql.insertOCIETC(map);//DB INSERT

				map.put("OCI_ETC_ISO_CODE","");//DB 넣은 후 초기화
				map.put("OCI_ETC_INFO_ID","");
				map.put("OCI_ETC_CUS_INFO_ID","");
				map.put("OCI_ETC_SUP_INFO","");
			}

		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "FWB OCI NONKR LINE ERROR");
				map.put("ERRORMSG_KOR", "");
			}
			flag = false;
		}
		return flag;
	}

	//20180515 OCI 중국 세관 SHP 항목 항목
	// 20241105 FHL OCI CIMP 수정 - ACAS F/U
	public boolean OCIShpCheck(HashMap<String, Object> map) throws Exception{
		// TODO Auto-generated method stub

		boolean flag = true;

		String oci_shp_chk = map.get("OCI_SHP_LINE").toString().replace("OCI/", "/");

		try{

//					if(Pattern.matches("((\\/[A-Z]{2}\\/(SHP)\\/(T)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//							+ "((\\/[A-Z]{2}\\/(SHP)\\/(KC)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//							+ "((\\/[A-Z]{2}\\/(SHP)\\/(U)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//							+ "((\\/[A-Z]{2}\\/(SHP)\\/(E)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?", oci_shp_chk)
//							|| Pattern.matches("((\\/[A-Z]{2}\\/(SHP)\\/(T)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//									+ "((\\/[A-Z]{2}\\/(SHP)\\/(CP)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//									+ "((\\/[A-Z]{2}\\/(SHP)\\/(CT)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//									+ "((\\/[A-Z]{2}\\/(SHP)\\/(E)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?", oci_shp_chk)){
					/*if(Pattern.matches("((\\/[A-Z]{2}\\/(SHP)\\/(T|KC|U|E|CP|CT)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n){1,4}", oci_shp_chk)){*/

					if (oci_shp_chk.endsWith("\r\n")){
						oci_shp_chk = oci_shp_chk.substring(0, oci_shp_chk.length() - "\r\n".length());
						map.put("OCI_SHP_LINE", oci_shp_chk);
					}
					String[] oci_shp_line = oci_shp_chk.toString().split("\r\n");

					for(int i=0; i<oci_shp_line.length; i++){

						String[] ocicn = oci_shp_line[i].split("/");

						map.put("OCI_SHP_CN", ocicn[1]);
						map.put("OCI_SHP", ocicn[2]);
						map.put("OCI_SHP_FLAG", ocicn[3]);

						/*if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "CN OCI SHP T NUM ERROR");
								map.put("ERRORMSG_KOR", "");
							}
							flag = false;
						}else{
							map.put("OCI_SHP_TXT"+map.get("OCI_SHP_SEQ"), ocicn[4]);
						}*/

						if(CommonUtil.nullChk(map.get("OCI_SHP_FLAG")).equals("T")){

							map.put("OCI_SHP_T_CN", ocicn[1]);
							map.put("OCI_SHP_T_FLAG", ocicn[3]);
							map.put("OCI_SHP_T_NUM", ocicn[4]);
//							if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//									map.put("ERRORMSG", "CN OCI SHP T NUM ERROR");
//									map.put("ERRORMSG_KOR", "");
//								}
//								flag = false;
//							}else{
//								map.put("OCI_SHP_T_NUM", ocicn[4]);
//							}

						}else if(CommonUtil.nullChk(map.get("OCI_SHP_FLAG")).equals("KC") || CommonUtil.nullChk(map.get("OCI_SHP_FLAG")).equals("CP")){

							map.put("OCI_SHP_KC_CN", ocicn[1]);
							map.put("OCI_SHP_KC_FLAG", ocicn[3]);
							map.put("OCI_SHP_KC_PERSON", ocicn[4]);
//							if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//									map.put("ERRORMSG", "CN OCI SHP KC PERSON ERROR");
//									map.put("ERRORMSG_KOR", "");
//								}
//								flag = false;
//							}else{
//								map.put("OCI_SHP_KC_PERSON", ocicn[4]);
//							}

						}else if(CommonUtil.nullChk(map.get("OCI_SHP_FLAG")).equals("U") || CommonUtil.nullChk(map.get("OCI_SHP_FLAG")).equals("CT")){

							map.put("OCI_SHP_U_CN", ocicn[1]);
							map.put("OCI_SHP_U_FLAG", ocicn[3]);
							map.put("OCI_SHP_U_NUM", ocicn[4]);
//							if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//									map.put("ERRORMSG", "CN OCI SHP U NUM ERROR");
//									map.put("ERRORMSG_KOR", "");
//								}
//								flag = false;
//							}else{
//								map.put("OCI_SHP_U_NUM", ocicn[4]);
//							}

						}else if(CommonUtil.nullChk(map.get("OCI_SHP_FLAG")).equals("E")){

							map.put("OCI_SHP_E_CN", ocicn[1]);
							map.put("OCI_SHP_E_FLAG", ocicn[3]);
							map.put("OCI_SHP_E_NUM", ocicn[4]);
//							if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//								if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//									map.put("ERRORMSG", "CN OCI SHP E NUM ERROR");
//									map.put("ERRORMSG_KOR", "");
//								}
//								flag = false;
//							}else{
//								map.put("OCI_SHP_E_NUM", ocicn[4]);
//							}
						}
					}
//				}else{
//					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//						map.put("ERRORMSG", "CN OCI SHP LINE INPUT ERROR");
//						map.put("ERRORMSG_KOR", "");
//					}
//					flag = false;
//				}
		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "CN OCI SHP EXCEPTION");
				map.put("ERRORMSG_KOR", "");
			}
			flag = false;
		}

		return flag;
	}

	//20180515 OCI 중국 세관 CNE 항목 항목
	//20180702 중국세관 CNE CP,CT 코드 추가
	// 20241105 FHL OCI CIMP 수정 - ACAS F/U
	public boolean OCICneCheck(HashMap<String, Object> map) throws Exception{
		// TODO Auto-generated method stub

		boolean flag = true;

		String oci_cne_chk = map.get("OCI_CNE_LINE").toString().replace("OCI/", "/");

		try{

//				if(Pattern.matches("((\\/[A-Z]{2}\\/(CNE)\\/(T)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//						+ "((\\/[A-Z]{2}\\/(CNE)\\/(KC)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//						+ "((\\/[A-Z]{2}\\/(CNE)\\/(U)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//						+ "((\\/[A-Z]{2}\\/(CNE)\\/(E)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?", oci_cne_chk)
//						|| Pattern.matches("((\\/[A-Z]{2}\\/(CNE)\\/(T)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//								+ "((\\/[A-Z]{2}\\/(CNE)\\/(CP)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//								+ "((\\/[A-Z]{2}\\/(CNE)\\/(CT)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//								+ "((\\/[A-Z]{2}\\/(CNE)\\/(E)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?", oci_cne_chk)){

				/*if(Pattern.matches("((\\/[A-Z]{2}\\/(CNE)\\/(T|KC|U|E|CP|CT)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n){1,4}", oci_cne_chk)){*/

				if (oci_cne_chk.endsWith("\r\n")){
					oci_cne_chk = oci_cne_chk.substring(0, oci_cne_chk.length() - "\r\n".length());
					map.put("OCI_CNE_LINE", oci_cne_chk);
				}
				String[] oci_cne_line = oci_cne_chk.toString().split("\r\n");

				for(int i=0; i<oci_cne_line.length; i++){

					String[] ocicn = oci_cne_line[i].split("/");

					map.put("OCI_CNE_CN", ocicn[1]);
					map.put("OCI_CNE", ocicn[2]);
					map.put("OCI_CNE_FLAG", ocicn[3]);

/*					if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "CN OCI CNE T NUM ERROR");
							map.put("ERRORMSG_KOR", "");
						}
						flag = false;
					}else{
						map.put("OCI_CNE_TXT", ocicn[4]);
					}*/

					if(CommonUtil.nullChk(map.get("OCI_CNE_FLAG")).equals("T")){

						map.put("OCI_CNE_T_CN", ocicn[1]);
						map.put("OCI_CNE_T_FLAG", ocicn[3]);
						map.put("OCI_CNE_T_NUM", ocicn[4]);
//						if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//								map.put("ERRORMSG", "CN OCI CNE T NUM ERROR");
//								map.put("ERRORMSG_KOR", "");
//							}
//							flag = false;
//						}else{
//							map.put("OCI_CNE_T_NUM", ocicn[4]);
//						}

					}else if(CommonUtil.nullChk(map.get("OCI_CNE_FLAG")).equals("KC")||CommonUtil.nullChk(map.get("OCI_CNE_FLAG")).equals("CP")){

						map.put("OCI_CNE_KC_CN", ocicn[1]);
						map.put("OCI_CNE_KC_FLAG", ocicn[3]);
						map.put("OCI_CNE_KC_PERSON", ocicn[4]);
//						if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//								map.put("ERRORMSG", "CN OCI CNE KC PERSON ERROR");
//								map.put("ERRORMSG_KOR", "");
//							}
//							flag = false;
//						}else{
//							map.put("OCI_CNE_KC_PERSON", ocicn[4]);
//						}

					}else if(CommonUtil.nullChk(map.get("OCI_CNE_FLAG")).equals("U")||CommonUtil.nullChk(map.get("OCI_CNE_FLAG")).equals("CT")){

						map.put("OCI_CNE_U_CN", ocicn[1]);
						map.put("OCI_CNE_U_FLAG", ocicn[3]);
						map.put("OCI_CNE_U_NUM", ocicn[4]);
//						if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//								map.put("ERRORMSG", "CN OCI CNE U NUM ERROR");
//								map.put("ERRORMSG_KOR", "");
//							}
//							flag = false;
//						}else{
//							map.put("OCI_CNE_U_NUM", ocicn[4]);
//						}

					}else if(CommonUtil.nullChk(map.get("OCI_CNE_FLAG")).equals("E")){

						map.put("OCI_CNE_E_CN", ocicn[1]);
						map.put("OCI_CNE_E_FLAG", ocicn[3]);
						map.put("OCI_CNE_E_NUM", ocicn[4]);
//						if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//								map.put("ERRORMSG", "CN OCI CNE E NUM ERROR");
//								map.put("ERRORMSG_KOR", "");
//							}
//							flag = false;
//						}else{
//							map.put("OCI_CNE_E_NUM", ocicn[4]);
//						}
					}
				}
//			}else{
//				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//					map.put("ERRORMSG", "CN OCI CNE LINE INPUT ERROR");
//					map.put("ERRORMSG_KOR", "");
//				}
//				flag = false;
//			}
		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "CN OCI CNE EXCEPTION");
				map.put("ERRORMSG_KOR", "");
			}
			flag = false;
		}

		return flag;
	}

	//20180517 OCI 중국 세관 NFY 항목 항목
	// 20241105 FHL OCI CIMP 수정 - ACAS F/U
	public boolean OCINfyCheck(HashMap<String, Object> map) throws Exception{
		// TODO Auto-generated method stub

		boolean flag = true;

		String oci_nfy_chk = map.get("OCI_NFY_LINE").toString().replace("OCI/", "/");

		try{

//				if(Pattern.matches("((\\/[A-Z]{2}\\/(NFY)\\/(T)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//						+ "((\\/[A-Z]{2}\\/(NFY)\\/(KC)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//						+ "((\\/[A-Z]{2}\\/(NFY)\\/(U)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//						+ "((\\/[A-Z]{2}\\/(NFY)\\/(E)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?", oci_nfy_chk)
//						|| Pattern.matches("((\\/[A-Z]{2}\\/(NFY)\\/(T)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//								+ "((\\/[A-Z]{2}\\/(NFY)\\/(CP)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//								+ "((\\/[A-Z]{2}\\/(NFY)\\/(CT)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?"
//								+ "((\\/[A-Z]{2}\\/(NFY)\\/(E)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n)?", oci_nfy_chk)){

				/*if(Pattern.matches("((\\/[A-Z]{2}\\/(NFY)\\/(T|KC|U|E|CP|CT)\\/[A-Z0-9\\s\\-\\.]{1,35})\r\n){1,4}", oci_nfy_chk)){*/
				if (oci_nfy_chk.endsWith("\r\n")){
					oci_nfy_chk = oci_nfy_chk.substring(0, oci_nfy_chk.length() - "\r\n".length());
					map.put("OCI_NFY_LINE", oci_nfy_chk);
				}
				String[] oci_nfy_line = oci_nfy_chk.toString().split("\r\n");

				for(int i=0; i<oci_nfy_line.length; i++){

					String[] ocicn = oci_nfy_line[i].split("/");

					map.put("OCI_NFY_CN", ocicn[1]);
					map.put("OCI_NFY", ocicn[2]);
					map.put("OCI_NFY_FLAG", ocicn[3]);

					/*if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "CN OCI NFY T NUM ERROR");
							map.put("ERRORMSG_KOR", "");
						}
						flag = false;
					}else{
						map.put("OCI_NFY_TXT", ocicn[4]);
					}*/

					if(CommonUtil.nullChk(map.get("OCI_NFY_FLAG")).equals("T")){

						map.put("OCI_NFY_T_CN", ocicn[1]);
						map.put("OCI_NFY_T_FLAG", ocicn[3]);
						map.put("OCI_NFY_T_NUM", ocicn[4]);
//						if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//								map.put("ERRORMSG", "CN OCI NFY T NUM ERROR");
//								map.put("ERRORMSG_KOR", "");
//							}
//							flag = false;
//						}else{
//							map.put("OCI_NFY_T_NUM", ocicn[4]);
//						}

					}else if(CommonUtil.nullChk(map.get("OCI_NFY_FLAG")).equals("KC") || CommonUtil.nullChk(map.get("OCI_NFY_FLAG")).equals("CP")){

						map.put("OCI_NFY_KC_CN", ocicn[1]);
						map.put("OCI_NFY_KC_FLAG", ocicn[3]);
						map.put("OCI_NFY_KC_PERSON", ocicn[4]);
//						if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//								map.put("ERRORMSG", "CN OCI NFY KC PERSON ERROR");
//								map.put("ERRORMSG_KOR", "");
//							}
//							flag = false;
//						}else{
//							map.put("OCI_NFY_KC_PERSON", ocicn[4]);
//						}

					}else if(CommonUtil.nullChk(map.get("OCI_NFY_FLAG")).equals("U") || CommonUtil.nullChk(map.get("OCI_NFY_FLAG")).equals("CT")){

						map.put("OCI_NFY_U_CN", ocicn[1]);
						map.put("OCI_NFY_U_FLAG", ocicn[3]);
						map.put("OCI_NFY_U_NUM", ocicn[4]);
//						if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//								map.put("ERRORMSG", "CN OCI NFY U NUM ERROR");
//								map.put("ERRORMSG_KOR", "");
//							}
//							flag = false;
//						}else{
//							map.put("OCI_NFY_U_NUM", ocicn[4]);
//						}

					}else if(CommonUtil.nullChk(map.get("OCI_NFY_FLAG")).equals("E")){

						map.put("OCI_NFY_E_CN", ocicn[1]);
						map.put("OCI_NFY_E_FLAG", ocicn[3]);
						map.put("OCI_NFY_E_NUM", ocicn[4]);
//						if(ocicn[4].replace(" ", "").length() == 0 || ocicn[4].replace(".", "").length() == 0|| ocicn[4].replace("-", "").length() == 0){
//							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//								map.put("ERRORMSG", "CN OCI NFY E NUM ERROR");
//								map.put("ERRORMSG_KOR", "");
//							}
//							flag = false;
//						}else{
//							map.put("OCI_NFY_E_NUM", ocicn[4]);
//						}
					}
				}
//			}else{
//				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
//					map.put("ERRORMSG", "CN OCI NFY LINE INPUT ERROR");
//					map.put("ERRORMSG_KOR", "");
//				}
//				flag = false;
//			}
		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "CN OCI NFY EXCEPTION");
				map.put("ERRORMSG_KOR", "");
			}
			flag = false;
		}

		return flag;
	}

	//SHP 항목
	public boolean SHPCheck(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		boolean flag = true;

		String[] shp_check = map.get("SHP_LINE").toString().split("\r\n");

		if(shp_check.length == 4){

			String fwrdname = shp_check[0];
			String fwrdaddr = shp_check[1];
			String fwrdloc = shp_check[2];
			String fwrdcode = shp_check[3];

			try{
				if(fwrdname.startsWith("SHP/") && Pattern.matches("[SHP]{3}\\/[A-Z0-9\\s\\-\\.]{1,35}", fwrdname.trim())){

					String shpname = fwrdname.replace("SHP/", "");

					if(shpname.replace(" ","").length()==0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "SHP NAME INPUT ERROR");
							map.put("ERRORMSG_KOR", "송하인 회사명 입력 오류");
						}
						flag = false;
					}else{
						map.put("SHP_NAME", shpname);
					}

				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "SHP NAME LINE ERROR");
						map.put("ERRORMSG_KOR", "송하인 회사명 오류");
					}
					flag = false;
				}

				if(Pattern.matches("[/]{1}[A-Z0-9\\s\\-\\.]{1,35}", fwrdaddr.trim())){
					String shpaddress = fwrdaddr.replace("/", "");

					if(shpaddress.replace(" ", "").length()==0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "SHP ADDRESS INPUT ERROR");
							map.put("ERRORMSG_KOR", "송하인 주소 항목 입력 오류");
						}
						flag = false;
					}else{
						map.put("SHP_ADDRESS", shpaddress);
					}
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "SHP ADDRESS LINE ERROR");
						map.put("ERRORMSG_KOR", "송하인 주소 항목 오류");
					}
					flag = false;
				}

				if(Pattern.matches("^/[A-Z0-9\\s\\-\\.]{1,17}(\\/[A-Z0-9\\s\\-\\.]{1,9})?", fwrdloc.trim())){
					String[] shploc = fwrdloc.split("/");

					if(shploc[1].replace(" ", "").length()==0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "SHP PLACE INPUT ERROR");
							map.put("ERRORMSG_KOR", "송하인 장소 항목 오류");
						}
						flag = false;
					}else{
						map.put("SHP_PLACE", shploc[1].trim());
					}

					if(shploc.length==3){
						if(shploc[2].replace(" ", "").length()==0){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "SHP STATEPROVINCE INPUT ERROR");
								map.put("ERRORMSG_KOR", "송하인 주코드 항목 오류");
							}
							flag = false;
						}else{
							map.put("SHP_STATEPROVINCE", shploc[2].trim());
						}
					}
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "SHP LOCATION LINE ERROR");
						map.put("ERRORMSG_KOR", "송하인 위치 항목 오류");
					}
					flag = false;
				}

				map.put("SHP_FULL_ADDR", map.get("SHP_ADDRESS")+" "+map.get("SHP_PLACE"));

				if(Pattern.matches("^[/]{1}[A-Z]{2}(([/]{1}[A-Z0-9\\s\\-\\.]{1,9})?(([/]{1}[A-Z0-9]{1,3}[/]{1}[A-Z0-9]{1,25})?){1,2}$|([/]{1}([/]{1}[A-Z0-9]{1,3}[/]{1}[A-Z0-9]{1,25}){1,2})?$)", fwrdcode.trim())){

					String[] coded = fwrdcode.split("/");
					map.put("SHP_ISOCOUNTRYCODE", coded[1]);

					if(coded.length==2){
						map.put("SHP_POSTCODE", null);
					}else if(coded.length==3){
						map.put("SHP_POSTCODE", coded[2]);
					}else if(coded.length==5){
						map.put("SHP_POSTCODE", coded[2]);
						map.put("SHP_CONTACTIDENTIFIER", coded[3]);
						map.put("SHP_CONTACTNUMBER", coded[4]);
					}else if(coded.length==7){
						map.put("SHP_POSTCODE", coded[2]);
						map.put("SHP_CONTACTIDENTIFIER", coded[3]);
						map.put("SHP_CONTACTNUMBER", coded[4]);
						map.put("SHP_CONTACTIDENTIFIER1", coded[5]);
						map.put("SHP_CONTACTNUMBER1", coded[6]);
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "SHP LOCATION FORMAT ERROR");
							map.put("ERRORMSG_KOR", "송하인 국가코드 입력 오류");
						}
						flag = false;
					}
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "SHP CODED LOCATION INPUT ERROR");
						map.put("ERRORMSG_KOR", "송하인 정보 입력 오류");
					}
					flag = false;
				}

			}catch(Exception e){
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "SHP LINE CHECK EXCEPTION");
					map.put("ERRORMSG_KOR", "송하인 라인 입력 EXCEPTION");
				}
				flag = false;
			}

		}else{
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "SHP LINE INPUT ERROR");
				map.put("ERRORMSG_KOR", "송하인 라인 입력 오류");
			}
			flag = false;
		}
		return flag;
	}

	// CNE 항목
	public boolean CNECheck(HashMap<String,Object> map){

		boolean flag = true;
		boolean bizflag = true;

		String[] cne_line = map.get("CNE_LINE").toString().split("\r\n");

		if(cne_line.length==4){

			String cnedname = cne_line[0];
			String cnedaddr = cne_line[1];
			String cnedloc = cne_line[2];
			String cnedcode = cne_line[3];

			try{
				if(cnedname.startsWith("CNE/") && Pattern.matches("[CNE]{3}\\/[A-Z0-9\\s\\-\\.]{1,35}", cnedname.trim())){
					String cnename = cnedname.replace("CNE/", "");

					if(cnename.replace(" ", "").length()==0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "CNE NAME INPUT ERROR");
							map.put("ERRORMSG_KOR", "수하인 회사명 입력 오류");
						}
						flag = false;
					}else{
						map.put("CNE_NAME", cnename);
					}

				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "CNE NAME LINE ERROR");
						map.put("ERRORMSG_KOR", "수하인 회사명 오류");
					}
					flag = false;
				}

				if(Pattern.matches("[/]{1}[A-Z0-9\\s\\-\\.]{1,35}", cnedaddr.trim())){
					String cneaddress = cnedaddr.replace("/", "");

					if(cneaddress.replace(" ", "").length()==0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "CNE ADDRESS INPUT ERROR");
							map.put("ERRORMSG_KOR", "수하인 주소 항목 입력 오류");
						}
						flag = false;
					}else{
						map.put("CNE_ADDRESS", cneaddress);
					}
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "CNE ADDRESS LINE ERROR");
						map.put("ERRORMSG_KOR", "수하인 주소 항목 오류");
					}
					flag = false;
				}

				if(Pattern.matches("^/[A-Z0-9\\s\\-\\.]{1,17}(\\/[A-Z0-9\\s\\-\\.]{1,9})?", cnedloc.trim())){
					String[] cneloc = cnedloc.split("/");

					if(cneloc[1].replace(" ", "").length()==0){
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "CNE PLACE INPUT ERROR");
							map.put("ERRORMSG_KOR", "수하인 장소 항목 오류");
						}
						flag = false;
					}else{
						map.put("CNE_PLACE", cneloc[1].trim());
					}

					if(cneloc.length==3){
						if(cneloc[2].replace(" ", "").length()==0){
							if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
								map.put("ERRORMSG", "CNE STATEPROVINCE INPUT ERROR");
								map.put("ERRORMSG_KOR", "수하인 주코드 항목 오류");
							}
							flag = false;
						}else{
							map.put("CNE_STATEPROVINCE", cneloc[2].trim());
						}
					}
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "CNE LOCATION INPUT ERROR");
						map.put("ERRORMSG_KOR", "수하인 위치 항목 오류");
					}
					flag = false;
				}


				map.put("CNE_FULL_ADDR", map.get("CNE_ADDRESS")+" "+map.get("CNE_PLACE"));

				if(Pattern.matches("^[/]{1}[A-Z]{2}(([/]{1}[A-Z0-9\\s\\-\\.]{1,9})?(([/]{1}[A-Z0-9]{1,3}[/]{1}[A-Z0-9]{1,25})?){1,2}$|([/]{1}([/]{1}[A-Z0-9]{1,3}[/]{1}[A-Z0-9]{1,25}){1,2})?$)", cnedcode.trim())){
					String[] cne_coded = cnedcode.split("/");
					map.put("CNE_ISOCOUNTRYCODE", cne_coded[1]);

					if(cne_coded.length==2){
						map.put("CNE_POSTCODE", null);
					}else if(cne_coded.length==3){
						map.put("CNE_POSTCODE", cne_coded[2]);
					}else if(cne_coded.length==5){
						map.put("CNE_POSTCODE", cne_coded[2]);
						map.put("CNE_CONTACTIDENTIFIER", cne_coded[3]);
						map.put("CNE_CONTACTNUMBER", cne_coded[4]);
					}else if(cne_coded.length==7){
						map.put("CNE_POSTCODE", cne_coded[2]);
						map.put("CNE_CONTACTIDENTIFIER", cne_coded[3]);
						map.put("CNE_CONTACTNUMBER", cne_coded[4]);
						map.put("CNE_CONTACTIDENTIFIER1", cne_coded[5]);
						map.put("CNE_CONTACTNUMBER1", cne_coded[6]);
					}else{
						if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
							map.put("ERRORMSG", "CNE LOCATION FORMAT INPUT ERROR");
							map.put("ERRORMSG_KOR", "수하인 국가코드 항목 입력 오류");
						}
						flag = false;
					}

					//Business Check
					if(!(CommonUtil.nullChk(map.get("OCI_TYPE")).equals("EXP") && (CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I") || CommonUtil.nullChk(map.get("OCI_FLAG")).equals("X")))){
						bizflag = bizcheck.BIZcheck(map);
					}
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "CNE CODED LOCATION INPUT ERROR");
						map.put("ERRORMSG_KOR", "수하인 정보 입력 오류");
					}
					flag = false;
				}

			}catch(Exception ee){
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "CNE LINE CHECK EXCEPTION");
					map.put("ERRORMSG_KOR", "수하인 항목 입력 오류");
				}
				flag = false;
			}
		}else{
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "CNE LINE LINE ERROR");
				map.put("ERRORMSG_KOR", "수하인 항목 입력 오류");
			}
			flag = false;

		}
		if(flag == false || bizflag == false){
			flag = false;
		}
		return flag;
	}

	// CVD 항목
	public boolean CVDCheck(HashMap<String,Object> map){

		boolean flag = true;

		String cvdline = map.get("CVD_LINE").toString();
		String[] cvd_line = cvdline.split("/");

		if(cvd_line.length ==6){

			try{
				if(Pattern.matches("[CVD]{3}\\/[A-Z]{3}\\/[PP|PC|CC|CP]{2}\\/([NVD]{3}|[0-9.]{1,12})\\/([NCV]{3}|[0-9.]{1,12})\\/([XXX]{3}|[0-9.]{1,11})", cvdline)){
					map.put("CVD_ISOCURRENCYCODE", cvd_line[1]);
					map.put("CVD_CHARGEDECLARATIONS", cvd_line[2]);
					map.put("CVD_CARRIAGEDECLARATIONS", cvd_line[3]);
					map.put("CVD_CUSTOMSDECLARATION", cvd_line[4]);
					map.put("CVD_INSURANCEDECLARATION", cvd_line[5]);
				}else{
					if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
						map.put("ERRORMSG", "CVD LINE CHECK ERROR");
						map.put("ERRORMSG_KOR", "CVD 입력 오류");
					}
					flag = false;
				}
			}catch(Exception ee){
				if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
					map.put("ERRORMSG", "CVD LINE CHECK EXCEPTION");
					map.put("ERRORMSG_KOR", "형식이 맞지 않은 운임정보가 입력 되었습니다.");
				}
				flag = false;
			}
		}else{
			if(CommonUtil.nullChk(map.get("ERRORMSG"),"").equals("")){
				map.put("ERRORMSG", "CVD LINE LINE ERROR");
				map.put("ERRORMSG_KOR", "운임정보 입력 오류");
			}
			flag = false;
		}
		return flag;
	}
}
