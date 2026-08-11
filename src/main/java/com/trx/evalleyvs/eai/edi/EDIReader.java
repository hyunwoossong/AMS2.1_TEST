/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.trx.evalleyvs.eai.message.ErrorMessages;

/**
 * Reads and parses an EDI interchange in any of the supported EDI standards.
 * Once a specific EDI standard is identified, EDIReader delegates the actual
 * parsing to a subclass of EDIReader that it creates. This delegation technique
 * allows an application to use EDIReader just as it would an XMLReader and
 * without having to configure or otherwise signal for a particular standard to
 * be used. Another advantage of this approach is that it provides a framework
 * for additional EDIReader subclasses to be developed and integrated wiht
 * little impact.
 */
public class EDIReader extends EDIAbstractReader implements ErrorMessages,
		XMLTags {

	private EDIReader theReader = null;

	/**
	 * If debug is set to true, then a parser may emit diagnostic information to
	 * System.err
	 */
	public static boolean debug = false;

	/**
	 * Constructor for the EDIReader object
	 */
	public EDIReader() {
		if (Boolean.getBoolean("edireader.debug")) {
			setDebug(true);
		}
	}

	/**
	 * Read enough of the EDI interchange to establish which characters are used
	 * for segment terminators, element delimiters, etc. Each sublcass of
	 * EDIReader overrides this method with logic specific to a particular EDI
	 * standard. Upon return, the input stream has been re-positioned so that
	 * the interchange will be parsed from the beginning by <code>parse()</code>.
	 * 
	 */
	public void preview() throws EDISyntaxException, IOException {
		throw new EDISyntaxException("EDIReader.preview() called unexptectedly");
	}
	
	public void parse(InputSource source) throws SAXException, IOException {
		if (theReader == null) {
			theReader = EDIReaderFactory.createEDIReader(source);
			if (debug) {
				trace("EDIReader.parse(InputSouce) created an EDIReader of type "
						+ theReader.getClass().getName());
			}
			theReader.setAcknowledgment(ackStream);
			theReader.setContentHandler(contentHandler);
		}
		theReader.parse(source);
	}
	/**
	 * Sets debug on or off.
	 */
	public static void setDebug(boolean d) {
		if (debug && d) {
			trace("Debug already on");
		} else if (!debug && d) {
			trace("Debug turned on");
		} else if (debug && !d) {
			trace("Debug turned off");
		}
		debug = d;
	}

}
