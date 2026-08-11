/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.trx.evalleyvs.eai.util.CommonUtil;


/**
 * An adaptor for XMLReader providing default implementations of several methods
 * to simplify each of the EDIReader classes that have XMLReader as an ancestor.
 */
public abstract class EDIAbstractReader implements XMLReader {

	/**
	 * The ContentHandler for this XMLReader
	 */
	protected ContentHandler contentHandler;

	/**
	 * The tokenizer used by this EDIAbstractReader
	 */
	protected EDITokenizer tokenizer;

	protected ErrorHandler errorHandler;

	protected EntityResolver entityResolver;

	/**
	 * Character marking the boundary between fields
	 */
	protected char delimiter;

	/**
	 * Character marking the boundary between sub-fields
	 */
	protected char subDelimiter;

	/**
	 * Character marking the boundary between sub-sub-fields
	 */
	protected char subSubDelimiter;

	/**
	 * Character marking the boundary between repeating fields
	 */
	protected char repetitionSeparator;

	/**
	 * Character marking the boundary between segments
	 */
	protected char terminator;

	/**
	 * The byte value used as a release or escape character.
	 */
	protected int release;

	/**
	 * Whitespace characters observed to follow the formal segment terminator.
	 */
	protected String terminatorSuffix;

	/**
	 * Where the functional acknowledgements are (optionally) written.
	 */
	protected Writer ackStream;

	/**
	 * Description of the Field
	 */
	protected Writer copyWriter = null;

	/**
	 * May contain a copy of the initial segment of the interchange.
	 */
	protected String firstSegment;

	/**
	 * An empty list of XML attributes to be passed to a ContentHanlder as
	 * needed.
	 */
	protected EDIAttributes emptyAttrList = new EDIAttributes();

	/**
	 * XML attributes relating to the EDI interchange
	 */
	protected EDIAttributes interchangeAttributes = new EDIAttributes();

	/**
	 * XML attributes relating to the EDI structure that ANSI X12 calls a
	 * functional group. In EDIFACT, this corresponds to the UNG/UNE structure.
	 */
	protected EDIAttributes groupAttributes = new EDIAttributes();

	/**
	 * XML attributes relating to the document. In ANSI X12 terminology this
	 * would be the Transaction Set (ST/SE). In EDIFACT, it would be a Message
	 * (UNH/UNT)..
	 */
	protected EDIAttributes documentAttributes = new EDIAttributes();

	/**
	 * Gets the character marking the boundary between segments
	 * 
	 * @return The terminator value
	 */
	public char getTerminator() {
		return terminator;
	}

	/**
	 * Gets the short String of 'whitespace' characters that follows the
	 * terminator.
	 * 
	 * @return The terminator value
	 */
	public String getTerminatorSuffix() {
		return terminatorSuffix;
	}

	/**
	 * Gets the character marking the boundary between fields
	 * 
	 * @return The delimiter value
	 */
	public char getDelimiter() {
		return delimiter;
	}

	/**
	 * Gets the character marking the boundary between sub-fields. Subfields may
	 * be called by different names in different EDI standards.
	 * 
	 * @return The subDelimiter value
	 */
	public char getSubDelimiter() {
		return subDelimiter;
	}

	/**
	 * Gets the character used in release/escape sequences.
	 * Exactly how this character is used may differ between standards.
	 * In ANSI, there is no release mechanism. When no release character
	 * is available, the int value -1 is returned, otherwise a char
	 * value is returned via the int.
	 * 
	 * @return The release char value or -1 if none
	 */
	public int getRelease() {
		return release;
	}

	/**
	 * Gets the character marking the boundary between sub-sub-fields.
	 * Sub-sub-fields are not used in ANSI or EDIFACT, but appear in HL7.
	 * 
	 * @return The subSubDelimiter value
	 */
	public char getSubSubDelimiter() {
		return subSubDelimiter;
	}

	/**
	 * Gets the character marking the boundary between repeating fields.
	 * 
	 * @return The repetitionSeparator value
	 */
	public char getRepetitionSeparator() {
		return repetitionSeparator;
	}

	/**
	 * Gets the tokenizer
	 */
	public EDITokenizer getTokenizer() {
		return tokenizer;
	}

	/**
	 * Sets the tokenizer
	 */
	public void setTokenizer(EDITokenizer t) {
		tokenizer = t;
		if (EDIReader.debug) {
			trace("EDIAbstractReader.setTokenizer("
					+ ((t == null) ? "null" : "non-null") + ")");
		}
	}

