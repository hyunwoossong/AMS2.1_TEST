package com.trx.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import oracle.sql.CLOB;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.trx.Msgprocess.CreateAck;
import com.trx.Msgprocess.CreateMSG;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;

public class DbSelectTrace {

	private static final Logger logger = LoggerFactory.getLogger(AirlineSelect.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	CommonSql commonSql;

	@Autowired
	CreateMSG createmsg = new CreateMSG();

	@Autowired
	DbinsertTrace dbinserttrace = new DbinsertTrace();

	@Value("#{prop['ikams.done.yn']}")
	//@Value("${ktnet.write.path}")
	private String done_yn;

	@Value("#{prop['ikams.success.flag']}")
	//@Value("${ktnet.write.path}")
	private String success_flag;


	@Autowired
	MainExceptionManager mainExceptionManager;

	@Autowired
	CreateAck createack = new CreateAck();

	final static String AMS_STEP = "900";

	public void orgrouteSelect(@Body String msg, Exchange exchange) throws Exception {
		boolean flag = true;

		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> org_route = new HashMap<String,Object>();
		String fnaMsg;

		logger.info(" === AMS ACK ORG ROUTE CHECK === ");
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("ACK_PARSE");

		List result = sqlMapClient.queryForList("TM_TRACE.ackRouteCheck", fhlmap);

		String ams_out_reverse_key ="";
		try{
			sqlMapClient.startTransaction();
			if(result.size() > 0){

				for (int i = 0; i < result.size(); i++) {
					org_route = (HashMap) result.get(i);

					fhlmap.put("TRX_ROUTE", (String) org_route.get("TRX_ROUTE"));
					fhlmap.put("ORG_ROUTE", (String) org_route.get("ORG_ROUTE"));
					 ams_out_reverse_key = CommonUtil.nullChk(fhlmap.get("ORG_ROUTE")).toString();
				}
			}else{

				logger.info(" === AMS ACK SKIP === ");
				flag = false;
			}

			if(flag){
				HashMap logMap = new HashMap();
				//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
				logMap.put("HISTORY_STATUS", "AMS ACK Route Change Success");
				logMap.put("AMS_TRX_ROUTE", fhlmap.get("TRX_ROUTE"));
				logMap.put("AMS_STEP", AMS_STEP);

				fhlmap = createack.Msb_Ack(fhlmap);
				logger.info(" === AMS ACK CREATE === ");
				commonSql.insertHistory(logMap);
				commonSql.AckupdateTrace(logMap, fhlmap);
			}

		}catch(IndexOutOfBoundsException e){
			logger.info(" === AMS ACK Create IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS ACK Create IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === AMS ACK Create NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS ACK Create NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === AMS ACK Create SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS ACK Create SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === AMS ACK Create Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	flag = false;
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS ACK Create Exception",e, msg);
		}finally{

			try{
	    		sqlMapClient.commitTransaction();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}finally{
	    		sqlMapClient.endTransaction();
	    	}
		}


	}

	public void ikamsEXPAck(@Body String msg, Exchange exchange) throws Exception {
		boolean flag = true;

		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> ikams_exp = new HashMap<String,Object>();
		String fnaMsg;

		String ams_out_reverse_key ="";
		try{
			sqlMapClient.startTransaction();
			sqlMapClient.startBatch();

			fhlmap.put("IKAMS_DONE_YN", done_yn);
			//fhlmap.put("SUC_FLAG", success_flag);

			/*int update_count = (int) sqlMapClient.update("INSERT_UPDATE.IKAMSExpUpdateTrace", fhlmap);
			logger.info(" === UPDATE COUNT IKAMS EXP - " + success_flag + " : " + update_count + "===" );*/

			List result = sqlMapClient.queryForList("TM_TRACE.IkamsExpRead", fhlmap);

			if(result.size() > 0){

				for (int i = 0; i < result.size(); i++) {
					ikams_exp = (HashMap) result.get(i);

					fhlmap.put("MAWB_NO", (String) ikams_exp.get("MAWB_NO"));
					fhlmap.put("HAWB_NO", (String) ikams_exp.get("HAWB_NO"));
					fhlmap.put("M_ORG", (String) ikams_exp.get("M_ORG"));
					fhlmap.put("M_DST", (String) ikams_exp.get("M_DST"));
					fhlmap.put("M_PC_Q", String.valueOf(ikams_exp.get("M_PC_Q")));
					fhlmap.put("M_WT_M", String.valueOf(ikams_exp.get("M_WT_M")));
					fhlmap.put("CMOD_N", (String) ikams_exp.get("CMOD_N"));
					fhlmap.put("MSG_CTRL_ID", (String) ikams_exp.get("MSG_CTRL_ID"));
					fhlmap.put("SUCCESS_FLAG", (String) ikams_exp.get("SUCCESS_FLAG"));
					fhlmap.put("SENDER_PIMA", (String) ikams_exp.get("SENDER_PIMA"));
					fhlmap.put("FAIL_MSG", (String) ikams_exp.get("FAIL_MSG"));
					//fhlmap.put("ORG_MSG", (String) ikams_exp.get("ORG_MSG"));
					fhlmap.put("ORG_MSG", CommonUtil.nullChk(CommonUtil.clobToStr((CLOB)ikams_exp.get("ORG_MSG"))));
					ams_out_reverse_key = fhlmap.get("MSG_CTRL_ID").toString();
					fhlmap.put("TRX_ROUTE", (String) ikams_exp.get("MSG_CTRL_ID"));
					if(fhlmap.get("MSG_CTRL_ID") != null){

						HashMap logMap = new HashMap();
						//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
						logMap.put("HISTORY_STATUS", "IKAMS EXP ACK CREATE");
						logMap.put("AMS_TRX_ROUTE", fhlmap.get("MSG_CTRL_ID"));

						if(fhlmap.get("SUCCESS_FLAG").equals("Y")){
							fhlmap.put("IKAMS_SMI", "IFMA");
							logMap.put("IKAMS_STEP", "1100");
							fhlmap = createack.ikamsFMA(fhlmap);
						}else{
							fhlmap.put("ACK_TYPE", "EXP");
							fhlmap.put("IKAMS_SMI", "IFNA");
							logMap.put("IKAMS_STEP", "1109");
							fhlmap = createack.ikamsFNA(fhlmap);
						}

						int flag_count = 0;
						flag_count = (Integer)sqlMapClient.update("INSERT_UPDATE.IKAMSExpFlagUpdateTrace", fhlmap);
						logger.info(" === IKAMS EXP SUCCESS FLAG UPDATE : " + flag_count +"=== ");
						logger.info(" === IKAMS EXP ACK CREATE === ");
						commonSql.insertHistory(logMap);
						commonSql.AckupdateTrace(logMap, fhlmap);
					}

					sqlMapClient.executeBatch();
					sqlMapClient.commitTransaction();
				}
			}else{

				logger.info(" === IKAMS EXP ACK WAITING  === ");
				flag = false;

			}

			if(!flag){

				if(fhlmap.get("MSG_CTRL_ID") != null){
					HashMap logMap = new HashMap();
					//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
					logMap.put("HISTORY_STATUS", "IKAMS EXP CREATE ERROR");
					logMap.put("AMS_TRX_ROUTE", fhlmap.get("MSG_CTRL_ID"));
					logMap.put("AMS_STEP", AMS_STEP);

					logger.info(" === IKAMS EXP CREATE ERROR === ");
					commonSql.insertHistory(logMap);
				}
			}

		}catch(IndexOutOfBoundsException e){
			logger.info(" === IKAMS EXP ACK Create IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS EXP ACK Create IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === IKAMS EXP ACK Create NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS EXP ACK Create NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === IKAMS EXP ACK Create SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS EXP ACK Create SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === IKAMS EXP ACK Create Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	flag = false;
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS EXP ACK Create Exception",e, msg);
		}finally{

			try{
	    		sqlMapClient.commitTransaction();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}finally{
	    		sqlMapClient.endTransaction();
	    	}
		}
	}

	public void ikamsIMPAck(@Body String msg, Exchange exchange) throws Exception {
		boolean flag = true;

		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> ikams_imp = new HashMap<String,Object>();
		String fnaMsg;

		String ams_out_reverse_key ="";
		try{
			sqlMapClient.startTransaction();
			sqlMapClient.startBatch();

			fhlmap.put("IKAMS_DONE_YN", done_yn);
			//fhlmap.put("SUC_FLAG", success_flag);

			/*int update_count = (int) sqlMapClient.update("INSERT_UPDATE.IKAMSImpUpdateTrace", fhlmap);
			logger.info(" === UPDATE COUNT IKAMS IMP - " + success_flag +" : " + update_count + "===" );*/

			List result = sqlMapClient.queryForList("TM_TRACE.IkamsImpRead", fhlmap);

			if(result.size() > 0){

				for (int i = 0; i < result.size(); i++) {
					ikams_imp = (HashMap) result.get(i);

					fhlmap.put("MAWB_NO", (String) ikams_imp.get("MAWB_NO"));
					fhlmap.put("MAWB_PRIFIX", (String) ikams_imp.get("MAWB_PRIFIX"));
					fhlmap.put("MAWB_SERIAL", (String) ikams_imp.get("MAWB_SERIAL"));
					fhlmap.put("HAWB_NO", (String) ikams_imp.get("HAWB_NO"));
					fhlmap.put("M_ORG", (String) ikams_imp.get("M_ORG"));
					fhlmap.put("M_DST", (String) ikams_imp.get("M_DST"));
					fhlmap.put("FLT", (String) ikams_imp.get("FLT"));
					fhlmap.put("M_PC_Q", String.valueOf(ikams_imp.get("M_PC_Q")));
					fhlmap.put("M_WT_M", String.valueOf(ikams_imp.get("M_WT_M")));
					fhlmap.put("CMOD_N", (String) ikams_imp.get("CMOD_N"));
					fhlmap.put("MSG_CTRL_ID", (String) ikams_imp.get("MSG_CTRL_ID"));
					fhlmap.put("SUCCESS_FLAG", (String) ikams_imp.get("SUCCESS_FLAG"));
					fhlmap.put("KAMS_UPDT_YN", (String) ikams_imp.get("KAMS_UPDT_YN"));
					fhlmap.put("FAIL_MSG", (String) ikams_imp.get("WORKTYPE"));
					fhlmap.put("SENDER_PIMA", (String) ikams_imp.get("SENDER_PIMA"));
					fhlmap.put("MSG_TYPE", (String) ikams_imp.get("MSG_TYPE"));
					fhlmap.put("CARR_C", (String) ikams_imp.get("CARR_C"));
					fhlmap.put("FLT_NUM", (String) ikams_imp.get("FLT_NUM"));
					fhlmap.put("H_PC_Q", String.valueOf(ikams_imp.get("H_PC_Q")));
					fhlmap.put("H_WT_M", String.valueOf(ikams_imp.get("H_WT_M")));
					fhlmap.put("SLAC", String.valueOf(ikams_imp.get("SLAC")));
					//fhlmap.put("ORG_MSG", (String) ikams_imp.get("ORG_MSG"));
					fhlmap.put("ORG_MSG", CommonUtil.nullChk(CommonUtil.clobToStr((CLOB)ikams_imp.get("ORG_MSG"))));
					ams_out_reverse_key = fhlmap.get("MSG_CTRL_ID").toString();
					fhlmap.put("TRX_ROUTE", (String) ikams_imp.get("MSG_CTRL_ID"));
					if(fhlmap.get("MSG_CTRL_ID") != null){

						HashMap logMap = new HashMap();
						//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
						logMap.put("HISTORY_STATUS", "IKAMS IMP ACK CREATE");
						logMap.put("AMS_TRX_ROUTE", fhlmap.get("MSG_CTRL_ID"));

						if(fhlmap.get("SUCCESS_FLAG").equals("Y")){
							fhlmap.put("IKAMS_SMI", "IFMA");
							logMap.put("IKAMS_STEP", "1100");
							fhlmap = createack.ikamsFMA(fhlmap);
						}else{
							fhlmap.put("ACK_TYPE", "IMP");
							fhlmap.put("IKAMS_SMI", "IFNA");
							logMap.put("IKAMS_STEP", "1109");
							fhlmap = createack.ikamsFNA(fhlmap);
						}

						int flag_count = sqlMapClient.update("INSERT_UPDATE.IKAMSImpFlagUpdateTrace", fhlmap);
						logger.info(" === IKAMS IMP SUCCESS FLAG UPDATE : " + flag_count +"=== ");
						logger.info(" === IKAMS IMP ACK CREATE === ");


						commonSql.insertHistory(logMap);
						commonSql.AckupdateTrace(logMap, fhlmap);
					}

					sqlMapClient.executeBatch();
					sqlMapClient.commitTransaction();
				}
			}else{

				logger.info(" === IKAMS IMP ACK WAITING  === ");
				flag = false;
			}

			if(!flag){

				if(fhlmap.get("MSG_CTRL_ID") != null){
					HashMap logMap = new HashMap();
					//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
					logMap.put("HISTORY_STATUS", "IKAMS IMP CREATE ERROR");
					logMap.put("AMS_TRX_ROUTE", fhlmap.get("MSG_CTRL_ID"));
					logMap.put("AMS_STEP", AMS_STEP);

					logger.info(" === IKAMS IMP ACK CREATE ERROR === ");

					commonSql.insertHistory(logMap);
				}

			}

		}catch(IndexOutOfBoundsException e){
			logger.info(" === IKAMS IMP ACK Create IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS EXP IMP Create IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === IKAMS IMP ACK Create NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS IMP ACK Create NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === IKAMS IMP ACK Create SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS IMP ACK Create SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === IKAMS IMP ACK Create Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	flag = false;
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS IMP ACK Create Exception",e, msg);
		}finally{

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
