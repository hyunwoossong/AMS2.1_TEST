/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;

/**
 * Reads and parses EDIFACT EDI interchanges. This class is not normally
 * constructed explicity from outside the package, although it is declared
 * public for special cases. The recommended use of this class is to first
 * establish an EDIReader using one of the factory techniques; when the
 * EDIReader is called upon to parse the EDI data, it deteremines which EDI
 * standard applies and internally constructs the proper subclass to continue
 * with parsing.
 */
public class EdifactReader extends StandardReader {
	private boolean ungExplicit;

	//private static final int ELEMENTS_IN_SEGMENT_MAXIMUM = 50;	// 50�ٷ� ������ �ɷ� �־���..
	private static final int ELEMENTS_IN_SEGMENT_MAXIMUM = 200;	// 200�ٷ� ������ �÷���(������_2011.10.24.)
	//private static final int ELEMENTS_IN_UNB_MAXIMUM = 30;
	private static final int ELEMENTS_IN_UNB_MAXIMUM = 30;

	protected TokenI recognizeBeginning() throws IOException, SAXException {
		TokenI t = tokenizer.nextToken();
		if (t.getType() == TokenI.SEGMENT_START) {
			String segType = t.getValue();
			boolean witnessedUNA = false;
			if (segType.equals("UNA")) {
				witnessedUNA = true;
				// We've already examined this UNA in the preview
				tokenizer.skipSegment();
				t = tokenizer.nextToken();
				if (t.getType() == TokenI.SEGMENT_START) {
					segType = t.getValue();
				} else {
					throw new EDISyntaxException(INVALID_UNA, tokenizer);
				}
			}
			if (!segType.equals("UNB")) {
				if (witnessedUNA) {
					throw new EDISyntaxException(
							"Mandatory UNB segment was not recognized after UNA. Terminator problem?");
				}
				throw new EDISyntaxException(FIRST_SEGMENT_MUST_BE_UNA_OR_UNB,
						tokenizer);
			}
		} else {
			throw new EDISyntaxException(FIRST_SEGMENT_MUST_BE_UNA_OR_UNB);
		}
		return t;
	}

	/**
	 * Parse Edifact interchange ( UNB .. UNZ )
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
		if (debug) {
			trace("entering parseInterchange for an EDIFACT interchange");
		}
		interchangeAttributes.clear();
		interchangeAttributes.addCDATA(STANDARD, "EDIFACT");
		groupCount = 0;

		tokenizer.nextToken();
		tokenizer.nextToken();

		/**
		 * Sender address
		 */
		List v = tokenizer.nextCompositeElement();
		String fromId = "", fromQual = "", fromExtra = null;
		try {
			fromId = (String) v.get(0);
			fromQual = (String) v.get(1);
			fromExtra = (String) v.get(2);
		} catch (IndexOutOfBoundsException e) {
		}

		/**
		 * Receiver address
		 */
		v = tokenizer.nextCompositeElement();
		String toId = "", toQual = "", toExtra = null;
		try {
			toId = (String) v.get(0);
			toQual = (String) v.get(1);
			toExtra = (String) v.get(2);
		} catch (IndexOutOfBoundsException e) {
		}

