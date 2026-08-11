package com.trx.validate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trx.db.DbinsertTrace;
import com.trx.Msgprocess.CreateAck;
import com.trx.Msgprocess.CreateMSG;
import com.trx.evalleyvs.eai.cargo.util.ValidationUtil;
import com.trx.evalleyvs.eai.exception.NoSuchPatternException;
import com.trx.evalleyvs.eai.exception.SystemException;
import com.trx.evalleyvs.eai.exception.ValidationException;
import com.trx.evalleyvs.eai.message.MessageXMLResourceManager;
import com.trx.util.CommonUtil;
import com.trx.util.CommonSql;
import com.trx.util.MainExceptionManager;
import com.trx.validate.CargoImpCheck;
import com.trx.validate.ValidationBiz;
import com.ibatis.sqlmap.client.SqlMapClient;

public class CargoImpCheck {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(CargoImpCheck.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	SqlMapClient sqlMapClient_msb;

	@Autowired
	CommonSql commonSql;

	@Autowired
	ValidationBiz bizcheck;

	@Autowired
	ValidationDoc validation;

	@Autowired
	MainExceptionManager mainExceptionManager;

	@Autowired
	CreateAck createack = new CreateAck();

	@Autowired
	DbinsertTrace dbinserttrace = new DbinsertTrace();

	final static String AMS_STEP = "500";
	static MessageXMLResourceManager mxr = MessageXMLResourceManager.getInstance("MessageResource.xml");

	public String cargoImpStart(@Header("AMS_TRX_ROUTE") String ams_out_reverse_key, @Body String msg, Exchange exchange) throws Exception {
		boolean flag = true;
		String smi = "";
		String version = "";
		HashMap<String,Object> fhlmap = new HashMap<String,Object>();
		HashMap<String,Object> fhlmap_header = new HashMap<String,Object>();
		HashMap<String,Object> fhlmap_body = new HashMap<String,Object>();
		String fnaMsg = "";
		fhlmap =  (HashMap<String, Object>) exchange.getIn().getHeader("EDI_PARSE");
		try{
			sqlMapClient.startTransaction();
			logger.info(" === CargoImp Check START === ");
			/** 정규식 검증 [s]**/
			// 1. 변수선언 및 정의
			StringBuffer pTotal = new StringBuffer();
			String refNo = "";
			String refName = "";
			String regEx = "";
			String repeateInfo = "";
			String refStr = "";
			String strCargoImp = fhlmap.get("BODY").toString();
			smi = fhlmap.get("SMI").toString();
			version = fhlmap.get("VERSION").toString();
			// 2. 정규식 조회
			HashMap[] maps = getSelectSpec(smi, version);
			// 3. Ref별 정규식 적용
			for (int i=0; i<maps.length; i++) {
				refNo		= CommonUtil.nvl(maps[i].get("REFER_NO"));
				refName		= CommonUtil.nvl(maps[i].get("REFER_NAME"));
				regEx		= CommonUtil.nvl(maps[i].get("REGEX"));
				repeateInfo	= CommonUtil.nvl(maps[i].get("REPEATE_INFO"));

				// 3-1. Mandartory
				if ("MM".equals(repeateInfo)) {
					// 전체정규식 정의
					pTotal.append(regEx);
					// 해당 Ref 체크
					refStr = CommonUtil.nvl(ValidationUtil.extractPatternData(strCargoImp, regEx));
					if ("".equals(refStr)) {
						throw new ValidationException(
								mxr.getProperty("validation.message.structure.exception",
										new String[] {"REF " + refNo + ". " + refName}));
					}

				}
				// 3-2. Optional
				else if ("OO".equals(repeateInfo)){
					// 전체정규식 정의
					pTotal.append("(").append(regEx).append(")?");
					// 해당 Ref 체크
					refStr = CommonUtil.nvl(ValidationUtil.extractPatternData(strCargoImp, regEx));
					if ("".equals(refStr)) {
					      String letters = "";
					      if(CommonUtil.nvl(regEx).length() > 4){
						       if(regEx.startsWith("(")){
						        letters = "\r\n"+regEx.substring(1,4);
						       }else{
						        letters = "\r\n"+regEx.substring(0,3);
						       }
					      }else{
					       letters = "\r\n"+CommonUtil.nvl(regEx);
					      }
					      if (!"".equals(letters) && CommonUtil.isExit(strCargoImp, letters)) {
					       throw new ValidationException(
					         mxr.getProperty("validation.message.structure.exception",
					           new String[] {"REF " + refNo + ". " + refName}));
					      }
				     }
				}
				/*else if ("OO".equals(repeateInfo)){
					// 전체정규식 정의
					pTotal.append("(").append(regEx).append(")?");
					// 해당 Ref 체크
					refStr = CommonUtil.nvl(ValidationUtil.extractPatternData(strCargoImp, regEx));
					if ("".equals(refStr)) {
						String letters = (CommonUtil.nvl(regEx).length() > 4) ? "\r\n"+regEx.substring(0,3): "\r\n"+CommonUtil.nvl(regEx);
						if (!"".equals(letters) && CommonUtil.isExit(strCargoImp, letters)) {
							throw new ValidationException(
									mxr.getProperty("validation.message.structure.exception",
											new String[] {"REF " + refNo + ". " + refName}));
						}
					}
				}*/
				// 3-3. Mandartory and Repeat
				else if ("MR".equals(repeateInfo)){
					// 전체정규식 정의
					regEx = "("+regEx+")+";
					pTotal.append(regEx);
					// 해당 Ref 체크
					refStr = CommonUtil.nvl(ValidationUtil.extractPatternData(strCargoImp, regEx));
					if ("".equals(refStr)) {
						throw new ValidationException(
								mxr.getProperty("validation.message.structure.exception",
										new String[] {"REF " + refNo + ". " + refName}));
					}
				}
				// 3-4. Optional and Repeat
				else if ("OR".equals(repeateInfo)){
					// 전체정규식 정의
					pTotal.append("((").append(regEx).append(")+)?");
					// 해당 Ref 체크
					// 존재하지 않을 수 있으므로 체크 생략
					refStr = CommonUtil.nvl(ValidationUtil.extractPatternData(strCargoImp, "("+regEx+")+"));
				}
			}
			// 4. 전체 메시지 정규식 적용
				flag = ValidationUtil.validPattern(strCargoImp, pTotal.toString());


			if (!flag) {
				logger.info("MSG PATTERN =>\n" + pTotal);
				logger.info(mxr.getProperty("validation.message.structure.exception").toString());
				throw new ValidationException(
						mxr.getProperty("validation.message.structure.exception",
								new String[] {"MESSAGE OF " + smi}));
			}

			HashMap logMap = new HashMap();
			logMap.put("HISTORY_STATUS", "CargoImp Check");
			logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
			logMap.put("AMS_STEP", AMS_STEP);

			commonSql.insertHistory(logMap);

		} catch(IndexOutOfBoundsException e){
			logger.info(" === CargoImp Check IndexOutOfBoundsException Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","CargoImp Check IndexOutOfBoundsException",e, msg);
		}catch(NullPointerException e){
			logger.info(" === CargoImp Check NullPointer Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","CargoImp Check NullPointerException",e, msg);
		}catch(SQLException e){
			logger.info(" === CargoImp Check SQL Error === ");
			logger.error("ERROR==>"+e.toString(), e);
			flag = false;
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","CargoImp Check SQLException",e, msg);
		}catch(Exception e){
			logger.info(" === CargoImp Check Error === ");
	    	//logger.error("ERROR==>"+e.toString(), e);
	    	String errorCode = new String();
	    	String errorMsg = new String();
	    	if (CommonUtil.isExit(e.toString(), ":::")) {
				errorCode = e.toString().substring(e.toString().indexOf(":::")-4, e.toString().indexOf(":::"));
				errorMsg = e.toString().substring(e.toString().indexOf(":::")+3);
			}else {
				errorCode = "9999";
				errorMsg = e.toString();
			}
	    	errorMsg = CommonUtil.nullChk(errorMsg,"");
	    	fhlmap.put("ERRORCODE", errorCode);
	    	fhlmap.put("ERRORMSG", (errorMsg.length()>65)?errorMsg.substring(0,65):errorMsg);
	    	flag = false;
			HashMap logMap = new HashMap();
			logMap = (HashMap)exchange.getIn().getHeader("LOG_MAP");
			mainExceptionManager.process(ams_out_reverse_key, "AMS-MAIN","CargoImp Check Exception",e, msg);
	    }finally{

	    	if(!flag){
		    	//** FNA 생성 [s] **//*
		    	fnaMsg = createack.FNAack(fhlmap);
		    	fhlmap.put("AMS_ACK", fnaMsg);
		    	fhlmap.put("AMS_SMI", "FNA");
				/** 정규식 검증 [e]**/
				//EDI HISTORY INSERT[S]
				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "CargoImp Check Error");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", "509");
				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
				//EDI HISTORY INSERT[E]
				flag = false;
				logger.info(" === CargoImp Check Error === ");
			}else{
				/** 정규식 검증 [e]**/
				//EDI HISTORY INSERT[S]
				HashMap logMap = new HashMap();
				logMap.put("HISTORY_STATUS", "CargoImp Check Success");
				logMap.put("AMS_TRX_ROUTE", ams_out_reverse_key);
				logMap.put("AMS_STEP", AMS_STEP);
				commonSql.insertHistory(logMap);
				commonSql.updateTrace(ams_out_reverse_key, logMap, fhlmap);
				//EDI HISTORY INSERT[E]
				flag = true;
				logger.info(" === CargoImp Check Success === ");
			}
			exchange.getIn().setHeader("Cargoimp_Check_Flag", flag);

	    	exchange.getIn().setHeader("CHECK_FLAG",Boolean.valueOf(flag));

			logger.info(" === CargoImp Check End === ");
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

	/**
     * CIMP 정규식 CHECK
     */
	public HashMap<String, Object>[] getSelectSpec(String smi, String version) throws NoSuchPatternException, SystemException {

		HashMap[] maps = null;
		Vector<HashMap<String, Object>> vt = new Vector<HashMap<String, Object>>();
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		try {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("SMI", smi);
			map.put("VERSION", version);
			list = sqlMapClient_msb.queryForList("COMMON_SQL.selectRegex",map);
			//list = sqlMapClient.queryForList("COMMON_SQL.selectRegex",map);
		    for(HashMap returnMap : list){
		    	vt.add(returnMap);
		    }
		} catch (Exception e) {
			logger.error(e.toString(), e);
			e.printStackTrace();
		} finally {
			maps = (HashMap[])vt.toArray(new HashMap[vt.size()]);
		}

		if (maps.length < 1) {
		}
		return maps;
	}



}
