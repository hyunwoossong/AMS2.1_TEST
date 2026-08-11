/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Parser;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 *  Wraps an EDIReader.
 *
 *  EDIParser is necessary to satisfy the JAXP conventions
 *  for dynamic selection and loading of a specific XML parser into
 *  an XML application. By following this convention,
 *  it is possible to integrate EDIReader (and therefore EDI data)
 *  into various XML applications without any changes to those applications.
 *
 */
public class EDIParser extends SAXParser {

	private EDIReader ediReader = null;


	/**
	 *Constructor for the EDIParser object
	 * :::
	 * This EDIParser provides for delayed format recognition,
	 * where the actual subclass of EDIReader is not created until
	 * the pase method is first called. This mechanism is in support
	 * of the JAXP interfaces.
	 */
	public EDIParser() { }


	/**
	 *Constructor for the EDIParser object
	 *
	 * @param  ediReader  Description of the Parameter
	 */
	public EDIParser(EDIReader ediReader) {
		this.ediReader = ediReader;
	}


	/**
	 *  Gets the XMLReader attribute of the EDIParser object
	 *
	 * @return    The XMLReader value
	 */
	public XMLReader getXMLReader() {
		if (ediReader == null) {
			ediReader = new EDIReader();
		}
		return ediReader;
	}


	/**
	 *  Gets the parser attribute of the EDIParser object
	 *
	 * @return    The parser value
	 */
	public Parser getParser() {
		return null;
	}


	/**
	 *  Gets the validating attribute of the EDIParser object
	 *
	 * @return    The validating value
	 */
	public boolean isValidating() {
		return false;
	}


	/**
	 *  Gets the namespaceAware attribute of the EDIParser object
	 *
	 * @return    The namespaceAware value
	 */
	public boolean isNamespaceAware() {
		return true;
	}



	/**
	 *  Sets the property attribute of the EDIReader object
	 *
	 * @param  name                      The new property value
	 * @param  value                      The new property value
	 * @exception  SAXNotRecognizedException  Description of the Exception
	 * @exception  SAXNotSupportedException   Description of the Exception
	 */
	public void setProperty(String name, Object value)
			 throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException("Not implemented");
	}


	/**
	 *  Gets the property attribute of the EDIReader object
	 *
	 * @param  name                           Description of the Parameter
	 * @return                                The property value
	 * @exception  SAXNotRecognizedException  Description of the Exception
	 * @exception  SAXNotSupportedException   Description of the Exception
	 */
	public Object getProperty(String name)
			 throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException("Not implemented");
	}

}

