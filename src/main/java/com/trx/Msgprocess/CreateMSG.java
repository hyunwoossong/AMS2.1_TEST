package com.trx.Msgprocess;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trx.util.CommonUtil;

public class CreateMSG {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateMSG.class);

	@Autowired
	ConversionMSG conversionmsg = new ConversionMSG();

	//KTNET -> CIMP FHL CONVERSION
	public String kt_fhl_con(HashMap<String, Object> ktConmap){

		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();

		StringBuffer sb = new StringBuffer();

		try{
			String returnMSG = "";

			sb.append("UNB+IATA:1+"+ktConmap.get("SENDPIMA")+":PIMA:"+ktConmap.get("REVERSE_ROUTING")+"+"+ktConmap.get("RCVDPIMA")+":PIMA+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+"+
					"KTNETIMP+0'UNH+"+ktConmap.get("COMPANY_CODE")+"+CIMFHL:4+"+ktConmap.get("COMPANY_CODE")+ktConmap.get("KTBODY"));
			sb.append("\r\n");
			sb.append("'UNT+3+"+ktConmap.get("COMPANY_CODE")+"'UNZ+1+KTNETIMP'");

		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
		}
		return sb.toString();

	}

	//KTNET -> CIMP FMA CONVERSION
	public String kt_fma_con(HashMap<String, Object> ktConmap){

		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();

		String returnMSG = "";

		StringBuffer sb = new StringBuffer();
		try{
			String send_sys_pima;

			if(ktConmap.get("KTSENDPIMA").equals("OZ")){
				send_sys_pima = "RKRAI08AAR";
			}else{
				send_sys_pima = ktConmap.get("KTSENDPIMA").toString();
			}

			sb.append("UNA:+.? 'UNB+IATA:1+"+send_sys_pima+":PIMA+"+ktConmap.get("KTRCVD_PIMA")+":PIMA:"+ktConmap.get("KTREROUTING")+"+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+"+
					"0000"+"+0'UNH+"+"0000"+"+CIMFMA:0+"+"0000"+ktConmap.get("KTBODY"));
			sb.append("\r\n");
			sb.append("'UNT+3+0000'UNZ+1+0000'");


		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달

		}
		return sb.toString();

	}

	//KTNET -> CIMP FNA CONVERSION
	public String kt_fna_con(HashMap<String, Object> ktConmap){

		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();

		String returnMSG = "";

		StringBuffer sb = new StringBuffer();

		try{
		String send_sys_pima;

			if(ktConmap.get("KTSENDPIMA").equals("OZ")){
				send_sys_pima = "RKRAI08AAR";
			}else{
				send_sys_pima = ktConmap.get("KTSENDPIMA").toString();
			}

			sb.append("UNA:+.? 'UNB+IATA:1+"+send_sys_pima+":PIMA+"+ktConmap.get("KTRCVD_PIMA")+":PIMA:"+ktConmap.get("KTREROUTING")+"+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+"+
					"0000"+"+0'UNH+"+"0000"+"+CIMFNA:0+"+"0000"+ktConmap.get("KTBODY"));
			sb.append("\r\n");
			sb.append("'UNT+3+0000'UNZ+1+0000'");


		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달

		}
		return sb.toString();

	}

	//KTNET FHL FORMAT CONVERSION(수입) M/I건
	//20180516 중국 세관 oci 항목 추가
	public HashMap<String, Object> fhl_kt_con(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();

		String returnMSG = "";

		StringBuffer sb = new StringBuffer();
		try{
			String KTNET_CODE = "";

			if(CommonUtil.nullChk(fhlmap.get("KTNETCODE"),"").equals("")){
				KTNET_CODE = fhlmap.get("AGT_CODE").toString().substring(fhlmap.get("AGT_CODE").toString().length()-3)+"X";
			}else{
				KTNET_CODE = fhlmap.get("KTNETCODE").toString();
			}
			//20210315 oz imp m건일 경우 I로 변경
			/*if(CommonUtil.nullChk(fhlmap.get("TGT_PIMA")).equals("OZ")
					&& CommonUtil.nullChk(fhlmap.get("IMP_EXP_FLAG")).equals("IMP") && CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("M")){

				sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
						fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:2"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+");

				sb.append("FHL/4");
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
				sb.append("\r\n");
				if(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).equals(""))){
					sb.append(fhlmap.get("TXT_LINE").toString());
					sb.append("\r\n");
				}
				if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
					sb.append(fhlmap.get("HTS_LINE").toString());
					sb.append("\r\n");
				}
				if(CommonUtil.nullChk(fhlmap.get("OCI_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_LINE")).equals(""))){
					sb.append(fhlmap.get("OCI_LINE").toString().replaceAll("/KR/IMP/M/", "/KR/IMP/I/"));
					sb.append("\r\n");
				}

				sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
				sb.append("\r\n");
				if(fhlmap.get("CVD_LINE") != null){
					sb.append(fhlmap.get("CVD_LINE").toString());
					sb.append("\r\n");
				}

				sb.append("'UNT+4+1"+"'UNZ+1+01"+fhlmap.get("MBI_NO")+"'");
			}else{*/
				sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
						fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:2"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+");

				sb.append("FHL/4");
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
				sb.append("\r\n");
				if(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).equals(""))){
					sb.append(fhlmap.get("TXT_LINE").toString());
					sb.append("\r\n");
				}
				if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
					sb.append(fhlmap.get("HTS_LINE").toString());
					sb.append("\r\n");
				}
				if(CommonUtil.nullChk(fhlmap.get("OCI_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_LINE")).equals(""))){
					sb.append(fhlmap.get("OCI_LINE").toString());
					sb.append("\r\n");
				}

				if(CommonUtil.nullChk(fhlmap.get("IMP_EXP_FLAG")).equals("IMP") && CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("M")){

					// 20250224 FHL OCI CIMP 수정 - ACAS F/U
					if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))){
						sb.append(fhlmap.get("OCI_ETC_LINE"));
						sb.append("\r\n");
					}

