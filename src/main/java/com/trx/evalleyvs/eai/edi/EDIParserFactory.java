/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 *  An implementation of SAXParserFactory to provide for the
 *  creation of a custom SAXParser in the JAXP manner.
 *  The SAXParser that it creates is actually an
 *  EDIParser that parses EDI input instead of XML input
 *  but otherwise behaves as a normal SAXParser.
 *
 */
public class EDIParserFactory extends SAXParserFactory {

	public boolean isValidating() {return false;}
	public boolean isNamespaceAware() {return true;}
		
	public static SAXParserFactory newInstance() throws FactoryConfigurationError
	{
		return new EDIParserFactory();
	}
	
	public SAXParser newSAXParser() throws ParserConfigurationException, SAXException
	{
		return new EDIParser();
	}
	
	public void setFeature(String name, boolean value) {}
	public boolean getFeature(String name) {return false;}
}

