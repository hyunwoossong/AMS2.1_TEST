/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * Reads and parses ANSI X.12 EDI interchanges. This class is not normally
 * constructed explicity from outside the package, although it is declared
 * public for special cases. The recommended use of this class is to first
 * establish an EDIReader using one of the factory techniques; when the
 * EDIReader is called upon to parse the EDI data, it deteremines which EDI
 * standard applies and internally constructs the proper subclass to continue
 * with parsing.
 */
public class AnsiReader extends StandardReader {
	/**
	 * Group-level function code (for example: PO)
	 */
	private String groupFunctionCode;

	/**
	 * Group-level application sender
	 */
	private String groupSender;

	/**
	 * Group-level application receiver
	 */
	private String groupReceiver;

	/**
	 * Group-level version (for example: 003040)
	 */
	private String groupVersion;

	/**
	 * Group-level date (for example: 20040410 or 040410)
	 */
	private String groupDate;

	private AnsiFAGenerator faGenerator;

	protected TokenI recognizeBeginning() throws IOException, EDISyntaxException {
		TokenI t = tokenizer.nextToken();
		if ((t.getType() != TokenI.SEGMENT_START) || (!t.valueEquals("ISA"))) {
			throw new EDISyntaxException(X12_MISSING_ISA);
		}
		return t;
	}

