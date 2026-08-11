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
import com.trx.validate.ValidationDoc;

public class Routing {

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

	@Value("#{prop['ktnet.write.path']}")
	//@Value("${ktnet.write.path}")
	private String ktnet_write_path;

	//tw 항공사 typeb 용
	/*@Value("#{prop['bkk.addr']}")
	private String bkk_addr;

	@Value("#{prop['pnh.addr']}")
	private String pnh_addr;

	@Value("#{prop['sgn.addr']}")
	private String sgn_addr;

	@Value("#{prop['handad.addr']}")
	private String handad_addr;

	@Value("#{prop['nrt.addr']}")
	private String nrt_addr;

	@Value("#{prop['jfk.addr']}")
	private String jfk_addr;

	@Value("#{prop['kix.addr']}")
	private String kix_addr;

	@Value("#{prop['tao.addr']}")
	private String tao_addr;

	@Value("#{prop['vte.addr']}")
	private String vte_addr;*/

	@Value("#{prop['zejpn.addr']}")
	private String zejpn_addr;

	final static String AMS_STEP = "700";

	//Routing 시작
	/*
	 * 2016.08.22~
	 * KTNET 수출 관련 Routing 추가
	 * AIRCIS M건일 경우 KTNETAMS만 AMQ로 전송
	 * 그 외의 문서는 KTNET FHL/4 스펙으로 변경하여 I건으로 KTNET AMQ 전송
	 * */
	public void routing(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, Exchange exchange) throws Exception {
		boolean flag = true;
		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> route_code = new HashMap<String,Object>();
		String fnaMsg ="";
		String tgt_point = "";
		String rou_point = "";
		String msg = "";
		List result = new ArrayList();
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("EDI_PARSE");
		msg = (String) CommonUtil.nullChk(fhlmap.get("EDI"));
		//재처리시 WEB에서 넘어오는 값 및 처리 프로세스
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

		logger.info(" === FHL ROUTE CHECK START === ");

		//FHL/4 OCI 항목 없이 들어 올 경우 KE 및 TRAXON NETWORK(외항사) 및 TYPEB 항공사로만 FHL 전송
		//ROUTING 테이블에 IMP_EXP_FLAG는 AMS OCI_TYPE은 T 로 설정
		if(CommonUtil.nullChk(fhlmap.get("VERSION")).equals("4") && CommonUtil.nullChk(fhlmap.get("OCI_TYPE")).equals("")){
			fhlmap.put("OCI_TYPE", "AMS");
		}
		if(CommonUtil.nullChk(fhlmap.get("VERSION")).equals("4") && CommonUtil.nullChk(fhlmap.get("OCI_FLAG")).equals("")){
			fhlmap.put("OCI_FLAG", "T");
		}
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
				fhlmap.put("TRANSSHIPMENT", "ANY");

				logger.info(" === TRANSSHPIMENT : ANY === ");
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
/*			if(CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")).equals("T") || CommonUtil.nullChk(fhlmap.get("OCI_CARGO_TYPE")).equals("R")){
				//ROUTING 테이블에 ANY 표시 되어 있는 항공사는 출/도착지가 있는 항공사로 환적일 경우 출/도착지 구분 없이 ROUTING
				fhlmap.put("TRANSSHIPMENT", "ANY");

				if(CommonUtil.nullChk(web_flag).length() > 0){
					logger.info(" === REPROC FLAG : " + web_flag + " === ");
					result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
				}else{
					result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
				}

			}else{
				//출/도착지가 있는 경우
				fhlmap.put("ORG_DES_CHK", "Y");

				if(CommonUtil.nullChk(web_flag).length() > 0){
					logger.info(" === REPROC FLAG : " + web_flag + " === ");
					result = sqlMapClient.queryForList("TM_TRACE.webRoutingCheck", fhlmap);
				}else{
					result = sqlMapClient.queryForList("TM_TRACE.routingCheck", fhlmap);
				}
			}
*/		}else{

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
					route_code = (HashMap) result.get(i);

					fhlmap.put("CARR_CODE", (String) CommonUtil.nullChk(route_code.get("CARR_CODE")));
					fhlmap.put("SMI_VERSION", (String) CommonUtil.nullChk(route_code.get("SMI_VERSION")));
					fhlmap.put("IMP_EXP_FLAG", (String) CommonUtil.nullChk(route_code.get("IMP_EXP_FLAG")));
					fhlmap.put("OCI_TYPE_FLAG", (String) CommonUtil.nullChk(route_code.get("OCI_TYPE_FLAG")));
					fhlmap.put("MANIFEST_COMPANY", (String) CommonUtil.nullChk(route_code.get("MANIFEST_COMPANY")));
					fhlmap.put("KTNET_SEND_FLAG", (String) CommonUtil.nullChk(route_code.get("KTNET_SEND_FLAG")));
					fhlmap.put("AIR_PIMA_MSB_SEND", (String) CommonUtil.nullChk(route_code.get("AIR_PIMA_MSB_SEND")));
					fhlmap.put("AIR_PIMA_MSB_SEND_VER", (String) CommonUtil.nullChk(route_code.get("AIR_PIMA_MSB_SEND_VER")));
					fhlmap.put("CONV_MULTI_SEND", (String) CommonUtil.nullChk(route_code.get("CONV_MULTI_SEND")));
					fhlmap.put("SITA_TYPEB_SEND", (String) CommonUtil.nullChk(route_code.get("SITA_TYPEB_SEND")));
					fhlmap.put("SITA_TYPEB_SEND_VER", (String) CommonUtil.nullChk(route_code.get("SITA_TYPEB_SEND_VER")));
					fhlmap.put("MULTI_SEND", (String) CommonUtil.nullChk(route_code.get("MULTI_SEND")));
					fhlmap.put("ORG", (String) CommonUtil.nullChk(route_code.get("ORG")));
					fhlmap.put("DST", (String) CommonUtil.nullChk(route_code.get("DST")));
					fhlmap.put("SEND_QNAME", (String) CommonUtil.nullChk(route_code.get("SEND_QNAME")));
					fhlmap.put("KTNET_SEND_FLAG2", (String) CommonUtil.nullChk(route_code.get("KTNET_SEND_FLAG2")));
					fhlmap.put("AIR_PIMA_MSB_SEND2", (String) CommonUtil.nullChk(route_code.get("AIR_PIMA_MSB_SEND2")));
					fhlmap.put("AIR_PIMA_MSB_SEND_VER2", (String) CommonUtil.nullChk(route_code.get("AIR_PIMA_MSB_SEND_VER2")));
					fhlmap.put("CONV_MULTI_SEND2", (String) CommonUtil.nullChk(route_code.get("CONV_MULTI_SEND2")));
					fhlmap.put("SITA_TYPEB_SEND2", (String) CommonUtil.nullChk(route_code.get("SITA_TYPEB_SEND2")));
					fhlmap.put("SITA_TYPEB_SEND_VER2", (String) CommonUtil.nullChk(route_code.get("SITA_TYPEB_SEND_VER2")));
				}