//					if(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_SHP_INPUT")));
//						sb.append("\r\n");
//					}
//					if(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_CNE_INPUT")));
//						sb.append("\r\n");
//					}
//					if(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_NFY_INPUT")));
//						sb.append("\r\n");
//					}
				}
				sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
				sb.append("\r\n");
				if(fhlmap.get("CVD_LINE") != null){
					sb.append(fhlmap.get("CVD_LINE").toString());
					sb.append("\r\n");
				}

				sb.append("'UNT+4+1"+"'UNZ+1+01"+fhlmap.get("MBI_NO")+"'");
			//}

			/*sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
					fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:1"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+"+fhlmap.get("BODY"));*/
			//관세청 4세대 용
			/*sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
					fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:2"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+"+fhlmap.get("BODY"));*/

			fhlmap.put("KTNET_MANIFEST_FHL", sb.toString());
		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}
		return fhlmap;

	}

	//20170303 최재준
	//KTNET FHL FORMAT CONVERSION(수입/수출) 해외연계 포워더 oci 제거 건
	//포워더 코드 입력 란에 세관코드+pima 값 작성하여 전달
	public HashMap<String, Object> fhl_kt_non_oci(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();

		String returnMSG = "";

		StringBuffer sb = new StringBuffer();
		try{
			String KTNET_CODE = "";

			if(CommonUtil.nullChk(fhlmap.get("KTNETCODE"),"").equals("")){
				KTNET_CODE = fhlmap.get("AGT_CODE").toString().substring(fhlmap.get("AGT_CODE").toString().length()-3)+"X";
			}else{
				KTNET_CODE = fhlmap.get("KTNETCODE").toString();
			}

			/*sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
					fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:1"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+"+fhlmap.get("BODY"));*/
			//관세청 4세대 용
			sb.append("UNB+KECA:4+"+KTNET_CODE+":57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
					fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:2"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+"+fhlmap.get("BODY"));
			sb.append("'UNT+4+1"+"'UNZ+1+01"+fhlmap.get("MBI_NO")+"'");
			fhlmap.put("KTNET_MANIFEST_FHL", sb.toString());
		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}
		return fhlmap;

	}

	//KTNET FHL FORMAT CONVERSION(수출) I건
	//20180517 중국 세관 OCI 제거 추가
	public HashMap<String, Object> fhl_kt_con_i(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat CreatTime = new SimpleDateFormat("yyyyMMddHHmmssSS");
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();

		String returnMSG = "";

		StringBuffer sb = new StringBuffer();
		try{
			String KTNET_CODE = "";

			if(CommonUtil.nullChk(fhlmap.get("KTNETCODE"),"").equals("")){
				KTNET_CODE = fhlmap.get("AGT_CODE").toString().substring(fhlmap.get("AGT_CODE").toString().length()-3)+"X";
			}else{
				KTNET_CODE = fhlmap.get("KTNETCODE").toString();
			}

			/*sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
					fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:1"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+"+fhlmap.get("BODY"));*/
			//관세청 4세대 용
			/*sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
					fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:2"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+"+fhlmap.get("BODY").toString().replaceAll("/KR/EXP/M/", "/KR/EXP/I/"));*/

			sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
					fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:2"+"'NAD+CG++"+fhlmap.get("TGT_PIMA")+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+");

			sb.append("FHL/4");
			sb.append("\r\n");
			sb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
			sb.append("\r\n");
			sb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
			sb.append("\r\n");
			if(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).equals(""))){
				sb.append(fhlmap.get("TXT_LINE").toString());
				sb.append("\r\n");
			}
			if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
				sb.append(fhlmap.get("HTS_LINE"));
				sb.append("\r\n");
			}
			if(CommonUtil.nullChk(fhlmap.get("OCI_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_LINE")).equals(""))){
				sb.append(fhlmap.get("OCI_LINE").toString().replaceAll("/KR/EXP/M/", "/KR/EXP/I/"));
				sb.append("\r\n");
			}
			sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
			sb.append("\r\n");
			sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
			sb.append("\r\n");
			if(fhlmap.get("CVD_LINE") != null){
				sb.append(fhlmap.get("CVD_LINE"));
				sb.append("\r\n");
			}
			sb.append("'UNT+4+1"+"'UNZ+1+01"+fhlmap.get("MBI_NO")+"'");
			fhlmap.put("KTNET_MANIFEST_FHL", sb.toString());

		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}
		return fhlmap;

	}

	// FHL 4 -> KCNET MANIFEST 전송 FLAG I로 변환
	//20180517 중국 세관 OCI 제거 추가
	public HashMap<String, Object> fhl_kc_con_i(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		String airline_pima = "";
		String kcnetcode = "";
		try{

				kcnetcode = fhlmap.get("KCNETCODE").toString();

				sb.append("UNB+IATA:1+"+kcnetcode+":PIMA:"+ams_out_reverse_key+"+"+fhlmap.get("TGT_PIMA")+":PIMA"+"+"+fhlmap.get("EDI_INP_D")+":"+fhlmap.get("EDI_INP_T")+"+"+
						fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:4+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'");
				//sb.append(fhlmap.get("BODY").toString().replaceAll("/KR/EXP/M/", "/KR/EXP/I/").replaceAll("/KR/IMP/M/", "/KR/IMP/I/"));
				//sb.append(fhlmap.get("BODY").toString().replaceAll("/KR/EXP/M/", "/KR/EXP/I/"));

				sb.append("FHL/4");
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
				sb.append("\r\n");
				if(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).equals(""))){
					sb.append(fhlmap.get("TXT_LINE").toString());
					sb.append("\r\n");
				}
				if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
					sb.append(fhlmap.get("HTS_LINE"));
					sb.append("\r\n");
				}
				if(CommonUtil.nullChk(fhlmap.get("OCI_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_LINE")).equals(""))){
					sb.append(fhlmap.get("OCI_LINE").toString().replaceAll("/KR/EXP/M/", "/KR/EXP/I/"));
					sb.append("\r\n");
				}
				sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
				sb.append("\r\n");
				if(fhlmap.get("CVD_LINE") != null){
					sb.append(fhlmap.get("CVD_LINE"));
					sb.append("\r\n");
				}

				sb.append("'UNT+3+"+fhlmap.get("CSTM_CODE")+"'UNZ+1+"+fhlmap.get("SEND_COMP")+"'");
				fhlmap.put("KCNET_MANIFEST_FHL", sb.toString());

		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}
		return fhlmap;
	}

	// FHL 4
	//20180517 중국 세관 OCI 추가
	public HashMap<String, Object> fhlwrite4(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		String airline_pima = "";
		try{

			if(fhlmap.get("CONV_MULTI_SEND").equals("KE")){
				airline_pima = "KE";
			}else{
				airline_pima = fhlmap.get("AIR_PIMA_MSB_SEND").toString();
			}

			if(CommonUtil.nullChk(fhlmap.get("VERSION")).equals("4")){
/*				sb.append("UNB+IATA:1+"+fhlmap.get("SRC_PIMA")+":PIMA:"+ams_out_reverse_key+"+"+airline_pima+":PIMA"+"+"+fhlmap.get("EDI_INP_D")+":"+fhlmap.get("EDI_INP_T")+"+"+
						fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:4+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'");*/

				sb.append("UNB+IATA:1+"+fhlmap.get("SRC_PIMA")+":PIMA:"+ams_out_reverse_key+"+"+airline_pima+":PIMA"+"+"+CommonUtil.getGMTtime("DATE")+":"+CommonUtil.getGMTtime("TIME")+"+"+
						fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:4+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'");

				if(airline_pima.equals("KE")){
					sb.append("FHL/4");
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
					sb.append("\r\n");
					if(CommonUtil.nullChk(fhlmap.get("COM1")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("COM1")).equals(""))){
						sb.append(fhlmap.get("COM1").toString().replace("TXT/.COM.", "TXT/"));
						sb.append("\r\n");
					}else{
						sb.append(fhlmap.get("T_FREETEXT").toString().replace(".COM.", "TXT/"));
						sb.append("\r\n");
					}
					if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
						sb.append(fhlmap.get("HTS_LINE"));
						sb.append("\r\n");
					}
					// 20241105 FHL OCI CIMP 수정 - ACAS F/U
					if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))){
						sb.append("OCI");
						sb.append(fhlmap.get("OCI_ETC_LINE"));
						sb.append("\r\n");
					}

					/*if((CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals("")))
							|| (CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals("")))
							|| (CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals("")))){
							sb.append("OCI");
					}
					if(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals(""))){
						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_SHP_INPUT")).replace("OCI/", "/"));
						sb.append("\r\n");
					}
					if(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals(""))){
						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_CNE_INPUT")).replace("OCI/", "/"));
						sb.append("\r\n");
					}
					if(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals(""))){
						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_NFY_INPUT")).replace("OCI/", "/"));
						sb.append("\r\n");
					}*/

					sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
					sb.append("\r\n");
					if(fhlmap.get("CVD_LINE") != null){
						sb.append(fhlmap.get("CVD_LINE"));
						sb.append("\r\n");
					}
				}else{
					sb.append(fhlmap.get("BODY"));
				}
				sb.append("'UNT+3+"+fhlmap.get("CSTM_CODE")+"'UNZ+1+"+fhlmap.get("SEND_COMP")+"'");

			}else if(CommonUtil.nullChk(fhlmap.get("VERSION")).equals("2")){
				/*
				 * sb.append("UNB+IATA:1+"+fhlmap.get("SRC_PIMA")+":PIMA:"+ams_out_reverse_key+
				 * "+"+airline_pima+":PIMA"+"+"+fhlmap.get("EDI_INP_D")+":"+fhlmap.get(
				 * "EDI_INP_T")+"+"+
				 * fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:2+"+
				 * CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'FHL/2"+"\r\n");
				 */
				sb.append("UNB+IATA:1+"+fhlmap.get("SRC_PIMA")+":PIMA:"+ams_out_reverse_key+"+"+airline_pima+":PIMA"+"+"+CommonUtil.getGMTtime("DATE")+":"+CommonUtil.getGMTtime("TIME")+"+"+
						fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:2+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'FHL/2"+"\r\n");
				sb.append(fhlmap.get("MBI_LINE"));
				sb.append("\r\n");
				if(CommonUtil.nullChk(fhlmap.get("H_SLAC"), "").equals("")||CommonUtil.nullChk(fhlmap.get("H_SLAC"), "").equals("0")){

					//sb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_PC")+"/"+fhlmap.get("H_WT_CODE")+fhlmap.get("H_WT")+"/"+fhlmap.get("H_COMMODITY"));
					sb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_PC")+"/"+fhlmap.get("H_WT_LINE")+"/"+fhlmap.get("H_COMMODITY"));
					sb.append("\r\n");
				}else{
					//sb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_SLAC")+"/"+fhlmap.get("H_WT_CODE")+fhlmap.get("H_WT")+"/"+fhlmap.get("H_COMMODITY"));
					sb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_SLAC")+"/"+fhlmap.get("H_WT_LINE")+"/"+fhlmap.get("H_COMMODITY"));
					sb.append("\r\n");
				}
				sb.append(fhlmap.get("T_FREETEXT").toString().replace(".COM.", "TXT/"));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
				sb.append("\r\n");
				if(fhlmap.get("CVD_LINE") != null){
					sb.append(fhlmap.get("CVD_LINE"));
					sb.append("\r\n");
				}
				sb.append("'UNT+3+"+fhlmap.get("CSTM_CODE")+"'UNZ+1+"+fhlmap.get("SEND_COMP")+"'");
				fhlmap.put("CONV_MSG", sb.toString());
			}
			if(fhlmap.get("CONV_MULTI_SEND").equals("KE")){

				fhlmap.put("FHL4", sb.toString().replaceAll("\\t", ""));
			}else{
				fhlmap.put("FHL4", sb.toString());
			}
		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}
		return fhlmap;
	}

	// FHL 4
	//20180517 중국 세관 OCI 제거 추가
	public HashMap<String, Object> fhlwriteoal4(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		String airline_pima = "";
		String src_pima = "";
		try{

			airline_pima = fhlmap.get("AIR_PIMA_MSB_SEND_VER").toString();
			//CX 항공사 일 경우 EZC PIMA로 변경하여 전송
			if(CommonUtil.nullChk(fhlmap.get("TGT_PIMA")).equals("CX") && !CommonUtil.nullChk(fhlmap.get("EZC_PIMA")).equals("")){
				src_pima = fhlmap.get("EZC_PIMA").toString();
			}else{
				src_pima = fhlmap.get("SRC_PIMA").toString();
			}

			if(CommonUtil.nullChk(fhlmap.get("VERSION")).equals("4")){
/*				sb.append("UNB+IATA:1+"+src_pima+":PIMA:"+ams_out_reverse_key+"+"+airline_pima+":PIMA"+"+"+fhlmap.get("EDI_INP_D")+":"+fhlmap.get("EDI_INP_T")+"+"+
						fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:4+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'");*/

				sb.append("UNB+IATA:1+"+src_pima+":PIMA:"+ams_out_reverse_key+"+"+airline_pima+":PIMA"+"+"+CommonUtil.getGMTtime("DATE")+":"+CommonUtil.getGMTtime("TIME")+"+"+
						fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:4+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'");

					sb.append("FHL/4");
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
					sb.append("\r\n");
					if(CommonUtil.nullChk(fhlmap.get("COM1")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("COM1")).equals(""))){
						sb.append(fhlmap.get("COM1").toString().replace("TXT/.COM.", "TXT/"));
						sb.append("\r\n");
					}else{
						sb.append(fhlmap.get("T_FREETEXT").toString().replace(".COM.", "TXT/"));
						sb.append("\r\n");
					}
					if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
						sb.append(fhlmap.get("HTS_LINE"));
						sb.append("\r\n");
					}
					// 20241105 FHL OCI CIMP 수정 - ACAS F/U
					if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))){
						sb.append("OCI");
						sb.append(fhlmap.get("OCI_ETC_LINE"));
						sb.append("\r\n");
					}

