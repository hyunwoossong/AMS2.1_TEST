/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.trx.evalleyvs.eai.message.ErrorMessages;

/**
 * Interprets EDI input as a sequence of primitive syntactic tokens.
 */
public class EDITokenizer implements ErrorMessages {

	private final static byte EXPECTING_SEGMENT = 1;

	private final static byte IN_SEGMENT = 2;

	private final static byte IN_COMPOSITE = 3;

	private boolean tokenReady;

	private Token currentToken;

	private byte state;

	int repeatCount = 0;

	private boolean repetition = false;

	/**
	 * Progress counters useful in reporting syntax errors
	 */
	private int segmentCount = 0;

	private int charCount = 0;

	private int segTokenCount = 0;

	private int segCharCount = 0;

	Reader inputReader;

	private Writer outputWriter;

	private boolean writingSuspended = false;

	private StringBuffer recording = new StringBuffer();

	private int rewindMark = 0;

	private boolean recorderOn = false;

	private boolean endOfFile = false;

	public String toString() {
		String result = "tokenizer state:";
		result += " segmentCount=" + segmentCount;
		result += " charCount=" + charCount;
		result += " segTokenCount=" + segTokenCount;
		result += " segCharCount=" + segCharCount;
		result += " currentToken=" + currentToken;
		return result;
	}

	/**
	 * Constructor for the EDITokenizer object
	 */
	public EDITokenizer(Reader source) {
		if (false && EDIReader.debug) {
			trace("Constructed a new EDITokenizer");
		}
		inputReader = source;
		outputWriter = null;
		currentToken = new Token(this);
		state = EXPECTING_SEGMENT;
		tokenReady = false;
	}

	/**
	 * Shorthand for EDIReader.trace(String)
	 */
	private void trace(String string) {
		EDIAbstractReader.trace(string);
	}

	/**
	 * Gets the reader attribute of the EDITokenizer object
	 */
	public Reader getReader() {
		return inputReader;
	}

	/**
	 * The outputWriter provides the service of copying parsed data to an output
	 * destination. This is particularly useful in splitting applications. This
	 * service is optional; setWriter(null) turns copying off, which is the
	 * default condition. In addition, copying can be suspended and then resumed
	 * via the suspendWriting() method. Note that with writing suspended, the
	 * copyToken() and similar methods are still available for manual copying of
	 * data to the output destination.
	 */
	public void setWriter(Writer writer) {
		outputWriter = writer;
	}

	/**
	 * Used in conjuction with setWriter to temporarily suspend and then resume
	 * copying parsed data to an output destination.
	 */
	public void suspendWriting(boolean b) {
		writingSuspended = b;
	}

	/**
	 * Peaks ahead to determine if nextToken() would find another token.
	 */
	public boolean hasMoreTokens() throws IOException, EDISyntaxException {
		if (!tokenReady) {
			advance();
		}
		return tokenReady;
	}

	/**
	 * Returns the next Token from the InputSource, or null if there are no more
	 * tokens.
	 */
	public TokenI nextToken() throws IOException, EDISyntaxException {
		if (!tokenReady) {
			advance();
		}
		tokenReady = false;
		return currentToken;
	}

	/**
	 * Arranges for the token most recently returned by <code>nextToken</code>
	 * to be returned again if a future call to <code>nextToken</code>.
	 */
	public void ungetToken() {
		tokenReady = true;
	}

