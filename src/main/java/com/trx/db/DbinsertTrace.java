package com.trx.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;
import com.trx.validate.ReverseKey;
import com.ibatis.sqlmap.client.SqlMapClient;

public class DbinsertTrace {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ReverseKey.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Produce
	ProducerTemplate producer;

	@Autowired
	CommonSql commonSql;

	@Value("#{prop['ikams.done.yn']}")
	//@Value("${ktnet.write.path}")
	private String done_yn;

	@Autowired
	MainExceptionManager mainExceptionManager;

	public int ikamsExpInsert(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();
			smiMap.put("FLT",CommonUtil.nullChk(fhlmap.get("FLT")));
			smiMap.put("CARR_CODE",CommonUtil.nullChk(fhlmap.get("FLT_AIRLINECODE")));
			smiMap.put("FLT_NUM",CommonUtil.nullChk(fhlmap.get("FLT_NUM")));
			smiMap.put("MBI_NO",CommonUtil.nullChk(fhlmap.get("MBI_NO")));
			smiMap.put("HBS_NO",CommonUtil.nullChk(fhlmap.get("HBS_NO")));
			smiMap.put("OCI_CARGO_TYPE",CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")));
			smiMap.put("M_PC",CommonUtil.nullChk(fhlmap.get("M_PC")));
			smiMap.put("M_WT",CommonUtil.nullChk(fhlmap.get("M_WT")));
			smiMap.put("H_PC",CommonUtil.nullChk(fhlmap.get("H_PC")));
			smiMap.put("H_WT",CommonUtil.nullChk(fhlmap.get("H_WT")));
			smiMap.put("H_SLAC",CommonUtil.nullChk(fhlmap.get("H_SLAC")));
			smiMap.put("H_COMMODITY",CommonUtil.nullChk(fhlmap.get("H_COMMODITY")));
			smiMap.put("TXT_COMODITY",CommonUtil.nullChk(fhlmap.get("TXT_COMODITY")));
			if(fhlmap.get("SNM_NAME") != null){
				smiMap.put("SHP_NAME",CommonUtil.nullChk(fhlmap.get("SNM_NAME")));
			}else{
				smiMap.put("SHP_NAME",CommonUtil.nullChk(fhlmap.get("SHP_NAME")));
			}
			if(fhlmap.get("SAR_ADDR") != null){
				smiMap.put("SHP_FULL_ADDR",CommonUtil.nullChk(fhlmap.get("SAR_ADDR")));
			}else{
				smiMap.put("SHP_FULL_ADDR",CommonUtil.nullChk(fhlmap.get("SHP_FULL_ADDR")));
			}
			smiMap.put("SHP_CONTACTNUMBER",CommonUtil.nullChk(fhlmap.get("SHP_CONTACTNUMBER")));
			if(fhlmap.get("CNM_NAME") != null){
				smiMap.put("CNE_NAME",CommonUtil.nullChk(fhlmap.get("CNM_NAME")));
			}else{
				smiMap.put("CNE_NAME",CommonUtil.nullChk(fhlmap.get("CNE_NAME")));
			}
			if(fhlmap.get("CAR_ADDR") != null){
				smiMap.put("CNE_FULL_ADDR",CommonUtil.nullChk(fhlmap.get("CAR_ADDR")));
			}else{
				smiMap.put("CNE_FULL_ADDR",CommonUtil.nullChk(fhlmap.get("CNE_FULL_ADDR")));
			}
			smiMap.put("CNE_CONTACTNUMBER",CommonUtil.nullChk(fhlmap.get("CNE_CONTACTNUMBER")));
			smiMap.put("OCI_SEQ",CommonUtil.nullChk(fhlmap.get("OCI_SEQ")));
			smiMap.put("FLT_DECONSOL",CommonUtil.nullChk(fhlmap.get("FLT_DECONSOL")));
			smiMap.put("AMS_TRX_ROUTE",CommonUtil.nullChk(ams_out_reverse_key));
			smiMap.put("H_ORG",CommonUtil.nullChk(fhlmap.get("H_ORG")));
			smiMap.put("H_DST",CommonUtil.nullChk(fhlmap.get("H_DST")));
			smiMap.put("M_ORG",CommonUtil.nullChk(fhlmap.get("M_ORG")));
			smiMap.put("M_DST",CommonUtil.nullChk(fhlmap.get("M_DST")));
			smiMap.put("SRC_PIMA",CommonUtil.nullChk(fhlmap.get("SRC_PIMA")));
			if(fhlmap.get("EDI") != null){
				byte[] kams_msg = fhlmap.get("EDI").toString().getBytes("UTF-8");
				fhlmap.put("EDI_MSG", kams_msg);
				smiMap.put("EDI", fhlmap.get("EDI_MSG"));
			}
			smiMap.put("SHPR_CITY_C",CommonUtil.nullChk(fhlmap.get("SCC_CODE")));
			smiMap.put("CNEE_CITY_C",CommonUtil.nullChk(fhlmap.get("CCC_CODE")));

			smiMap.put("DONE_YN", done_yn);

			if(fhlmap.get("SEND_COMP") != null){
				if(fhlmap.get("SEND_COMP").equals(fhlmap.get("HAWB")) || fhlmap.get("SEND_COMP").equals("KTNETFHL")){
					smiMap.put("SENDER_ID", "KTNET");
				}else if(fhlmap.get("SEND_COMP").equals("KCNETFHL")){
					smiMap.put("SENDER_ID", "KCNET");
				}else{
					smiMap.put("SENDER_ID", "TRAXON");
				}
			}

			result = (int) sqlMapClient.update("INSERT_UPDATE.insertEXPhawb",smiMap);
			//신규 IKAMS 로직 2018.01.15 최재준 추가
			//result = (int) sqlMapClient.update("INSERT_UPDATE.insertNewEXPhawb",smiMap);

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

	public int ikamsExpEpnInsert(HashMap<String, Object> fhlmap) throws Exception {
		int result = 0;
		try{
			HashMap smiMap = new HashMap<String,Object>();
			//fhlmap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
			smiMap.put("FLT",CommonUtil.nullChk(fhlmap.get("FLT")));
			smiMap.put("CARR_CODE",CommonUtil.nullChk(fhlmap.get("FLT_AIRLINECODE")));
			smiMap.put("FLT_NUM",CommonUtil.nullChk(fhlmap.get("FLT_NUM")));
			smiMap.put("MBI_NO",CommonUtil.nullChk(fhlmap.get("MBI_NO")));
			smiMap.put("HBS_NO",CommonUtil.nullChk(fhlmap.get("HBS_NO")));
			smiMap.put("FLT_DECONSOL",CommonUtil.nullChk(fhlmap.get("FLT_DECONSOL")));
			smiMap.put("FLT_LOCATION1",CommonUtil.nullChk(fhlmap.get("FLT_LOCATION1")));
			smiMap.put("FLT_LOCATION2",CommonUtil.nullChk(fhlmap.get("FLT_LOCATION2")));
			smiMap.put("OCI_CARGO_TYPE",CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")));
			smiMap.put("OCI_SEQ",CommonUtil.nullChk(fhlmap.get("OCI_SEQ")));
			smiMap.put("OCI_EPN_NO",CommonUtil.nullChk(fhlmap.get("OCI_EPN_NO")));
			smiMap.put("OCI_EPN_PC",CommonUtil.nullChk(fhlmap.get("OCI_EPN_PC")));
			smiMap.put("OCI_EPN_WT",CommonUtil.nullChk(fhlmap.get("OCI_EPN_WT")));
			smiMap.put("OCI_EPN_SPACK_CODE",CommonUtil.nullChk(fhlmap.get("OCI_EPN_SPACK_CODE")));
			smiMap.put("OCI_EPN_SPACK_PC",CommonUtil.nullChk(fhlmap.get("OCI_EPN_SPACK_PC")));
			smiMap.put("OCI_EPN_SPLIT_YN",CommonUtil.nullChk(fhlmap.get("OCI_EPN_SPLIT_YN")));
			smiMap.put("OCI_EPN_SPLIT_SEQ",CommonUtil.nullChk(fhlmap.get("OCI_EPN_SPLIT_SEQ")));
			smiMap.put("AMS_TRX_ROUTE",CommonUtil.nullChk(fhlmap.get("AMS_TRX_ROUTE")));
			smiMap.put("SHIPPER_TARE",CommonUtil.nullChk(fhlmap.get("OCI_SHIP_CODE")));
			smiMap.put("EPN_ERR_YN",CommonUtil.nullChk(fhlmap.get("EPN_ERR_YN")));
			smiMap.put("EPN_CHK_MSG",CommonUtil.nullChk(fhlmap.get("EPN_CHK_MSG")));
			smiMap.put("DONE_YN", done_yn);


			result = (int) sqlMapClient.update("INSERT_UPDATE.insertEXPEpn",smiMap);
			//신규 IKAMS 로직 2018.01.15 최재준 추가
			//result = (int) sqlMapClient.update("INSERT_UPDATE.insertNewEXPEpn",smiMap);

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

	public int ikamsImpInsert(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {

		int result = 0;
		//int impcount = 0;
		try{

			HashMap smiMap = new HashMap<String,Object>();
			//fhlmap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
			smiMap.put("FLT",CommonUtil.nullChk(fhlmap.get("FLT")));
			smiMap.put("CARR_CODE",CommonUtil.nullChk(fhlmap.get("FLT_AIRLINECODE")));
			smiMap.put("FLT_NUM",CommonUtil.nullChk(fhlmap.get("FLT_NUM")));
			smiMap.put("MBI_NO",CommonUtil.nullChk(fhlmap.get("MBI_NO")));
			smiMap.put("HBS_NO",CommonUtil.nullChk(fhlmap.get("HBS_NO")));
			smiMap.put("OCI_CARGO_TYPE",CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")));
			smiMap.put("M_PC",CommonUtil.nullChk(fhlmap.get("M_PC")));
			smiMap.put("M_WT",CommonUtil.nullChk(fhlmap.get("M_WT")));
			smiMap.put("H_PC",CommonUtil.nullChk(fhlmap.get("H_PC")));
			smiMap.put("H_WT",CommonUtil.nullChk(fhlmap.get("H_WT")));
			smiMap.put("H_SLAC",CommonUtil.nullChk(fhlmap.get("H_SLAC"),"0"));
			smiMap.put("H_COMMODITY",CommonUtil.nullChk(fhlmap.get("H_COMMODITY")));
			smiMap.put("TXT_COMODITY",CommonUtil.nullChk(fhlmap.get("TXT_COMODITY")));
			if(fhlmap.get("SNM_NAME") != null){
				smiMap.put("SHP_NAME",CommonUtil.nullChk(fhlmap.get("SNM_NAME")));
			}else{
				smiMap.put("SHP_NAME",CommonUtil.nullChk(fhlmap.get("SHP_NAME")));
			}
			if(fhlmap.get("SAR_ADDR") != null){
				smiMap.put("SHP_FULL_ADDR",CommonUtil.nullChk(fhlmap.get("SAR_ADDR")));
			}else{
				smiMap.put("SHP_FULL_ADDR",CommonUtil.nullChk(fhlmap.get("SHP_FULL_ADDR")));
			}
			smiMap.put("SHP_CONTACTNUMBER",CommonUtil.nullChk(fhlmap.get("SHP_CONTACTNUMBER")));
			if(fhlmap.get("CNM_NAME") != null){
				smiMap.put("CNE_NAME",CommonUtil.nullChk(fhlmap.get("CNM_NAME")));
			}else{
				smiMap.put("CNE_NAME",CommonUtil.nullChk(fhlmap.get("CNE_NAME")));
			}
			if(fhlmap.get("CAR_ADDR") != null){
				smiMap.put("CNE_FULL_ADDR",CommonUtil.nullChk(fhlmap.get("CAR_ADDR")));
			}else{
				smiMap.put("CNE_FULL_ADDR",CommonUtil.nullChk(fhlmap.get("CNE_FULL_ADDR")));
			}
			smiMap.put("CNE_CONTACTNUMBER",CommonUtil.nullChk(fhlmap.get("CNE_CONTACTNUMBER")));
			smiMap.put("FLT_DECONSOL",CommonUtil.nullChk(fhlmap.get("FLT_DECONSOL")));
			smiMap.put("H_ORG",CommonUtil.nullChk(fhlmap.get("H_ORG")));
			smiMap.put("H_DST",CommonUtil.nullChk(fhlmap.get("H_DST")));
			smiMap.put("DECONSOL_NAME",CommonUtil.nullChk(fhlmap.get("DECONSOL_NAME")));
			smiMap.put("FLT_LOCATION1",CommonUtil.nullChk(fhlmap.get("FLT_LOCATION1")));
			smiMap.put("FLT_LOCATION2",CommonUtil.nullChk(fhlmap.get("FLT_LOCATION2")));
			smiMap.put("OCI_GOODSCODE",CommonUtil.nullChk(fhlmap.get("OCI_GOODSCODE")));
			smiMap.put("SMI",CommonUtil.nullChk(fhlmap.get("SMI")));
			smiMap.put("SRC_PIMA",CommonUtil.nullChk(fhlmap.get("SRC_PIMA")));
			//KE가 아니고, 한진 PIMA, 수입 특송(IMP/I) 일때 DEAFAULT로 M 값 입력
			/*if(!CommonUtil.nullChk(fhlmap.get("CARR_CODE")).equals("KE")
					&& CommonUtil.nullChk(fhlmap.get("SRC_PIMA")).equals("RKRAGT82AMSHNX01/SEL01")
					&& CommonUtil.nullChk(fhlmap.get("IMP_EXP_FLAG")).equals("IMP")
					&& CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("I")){

				smiMap.put("OCI_FLAG", "M");
			}else{
				smiMap.put("OCI_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_FLAG")));
			}*/
			smiMap.put("OCI_FLAG",CommonUtil.nullChk(fhlmap.get("OCI_FLAG")));
			smiMap.put("OCI_HSN",CommonUtil.nullChk(fhlmap.get("OCI_HSN")));
			smiMap.put("AMS_TRX_ROUTE",CommonUtil.nullChk(ams_out_reverse_key));
			smiMap.put("M_ORG",CommonUtil.nullChk(fhlmap.get("M_ORG")));
			smiMap.put("M_DST",CommonUtil.nullChk(fhlmap.get("M_DST")));
			smiMap.put("FLT_DECONSOL",CommonUtil.nullChk(fhlmap.get("FLT_DECONSOL")));
			if(fhlmap.get("EDI") != null){
				byte[] kams_msg = fhlmap.get("EDI").toString().getBytes("UTF-8");
				fhlmap.put("EDI_MSG", kams_msg);
				smiMap.put("EDI", fhlmap.get("EDI_MSG"));
			}
			smiMap.put("OCI_GOOD_STS_CODE",CommonUtil.nullChk(fhlmap.get("OCI_GOOD_STS_CODE")));
			smiMap.put("SHPR_CITY_C",CommonUtil.nullChk(fhlmap.get("SCC_CODE")));
			smiMap.put("CNEE_CITY_C",CommonUtil.nullChk(fhlmap.get("CCC_CODE")));
			smiMap.put("DONE_YN", done_yn);

			/*if(fhlmap.get("SEND_COMP") != null){
				if(fhlmap.get("SEND_COMP").equals("KTNETIMP")){
					smiMap.put("SENDER_ID", "TRAXON_KTNET");
				}else if(fhlmap.get("SEND_COMP").equals("KCNETFHL")){
					smiMap.put("SENDER_ID", "TRAXON_KCNET");
				}else{
					smiMap.put("SENDER_ID", "TRAXON");
				}
			}*/

			//MAWB,HBS,FLT,FLT_NUM,CARR,HSN 정보를 가지고 데이터 여부 확인 후 수정/삭제 FLAG 추가(KAMS_UPDT_YN 컬럼에 'I' 신규, 'U' 수정, 'D' 삭제)
			/*impcount = (int)sqlMapClient.queryForObject("TM_TRACE.imprevise", fhlmap);

			if(impcount > 0){
				fhlmap.put("KAMS_UPDT_YN", "U");
				smiMap.put("KAMS_UPDT_YN", CommonUtil.nullChk(fhlmap.get("KAMS_UPDT_YN")));
				result = (int) sqlMapClient.update("INSERT_UPDATE.insertIMPhawb",smiMap);
			}else{
				fhlmap.put("KAMS_UPDT_YN", "I");
				smiMap.put("KAMS_UPDT_YN", CommonUtil.nullChk(fhlmap.get("KAMS_UPDT_YN")));
				result = (int) sqlMapClient.update("INSERT_UPDATE.insertIMPhawb",smiMap);
			}*/
			result = (int) sqlMapClient.update("INSERT_UPDATE.insertIMPhawb",smiMap);
			//신규 IKAMS 로직 2018.01.15 최재준 추가
			//result = (int) sqlMapClient.update("INSERT_UPDATE.insertNewIMPhawb",smiMap);
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
}
