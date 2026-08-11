package com.trx.Msgprocess;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.camel.Body;
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
import com.trx.validate.IkamsValidationDoc;
import com.trx.validate.ValidationDoc;

public class IkamsRouting {

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
	IkamsValidationDoc ikamsvalidationdoc = new IkamsValidationDoc();

	@Autowired
	MainExceptionManager mainExceptionManager;

	@Produce
	ProducerTemplate producer;

	@Value("#{prop['ktnet.write.path']}")
	//@Value("${ktnet.write.path}")
	private String ktnet_write_path;
	
/*	@Value("#{prop['ke.xflag.write.path']}")
	//@Value("${ktnet.write.path}")
	private String ke_xflag_write_path;*/

	final static String IKAMS_STEP = "700";

	//Routing 시작
	/*
	 * 2016.08.22~
	 * KTNET 수출 관련 Routing 추가
	 * AIRCIS M건일 경우 KTNETAMS만 AMQ로 전송
	 * 그 외의 문서는 KTNET FHL/4 스펙으로 변경하여 I건으로 KTNET AMQ 전송
	 * */
	public void ikamsrouting(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, Exchange exchange) throws Exception {
		boolean flag = true;
		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> kams_route_code = new HashMap<String,Object>();
		String fnaMsg ="";
		String IKAMS_POINT = "";
		String rou_point = "";
		String msg = "";
		List result = new ArrayList();
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("EDI_PARSE");
		msg = (String) CommonUtil.nullChk(fhlmap.get("EDI"));
		String web_flag = (String)exchange.getIn().getHeader("WEB_FLAG");
		//String web_flag = "REPROC_IKAMS/REPROC_OAL";
		String[] arrWebFlag = CommonUtil.nullChk(web_flag).split("/");
		for(int w=0; w<arrWebFlag.length; w++){

			if(arrWebFlag[w].toString().indexOf("REPROC_MSB_KE") != -1){
				fhlmap.put("REPROC_KE_FLAG", "Y");
			}else if(arrWebFlag[w].toString().indexOf("REPROC_MSB_OAL") != -1){
				fhlmap.put("REPROC_OAL_FLAG", "Y");
			}else if(arrWebFlag[w].toString().indexOf("REPROC_OAL4") != -1){
				fhlmap.put("REPROC_OAL_VER", "Y");
			}else if(arrWebFlag[w].toString().indexOf("REPROC_TYPEB") != -1){
				fhlmap.put("REPROC_TYPEB_FLAG", "Y");
			}else if(arrWebFlag[w].toString().indexOf("REPROC_TY_VER4") != -1){
				fhlmap.put("REPROC_TYPEB_VER", "Y");
			}else if(arrWebFlag[w].toString().indexOf("REPROC_IKAMS") != -1){
				fhlmap.put("REPROC_IKAMS_FLAG", "Y");
			}else if(arrWebFlag[w].toString().indexOf("REPROC_KTNET") != -1){
				fhlmap.put("REPROC_KT_FLAG", "Y");
			}else if(arrWebFlag[w].toString().indexOf("REPROC_KTMANIFEST") != -1){
				fhlmap.put("REPROC_KT_MANIFEST", "Y");
			}else if(arrWebFlag[w].toString().indexOf("REPROC_KCMANIFEST") != -1){
				fhlmap.put("REPROC_KC_MANIFEST", "Y");
			}
		}

		logger.info(" === IKAMS ROUTE CHECK START === ");
		int count = (int)sqlMapClient.queryForObject("TM_TRACE.routingCheckOrgDes", fhlmap);

		if(count > 0){
			//환적 일 경우
			String org_code = "ICN,SEL,PUS,GMP";

			if(org_code.indexOf(CommonUtil.nullChk(fhlmap.get("M_ORG"))) !=-1 || org_code.indexOf(CommonUtil.nullChk(fhlmap.get("M_DST"))) !=-1){
				//출/도착지가 있는 경우
				fhlmap.put("ORG_DES_CHK", "Y");

				if(CommonUtil.nullChk(web_flag).length() > 0){
					logger.info(" === REPROC FLAG : " + web_flag + " === ");
					if(CommonUtil.nullChk(fhlmap.get("CARR_CODE")).equals("CA") && CommonUtil.nullChk(fhlmap.get("M_ORG")).equals("FRA")){

						if(CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals("IMP")){
							result = sqlMapClient.queryForList("TM_TRACE.CAwebRoutingCheck", fhlmap);
						}else{
							result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
						}
					}else{
						result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
						if(result.size() == 0){
							//20180618 최재준 추가
							//ORG/DST가 ICN/SEL/PUS/GMP 일때 별다른 routing 조건이 없을 경우
							//routing table의 TRANSSHPIMENT 컬럼에 'ALL' 입력으로 전송
							logger.info(" === TRANSSHPIMENT : ALL === ");
							fhlmap.put("ORG_DES_CHK", "N");
							fhlmap.put("TRANSSHIPMENT", "ALL");
							result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
						}
					}
				}else{
					if(CommonUtil.nullChk(fhlmap.get("CARR_CODE")).equals("CA") && CommonUtil.nullChk(fhlmap.get("M_ORG")).equals("FRA")){

						if(CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals("IMP")){
							result = sqlMapClient.queryForList("TM_TRACE.CAroutingCheck", fhlmap);
						}else{
							result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
						}
					}else{
						result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
						if(result.size() == 0){
							//20180618 최재준 추가
							//ORG/DST가 ICN/SEL/PUS/GMP 일때 별다른 routing 조건이 없을 경우
							//routing table의 TRANSSHPIMENT 컬럼에 'ALL' 입력으로 전송
							logger.info(" === TRANSSHPIMENT : ALL === ");
							fhlmap.put("ORG_DES_CHK", "N");
							fhlmap.put("TRANSSHIPMENT", "ALL");
							result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
						}
					}
				}

			}else{
				//ORG/DST가 ICN/SEL/PUS/GMP 외의 routing 일 경우
				//routing table의 TRANSSHPIMENT 컬럼에 'ANY' 입력으로 전송
				logger.info(" === TRANSSHPIMENT : ANY === ");
				fhlmap.put("TRANSSHIPMENT", "ANY");

				if(CommonUtil.nullChk(web_flag).length() > 0){
					logger.info(" === REPROC FLAG : " + web_flag + " === ");
					if(CommonUtil.nullChk(fhlmap.get("CARR_CODE")).equals("CA") && CommonUtil.nullChk(fhlmap.get("M_ORG")).equals("FRA")){

						if(CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals("IMP")){
							result = sqlMapClient.queryForList("TM_TRACE.CAwebRoutingCheck", fhlmap);
						}else{
							result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
						}
					}else{
						result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
					}
				}else{
					if(CommonUtil.nullChk(fhlmap.get("CARR_CODE")).equals("CA") && CommonUtil.nullChk(fhlmap.get("M_ORG")).equals("FRA")){

						if(CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals("IMP")){
							result = sqlMapClient.queryForList("TM_TRACE.CAroutingCheck", fhlmap);
						}else{
							result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
						}
					}else{
						result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
					}
				}

			}
			/*if(CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")).equals("T") || CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")).equals("R")){
				fhlmap.put("TRANSSHIPMENT", "ANY");

				if(CommonUtil.nullChk(web_flag).length() > 0){
					logger.info(" === REPROC FLAG : " + web_flag + " === ");
					result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
				}else{
					result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
				}

			}else{
				fhlmap.put("ORG_DES_CHK", "Y");

				if(CommonUtil.nullChk(web_flag).length() > 0){
					logger.info(" === REPROC FLAG : " + web_flag + " === ");
					result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
				}else{
					result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
				}
			}*/
		}else{

			if(CommonUtil.nullChk(web_flag).length() > 0){
				logger.info(" === REPROC FLAG : " + web_flag + " === ");
				if(CommonUtil.nullChk(fhlmap.get("CARR_CODE")).equals("CA") && CommonUtil.nullChk(fhlmap.get("M_ORG")).equals("FRA")){

					if(CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals("IMP")){
						result = sqlMapClient.queryForList("TM_TRACE.CAwebRoutingCheck", fhlmap);
					}else{
						result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
					}
				}else{
					result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
				}
			}else{
				if(CommonUtil.nullChk(fhlmap.get("CARR_CODE")).equals("CA") && CommonUtil.nullChk(fhlmap.get("M_ORG")).equals("FRA")){

					if(CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals("IMP")){
						result = sqlMapClient.queryForList("TM_TRACE.CAroutingCheck", fhlmap);
					}else{
						result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
					}
				}else{
					result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
				}
			}
		}

		try{
			sqlMapClient.startTransaction();
			if(result.size() > 0){

				for (int i = 0; i < result.size(); i++) {
					kams_route_code = (HashMap) result.get(i);

					fhlmap.put("CARR_CODE", (String) CommonUtil.nullChk(kams_route_code.get("CARR_CODE")));
					fhlmap.put("SMI_VERSION", (String) CommonUtil.nullChk(kams_route_code.get("SMI_VERSION")));
					fhlmap.put("IMP_EXP_FLAG", (String) CommonUtil.nullChk(kams_route_code.get("IMP_EXP_FLAG")));
					fhlmap.put("OCI_TYPE_FLAG", (String) CommonUtil.nullChk(kams_route_code.get("OCI_TYPE_FLAG")));
					fhlmap.put("MANIFEST_COMPANY", (String) CommonUtil.nullChk(kams_route_code.get("MANIFEST_COMPANY")));
					fhlmap.put("KAMS_KT_SEND_FLAG", (String) CommonUtil.nullChk(kams_route_code.get("KTNET_SEND_FLAG")));
					fhlmap.put("KAMS_KTNET_MANIFEST", (String) CommonUtil.nullChk(kams_route_code.get("KTNET_MANIFEST")));
					fhlmap.put("KAMS_KCNET_MANIFEST", (String) CommonUtil.nullChk(kams_route_code.get("KCNET_MANIFEST")));
					fhlmap.put("IKAMS_SEND", (String) CommonUtil.nullChk(kams_route_code.get("IKAMS_SEND")));
					fhlmap.put("ORG", (String) CommonUtil.nullChk(kams_route_code.get("ORG")));
					fhlmap.put("DST", (String) CommonUtil.nullChk(kams_route_code.get("DST")));
					fhlmap.put("SEND_QNAME", (String) CommonUtil.nullChk(kams_route_code.get("SEND_QNAME")));
					fhlmap.put("KAMS_KT_SEND_FLAG2", (String) CommonUtil.nullChk(kams_route_code.get("KTNET_SEND_FLAG2")));
					fhlmap.put("KAMS_KTNET_MANIFEST2", (String) CommonUtil.nullChk(kams_route_code.get("KTNET_MANIFEST2")));
					fhlmap.put("KAMS_KCNET_MANIFEST2", (String) CommonUtil.nullChk(kams_route_code.get("KCNET_MANIFEST2")));
					fhlmap.put("IKAMS_SEND2", (String) CommonUtil.nullChk(kams_route_code.get("IKAMS_SEND2")));
				}
				HashMap logMap = new HashMap();
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);

				if(fhlmap.get("KAMS_KT_SEND_FLAG2") != null && fhlmap.get("KAMS_KT_SEND_FLAG2").toString().length() > 0){
					if(fhlmap.get("KAMS_KT_SEND_FLAG2").equals("KTNETIMP")){
						fhlmap.put("IKAMS_ROUTING_KT", "/KTNET");
					}
				}
				if(fhlmap.get("IKAMS_SEND2") != null && fhlmap.get("IKAMS_SEND2").toString().length() > 0){
					fhlmap.put("IKAMS_ROUTING_IKAMS", "/IKAMS");
				}

				if(fhlmap.get("KAMS_KTNET_MANIFEST2") != null && fhlmap.get("KAMS_KTNET_MANIFEST2").toString().length() > 0){
						fhlmap.put("IKAMS_ROUTING_KTMANIFEST", "/KTMANIFEST");
				}

				if(fhlmap.get("KAMS_KCNET_MANIFEST2") != null && fhlmap.get("KAMS_KCNET_MANIFEST2").toString().length() > 0){
					fhlmap.put("IKAMS_ROUTING_KCMANIFEST", "/KCMANIFEST");
			}

				rou_point = CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTING_IKAMS"))+CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTING_KT"))+CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTING_KTMANIFEST"))
						+CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTING_KCMANIFEST"));