	/**
	 * Advances to the next token. <pr>Sets tokenReady, currentToken, and state.
	 */
	private void advance() throws IOException, EDISyntaxException {
		getChar();
		switch (cClass) {
		case RELEASE:
			// Ignore this release character, but get the next
			// character and treat it as data (by falling in
			// to the following case) without regard to the class
			// that character would naturally be.
			getChar();
		case DATA:
			if (!repetition) {
				repeatCount = 0;
				if (false && EDIReader.debug) {
					trace("resetting repeatCount to 0");
				}
			}
			if (state == IN_SEGMENT) {
				segTokenCount++;
				currentToken.type = TokenI.SIMPLE;
				currentToken.value.setLength(1);
				currentToken.value.setCharAt(0, cChar);
				tokenReady = true;
				if ((repetition) && (repeatCount > 0)) {
					// do not increment the token index in this case.
					if (false && EDIReader.debug) {
						trace("leaving index at current value of "
								+ currentToken.index);
					}
				} else {
					currentToken.index++;
					if (false && EDIReader.debug) {
						trace("incrementing index to " + currentToken.index);
					}
				}
				if (scanData() == SUB_DELIMITER) {
					// We have a composite token instead of a simple one
					currentToken.type = TokenI.SUB_ELEMENT;
					currentToken.subElementIndex = 0;
					currentToken.lastSubElement = false;
					state = IN_COMPOSITE;
					if (false && EDIReader.debug) {
						trace("noted subelements within element "
								+ currentToken.index);
					}
				}
				if (false && EDIReader.debug) {
					trace("within a segment, token " + segTokenCount);
				}
			} else if (state == IN_COMPOSITE) {
				segTokenCount++;
				currentToken.type = TokenI.SUB_ELEMENT;
				currentToken.subElementIndex++;
				currentToken.value.setLength(1);
				currentToken.value.setCharAt(0, cChar);
				tokenReady = true;
				if (scanData() == SUB_DELIMITER) {
					// We have yet another subelement
					if (false && EDIReader.debug) {
						trace("noted another subelement within element "
								+ currentToken.index);
					}
				} else {
					// We hit something that marks the end of a series of
					// subelements
					state = IN_SEGMENT;
					currentToken.lastSubElement = true;
				}
				if (false && EDIReader.debug) {
					trace("within a composite, token " + segTokenCount);
				}
			} else {
				// We are at the beginning of a segment
				segmentCount++;
				segTokenCount = 1;
				segCharCount = 1;
				currentToken.type = TokenI.SEGMENT_START;
				currentToken.value.setLength(1);
				currentToken.value.setCharAt(0, cChar);
				currentToken.index = 0;
				currentToken.subElementIndex = 0;
				tokenReady = true;
				scanData(10);
				currentToken.setSegmentType(currentToken.value);
				state = IN_SEGMENT;
				if (false && EDIReader.debug) {
					trace("at the beginning of a segment, token "
							+ segTokenCount);
				}
			}
			break;
		case TERMINATOR:
			if (state == IN_COMPOSITE) {
				// return an empty subelement token, marked as last,
				// before returning the segment terminator token.
				currentToken.subElementIndex++;
				currentToken.lastSubElement = true;
				currentToken.type = TokenI.SUB_EMPTY;
				currentToken.value.setLength(0);
				ungetChar();
				// change state so that next time
				// we will go down a diffent path.
				state = IN_SEGMENT;
				if (false && EDIReader.debug) {
					trace("noted empty subelement before terminator "
							+ currentToken.index);
				}
			} else {
				currentToken.type = TokenI.SEGMENT_END;
				state = EXPECTING_SEGMENT;
				scanTerminatorSuffix();
				currentToken.subElementIndex = 0;
			}
			tokenReady = true;
			break;
		case DELIMITER:
			if (state == IN_COMPOSITE) {
				// return an empty subelement token, marked as last,
				// before returning the delimiter token.
				currentToken.subElementIndex++;
				currentToken.type = TokenI.SUB_EMPTY;
				state = IN_SEGMENT;
				if (false && EDIReader.debug) {
					trace("noted empty subelement before delimiter "
							+ currentToken.index);
				}
			} else {
				if (false && EDIReader.debug) {
					trace("delimiter completes element " + currentToken.index);
				}
				segTokenCount++;
				currentToken.index++;
				currentToken.subElementIndex = 0;
				currentToken.type = TokenI.EMPTY;
			}
			currentToken.lastSubElement = true;
			currentToken.value.setLength(0);
			tokenReady = true;
			break;
		case SUB_DELIMITER:
			if (state == IN_SEGMENT) {
				currentToken.index++;
				state = IN_COMPOSITE;
				currentToken.subElementIndex = 0;
			} else if (state == IN_COMPOSITE) {
				currentToken.subElementIndex++;
			}
			currentToken.lastSubElement = false;
			currentToken.type = TokenI.SUB_EMPTY;
			currentToken.value.setLength(0);
			tokenReady = true;
			if (false && EDIReader.debug) {
				trace("noted empty subelement within element "
						+ currentToken.index);
			}
			break;
		case EOF:
			currentToken.type = TokenI.END_OF_DATA;
			tokenReady = false;
			break;
		}
	}