	/**
	 * Sets the copyWriter
	 */
	public void setCopyWriter(Writer writer) {
		// Assert.that(tokenizer!=null);
		if (tokenizer != null) {
			tokenizer.setWriter(writer);
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param source
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception IOException
	 *                Description of the Exception
	 */
	protected static Reader createReader(InputSource source) throws IOException {
		Reader inputReader;
		if (source == null) {
			throw new IOException("createReader called with null InputSource");
		}
		// first try to establish inputReader from the InputSource's
		// CharacterStream
		inputReader = source.getCharacterStream();
		if (inputReader == null) {
			InputStream inputStream = source.getByteStream();
			if (inputStream != null) {
				// establsh inputReader from a ByteStream
				inputReader = new InputStreamReader(inputStream);
			} else {
				String systemId = source.getSystemId();
				if (systemId != null) {
					// try to establish inputReader using the SystemId
					if (systemId.startsWith("file:")) {
						// systemId names a file
						inputReader = new FileReader(systemId.substring(5));
					} else {
						// some kind of URL not yet supported
						throw new IOException("InputSource using SystemId ("
								+ systemId + ") not yet supported");
					}
				} else {
					// getCharacterStream(), getByteStream(), and
					// getSystemId() all return null
					throw new IOException(
							"Cannot get ByteStream, CharacterStream, or SystemId from EDI InputSource");
				}
			}
		}

		return inputReader;
	}

	/**
	 * Prepare the parser for its parse method to be called. This involves
	 * previewing some of the interchange to discover syntactic details, and
	 * making sure a tokenizer is in place. The preview method, of course,
	 * varies with each EDI standard.
	 * 
	 * @param source
	 *            provides read access to the EDI data
	 * @exception EDISyntaxException
	 * @exception IOException
	 */
	protected void parseSetup(InputSource source) throws EDISyntaxException,
			IOException {
		if (EDIReader.debug) {
			trace("EDIAbstractReader.parseSetup()");
		}
		Reader inputReader = createReader(source);

		if (tokenizer == null) {
			setTokenizer(new EDITokenizer(inputReader));
			if (EDIReader.debug) {
				trace("Constructed new tokenizer because this reader did not have one");
			}
		} else if (inputReader != tokenizer.getReader()) {
			setTokenizer(new EDITokenizer(inputReader));
			if (EDIReader.debug) {
				trace("Constructed new tokenizer because this reader has a different inputReader");
			}
		} else {
			if (EDIReader.debug) {
				trace("Reusing existing tokenizer");
			}
		}

		if (!previewed) {
			if (EDIReader.debug) {
				trace("EDIAbstractReader: not yet previewed");
			}
			preview();
			previewed = true;
		}
		if (copyWriter != null) {
			tokenizer.setWriter(copyWriter);
		}
		if (EDIReader.debug) {
			trace("parseSetup completed");
		}
	}

	/**
	 * Preview the EDI interchange to discover syntactic details that will be
	 * useful to know before the actual parse method is called.
	 * 
	 * @exception EDISyntaxException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	public abstract void preview() throws EDISyntaxException, IOException;

	/**
	 * Description of the Field
	 */
	protected boolean previewed = false;

	/**
	 * Indicate that functional acknowledgments are to be generated by
	 * designating a Writer. This method should be called before calling parse()
	 * if acknowledgments are desired.
	 * 
	 * @param s
	 *            The new acknowledgment value
	 */
	public void setAcknowledgment(Writer s) {
		ackStream = s;
	}

	/**
	 * Sets the locale attribute of the EDIReader object
	 * 
	 * @param locale
	 *            The new locale value
	 * @exception SAXException
	 *                Description of the Exception
	 */
	public void setLocale(Locale locale) throws SAXException {
		throw new SAXNotSupportedException("setLocale not supported");
	}

	/**
	 * Sets the entityResolver attribute of the EDIReader object
	 * 
	 * @param resolver
	 *            The new entityResolver value
	 */
	public void setEntityResolver(EntityResolver resolver) {
		entityResolver = resolver;
	}

	/**
	 * Sets the dTDHandler attribute of the EDIReader object
	 * 
	 * @param handler
	 *            The new dTDHandler value
	 */
	public void setDTDHandler(DTDHandler handler) {
	}

	/**
	 * Sets the errorHandler attribute of the EDIReader object
	 * 
	 * @param handler
	 *            The new errorHandler value
	 */
	public void setErrorHandler(ErrorHandler handler) {
		errorHandler = handler;
	}

	/**
	 * Parse the EDI interchange. Each subclass must override this method.
	 * 
	 * @param systemId
	 *            Description of the Parameter
	 * @exception SAXException
	 *                Description of the Exception
	 * @exception IOException
	 *                Description of the Exception
	 */
	public void parse(String systemId) throws SAXException, IOException {
		throw new SAXException("parse(systemId) not supported");
	}

	/**
	 * Sets the contentHandler attribute of the EDIReader object
	 * 
	 * @param handler
	 *            The new contentHandler value
	 */
	public void setContentHandler(ContentHandler handler) {
		contentHandler = handler;
	}

	/**
	 * Gets the contentHandler attribute of the EDIReader object
	 * 
	 * @return The contentHandler value
	 */
	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	/**
	 * Sets the feature attribute of the EDIReader object
	 * 
	 * @param name
	 *            The new feature value
	 * @param value
	 *            The new feature value
	 * @exception SAXNotRecognizedException
	 *                Description of the Exception
	 * @exception SAXNotSupportedException
	 *                Description of the Exception
	 */
	public void setFeature(String name, boolean value)
			throws SAXNotRecognizedException, SAXNotSupportedException {
	}

	/**
	 * Gets the feature attribute of the EDIReader object
	 * 
	 * @param name
	 *            Description of the Parameter
	 * @return The feature value
	 * @exception SAXNotRecognizedException
	 *                Description of the Exception
	 * @exception SAXNotSupportedException
	 *                Description of the Exception
	 */
	public boolean getFeature(String name) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		throw new SAXNotSupportedException("Not yet implemented");
	}

	/**
	 * Sets the property attribute of the EDIReader object
	 * 
	 * @param name
	 *            The new property value
	 * @param value
	 *            The new property value
	 * @exception SAXNotRecognizedException
	 *                Description of the Exception
	 * @exception SAXNotSupportedException
	 *                Description of the Exception
	 */
	public void setProperty(String name, Object value)
			throws SAXNotRecognizedException, SAXNotSupportedException {
	}

	/**
	 * Gets the property attribute of the EDIReader object
	 * 
	 * @param name
	 *            Description of the Parameter
	 * @return The property value
	 * @exception SAXNotRecognizedException
	 *                Description of the Exception
	 * @exception SAXNotSupportedException
	 *                Description of the Exception
	 */
	public Object getProperty(String name) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		throw new SAXNotSupportedException("Not yet implemented");
	}

