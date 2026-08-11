/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.cargo;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.trx.evalleyvs.eai.edi.EDIReader;
import com.trx.evalleyvs.eai.edi.EDIReaderFactory;
import com.trx.evalleyvs.eai.log.LoggerFactory;
import com.trx.evalleyvs.eai.util.CommonUtil;

/**
 * Cargo EDIFACT Validator
 */
public class CargoValidator {
	
	static Logger logger = LoggerFactory.getLogger("MSB");
	
	private InputSource inputSource;
	private ContentHandler handler;
	private EDIReader parser;
	private HashMap map;
	public CargoValidator(InputStream inStream) {
		inputSource = new InputSource(inStream);
		map = new HashMap();
	}

	public CargoValidator(Reader reader) {
		inputSource = new InputSource(reader);
		map = new HashMap();
	}
	
	public boolean validate() {
		
		boolean isOK = true;
		//CommonUtil.writelog("=== VALIDATOR 1 ===");
		handler = new ScanningHandler();
		
		try {
			int i = 0;
			while (true) {
				i = i + 1;
				//CommonUtil.writelog("=== VALIDATOR 2 ===" + i);
				parser = EDIReaderFactory.createEDIReader(inputSource,false);
				if (parser == null) {
					//CommonUtil.writelog("=== VALIDATOR 3 ===");
					break;
					
				}
				//CommonUtil.writelog("=== VALIDATOR 4 ===");
				parser.setContentHandler(handler);
				//CommonUtil.writelog("=== VALIDATOR 5 ===");
				parser.parse(inputSource);
				//CommonUtil.writelog("=== VALIDATOR 6 ===");
			}
		} catch (Exception e) {
			//map.put("CONTENT", e.getMessage());
			CommonUtil.writelog("VALIDATION is FALSE = " + e.getMessage());
			isOK = false;
		}
		return isOK;
	}
	
	public boolean hkValidate() {
		
		boolean isOK = true;
		CommonUtil.writelog("=== VALIDATOR 1 ===");
		handler = new ScanningHandler();
		
		try {
			int i = 0;
			while (true) {
				// The following line creates an EDIReader explicitly
				// as an alternative to the JAXP-based technique.
				i = i + 1;
				CommonUtil.writelog("=== VALIDATOR 2 ===" + i);
				parser = EDIReaderFactory.createEDIReader(inputSource,false);
				if (parser == null) {
					CommonUtil.writelog("=== VALIDATOR 3 ===");
					// end of input
					break;
					
				}
				CommonUtil.writelog("=== VALIDATOR 4 ===");
				parser.setContentHandler(handler);
				//parser.parse2(inputSource, 0);
				CommonUtil.writelog("=== VALIDATOR 5 ===");
				parser.parse(inputSource);
				CommonUtil.writelog("=== VALIDATOR 6 ===");
			}
		} catch (Exception e) {
			map.put("CONTENT", e.getMessage());
			CommonUtil.writelog("VALIDATION is FALSE = " + e.getMessage());
			isOK = false;
		}
		return isOK;
	}
	public Map getResult() {
		return map;
	}
	
	class ScanningHandler extends DefaultHandler {
		int interchangeCount = 0;
		String prefix = "";

		public void startElement(String namespace, String localName, String qName, Attributes atts) throws SAXException {
			String indent = "";
			
			if (localName.startsWith(EDIReader.INTERCHANGE_TAG)) {
				//CommonUtil.writelog("+Interchange  (" + interchangeCount + ")");
				interchangeCount++;
				indent = "   ";
				prefix = "";
			} else if (localName.startsWith(EDIReader.SENDER_TAG)) {
				//CommonUtil.writelog("  +Sender");
				indent = "     ";
				prefix = "SENDER_";
			} else if (localName.startsWith(EDIReader.RECEIVER_TAG)) {
				//CommonUtil.writelog("  +Recipient");
				indent = "     ";
				prefix = "RECIPIENT_";
			} else if (localName.startsWith(EDIReader.ADDRESS_TAG)) {
				//CommonUtil.writelog("    +Address");
				indent = "       ";
			} else if (localName.startsWith(EDIReader.DOCUMENT_TAG)) {
				//CommonUtil.writelog("    +Document");
				indent = "       ";
			} else {
				return;
			}

			int n = atts.getLength();
			for (int i = 0; i < n; i++) {
				//CommonUtil.writelog(indent + atts.getLocalName(i) + "=" + atts.getValue(i));
				map.put(prefix + atts.getLocalName(i).toUpperCase(), atts.getValue(i));
			}
		}
	}

}