	/**
	 * These data members all are used within getChar() as it deals with one
	 * character at a time.
	 */
	private char cChar;

	private char delimiter = '+';

	private char subDelimiter = ':';

	// release is an int instead of a char so that it can hold
	// a char value (as a positive int) or an indicator of
	// "no release char" (an int value of -1).
	private int release = -1;

	// escape is an int instead of a char so that it can hold
	// a char value (as a positive int) or an indicator of
	// "no escape sequences" (an int value of -1).
	private int escape = -1;

	// repetitionSeparator is an int instead of a char so that it can hold
	// a char value (as a positive int) or an indicator of
	// "no repeating fields" (an int value of -1)
	private int repetitionSeparator = -1;

	private char terminator = '.';

	private static String whitespace = "\n\r \\";

	private boolean unGot = false;

	// cClass indicates the 'class' of cChar,
	// one of the code values following.
	private byte cClass;

	private final static byte DATA = 0;

	private final static byte DELIMITER = 1;

	private final static byte SUB_DELIMITER = 2;

	private final static byte RELEASE = 3;

	private final static byte TERMINATOR = 4;

	private final static byte REPEAT_DELIMITER = 5;

	private final static byte EOF = 6;

	private char[] buffer = new char[1000];

	private int bufferUsed = 0;

	private int bufferIndex = 0;

	/**
	 * Gets the next character of input. <pr>Sets cChar, cClass
	 */
	private void getChar() throws IOException, EDISyntaxException {
		if (unGot) {
			// The current character has been "put back" with ungetChar()
			// after having been seen with getChar(). Therefore, this call
			// to getChar() can simply reget the current character.
			unGot = false;
			charCount++;
			segCharCount++;
			return;
		}

		// Read a fresh character from the input source.
		// But first copy the current one to an outputWriter
		// or the recorder if necessary.
		if (outputWriter != null) {
			// We do have an outputWriter wanting data, but do we have
			// a current character to write? And make sure writing is
			// not suspended.
			if ((!endOfFile) && (!writingSuspended)) {
				outputWriter.write(cChar);
			}
		}
		if (recorderOn) {
			recording.append(cChar);
			if (false && EDIReader.debug) {
				trace("recording " + cChar + " at " + recording.length());
			}
		}

		if (bufferIndex >= bufferUsed) {
			// It's time to refill the buffer
			while ((bufferUsed = inputReader.read(buffer)) == 0) {
			}
			bufferIndex = 0;
			if (bufferUsed < 0) {
				endOfFile = true;
			}
		}

		if (endOfFile) {
			if (cClass == EOF) {
				throw new EDISyntaxException(INTERNAL_ERROR_MULTIPLE_EOFS, this);
			}
			cClass = EOF;
			if (false && EDIReader.debug) {
				trace("end-of-file encountered");
			}
		} else {
			cChar = buffer[bufferIndex++];
			if (cChar == delimiter) {
				cClass = DELIMITER;
			} else if (cChar == subDelimiter) {
				cClass = SUB_DELIMITER;
			} else if (cChar == release) {
				cClass = RELEASE;
			} else if (cChar == terminator) {
				cClass = TERMINATOR;
			} else if (cChar == repetitionSeparator) {
				cClass = REPEAT_DELIMITER;
			} else {
				cClass = DATA;
			}
			if (false && EDIReader.debug) {
				trace("getChar() returns new char |" + cChar + "|");
			}
		}
		charCount++;
		segCharCount++;
	}

	/**
	 * Arranges for getChar() to see the current char again the next time it is
	 * called, in effect "putting back" that char to be seen again.
	 */
	private void ungetChar() {
		unGot = true;
		charCount--;
		segCharCount--;
	}

