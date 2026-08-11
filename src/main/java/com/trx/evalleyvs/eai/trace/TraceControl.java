package com.trx.evalleyvs.eai.trace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.trx.evalleyvs.eai.cargo.util.ValidationUtil;
import com.trx.evalleyvs.eai.log.LoggerFactory;
import com.trx.evalleyvs.eai.util.CommonUtil;

public class TraceControl {

	static Logger logger = LoggerFactory.getLogger("MSB");

	
	private static TraceControl control;
	
	public static TraceControl getInstance() {
		if(control == null) {
			control = new TraceControl();
			return control;
		}else {
			return control;
		}
	}

	
}
