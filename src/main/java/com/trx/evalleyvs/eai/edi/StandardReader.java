/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Common parent class to several EDIReader subclasses that provide for the
 * parsing of specific EDI standards. This common parent provides an opportunity
 * to factor and share common concepts and logic.
  *
 */
public abstract class StandardReader extends EDIReader {
	
	/**
	 * Interchange Control Number
	 */
	protected String interchangeControlNumber;

	/**
	 * Group-level control number
	 */
	protected String groupControlNumber;

	/**
	 * Count of the groups in the interchange
	 */
	protected int groupCount = 0;

	protected abstract TokenI recognizeBeginning() throws IOException,
			EDISyntaxException, SAXException;

	protected abstract TokenI parseInterchange(TokenI t) throws SAXException,
			IOException;

	public void parse(InputSource source) throws SAXException, IOException {
		if (source == null) {
			throw new IOException("parse called with null InputSource");
		}
		if (contentHandler == null) {
			throw new IOException("parse called with null ContentHandler");
		}
		parseSetup(source);

		tokenizer.setDelimiter(delimiter);
		tokenizer.setSubDelimiter(subDelimiter);
		tokenizer.setRelease(release);
		tokenizer.setTerminator(terminator);

		AttributesImpl attrList = new AttributesImpl();
		attrList.clear();
		contentHandler.startDocument();
		startElement(ROOT_TAG, attrList);

		TokenI t = recognizeBeginning();

		t = parseInterchange(t);

		endElement(ROOT_TAG);
		contentHandler.endDocument();

	}

	/**
	 * Issue SAX calls on behalf of an EDI element. The token passed as an
	 * argument is first token of a field.
	 * 
	 * @param t
	 *            the parsed token
	 * @exception SAXException
	 *                Description of the Exception
	 */
	protected void parseSegmentElement(TokenI t) throws SAXException {
		String elementId = t.getElementId();
		documentAttributes.clear();
	
		if (t.getType() == TokenI.SIMPLE) {
			documentAttributes.addAttribute("", ID_ATTRIBUTE, ID_ATTRIBUTE,
					"CDATA", elementId);
			contentHandler.startElement("", ELEMENT_TAG, ELEMENT_TAG,
					documentAttributes);
			char[] cv = t.getValueChars();
			contentHandler.characters(cv, 0, cv.length);
			endElement(ELEMENT_TAG);
			if (debug) {
				trace("... SIMPLE element " + elementId);
			}
		} else if ((t.getType() == TokenI.SUB_ELEMENT)
				|| (t.getType() == TokenI.SUB_EMPTY)) {
			if (t.isFirst()) {
				documentAttributes.addAttribute("", ID_ATTRIBUTE, ID_ATTRIBUTE,
						"CDATA", elementId);
				documentAttributes.addAttribute("", COMPOSITE_INDICATOR,
						COMPOSITE_INDICATOR, "CDATA", "yes");
				startElement(ELEMENT_TAG, documentAttributes);
				if (debug) {
					trace("... first subelement of a composite");
				}
			}
			if (t.getType() == TokenI.SUB_ELEMENT) {
				documentAttributes.clear();
				documentAttributes.addAttribute("", SUB_ELEMENT_SEQUENCE,
						SUB_ELEMENT_SEQUENCE, "CDATA", String.valueOf(1 + t
								.getSubIndex()));
				startElement(SUB_ELEMENT_TAG, documentAttributes);
				char[] cv = t.getValueChars();
				contentHandler.characters(cv, 0, cv.length);
				endElement(SUB_ELEMENT_TAG);
				if (debug) {
					trace("... subelement");
				}
			}
			if (t.isLast()) {
				endElement(ELEMENT_TAG);
				if (debug) {
					trace("... last subelement of a composite");
				}
			}
		}
	}

	protected void startElement(String tag, Attributes attributes) throws SAXException {
		contentHandler.startElement("", tag, tag, attributes);
	}

	protected void endElement(String tag) throws SAXException {
		contentHandler.endElement("", tag, tag);
	}

}