//					if(CommonUtil.nullChk(fhlmap.get("OCI_CIMP_LINE")).length() > 0 && !CommonUtil.nullChk(fhlmap.get("OCI_CIMP_LINE")).equals("")){
//						sb.append(fhlmap.get("OCI_CIMP_LINE"));
//						sb.append("\r\n");
//					}else{
//						if((CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals("")))
//								|| (CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals("")))
//								|| (CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals("")))){
//
//							sb.append("OCI");
//						}
//					}
//
//					if(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_SHP_INPUT")).replace("OCI/", "/"));
//						sb.append("\r\n");
//					}
//					if(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_CNE_INPUT")).replace("OCI/", "/"));
//						sb.append("\r\n");
//					}
//					if(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_NFY_INPUT")).replace("OCI/", "/"));
//						sb.append("\r\n");
//					}

					sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
					sb.append("\r\n");
					if(fhlmap.get("CVD_LINE") != null){
						sb.append(fhlmap.get("CVD_LINE"));
						sb.append("\r\n");
					}
				sb.append("'UNT+3+"+fhlmap.get("CSTM_CODE")+"'UNZ+1+"+fhlmap.get("SEND_COMP")+"'");
				fhlmap.put("OAL_VER4", sb.toString());
			}
		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}
		return fhlmap;
	}

	// FHL 4 -> 2 CONVERSION
	public HashMap<String, Object> fhlwrite2(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		try{

			String vencode =(String) fhlmap.get("SEND_COMP");

			if(vencode.equals("AIRCISFHL")){
				vencode = "FHLCON";
			}else if(vencode.equals("KCNETFHL")){
				vencode = "KCNETCON";
			}else{
				vencode = (String) fhlmap.get("SEND_COMP");
			}
			//20180516 중국 세관 용 fhl/4 버전 업그레이드 추가
			// 20241105 FHL OCI CIMP 수정 - ACAS F/U
			if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))) {
//			if( (CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals("")))
//					|| (CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals("")))
//					|| (CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals("")))) {

/*					sb.append("UNB+IATA:1+"+fhlmap.get("SRC_PIMA")+":PIMA:"+ams_out_reverse_key+"+"+fhlmap.get("AIR_PIMA_MSB_SEND")+":PIMA"+"+"+fhlmap.get("EDI_INP_D")+":"+fhlmap.get("EDI_INP_T")+"+"+
						fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:4+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'");*/
					sb.append("UNB+IATA:1+"+fhlmap.get("SRC_PIMA")+":PIMA:"+ams_out_reverse_key+"+"+fhlmap.get("AIR_PIMA_MSB_SEND")+":PIMA"+"+"+CommonUtil.getGMTtime("DATE")+":"+CommonUtil.getGMTtime("TIME")+"+"+
						fhlmap.get("SEND_COMP")+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:4+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'");
					sb.append("FHL/4");
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
					sb.append("\r\n");
					if(CommonUtil.nullChk(fhlmap.get("COM1")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("COM1")).equals(""))){
						sb.append(fhlmap.get("COM1").toString().replace("TXT/.COM.", "TXT/"));
						sb.append("\r\n");
					}else{
						sb.append(fhlmap.get("T_FREETEXT").toString().replace(".COM.", "TXT/"));
						sb.append("\r\n");
					}
					if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
						sb.append(fhlmap.get("HTS_LINE"));
						sb.append("\r\n");
					}
					// 20241105 FHL OCI CIMP 수정 - ACAS F/U
					if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))){
						sb.append("OCI");
						sb.append(fhlmap.get("OCI_ETC_LINE"));
						sb.append("\r\n");
					}