	/**
	 * Scans a series of data characters up to the first character other than a
	 * data character.
	 * :::
	 * Each character is appended to the value of the current token. Upon
	 * return, cChar and cClass are left referencing the character after the
	 * last character of data; in other words, getChar() will have been called
	 * seeing something other than a character of data. Release sequences are
	 * handled within this method.
	 * 
	 * @param limit
	 *            maximum number of data characters allowed in the element
	 * @return true iff the non-data character was the subDelimiter
	 * @exception IOException
	 *                problem reading EDI input
	 * @exception EDISyntaxException
	 *                specific syntax error in parsed EDI data
	 */
	private int scanData(int limit) throws IOException, EDISyntaxException{
		loop: while (true) {
			getChar();
			switch (cClass) {
			case RELEASE:
				// Ignore this release character, but get the next
				// character and treat it as data (by falling in
				// to the following case) without regard to the class
				// that character would naturally be.
				getChar();
			case DATA:
				if (--limit == 0) {
					throw new EDISyntaxException(ELEMENT_TOO_LONG, this);
				}
				currentToken.value.append(cChar);
				break;
			case SUB_DELIMITER:
				break loop;
			case REPEAT_DELIMITER:
				repeatCount++;
				repetition = true;
				if (false && EDIReader.debug) {
					trace("set repetition to true and incremented repeatCount to "
							+ repeatCount);
				}
				break loop;
			case TERMINATOR:
				ungetChar();
			// fall into the default logic below
			default:
				if (repetition) {
					repeatCount++;
					repetition = false;
					if (false && EDIReader.debug) {
						trace("reset repetition to false but incremented repeatCount to "
								+ repeatCount);
					}
				}
				break loop;
			}
		}
		return cClass;
	}

	/**
	 * Equivalent to scanData(infinite)
	 */
	private int scanData() throws IOException, EDISyntaxException {
		return scanData(0);
	}

	/**
	 * Scans over a series of characters that, after a segment terminator, are
	 * considered to be ignorable whitespace. This allows segments with formal
	 * segment terminator characters to be followed be line-oriented characters
	 * (line feeds and carriage returns).
	 * 
	 * @exception IOException
	 *                Description of the Exception
	 */
	void scanTerminatorSuffix() throws IOException, EDISyntaxException {
		while (true) {
			getChar();
			if (false && EDIReader.debug) {
				trace("scanTerminatorSuffix: getChar() gets " + cChar);
			}
			if ((cClass == EOF) || (whitespace.indexOf(cChar) == -1)) {
				break;
			}
		}
		ungetChar();
	}

	public TokenI expectSegment(TokenI token, String segmentType)
			throws SAXException, IOException {
		return token;
	}

	/**
	 * Returns the value of the next token, expected to be of type SIMPLE or
	 * EMPTY. If <code>required</code> is true, then it may not be EMPTY. A
	 * syntax exception is thrown for any other types.
	 * 
	 * @param required
	 *            an EMPTY token is not allowed
	 * @return String value of the token
	 * @exception SAXException
	 *                unexpected tokens
	 * @exception IOException
	 */
	public String nextSimpleValue(boolean required) throws SAXException,
			IOException {
		TokenI t = nextToken();
		switch (t.getType()) {
		case TokenI.EMPTY:
		case TokenI.SEGMENT_END:
			if (required) {
				throw new EDISyntaxException("Mandatory element missing in "
						+ t.getSegmentType() + " segment", this);
			}
			break;
		case TokenI.SIMPLE:
			break;
		default:
			throw new EDISyntaxException(EXPECTED_SIMPLE_TOKEN, this);
		}
		return t.getValue();
	}

	/**
	 * Equivalent to <code>nextSimpleValue(true)</code>
	 * 
	 * @return String value of the token
	 * @exception SAXException
	 *                unexpected tokens
	 * @exception IOException
	 */
	public String nextSimpleValue() throws SAXException, IOException {
		return nextSimpleValue(true);
	}

	/**
	 * Gets the next token expecting it to be a digit sequence.
	 * :::
	 * 
	 * @return value integer value implied by digits
	 * @exception SAXException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 * @throws EDISyntaxException
	 *             if contains non-dgits
	 * @throws EDISyntaxExceptoin
	 *             if empty
	 */
	public int nextIntValue() throws SAXException, IOException {
		int i;
		try {
			i = Integer.parseInt(nextSimpleValue());
		} catch (NumberFormatException e) {
			throw new EDISyntaxException(DIGITS_ONLY, this);
		}
		return i;
	}

