package com.trx.validate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;


public class ReverseKey {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ReverseKey.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	CommonSql commonSql;

	@Autowired
	MainExceptionManager mainExceptionManager;

	final static String ams_step = "100";
	final static String ikams_step = "100";

	public String mainStart(@Body String msg, Exchange exchange) throws Exception {
		String amsOutReverseKey = "";

		try {

			sqlMapClient.startTransaction();

			logger.info(" === Reverse Key GET START === ");
			amsOutReverseKey = (String) sqlMapClient.queryForObject("COMMON_SQL.selectReverseKey");
			logger.info(" Rerverse Key GET ["+amsOutReverseKey+"]");
			exchange.getIn().setHeader("AMS_TRX_ROUTE", amsOutReverseKey);
			byte[] bytes = msg.getBytes("UTF-8");
			// EDI HISTORY INSERT[S] 초기셋팅
			HashMap logMap = new HashMap<String, Object>();
			logMap.put("AMS_TRX_ROUTE", amsOutReverseKey);
			logMap.put("AMS_STEP", ams_step);
			logMap.put("IKAMS_STEP", ikams_step);
			logMap.put("HISTORY_STATUS", "Route key Create");
			logMap.put("ORG_MSG", bytes);

			String ktroute = (String) exchange.getIn().getHeader("KTNET_ROUTE");
			logMap.put("KTNET_ROUTE", ktroute);

			//재처리시 전송 된 원본 문서의 TRX_ROUTE 값 새로 생성 후 재처리하는 기존 TRX_ROUTE 값 받아서 TM_TRACE의  재처리 컬럼 Y로 변경.
			String reroute = (String) exchange.getIn().getHeader("RE_ROUTE");
			logMap.put("RE_TRX_ROUTE", reroute);
			if(CommonUtil.nullChk(logMap.get("RE_TRX_ROUTE")) != null && CommonUtil.nullChk(logMap.get("RE_TRX_ROUTE")).toString().length() > 0) {
				logger.info(" === REPROC TRX ROUTE : " + logMap.get("RE_TRX_ROUTE") + " === ");
				commonSql.reprocTrace(logMap);
			}
			exchange.getIn().setHeader("LOG_MAP", logMap);
			//System.out.println("**********************KTNET_ROUTE :"+logMap.get("KTNET_ROUTE"));
			commonSql.insertTrace(logMap);
			commonSql.insertHistory(logMap);
			exchange.getIn().setHeader("MSG", msg);
			// EDI HISTORY INSERT[E]
			logger.info(" === Reverse Key GET Success === ");

		}catch(IndexOutOfBoundsException e){
			logger.info(" === Reverse Key GET IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(amsOutReverseKey, "AMS-MAIN", "Reverse Key GET IndexOutOfBoundsException", e, msg);
		}catch(NullPointerException e){
			logger.info(" === Reverse Key GET NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(amsOutReverseKey, "AMS-MAIN","Reverse Key GET NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === Reverse Key GET SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(amsOutReverseKey, "AMS-MAIN","Reverse Key GET SQLException",e, msg);
		}catch (Exception e) {
			logger.info(" === Reverse Key GET ERROR === ");
			logger.info("ERROR==> " + e.toString());
			mainExceptionManager.process(amsOutReverseKey, "AMS-MAIN","Reverse Key GET Exception",e, msg);
			e.printStackTrace();
		}finally{
			logger.info(" === Reverse Key GET END === ");
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
}