//					if((CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals("")))
//							|| (CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals("")))
//							|| (CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals("")))){
//							sb.append("OCI");
//					}
//					if(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_SHP_INPUT")).replace("OCI/", "/"));
//						sb.append("\r\n");
//					}
//					if(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_CNE_INPUT")).replace("OCI/", "/"));
//						sb.append("\r\n");
//					}
//					if(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals(""))){
//						sb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_NFY_INPUT")).replace("OCI/", "/"));
//						sb.append("\r\n");
//					}
					sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
					sb.append("\r\n");
					sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
					sb.append("\r\n");
					if(fhlmap.get("CVD_LINE") != null){
						sb.append(fhlmap.get("CVD_LINE"));
						sb.append("\r\n");
					}
				sb.append("'UNT+3+"+fhlmap.get("CSTM_CODE")+"'UNZ+1+"+fhlmap.get("SEND_COMP")+"'");
				fhlmap.put("OAL_VER4", sb.toString());
			}else{

				/*
				 * sb.append("UNB+IATA:1+"+fhlmap.get("SRC_PIMA")+":PIMA:"+ams_out_reverse_key+
				 * "+"+fhlmap.get("AIR_PIMA_MSB_SEND")+":PIMA+"+YYMMddFormat.format(now)+":"+
				 * hhmmFormat.format(now)+"+"+
				 * vencode+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:2+"+CommonUtil.nullChk(
				 * fhlmap.get("REFERENCE_NUMBER"))+"'FHL/2"+"\r\n");
				 */
				sb.append("UNB+IATA:1+"+fhlmap.get("SRC_PIMA")+":PIMA:"+ams_out_reverse_key+"+"+fhlmap.get("AIR_PIMA_MSB_SEND")+":PIMA+"+CommonUtil.getGMTtime("DATE")+":"+CommonUtil.getGMTtime("TIME")+"+"+
						 vencode+"+0'UNH+"+fhlmap.get("CSTM_CODE")+"+CIMFHL:2+"+CommonUtil.nullChk(fhlmap.get("REFERENCE_NUMBER"))+"'FHL/2"+"\r\n");
				sb.append(fhlmap.get("MBI_LINE"));
				sb.append("\r\n");
				if(CommonUtil.nullChk(fhlmap.get("H_SLAC"), "").equals("")||CommonUtil.nullChk(fhlmap.get("H_SLAC"), "").equals("0")){

					//sb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_PC")+"/"+fhlmap.get("H_WT_CODE")+fhlmap.get("H_WT")+"/"+fhlmap.get("H_COMMODITY"));
					sb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_PC")+"/"+fhlmap.get("H_WT_LINE")+"/"+fhlmap.get("H_COMMODITY"));
					sb.append("\r\n");
				}else{
					//sb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_SLAC")+"/"+fhlmap.get("H_WT_CODE")+fhlmap.get("H_WT")+"/"+fhlmap.get("H_COMMODITY"));
					sb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_SLAC")+"/"+fhlmap.get("H_WT_LINE")+"/"+fhlmap.get("H_COMMODITY"));
					sb.append("\r\n");
				}
				sb.append(fhlmap.get("T_FREETEXT").toString().replace(".COM.", "TXT/"));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
				sb.append("\r\n");
				sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
				sb.append("\r\n");
				if(fhlmap.get("CVD_LINE") != null){
					sb.append(fhlmap.get("CVD_LINE"));
					sb.append("\r\n");
				}
				sb.append("'UNT+3+"+fhlmap.get("CSTM_CODE")+"'UNZ+1+"+vencode+"'");
				fhlmap.put("CONVERSION_MSG", sb.toString());
			}

		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}

		return fhlmap;
	}

	// TYPEB Conversion
	//20180516 TYPE-B FHL/4으로 전송 + 중국세관 OCI 로직 추가
	public HashMap<String, Object> typebwrite(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer typeb = new StringBuffer();
		try{



			typeb.append("Y.QD "+fhlmap.get("SITA_TYPEB_SEND"));
			typeb.append("\n"+".SELTZKE "+CommonUtil.getGMTtime("TYPEB")+CommonUtil.getYearformat());
			// 20241105 FHL OCI CIMP 수정 - ACAS F/U
			if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))) {
//			if( (CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals("")))
//					|| (CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals("")))
//					|| (CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals("")))) {

				typeb.append("\n"+"FHL/4");
				typeb.append("\n");
				typeb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
				typeb.append("\n");
				typeb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
				typeb.append("\n");
				if(CommonUtil.nullChk(fhlmap.get("COM1")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("COM1")).equals(""))){
					typeb.append(fhlmap.get("COM1").toString().replace("TXT/.COM.", "TXT/"));
					typeb.append("\n");
				}else{
					typeb.append(fhlmap.get("T_FREETEXT").toString().replace(".COM.", "TXT/"));
					typeb.append("\n");
				}
				if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
					typeb.append(fhlmap.get("HTS_LINE"));
					typeb.append("\n");
				}
				// 20241105 FHL OCI CIMP 수정 - ACAS F/U
				if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))){
					typeb.append("OCI");
					typeb.append(fhlmap.get("OCI_ETC_LINE"));
					typeb.append("\r\n");
				}