				HashMap logMap = new HashMap();
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);

				if(fhlmap.get("KTNET_SEND_FLAG2") != null && fhlmap.get("KTNET_SEND_FLAG2").toString().length() > 0){
					if(fhlmap.get("KTNET_SEND_FLAG2").equals("KTNETAMS")){
						fhlmap.put("ROUTING_KT", "/KTNETAMS");
					}else if(fhlmap.get("KTNET_SEND_FLAG2").equals("KTNETIMP") && (fhlmap.get("OCI_TYPE").equals("AMS") && fhlmap.get("OCI_FLAG").equals("T"))){
						fhlmap.put("ROUTING_KT", "/KTNET");
					}
				}
				if(fhlmap.get("AIR_PIMA_MSB_SEND2") != null && fhlmap.get("AIR_PIMA_MSB_SEND2").toString().length() > 0){
					fhlmap.put("ROUTING_OAL", "/MSB_OAL");
				}
				if(fhlmap.get("AIR_PIMA_MSB_SEND_VER2") != null && fhlmap.get("AIR_PIMA_MSB_SEND_VER2").toString().length() > 0){
					fhlmap.put("ROUTING_OAL4", "/OAL4");
				}
				if(fhlmap.get("CONV_MULTI_SEND2") != null && fhlmap.get("CONV_MULTI_SEND2").toString().length() > 0){
					fhlmap.put("ROUTING_KE", "/MSB_KE");
				}
				if(fhlmap.get("SITA_TYPEB_SEND2") != null && fhlmap.get("SITA_TYPEB_SEND2").toString().length() > 0){
					fhlmap.put("ROUTING_TYPEB", "/TYPEB");
				}
				if(fhlmap.get("SITA_TYPEB_SEND_VER2") != null && fhlmap.get("SITA_TYPEB_SEND_VER2").toString().length() > 0){
					fhlmap.put("ROUTING_TYPEB_VER", "/TY_VER4");
				}

				//ROUTING 테이블에 저장 된 조회 된 항공사의 원래 Routing 값
				rou_point = CommonUtil.nullChk(fhlmap.get("ROUTING_KE"))+CommonUtil.nullChk(fhlmap.get("ROUTING_OAL"))+CommonUtil.nullChk(fhlmap.get("ROUTING_OAL4"))
						+CommonUtil.nullChk(fhlmap.get("ROUTING_TYPEB"))+CommonUtil.nullChk(fhlmap.get("ROUTING_TYPEB_VER"))+CommonUtil.nullChk(fhlmap.get("ROUTING_KT"));

				fhlmap.put("ROUTE_POINT", rou_point.replaceFirst("/", ""));
				commonSql.updateTgtPoint(ams_out_reverse_key, logMap, fhlmap);
				exchange.getIn().setHeader("AMS_ROUTING_FLAG",Boolean.valueOf(flag));
			}

			logger.info(" === FHL VERSION : " + CommonUtil.nullChk(fhlmap.get("VERSION")) + " === ");
			logger.info(" === SEND_COMP : " + CommonUtil.nullChk(fhlmap.get("SEND_COMP")) + " === ");
			logger.info(" === MSG_CTRL_ID :" + CommonUtil.nullChk(fhlmap.get("MSG_CTRL_ID")) + " === ");
			logger.info(" === AIRLINE : " + CommonUtil.nullChk(fhlmap.get("TGT_PIMA")) + " === ");
			logger.info(" === MAWB/HWB NO : " + CommonUtil.nullChk(fhlmap.get("MBI_NO")) + "_" + CommonUtil.nullChk(fhlmap.get("HBS_NO")) + " === ");
			logger.info(" === ORG/DST : " + CommonUtil.nullChk(fhlmap.get("M_ORG")) +  "/" + CommonUtil.nullChk(fhlmap.get("M_DST")) + " === ");
			logger.info(" === OCI TYPE/FLAG : " + CommonUtil.nullChk(fhlmap.get("OCI_TYPE")) +  "/" + CommonUtil.nullChk(fhlmap.get("OCI_FLAG")) + " === ");
			logger.info(" === MANIFEST : " + CommonUtil.nullChk(fhlmap.get("MANIFEST_COMPANY")) + " === ");


			//FHL 버전 4
			if(CommonUtil.nullChk(fhlmap.get("SMI_VERSION")).equals("4")){
				logger.info(" === FHL/4 ROUTE CHECK START === ");
				//OCI 값이 수출 EXP 일 때
				//AMS FHL4 Routing
				fhlmap = routingAMS(ams_out_reverse_key, fhlmap, exchange);

				//routing 로직 탄 후 셋팅 되어 MSB 및 외부로 전송되는 실제 전송 값
				tgt_point = CommonUtil.nullChk(fhlmap.get("MSB_KE_SEND"))+CommonUtil.nullChk(fhlmap.get("MSB_OAL_SEND"))+CommonUtil.nullChk(fhlmap.get("MSB_OAL_SEND4"))
							+CommonUtil.nullChk(fhlmap.get("TYPEB_SEND"))+CommonUtil.nullChk(fhlmap.get("TYPEB_SEND_VER"))+CommonUtil.nullChk(fhlmap.get("KTNET_SEND"))+CommonUtil.nullChk(fhlmap.get("KAMS_KT_SEND"));

				fhlmap.put("TGT_POINT", tgt_point.replaceFirst("/", ""));
				HashMap logMap = new HashMap();
				//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
				logMap.put("HISTORY_STATUS", "FHL/4 Routing");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", AMS_STEP);

				exchange.getIn().setHeader("AMS_ROUTING_FLAG",(Boolean)fhlmap.get("FLAG_STATUS"));
				commonSql.insertHistory(logMap);
				commonSql.updateTgtPoint(ams_out_reverse_key, logMap, fhlmap);
				logger.info(" === FHL/4 ROUTE CHECK SUCCESSS === ");
			}else if(CommonUtil.nullChk(fhlmap.get("SMI_VERSION")).equals("2")){
				logger.info(" === FHL/2 ROUTE CHECK START === ");

				//AMS FHL2 Routing
				fhlmap = routingAMS(ams_out_reverse_key, fhlmap, exchange);

				tgt_point = CommonUtil.nullChk(fhlmap.get("MSB_KE_SEND"))+CommonUtil.nullChk(fhlmap.get("MSB_OAL_SEND"))+CommonUtil.nullChk(fhlmap.get("MSB_OAL_SEND4"))
						   +CommonUtil.nullChk(fhlmap.get("TYPEB_SEND"))+CommonUtil.nullChk(fhlmap.get("TYPEB_SEND_VER"))+CommonUtil.nullChk(fhlmap.get("KTNET_SEND"))+CommonUtil.nullChk(fhlmap.get("KAMS_KT_SEND"));

				fhlmap.put("TGT_POINT", tgt_point.replaceFirst("/", ""));

				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "FHL/2 Routing");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", AMS_STEP);

				exchange.getIn().setHeader("AMS_ROUTING_FLAG",(Boolean)fhlmap.get("FLAG_STATUS"));
				commonSql.insertHistory(logMap);
				commonSql.updateTgtPoint(ams_out_reverse_key, logMap, fhlmap);
				logger.info(" === FHL/2 ROUTE CHECK SUCCESSS === ");
			}else{
				logger.info(" === FHL ROUTE CHECK ERROR === ");

				if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
					fhlmap.put("ERRORMSG",  fhlmap.get("TGT_PIMA") +" - AMS SERVICE IS NOT AVAILABLE FOR THIS AIRLINE");
					fhlmap.put("ERRORMSG_KOR", fhlmap.get("TGT_PIMA") +" - 서비스 하지 않는 항공사");
				}
				fhlmap.put("AMS_SMI", "FNA");
				fnaMsg = createack.FNAack(fhlmap);
				//fna 로그 담을 곳 체크(변수값 및 테이블 컬럼)
				fhlmap.put("AMS_ACK", fnaMsg);

				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "AMS Route_Check Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", "709");
				flag = false;
				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
				exchange.getIn().setHeader("AMS_ROUTING_FLAG",Boolean.valueOf(flag));
				exchange.getIn().setHeader("FHLMAP",fhlmap);
				exchange.getIn().setHeader("AIR_CARR_CODE",fhlmap.get("CARR_CODE"));
			}

		}catch(IndexOutOfBoundsException e){
			logger.info(" === ROUTE Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","ROUTE Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === ROUTE Check is NullPointer === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			//mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","ROUTE Check NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === ROUTE Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","ROUTE Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === ROUTE Check Error === ");
	    	logger.error("ERROR==>"+e.toString(), e);
	    	mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","ROUTE Check Exception",e, msg);
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

	//ams routing
	public HashMap<String, Object> routingAMS(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, HashMap<String, Object> fhlmap, Exchange exchange) throws Exception {
		boolean ams_status = true;

		String msg = fhlmap.get("EDI").toString();
		String fmaMsg ="";
		String fnaMsg ="";

		logger.info(" === FHL AMS ROUTE CHECK START === ");

		try{

//			//20210729 싸이버로지텍 연계 관련 MSB 전송 Routing 삭제 추가
//			//20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
//			//KE+IKAMS+KTNET+KCNET 전송만 가능
//			if(fhlmap.get("SRC_PIMA").toString().startsWith("RKRAGT85CYBERLOGITEC/")){
//
//				fhlmap.put("AIR_PIMA_MSB_SEND", "");
//				fhlmap.put("AIR_PIMA_MSB_SEND_VER", "");
//				fhlmap.put("SITA_TYPEB_SEND", "");
//				fhlmap.put("SITA_TYPEB_SEND_VER", "");
//				fhlmap.put("MULTI_SEND", "");
//			}

			//KCNET 취합
			if(fhlmap.get("MANIFEST_COMPANY").equals("KCNET")){

				//수입 OCI M 건일 때 수입 PIMA 이면서 KCNET 취합일때 KE+IKAMS 전송
				/* 25.11.12 YHT 이면서 KE 아닌경우 버린 조건 제외(항공사로 전송)히스토리X -컨펌 nkjeon
				 * if(((CommonUtil.nullChk(fhlmap.get("IMP_ROUTE")).equals("KCNET") &&
				 * CommonUtil.nullChk(fhlmap.get("SRC_PIMA")).toString().indexOf(
				 * "RKRAGT85KCNET") !=-1) ||
				 * CommonUtil.nullChk(fhlmap.get("IMP_ROUTE")).equals("YHT")) &&
				 * CommonUtil.nullChk(fhlmap.get("IMP_USE_FLAG")).equals("Y")){
				 * logger.info(" === IMP ROUTE CARR CODE CHANGE KE AND MSB SEND === ");
				 * fhlmap.put("AIR_PIMA_MSB_SEND", ""); fhlmap.put("AIR_PIMA_MSB_SEND_VER", "");
				 * fhlmap.put("SITA_TYPEB_SEND", ""); fhlmap.put("SITA_TYPEB_SEND_VER", "");
				 * fhlmap.put("KTNET_SEND_FLAG", ""); fhlmap.put("MULTI_SEND", "");
				 *
				 * HashMap logMap = new HashMap(); logMap.put("HISTORY_STATUS",
				 * "IMP Route Carr Code Change KE and MSB Send"); logMap.put("AMS_TRX_ROUTE",
				 * ams_out_reverse_key); logMap.put("AMS_STEP", AMS_STEP);
				 * commonSql.insertHistory(logMap); }
				 */

				if(fhlmap.get("CONV_MULTI_SEND") != null && fhlmap.get("CONV_MULTI_SEND").toString().length() > 0){
					//KE 전송항공사
					fhlmap = createmsg.fhlwrite4(ams_out_reverse_key, fhlmap);
					String ranchar = commonUtil.randomchar();
					String kefileName = "KE_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
					HashMap kefhlMap = new HashMap();
					kefhlMap.put("FILENAME", kefileName);
					producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("FHL4"), kefhlMap);
					fhlmap.put("MSB_KE_SEND", "/MSB_KE");
				}
				if(fhlmap.get("AIR_PIMA_MSB_SEND") != null && fhlmap.get("AIR_PIMA_MSB_SEND").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//MSB 전송 항공사
						fhlmap = createmsg.fhlwrite2(ams_out_reverse_key, fhlmap);
						//20180516 중국 세관 용 fhl 버전 업 추가
						String ranchar = commonUtil.randomchar();
						if(fhlmap.get("OAL_VER4") != null && fhlmap.get("OAL_VER4").toString().length() > 0){

							fhlmap.put("CONV_MSG", fhlmap.get("OAL_VER4"));
							String oal4fileName = "OAL4_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap oal4fhlMap = new HashMap();
							oal4fhlMap.put("FILENAME", oal4fileName);
							producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oal4fhlMap);
							fhlmap.put("MSB_OAL_SEND4", "/OAL4");
						}else if(fhlmap.get("CONVERSION_MSG") != null && fhlmap.get("CONVERSION_MSG").toString().length() > 0){

							fhlmap.put("CONV_MSG", fhlmap.get("CONVERSION_MSG"));
							String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap oalfhlMap = new HashMap();
							oalfhlMap.put("FILENAME", oalfileName);
							producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oalfhlMap);
							fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("AIR_PIMA_MSB_SEND_VER") != null && fhlmap.get("AIR_PIMA_MSB_SEND_VER").toString().length() > 0){

					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//MSB 전송 항공사
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("CONV_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oal4fileName = "OAL4_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap oal4fhlMap = new HashMap();
						oal4fhlMap.put("FILENAME", oal4fileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oal4fhlMap);
						fhlmap.put("MSB_OAL_SEND4", "/OAL4");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("SITA_TYPEB_SEND") != null && fhlmap.get("SITA_TYPEB_SEND").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//TYPEB 전송 항공사
						fhlmap = createmsg.typebwrite(ams_out_reverse_key, fhlmap);
						String ranchar = commonUtil.randomchar();
						String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap typebMap = new HashMap();
						typebMap.put("TYPEBFILE", tyepbfileName);
						producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
						fhlmap.put("TYPEB_SEND", "/TYPEB");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("SITA_TYPEB_SEND_VER") != null && fhlmap.get("SITA_TYPEB_SEND_VER").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//TYPEB FHL/4 전송 항공사
						fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
						String ranchar = commonUtil.randomchar();
						String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
						HashMap typebMap = new HashMap();
						typebMap.put("TYPEBFILE", tyepbfileName);
						producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
						fhlmap.put("TYPEB_SEND_VER", "/TY_VER4");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("KTNET_SEND_FLAG") != null && fhlmap.get("KTNET_SEND_FLAG").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1 || fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){
					//수출 일 경우 KTNET 전송은 KTNETAMS로만 전송
						if(fhlmap.get("KTNET_SEND_FLAG").equals("KTNETAMS")){
							if(!Pattern.matches("[A-Z\\s0-9]+", CommonUtil.nullChk(fhlmap.get("H_COMMODITY") + CommonUtil.nullChk(fhlmap.get("TXT_COMODITY"))))){
								 if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
										fhlmap.put("ERRORMSG", "COMMODITY INPUT ERROR");
										fhlmap.put("ERRORMSG_KOR", "공백, 숫자, 영문 대문자만 가능");
									}
								 ams_status = false;
							 }else{
								//KTNET AMS 전송 항공사
								logger.info(" === KTNET AMS FHL CREATE === ");
								fhlmap = createmsg.sendKTNETFHL(ams_out_reverse_key, fhlmap);
								String ranchar = commonUtil.randomchar();
								String fileName = "KTNET_FHL_AMS_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
								HashMap ktamsMap = new HashMap();
								ktamsMap.put("KTAMSFILE", fileName);
								producer.sendBodyAndHeaders("direct:AMS2KTNETAMSqueuesend",fhlmap.get("KTNET_FHL"), ktamsMap);
								fhlmap.put("OUT_AMS_FLAG", "A");
								fhlmap.put("KTNET_SEND", "/KTNETAMS");
							 }
							 //해외 연계 포워더 용 OZ 건 KTNETIMP 전송
						}else if(fhlmap.get("KTNET_SEND_FLAG").equals("KTNETIMP") && (fhlmap.get("OCI_TYPE").equals("AMS") && fhlmap.get("OCI_FLAG").equals("T"))){
							//KTNET IMP 전송 항공사
							logger.info(" === KTNET IMP MANIFEST FHL CREATE === ");
							fhlmap = createmsg.fhl_kt_non_oci(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String fileName = "KTNET_FHL_IMP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap ktfhlMap = new HashMap();
							ktfhlMap.put("KTFHLFILE", fileName);
							producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);
							fhlmap.put("OUT_AMS_FLAG", "A");
							fhlmap.put("KAMS_KT_SEND", "/KTNET");
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}

				//PO, NH, KJ, 7C, OZ, KE, TW 일때Prefix 933이면 KZ 동보
				if((fhlmap.get("CARR_CODE").equals("PO")
				 || fhlmap.get("CARR_CODE").equals("NH")
				 || fhlmap.get("CARR_CODE").equals("KJ")
				 || fhlmap.get("CARR_CODE").equals("7C")
				 || fhlmap.get("CARR_CODE").equals("OZ")
				 || fhlmap.get("CARR_CODE").equals("KE")
				 || fhlmap.get("CARR_CODE").equals("TW"))
				 && fhlmap.get("MBI_PRE").equals("933")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//KZ 항공사 코드로 MSB 전송(fhlmap.get("DONB_BO_SEND") 값에 KZ 항공사 PIMA 적용
						//MSB 전송 항공사
						//fhlmap.put("AIR_PIMA_MSB_SEND", fhlmap.get("MULTI_SEND"));
						//fhlmap = createmsg.fhlwrite2(ams_out_reverse_key, fhlmap);
						//fhlmap.put("MULTI_MSG", fhlmap.get("CONVERSION_MSG"));
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", fhlmap.get("MULTI_SEND")); //23.03.06 FHL/4로 보내고 KZ 동보전송도 FHL/4로 변경
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap kzfhlMap = new HashMap();
						kzfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), kzfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === KZ DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}else if(fhlmap.get("CARR_CODE").equals("KJ") && !(fhlmap.get("MBI_PRE").equals("933"))){
					if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
						fhlmap.put("ERRORMSG",  fhlmap.get("TGT_PIMA") +" - AMS SERVICE IS NOT AVAILABLE FOR THIS AIRLINE");
						fhlmap.put("ERRORMSG_KOR", fhlmap.get("TGT_PIMA") +" - 서비스 하지 않는 항공사");
					}
					ams_status = false;
				}
				//2023.6.26 여찬양 - TP, UX, WY 동보전송 요청드립니다(메일 참조)
				//QR,EK,OZ 일때 Prefix 047이면 TP 동보
				//multi_send 사용 중인 항공사가 있어 하드코딩
				if((fhlmap.get("CARR_CODE").equals("QR")
				 || fhlmap.get("CARR_CODE").equals("EK")
				 || fhlmap.get("CARR_CODE").equals("OZ"))
				 && fhlmap.get("MBI_PRE").equals("047")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08TAP");//multi_send 사용중
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap tpfhlMap = new HashMap();
						tpfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), tpfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === TP DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//Prefix 910이면 WY 동보
				if((fhlmap.get("CARR_CODE").equals("KE")
				 || fhlmap.get("CARR_CODE").equals("OZ")
				 || fhlmap.get("CARR_CODE").equals("5J")
				 || fhlmap.get("CARR_CODE").equals("TW")
				 || fhlmap.get("CARR_CODE").equals("TG")
				 || fhlmap.get("CARR_CODE").equals("LJ"))
				 && fhlmap.get("MBI_PRE").equals("910")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08OAS");//multi_send 사용중
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap wyfhlMap = new HashMap();
						wyfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), wyfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === WY DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//QR,EK 일때 Prefix 996이면 UX 동보
				if((fhlmap.get("CARR_CODE").equals("QR")
				 || fhlmap.get("CARR_CODE").equals("EK"))
				 && fhlmap.get("MBI_PRE").equals("996")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08AEA");
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap uxfhlMap = new HashMap();
						uxfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), uxfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === UX DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//Prefix 936이면 D0, AACT(TYPEB) 동보
				if(fhlmap.get("MBI_PRE").equals("936")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){
						// D0 동보
						if (!fhlmap.get("CARR_CODE").equals("D0")) {
							if(CommonUtil.nullChk(fhlmap.get("AIR_PIMA_MSB_SEND")).equals("TDVAIR08DHV")
							|| CommonUtil.nullChk(fhlmap.get("AIR_PIMA_MSB_SEND_VER")).equals("TDVAIR08DHV")){
								// D0와 동일한 PIMA로 변환하여 MSB 전송하는 경우, 동보 제외
							} else {
								fhlmap.put("AIR_PIMA_MSB_SEND_VER", "TDVAIR08DHV"); // D0 PIMA
								fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
								fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
								String ranchar = commonUtil.randomchar();
								String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
								HashMap d0fhlMap = new HashMap();
								d0fhlMap.put("FILENAME", oalfileName);
								producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), d0fhlMap);
								fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
								logger.info(" === D0 DONGBO MSB SEND === ");
							}
						}

						// AACT TYPEB 동보
						if(CommonUtil.nullChk(fhlmap.get("SITA_TYPEB_SEND")).equals("ICNACXH")
						|| CommonUtil.nullChk(fhlmap.get("SITA_TYPEB_SEND_VER")).equals("ICNACXH")){
							// AACT TYPEB 라우팅 정보가 존재하는 경우, 동보 제외
						} else {
							fhlmap.put("SITA_TYPEB_SEND_VER", "ICNACXH");
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND_VER", "/TY_VER4");
							logger.info(" === AACT DONGBO TYPEB SEND === ");
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
						ams_status = false;
					}
				}

			//KTNET 취합
			}else if(fhlmap.get("MANIFEST_COMPANY").equals("KTNET")){

				if(fhlmap.get("CARR_CODE").equals("ZE") && fhlmap.get("MBI_PRE").equals("077")){

					fhlmap.put("SITA_TYPEB_SEND", "");
					fhlmap.put("SITA_TYPEB_SEND_VER", zejpn_addr);

				}

				if(fhlmap.get("CONV_MULTI_SEND") != null && fhlmap.get("CONV_MULTI_SEND").toString().length() > 0){
					//KE 전송항공사
					fhlmap = createmsg.fhlwrite4(ams_out_reverse_key, fhlmap);
					String ranchar = commonUtil.randomchar();
					String kefileName = "KE_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
					HashMap kefhlMap = new HashMap();
					kefhlMap.put("FILENAME", kefileName);
					producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("FHL4"), kefhlMap);
					fhlmap.put("MSB_KE_SEND", "/MSB_KE");
				}
				if(fhlmap.get("AIR_PIMA_MSB_SEND_VER") != null && fhlmap.get("AIR_PIMA_MSB_SEND_VER").toString().length() > 0){

					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//MSB 전송 항공사
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("CONV_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oal4fileName = "OAL4_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap oal4fhlMap = new HashMap();
						oal4fhlMap.put("FILENAME", oal4fileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oal4fhlMap);
						fhlmap.put("MSB_OAL_SEND4", "/OAL4");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("AIR_PIMA_MSB_SEND") != null && fhlmap.get("AIR_PIMA_MSB_SEND").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//MSB 전송 항공사
						fhlmap = createmsg.fhlwrite2(ams_out_reverse_key, fhlmap);
						//20180516 중국 세관 용 fhl 버전 업 추가
						String ranchar = commonUtil.randomchar();
						if(fhlmap.get("OAL_VER4") != null && fhlmap.get("OAL_VER4").toString().length() > 0){

							fhlmap.put("CONV_MSG", fhlmap.get("OAL_VER4"));
							String oal4fileName = "OAL4_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap oal4fhlMap = new HashMap();
							oal4fhlMap.put("FILENAME", oal4fileName);
							producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oal4fhlMap);
							fhlmap.put("MSB_OAL_SEND4", "/OAL4");
						}else if(fhlmap.get("CONVERSION_MSG") != null && fhlmap.get("CONVERSION_MSG").toString().length() > 0){

							fhlmap.put("CONV_MSG", fhlmap.get("CONVERSION_MSG"));
							String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap oalfhlMap = new HashMap();
							oalfhlMap.put("FILENAME", oalfileName);
							producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oalfhlMap);
							fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						}
						//BR 일경우 OZ KTNETAMS 전송
						if(fhlmap.get("CARR_CODE").equals("BR") && fhlmap.get("OCI_TYPE").equals("EXP")){
							 if(!Pattern.matches("[A-Z\\s0-9]+", CommonUtil.nullChk(fhlmap.get("H_COMMODITY") + CommonUtil.nullChk(fhlmap.get("TXT_COMODITY"))))){
								 if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
										fhlmap.put("ERRORMSG", "COMMODITY INPUT ERROR");
										fhlmap.put("ERRORMSG_KOR", "공백, 숫자, 영문 대문자만 가능");
									}
								 ams_status = false;
							 }else{
								//KTNET AMS 전송 항공사
								 	logger.info(" === KTNET AMS FHL CREATE === ");
									fhlmap = createmsg.sendKTNETFHL(ams_out_reverse_key, fhlmap);
									logger.info(" === BR -> OZ DONGBO KTNET SEND === ");
									String fileName = "KTNET_FHL_AMS_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
									HashMap ktamsMap = new HashMap();
									ktamsMap.put("KTAMSFILE", fileName);
									//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_FHL").toString());
									producer.sendBodyAndHeaders("direct:AMS2KTNETAMSqueuesend",fhlmap.get("KTNET_FHL"), ktamsMap);
									fhlmap.put("OUT_AMS_FLAG", "A");
									fhlmap.put("KTNET_SEND", "/KTNETAMS");
							 }
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("SITA_TYPEB_SEND") != null && fhlmap.get("SITA_TYPEB_SEND").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
							//TYPEB 전송 항공사
							fhlmap = createmsg.typebwrite(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TYPEB");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("SITA_TYPEB_SEND_VER") != null && fhlmap.get("SITA_TYPEB_SEND_VER").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//TYPEB FHL/4 전송 항공사
						fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
						String ranchar = commonUtil.randomchar();
						String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
						HashMap typebMap = new HashMap();
						typebMap.put("TYPEBFILE", tyepbfileName);
						producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
						fhlmap.put("TYPEB_SEND_VER", "/TY_VER4");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("KTNET_SEND_FLAG") != null && fhlmap.get("KTNET_SEND_FLAG").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1 || fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){
						if(fhlmap.get("KTNET_SEND_FLAG").equals("KTNETAMS")){
							if(!Pattern.matches("[A-Z\\s0-9]+", CommonUtil.nullChk(fhlmap.get("H_COMMODITY") + CommonUtil.nullChk(fhlmap.get("TXT_COMODITY"))))){
								 if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
										fhlmap.put("ERRORMSG", "COMMODITY INPUT ERROR");
										fhlmap.put("ERRORMSG_KOR", "공백, 숫자, 영문 대문자만 가능");
									}
								 ams_status = false;
							 }else{
								//KTNET AMS 전송 항공사
								logger.info(" === KTNET AMS FHL CREATE === ");
								fhlmap = createmsg.sendKTNETFHL(ams_out_reverse_key, fhlmap);
								String ranchar = commonUtil.randomchar();
								String fileName = "KTNET_FHL_AMS_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
								HashMap ktamsMap = new HashMap();
								ktamsMap.put("KTAMSFILE", fileName);
								//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_FHL").toString());
								producer.sendBodyAndHeaders("direct:AMS2KTNETAMSqueuesend",fhlmap.get("KTNET_FHL"), ktamsMap);
								fhlmap.put("OUT_AMS_FLAG", "A");
								fhlmap.put("KTNET_SEND", "/KTNETAMS");
							 }
							//해외 연계 포워더 용 OZ 건 KTNETIMP 전송
						}else if(fhlmap.get("KTNET_SEND_FLAG").equals("KTNETIMP") && (fhlmap.get("OCI_TYPE").equals("AMS") && fhlmap.get("OCI_FLAG").equals("T"))){
							//KTNET IMP 전송 항공사
							logger.info(" === KTNET IMP MANIFEST FHL CREATE === ");
							fhlmap = createmsg.fhl_kt_non_oci(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String fileName = "KTNET_FHL_IMP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap ktfhlMap = new HashMap();
							ktfhlMap.put("KTFHLFILE", fileName);
							producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);
							fhlmap.put("OUT_AMS_FLAG", "A");
							fhlmap.put("KAMS_KT_SEND", "/KTNET");
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//PO, NH, KJ, 7C, OZ, KE, TW 일때Prefix 933이면 KZ 동보
				if((fhlmap.get("CARR_CODE").equals("PO")
				 || fhlmap.get("CARR_CODE").equals("NH")
				 || fhlmap.get("CARR_CODE").equals("KJ")
				 || fhlmap.get("CARR_CODE").equals("7C")
				 || fhlmap.get("CARR_CODE").equals("OZ")
				 || fhlmap.get("CARR_CODE").equals("KE")
				 || fhlmap.get("CARR_CODE").equals("TW"))
				 && fhlmap.get("MBI_PRE").equals("933")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//KZ 항공사 코드로 MSB 전송(fhlmap.get("DONB_BO_SEND") 값에 KZ 항공사 PIMA 적용
						//MSB 전송 항공사
						//fhlmap.put("AIR_PIMA_MSB_SEND", fhlmap.get("MULTI_SEND"));
						//fhlmap = createmsg.fhlwrite2(ams_out_reverse_key, fhlmap);
						//fhlmap.put("MULTI_MSG", fhlmap.get("CONVERSION_MSG"));
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", fhlmap.get("MULTI_SEND")); //23.03.06 FHL/4로 보내고 KZ 동보전송도 FHL/4로 변경
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap kzfhlMap = new HashMap();
						kzfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), kzfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === KZ DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//QR,EK,OZ 일때Prefix 047이면 TP 동보
				if((fhlmap.get("CARR_CODE").equals("QR")
				|| fhlmap.get("CARR_CODE").equals("EK")
				|| fhlmap.get("CARR_CODE").equals("OZ"))
				&& fhlmap.get("MBI_PRE").equals("047")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08TAP");//multi_send 사용중
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap tpfhlMap = new HashMap();
						tpfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), tpfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === TP DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
							ams_status = false;
					}
				}
				//QR,EK 일때 Prefix 996이면 UX 동보
				if((fhlmap.get("CARR_CODE").equals("QR")
				 || fhlmap.get("CARR_CODE").equals("EK"))
				 && fhlmap.get("MBI_PRE").equals("996")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08AEA");
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap uxfhlMap = new HashMap();
						uxfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), uxfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === UX DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//Prefix 910이면 WY 동보
				if((fhlmap.get("CARR_CODE").equals("KE")
				 || fhlmap.get("CARR_CODE").equals("OZ")
				 || fhlmap.get("CARR_CODE").equals("5J")
				 || fhlmap.get("CARR_CODE").equals("TW")
				 || fhlmap.get("CARR_CODE").equals("TG")
				 || fhlmap.get("CARR_CODE").equals("LJ"))
				 && fhlmap.get("MBI_PRE").equals("910")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08OAS");//multi_send 사용중
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap wyfhlMap = new HashMap();
						wyfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), wyfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === WY DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//SQ 이면서 Prefix 020 일때 LH 동보
				if(fhlmap.get("CARR_CODE").equals("SQ") && fhlmap.get("MBI_PRE").equals("020")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//MSB 전송 항공사
						//fhlmap.put("AIR_PIMA_MSB_SEND", fhlmap.get("MULTI_SEND"));
						//fhlmap = createmsg.fhlwrite2(ams_out_reverse_key, fhlmap);
						//fhlmap.put("MULTI_MSG", fhlmap.get("CONVERSION_MSG"));
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", fhlmap.get("MULTI_SEND")); //23.03.06테스트 중 SQ FHL/4로 보내고 LH 동보전송도 FHL/4로 변경
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap lhlfhlMap = new HashMap();
						lhlfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), lhlfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === SQ/LH DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//Prefix 936이면 D0, AACT(TYPEB) 동보
				if(fhlmap.get("MBI_PRE").equals("936")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){
						// D0 동보
						if (!fhlmap.get("CARR_CODE").equals("D0")) {
							if(CommonUtil.nullChk(fhlmap.get("AIR_PIMA_MSB_SEND")).equals("TDVAIR08DHV")
							|| CommonUtil.nullChk(fhlmap.get("AIR_PIMA_MSB_SEND_VER")).equals("TDVAIR08DHV")){
								// D0와 동일한 PIMA로 변환하여 MSB 전송하는 경우, 동보 제외
							} else {
								fhlmap.put("AIR_PIMA_MSB_SEND_VER", "TDVAIR08DHV"); // D0 PIMA
								fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
								fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
								String ranchar = commonUtil.randomchar();
								String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
								HashMap d0fhlMap = new HashMap();
								d0fhlMap.put("FILENAME", oalfileName);
								producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), d0fhlMap);
								fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
								logger.info(" === D0 DONGBO MSB SEND === ");
							}
						}

						// AACT TYPEB 동보
						if(CommonUtil.nullChk(fhlmap.get("SITA_TYPEB_SEND")).equals("ICNACXH")
						|| CommonUtil.nullChk(fhlmap.get("SITA_TYPEB_SEND_VER")).equals("ICNACXH")){
							// AACT TYPEB 라우팅 정보가 존재하는 경우, 동보 제외
						} else {
							fhlmap.put("SITA_TYPEB_SEND_VER", "ICNACXH");
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND_VER", "/TY_VER4");
							logger.info(" === AACT DONGBO TYPEB SEND === ");
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
						ams_status = false;
					}
				}

				//TW Airline
				//ORG = ICN
				//PRE-FIX = 722/829
				//DST = BKK, PNH, RGN, DAC, MLE, BOM, VTE, REP Type-b 전송
				//PRE-FIX = 722
				//DST = SGN, HAN, DAD, KIX, TAO Type-b 전송
				//PRE-FIX = 722/933
				//NRT, JFK, ORD, LAX Type-b 전송
				//PRE-FIX = 722
				//VTE Type-b 전송
				/*if(fhlmap.get("CARR_CODE").equals("TW") && fhlmap.get("M_ORG").equals("ICN")){

					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내

						if((fhlmap.get("MBI_PRE").equals("722") || fhlmap.get("MBI_PRE").equals("829"))
							&& fhlmap.get("M_DST").equals("BKK")){

							fhlmap.put("SITA_TYPEB_SEND_VER", bkk_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("829") && (fhlmap.get("M_DST").equals("PNH")
								|| fhlmap.get("M_DST").equals("RGN") || fhlmap.get("M_DST").equals("DAC")
								|| fhlmap.get("M_DST").equals("MLE") || fhlmap.get("M_DST").equals("BOM")
								|| fhlmap.get("M_DST").equals("VTE") || fhlmap.get("M_DST").equals("REP"))){

							fhlmap.put("SITA_TYPEB_SEND_VER", pnh_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("722") && fhlmap.get("M_DST").equals("SGN")){

							fhlmap.put("SITA_TYPEB_SEND_VER", sgn_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("722") && (fhlmap.get("M_DST").equals("HAN") || fhlmap.get("M_DST").equals("DAD"))){

							fhlmap.put("SITA_TYPEB_SEND_VER", handad_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if((fhlmap.get("MBI_PRE").equals("722") || fhlmap.get("MBI_PRE").equals("933"))
								&&  fhlmap.get("M_DST").equals("NRT")){

							fhlmap.put("SITA_TYPEB_SEND_VER", nrt_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("933") 	&&  (fhlmap.get("M_DST").equals("JFK")
								|| fhlmap.get("M_DST").equals("ORD") || fhlmap.get("M_DST").equals("LAX"))){

								fhlmap.put("SITA_TYPEB_SEND_VER", jfk_addr);
								fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
								String ranchar = commonUtil.randomchar();
								String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
								HashMap typebMap = new HashMap();
								typebMap.put("TYPEBFILE", tyepbfileName);
								producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
								fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("722") && fhlmap.get("M_DST").equals("KIX")){

							fhlmap.put("SITA_TYPEB_SEND_VER", kix_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("722") && fhlmap.get("M_DST").equals("TAO")){

							fhlmap.put("SITA_TYPEB_SEND_VER", tao_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");
							//20180402 VTE 추가
						}else if(fhlmap.get("MBI_PRE").equals("722") && fhlmap.get("M_DST").equals("VTE")){

							fhlmap.put("SITA_TYPEB_SEND_VER", vte_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else{
							if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
								fhlmap.put("ERRORMSG",  fhlmap.get("TGT_PIMA") +" - AMS SERVICE IS NOT AVAILABLE FOR THIS DST");
								fhlmap.put("ERRORMSG_KOR", fhlmap.get("TGT_PIMA") +" - 서비스 하지 않는 지역");
							}
						 ams_status = false;
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}*/

			}else{

				if(fhlmap.get("CARR_CODE").equals("ZE") && fhlmap.get("MBI_PRE").equals("077")){

					fhlmap.put("SITA_TYPEB_SEND", "");
					fhlmap.put("SITA_TYPEB_SEND_VER", zejpn_addr);

				}

				if(fhlmap.get("CONV_MULTI_SEND") != null && fhlmap.get("CONV_MULTI_SEND").toString().length() > 0){
					//KE 전송항공사
					fhlmap = createmsg.fhlwrite4(ams_out_reverse_key, fhlmap);
					String ranchar = commonUtil.randomchar();
					String kefileName = "KE_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
					HashMap kefhlMap = new HashMap();
					kefhlMap.put("FILENAME", kefileName);
					producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("FHL4"), kefhlMap);
					fhlmap.put("MSB_KE_SEND", "/MSB_KE");
				}
				if(fhlmap.get("AIR_PIMA_MSB_SEND_VER") != null && fhlmap.get("AIR_PIMA_MSB_SEND_VER").toString().length() > 0){

					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//MSB 전송 항공사
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("CONV_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oal4fileName = "OAL4_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap oal4fhlMap = new HashMap();
						oal4fhlMap.put("FILENAME", oal4fileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oal4fhlMap);
						fhlmap.put("MSB_OAL_SEND4", "/OAL4");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("AIR_PIMA_MSB_SEND") != null && fhlmap.get("AIR_PIMA_MSB_SEND").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//MSB 전송 항공사
						fhlmap = createmsg.fhlwrite2(ams_out_reverse_key, fhlmap);
						//20180516 중국 세관 용 fhl 버전 업 추가
						String ranchar = commonUtil.randomchar();
						if(fhlmap.get("OAL_VER4") != null && fhlmap.get("OAL_VER4").toString().length() > 0){

							fhlmap.put("CONV_MSG", fhlmap.get("OAL_VER4"));
							String oal4fileName = "OAL4_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap oal4fhlMap = new HashMap();
							oal4fhlMap.put("FILENAME", oal4fileName);
							producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oal4fhlMap);
							fhlmap.put("MSB_OAL_SEND4", "/OAL4");
						}else if(fhlmap.get("CONVERSION_MSG") != null && fhlmap.get("CONVERSION_MSG").toString().length() > 0){

							fhlmap.put("CONV_MSG", fhlmap.get("CONVERSION_MSG"));
							String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap oalfhlMap = new HashMap();
							oalfhlMap.put("FILENAME", oalfileName);
							producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("CONV_MSG"), oalfhlMap);
							fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("SITA_TYPEB_SEND") != null && fhlmap.get("SITA_TYPEB_SEND").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//TYPEB 전송 항공사
						fhlmap = createmsg.typebwrite(ams_out_reverse_key, fhlmap);
						String ranchar = commonUtil.randomchar();
						String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap typebMap = new HashMap();
						typebMap.put("TYPEBFILE", tyepbfileName);
						producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
						fhlmap.put("TYPEB_SEND", "/TYPEB");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("SITA_TYPEB_SEND_VER") != null && fhlmap.get("SITA_TYPEB_SEND_VER").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//TYPEB FHL/4 전송 항공사
						fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
						String ranchar = commonUtil.randomchar();
						String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
						HashMap typebMap = new HashMap();
						typebMap.put("TYPEBFILE", tyepbfileName);
						producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
						fhlmap.put("TYPEB_SEND_VER", "/TY_VER4");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				if(fhlmap.get("KTNET_SEND_FLAG") != null && fhlmap.get("KTNET_SEND_FLAG").toString().length() > 0){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1 || fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){
					//수출 일 경우 KTNET 전송은 KTNETAMS로만 전송
						if(fhlmap.get("KTNET_SEND_FLAG").equals("KTNETAMS")){
							if(!Pattern.matches("[A-Z\\s0-9]+", CommonUtil.nullChk(fhlmap.get("H_COMMODITY") + CommonUtil.nullChk(fhlmap.get("TXT_COMODITY"))))){
								 if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
										fhlmap.put("ERRORMSG", "COMMODITY INPUT ERROR");
										fhlmap.put("ERRORMSG_KOR", "공백, 숫자, 영문 대문자만 가능");
									}
								 ams_status = false;
							 }else{
								//KTNET AMS 전송 항공사
								logger.info(" === KTNET AMS FHL CREATE === ");
								fhlmap = createmsg.sendKTNETFHL(ams_out_reverse_key, fhlmap);
								String ranchar = commonUtil.randomchar();
								String fileName = "KTNET_FHL_AMS_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
								HashMap ktamsMap = new HashMap();
								ktamsMap.put("KTAMSFILE", fileName);
								//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_FHL").toString());
								producer.sendBodyAndHeaders("direct:AMS2KTNETAMSqueuesend",fhlmap.get("KTNET_FHL"), ktamsMap);
								fhlmap.put("OUT_AMS_FLAG", "A");
								fhlmap.put("KTNET_SEND", "/KTNETAMS");
							 }
						//해외 연계 포워더 용 OZ 건 KTNETIMP 전송
						}else if(fhlmap.get("KTNET_SEND_FLAG").equals("KTNETIMP") && (fhlmap.get("OCI_TYPE").equals("AMS") && fhlmap.get("OCI_FLAG").equals("T"))){
							//KTNET IMP 전송 항공사
							logger.info(" === KTNET IMP MANIFEST FHL CREATE === ");
							fhlmap = createmsg.fhl_kt_non_oci(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String fileName = "KTNET_FHL_IMP_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
							HashMap ktfhlMap = new HashMap();
							ktfhlMap.put("KTFHLFILE", fileName);
							//commonUtil.savefile(ktnet_write_path+fileName, fhlmap.get("KTNET_MANIFEST_FHL").toString());
							producer.sendBodyAndHeaders("direct:AMS2KTNETFHLqueuesend",fhlmap.get("KTNET_MANIFEST_FHL"), ktfhlMap);
							fhlmap.put("OUT_AMS_FLAG", "A");
							fhlmap.put("KAMS_KT_SEND", "/KTNET");
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}

				//PO, NH, KJ, 7C, OZ, KE, TW 일때Prefix 933이면 KZ 동보
				if((fhlmap.get("CARR_CODE").equals("PO")
				 || fhlmap.get("CARR_CODE").equals("NH")
				 || fhlmap.get("CARR_CODE").equals("KJ")
				 || fhlmap.get("CARR_CODE").equals("7C")
				 || fhlmap.get("CARR_CODE").equals("OZ")
				 || fhlmap.get("CARR_CODE").equals("KE")
				 || fhlmap.get("CARR_CODE").equals("TW"))
				 && fhlmap.get("MBI_PRE").equals("933")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//KZ 항공사 코드로 MSB 전송(fhlmap.get("DONB_BO_SEND") 값에 KZ 항공사 PIMA 적용
						//MSB 전송 항공사
						//fhlmap.put("AIR_PIMA_MSB_SEND", fhlmap.get("MULTI_SEND"));
						//fhlmap = createmsg.fhlwrite2(ams_out_reverse_key, fhlmap);
						//fhlmap.put("MULTI_MSG", fhlmap.get("CONVERSION_MSG"));
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", fhlmap.get("MULTI_SEND")); //23.03.06 FHL/4로 보내고 KZ 동보전송도 FHL/4로 변경
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap kzfhlMap = new HashMap();
						kzfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), kzfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === KZ DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//QR,EK,OZ 일때Prefix 047이면 TP 동보
				if((fhlmap.get("CARR_CODE").equals("QR")
				|| fhlmap.get("CARR_CODE").equals("EK")
				|| fhlmap.get("CARR_CODE").equals("OZ"))
				&& fhlmap.get("MBI_PRE").equals("047")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08TAP");//multi_send 사용중
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap tpfhlMap = new HashMap();
						tpfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), tpfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === TP DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
							ams_status = false;
					}
				}
				//QR,EK 일때 Prefix 996이면 UX 동보
				if((fhlmap.get("CARR_CODE").equals("QR")
				 || fhlmap.get("CARR_CODE").equals("EK"))
				 && fhlmap.get("MBI_PRE").equals("996")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08AEA");
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap uxfhlMap = new HashMap();
						uxfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), uxfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === UX DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//Prefix 910이면 WY 동보
				if((fhlmap.get("CARR_CODE").equals("KE")
				 || fhlmap.get("CARR_CODE").equals("OZ")
				 || fhlmap.get("CARR_CODE").equals("5J")
				 || fhlmap.get("CARR_CODE").equals("TW")
				 || fhlmap.get("CARR_CODE").equals("TG")
				 || fhlmap.get("CARR_CODE").equals("LJ"))
				 && fhlmap.get("MBI_PRE").equals("910")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", "REUAIR08OAS");//multi_send 사용중
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap wyfhlMap = new HashMap();
						wyfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), wyfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === WY DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//SQ 이면서 Prefix 020 일때 LH 동보
				if(fhlmap.get("CARR_CODE").equals("SQ") && fhlmap.get("MBI_PRE").equals("020")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내
						//MSB 전송 항공사
						//fhlmap.put("AIR_PIMA_MSB_SEND", fhlmap.get("MULTI_SEND"));
						//fhlmap = createmsg.fhlwrite2(ams_out_reverse_key, fhlmap);
						//fhlmap.put("MULTI_MSG", fhlmap.get("CONVERSION_MSG"));
						fhlmap.put("AIR_PIMA_MSB_SEND_VER", fhlmap.get("MULTI_SEND")); //23.03.06 SQ FHL/4로 보내고 LH 동보전송도 FHL/4로 변경
						fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
						fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));//여기까지 테스트
						String ranchar = commonUtil.randomchar();
						String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
						HashMap lhfhlMap = new HashMap();
						lhfhlMap.put("FILENAME", oalfileName);
						producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), lhfhlMap);
						fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
						logger.info(" === SQ/LH DONGBO MSB SEND === ");
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG","PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}
				//Prefix 936이면 D0, AACT(TYPEB) 동보
				if(fhlmap.get("MBI_PRE").equals("936")){
					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){
						// D0 동보
						if (!fhlmap.get("CARR_CODE").equals("D0")) {
							if(CommonUtil.nullChk(fhlmap.get("AIR_PIMA_MSB_SEND")).equals("TDVAIR08DHV")
							|| CommonUtil.nullChk(fhlmap.get("AIR_PIMA_MSB_SEND_VER")).equals("TDVAIR08DHV")){
								// D0와 동일한 PIMA로 변환하여 MSB 전송하는 경우, 동보 제외
							} else {
								fhlmap.put("AIR_PIMA_MSB_SEND_VER", "TDVAIR08DHV"); // D0 PIMA
								fhlmap = createmsg.fhlwriteoal4(ams_out_reverse_key, fhlmap);
								fhlmap.put("MULTI_MSG", fhlmap.get("OAL_VER4"));
								String ranchar = commonUtil.randomchar();
								String oalfileName = "OAL_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+".txt";
								HashMap d0fhlMap = new HashMap();
								d0fhlMap.put("FILENAME", oalfileName);
								producer.sendBodyAndHeaders("direct:AMS2MSBqueuesend",fhlmap.get("MULTI_MSG"), d0fhlMap);
								fhlmap.put("MSB_OAL_SEND", "/MSB_OAL");
								logger.info(" === D0 DONGBO MSB SEND === ");
							}
						}

						// AACT TYPEB 동보
						if(CommonUtil.nullChk(fhlmap.get("SITA_TYPEB_SEND")).equals("ICNACXH")
						|| CommonUtil.nullChk(fhlmap.get("SITA_TYPEB_SEND_VER")).equals("ICNACXH")){
							// AACT TYPEB 라우팅 정보가 존재하는 경우, 동보 제외
						} else {
							fhlmap.put("SITA_TYPEB_SEND_VER", "ICNACXH");
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND_VER", "/TY_VER4");
							logger.info(" === AACT DONGBO TYPEB SEND === ");
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
						ams_status = false;
					}
				}

				//TW Airline
				//ORG = ICN
				//PRE-FIX = 722/829
				//DST = BKK, PNH, RGN, DAC, MLE, BOM, VTE, REP Type-b 전송
				//PRE-FIX = 722
				//DST = SGN, HAN, DAD, KIX, TAO Type-b 전송
				//PRE-FIX = 722/933
				//NRT, JFK, ORD, LAX Type-b 전송
				//PRE-FIX = 722
				//VTE Type-b 전송
				/*if(fhlmap.get("CARR_CODE").equals("TW") && fhlmap.get("M_ORG").equals("ICN")){

					if(fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT82") !=-1
					|| fhlmap.get("SRC_PIMA").toString().indexOf("RKRAGT85CYBERLOGITEC") !=-1){ // 20240911 싸이버로지텍 연계 관련 MSB 전송 Routing 허용, 싸이버로지텍에 Non-Fwdr PIMA로 FHL 전송 시 FNA 발생 Case 안내

						if((fhlmap.get("MBI_PRE").equals("722") || fhlmap.get("MBI_PRE").equals("829"))
								&& fhlmap.get("M_DST").equals("BKK")){

								fhlmap.put("SITA_TYPEB_SEND_VER", bkk_addr);
								fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
								String ranchar = commonUtil.randomchar();
								String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
								HashMap typebMap = new HashMap();
								typebMap.put("TYPEBFILE", tyepbfileName);
								producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
								fhlmap.put("TYPEB_SEND", "/TY_VER4");

							}else if(fhlmap.get("MBI_PRE").equals("829") && (fhlmap.get("M_DST").equals("PNH")
									|| fhlmap.get("M_DST").equals("RGN") || fhlmap.get("M_DST").equals("DAC")
									|| fhlmap.get("M_DST").equals("MLE") || fhlmap.get("M_DST").equals("BOM")
									|| fhlmap.get("M_DST").equals("VTE") || fhlmap.get("M_DST").equals("REP"))){

								fhlmap.put("SITA_TYPEB_SEND_VER", pnh_addr);
								fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
								String ranchar = commonUtil.randomchar();
								String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
								HashMap typebMap = new HashMap();
								typebMap.put("TYPEBFILE", tyepbfileName);
								producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
								fhlmap.put("TYPEB_SEND", "/TY_VER4");

							}else if(fhlmap.get("MBI_PRE").equals("722") && fhlmap.get("M_DST").equals("SGN")){

							fhlmap.put("SITA_TYPEB_SEND_VER", sgn_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("722") && (fhlmap.get("M_DST").equals("HAN") || fhlmap.get("M_DST").equals("DAD"))){

							fhlmap.put("SITA_TYPEB_SEND_VER", handad_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if((fhlmap.get("MBI_PRE").equals("722") || fhlmap.get("MBI_PRE").equals("933"))
								&&  fhlmap.get("M_DST").equals("NRT")){

							fhlmap.put("SITA_TYPEB_SEND_VER", nrt_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("933") &&  (fhlmap.get("M_DST").equals("JFK")
								|| fhlmap.get("M_DST").equals("ORD") || fhlmap.get("M_DST").equals("LAX"))){

								fhlmap.put("SITA_TYPEB_SEND_VER", jfk_addr);
								fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
								String ranchar = commonUtil.randomchar();
								String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
								HashMap typebMap = new HashMap();
								typebMap.put("TYPEBFILE", tyepbfileName);
								producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
								fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("722") && fhlmap.get("M_DST").equals("KIX")){

							fhlmap.put("SITA_TYPEB_SEND_VER", kix_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else if(fhlmap.get("MBI_PRE").equals("722") && fhlmap.get("M_DST").equals("TAO")){

							fhlmap.put("SITA_TYPEB_SEND_VER", tao_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");
							//20180402 VTE 추가
						}else if(fhlmap.get("MBI_PRE").equals("722") && fhlmap.get("M_DST").equals("VTE")){

							fhlmap.put("SITA_TYPEB_SEND_VER", vte_addr);
							fhlmap = createmsg.typebwrite4(ams_out_reverse_key, fhlmap);
							String ranchar = commonUtil.randomchar();
							String tyepbfileName = "TYPEB_"+fhlmap.get("MAWB_NO")+"_"+fhlmap.get("HAWB")+"_"+ranchar+"_VER4"+".txt";
							HashMap typebMap = new HashMap();
							typebMap.put("TYPEBFILE", tyepbfileName);
							producer.sendBodyAndHeaders("direct:AMS2TYPEBqueuesend",fhlmap.get("TYPEB"), typebMap);
							fhlmap.put("TYPEB_SEND", "/TY_VER4");

						}else{
							if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
								fhlmap.put("ERRORMSG",  fhlmap.get("TGT_PIMA") +" - AMS SERVICE IS NOT AVAILABLE FOR THIS DST");
								fhlmap.put("ERRORMSG_KOR", fhlmap.get("TGT_PIMA") +" - 서비스 하지 않는 지역");
							}
						 ams_status = false;
						}
					}else{
						if(CommonUtil.nullChk(fhlmap.get("ERRORMSG"), "").equals("")){
							fhlmap.put("ERRORMSG", "PIMA IS NOTAVAILABLE FOR THIS AMS SERVICE");
							fhlmap.put("ERRORMSG_KOR", fhlmap.get("SRC_PIMA") +" - PIMA는 AMS 피마가 아닙니다.");
						}
					 ams_status = false;
					}
				}*/
			}

		}catch(IndexOutOfBoundsException e){
			logger.info(" === AMS ROUTE Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			ams_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Route Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === AMS ROUTE Check NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			ams_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Route Check NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === AMS ROUTE Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			ams_status = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Route Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === AMS ROUTE Check Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","AMS Route Check Exception",e, msg);
			ams_status = false;
		}finally{

			if(!ams_status){

				fhlmap.put("AMS_SMI", "FNA");
				fnaMsg = createack.FNAack(fhlmap);
				fhlmap.put("AMS_ACK", fnaMsg); //AMS ACK로 변경

				HashMap logMap = new HashMap();
				//logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
				logMap.put("HISTORY_STATUS", "AMS_Route_Check Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", "709");

				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
				fhlmap.put("FLAG_STATUS", ams_status);
				logger.info(" === AMS ROUTE CHECK ERROR === ");

			}else{

				fhlmap.put("AMS_SMI", "FMA");
				fmaMsg = createack.FMAack(fhlmap);
				fhlmap.put("AMS_ACK", fmaMsg);

				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "AMS_Route_Check");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", AMS_STEP);

				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
				fhlmap.put("FLAG_STATUS", ams_status);
				logger.info(" === AMS ROUTE CHECK SUCCESSS === ");

			}
		}
		return fhlmap;
	}
}