		/**
		 * Date and time
		 */
		v = tokenizer.nextCompositeElement();
		String date = "";
		String time = "";
		try {
			date = (String) v.get(0);
			time = (String) v.get(1);
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		if (debug) {
			trace("UNB field 4 (date,time) parsed " + date + "," + time);
		}
		interchangeAttributes.addCDATA(DATE, date);
		interchangeAttributes.addCDATA(TIME, time);

		/**
		 * Control number
		 */
		interchangeAttributes.addCDATA(CONTROL, interchangeControlNumber = tokenizer
				.nextSimpleValue());
		if (debug) {
			trace("UNB field 5 (control number) parsed");
		}
		startElement(INTERCHANGE_TAG, interchangeAttributes);

		interchangeAttributes.clear();
		startElement(SENDER_TAG, interchangeAttributes);
		interchangeAttributes.addCDATA(ID_ATTRIBUTE, fromId);
		interchangeAttributes.addCDATA(QUALIFIER_ATTRIBUTE, fromQual);
		if (fromExtra != null) {
			interchangeAttributes.addCDATA("Extra", fromExtra);
		}
		startElement(ADDRESS_TAG, interchangeAttributes);
		endElement(ADDRESS_TAG);
		endElement(SENDER_TAG);

		interchangeAttributes.clear();
		startElement(RECEIVER_TAG, interchangeAttributes);
		interchangeAttributes.addCDATA(ID_ATTRIBUTE, toId);
		interchangeAttributes.addCDATA(QUALIFIER_ATTRIBUTE, toQual);
		if (toExtra != null) {
			interchangeAttributes.addCDATA(ADDRESS_EXTRA_ATTRIBUTE, toExtra);
		}
		startElement(ADDRESS_TAG, interchangeAttributes);
		endElement(ADDRESS_TAG);
		endElement(RECEIVER_TAG);

		if (debug) {
			trace("skipping tokens to the end of the UNB");
		}
		while ((token = tokenizer.nextToken()).getType() != TokenI.SEGMENT_END) {
			if (tokenizer.getElementInSegmentCount() > ELEMENTS_IN_UNB_MAXIMUM) {
				throw new EDISyntaxException("Too many ("
						+ tokenizer.getElementInSegmentCount()
						+ ") elements for a UNB. Segment terminator problem?",
						tokenizer);
			}
		}

		while (true) {
			ungExplicit = true;
			token = tokenizer.nextToken();
			if (token.getType() != TokenI.SEGMENT_START) {
				throw new EDISyntaxException(
						"Invalid beginning of UNG|UNH|UNZ segment", tokenizer);
			}
			String sType = token.getValue();
			if (sType.equals("UNG")) {
				groupCount++;
				token = parseFunctionalGroup(token);
			} else if (sType.equals("UNH")) {
				token = impliedFunctionalGroup(token);
			} else if (sType.equals("UNZ")) {
				break;
			} else {
				throw new EDISyntaxException(
						"Expected UNZ or UNG segment instead of " + sType,
						tokenizer);
			}
		}

		int n;
		if (groupCount != (n = tokenizer.nextIntValue())) {
			throw new EDISyntaxException(COUNT_UNZ, groupCount, n, tokenizer);
		}
		String s;
		if (!(s = tokenizer.nextSimpleValue()).equals(interchangeControlNumber)) {
			throw new EDISyntaxException(CONTROL_NUMBER_UNZ,
					interchangeControlNumber, s, tokenizer);
		}

		endElement(INTERCHANGE_TAG);

		TokenI lastToken = tokenizer.skipSegment();
		return (lastToken);
	}

	/**
	 * Parse Edifact group (UNG .. UNE)
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
		groupAttributes.addCDATA("GroupType", tokenizer.nextSimpleValue());

		tokenizer.nextCompositeElement();
		tokenizer.nextCompositeElement();
		tokenizer.nextCompositeElement();

		groupAttributes.addCDATA(CONTROL, groupControlNumber = tokenizer
				.nextSimpleValue());
		groupAttributes.addCDATA("StandardCode", tokenizer.nextSimpleValue());

		tokenizer.nextCompositeElement();
		startElement(GROUP_TAG, groupAttributes);
		tokenizer.skipSegment();

		while (true) {
			token = tokenizer.nextToken();
			if (token.getType() != TokenI.SEGMENT_START) {
				throw new EDISyntaxException(
						"Invalid beginning of UNH|UNE segment", tokenizer
								.getSegmentCount());
			}
			String sType = token.getValue();
			if (sType.equals("UNH")) {
				docCount++;
				token = parseDocument(token);
			} else if (sType.equals("UNE")) {
				break;
			} else {
				throw new EDISyntaxException(
						"Expected UNE or UNH segment instead of " + sType,
						tokenizer);
			}
		}

		int n;
		if (docCount != (n = tokenizer.nextIntValue())) {
			throw new EDISyntaxException(
					"Transaction set count error in UNE segment. Expected "
							+ docCount + " instead of " + n, tokenizer);
		}
		String s;
		if (!(s = tokenizer.nextSimpleValue()).equals(groupControlNumber)) {
			throw new EDISyntaxException(
					"Control number error in UNE segment. Expected "
							+ groupControlNumber + " instead of " + s,
					tokenizer);
		}

		endElement(GROUP_TAG);
		TokenI retval = tokenizer.skipSegment();
		return (retval);
	}

	/**
	 * Handle implied Edifact group (UNG .. UNE)
	 * 
	 * @param token
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception SAXException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	private TokenI impliedFunctionalGroup(TokenI token) throws SAXException,
			IOException {
		int docCount = 0;
		groupAttributes.clear();
		startElement(GROUP_TAG, groupAttributes);
		while (true) {
			if (token.getType() != TokenI.SEGMENT_START) {
				throw new EDISyntaxException(
						"Invalid beginning of UNH|UNZ segment", tokenizer
								.getSegmentCount());
			}
			String sType = token.getValue();
			if (sType.equals("UNH")) {
				docCount++;
				groupCount++;
				token = parseDocument(token);
				token = tokenizer.nextToken();
			} else if (sType.equals("UNZ")) {
				tokenizer.ungetToken();
				break;
			} else {
				throw new EDISyntaxException(
						"Expected UNE or UNZ segment instead of " + sType,
						tokenizer);
			}
		}

		endElement(GROUP_TAG);
		return (token);
	}

	/**
	 * Parse Edifact Message (UNH .. UNT)
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
		/*
		 * document control number
		 */
		String control;
		/*
		 * EDIFACT message type (called document type within this parser)
		 */
		String documentType;
		/*
		 * segment count, includes UNH and UNT
		 */
		int segCount = 2;

