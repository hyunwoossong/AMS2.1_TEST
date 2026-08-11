/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

/**
 *  A token noted by EDITokenizer.
 */
public interface TokenI {
	/**
	 *  getType() constant for an invalid token.
	 */
	public final byte UNKNOWN = 0;
	/**
	 *  getType() constant for the first token in a segment
	 */
	public final byte SEGMENT_START = 1;
	/**
	 *  getType() constant for a regular element of a segment. Not the first
	 *  element nor an empty element nor an element composed of subelements.
	 */
	public final byte SIMPLE = 2;
	/**
	 *  getType() constant for the first token in a segment.
	 */
	public final byte EMPTY = 3;
	/**
	 *  getType() constant for an empty element.
	 */
	public final byte COMPOSITE = 4;
	/**
	 *  getType() constant for a non-empty subelement.
	 */
	public final byte SUB_ELEMENT = 5;
	/**
	 *  getType() constant for an empty subelement.
	 */
	public final byte SUB_EMPTY = 6;
	/**
	 *  getType() constant for the end of a segment.
	 */
	public final byte SEGMENT_END = 7;
	/**
	 *  getType() constant for the end of input data to parse.
	 */
	public final byte END_OF_DATA = 8;


	/**
	 * Gets the type of the token.
	 *
	 * @return    SEGMENT_START, SIMPLE, SUB_ELEMENT, ...
	 */
	public byte getType();


	/**
	 *  Is true for the first subelement in a series of subelements.
	 *
	 * @return    boolean
	 */
	public boolean isFirst();


	/**
	 *  Is true for the last subelemnt in a series of subelements.
	 *
	 * @return    boolean
	 */
	public boolean isLast();


	/**
	 * Gets the ordinal position of the token in the segment, origin 0.
	 *
	 * @return    The index value
	 */
	public int getIndex();


	/**
	 * Gets the ordinal position of a subelement within a series of subelements token in the segment.
	 *
	 * @return    int position origin 0
	 */
	public int getSubIndex();


	/**
	 * Gets the value of a SIMPLE token.
	 * <pr>
	 * If this token is of type SEGMENT_START, the value of getSegmentType()
	 * is returned.
	 *
	 * @return    The value value
	 */
	public String getValue();


	/**
	 * Gets the same thing as <code>getValue</code>, returning it
	 * as a <code>char[]</code>.
	 *
	 * @return    The valueChars value
	 */
	public char[] getValueChars();


	/**
	 * Returns true if the value of this token equals
	 * the argument.
	 *
	 * @param  v  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	public boolean valueEquals(String v);


	/**
	 * Gets the value of the first token in the segment.
	 *
	 * @return    The segmentType value
	 */
	public String getSegmentType();


	/**
	 * For a token of type COMPOSITE, returns an array of tokens
	 * corresponding to the first-level subtotkens.
	 *
	 * @return    The subTokens value
	 */
	public TokenI[] getSubTokens();


	/**
	 * Returns a String concatenation of the segment type and
	 * a two-digit (or more) representation of the token's
	 * getIndex() value.
	 *
	 * @return    The elementIdS value
	 */
	public String getElementId();


	/**
	 *  For SIMPLE tokens that are part of a repeating sequence,
	 *  returns the ordinal position within the sequence.
	 *  Otherwise returns 0.
	 *
	 * @return    The repeatCount value
	 */
	public int getRepeatCount();
}