	/**
	 * Gets the errorHandler attribute of the EDIReader object
	 * 
	 * @return The errorHandler value
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Gets the dTDHandler attribute of the EDIReader object
	 * 
	 * @return The dTDHandler value
	 */
	public DTDHandler getDTDHandler() {
		return null;
	}

	/**
	 * Gets the entityResolver attribute of the EDIReader object
	 * 
	 * @return The entityResolver value
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	public int getCharCount() {
		if (tokenizer == null) {
			return 0;
		}
		return tokenizer.getCharCount();
	}

	public int getSegmentCharCount() {
		if (tokenizer == null) {
			return 0;
		}
		return tokenizer.getSegmentCharCount();
	}

	/**
	 * Write a message to a diagnostic trace stream.
	 */
	protected static void trace(String msg) {
		CommonUtil.writelog(msg);
		System.err.println(msg);
	}

	/**
	 * Write a message to a diagnostic trace stream.
	 */
	protected static void trace(Exception e) {
		CommonUtil.writelog(e.toString());
		System.err.println(e.toString());
	}

	public String toString() {
		String summary;
		String lineBreak = System.getProperty("line.separator");
		summary = lineBreak + "EDIReader summary:" + lineBreak + " class: "
				+ getClass().getName() + lineBreak + " delimiter: "
				+ getDelimiter() + lineBreak + " subDelimiter: "
				+ getSubDelimiter() + lineBreak + " subSubDelimiter: "
				+ getSubSubDelimiter() + lineBreak + " repetitionSeparatorx: "
				+ getRepetitionSeparator() + lineBreak + " terminator: "
				+ getTerminator() + lineBreak + " terminatorSuffix: "
				+ getTerminatorSuffix() + lineBreak + " charCount: "
				+ getCharCount() + lineBreak + " segmentCharCount: "
				+ getSegmentCharCount() + lineBreak;
		return summary;
	}
}
