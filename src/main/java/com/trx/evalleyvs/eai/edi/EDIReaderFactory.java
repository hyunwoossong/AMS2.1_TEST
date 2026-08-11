/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.InputSource;

import com.trx.evalleyvs.eai.message.ErrorMessages;

/**
 * Creates a subcless of EDIReader appropriate for parsing a particular EDI
 * interchange. This class has just enough knowledge of the supported standards
 * make a decision based on observation of the first several characters of data.
 * This decision does not imply that data in well-formed with regard to the
 * chosen standard, but merely that we know which actual parser to use.
 */
public class EDIReaderFactory {

	static EDITokenizer tokenizer = null;

	/**
	 * Equivalent to createEDIReader(source, debugging=false)
	 */
	public static EDIReader createEDIReader(InputSource source)
			throws EDISyntaxException, IOException {
		return createEDIReader(source, false);
	}

	/**
	 * Factory method to create an instance of a subclass of EDIReader based on
	 * examiniation of the first few characters of data.
	 */
	public static EDIReader createEDIReader(InputSource source, boolean debug)
			throws EDISyntaxException, IOException {
		EDIReader parser;
		Reader inputReader = EDIAbstractReader.createReader(source);

		if (tokenizer != null && inputReader == tokenizer.getReader()) {
			if (debug) {
				trace("ReaderFactory reusing tokenizer");
			}
		} else {
			if (debug) {
				trace("ReaderFactory creating tokenizer");
			}
			
			tokenizer = new EDITokenizer(inputReader);
		}

		// Skip past any leading whitespace
		tokenizer.scanTerminatorSuffix();
		

		char[] buf = tokenizer.lookahead(3);
		if (buf == null || buf.length < 3) {
			
			return null;
		}
		

		if (buf[0] == 'I' && buf[1] == 'S' && buf[2] == 'A') {
			
			parser = new AnsiReader();
		} else if (buf[0] == 'U' && buf[1] == 'N') {
			parser = new EdifactReader();
		} else if (buf[0] == 'M' && buf[1] == 'S' && buf[2] == 'H') {
			
			try {
				Class parserClass = Class
						.forName("com.berryworks.edireader.hl7.HL7Reader");
				
				parser = (EDIReader) parserClass.newInstance();
			} catch (Exception e) {
				
				throw new EDISyntaxException(ErrorMessages.NO_HL7_PARSER);
			}
		} else {
			throw new EDISyntaxException(ErrorMessages.NO_STANDARD_BEGINS_WITH
					+ String.valueOf(buf));
		}
		
		
		source.setCharacterStream(inputReader);
		parser.setTokenizer(tokenizer);
		parser.preview();
		return parser;
	}

	/**
	 * Shorthand for EDIReader.trace(String)
	 */
	protected static void trace(String string) {
		EDIAbstractReader.trace(string);
	}

}
