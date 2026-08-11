package com.trx.Msgprocess;

import java.sql.SQLException;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.trx.db.AirlineSelect;
import com.trx.db.DbinsertTrace;
import com.trx.util.CommonSql;
import com.trx.util.CommonUtil;
import com.trx.util.MainExceptionManager;
import com.trx.validate.ValidationDoc;

public class RoutingBIZ {

	private static final Logger logger = LoggerFactory.getLogger(AirlineSelect.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	CommonSql commonSql;

	@Autowired
	CommonUtil commonUtil;

	@Autowired
	CreateMSG createmsg = new CreateMSG();

	@Autowired
	DbinsertTrace dbinserttrace = new DbinsertTrace();

	@Autowired
	CreateAck createack = new CreateAck();

	@Autowired
	ValidationDoc validationdoc = new ValidationDoc();

	@Autowired
	MainExceptionManager mainExceptionManager;

	@Produce
	ProducerTemplate producer;

	final static String AMS_STEP = "750";
	public void routing7CBIZ(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, Exchange exchange ) throws Exception {
		// TODO Auto-generated method stub
		boolean bizflag = true;
		String tgt_point = "";
		String rou_point = "";
		String msg = "";
		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("FHLMAP");
		msg = (String) CommonUtil.nullChk(fhlmap.get("EDI"));
		try{
			sqlMapClient.startTransaction();
			//7C 일때Prefix 933이면 KZ 동보
			//Routing에서 FNA가 났을 경우 로직 발생
			if(fhlmap.get("CARR_CODE").equals("7C") && fhlmap.get("MBI_PRE").equals("933")){

				if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82AMS") !=-1){

					/*
					 * fhlmap.put("AIR_PIMA_MSB_SEND", "RASAIR08NCA"); fhlmap =
					 * createmsg.fhlwrite2(ams_out_reverse_key, fhlmap); fhlmap.put("MULTI_MSG",
					 * fhlmap.get("CONVERSION_MSG"));
					 */
					fhlmap.put("AIR_PIMA_MSB_SEND_VER", "RASAIR08NCA"); //23.04.05 FHL/4로 보내고 KZ 동보전송도 FHL/4로 변경
					fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
					fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
					String ranchar = commonUtil.randomchar();
					String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
					HashMap kzfhlMap = new HashMap();
					kzfhlMap.put("FILENAME", oalfileName);
					producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), kzfhlMap);
					fhlmap.put("MSB_OAL_SEND", "MSB_OAL");

					HashMap logMap = new HashMap();
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);

					fhlmap.put("ROUTE_POINT", fhlmap.get("MSB_OAL_SEND"));
					fhlmap.put("TGT_POINT", fhlmap.get("MSB_OAL_SEND"));

					logMap.put("HISTORY_STATUS", "7C Prefix 933 KZ DongBo");
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
					logMap.put("AMS_STEP", AMS_STEP);

					commonSql.insertHistory(logMap);
					commonSql.updateTgtPoint(ams_out_reverse_key, logMap, fhlmap);
					commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
					logger.info("=============== 7C PREFIX 933 KZ DONGBO MSB SEND ===============");
				}else{
					if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
						fhlmap.put("ERRORMSG",  fhlmap.get("SRC_PIMA") +" - PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
						fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
					}
					bizflag = false;
				}
			}
		}catch(IndexOutOfBoundsException e){
			logger.info(" === 7C ROUTE Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			bizflag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","7C ROUTE Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === 7C ROUTE Check is NullPointer === ");
			logger.error("ERROR==>"+e.toString(), e);
			bizflag = false;
		}catch(SQLException e){
			logger.info(" === 7C ROUTE Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			bizflag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","7C ROUTE Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === 7C ROUTE Check Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","7C ROUTE Check Exception",e, msg);
	    	bizflag = false;
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