		documentAttributes.clear();
		documentAttributes.addCDATA(CONTROL, control = tokenizer
				.nextSimpleValue());
		LoopingState loopingState = new LoopingState("EDIFACT");
		loopingState.setDebug(debug);
		List v = tokenizer.nextCompositeElement();
		if (v != null) {
			Object obj = v.get(0);
			if (obj != null) {
				documentType = (String) obj;
				documentAttributes.addCDATA("DocType", documentType);
				if (loopingState.useLoopingStructure(documentType)) {
					documentAttributes.addCDATA("Name", loopingState
							.getDocumentName());
				}
			}
		}
		startElement(DOCUMENT_TAG, documentAttributes);

		String segmentType;
		while (!(segmentType = tokenizer.nextSegment()).equals("UNT")) {
			segCount++;
			if (loopingState.transition(segmentType)) {
				/*
				 * First close off any loops that were closed as the result of
				 * the transition
				 */
				int toClose = loopingState.closedCount();
				if (debug)
					trace("closing " + toClose + " loops");
				for (; toClose > 0; toClose--) {
					endElement(LOOP_TAG);
				}

				String s = loopingState.getLoopEntered();
				if (s.startsWith("/")) {
					/*
					 * This is the outer loop which we do not explicitly
					 * represent
					 */
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
				if (tokenizer.getElementInSegmentCount() > ELEMENTS_IN_SEGMENT_MAXIMUM) {
					throw new EDISyntaxException(
							"Too many ("
									+ tokenizer.getElementInSegmentCount()
									+ ") elements for a valid segment. Segment terminator problem?",
							tokenizer);
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
			throw new EDISyntaxException(COUNT_UNT, segCount, n, tokenizer);
		}
		String s;
		if (!(s = tokenizer.nextSimpleValue()).equals(control)) {
			throw new EDISyntaxException(CONTROL_NUMBER_UNT, control, s,
					tokenizer);
		}
		endElement(DOCUMENT_TAG);

		/*
		 * Skip over this UNT segment and return the SEGEMENT_END token
		 */
		TokenI retval = tokenizer.skipSegment();
		return (retval);
	}

	/**
	 * Preview the EDI input before attempting to tokenize it in order to
	 * discover syntactic details including segment terminator and element
	 * delimiter. Upon return, the input stream must be re-positioned so that
	 * the tokenizer can read from the beginning of the interchange.
	 * 
	 * @exception EDISyntaxException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	public void preview() throws EDISyntaxException, IOException {
		char[] buf = tokenizer.lookahead(128);

		if (!(buf[0] == 'U' && buf[1] == 'N')) {
			throw new EDISyntaxException(
					"EDIFACT interchange must begin with UN");
		}

		if (previewed) {
			throw new EDISyntaxException(
					"Internal error: EDIFACT interchange previewed more than once");
		}

		// Now we establish subDelimiter, delimiter, release, and terminator.
		// If there is a UNA segment, we get the values from that. If there
		// is no UNA, then we look to the UNB and use the defaults associated
		// with the Syntax Code found there. However, if the release char
		// in a UNA is space, then it means "not specified" and that there
		// is no release character at all (i.e., no release character
		// processing for this interchange). This is not the only
		// reasonable interpretation of the EDIFACT standards, but
		// one that is commonly used.
		//
		// So our approach will be to react to a UNA if one is there and
		// note which of the 4 attributes are established. If one or more
		// of the 4 is not established, then and only then do we shift the
		// buffer and look at the UNB to establish those attributes that
		// not yet established.
		boolean _subDelimiter = false;
		boolean _delimiter = false;
		boolean _release = false;
		boolean _terminator = false;
		boolean _terminatorSuffix = false;
		terminatorSuffix = "";
		if (buf[2] == 'A') {
			/*
			 * We do have a UNA
			 */
			subDelimiter = buf[3];
			_subDelimiter = true;
			delimiter = buf[4];
			_delimiter = true;
			terminator = buf[8];
			_terminator = true;
			if (buf[6] == ' ') {
				/*
				 * no release processing
				 */
				release = -1;
			} else {
				release = buf[6];
			}
			_release = true;

			if (_release && _subDelimiter && _delimiter && _terminator
					&& _terminatorSuffix) {
				// Don't bother to shift the UNA, we won't need to look at UNB
			} else {
				// Shift the buffer to look at the UNB.
				// This is a little tricky because we don't know exactly how
				// many bytes to shift. We need to find the first U soon after
				// the end of the UNA segment. Remember, there might be
				// whitespace chars between the terminator and the UNB. Take
				// note of this whitespace, saving it as a terminatorSuffix, so
				// that a segment could be generated with matching whitespace
				// conventions.
				int nShift = 9;
				for (int j = 9; j < 14; j++) {
					// buf[9] is the 1st char after UNA terminator
					if (Character.isLetter(buf[j])) {
						nShift = j;
						break;
					}
					terminatorSuffix += buf[j];
					// trace("...appended buf[" + j + "] to suffix");
					_terminatorSuffix = true;
				}
				for (int j = 0; j < buf.length - nShift; j++) {
					buf[j] = buf[j + nShift];
				}
				// trace("Shifted buffer " + nShift + " chars to examine UNB");
			}
		}
		if (_release && _subDelimiter && _delimiter && _terminator
				&& _terminatorSuffix) {
			// We have everything we need; don't bother looking at UNB.
		} else {
			if (buf[2] == 'B') {
				// UNB+UNOA...
				// 01234567
				if (buf[7] == 'A') {
					// Level A Character Set
					if (!_delimiter) {
						delimiter = '+';
					}
					if (buf[3] != delimiter) {
						throw new EDISyntaxException(
								"Expected data element separator after UNB segment tag");
					}
					if (!_terminator) {
						terminator = '\'';
					}
					if (!_subDelimiter) {
						subDelimiter = ':';
					}
					if (!_release) {
						release = '?';
					}
				} else if (buf[7] == 'B') {
					// Level B Character Set
					if (!_delimiter) {
						delimiter = '+';
					}
					// WRONG!
					if (buf[3] != delimiter) {
						throw new EDISyntaxException(
								"Expected data element separator after UNB segment tag");
					}
					if (!_terminator) {
						terminator = '\'';
					}
					// WRONG!
					if (!_subDelimiter) {
						subDelimiter = ':';
					}
					// WRONG!
					if (!_release) {
						release = '?';
					}
					// WRONG!
					// Assert.that(false,"UNOB: Level B Syntax not
					// implemented");
				}
				if (!_terminatorSuffix) {
					// We still have not observed a terminator suffix
					// following the first terminator in the interchange.
					// Therefore, we must scan the buffer until we see the
					// segment terminator, and then note suffix characters
					// following.
					terminatorSuffix = scanForSuffix(terminator, buf, 3);
				}
			} else {
				throw new EDISyntaxException(
						"Required UNB segment not found in EDIFACT interchange");
			}

		}
		previewed = true;
	}

	private String scanForSuffix(char _terminator, char[] buffer, int index) {
		String suffix = "";
		for (int i = index; i < buffer.length; i++) {
			if (buffer[i] == _terminator) {
				for (int j = 1; j < 3; j++) {
					i++;
					if (i < buffer.length && !Character.isLetter(buffer[i])) {
						suffix += buffer[i];
					}
				}
				break;
			}
		}
		return suffix;
	}
}