//				if((CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals("")))
//						|| (CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals("")))
//						|| (CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals("")))){
//					typeb.append("OCI");
//				}
//				if(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals(""))){
//					typeb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_SHP_INPUT")).replace("OCI/", "/"));
//					typeb.append("\r\n");
//				}
//				if(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals(""))){
//					typeb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_CNE_INPUT")).replace("OCI/", "/"));
//					typeb.append("\r\n");
//				}
//				if(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals(""))){
//					typeb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_NFY_INPUT")).replace("OCI/", "/"));
//					typeb.append("\r\n");
//				}
				typeb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
				typeb.append("\n");
				typeb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
				if(fhlmap.get("CVD_LINE") != null){
					typeb.append("\n");
					typeb.append(fhlmap.get("CVD_LINE"));
				}
			}else{
				typeb.append("\n"+"FHL/2");
				typeb.append("\n"+fhlmap.get("MBI_LINE"));
				typeb.append("\n");
				if(CommonUtil.nullChk(fhlmap.get("H_SLAC"), "").equals("")||CommonUtil.nullChk(fhlmap.get("H_SLAC"), "").equals("0")){

					//typeb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_PC")+"/"+fhlmap.get("H_WT_CODE")+fhlmap.get("H_WT")+"/"+fhlmap.get("H_COMMODITY"));
					typeb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_PC")+"/"+fhlmap.get("H_WT_LINE")+"/"+fhlmap.get("H_COMMODITY"));
					typeb.append("\n");
				}else{
					//typeb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_SLAC")+"/"+fhlmap.get("H_WT_CODE")+fhlmap.get("H_WT")+"/"+fhlmap.get("H_COMMODITY"));
					typeb.append("HBS/"+fhlmap.get("HBS_NO")+"/"+fhlmap.get("H_ORG")+fhlmap.get("H_DST")+"/"+fhlmap.get("H_SLAC")+"/"+fhlmap.get("H_WT_LINE")+"/"+fhlmap.get("H_COMMODITY"));
					typeb.append("\n");
				}
				typeb.append(fhlmap.get("T_FREETEXT").toString().replace(".COM.", "TXT/"));
				typeb.append("\n");
				typeb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
				typeb.append("\n");
				typeb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
				if(fhlmap.get("CVD_LINE") != null){
					typeb.append("\n");
					typeb.append(fhlmap.get("CVD_LINE"));
				}
			}
			fhlmap.put("TYPEB", typeb.toString());
		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}
		return fhlmap;
	}

	// TYPEB Conversion
	//TYPE-B FHL/4으로 전송 + 중국세관 OCI 로직 추가
	public HashMap<String, Object> typebwrite4(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();
		StringBuffer typeb = new StringBuffer();
		try{

			typeb.append("Y.QD "+fhlmap.get("SITA_TYPEB_SEND_VER"));
			typeb.append("\n"+".SELTZKE "+CommonUtil.getGMTtime("TYPEB")+CommonUtil.getYearformat());
			typeb.append("\n"+"FHL/4");
			typeb.append("\n");
			typeb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
			typeb.append("\n");
			typeb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
			typeb.append("\n");
			if(CommonUtil.nullChk(fhlmap.get("COM1")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("COM1")).equals(""))){
				typeb.append(fhlmap.get("COM1").toString().replace("TXT/.COM.", "TXT/"));
				typeb.append("\n");
			}else{
				typeb.append(fhlmap.get("T_FREETEXT").toString().replace(".COM.", "TXT/"));
				typeb.append("\n");
			}
			if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
				typeb.append(fhlmap.get("HTS_LINE"));
				typeb.append("\n");
			}
			// 20241105 FHL OCI CIMP 수정 - ACAS F/U
			if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))){
				typeb.append("OCI");
				typeb.append(fhlmap.get("OCI_ETC_LINE"));
				typeb.append("\r\n");
			}

