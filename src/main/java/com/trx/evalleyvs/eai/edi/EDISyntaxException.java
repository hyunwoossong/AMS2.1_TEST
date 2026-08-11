/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.trx.evalleyvs.eai.log.LoggerFactory;
import com.trx.evalleyvs.eai.util.CommonUtil;

/**
 * An exception thrown during EDI parsing when invalid EDI syntax, structure,
 * or content is encountered.
 *
 */
public class EDISyntaxException extends SAXException {

	private int errorSegmentNumber;

	private int errorElementNumber;
	private static Logger logger = LoggerFactory.getLogger("MSB_ADMIN");
	public EDISyntaxException(String desc) {
		super(desc);
	}

	public EDISyntaxException(String desc, int seg) {
		super(desc + " at segment " + seg);
		errorSegmentNumber = seg;
	}

	public EDISyntaxException(String desc, EDITokenizer tokenizer) {
		super(desc + " at segment " + tokenizer.getSegmentCount() + ", field "
				+ tokenizer.getElementInSegmentCount());
		errorSegmentNumber = tokenizer.getSegmentCount();
		errorElementNumber = tokenizer.getElementInSegmentCount();

		CommonUtil.writelog(desc);
		//CommonUtil.writelog(tokenizer);
		CommonUtil.writelog("=============");
	}

	public EDISyntaxException(String desc, String expected, String actual,
			EDITokenizer tokenizer) {
		this(desc + ". Expected " + expected + " instead of " + actual,
				tokenizer);
	}

	public EDISyntaxException(String desc, int expected, int actual, EDITokenizer tokenizer) {
		this(desc + ". Expected " + expected + " instead of " + actual,
				tokenizer);
	}

	public EDISyntaxException(String desc, IOException e) {
		this(desc);
	}

	public int getErrorElementNumber() {
		return errorElementNumber;
	}

	public int getErrorSegmentNumber() {
		return errorSegmentNumber;
	}

}
