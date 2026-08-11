/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import com.trx.evalleyvs.eai.util.CommonUtil;

/**
 * Superclass for all document plugins.
 * 
 */
public abstract class Plugin {

	private final String documentType;

	private final String documentName;

	/**
	 * LoopDescriptor[] is a table of state transfer information specific to a
	 * particular document type. The transition method uses this table to
	 * determine if a state transition occurred or not.
	 */
	protected LoopDescriptor[] loops;

	/**
	 * Determines whether tracing for debugging purposes to System.err is turned
	 * on or not.
	 */
	protected boolean debug;

	/**
	 * Constructor
	 * 
	 * @param documentType
	 *            for example: "837"
	 * @param documentName
	 *            for example: "Health Care Claim"
	 */
	public Plugin(String documentType, String documentName) {
		this.documentType = documentType;
		this.documentName = documentName;
	}

	/**
	 * Get the document type (for example, "837")
	 * 
	 * @return The documentType value
	 */
	public String getDocumentType() {
		return documentType;
	}

	/**
	 * Get the readable name for the documnet (for example, "Health Care Claim")
	 * 
	 * @return The documentName value
	 */
	public String getDocumentName() {
		return documentName;
	}

	/**
	 * Query the plugin about a loop that starts with a designated segment type,
	 * given that you are already within a particular loop.
	 * 
	 * @param firstSegment
	 *            Description of the Parameter
	 * @param loopContext
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 */
	public LoopDescriptor query(String firstSegment, String loopContext) {
		LoopDescriptor result = null;
		if (loopContext == null) {
//			if (debug) {
//				System.err
//						.println("loopContext argument was null with segment "
//								+ firstSegment);
//			}
			loopContext = "*";
		}
		if (loops == null) {
//			if (debug) {
//				System.err.println("plugin has null array of loop descriptors");
//			}
			return null;
		}
		for (int n = 0; n < loops.length; n++) {
			String candidateSegment = loops[n].getFirstSegment();
//			if (debug) {
//				System.err.println("comparing segment " + firstSegment
//						+ " to loop descriptor's initial segment "
//						+ candidateSegment);
//			}
			if (candidateSegment.equals(firstSegment)) {
//				if (debug)
//					System.err.println(firstSegment
//							+ " matched, now checking loop context |"
//							+ loopContext + "|");
				String candidateContext = loops[n].getLoopContext();
				if ("*".equals(candidateContext)
						|| loopContext.equals(candidateContext)) {
					if (debug)
						CommonUtil.writelog("match complete");
					result = loops[n];
//					if (debug) {
//						System.err.println("loop selected: " + result);
//					}
					break;
				}
			}
		}
		return result;
	}

	public void debug(boolean d) {
		this.debug = d;
	}

	public String toString() {
		String result;
		result = "Plugin " + getClass().getName();
		result += "\n  " + getDocumentName() + " (" + getDocumentType() + ")";
		for (int i = 0; i < loops.length; i++) {
			result += "\n  " + loops[i].toString();
		}
		return result;
	}

}