				fhlmap.put("IKAMS_ROUTE_POINT", rou_point.replaceFirst("/", ""));
				commonSql.updateTgtPoint(ams_out_reverse_key, logMap, fhlmap);
				logger.info(" === IKAMS ROUTE CHECK END === ");
			}/*else{

				logger.info(" === IKAMS ROUTE CHECK IS NULL === ");

				if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("I")){
					fhlmap.put("IKAMS_ERRMSG",  fhlmap.get("CARR_CODE")+" - FLAG I KTNET MANIFEST SERVICE IS NOT AVAILABLE");
					fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("CARR_CODE")+" - KTNET 취합 FLAG I 적하 서비스 하지 않음.");
				}else{
					fhlmap.put("IKAMS_ERRMSG", fhlmap.get("CARR_CODE")+" -AMS SERVICE IS NOT AVAILABLE FOR THIS AIRLINE");
					fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("CARR_CODE")+" - AMS 서비스 하지 않는 항공사");
				}

				fhlmap.put("IKAMS_SMI", "FNA");
				fnaMsg = createack.FNAack(fhlmap);
				//fna 로그 담을 곳 체크(변수값 및 테이블 컬럼)
				fhlmap.put("KAMS_ACK", fnaMsg);
				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "IKAMS Route_Check Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("IKAMS_STEP", "709");

				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
				flag = false;
			}*/

			logger.info(" === FHL VERSION : " + fhlmap.get("VERSION") + " === ");
			logger.info(" === SEND_COMP : " + fhlmap.get("SEND_COMP") + " === ");
			logger.info(" === MSG_CTRL_ID :" + fhlmap.get("MSG_CTRL_ID") + " === ");
			logger.info(" === AIRLINE : " + fhlmap.get("TGT_PIMA") + " === ");
			logger.info(" === MAWB/HWB NO : " + fhlmap.get("MBI_NO") + "_" + fhlmap.get("HBS_NO") + " === ");
			logger.info(" === ORG/DST : " + fhlmap.get("M_ORG") +  "/" + fhlmap.get("M_DST") + " === ");
			logger.info(" === OCI TYPE/FLAG : " + fhlmap.get("OCI_TYPE") +  "/" + fhlmap.get("OCI_FLAG") + " === ");
			logger.info(" === MANIFEST : " + fhlmap.get("MANIFEST_COMPANY") + " === ");

			//FHL 버전 4
			if(CommonUtil.nullChk(fhlmap.get("SMI_VERSION")).equals("4")){
				//로직 수행 시 순서대로 처리 하는 메소드, 메소드를 한번 실행 시키면 그안에 있는 프로세스가 모두 종료될때까지 다음 문서를 처리 하지 못하게 하는 메소드
				//20170223 수입 특송 데이터 관련 수정 데이터 처리 로직구분으로 추가
				synchronized (this) {
					logger.info(" === IKAMS ROUTE START === ");
					//IKAMS 전송

					fhlmap = routingManifest(ams_out_reverse_key, fhlmap);

					IKAMS_POINT = CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTING"))+CommonUtil.nullChk(fhlmap.get("KAMS_KT_SEND"))+CommonUtil.nullChk(fhlmap.get("KAMS_KT_MANIFEST"))
							+CommonUtil.nullChk(fhlmap.get("KAMS_KC_MANIFEST"));

					fhlmap.put("IKAMS_POINT", IKAMS_POINT.replaceFirst("/", ""));
					HashMap logMap = new HashMap();
					//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
					logMap.put("HISTORY_STATUS", "IKAMS FHL/4 Routing");
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
					logMap.put("IKAMS_STEP", IKAMS_STEP);

					commonSql.insertHistory(logMap);
					commonSql.updateTgtPoint(ams_out_reverse_key, logMap, fhlmap);
					logger.info(" === IKAMS ROUTE SUCCESSS === ");
				}
				//20180614 ZE 항공사 BKK, HAN, DAD 외 Routing 일 경우 SELTPZE로 TYPEB 전송 추가
			}/*else if(fhlmap.get("TGT_PIMA").equals("ZE") && result.size() == 0){
				synchronized (this) {
					logger.info(" === ZE IKAMS ROUTE START === ");
					//IKAMS 전송

					HashMap logMap = new HashMap();
					//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
					logMap.put("HISTORY_STATUS", "IKAMS FHL/4 Routing");
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
					logMap.put("IKAMS_STEP", IKAMS_STEP);

					fhlmap.put("IMP_EXP_FLAG", fhlmap.get("OCI_TYPE"));
					fhlmap.put("MANIFEST_COMPANY", "KTNET");

					if(fhlmap.get("IMP_EXP_FLAG").equals("EXP")){

						fhlmap.put("KAMS_KTNET_MANIFEST", "KTMANIFEST");
						fhlmap.put("IKAMS_ROUTE_POINT", "KTMANIFEST");
					}else if(fhlmap.get("IMP_EXP_FLAG").equals("IMP")){

						fhlmap.put("KAMS_KT_SEND_FLAG", "KTNETIMP");
						fhlmap.put("IKAMS_ROUTE_POINT", "KTNETIMP");
					}

					commonSql.updateTgtPoint(ams_out_reverse_key, logMap, fhlmap);

					fhlmap = routingManifest(ams_out_reverse_key, fhlmap);

					IKAMS_POINT = CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTING"))+CommonUtil.nullChk(fhlmap.get("KAMS_KT_SEND"))+CommonUtil.nullChk(fhlmap.get("KAMS_KT_MANIFEST"))
							+CommonUtil.nullChk(fhlmap.get("KAMS_KC_MANIFEST"));

					fhlmap.put("IKAMS_POINT", IKAMS_POINT.replaceFirst("/", ""));

					commonSql.insertHistory(logMap);
					commonSql.updateTgtPoint(ams_out_reverse_key, logMap, fhlmap);
					logger.info(" === ZE IKAMS ROUTE SUCCESSS === ");
				}
			}*/else{
				logger.info(" === IKAMS ROUTE CHECK ERROR === ");

				/*if(fhlmap.get("OCI_FLAG").equals("I")){
					fhlmap.put("IKAMS_ERRMSG",  fhlmap.get("CARR_CODE")+" - FLAG I KTNET MANIFEST SERVICE IS NOT AVAILABLE");
					fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("CARR_CODE")+" - KTNET 취합 FLAG I 적하 서비스 하지 않음.");
				}else{
					fhlmap.put("IKAMS_ERRMSG", fhlmap.get("CARR_CODE")+" -AMS SERVICE IS NOT AVAILABLE FOR THIS AIRLINE");
					fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("CARR_CODE")+" - AMS 서비스 하지 않는 항공사");
				}*/

				fhlmap.put("IKAMS_ERRMSG", fhlmap.get("CARR_CODE")+" -AMS SERVICE IS NOT AVAILABLE FOR THIS AIRLINE");
				fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("CARR_CODE")+" - AMS 서비스 하지 않는 항공사");

				fhlmap.put("IKAMS_SMI", "FNA");
				fnaMsg = createack.FNAack(fhlmap);
				//fna 로그 담을 곳 체크(변수값 및 테이블 컬럼)
				fhlmap.put("KAMS_ACK", fnaMsg);

				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "IKAMS Route_Check Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("IKAMS_STEP", "709");
				flag = false;
				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
			}


		}catch(IndexOutOfBoundsException e){
			logger.info(" === IKAMS ROUTE Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS ROUTE Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === IKAMS ROUTE Check is NullPointer === ");
			//logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			//mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","ROUTE Check NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === IKAMS ROUTE Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS ROUTE Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === IKAMS ROUTE Check Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","IKAMS ROUTE Check Exception",e, msg);
			flag = false;
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


	//적하 목록 routing
	public HashMap<String, Object> routingManifest(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {
		boolean manifest_status = true;
		boolean APIflag = true;

		String msg = fhlmap.get("EDI").toString();
		String fmaMsg ="";
		String fnaMsg ="";
		int resultChk = 0;
		logger.info(" === FHL MANIFEST ROUTE CHECK START === ");
		try{
				//KTNET X FLAG 대리점 로직
				/*if(ke_xflag_write_path.indexOf(fhlmap.get("SRC_PIMA").toString()) == -1){
					
					if(CommonUtil.nullChk(fhlmap.get("TGT_PIMA")).equals("KE") && CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("X")){
						fhlmap.put("IKAMS_SEND","");
						if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
							fhlmap.put("IKAMS_ERRMSG","KE X FLAG SERVICE IS NOT AVAILABLE FOR THIS AGENT");
							fhlmap.put("IKAMS_ERRMSG_KOR", "X FLAG 서비스 하지 않는 대리점");
						}
						manifest_status = false;
					}
				}*/
				
				//취합사 KCNET 전송
				if(fhlmap.get("MANIFEST_COMPANY").equals("KCNET")){
					if(fhlmap.get("IMP_EXP_FLAG").equals("EXP")){
						if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("M") || CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("I") || CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("X")){
							if(fhlmap.get("IKAMS_SEND") != null && fhlmap.get("IKAMS_SEND").toString().length() > 0){
								//수출 IKAMS 전용
								resultChk = dbinserttrace.ikamsExpInsert(ams_out_reverse_key, fhlmap);
								if(resultChk > 0){
									if(fhlmap.get("OCI_CHECK").equals("C")){
										fhlmap.put("OCI_CHECK", "P");
										APIflag = ikamsvalidationdoc.OCICheck(fhlmap);
										//20191204 최재준 추가
										//관세청 수출 epn no 체크 api(KE/LJ만)
										if(APIflag == false){
											/*fhlmap.put("IKAMS_SMI", "FNA");
											fnaMsg = createack.FNAack(fhlmap);*/
											manifest_status = false;
										}
									}
								}
								fhlmap.put("IKAMS_ROUTING", "/IKAMS");
								logger.info(" === IKAMS EXP INSERT === ");
							}
						}

						//M 일때 I로 변경 또는 I 일때 FHL/4 헤더 정보 변경 후 KTNET 전송
						if(fhlmap.get("KAMS_KTNET_MANIFEST") != null && fhlmap.get("KAMS_KTNET_MANIFEST").toString().length() > 0){

							if(fhlmap.get("MSG_CTRL_ID").equals("AIRCIS") && fhlmap.get("OCI_FLAG").equals("M")){
								//AIRCIS M건일 경우 AMSAIR로 1COPY 전송
								if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
									fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
									fhlmap.put("IKAMS_ERRMSG_KOR", "AIRCIS FLAG M KTNET 적하 서비스 하지 않음");
								}
								manifest_status = false;
							}else{
								if(fhlmap.get("KAMS_KTNET_MANIFEST").equals("KTMANIFEST")){
									//20210714 Cyberlogitec 연계 관련 KCNET PIMA만 전송 안되도록 수정(RKRAGT85 모든 항목에 추가함)
									if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85KCNET") !=-1){
										if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
											fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
											fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KTNET 적하 서비스 하지 않음.");
										}
										manifest_status = false;
									}else{
										logger.info(" === KTNET EXP MANIFEST FHL CREATE === ");
										fhlmap = createmsg.fhl_kt_con_i(ams_out_reverse_key, fhlmap);
										String ranchar = commonUtil.randomchar();
										String fileName = "KTNET_FHL_EXP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
										HashMap ktfhlMap = new HashMap();
										ktfhlMap.put("KTFHLFILE", fileName);
										//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
										producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);
										fhlmap.put("OUT_KAMS_FLAG", "I");
										fhlmap.put("KAMS_KT_MANIFEST", "/KTMANIFEST");
									}
								}
							}
						}

						//KCNET 취합 이면서 수출 M OR I 일 경우 I로 변경하여 KCNET 전송
						if(fhlmap.get("KAMS_KCNET_MANIFEST") != null && fhlmap.get("KAMS_KCNET_MANIFEST").toString().length() > 0){
							if(fhlmap.get("KAMS_KCNET_MANIFEST").equals("KCMANIFEST")){

								if(!CommonUtil.nullChk(fhlmap.get("SEND_COMP")).equals("KCNETFHL")){

									if(fhlmap.get("KCNETCODE") != null && fhlmap.get("KCNETCODE").toString().length() > 0){

										logger.info(" === KCNET EXP MANIFEST FHL CREATE === ");
										fhlmap = createmsg.fhl_kc_con_i(ams_out_reverse_key, fhlmap);
										String ranchar = commonUtil.randomchar();
										String fileName = "KCNET_FHL_EXP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
										HashMap kcfhlMap = new HashMap();
										kcfhlMap.put("KCFHLFILE", fileName);
										//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
										producer.sendBodyAndHeaders("direct:AMS2KCNETFHLqueuesend",fhlmap.get("KCNET_MANIFEST_FHL"), kcfhlMap);
										fhlmap.put("KAMS_KC_MANIFEST", "/KCMANIFEST");
									}else{
										if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
											fhlmap.put("IKAMS_ERRMSG","KCNET MANIFEST UNREGISTERED PIMA");
											fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - KCNET 미등록 PIMA 코드");
											}
										manifest_status = false;
									}
								}else{
									logger.info(" === KCNET EXP MANIFEST RECEIVE MESSAGE SKIP === ");
								}
							}else{
								if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
									fhlmap.put("IKAMS_ERRMSG","KCNET MANIFEST SERVICE IS NOT SUPPORTED");
									fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KCNET 적하 서비스 하지 않음.");
									}
								manifest_status = false;
							}
						}
					}else if(fhlmap.get("IMP_EXP_FLAG").equals("IMP")){
						if(fhlmap.get("IKAMS_SEND") != null && fhlmap.get("IKAMS_SEND").toString().length() > 0 ){
								//수입 IKAMS 전용
								resultChk = dbinserttrace.ikamsImpInsert(ams_out_reverse_key, fhlmap);
								fhlmap.put("IKAMS_ROUTING", "/IKAMS");
								logger.info(" === IKAMS IMP INSERT === ");
						}
						//KTNET 전송 FLAG가 KTNETIMP 일 경우 KTNET 수입 전송
						if(fhlmap.get("KAMS_KT_SEND_FLAG") != null && fhlmap.get("KAMS_KT_SEND_FLAG").toString().length() > 0){

							if(fhlmap.get("KAMS_KT_SEND_FLAG").equals("KTNETIMP")){
								if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85KCNET") !=-1){
									if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
										fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
										fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KTNET 적하 서비스 하지 않음.");
									}
									manifest_status = false;
								}else{
									//KTNET 전송 항공사
									logger.info(" === KTNET IMP MANIFEST FHL CREATE === ");
									fhlmap = createmsg.fhl_kt_con(ams_out_reverse_key, fhlmap);
									String ranchar = commonUtil.randomchar();
									String fileName = "KTNET_FHL_IMP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
									HashMap ktfhlMap = new HashMap();
									ktfhlMap.put("KTFHLFILE", fileName);
									//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
									producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);

									if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("M")){
										fhlmap.put("OUT_AMS_FLAG", fhlmap.get("OCI_FLAG"));
									}else if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("I")){
										fhlmap.put("OUT_KAMS_FLAG", fhlmap.get("OCI_FLAG"));
									}
									fhlmap.put("KAMS_KT_SEND", "/KTNET");
								}
							}
						}

						//KCNET 취합 이면서 수입 M OR I일 경우 I로 변경하여 KCNET 전송
						if(fhlmap.get("KAMS_KCNET_MANIFEST") != null && fhlmap.get("KAMS_KCNET_MANIFEST").toString().length() > 0){

							if(fhlmap.get("KAMS_KCNET_MANIFEST").equals("KCMANIFEST")){

								if(!CommonUtil.nullChk(fhlmap.get("SEND_COMP")).equals("KCNETFHL")){

									if(fhlmap.get("KCNETCODE") != null && fhlmap.get("KCNETCODE").toString().length() > 0){

										logger.info(" === KCNET IMP MANIFEST FHL CREATE === ");
										fhlmap = createmsg.fhl_kc_con_i(ams_out_reverse_key, fhlmap);
										String ranchar = commonUtil.randomchar();
										String fileName = "KCNET_FHL_IMP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
										HashMap kcfhlMap = new HashMap();
										kcfhlMap.put("KCFHLFILE", fileName);
										//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
										producer.sendBodyAndHeaders("direct:AMS2KCNETFHLqueuesend",fhlmap.get("KCNET_MANIFEST_FHL"), kcfhlMap);
										fhlmap.put("KAMS_KC_MANIFEST", "/KCMANIFEST");
									}else{
										if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
											fhlmap.put("IKAMS_ERRMSG","KCNET MANIFEST UNREGISTERED PIMA");
											fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - KCNET 미등록 PIMA 코드");
											}
										manifest_status = false;
									}
								}else{
									logger.info(" === KCNET IMP MANIFEST RECEIVE MESSAGE SKIP === ");
								}

							}else{
								if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
									fhlmap.put("IKAMS_ERRMSG","KCNET MANIFEST SERVICE IS NOT SUPPORTED");
									fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KCNET 적하 서비스 하지 않음.");
									}
								manifest_status = false;
							}
						}
					}else{
						//KCNET 적하 미전송
						if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
							fhlmap.put("IKAMS_ERRMSG","MANIFEST SERVICE IS NOT SUPPORTED");
							fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("TGT_PIMA")+" - 적하 서비스 하지 않음.");
						}
						manifest_status = false;
					}
					//KTNET 취합
				}else if(fhlmap.get("MANIFEST_COMPANY").equals("KTNET")){
					//KTENT 수입 건 일 경우
					if(fhlmap.get("IMP_EXP_FLAG").equals("IMP")){
						//KTNET 전송 FLAG가 KTNETIMP 일 경우 KTNET 수입 전송
						if(fhlmap.get("KAMS_KT_SEND_FLAG") != null && fhlmap.get("KAMS_KT_SEND_FLAG").toString().length() > 0){

							if(fhlmap.get("KAMS_KT_SEND_FLAG").equals("KTNETIMP")){
								if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85KCNET") !=-1){
									if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
										fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
										fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KTNET 적하 서비스 하지 않음.");
									}
									manifest_status = false;
								}else{
									//KTNET IMP 전송 항공사
									logger.info(" === KTNET IMP MANIFEST FHL CREATE === ");
									fhlmap = createmsg.fhl_kt_con(ams_out_reverse_key, fhlmap);
									String ranchar = commonUtil.randomchar();
									String fileName = "KTNET_FHL_IMP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
									HashMap ktfhlMap = new HashMap();
									ktfhlMap.put("KTFHLFILE", fileName);
									//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
									producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);

									if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("M")){
										fhlmap.put("OUT_AMS_FLAG", fhlmap.get("OCI_FLAG"));
									}else if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("I")){
										fhlmap.put("OUT_KAMS_FLAG", fhlmap.get("OCI_FLAG"));
									}
									fhlmap.put("KAMS_KT_SEND", "/KTNET");
								}
							}
						}else if(fhlmap.get("IKAMS_SEND") != null && fhlmap.get("IKAMS_SEND").toString().length() > 0){
							//수입 IKAMS 전용
							resultChk = dbinserttrace.ikamsImpInsert(ams_out_reverse_key, fhlmap);
							fhlmap.put("IKAMS_ROUTING", "/IKAMS");
							//도착지 Routing 생각 하기
							logger.info(" === IKAMS IMP INSERT === ");
						}
					}else if(fhlmap.get("IMP_EXP_FLAG").equals("EXP")){
						//M 일때 I로 변경 또는 I 일때 FHL/4 헤더 정보 변경 후 KTNET 전송
						if(fhlmap.get("KAMS_KTNET_MANIFEST") != null && fhlmap.get("KAMS_KTNET_MANIFEST").toString().length() > 0){

							if(fhlmap.get("MSG_CTRL_ID").equals("AIRCIS") && fhlmap.get("OCI_FLAG").equals("M")){
								//AIRCIS M건일 경우 AMSAIR로 1COPY 전송
								if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
									fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
									fhlmap.put("IKAMS_ERRMSG_KOR", "AIRCIS FLAG M KTNET 적하 서비스 하지 않음");
								}
								manifest_status = false;
							}else{
								if(fhlmap.get("KAMS_KTNET_MANIFEST").equals("KTMANIFEST")){
									if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85KCNET") !=-1){
										if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
											fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
											fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KTNET 적하 서비스 하지 않음.");
										}
										manifest_status = false;
									}else{
										logger.info(" === KTNET EXP MANIFEST FHL CREATE === ");
										fhlmap = createmsg.fhl_kt_con_i(ams_out_reverse_key, fhlmap);
										String ranchar = commonUtil.randomchar();
										String fileName = "KTNET_FHL_EXP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
										HashMap ktfhlMap = new HashMap();
										ktfhlMap.put("KTFHLFILE", fileName);
										//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
										producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);
										fhlmap.put("OUT_KAMS_FLAG", "I");
										fhlmap.put("KAMS_KT_MANIFEST", "/KTMANIFEST");
									}
								}
							}
						}
					}else{
						//KTNET 적하목록 서비스 미 지원 오류 생성
						if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
							fhlmap.put("IKAMS_ERRMSG","MANIFEST SERVICE IS NOT SUPPORTED");
							fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("TGT_PIMA")+" - 적하 서비스 하지 않음.");
						}
						manifest_status = false;
					}

				}else{

					//취합사 구분 어려운 곳
					if(fhlmap.get("IMP_EXP_FLAG").equals("EXP")){
						if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("M") || CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("I") || CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("X")){
							if(fhlmap.get("IKAMS_SEND") != null && fhlmap.get("IKAMS_SEND").toString().length() > 0){
								//수출 IKAMS 전용
								resultChk = dbinserttrace.ikamsExpInsert(ams_out_reverse_key, fhlmap);
								if(resultChk > 0){
									if(fhlmap.get("OCI_CHECK").equals("C")){
										fhlmap.put("OCI_CHECK", "P");
										APIflag = ikamsvalidationdoc.OCICheck(fhlmap);
										//20191204 최재준 추가
										//관세청 수출 epn no 체크 api(KE/LJ만)
										if(APIflag == false){
											/*fhlmap.put("IKAMS_SMI", "FNA");
											fnaMsg = createack.FNAack(fhlmap);*/
											manifest_status = false;
										}
									}
								}
								fhlmap.put("IKAMS_ROUTING", "/IKAMS");
								logger.info(" === IKAMS EXP INSERT === ");
							}
						}

						//M 일때 I로 변경 또는 I 일때 FHL/4 헤더 정보 변경 후 KTNET 전송
						if(fhlmap.get("KAMS_KTNET_MANIFEST") != null && fhlmap.get("KAMS_KTNET_MANIFEST").toString().length() > 0){

							if(fhlmap.get("MSG_CTRL_ID").equals("AIRCIS") && fhlmap.get("OCI_FLAG").equals("M")){
								//AIRCIS M건일 경우 AMSAIR로 1COPY 전송
								if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
									fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
									fhlmap.put("IKAMS_ERRMSG_KOR", "AIRCIS FLAG M KTNET 적하 서비스 하지 않음");
								}
								manifest_status = false;
							}else{
								if(fhlmap.get("KAMS_KTNET_MANIFEST").equals("KTMANIFEST")){
									if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85KCNET") !=-1){
										if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
											fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
											fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KTNET 적하 서비스 하지 않음.");
										}
										manifest_status = false;
									}else{
										logger.info(" === KTNET EXP MANIFEST FHL CREATE === ");
										fhlmap = createmsg.fhl_kt_con_i(ams_out_reverse_key, fhlmap);
										String ranchar = commonUtil.randomchar();
										String fileName = "KTNET_FHL_EXP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
										HashMap ktfhlMap = new HashMap();
										ktfhlMap.put("KTFHLFILE", fileName);
										//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
										producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);
										fhlmap.put("OUT_KAMS_FLAG", "I");
										fhlmap.put("KAMS_KT_MANIFEST", "/KTMANIFEST");
									}
								}
							}
						}

						//KCNET 취합 이면서 수출 M OR I 일 경우 I로 변경하여 KCNET 전송
						if(fhlmap.get("KAMS_KCNET_MANIFEST") != null && fhlmap.get("KAMS_KCNET_MANIFEST").toString().length() > 0){
							if(fhlmap.get("KAMS_KCNET_MANIFEST").equals("KCMANIFEST")){

								if(!CommonUtil.nullChk(fhlmap.get("SEND_COMP")).equals("KCNETFHL")){

									if(fhlmap.get("KCNETCODE") != null && fhlmap.get("KCNETCODE").toString().length() > 0){

										logger.info(" === KCNET EXP MANIFEST FHL CREATE === ");
										fhlmap = createmsg.fhl_kc_con_i(ams_out_reverse_key, fhlmap);
										String ranchar = commonUtil.randomchar();
										String fileName = "KCNET_FHL_EXP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
										HashMap kcfhlMap = new HashMap();
										kcfhlMap.put("KCFHLFILE", fileName);
										//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
										producer.sendBodyAndHeaders("direct:AMS2KCNETFHLqueuesend",fhlmap.get("KCNET_MANIFEST_FHL"), kcfhlMap);
										fhlmap.put("KAMS_KC_MANIFEST", "/KCMANIFEST");
									}else{
										if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
											fhlmap.put("IKAMS_ERRMSG","KCNET MANIFEST UNREGISTERED PIMA");
											fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - KCNET 미등록 PIMA 코드");
											}
										manifest_status = false;
									}
								}else{
									logger.info(" === KCNET EXP MANIFEST RECEIVE MESSAGE SKIP === ");
								}

							}else{
								if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
									fhlmap.put("IKAMS_ERRMSG","KCNET MANIFEST SERVICE IS NOT SUPPORTED");
									fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KCNET 적하 서비스 하지 않음.");
									}
								manifest_status = false;
							}
						}
					}else if(fhlmap.get("IMP_EXP_FLAG").equals("IMP")){
						if(fhlmap.get("IKAMS_SEND") != null && fhlmap.get("IKAMS_SEND").toString().length() > 0 ){
							//수입 IKAMS 전용
							resultChk = dbinserttrace.ikamsImpInsert(ams_out_reverse_key, fhlmap);
							fhlmap.put("IKAMS_ROUTING", "/IKAMS");
							logger.info(" === IKAMS IMP INSERT === ");
						}
						//KTNET 전송 FLAG가 KTNETIMP 일 경우 KTNET 수입 전송
						if(fhlmap.get("KAMS_KT_SEND_FLAG") != null && fhlmap.get("KAMS_KT_SEND_FLAG").toString().length() > 0){

							if(fhlmap.get("KAMS_KT_SEND_FLAG").equals("KTNETIMP")){
								if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85KCNET") !=-1){
									if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
										fhlmap.put("IKAMS_ERRMSG","KTNET MANIFEST SERVICE IS NOT SUPPORTED");
										fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KTNET 적하 서비스 하지 않음.");
									}
									manifest_status = false;
								}else{
									//KTNET 전송 항공사
									logger.info(" === KTNET IMP MANIFEST FHL CREATE === ");
									fhlmap = createmsg.fhl_kt_con(ams_out_reverse_key, fhlmap);
									String ranchar = commonUtil.randomchar();
									String fileName = "KTNET_FHL_IMP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
									HashMap ktfhlMap = new HashMap();
									ktfhlMap.put("KTFHLFILE", fileName);
									//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
									producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);

									if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("M")){
										fhlmap.put("OUT_AMS_FLAG", fhlmap.get("OCI_FLAG"));
									}else if(CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("I")){
										fhlmap.put("OUT_KAMS_FLAG", fhlmap.get("OCI_FLAG"));
									}
									fhlmap.put("KAMS_KT_SEND", "/KTNET");
								}
							}
						}

						//KCNET 취합 이면서 수입 M OR I일 경우 I로 변경하여 KCNET 전송
						if(fhlmap.get("KAMS_KCNET_MANIFEST") != null && fhlmap.get("KAMS_KCNET_MANIFEST").toString().length() > 0){

							if(fhlmap.get("KAMS_KCNET_MANIFEST").equals("KCMANIFEST")){

								if(!CommonUtil.nullChk(fhlmap.get("SEND_COMP")).equals("KCNETFHL")){

									if(fhlmap.get("KCNETCODE") != null && fhlmap.get("KCNETCODE").toString().length() > 0){

										logger.info(" === KCNET IMP MANIFEST FHL CREATE === ");
										fhlmap = createmsg.fhl_kc_con_i(ams_out_reverse_key, fhlmap);
										String ranchar = commonUtil.randomchar();
										String fileName = "KCNET_FHL_IMP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
										HashMap kcfhlMap = new HashMap();
										kcfhlMap.put("KCFHLFILE", fileName);
										//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
										producer.sendBodyAndHeaders("direct:AMS2KCNETFHLqueuesend",fhlmap.get("KCNET_MANIFEST_FHL"), kcfhlMap);
										fhlmap.put("KAMS_KC_MANIFEST", "/KCMANIFEST");
									}else{
										if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
											fhlmap.put("IKAMS_ERRMSG","KCNET MANIFEST UNREGISTERED PIMA");
											fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - KCNET 미등록 PIMA 코드");
											}
										manifest_status = false;
									}
								}else{
									logger.info(" === KCNET IMP MANIFEST RECEIVE MESSAGE SKIP === ");
								}

							}else{
								if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
									fhlmap.put("IKAMS_ERRMSG","KCNET MANIFEST SERVICE IS NOT SUPPORTED");
									fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("SRC_PIMA")+" - PIMA는 KCNET 적하 서비스 하지 않음.");
									}
								manifest_status = false;
							}
						}
					}else{
						//적하 미전송
						if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"), "").equals("")){
							fhlmap.put("IKAMS_ERRMSG","MANIFEST SERVICE IS NOT SUPPORTED");
							fhlmap.put("IKAMS_ERRMSG_KOR", fhlmap.get("TGT_PIMA")+" - 적하 서비스 하지 않음.");
						}
						manifest_status = false;
					}
				}

				//e-freight DB Insert 로직
				/*System.out.println(fhlmap.get("OCI_FLAG"));
				if(fhlmap.get("OCI_FLAG").equals("I")){
					//등록된 수입 PIMA 및 한진 PIMA(RKRAGT82AMSHNX01/SEL01) 전송 여부 확인 필요
					routingEfreight(ams_out_reverse_key, fhlmap);
					System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
				}*/
		}catch(IndexOutOfBoundsException e){
			logger.info(" === AMS MANIFEST ROUTE Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			fhlmap.put("IKAMS_ERRMSG", "MANIFEST ROUTE CHECK EXCEPTION");
			manifest_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Manifest Route Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === AMS MANIFEST ROUTE Check NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			fhlmap.put("IKAMS_ERRMSG", "MANIFEST ROUTE CHECK EXCEPTION");
			manifest_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Manifest Route Check NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === AMS MANIFEST ROUTE Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			fhlmap.put("IKAMS_ERRMSG", "MANIFEST ROUTE CHECK EXCEPTION");
			manifest_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Manifest Route Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === AMS MANIFEST ROUTE Check Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			fhlmap.put("IKAMS_ERRMSG", "MANIFEST ROUTE CHECK EXCEPTION");
			manifest_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Manifest Route Check Exception",e, msg);
		}finally{
			if(resultChk == 0){
				new Exception();
			}
			if(!manifest_status){
				/*if((fhlmap.get("MANIFEST_COMPANY").equals("") || fhlmap.get("MANIFEST_COMPANY").toString().length() == 0)
						|| (CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTE_POINT")).equals("") || CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTE_POINT")).toString().length() == 0)){*/
				if(fhlmap.get("MANIFEST_COMPANY").equals("") || fhlmap.get("MANIFEST_COMPANY").toString().length() == 0){

					logger.info(" === FHL MANIFEST ACK SKIP === ");

				}else{
					fhlmap.put("IKAMS_SMI", "FNA");
					fnaMsg = createack.FNAack(fhlmap);
					fhlmap.put("KAMS_ACK", fnaMsg);

					HashMap logMap = new HashMap();
					//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
					logMap.put("HISTORY_STATUS", "Manifest_Route_Check Error");
					logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
					logMap.put("IKAMS_STEP", "709");

					commonSql.insertHistory(logMap);
					commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);

					logger.info(" === FHL MANIFEST ROUTE CHECK ERROR === ");
				}

			}else{
				/*if((fhlmap.get("MANIFEST_COMPANY").equals("") || fhlmap.get("MANIFEST_COMPANY").toString().length() == 0)
				|| (CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTE_POINT")).equals("") || CommonUtil.nullChk(fhlmap.get("IKAMS_ROUTE_POINT")).toString().length() == 0)){*/
				if(fhlmap.get("MANIFEST_COMPANY").equals("") || fhlmap.get("MANIFEST_COMPANY").toString().length() == 0){

					logger.info(" === FHL MANIFEST ACK SKIP === ");

				}else{
					if((CommonUtil.nullChk(fhlmap.get("KAMS_KCNET_MANIFEST")).equals("KCMANIFEST") && CommonUtil.nullChk(fhlmap.get("SEND_COMP")).equals("KCNETFHL"))){

						if(fhlmap.get("IKAMS_SEND") != null && fhlmap.get("IKAMS_SEND").toString().length() > 0){

							fhlmap.put("IKAMS_SMI", "FMA");
							fmaMsg = createack.FMAack(fhlmap);
							fhlmap.put("KAMS_ACK", fmaMsg);

							HashMap logMap = new HashMap();
							//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
							logMap.put("HISTORY_STATUS", "Manifest_Route_Check");
							logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
							logMap.put("IKAMS_STEP", IKAMS_STEP);

							commonSql.insertHistory(logMap);
							commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);

						}else{

							logger.info(" === KCNET FHL MANIFEST ACK SKIP === ");
						}

					}else{
						fhlmap.put("IKAMS_SMI", "FMA");
						fmaMsg = createack.FMAack(fhlmap);
						fhlmap.put("KAMS_ACK", fmaMsg);

						HashMap logMap = new HashMap();
						//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
						logMap.put("HISTORY_STATUS", "Manifest_Route_Check");
						logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
						logMap.put("IKAMS_STEP", IKAMS_STEP);

						commonSql.insertHistory(logMap);
						commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
					}

					logger.info(" === FHL MANIFEST ROUTE CHECK SUCCESSS === ");

				}
			}
		}
		return fhlmap;
	}
