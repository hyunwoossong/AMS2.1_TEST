package com.trx.db;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.trx.Msgprocess.ConversionMSG;
import com.trx.Msgprocess.CreateMSG;
import com.trx.validate.HeaderCheck;

public class MessageSelect {

	private static final Logger logger = LoggerFactory.getLogger(MessageSelect.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	HeaderCheck headercheck;

	@Produce
	ProducerTemplate producer;

	@Autowired
	ConversionMSG conversionmsg = new ConversionMSG();

	@Autowired
	CreateMSG createmsg = new CreateMSG();

	public void ktnetTraceselect(Exchange exchange) throws SQLException {
		logger.info(" === KTNET IMP TRACE SELECT START === ");
		List<HashMap<String, Object>> result = sqlMapClient.queryForList("TM_TRACE.ktnetTrace");
		HashMap<String,Object> ktConmap = new HashMap<String,Object>();

		if (result.size() > 0) {

			for (int i = 0; i < result.size(); i++) {
				String fhlcon = "";
				ktConmap = (HashMap) result.get(i);
				String ktnetRoute = (String) ktConmap.get("KTNET_ROUTE");
				String divisionMsg = (String) ktConmap.get("DIVISION_MSG");
				String inpD = (String) ktConmap.get("INP_D");
				String inpT = (String) ktConmap.get("INP_T");


				if(ktConmap.get("DIVISION_MSG").toString().indexOf("CIMFHL")!=-1){
					System.out.println("KTNET FHL");
					fhlcon = createmsg.kt_fhl_con(conversionmsg.ktnetHeader(ktConmap));
				}else if(ktConmap.get("DIVISION_MSG").toString().indexOf("CIMFMA")!=-1){
					System.out.println("KTNET FMA");
					fhlcon = createmsg.kt_fma_con(conversionmsg.ktnetACKpaserFMA(ktConmap));
				}else if(ktConmap.get("DIVISION_MSG").toString().indexOf("CIMFNA")!=-1){
					System.out.println("KTNET FNA");
					fhlcon = createmsg.kt_fna_con(conversionmsg.ktnetACKpaserFNA(ktConmap));
				}else{
					logger.info(" === UNKNOWN TARGET ===");
				}

				HashMap<String,Object> ktroute = new HashMap<String,Object>();
				ktroute.put("KTNET_ROUTE", ktnetRoute);
				if(ktConmap.get("DIVISION_MSG").toString().indexOf("OCI/KR/IMP/I/")!=-1){
					//특송
					producer.sendBodyAndHeaders("direct:ktnetSPCqueuesend",fhlcon, ktroute);
				}else{
					//일반
					producer.sendBodyAndHeaders("direct:ktnetIMPqueuesend",fhlcon, ktroute);
				}

				sqlMapClient.update("INSERT_UPDATE.updateKTTrace", ktConmap);
				logger.info(" === KTNET IMP QUEUE SEND === ");

				//e-Freight DB Insert&Update 로직 추가 예정
				if(ktConmap.get("KTRCVD_PIMA").equals("RKRAGT82AMSHNX01/SEL01")&&ktConmap.get("KTSENDPIMA").equals("RKRCCS77KTNET")){

				}else if(ktConmap.get("KTRCVD_PIMA").equals("RKRAGT82AMSHNX01/SEL01")&&ktConmap.get("KTSENDPIMA").equals("RKRCCS77MFCS")){

				}

			}

			try {
				logger.info(" === KTNET CIMP CONVERSION SUCCESS === ");
			} catch (Exception e) {
				e.printStackTrace();
				logger.info(" === KTNET CIMP CONVERSION FAIL === ");
			}

		}



	}
}