	/**
	 * Parses the next token expecting to find a composite element - one
	 * composed of subelements separated by the subElementDelimiter.
	 * 
	 * @return subelements as a Vector of Strings
	 * @exception IOException
	 *                Description of the Exception
	 * @exception EDISyntaxException
	 *                Description of the Exception
	 */
	public List nextCompositeElement() throws IOException, EDISyntaxException {
		List result = new ArrayList();
		loop: while (true) {
			TokenI t = nextToken();
			switch (t.getType()) {
			case TokenI.SUB_ELEMENT:
				// add this token's value to the vector and
				// others that follow it
				result.add(t.getValue());
				if (t.isLast()) {
					break loop;
				}
				break;
			case TokenI.SUB_EMPTY:
				result.add("");
				if (t.isLast()) {
					break loop;
				}
				break;
			case TokenI.SIMPLE:
				// We saw a simple token teminated by a normal
				// element delimiter, not the subElement delimiter.
				// Treat this as a composite element with only one
				// value.
				result.add(t.getValue());
				break loop;
			case TokenI.EMPTY:
			// An empty token terminated by
			// a normal element delimiter, segment end, etc.
			// Treat this as a composite element with no values
			// by returning an empty List.
			case TokenI.SEGMENT_END:
				// Note that the above case falls into this one.
				break loop;
			default:
				throw new EDISyntaxException(INVALID_COMPOSITE, this);
			}
		}
		return result;
	}

	/**
	 * Skips over tokens until an END_SEGMENT token is reached, marking the end
	 * of the current segment. This Tokenizer is therefore positioned so that
	 * the next call to getToken() sees the first token after the end of this
	 * segment.
	 * 
	 * @return token SEGMENT_END
	 * @exception SAXException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	public TokenI skipSegment() throws SAXException, IOException {
		TokenI t;
		int i = 0;
		while (true) {
			t = nextToken();
			int tokenType = t.getType();
			if ((tokenType == TokenI.SEGMENT_END)
					|| (tokenType == TokenI.END_OF_DATA)) {
				break;
			}
			if (++i > 30) {
				throw new EDISyntaxException("Too many fields in "
						+ t.getSegmentType() + " segment", this);
			}
		}
		return t;
	}

	/**
	 * Skips over tokens until the beginning of a new segment is encourntered.
	 * 
	 * @return segType String containing value of leading field
	 * @exception SAXException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	public String nextSegment() throws SAXException, IOException {
		TokenI t;
		int i = 0;
		while (true) {
			t = nextToken();
			int tokenType = t.getType();
			if (tokenType == TokenI.SEGMENT_START) {
				break;
			}
			if (tokenType == TokenI.END_OF_DATA) {
				throw new EDISyntaxException(UNEXPECTED_EOF, this);
			}
			if (++i > 30) {
				throw new EDISyntaxException("Too many fields for "
						+ t.getSegmentType()
						+ " segment (Segment terminator problem?)", this);
			}
		}
		return t.getSegmentType();
	}

	/**
	 * Look ahead into the source of input chars and retun the next n chars to
	 * be seen, without disturbing the normal operation of getChar().
	 * 
	 * @param n
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception IOException
	 *                Description of the Exception
	 */
	public char[] lookahead(int n) throws IOException, EDISyntaxException {
		if (false && EDIReader.debug) {
			trace("EDITokeninzer.lookahead(" + n + ")");
		}
		char[] rval = new char[n];
		// The 1st char is grabbed using the tokenizer's built-in
		// getChar() / ungetChar() mechanism. This allows things to work
		// properly whether or not the next char has already been gotten.
		getChar();
		rval[0] = cChar;
		ungetChar();

		if ((bufferUsed - bufferIndex) < (n - 1)) {
			// There are not enough chars in the current buffer to satisfy
			// lookahead request. This could be the result of using lookahead
			// other than when just starting to examine an interchange, or
			// possibly the result of an unexpectedly small amount of input
			// data. In either case, retuning null indicates that
			// the request could not be honored.
			rval = null;
		} else {
			int j = 1;
			for (int i = bufferIndex; i < bufferIndex + n - 1; i++) {
				rval[j++] = buffer[i];
			}
		}

		if (false && EDIReader.debug) {
			if (rval == null) {
				trace("EDITokenizer.lookahead returns null");
			} else {
				trace("EDITokenizer.lookahead returns " + String.valueOf(rval));
			}
		}
		return rval;
	}

