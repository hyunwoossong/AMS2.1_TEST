/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

/**
 * Static metadata about a segment group or loop segments within an EDI document
 * (also known as transaction set or message). A sequence of LoopDescriptors
 * comrise the essence of a transaction set Plugin which allows a subclass of
 * EDIReader to consider the nested segment loops that are often important to
 * the semantics of a document.
 */
public class LoopDescriptor {

	/**
	 * Name of the loop entered as the result of applying this descriptor.
	 * 
	 * The name is typically a simple text name that appears in the
	 * specifications of the EDI document but not in the actual data. The value
	 * "/" in this field designates the implicit outer loop. The variant form
	 * /segmentname designates the outer loop following the appearance of the
	 * named segment.
	 */
	private String name;

	/**
	 * Segment type of the first segment in this loop.
	 */
	private String firstSegment;

	/**
	 * Level of nesting for this loop.
	 * 
	 * Typically, a document will begin with one or more segments that are
	 * considered to be outside of any segment loops. The segments are
	 * implicitly at nesting level 0. The first level of explicit nesting is
	 * level 1, and increases with each new level.
	 */
	private int nestingLevel;

	/**
	 * Context which determines whether or not a particular appearance of a
	 * segment type that matches the firstSegment is in fact an instance of this
	 * loop. The value of the loop context is one of the following: <br>
	 * <br> * - indicates that any appearance of the segment type constitutes a
	 * new instance of the loop <br>
	 * <br>
	 * loopname - indicates that a new instance of the loop is indicated if we
	 * are currently in the loop named by loopname <br>
	 * <br>
	 * /segmentname - a special-case of loopname corresponding to a portion of
	 * the implicit outer loop following an appearance of a segment of type
	 * segmentname. This form is rarely required.
	 */
	private String loopContext;

	/**
	 * Constructor a descriptor for recognizing the beginning of a nested loop.
	 * 
	 * @param loopName
	 *            Name of the loop, suitable for use as an XML attribute value
	 * @param firstSegment
	 *            Semgent type that (at least sometimes) indicates entry into
	 *            this loop.
	 * @param nestingLevel
	 *            How deeply is this loop nested within other loops.
	 * @param loopContext
	 *            Nmae of a loop; indicates a valid prior state
	 */
	public LoopDescriptor(String loopName, String firstSegment, int nestingLevel,
			String loopContext) {
		this.name = loopName;
		this.firstSegment = firstSegment;
		this.nestingLevel = nestingLevel;
		this.loopContext = loopContext;
	}

	/**
	 * Get the name of the loop.
	 * 
	 * @return The name value
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the nested loop depth for this loop within the document.
	 * 
	 * @return The nestingLevel value
	 */
	public int getNestingLevel() {
		return nestingLevel;
	}

	/**
	 * Get the name of the loop in which this loop appears as a nest loop.
	 * 
	 * @return name of the loop context, or null if this is a top-level loop
	 */
	public String getLoopContext() {
		return loopContext;
	}

	/**
	 * Get the segment type of the first segment in the loop which, in context,
	 * defines an occurence of the loop.
	 * 
	 * @return The firstSegment value
	 */
	public String getFirstSegment() {
		return firstSegment;
	}

	public String toString() {
		String result;
		result = "loop " + getName() + " at nesting level " + getNestingLevel()
				+ ": encountering segment " + getFirstSegment();
		String context = getLoopContext();
		if ("*".equals( context)) {
			result += " anytime";
		} else if ("/".equals(context)) {
			result += " while outside any loop";
		} else {
			result += " while currently in loop " + context;
		}
		return result;
	}
}
