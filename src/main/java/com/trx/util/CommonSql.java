package com.trx.util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trx.util.CommonUtil;
import com.ibatis.sqlmap.client.SqlMapClient;

public class CommonSql {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(CommonSql.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Produce
	ProducerTemplate producer;

    /**
     * HISTORY TABLE INSERT
     * @param logMap
     * @return
     * @throws Exception
     */
	public int insertHistory(HashMap logMap) throws Exception {
		int result = 0;
		try {
			result = (int) sqlMapClient.update("INSERT_UPDATE.insertTraceHistory",logMap);
		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch (Exception e) {
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
		}

		return result;
	}

	public int insertTrace(HashMap logMap) throws Exception {
		int result = 0;
		try{
			result = (int) sqlMapClient.update("INSERT_UPDATE.insertTrace",logMap);

		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
		}

		return result;
	}



	public int updateTrace(String ams_out_reverse_key, HashMap<String, Object> logMap, HashMap<String, Object> fhlmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();


			//sqlMapClient.startTransaction();

			smiMap.put("AMS_TRX_ROUTE", CommonUtil.nullChk(ams_out_reverse_key));
			smiMap.put("ORG_ROUTE", CommonUtil.nullChk(fhlmap.get("MSG_CTRL_ID")));
			if(logMap.get("AMS_STEP") != null){
				smiMap.put("AMS_STEP", CommonUtil.nullChk(logMap.get("AMS_STEP")));
			}
			smiMap.put("SMI", CommonUtil.nullChk(fhlmap.get("SMI")));
			smiMap.put("VERSION", CommonUtil.nullChk(fhlmap.get("VERSION")));
			smiMap.put("CON_VERSION", CommonUtil.nullChk(fhlmap.get("CON_VERSION")));
			smiMap.put("SRC_POINT", CommonUtil.nullChk(fhlmap.get("SRC_POINT")));
			smiMap.put("SRC_PIMA", CommonUtil.nullChk(fhlmap.get("SRC_PIMA")));
			smiMap.put("TGT_POINT", CommonUtil.nullChk(fhlmap.get("TGT_POINT")));
			smiMap.put("TGT_PIMA", CommonUtil.nullChk(fhlmap.get("TGT_PIMA")));
			if(fhlmap.get("SEND_COMP") != null){
				if(fhlmap.get("SEND_COMP").equals(fhlmap.get("HAWB"))){
					smiMap.put("TGT_ROUTE", "KTNET");
				}else{
					smiMap.put("TGT_ROUTE", fhlmap.get("SEND_COMP"));
				}
			}
			smiMap.put("MAWB_NO", CommonUtil.nullChk(fhlmap.get("MAWB")));
			smiMap.put("HAWB_NO", CommonUtil.nullChk(fhlmap.get("HAWB"),"-"));
			smiMap.put("CARR_CODE", CommonUtil.nullChk(fhlmap.get("TGT_PIMA")));
			smiMap.put("EXP_IMP_FLAG", CommonUtil.nullChk(fhlmap.get("OCI_TYPE")));
			smiMap.put("TYPE_DIVICE", CommonUtil.nullChk(fhlmap.get("OCI_FLAG")));
			smiMap.put("EDI_INP_D", CommonUtil.nullChk(fhlmap.get("EDI_INP_D")));
			smiMap.put("EDI_INP_T", CommonUtil.nullChk(fhlmap.get("EDI_INP_T")));
			if(fhlmap.get("EDI") != null){
				byte[] org_msg = fhlmap.get("EDI").toString().getBytes("UTF-8");
				fhlmap.put("ORG_MESSAGE", org_msg);
				smiMap.put("ORG_MSG", fhlmap.get("ORG_MESSAGE"));
			}
			if(fhlmap.get("CONV_MSG") != null){
				byte[] conv_msg = fhlmap.get("CONV_MSG").toString().getBytes("UTF-8");
				fhlmap.put("CONV_MESSAGE", conv_msg);
				smiMap.put("CONV_MSG", fhlmap.get("CONV_MESSAGE"));
			}
			if(fhlmap.get("AMS_ACK") != null){
				byte[] ams_ack = fhlmap.get("AMS_ACK").toString().getBytes("UTF-8");
				fhlmap.put("AMS_ACKMSG", ams_ack);
				smiMap.put("AMS_ACK", fhlmap.get("AMS_ACKMSG"));
			}
			if(fhlmap.get("MSB_ACK") != null){
				byte[] msb_ack = fhlmap.get("MSB_ACK").toString().getBytes("UTF-8");
				fhlmap.put("MSB_ACKMSG", msb_ack);
				smiMap.put("MSB_ACK", fhlmap.get("MSB_ACKMSG"));
			}
			if(fhlmap.get("KE_AIR_ACK") != null){
				byte[] air_ack = fhlmap.get("KE_AIR_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KE_AIR_ACKMSG", air_ack);
				smiMap.put("KE_AIR_ACK", fhlmap.get("KE_AIR_ACKMSG"));
			}
			if(fhlmap.get("OAL_AIR_ACK") != null){
				byte[] oal_air_ack = fhlmap.get("OAL_AIR_ACK").toString().getBytes("UTF-8");
				fhlmap.put("OAL_AIR_ACKMSG", oal_air_ack);
				smiMap.put("OAL_AIR_ACK", fhlmap.get("OAL_AIR_ACKMSG"));
			}
			if(fhlmap.get("KAMS_ACK") != null){
				byte[] kams_ack = fhlmap.get("KAMS_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KAMS_ACKMSG", kams_ack);
				smiMap.put("KAMS_ACK", fhlmap.get("KAMS_ACKMSG"));
			}
			if(fhlmap.get("MULTI_MSG") != null){
				byte[] manifest_ack = fhlmap.get("MULTI_MSG").toString().getBytes("UTF-8");
				fhlmap.put("MULTI_MESSAGE", manifest_ack);
				smiMap.put("MULTI_MSG", fhlmap.get("MULTI_MESSAGE"));
			}
			if(fhlmap.get("TYPEB") != null){
				byte[] typeb = fhlmap.get("TYPEB").toString().getBytes("UTF-8");
				fhlmap.put("TYPEB_MSG", typeb);
				smiMap.put("TYPEB", fhlmap.get("TYPEB_MSG"));
			}
			if(fhlmap.get("KTNET_FHL") != null){
				byte[] ktfhl = fhlmap.get("KTNET_FHL").toString().getBytes("UTF-8");
				fhlmap.put("KTNET_MSG", ktfhl);
				smiMap.put("KTNET_FHL", fhlmap.get("KTNET_MSG"));
			}
			if(fhlmap.get("KTNET_MANIFEST_FHL") != null){
				byte[] ktfhl = fhlmap.get("KTNET_MANIFEST_FHL").toString().getBytes("UTF-8");
				fhlmap.put("KTNET_MANIFEST_MSG", ktfhl);
				smiMap.put("KTNET_MANIFEST_FHL", fhlmap.get("KTNET_MANIFEST_MSG"));
			}
			if(fhlmap.get("KCNET_MANIFEST_FHL") != null){
				byte[] kcfhl = fhlmap.get("KCNET_MANIFEST_FHL").toString().getBytes("UTF-8");
				fhlmap.put("KCNET_MANIFEST_MSG", kcfhl);
				smiMap.put("KCNET_MANIFEST_FHL", fhlmap.get("KCNET_MANIFEST_MSG"));
			}
			smiMap.put("FLT_NO", CommonUtil.nullChk(fhlmap.get("FLT_NUM")));
			smiMap.put("FLT_DATE", CommonUtil.nullChk(fhlmap.get("FLT")));
			smiMap.put("DEP", CommonUtil.nullChk(fhlmap.get("M_ORG")));
			smiMap.put("ARR", CommonUtil.nullChk(fhlmap.get("M_DST")));
			smiMap.put("ORG", CommonUtil.nullChk(fhlmap.get("H_ORG")));
			smiMap.put("DST", CommonUtil.nullChk(fhlmap.get("H_DST")));
			smiMap.put("CSTM_COD_C_3", CommonUtil.nullChk(fhlmap.get("CSTM_COD_C_3")));//포워더 코드 3자리 XXX
			smiMap.put("CSTM_COD_C", CommonUtil.nullChk(fhlmap.get("FLT_DECONSOL")));//포워더 코드 SELXXX
			if(fhlmap.get("AMS_SMI") != null){
				smiMap.put("AMS_SMI", fhlmap.get("AMS_SMI"));// AMS ACK 상태 값
			}
			if(fhlmap.get("ERRORMSG") != null){
				smiMap.put("ERRORMSG", fhlmap.get("ERRORMSG"));// ERRORMSG 상태 값
			}
			if(fhlmap.get("ERRORMSG_KOR") != null){
				smiMap.put("ERRORMSG_KOR", fhlmap.get("ERRORMSG_KOR"));// ERRORMSG_KOR 상태 값
			}
			if(fhlmap.get("IKAMS_SMI") != null){
				smiMap.put("IKAMS_SMI", fhlmap.get("IKAMS_SMI"));// IKAMS ACK 상태 값
			}
			if(fhlmap.get("IKAMS_ERRMSG") != null){
				smiMap.put("IKAMS_ERRMSG", fhlmap.get("IKAMS_ERRMSG"));// ERRORMSG 상태 값
			}
			if(fhlmap.get("IKAMS_ERRMSG_KOR") != null){
				smiMap.put("IKAMS_ERRMSG_KOR", fhlmap.get("IKAMS_ERRMSG_KOR"));// ERRORMSG_KOR 상태 값
			}
			smiMap.put("IKAMS_STEP", CommonUtil.nullChk(logMap.get("IKAMS_STEP")));

			smiMap.put("REPROC", CommonUtil.nullChk(fhlmap.get("REPROC")));// 재처리 상태 값
			smiMap.put("OUT_AMS_FLAG", CommonUtil.nullChk(fhlmap.get("OUT_AMS_FLAG")));
			smiMap.put("OUT_KAMS_FLAG", CommonUtil.nullChk(fhlmap.get("OUT_KAMS_FLAG")));
			//System.out.println("ERROR_DESC : "+fhlmap.get("ERROR_DESC").toString());
			//System.out.println("ERROR_DESC_KOR : "+fhlmap.get("ERROR_DESC_KOR").toString());
			//System.out.println("@@@@@@ : "+smiMap.get("ORG_ROUTE").toString());
			result = (int) sqlMapClient.update("INSERT_UPDATE.UpdateTrace",smiMap);
			//sqlMapClient.commitTransaction();
		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
		}

		return result;
	}

	public int insertFHL(String ams_out_reverse_key, HashMap<String, Object> logMap, HashMap<String, Object> fhlmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();
			//sqlMapClient.startTransaction();
			smiMap.put("TRX_ROUTE",CommonUtil.nullChk(ams_out_reverse_key));
			smiMap.put("COMPANYCODE",CommonUtil.nullChk(fhlmap.get("CSTM_CODE")));
			if(fhlmap.get("SEND_COMP") != null){
				if(fhlmap.get("SEND_COMP").equals(fhlmap.get("HAWB"))){
					smiMap.put("SEND_COMP", "KTNET");
				}else{
					smiMap.put("SEND_COMP", fhlmap.get("SEND_COMP"));
				}
			}
			smiMap.put("REV_RUT_KEY",CommonUtil.nullChk(fhlmap.get("MSG_CTRL_ID")));
			smiMap.put("STANDARDMESSAGEIDENTIFICATION",CommonUtil.nullChk(fhlmap.get("SMI")));
			smiMap.put("MESSAGETYPEVERSIONNUMBER",CommonUtil.nullChk(fhlmap.get("VERSION")));
			smiMap.put("AIRLINEPREFIX",CommonUtil.nullChk(fhlmap.get("MBI_PRE")));
			smiMap.put("AWBSERIALNUMBER",CommonUtil.nullChk(fhlmap.get("MBI_SUF")));
			smiMap.put("AIRPORTCITYCODE_ORG",CommonUtil.nullChk(fhlmap.get("M_ORG")));
			smiMap.put("AIRPORTCITYCODE_DES",CommonUtil.nullChk(fhlmap.get("M_DST")));
			smiMap.put("SHIPMENTDESCRIPTIONCODE",CommonUtil.nullChk(fhlmap.get("M_PC_CODE")));
			smiMap.put("NUMBEROFPIECES",CommonUtil.nullChk(fhlmap.get("M_PC")));
			smiMap.put("WEIGHTCODE",CommonUtil.nullChk(fhlmap.get("M_WT_CODE")));
			smiMap.put("WEIGHT",CommonUtil.nullChk(fhlmap.get("M_WT")));
			smiMap.put("HS_HWBSERIALNUMBER",CommonUtil.nullChk(fhlmap.get("HBS_NO")));
			smiMap.put("HS_AIRPORTCITYCODE_DEP",CommonUtil.nullChk(fhlmap.get("H_ORG")));
			smiMap.put("HS_AIRPORTCITYCODE_DES",CommonUtil.nullChk(fhlmap.get("H_DST")));
			smiMap.put("HS_NUMBEROFPIECES",CommonUtil.nullChk(fhlmap.get("H_PC")));
			smiMap.put("HS_WEIGHTCODE",CommonUtil.nullChk(fhlmap.get("H_WT_CODE")));
			smiMap.put("HS_WEIGHT",CommonUtil.nullChk(fhlmap.get("H_WT")));
			smiMap.put("HS_MANIFESTDESCRIPTIONOFGOODS",CommonUtil.nullChk(fhlmap.get("H_COMMODITY")));
			smiMap.put("SLAC",CommonUtil.nullChk(fhlmap.get("H_SLAC")));
			smiMap.put("HS_SPECIALHANDLINGCODE1",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE1")));
			smiMap.put("HS_SPECIALHANDLINGCODE2",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE2")));
			smiMap.put("HS_SPECIALHANDLINGCODE3",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE3")));
			smiMap.put("HS_SPECIALHANDLINGCODE4",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE4")));
			smiMap.put("HS_SPECIALHANDLINGCODE5",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE5")));
			smiMap.put("HS_SPECIALHANDLINGCODE6",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE6")));
			smiMap.put("HS_SPECIALHANDLINGCODE7",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE7")));
			smiMap.put("HS_SPECIALHANDLINGCODE8",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE8")));
			smiMap.put("HS_SPECIALHANDLINGCODE9",CommonUtil.nullChk(fhlmap.get("HS_SPECIALHANDLINGCODE9")));
			smiMap.put("GS_FREETEXT",CommonUtil.nullChk(fhlmap.get("T_FREETEXT")));
			smiMap.put("GS_FREETEXT1",CommonUtil.nullChk(fhlmap.get("T_FREETEXT1")));
			smiMap.put("GS_FREETEXT2",CommonUtil.nullChk(fhlmap.get("T_FREETEXT2")));
			smiMap.put("GS_FREETEXT3",CommonUtil.nullChk(fhlmap.get("T_FREETEXT3")));
			smiMap.put("GS_FREETEXT4",CommonUtil.nullChk(fhlmap.get("T_FREETEXT4")));
			smiMap.put("GS_FREETEXT5",CommonUtil.nullChk(fhlmap.get("T_FREETEXT5")));
			smiMap.put("GS_FREETEXT6",CommonUtil.nullChk(fhlmap.get("T_FREETEXT6")));
			smiMap.put("GS_FREETEXT7",CommonUtil.nullChk(fhlmap.get("T_FREETEXT7")));
			smiMap.put("GS_FREETEXT8",CommonUtil.nullChk(fhlmap.get("T_FREETEXT8")));
			smiMap.put("GS_FREETEXT9",CommonUtil.nullChk(fhlmap.get("T_FREETEXT9")));
			smiMap.put("GS_FREETEXT10",CommonUtil.nullChk(fhlmap.get("T_FREETEXT10")));
			smiMap.put("GS_FREETEXT11",CommonUtil.nullChk(fhlmap.get("T_FREETEXT11")));
			smiMap.put("GS_FREETEXT12",CommonUtil.nullChk(fhlmap.get("T_FREETEXT12")));
			smiMap.put("GS_FREETEXT13",CommonUtil.nullChk(fhlmap.get("T_FREETEXT13")));
			smiMap.put("GS_FREETEXT14",CommonUtil.nullChk(fhlmap.get("T_FREETEXT14")));
			smiMap.put("GS_FREETEXT15",CommonUtil.nullChk(fhlmap.get("T_FREETEXT15")));
			smiMap.put("GS_FREETEXT16",CommonUtil.nullChk(fhlmap.get("T_FREETEXT16")));
			smiMap.put("GS_FREETEXT17",CommonUtil.nullChk(fhlmap.get("T_FREETEXT17")));
			smiMap.put("GS_FREETEXT18",CommonUtil.nullChk(fhlmap.get("T_FREETEXT18")));
			smiMap.put("HARMONISED",CommonUtil.nullChk(fhlmap.get("HARMONISED")));
			smiMap.put("HARMONISED1",CommonUtil.nullChk(fhlmap.get("HARMONISED1")));
			smiMap.put("HARMONISED2",CommonUtil.nullChk(fhlmap.get("HARMONISED2")));
			smiMap.put("HARMONISED3",CommonUtil.nullChk(fhlmap.get("HARMONISED3")));
			smiMap.put("HARMONISED4",CommonUtil.nullChk(fhlmap.get("HARMONISED4")));
			smiMap.put("HARMONISED5",CommonUtil.nullChk(fhlmap.get("HARMONISED5")));
			smiMap.put("HARMONISED6",CommonUtil.nullChk(fhlmap.get("HARMONISED6")));
			smiMap.put("HARMONISED7",CommonUtil.nullChk(fhlmap.get("HARMONISED7")));
			smiMap.put("HARMONISED8",CommonUtil.nullChk(fhlmap.get("HARMONISED8")));
			smiMap.put("SH_NAME",CommonUtil.nullChk(fhlmap.get("SHP_NAME")));
			smiMap.put("SH_STREETADDRESS",CommonUtil.nullChk(fhlmap.get("SHP_ADDRESS")));
			smiMap.put("SH_PLACE",CommonUtil.nullChk(fhlmap.get("SHP_PLACE")));
			smiMap.put("SH_STATEPROVINCE",CommonUtil.nullChk(fhlmap.get("SHP_STATEPROVINCE")));
			smiMap.put("SH_ISOCOUNTRYCODE",CommonUtil.nullChk(fhlmap.get("SHP_ISOCOUNTRYCODE")));
			smiMap.put("SH_POSTCODE",CommonUtil.nullChk(fhlmap.get("SHP_POSTCODE")));
			smiMap.put("SH_CONTACTIDENTIFIER",CommonUtil.nullChk(fhlmap.get("SHP_CONTACTIDENTIFIER")));
			smiMap.put("SH_CONTACTNUMBER",CommonUtil.nullChk(fhlmap.get("SHP_CONTACTNUMBER")));
			smiMap.put("SH_CONTACTIDENTIFIER1",CommonUtil.nullChk(fhlmap.get("SHP_CONTACTIDENTIFIER1")));
			smiMap.put("SH_CONTACTNUMBER1",CommonUtil.nullChk(fhlmap.get("SHP_CONTACTNUMBER1")));
			smiMap.put("CN_NAME",CommonUtil.nullChk(fhlmap.get("CNE_NAME")));
			smiMap.put("CN_STREETADDRESS",CommonUtil.nullChk(fhlmap.get("CNE_ADDRESS")));
			smiMap.put("CN_PLACE",CommonUtil.nullChk(fhlmap.get("CNE_PLACE")));
			smiMap.put("CN_STATEPROVINCE",CommonUtil.nullChk(fhlmap.get("CNE_STATEPROVINCE")));
			smiMap.put("CN_ISOCOUNTRYCODE",CommonUtil.nullChk(fhlmap.get("CNE_ISOCOUNTRYCODE")));
			smiMap.put("CN_POSTCODE",CommonUtil.nullChk(fhlmap.get("CNE_POSTCODE")));
			smiMap.put("CN_CONTACTIDENTIFIER",CommonUtil.nullChk(fhlmap.get("CNE_CONTACTIDENTIFIER")));
			smiMap.put("CN_CONTACTNUMBER",CommonUtil.nullChk(fhlmap.get("CNE_CONTACTNUMBER")));
			smiMap.put("CN_CONTACTIDENTIFIER1",CommonUtil.nullChk(fhlmap.get("CNE_CONTACTIDENTIFIER1")));
			smiMap.put("CN_CONTACTNUMBER1",CommonUtil.nullChk(fhlmap.get("CN_CONTACTNUMBER1")));
			smiMap.put("CH_ISOCURRENCYCODE",CommonUtil.nullChk(fhlmap.get("CVD_ISOCURRENCYCODE")));
			smiMap.put("CH_CHARGEDECLARATIONS",CommonUtil.nullChk(fhlmap.get("CVD_CHARGEDECLARATIONS")));
			smiMap.put("CH_CARRIAGEDECLARATIONS",CommonUtil.nullChk(fhlmap.get("CVD_CARRIAGEDECLARATIONS")));
			smiMap.put("CH_CUSTOMSDECLARATION",CommonUtil.nullChk(fhlmap.get("CVD_CUSTOMSDECLARATION")));
			smiMap.put("CH_INSURANCEDECLARATION",CommonUtil.nullChk(fhlmap.get("CVD_INSURANCEDECLARATION")));
			smiMap.put("CU_FLIGHTDATE",CommonUtil.nullChk(fhlmap.get("FLT")));
			smiMap.put("CU_AIRLINECODE",CommonUtil.nullChk(fhlmap.get("FLT_AIRLINECODE")));
			smiMap.put("CU_FLIGHTNUMBER",CommonUtil.nullChk(fhlmap.get("FLT_NUM")));
			smiMap.put("CU_DECONSOLCODE",CommonUtil.nullChk(fhlmap.get("FLT_DECONSOL")));
			smiMap.put("CU_DECONSOLNAME",CommonUtil.nullChk(fhlmap.get("DECONSOL_NAME")));
			smiMap.put("CU_LOCATION1",CommonUtil.nullChk(fhlmap.get("FLT_LOCATION1")));
			smiMap.put("CU_LOCATION2",CommonUtil.nullChk(fhlmap.get("FLT_LOCATION2")));
/*			smiMap.put("OCI_SEQ",CommonUtil.nullChk(fhlmap.get("OCI_SEQ")));
			smiMap.put("OCI_ISO_CODE",CommonUtil.nullChk(fhlmap.get("OCI_ISO_CODE")));
			smiMap.put("OCI_INFO_ID",CommonUtil.nullChk(fhlmap.get("OCI_TYPE")));
			smiMap.put("OCI_CUS_INFO_ID",CommonUtil.nullChk(fhlmap.get("OCI_FLAG")));
			smiMap.put("OCI_CUS_ORG_CODE",CommonUtil.nullChk(fhlmap.get("")));
			smiMap.put("OCI_SUP_INFO1",CommonUtil.nullChk(fhlmap.get("LINE")));
			smiMap.put("OCI_CARGO_TYPE",CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")));
			smiMap.put("OCI_HSN",CommonUtil.nullChk(fhlmap.get("OCI_HSN")));
			smiMap.put("OCI_GOODSCODE",CommonUtil.nullChk(fhlmap.get("OCI_GOODSCODE")));*/
			//System.out.println("commonsql의 tgt_pima 확인 ==================>"+CommonUtil.nullChk(fhlmap.get("TGT_PIMA")));
			//System.out.println("ERROR_DESC : "+fhlmap.get("ERROR_DESC").toString());
			//System.out.println("ERROR_DESC_KOR : "+fhlmap.get("ERROR_DESC_KOR").toString());
			//System.out.println("@@@@@@ : "+smiMap.get("ORG_ROUTE").toString());
			result = (int) sqlMapClient.update("INSERT_UPDATE.insertFHL",smiMap);
			//sqlMapClient.commitTransaction();
		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
		}

		return result;
	}

	public int insertFHLOCI(HashMap<String, Object> fhlmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();
			String DOC_NO = CommonUtil.nullChk(fhlmap.get("MBI_FULL")).toString()+"_"+CommonUtil.nullChk(fhlmap.get("HBS_NO")).toString()+"_"+CommonUtil.nullChk(fhlmap.get("VERSION")).toString();
			smiMap.put("DOC_NO",CommonUtil.nullChk(DOC_NO));
			smiMap.put("OCI_SEQ",CommonUtil.nullChk(fhlmap.get("OCI_SEQ")));
			smiMap.put("TGT_PIMA",CommonUtil.nullChk(fhlmap.get("TGT_PIMA")));
			smiMap.put("OCI_ISO_CODE",CommonUtil.nullChk(fhlmap.get("OCI_ISO_CODE")));
			smiMap.put("OCI_INFO_ID",CommonUtil.nullChk(fhlmap.get("OCI_TYPE")));
			smiMap.put("OCI_CUS_INFO_ID",CommonUtil.nullChk(fhlmap.get("OCI_FLAG")));
			smiMap.put("OCI_CUS_ORG_CODE",CommonUtil.nullChk(fhlmap.get("")));
			smiMap.put("OCI_SUP_INFO1",CommonUtil.nullChk(fhlmap.get("OCI_SUP_INFO1")));
			smiMap.put("OCI_SUP_INFO2",CommonUtil.nullChk(fhlmap.get("OCI_SUP_INFO2")));
			smiMap.put("OCI_EPN_NO",CommonUtil.nullChk(fhlmap.get("OCI_EPN_NO")));
			smiMap.put("OCI_EPN_PC",CommonUtil.nullChk(fhlmap.get("OCI_EPN_PC")));
			smiMap.put("OCI_EPN_WT",CommonUtil.nullChk(fhlmap.get("OCI_EPN_WT")));
			smiMap.put("OCI_EPN_SPACK_CODE",CommonUtil.nullChk(fhlmap.get("OCI_EPN_SPACK_CODE")));
			smiMap.put("OCI_EPN_SPACK_PC",CommonUtil.nullChk(fhlmap.get("OCI_EPN_SPACK_PC")));
			smiMap.put("OCI_EPN_SPLIT_YN",CommonUtil.nullChk(fhlmap.get("OCI_EPN_SPLIT_YN")));
			smiMap.put("OCI_EPN_SPLIT_SEQ",CommonUtil.nullChk(fhlmap.get("OCI_EPN_SPLIT_SEQ")));
			smiMap.put("OCI_CARGO_TYPE",CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")));
			smiMap.put("OCI_HSN",CommonUtil.nullChk(fhlmap.get("OCI_HSN")));
			smiMap.put("OCI_GOODSCODE",CommonUtil.nullChk(fhlmap.get("OCI_GOODSCODE")));
			smiMap.put("TRX_ROUTE",CommonUtil.nullChk(fhlmap.get("AMS_TRX_ROUTE")));
			smiMap.put("SHIPPER_TARE",CommonUtil.nullChk(fhlmap.get("OCI_SHIP_CODE")));

			//System.out.println("ERROR_DESC : "+fhlmap.get("ERROR_DESC").toString());
			//System.out.println("ERROR_DESC_KOR : "+fhlmap.get("ERROR_DESC_KOR").toString());
			//System.out.println("@@@@@@ : "+smiMap.get("ORG_ROUTE").toString());
			result = (int) sqlMapClient.update("INSERT_UPDATE.insertFHLOCI",smiMap);
		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
		}


		return result;
	}

	public int AckupdateTrace(HashMap<String, Object> logMap, HashMap<String, Object> fhlmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();

			//sqlMapClient.startTransaction();
			//smiMap.put("AMS_TRX_ROUTE", CommonUtil.nullChk(ams_out_reverse_key));
			if(fhlmap.get("TRX_ROUTE") != null){
				smiMap.put("AMS_TRX_ROUTE", CommonUtil.nullChk(fhlmap.get("TRX_ROUTE")));
			}else{
				smiMap.put("AMS_TRX_ROUTE", CommonUtil.nullChk(fhlmap.get("AMS_TRX_ROUTE")));
			}
			if(fhlmap.get("AMS_ACK") != null){
				byte[] ams_ack = fhlmap.get("AMS_ACK").toString().getBytes("UTF-8");
				fhlmap.put("AMS_ACKMSG", ams_ack);
				smiMap.put("AMS_ACK", fhlmap.get("AMS_ACKMSG"));
			}
			if(fhlmap.get("MSB_ACK") != null){
				byte[] msb_ack = fhlmap.get("MSB_ACK").toString().getBytes("UTF-8");
				fhlmap.put("MSB_ACKMSG", msb_ack);
				smiMap.put("MSB_ACK", fhlmap.get("MSB_ACKMSG"));
			}
			if(fhlmap.get("KE_AIR_ACK") != null){
				byte[] air_ack = fhlmap.get("KE_AIR_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KE_AIR_ACKMSG", air_ack);
				smiMap.put("KE_AIR_ACK", fhlmap.get("KE_AIR_ACKMSG"));
			}
			if(fhlmap.get("OAL_AIR_ACK") != null){
				byte[] oal_air_ack = fhlmap.get("OAL_AIR_ACK").toString().getBytes("UTF-8");
				fhlmap.put("OAL_AIR_ACKMSG", oal_air_ack);
				smiMap.put("OAL_AIR_ACK", fhlmap.get("OAL_AIR_ACKMSG"));
			}
			if(fhlmap.get("KAMS_ACK") != null){
				byte[] kams_ack = fhlmap.get("KAMS_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KAMS_ACKMSG", kams_ack);
				smiMap.put("KAMS_ACK", fhlmap.get("KAMS_ACKMSG"));
			}
			if(fhlmap.get("KTNET_ACK") != null){
				byte[] ktnet_ack = fhlmap.get("KTNET_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KTNET_ACKMSG", ktnet_ack);
				smiMap.put("KTNET_ACK", fhlmap.get("KTNET_ACKMSG"));
			}
			if(fhlmap.get("KTNET_MFCS_ACK") != null){
				byte[] kt_mfcs_ack = fhlmap.get("KTNET_MFCS_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KT_MFCS_ACKMSG", kt_mfcs_ack);
				smiMap.put("KTNET_MFCS_ACK", fhlmap.get("KT_MFCS_ACKMSG"));
			}
			if(fhlmap.get("KTNET_FHL_ACK") != null){
				byte[] kt_fhl_ack = fhlmap.get("KTNET_FHL_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KT_FHL_ACKMSG", kt_fhl_ack);
				smiMap.put("KTNET_FHL_ACK", fhlmap.get("KT_FHL_ACKMSG"));
			}
			if(fhlmap.get("KTNET_AMS_ACK") != null){
				byte[] kt_ams_ack = fhlmap.get("KTNET_AMS_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KT_AMS_ACKMSG", kt_ams_ack);
				smiMap.put("KTNET_AMS_ACK", fhlmap.get("KT_AMS_ACKMSG"));
			}
			if(fhlmap.get("KT_ACK_STATUS") != null){
				smiMap.put("KT_ACK_STATUS", fhlmap.get("KT_ACK_STATUS"));// KTNET ACK 상태 값
			}
			if(fhlmap.get("KT_MFCS_STATUS") != null){
				smiMap.put("KT_MFCS_STATUS", fhlmap.get("KT_MFCS_STATUS"));// KTNET MFCS ACK 상태 값
			}
			if(fhlmap.get("KT_FHL_STATUS") != null){
				smiMap.put("KT_FHL_STATUS", fhlmap.get("KT_FHL_STATUS"));// KTNET FHL ACK 상태 값
			}
			if(fhlmap.get("KT_AMS_STATUS") != null){
				smiMap.put("KT_AMS_STATUS", fhlmap.get("KT_AMS_STATUS"));// KTNET AMS ACK 상태 값
			}
			if(fhlmap.get("AMS_SMI") != null){
				smiMap.put("AMS_SMI", fhlmap.get("AMS_SMI"));// AMS ACK 상태 값
			}
			if(fhlmap.get("MSB_SMI") != null){
				smiMap.put("MSB_SMI", fhlmap.get("MSB_SMI"));// AMS ACK 상태 값
			}
			if(fhlmap.get("AIR_SMI") != null){
				smiMap.put("AIR_SMI", fhlmap.get("AIR_SMI"));// AMS ACK 상태 값
			}
			if(fhlmap.get("OAL_AIR_SMI") != null){
				smiMap.put("OAL_AIR_SMI", fhlmap.get("OAL_AIR_SMI"));// AMS ACK 상태 값
			}
			if(fhlmap.get("IKAMS_SMI") != null){
				smiMap.put("IKAMS_SMI", fhlmap.get("IKAMS_SMI"));// IKAMS ACK 상태 값
			}
			if(CommonUtil.nullChk(fhlmap.get("SUCCESS_FLAG")).equals("N")){
				if(fhlmap.get("IKAMS_ERRMSG") != null){
					smiMap.put("IKAMS_ERRMSG", fhlmap.get("IKAMS_ERRMSG"));// ERRORMSG 상태 값
				}
				if(fhlmap.get("IKAMS_ERRMSG_KOR") != null){
					smiMap.put("IKAMS_ERRMSG_KOR", fhlmap.get("IKAMS_ERRMSG_KOR"));// ERRORMSG_KOR 상태 값
				}
			}
			if(fhlmap.get("KTNET_AIR_ACK") != null){
				byte[] ktnet_ack = fhlmap.get("KTNET_AIR_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KTNET_AIR_ACKMSG", ktnet_ack);
				smiMap.put("KTNET_AIR_ACK", fhlmap.get("KTNET_AIR_ACKMSG"));
			}
			if(fhlmap.get("KT_AIR_STATUS") != null){
				smiMap.put("KT_AIR_STATUS", fhlmap.get("KT_AIR_STATUS"));// AMS ACK 상태 값
			}
			if(fhlmap.get("KCNET_ACPS_ACK") != null){
				byte[] ktnet_ack = fhlmap.get("KCNET_ACPS_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KC_ACPS_ACKMSG", ktnet_ack);
				smiMap.put("KCNET_ACPS_ACK", fhlmap.get("KC_ACPS_ACKMSG"));
			}
			if(fhlmap.get("KC_ACPS_STATUS") != null){
				smiMap.put("KC_ACPS_STATUS", fhlmap.get("KC_ACPS_STATUS"));// AMS ACK 상태 값
			}
			if(fhlmap.get("KCNET_MFCS_ACK") != null){
				byte[] ktnet_ack = fhlmap.get("KCNET_MFCS_ACK").toString().getBytes("UTF-8");
				fhlmap.put("KC_MFCS_ACKMSG", ktnet_ack);
				smiMap.put("KCNET_MFCS_ACK", fhlmap.get("KC_MFCS_ACKMSG"));
			}
			if(fhlmap.get("KC_MFCS_STATUS") != null){
				smiMap.put("KC_MFCS_STATUS", fhlmap.get("KC_MFCS_STATUS"));// AMS ACK 상태 값
			}
			if(fhlmap.get("ACK_DESCRIPTION") != null){
				String ack_line = fhlmap.get("ACK_DESCRIPTION").toString().replaceAll("ACK/", "").replace("/", " ");
				smiMap.put("ACK_DESCRIPTION", ack_line);
			}
			result = (int) sqlMapClient.update("INSERT_UPDATE.ACKUpdateTrace",smiMap);
			//sqlMapClient.commitTransaction();
		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
		}
		return result;
	}



	public int insertKTTrace(Map<String, Object> ktmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();

			//sqlMapClient.startTransaction();

			smiMap.put("KTNET_ROUTE", ktmap.get("KTNET_ROUTE"));
			smiMap.put("DIVISION_MSG", ktmap.get("DIVISION_MSG"));

			result = (int) sqlMapClient.update("INSERT_UPDATE.insertKTTrace",smiMap);

			//sqlMapClient.commitTransaction();
		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
		}

		return result;
	}

	public int updateTgtPoint(String ams_out_reverse_key, HashMap<String, Object> logMap, HashMap<String, Object> fhlmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();

			smiMap.put("AMS_TRX_ROUTE", CommonUtil.nullChk(ams_out_reverse_key));
			smiMap.put("TGT_POINT", CommonUtil.nullChk(fhlmap.get("TGT_POINT")));
			smiMap.put("IKAMS_POINT", CommonUtil.nullChk(fhlmap.get("IKAMS_POINT")));
			smiMap.put("ROUTE_POINT", CommonUtil.nullChk(fhlmap.get("ROUTE_POINT")));
			smiMap.put("IKAMS_ROUTE_POINT", CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTE_POINT")));

			result = (int) sqlMapClient.update("INSERT_UPDATE.UpdateTgtPoint",smiMap);
		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
		}

		return result;
	}

	public int reprocTrace(HashMap logMap) throws Exception {
		int result = 0;
		try{
			result = (int) sqlMapClient.update("INSERT_UPDATE.reprocTrace",logMap);

		}catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * TM_EXCEPTION TABLE INSERT
	 * @param fhlmap
	 * @return
	 * @throws Exception
	 */
	public int insertException(String ams_out_reverse_key, String system, String subject, byte[] err_bytes, byte[] msg_bytes) throws Exception {
		int result = 0;
		try {
	    	HashMap errMap = new HashMap<String,Object>();
	    	errMap.put("AMS_TRX_ROUTE", CommonUtil.nullChk(ams_out_reverse_key));
	    	errMap.put("SYSTEM_NAME", CommonUtil.nullChk(system));
	    	errMap.put("SUBJECT", CommonUtil.nullChk(subject));
	    	errMap.put("ERR_MSG", err_bytes);
	    	errMap.put("ORG_MSG", msg_bytes);
	    	result = (int) sqlMapClient.update("COMMON_SQL.insertException",errMap);
		}catch (Exception e) {
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
		}

		return result;
	}

	// 20241105 FHL OCI CIMP 수정 - ACAS F/U
	public int insertOCIETC(HashMap<String, Object> fhlmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();
			smiMap.put("TRX_ROUTE",CommonUtil.nullChk(fhlmap.get("AMS_TRX_ROUTE")));
			smiMap.put("MAWB_NO", CommonUtil.nullChk(fhlmap.get("MAWB")));
			smiMap.put("HAWB_NO", CommonUtil.nullChk(fhlmap.get("HAWB"),"-"));
			smiMap.put("OCI_ETC_SEQ",CommonUtil.nullChk(fhlmap.get("OCI_ETC_SEQ")));
			smiMap.put("OCI_ETC_ISO_CODE",CommonUtil.nullChk(fhlmap.get("OCI_ETC_ISO_CODE")));
			smiMap.put("OCI_ETC_INFO_ID",CommonUtil.nullChk(fhlmap.get("OCI_ETC_INFO_ID")));
			smiMap.put("OCI_ETC_CUS_INFO_ID",CommonUtil.nullChk(fhlmap.get("OCI_ETC_CUS_INFO_ID")));
			smiMap.put("OCI_ETC_SUP_INFO",CommonUtil.nullChk(fhlmap.get("OCI_ETC_SUP_INFO")));

			result = (int) sqlMapClient.update("INSERT_UPDATE.insertOCIETC",smiMap);

		} catch(Exception e) {
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
		}

		return result;
	}

	public int insertOCICN(String ams_out_reverse_key, HashMap<String, Object> logMap, HashMap<String, Object> fhlmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();

			smiMap.put("TRX_ROUTE",CommonUtil.nullChk(ams_out_reverse_key));
			smiMap.put("MAWB_NO", CommonUtil.nullChk(fhlmap.get("MAWB")));
			smiMap.put("HAWB_NO", CommonUtil.nullChk(fhlmap.get("HAWB"),"-"));
			smiMap.put("OCI_SHP_T_CN",CommonUtil.nullChk(fhlmap.get("OCI_SHP_T_CN")));
			smiMap.put("OCI_SHP_T_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_SHP_T_FLAG")));
			smiMap.put("OCI_SHP_T_NUM",CommonUtil.nullChk(fhlmap.get("OCI_SHP_T_NUM")));
			smiMap.put("OCI_SHP_KC_CN",CommonUtil.nullChk(fhlmap.get("OCI_SHP_KC_CN")));
			smiMap.put("OCI_SHP_KC_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_SHP_KC_FLAG")));
			smiMap.put("OCI_SHP_KC_PERSON",CommonUtil.nullChk(fhlmap.get("OCI_SHP_KC_PERSON")));
			smiMap.put("OCI_SHP_U_CN",CommonUtil.nullChk(fhlmap.get("OCI_SHP_U_CN")));
			smiMap.put("OCI_SHP_U_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_SHP_U_FLAG")));
			smiMap.put("OCI_SHP_U_NUM",CommonUtil.nullChk(fhlmap.get("OCI_SHP_U_NUM")));
			smiMap.put("OCI_SHP_E_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_SHP_E_FLAG")));
			smiMap.put("OCI_SHP_E_CN",CommonUtil.nullChk(fhlmap.get("OCI_SHP_E_CN")));
			smiMap.put("OCI_SHP_E_NUM",CommonUtil.nullChk(fhlmap.get("OCI_SHP_E_NUM")));
			smiMap.put("OCI_CNE_T_CN",CommonUtil.nullChk(fhlmap.get("OCI_CNE_T_CN")));
			smiMap.put("OCI_CNE_T_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_CNE_T_FLAG")));
			smiMap.put("OCI_CNE_T_NUM",CommonUtil.nullChk(fhlmap.get("OCI_CNE_T_NUM")));
			smiMap.put("OCI_CNE_KC_CN",CommonUtil.nullChk(fhlmap.get("OCI_CNE_KC_CN")));
			smiMap.put("OCI_CNE_KC_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_CNE_KC_FLAG")));
			smiMap.put("OCI_CNE_KC_PERSON",CommonUtil.nullChk(fhlmap.get("OCI_CNE_KC_PERSON")));
			smiMap.put("OCI_CNE_U_CN",CommonUtil.nullChk(fhlmap.get("OCI_CNE_U_CN")));
			smiMap.put("OCI_CNE_U_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_CNE_U_FLAG")));
			smiMap.put("OCI_CNE_U_NUM",CommonUtil.nullChk(fhlmap.get("OCI_CNE_U_NUM")));
			smiMap.put("OCI_CNE_E_CN",CommonUtil.nullChk(fhlmap.get("OCI_CNE_E_CN")));
			smiMap.put("OCI_CNE_E_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_CNE_E_FLAG")));
			smiMap.put("OCI_CNE_E_NUM",CommonUtil.nullChk(fhlmap.get("OCI_CNE_E_NUM")));
			smiMap.put("OCI_NFY_T_CN",CommonUtil.nullChk(fhlmap.get("OCI_NFY_T_CN")));
			smiMap.put("OCI_NFY_T_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_NFY_T_FLAG")));
			smiMap.put("OCI_NFY_T_NUM",CommonUtil.nullChk(fhlmap.get("OCI_NFY_T_NUM")));
			smiMap.put("OCI_NFY_KC_CN",CommonUtil.nullChk(fhlmap.get("OCI_NFY_KC_CN")));
			smiMap.put("OCI_NFY_KC_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_NFY_KC_FLAG")));
			smiMap.put("OCI_NFY_KC_PERSON",CommonUtil.nullChk(fhlmap.get("OCI_NFY_KC_PERSON")));
			smiMap.put("OCI_NFY_U_CN",CommonUtil.nullChk(fhlmap.get("OCI_NFY_U_CN")));
			smiMap.put("OCI_NFY_U_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_NFY_U_FLAG")));
			smiMap.put("OCI_NFY_U_NUM",CommonUtil.nullChk(fhlmap.get("OCI_NFY_U_NUM")));
			smiMap.put("OCI_NFY_E_CN",CommonUtil.nullChk(fhlmap.get("OCI_NFY_E_CN")));
			smiMap.put("OCI_NFY_E_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_NFY_E_FLAG")));
			smiMap.put("OCI_NFY_E_NUM",CommonUtil.nullChk(fhlmap.get("OCI_NFY_E_NUM")));

			/*System.out.println("TRX_ROUTE : "+ smiMap.get("TRX_ROUTE"));
			System.out.println("MAWB_NO : "+ smiMap.get("MAWB_NO"));
			System.out.println("HAWB_NO : "+ smiMap.get("HAWB_NO"));

			System.out.println("OCI SHP CRN CN : "+ smiMap.get("OCI_SHP_T_CN"));
			System.out.println("OCI SHP CRN FLAG : "+ smiMap.get("OCI_SHP_T_FLAG"));
			System.out.println("OCI SHP CRN NUM: "+ smiMap.get("OCI_SHP_T_NUM"));
			System.out.println("OCI SHP CRN CN : "+ smiMap.get("OCI_SHP_KC_CN"));
			System.out.println("OCI SHP CTC FLAG : "+ smiMap.get("OCI_SHP_KC_FLAG"));
			System.out.println("OCI SHP CTC PERSON: "+ smiMap.get("OCI_SHP_KC_PERSON"));
			System.out.println("OCI SHP CRN CN : "+ smiMap.get("OCI_SHP_U_CN"));
			System.out.println("OCI SHP TEL FLAG : "+ smiMap.get("OCI_SHP_U_FLAG"));
			System.out.println("OCI SHP TEL NUM: "+ smiMap.get("OCI_SHP_U_NUM"));
			System.out.println("OCI SHP CRN CN : "+ smiMap.get("OCI_SHP_E_CN"));
			System.out.println("OCI SHP AEO FLAG : "+ smiMap.get("OCI_SHP_E_FLAG"));
			System.out.println("OCI SHP AEO NUM: "+ smiMap.get("OCI_SHP_E_NUM"));

			System.out.println("OCI CNE CRN CN : "+ smiMap.get("OCI_CNE_T_CN"));
			System.out.println("OCI CNE CRN FLAG : "+ smiMap.get("OCI_CNE_T_FLAG"));
			System.out.println("OCI CNE CRN NUM: "+ smiMap.get("OCI_CNE_T_NUM"));
			System.out.println("OCI CNE CRN CN : "+ smiMap.get("OCI_CNE_KC_CN"));
			System.out.println("OCI CNE CTC FLAG : "+ smiMap.get("OCI_CNE_KC_FLAG"));
			System.out.println("OCI CNE CTC PERSON: "+ smiMap.get("OCI_CNE_KC_PERSON"));
			System.out.println("OCI CNE CRN CN : "+ smiMap.get("OCI_CNE_U_CN"));
			System.out.println("OCI CNE TEL FLAG : "+ smiMap.get("OCI_CNE_U_FLAG"));
			System.out.println("OCI CNE TEL NUM: "+ smiMap.get("OCI_CNE_U_NUM"));
			System.out.println("OCI CNE CRN CN : "+ smiMap.get("OCI_CNE_E_CN"));
			System.out.println("OCI CNE AEO FLAG : "+ smiMap.get("OCI_CNE_E_FLAG"));
			System.out.println("OCI CNE AEO NUM: "+ smiMap.get("OCI_CNE_E_NUM"));

			System.out.println("OCI NFY CRN CN : "+ smiMap.get("OCI_NFY_T_CN"));
			System.out.println("OCI NFY CRN FLAG : "+ smiMap.get("OCI_NFY_T_FLAG"));
			System.out.println("OCI NFY CRN NUM: "+ smiMap.get("OCI_NFY_T_NUM"));
			System.out.println("OCI NFY CRN CN : "+ smiMap.get("OCI_NFY_KC_CN"));
			System.out.println("OCI NFY CTC FLAG : "+ smiMap.get("OCI_NFY_KC_FLAG"));
			System.out.println("OCI NFY CTC PERSON: "+ smiMap.get("OCI_NFY_KC_PERSON"));
			System.out.println("OCI NFY CRN CN : "+ smiMap.get("OCI_NFY_U_CN"));
			System.out.println("OCI NFY TEL FLAG : "+ smiMap.get("OCI_NFY_U_FLAG"));
			System.out.println("OCI NFY TEL NUM: "+ smiMap.get("OCI_NFY_U_NUM"));
			System.out.println("OCI NFY CRN CN : "+ smiMap.get("OCI_NFY_E_CN"));
			System.out.println("OCI NFY AEO FLAG : "+ smiMap.get("OCI_NFY_E_FLAG"));
			System.out.println("OCI NFY AEO NUM: "+ smiMap.get("OCI_NFY_E_NUM"));*/


			result = (int) sqlMapClient.update("INSERT_UPDATE.insertOCICN",smiMap);
			//sqlMapClient.commitTransaction();
		}/*catch(SQLException e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
			throw new SQLException();
		}*/catch(Exception e){
			logger.info("ERROR===> " + e.toString());
			e.printStackTrace();
			//Exception 난 값 catch 문으로 전달
		}

		return result;
	}


}