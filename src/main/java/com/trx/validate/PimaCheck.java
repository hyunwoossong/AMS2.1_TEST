package com.trx.validate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.trx.Msgprocess.CreateAck;
import com.trx.Msgprocess.CreateMSG;
import com.trx.db.AirlineSelect;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;

public class PimaCheck {

	private static final Logger logger = LoggerFactory.getLogger(AirlineSelect.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	SqlMapClient sqlMapClient_msb;

	@Autowired
	CommonSql commonSql;

	@Autowired
	CreateMSG createmsg = new CreateMSG();

	@Autowired
	CreateAck createack = new CreateAck();

	@Autowired
	MainExceptionManager mainExceptionManager;

	final static String AMS_STEP = "300";
	final static String IKAMS_STEP = "300";

	public void PimaCheck(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, @Body String msg, Exchange exchange) throws Exception {
		boolean flag = true;

		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> route_code = new HashMap<String,Object>();
		String fnaMsg;

		logger.info(" === FHL PIMA CHECK START === ");
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("EDI_PARSE");
		String PIMA_CODE = (String)fhlmap.get("SRC_PIMA");
		List result = sqlMapClient_msb.queryForList("TM_TRACE.pimacheck", PIMA_CODE);

		try{
			sqlMapClient.startTransaction();
			if(result.size() > 0){

				for (int i = 0; i < result.size(); i++) {
					route_code = (HashMap) result.get(i);

					fhlmap.put("PIMA", (String) CommonUtil.nullChk(route_code.get("PIMA")));
					fhlmap.put("KOR_NAME", (String) CommonUtil.nullChk(route_code.get("KOR_NAME")));
					fhlmap.put("ENG_NAME", (String) CommonUtil.nullChk(route_code.get("ENG_NAME")));
					fhlmap.put("COUNTRY_CODE", (String) CommonUtil.nullChk(route_code.get("COUNTRY_CODE")));
					fhlmap.put("CITY_CODE", (String) CommonUtil.nullChk(route_code.get("CITY_CODE")));
					fhlmap.put("PIMA_TYPE", (String) CommonUtil.nullChk(route_code.get("PIMA_TYPE")));
					fhlmap.put("AGT_CODE", (String) CommonUtil.nullChk(route_code.get("AGT_CODE")));
					fhlmap.put("KTNETCODE", (String) CommonUtil.nullChk(route_code.get("CSTM_COD_C")));
					fhlmap.put("IMP_ROUTE", (String) CommonUtil.nullChk(route_code.get("IMP_ROUTE")));
					fhlmap.put("IMP_USE_FLAG", (String) CommonUtil.nullChk(route_code.get("IMP_USE_FLAG")));
					fhlmap.put("AMS_USE_FLAG", (String) CommonUtil.nullChk(route_code.get("AMS_USE_FLAG")));
					fhlmap.put("KCNETCODE", (String) CommonUtil.nullChk(route_code.get("KCNETCODE")));
					fhlmap.put("EZC_PIMA", (String) CommonUtil.nullChk(route_code.get("EZC_PIMA")));

					if(fhlmap.get("SRC_PIMA").equals(fhlmap.get("PIMA"))){
						if(fhlmap.get("AMS_USE_FLAG").equals("Y")){
							//수입 pima 체크 여부
							if(CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals("IMP")){

								if(fhlmap.get("IMP_ROUTE") !=null && fhlmap.get("IMP_ROUTE").toString().length() > 0){
									if(fhlmap.get("IMP_USE_FLAG").equals("Y")){
										logger.info(" === FHL IMP PIMA CHECK TRUE === ");
									}else{
										if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
											fhlmap.put("ERRORMSG", "UNKNOWN PARTICIPANT IDENTIFIER FHL");
											fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - 미등록 PIMA");
										}
										logger.info(" === FHL IMP PIMA CHECK UNKNOWN === ");
										flag = false;
									}
								}else{
									if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
										fhlmap.put("ERRORMSG", "UNKNOWN PARTICIPANT IDENTIFIER FHL");
										fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - 미등록 PIMA");
									}
									logger.info(" === FHL IMP PIMA CHECK UNKNOWN === ");
									flag = false;
								}
							}

						}else{
							if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
								fhlmap.put("ERRORMSG","UNKNOWN PARTICIPANT IDENTIFIER FHL");
								fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - 미등록 PIMA");
							}
							logger.info(" === FHL PIMA CHECK UNKNOWN === ");
							flag = false;

						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","UNKNOWN PARTICIPANT IDENTIFIER FHL");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - 미등록 PIMA");
						}
						logger.info(" === FHL PIMA CHECK UNKNOWN === ");
						flag = false;
					}
					
					//CX 항공사 PIMA 등록여부 체크
					/*if(fhlmap.get("TGT_PIMA").equals("CX") && fhlmap.get("EZC_PIMA").equals("")){
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","UNKNOWN PARTICIPANT IDENTIFIER FWB CX");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - CX 미등록 PIMA");
						}
						logger.info(" === FWB PIMA CHECK UNKNOWN CX === ");
						flag = false;
					}*/
				}
			}else{
				if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
					fhlmap.put("ERRORMSG","UNKNOWN PARTICIPANT IDENTIFIER FHL");
					fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - 미등록 PIMA");
				}
				logger.info(" === FHL PIMA CHECK UNKNOWN === ");
				flag = false;
			}

			HashMap logMap = new HashMap();
			logMap.put("HISTORY_STATUS", "Pima_Code Check");
			logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
			logMap.put("AMS_STEP", AMS_STEP);
			logMap.put("IKAMS_STEP", IKAMS_STEP);

			commonSql.insertHistory(logMap);
			commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);

		}catch(IndexOutOfBoundsException e){
			logger.info(" === PIMA Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","PIMA Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === PIMA Check NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","PIMA Check NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === PIMA Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","PIMA Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === PIMA Check Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	flag = false;
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","PIMA Check Exception",e, msg);
		}finally{

			if(!flag){

				//FNA 생성
		    	fnaMsg = createack.FNAack(fhlmap);
		    	fhlmap.put("AMS_ACK", fnaMsg);
		    	fhlmap.put("AMS_SMI", "FNA");
				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "Pima Check Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", "309");
				logMap.put("IKAMS_STEP", "309");

				logger.info(" === FHL PIMA CHECK ERROR === ");
				commonSql.insertHistory(logMap);
				commonSql.AckupdateTrace(logMap, fhlmap);
			}else{

				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "Pima Check Success");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", AMS_STEP);
				logMap.put("IKAMS_STEP", IKAMS_STEP);

				logger.info(" === FHL PIMA CHECK SUCCESS === ");
				commonSql.insertHistory(logMap);

	    	}

			exchange.getIn().setHeader("Pima_Check_Flag", flag);
			exchange.getIn().setHeader("FHL_PIMA_LIST", fhlmap);
			exchange.getIn().setHeader("EDI_PARSE",fhlmap);

			try{
	    		sqlMapClient.commitTransaction();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}finally{
	    		sqlMapClient.endTransaction();
	    	}
		}


	}
}