/*
	//E-freight routing
	public void routingEfreight(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap) throws Exception {
		boolean efreight_status = true;

		String msg = fhlmap.get("EDI").toString();
		String DOC_NO = fhlmap.get("MBI_FULL").toString()+"_"+fhlmap.get("HBS_NO").toString()+"_"+fhlmap.get("VERSION").toString();
		logger.info(" === FHL Efreight INSERT START === ");

		try{

			if(fhlmap.get("SRC_PIMA").equals("RKRAGT82AMSHNX01/SEL01")){
				fhlmap.put("MFT_STATUS", "TRAXON_SENT");
				//T_EF_IMP_FHL_HISTORY INSERT
				if(dbinserttrace.insertEfregightFHL(ams_out_reverse_key, fhlmap)){
					logger.info(" === e-freight DB T_EF_IMP_FHL_HISTORY INSERT SUCCUSS === : " + fhlmap.get("MBI_FULL") + "_" + fhlmap.get("HBS_NO"));
				}else{
					logger.info(" === e-freight DB T_EF_IMP_FHL_HISTORY INSERT FAIL === : " + fhlmap.get("MBI_FULL") + "_" + fhlmap.get("HBS_NO"));
				}
				//T_EF_IMP_HAWB_HISTORY_INSERT
				if(dbinserttrace.insertIMPHISTORYefreight(ams_out_reverse_key, fhlmap)){
					logger.info(" === e-freight DB T_EF_IMP_HAWB_HISTORY INSERT SUCCUSS === : " + fhlmap.get("MBI_FULL") + "_" + fhlmap.get("HBS_NO"));
				}else{
					logger.info(" === e-freight DB T_EF_IMP_HAWB_HISTORY INSERT FAIL === : " + fhlmap.get("MBI_FULL") + "_" + fhlmap.get("HBS_NO"));
				}
				//T_EF_IMP_HAWB COUNT CHECK
				int checkpoint = (int)sqlMapClient.queryForObject("TM_TRACE.countIMPefreight", fhlmap);

				if(checkpoint !=0 && checkpoint !=999){
					//T_EF_IMP_HAWB UPDATE
					if(dbinserttrace.insertIMPHISTORYefreight(ams_out_reverse_key, fhlmap)){
						logger.info(" === e-freight T_EF_IMP_HAWB DB UPDATE SUCCUSS === : " + fhlmap.get("MBI_FULL") + "_" + fhlmap.get("HBS_NO"));
					}else{
						logger.info(" === e-freight T_EF_IMP_HAWB DB UPDATE FAIL === : " + fhlmap.get("MBI_FULL") + "_" + fhlmap.get("HBS_NO"));
					}
				}else{
					//T_EF_IMP_HAWB INSERT.
					if(dbinserttrace.insertIMPefreight(ams_out_reverse_key, fhlmap)){
						logger.info(" === e-freight DB T_EF_IMP_HAWB INSERT SUCCUSS === : " + fhlmap.get("MBI_FULL") + "_" + fhlmap.get("HBS_NO"));
					}else{
						logger.info(" === e-freight DB T_EF_IMP_HAWB INSERT FAIL === : " + fhlmap.get("MBI_FULL") + "_" + fhlmap.get("HBS_NO"));
					}
				}
			}else{
				efreight_status = false;
			}


		}catch(IndexOutOfBoundsException e){
			logger.info(" === Efreight Insert IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			efreight_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","Efreight Insert IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === Efreight Insert NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			efreight_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","Efreight Insert NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === Efreight Insert Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			efreight_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","Efreight Insert SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === Efreight Insert Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","Efreight Insert Exception",e, msg);
			efreight_status = false;
		}finally{
			if(!efreight_status){

				HashMap logMap = new HashMap();
				//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
				logMap.put("HISTORY_STATUS", "Efreight_Insert Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("IKAMS_STEP", IKAMS_STEP);

				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);

				logger.info(" === FHL Efreight INSERT ERROR === ");

			}else{

				HashMap logMap = new HashMap();
				//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
				logMap.put("HISTORY_STATUS", "Efreight_Insert_Check");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("IKAMS_STEP", IKAMS_STEP);

				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);

				logger.info(" === FHL Efreight INSERT SUCCESSS === ");

			}
		}
	}*/
}