//			if(CommonUtil.nullChk(fhlmap.get("OCI_CIMP_LINE")).length() > 0 && !CommonUtil.nullChk(fhlmap.get("OCI_CIMP_LINE")).equals("")){
//				typeb.append(fhlmap.get("OCI_CIMP_LINE"));
//				typeb.append("\r\n");
//			}else{
//				if((CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals("")))
//						|| (CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals("")))
//						|| (CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals("")))){
//
//					typeb.append("OCI");
//				}
//			}
//			if(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_SHP_LINE")).equals(""))){
//				typeb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_SHP_INPUT")).replace("OCI/", "/"));
//				typeb.append("\r\n");
//			}
//			if(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_CNE_LINE")).equals(""))){
//				typeb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_CNE_INPUT")).replace("OCI/", "/"));
//				typeb.append("\r\n");
//			}
//			if(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_NFY_LINE")).equals(""))){
//				typeb.append(CommonUtil.nullChknotrim(fhlmap.get("OCI_NFY_INPUT")).replace("OCI/", "/"));
//				typeb.append("\r\n");
//			}
			typeb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
			typeb.append("\n");
			typeb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
			if(fhlmap.get("CVD_LINE") != null){
				typeb.append("\n");
				typeb.append(fhlmap.get("CVD_LINE"));
			}
			fhlmap.put("TYPEB", typeb.toString());
		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}
		return fhlmap;
	}


	public HashMap<String, Object> sendKTNETFHL(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception{
		DateFormat hhmmFormat = new SimpleDateFormat("HHmm");
		DateFormat YYMMddFormat = new SimpleDateFormat("yyMMdd");
		Date now = new Date();

		StringBuffer sb = new StringBuffer();
		try{
			String KTNET_CODE = "";

			if(CommonUtil.nullChk(fhlmap.get("KTNETCODE"),"").equals("")){
				KTNET_CODE = fhlmap.get("AGT_CODE").toString().substring(fhlmap.get("AGT_CODE").toString().length()-3)+"X";
			}else{
				KTNET_CODE = fhlmap.get("KTNETCODE").toString();
			}

			String tgt_pima = CommonUtil.nullChk(fhlmap.get("TGT_PIMA"));

			if (tgt_pima.equals("BR")) {
				tgt_pima = "OZ";
			}

			sb.append("UNB+KECA:4+FF"+KTNET_CODE+"2000:57:"+ams_out_reverse_key+"+KTNETACPS:57+"+YYMMddFormat.format(now)+":"+hhmmFormat.format(now)+"+01"+
					fhlmap.get("MBI_NO")+"'UNH+1+AIRFHL:2"+"'NAD+CG++"+tgt_pima+"'BIN+"+fhlmap.get("KT_MSG_SIZE")+"+");

			sb.append("FHL/4");
			sb.append("\r\n");
			sb.append(CommonUtil.nullChk(fhlmap.get("MBI_LINE")));
			sb.append("\r\n");
			sb.append(CommonUtil.nullChk(fhlmap.get("HBS_LINE")));
			sb.append("\r\n");

			// KTNET FHL-E 필수항목은 .COM., .FLT.까지임
			// TXT 라인 전체 정보를 FHL-E 건에 대하여 전송해도 문제되지는 않음
			if(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("TXT_LINE")).equals(""))){
				sb.append(fhlmap.get("TXT_LINE").toString());
				sb.append("\r\n");
			}

			if(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("HTS_LINE")).equals(""))){
				sb.append(fhlmap.get("HTS_LINE").toString());
				sb.append("\r\n");
			}

			// OCI Dummy 값 부분은 KTNET FHL-E 필수항목으로 항상 포함되어야 함
			sb.append("OCI/KR/EXP/E/E001-NCV--0");
			sb.append("\r\n");
			sb.append("/KR/EXP/E/S001-0-0-N-0");
			sb.append("\r\n");
			if(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("OCI_ETC_LINE")).equals(""))){
				sb.append(fhlmap.get("OCI_ETC_LINE"));
				sb.append("\r\n");
			}

			sb.append(CommonUtil.nullChk(fhlmap.get("SHP_LINE")));
			sb.append("\r\n");
			sb.append(CommonUtil.nullChk(fhlmap.get("CNE_LINE")));
			sb.append("\r\n");
			if(CommonUtil.nullChk(fhlmap.get("CVD_LINE")).length() > 0 && !(CommonUtil.nullChk(fhlmap.get("CVD_LINE")).equals(""))){
				sb.append(fhlmap.get("CVD_LINE").toString());
				sb.append("\r\n");
			}

			sb.append("'UNT+4+1"+"'UNZ+1+01"+fhlmap.get("MBI_NO")+"'");

			fhlmap.put("KTNET_FHL", sb.toString());

		}catch (Exception e) {
			e.printStackTrace();
			fhlmap.put("ERRORMSG", "AMS ROUTE CHECK EXCEPTION");

			//Exception 난 값 catch 문으로 전달
			throw new Exception();
		}

		return fhlmap;
	}

	//java 문자열 앞/뒤 자리 공백 처리 해주는 메소드
	public String appendSpace(String type, String str, int len) {

		if(str == null){

			str = "";
		}
		int strLength = str.getBytes().length;
		String tempStr = str;

		if (strLength <len) {
			int endCount = len - strLength;

			for (int i=0 ; i<endCount ; i++) {

		    	if(type.equals("PIECE")){

		        	 str = " " + str;
		    	}else{
		    		str = str + " ";
		    	}
		    }
		} else if (strLength > len) {
			byte[] temp = new byte[len];
			System.arraycopy (str.getBytes(), 0, temp, 0, len);
			str = new String (temp);
		} else {

		}

		// 한글을 못짜를때.... len 에 한글이 물려 있을경우...
		// 이경우는 len-1 만큼 자른후 공백을 붙여 반환한다.
		if (str.length() == 0) {
			byte[] temp = new byte[len];
			System.arraycopy (tempStr.getBytes(), 0, temp, 0, len-1);
			str = new String (temp);
		}

		return str;
	}
}
