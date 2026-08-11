package com.trx.util;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;

public class MainExceptionManager {

	private static final Logger logger = LoggerFactory.getLogger(MainExceptionManager.class);

	@Autowired
	SqlMapClient sqlMapClient;

	@Autowired
	CommonSql commonSql;

	public void process(String msb_out_reverse_key, String system, String subject, Exception e, String msg) throws Exception {
		if(e instanceof SQLException){
			throw e;
		}else{
			byte[] msg_bytes = msg.getBytes("UTF-8");
			String errMsg = "";
			/**  oracle 에서 blob 데이터 조회 가능 (1500자 이내) **/
			if(printStackTraceToString(e).length() > 1500){
				errMsg = printStackTraceToString(e).substring(0,1500);
			}
			byte[] err_bytes = errMsg.getBytes("UTF-8");
			logger.info(subject+"===>\r\n["+printStackTraceToString(e)+"\r\n]");
			commonSql.insertException(msb_out_reverse_key, system, subject, err_bytes, msg_bytes);
		}

	}
	/**
	 * e.printStackTrace(); -> String 으로 변환
	 * @param e
	 * @return
	 */
	public String printStackTraceToString(Throwable e) {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(e.toString());
			sb.append("\n");
			StackTraceElement element[] = e.getStackTrace();
			for (int idx = 0; idx < element.length; idx++) {
				sb.append("\tat ");
				sb.append(element[idx].toString());
				sb.append("\n");
			}
		} catch (Exception ex) {
			return e.toString();
		}
		return sb.toString();
	}
}
