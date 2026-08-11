package com.trx.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.trx.Msgprocess.CreateAck;
import com.trx.Msgprocess.CreateMSG;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;

public class AirlineSelect {

	private static final Logger logger = LoggerFactory.getLogger(AirlineSelect.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	CommonSql commonSql;

	@Autowired
	CreateMSG createmsg = new CreateMSG();

	@Autowired
	DbinsertTrace dbinserttrace = new DbinsertTrace();

	@Autowired
	CreateAck createack = new CreateAck();

	@Autowired
	MainExceptionManager mainExceptionManager;

	@Produce
	ProducerTemplate producer;

	final static String AMS_STEP = "400";
	final static String IKAMS_STEP = "400";

	public void airlineSelect(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, @Body String msg, Exchange exchange) throws Exception {
		boolean flag = true;

		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> route_code = new HashMap<String,Object>();
		String fnaMsg;

		logger.info(" === FHL CARR CODE CHECK START === ");
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("EDI_PARSE");

		String CARR_CODE = (String)fhlmap.get("TGT_PIMA");
		List result = sqlMapClient.queryForList("TM_TRACE.airlineCheck", CARR_CODE);

		try{
			sqlMapClient.startTransaction();
			if(result.size() > 0){

				for (int i = 0; i < result.size(); i++) {
					route_code = (HashMap) result.get(i);

					fhlmap.put("CARR_CODE", (String) route_code.get("CARR_CODE"));

					if(fhlmap.get("TGT_PIMA").equals(fhlmap.get("CARR_CODE"))){
						logger.info(" === FHL CARR CODE CHECK TRUE === ");

					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG",  fhlmap.get("TGT_PIMA") +" - AMS SERVICE IS NOT AVAILABLE FOR THIS AIRLINE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("TGT_PIMA") +" - 서비스 하지 않는 항공사");
						}
						logger.info(" === FHL CARR CODE CHECK FALSE === ");
						flag = false;
					}
				}
			}else{
				if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
					fhlmap.put("ERRORMSG",  fhlmap.get("TGT_PIMA") +" - AMS SERVICE IS NOT AVAILABLE FOR THIS AIRLINE");
					fhlmap.put("ERRORMSG_KOR", fhlmap.get("TGT_PIMA") +" - 서비스 하지 않는 항공사");
				}
				logger.info(" === FHL CARR CODE CHECK FALSE === ");
				flag = false;
			}


			HashMap logMap = new HashMap();
			//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
			logMap.put("HISTORY_STATUS", "Carr_Code Check");
			logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
			logMap.put("AMS_STEP", AMS_STEP);
			logMap.put("IKAMS_STEP", IKAMS_STEP);

			commonSql.insertHistory(logMap);
			commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);

		}catch(IndexOutOfBoundsException e){
			logger.info(" === CARR CODE CHECK IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "CARR CODE CHECK IndexOutOfBoundsException", e, msg);
			flag = false;
		}catch(NullPointerException e){
			logger.info(" === CARR CODE CHECK NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","CARR CODE CHECK NullPointerException",e, msg);
			flag = false;
		}catch(SQLException e){
			logger.info(" === CARR CODE CHECK SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","CARR CODE CHECK SQLException",e, msg);
			flag = false;
		}catch (Exception e) {
			logger.info(" === CARR CODE CHECK ERROR === ");
			logger.info("ERROR==> " + e.toString());
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","CARR CODE CHECK Exception",e, msg);
			flag = false;
		}finally{

			if(!flag){
				//FNA 생성
		    	fnaMsg = createack.NonService(fhlmap);
		    	fhlmap.put("AMS_ACK", fnaMsg);
		    	fhlmap.put("AMS_SMI", "FNA");
				HashMap logMap = new HashMap();
				//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
				logMap.put("HISTORY_STATUS", "Carr_Code Check Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", "409");
				logMap.put("IKAMS_STEP", "409");

				logger.info(" === FHL CARR CODE CHECK ERROR=== ");
				commonSql.insertHistory(logMap);
				commonSql.AckupdateTrace(logMap, fhlmap);
			}else{

				HashMap logMap = new HashMap();
				//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
				logMap.put("HISTORY_STATUS", "Carr_Code Check Success");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", AMS_STEP);
				logMap.put("IKAMS_STEP", IKAMS_STEP);
				logger.info(" === FHL CARR CODE SUCCESS === ");
				commonSql.insertHistory(logMap);

	    	}

			exchange.getIn().setHeader("Air_Check_Flag", flag);
			exchange.getIn().setHeader("EDI_PARSE", fhlmap);
			try{
	    		sqlMapClient.commitTransaction();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}finally{
	    		sqlMapClient.endTransaction();
	    	}
		}


	}

	public void amsikamschk(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, @Body String msg, Exchange exchange) throws Exception {

		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> route_chk = new HashMap<String,Object>();
		String fnaMsg;

		logger.info(" === FHL AMS/IKAMS ROUTING CHECK START === ");
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("EDI_PARSE");
		try{

			if(msg.indexOf("OCI/KR/EXP/M") !=-1 || msg.indexOf("OCI/KR/IMP/M") !=-1){
				route_chk.put("AMS_SEND", "AMS");
				route_chk.put("IKAMS_SEND", "IKAMS");
				//producer.sendBody("direct:AMS_SEND", exchange.getIn().getHeader("EDI_PARSE"));
			}else if(msg.indexOf("OCI/KR/EXP/I") !=-1 || msg.indexOf("OCI/KR/IMP/I") !=-1){
				route_chk.put("IKAMS_SEND", "IKAMS");
			}else if(msg.indexOf("OCI/KR/EXP/A") !=-1){
				route_chk.put("AMS_SEND", "AMS");
			}else{
				route_chk.put("AMS_SEND", "AMS");
			}

			/*if(route_chk.get("AMS_SEND").equals("AMS")){
				exchange.getIn().setHeader("AMS", "AMS");
			}else if(route_chk.get("IKAMS_SEND").equals("IKAMS")){
				exchange.getIn().setHeader("IKAMS", "IKAMS");
			}
*/
			logger.info(" === FHL AMS/IKAMS ROUTING : " + CommonUtil.nullChk(route_chk.get("AMS_SEND")) + "/" + CommonUtil.nullChk(route_chk.get("IKAMS_SEND")) +"=== ");
			logger.info(" === FHL AMS/IKAMS ROUTING CHECK END === ");

		}catch(IndexOutOfBoundsException e){
			logger.info(" === FHL AMS/IKAMS ROUTING CHECK IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN", "FHL AMS/IKAMS ROUTING CHECK IndexOutOfBoundsException", e, msg);
		}catch(NullPointerException e){
			logger.info(" === FHL AMS/IKAMS ROUTING CHECK NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","FHL AMS/IKAMS ROUTING CHECK NullPointerException",e, msg);
		}catch (Exception e) {
			logger.info(" === FHL AMS/IKAMS ROUTING CHECK ERROR === ");
			logger.info("ERROR==> " + e.toString());
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","FHL AMS/IKAMS ROUTING CHECK Exception",e, msg);
		}
	}
}