	/**
	 * Description of the Method
	 * 
	 * @param token
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception SAXException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	protected TokenI parseInterchange(TokenI token) throws SAXException,
			IOException {
		groupCount = 0;

		if (debug) {
			trace("entering parseInterchange");
		}
		interchangeAttributes.clear();
		interchangeAttributes.addCDATA(STANDARD, "ANSI X.12");
		tokenizer.nextToken();
		tokenizer.nextToken();
		tokenizer.nextToken();
		tokenizer.nextToken();
		String fromQual = tokenizer.nextSimpleValue();
		String fromId = tokenizer.nextSimpleValue();
		String toQual = tokenizer.nextSimpleValue();
		String toId = tokenizer.nextSimpleValue();
		interchangeAttributes.addCDATA(DATE, tokenizer.nextSimpleValue());
		interchangeAttributes.addCDATA(TIME, tokenizer.nextSimpleValue());
		tokenizer.nextToken();
		tokenizer.nextToken();
		interchangeAttributes.addCDATA(CONTROL, interchangeControlNumber = tokenizer
				.nextSimpleValue());

		// Go ahead and parse tokens until the end of the segment is reached
		while ((token = tokenizer.nextToken()).getType() != TokenI.SEGMENT_END) {
			if (tokenizer.getElementInSegmentCount() > 30) {
				throw new EDISyntaxException(TOO_MANY_ISA_FIELDS, tokenizer);
			}
		}

		// Now make the the callbacks to the ContentHandler
		startElement(INTERCHANGE_TAG, interchangeAttributes);

		interchangeAttributes.clear();
		startElement(SENDER_TAG, interchangeAttributes);
		interchangeAttributes.addCDATA(ID_ATTRIBUTE, fromId);
		interchangeAttributes.addCDATA(QUALIFIER_ATTRIBUTE, fromQual);
		startElement(ADDRESS_TAG, interchangeAttributes);
		endElement(ADDRESS_TAG);
		endElement(SENDER_TAG);

		interchangeAttributes.clear();
		startElement(RECEIVER_TAG, interchangeAttributes);
		interchangeAttributes.addCDATA(ID_ATTRIBUTE, toId);
		interchangeAttributes.addCDATA(QUALIFIER_ATTRIBUTE, toQual);
		startElement(ADDRESS_TAG, interchangeAttributes);
		endElement(ADDRESS_TAG);
		endElement(RECEIVER_TAG);

		while (true) {
			token = tokenizer.nextToken();
			if (token.getType() != TokenI.SEGMENT_START) {
				throw new EDISyntaxException(INVALID_BEGINNING_OF_SEGMENT,
						tokenizer.getSegmentCount());
			}
			String sType = token.getValue();
			if (sType.equals("GS")) {
				groupCount++;
				token = parseFunctionalGroup(token);
			} else if (sType.equals("IEA")) {
				break;
			} else {
				throw new EDISyntaxException(
						"Expected IEA or GS segment instead of " + sType,
						tokenizer.getSegmentCount());
			}
		}

		int n;
		if (groupCount != (n = tokenizer.nextIntValue())) {
			throw new EDISyntaxException(COUNT_IEA, groupCount, n, tokenizer);
		}
		String s;
		if (!(s = tokenizer.nextSimpleValue()).equals(interchangeControlNumber)) {
			throw new EDISyntaxException(CONTROL_NUMBER_IEA,
					interchangeControlNumber, s, tokenizer);
		}

		getFAGenerator().generateAcknowledgementWrapup();

		endElement(INTERCHANGE_TAG);

		return (tokenizer.skipSegment());
	}

	/**
	 * Parse ANSI Functional Group (GS .. GE)
	 * 
	 * @param token
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception SAXException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	private TokenI parseFunctionalGroup(TokenI token) throws SAXException,
			IOException {
		int docCount = 0;

		groupAttributes.clear();
		groupAttributes.addCDATA(GROUP_TYPE, groupFunctionCode = tokenizer
				.nextSimpleValue());
		groupAttributes.addCDATA(APPL_SENDER, groupSender = tokenizer
				.nextSimpleValue());
		groupAttributes.addCDATA(APPL_RECEIVER, groupReceiver = tokenizer
				.nextSimpleValue());
		groupAttributes.addCDATA(DATE, groupDate = tokenizer.nextSimpleValue());
		groupAttributes.addCDATA(TIME, tokenizer.nextSimpleValue());
		groupAttributes.addCDATA(CONTROL, groupControlNumber = tokenizer
				.nextSimpleValue());
		groupAttributes.addCDATA(STANDARD_CODE, tokenizer.nextSimpleValue());
		groupAttributes.addCDATA(STANDARD_VERSION, groupVersion = tokenizer
				.nextSimpleValue());
		tokenizer.skipSegment();
		startElement(GROUP_TAG, groupAttributes);

		getFAGenerator().generateGroupAcknowledgmentHeader(firstSegment,
				groupSender, groupReceiver, groupDate.length(), groupVersion,
				groupFunctionCode, groupControlNumber);

		while (true) {
			token = tokenizer.nextToken();
			if (token.getType() != TokenI.SEGMENT_START) {
				throw new EDISyntaxException(INVALID_BEGINNING_OF_SEGMENT,
						tokenizer.getSegmentCount());
			}

			String sType = token.getValue();
			if (sType.equals("ST")) {
				docCount++;
				token = parseDocument(token);
			} else if (sType.equals("GE")) {
				break;
			} else {
				throw new SAXException("Expected GE or ST segment instead of "
						+ sType);
			}
		}

		int n;
		if (docCount != (n = tokenizer.nextIntValue())) {
			throw new EDISyntaxException(COUNT_GE, docCount, n, tokenizer);
		}
		String s;
		if (!(s = tokenizer.nextSimpleValue()).equals(groupControlNumber)) {
			throw new EDISyntaxException(CONTROL_NUMBER_GE, groupControlNumber,
					s, tokenizer);
		}

		endElement(GROUP_TAG);
		getFAGenerator().generateGroupAcknowledgmentTrailer(docCount);
		return (tokenizer.skipSegment());
	}

	/**
	 * Parse ANSI Document/Transaction Set (ST .. SE)
	 * 
	 * @param token
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception SAXException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	private TokenI parseDocument(TokenI token) throws SAXException, IOException {
		String control;
		String documentType;
		int segCount = 2;

		if (debug) {
			trace("entering parseDcoument");
		}

		documentAttributes.clear();
		documentAttributes.addCDATA("DocType", documentType = tokenizer
				.nextSimpleValue());
		LoopingState loopingState = new LoopingState("ANSI");
		loopingState.setDebug(debug);
		if (loopingState.useLoopingStructure(documentType)) {
			documentAttributes.addCDATA("Name", loopingState.getDocumentName());
		}
		documentAttributes.addCDATA(CONTROL, control = tokenizer
				.nextSimpleValue());
		tokenizer.skipSegment();
		startElement(DOCUMENT_TAG, documentAttributes);

		String segmentType;
		while (!(segmentType = tokenizer.nextSegment()).equals("SE")) {
			if (debug) {
				trace("parsing segment " + segmentType + " within the "
						+ documentType + " document");
			}
			segCount++;
			if (loopingState.transition(segmentType)) {
				// First close off any loops that were closed as the result of
				// the transition
				int toClose = loopingState.closedCount();
				if (debug)
					trace("closing " + toClose + " loops");
				for (; toClose > 0; toClose--) {
					endElement(LOOP_TAG);
				}

				String s = loopingState.getLoopEntered();
				if (s.startsWith("/")) {
					// This is the outer loop which we do not explicitly
					// represent
				} else {
					documentAttributes.clear();
					documentAttributes.addCDATA(ID_ATTRIBUTE, s);
					startElement(LOOP_TAG, documentAttributes);
				}

			}

			documentAttributes.clear();
			documentAttributes.addCDATA(ID_ATTRIBUTE, segmentType);
			startElement(SEG_TAG, documentAttributes);
			TokenI t;
			while ((t = tokenizer.nextToken()).getType() != TokenI.SEGMENT_END) {
				if (debug) {
					trace("...nextToken in the " + segmentType);
				}
				parseSegmentElement(t);
			}
			endElement(SEG_TAG);
		}

		int toClose = loopingState.getNestingLevel();
		if (debug)
			trace("closing all " + toClose + " loops");
		for (; toClose > 0; toClose--) {
			endElement(LOOP_TAG);

		}

		int n;
		if (segCount != (n = tokenizer.nextIntValue())) {
			throw new EDISyntaxException(COUNT_SE, segCount, n, tokenizer);
		}
		String s;
		if (!(s = tokenizer.nextSimpleValue()).equals(control)) {
			throw new EDISyntaxException(CONTROL_NUMBER_SE, control, s,
					tokenizer);
		}

		getFAGenerator().generateTransactionAcknowledgment(documentType,
				control);
		endElement(DOCUMENT_TAG);

		// Skip over this SE segment
		// return the SEGEMENT_END token
		TokenI t = tokenizer.skipSegment();
		return t;
	}

	/**
	 * Preview the ANSI X.12 input before attempting to tokenize it in order to
	 * discover syntactic details including segment terminator and field
	 * delimiter. Upon return, the input stream has been re-positioned so that
	 * the tokenizer can read from the beginning of the interchange.
	 * 
	 * @exception EDISyntaxException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	public void preview() throws EDISyntaxException, IOException {
		if (debug) {
			trace("previewing ANSI interchange for syntax details");
		}
		if (previewed) {
			throw new EDISyntaxException(INTERNAL_ERROR_MULTIPLE_EOFS);
		}

		// No release character is support for ANSI X.12
		release = -1;

		char[] buf = tokenizer.lookahead(128);
		if ((buf == null) || (buf.length < 128)) {
			throw new EDISyntaxException(INCOMPLETE_X12);
		}

		if (!(buf[0] == 'I' && buf[1] == 'S' && buf[2] == 'A')) {
			throw new EDISyntaxException(X12_MISSING_ISA);
		}
		// ISA*.....
		// ^ (offset 3)
		delimiter = buf[3];

		if (buf[84] == 'U') {
			subDelimiter = buf[107];
			terminator = buf[108];
			if (debug) {
				trace("At offset 108 is segment terminator " + terminator);
			}
			terminatorSuffix = findTerminatorSuffix(buf, 109, 128);
			// Keep an image of the ISA for future reference.
			// Note that we keep the terminator, but not any suffix
			firstSegment = new String(buf, 0, 109);
		} else { // if (buf[82] == 'U') {
			subDelimiter = buf[104];
			terminator = buf[105];
			if (debug) {
				trace("At offset 105 is segment terminator " + terminator);
			}
			terminatorSuffix = findTerminatorSuffix(buf, 106, 128);
			// Keep an image of the ISA for future reference.
			// Note that we keep the terminator, but not any suffix
			firstSegment = new String(buf, 0, 106);
			// } else {
			// throw new EDISyntaxException(
			// "ANSI X.12 interchange must have a Standards Identifier of U");
		}

		previewed = true;

	}

	private String findTerminatorSuffix(char[] buf, int i, int j) {
		StringBuffer result = new StringBuffer();
		for (int n = i; n < j && !Character.isLetter(buf[n]); n++) {
			result.append(buf[n]);
		}
		return result.toString();
	}

	/**
	 * Set an override value to be used whenever generating a control date and
	 * time. This method is used only for automated testing.
	 */
	public void setControlDateAndTime(String overrideValue) {
		getFAGenerator().setControlDateAndTime(overrideValue);
	}

	private AnsiFAGenerator getFAGenerator() {
		if (faGenerator == null) {
			faGenerator = new AnsiFAGenerator(this, ackStream);
		}
		return faGenerator;
	}

}