	/**
	 * Copy the current token to the outputWriter, if there is one. Normally,
	 * copying to the outputWriter happens as characters are read. Sometimes,
	 * however, it is desirable to turn off this "automatic" copying for portion
	 * of the input and manually cause data to be written via outputWriter.
	 * copyToken() writes the currentToken to the outputWriter.
	 */
	public void copyToken() {
		if (outputWriter != null) {
			try {
				outputWriter.write(currentToken.getValue());
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Turn the recorder on (true) or off (false).
	 * 
	 * @param b
	 *            The new recorder value
	 */
	public void setRecorder(boolean b) {
		recorderOn = b;
		if (false && EDIReader.debug) {
			if (b) {
				trace("recorder turned on");
			} else {
				trace("recorder turned off");
			}
		}
	}

	/**
	 * Return the recording.
	 * 
	 * @return The recording value
	 */
	public String getRecording() {
		return recording.toString();
	}

	/**
	 * Establish an interesting point in the current recording to which the
	 * recording may be later rewound.
	 */
	public void setRewindMark() {
		rewindMark = recording.length();
		if (false && EDIReader.debug) {
			trace("setRewind mark at " + rewindMark);
		}
	}

	/**
	 * Rewind the recording to the beginning.
	 */
	public void rewind() {
		if (false && EDIReader.debug) {
			trace("rewind to beginning from " + recording.length());
		}
		rewindMark = 0;
		recording.setLength(rewindMark);
	}

	/**
	 * Rewind the recording to a point established by the most recent call to
	 * setRewind(). If no such call has been made, then rewind to the beginning.
	 */
	public void rewindToMark() {
		if (false && EDIReader.debug) {
			trace("rewind from " + recording.length() + " back to "
					+ rewindMark);
		}
		recording.setLength(rewindMark);
	}

	public void copy(String s) {
		if (outputWriter != null) {
			try {
				outputWriter.write((s == null) ? "(null)" : s);
			} catch (IOException e) {
			}
		}
	}

	public void copy(char c) {
		if (outputWriter != null) {
			try {
				outputWriter.write(c);
			} catch (IOException e) {
			}
		}
	}

	public void copyDelimiter() {
		copy(delimiter);
	}

	public void copyTerminator() {
		copy(terminator);
	}

	public int getCharCount() {
		return charCount;
	}

	public int getSegmentCharCount() {
		return segCharCount;
	}

	/**
	 * Implementation of TokenI as an inner class.
	 */
	private class Token implements TokenI {

		byte type = UNKNOWN;

		int index = 0;

		int subElementIndex = 0;

		boolean lastSubElement = false;

		private String segmentType = "";

		StringBuffer value = new StringBuffer();

		private final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9' };

		EDITokenizer tokenizer;

		/**
		 * Constructor for the Token object
		 */
		public Token(EDITokenizer tokenizer) {
			this.tokenizer = tokenizer;
		}

		/**
		 * Gets the type attribute of the Token object
		 * 
		 * @return The type value
		 */
		public byte getType() {
			return type;
		}

		/**
		 * Sets the type attribute of the Token object
		 * 
		 * @param t
		 *            The new type value
		 */
		void setType(byte t) {
			type = t;
		}

		/**
		 * Gets the typeString attribute of the Token object
		 * 
		 * @return The typeString value
		 */
		public String getTypeString() {
			String result;
			switch (type) {
			case UNKNOWN:
				result = "Unknown";
				break;
			case SEGMENT_START:
				result = "SegmentStart";
				break;
			case SIMPLE:
				result = "Simple";
				break;
			case SUB_ELEMENT:
				result = "SubElement";
				break;
			case EMPTY:
				result = "Empty";
				break;
			case SUB_EMPTY:
				result = "EmptySubElement";
				break;
			case COMPOSITE:
				result = "Composite";
				break;
			case SEGMENT_END:
				result = "SegmentEnd";
				break;
			case END_OF_DATA:
				result = "EndOfData";
				break;
			default:
				result = "Invalid code value";
				break;
			}
			result = result + "(" + type + ")";
			return result;
		}

		/**
		 * Returns true for the first subelement in a sequence of subelements.
		 * 
		 * @return The first value
		 */
		public boolean isFirst() {
			return (subElementIndex == 0);
		}

		/**
		 * Returns true if this is the last sublement in a subelement sequence.
		 * 
		 * @return The last value
		 */
		public boolean isLast() {
			return lastSubElement;
		}

		/**
		 * Gets the 0-origin sequential position of this token within the
		 * segment.
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * Gets the 0-origian sequencial position of a subelement within a
		 * series of subelements.
		 */
		public int getSubIndex() {
			return subElementIndex;
		}

		/**
		 * For a SIMPLE token that is part of a repeating sequnce, gets the
		 * ordinal position of the token within the sequence. Otherwise returns
		 * 0.
		 */
		public int getRepeatCount() {
			return repeatCount;
		}

		/**
		 * Gets the data value of the token as a String.
		 */
		public String getValue() {
			return value.toString();
		}

		/**
		 * Gets the data value of the token as a char array.
		 * 
		 * @return The valueChars value
		 */
		public char[] getValueChars() {
			char[] ca = new char[value.length()];
			value.getChars(0, value.length(), ca, 0);
			return ca;
		}

		/**
		 * Description of the Method
		 * 
		 * @param v
		 *            Description of the Parameter
		 * @return Description of the Return Value
		 */
		public boolean valueEquals(String v) {
			if (true) {
				String s = value.toString();
				if (s.equals(v)) {
					return true;
				}
				return false;
			}
			if (v.length() != value.length()) {
				return false;
			}
			for (int i = 0; i < v.length(); i++) {
				if (v.charAt(i) != value.charAt(i)) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Gets the segmentType attribute of the Token object
		 * 
		 * @return The segmentType value
		 */
		public String getSegmentType() {
			return segmentType;
		}

		public void setSegmentType(StringBuffer sb) throws EDISyntaxException {
			segmentType = new String(sb);
			if ("".equals(segmentType)) {
				throw new EDISyntaxException(
						ErrorMessages.INVALID_BEGINNING_OF_SEGMENT, tokenizer);
			}
		}

		/**
		 * Gets the subTokens attribute of the Token object
		 * 
		 * @return The subTokens value
		 */
		public TokenI[] getSubTokens() {
			return null;
		}

		/**
		 * Description of the Method
		 * 
		 * @return Description of the Return Value
		 */
		public String toString() {
			String result = "Token type=" + getTypeString() + " value="
					+ getValue() + " index=" + getIndex() + " segement="
					+ getSegmentType();
			return result;
		}

		/**
		 * Gets the elementId of the Token.
		 * 
		 * @return The elementId value
		 */
		public String getElementId() {
			int n = getIndex();
			String rval;
			if (n < 10) {
				rval = getSegmentType() + "0" + n;
			} else {
				rval = getSegmentType() + n;
			}

			return rval;
		}
	}

	/**
	 * Sets the delimiter attribute of the EDITokenizer object
	 * 
	 * @param d
	 *            The new delimiter value
	 */
	public void setDelimiter(char d) {
		delimiter = d;
	}

	/**
	 * Sets the subDelimiter attribute of the EDITokenizer object
	 * 
	 * @param sd
	 *            The new subDelimiter value
	 */
	public void setSubDelimiter(char sd) {
		subDelimiter = sd;
	}

	/**
	 * Sets the release character
	 * 
	 * @param e
	 *            The new release value
	 */
	public void setRelease(int e) {
		release = e;
	}

	/**
	 * Sets the character used to begin and end escape sequences.
	 * 
	 * @param e
	 *            The new escape value
	 */
	public void setEscape(int e) {
		escape = e;
	}

	/**
	 * Sets the character used to delimit repeating fields.
	 * 
	 * @param e
	 *            The new value
	 */
	public void setRepetitionSeparator(int e) {
		repetitionSeparator = e;
	}

	/**
	 * Sets the terminator attribute of the EDITokenizer object
	 * 
	 * @param d
	 *            The new terminator value
	 */
	public void setTerminator(char d) {
		terminator = d;
	}

	public char getTerminator() {
		return terminator;
	}

	/**
	 * Gets the count of segments that have been read or partially read.
	 * 
	 * @return The segmentCount value
	 */
	public int getSegmentCount() {
		return segmentCount;
	}

	/**
	 * Gets the elementInSegmentCount attribute of the EDITokenizer object
	 * 
	 * @return The elementInSegmentCount value
	 */
	public int getElementInSegmentCount() {
		return segTokenCount;
	}

	/**
	 * Gets the whitespace attribute of the EDITokenizer class
	 * 
	 * @return The whitespace value
	 */
	public static String getWhitespace() {
		return whitespace;
	}
}
