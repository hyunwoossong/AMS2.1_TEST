/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import com.trx.evalleyvs.eai.util.CommonUtil;

/**
 * Determines and maintains state transitions for the segment looping structure
 * within a particular EDI document.
 */
public class LoopingState {

	private boolean debug = false;

	private String standard;

	private String documentType;

	private boolean enabled = false;

	private Plugin plugin = null;

	/**
	 * Descriptor that caused us to enter the loop we are now in.
	 */
	private LoopDescriptor loopDescriptor = new LoopDescriptor("/", "", 0, "/");

	/**
	 * Number of loops that were closed as the result of the most recent
	 * transition. A transition that re-enters the implicit outer loop does not
	 * consider the outer loop in this count.
	 */
	private int numberOfLoopsClosed;

	/**
	 * Construct a LoopingState instance. There is no custom behavior based on
	 * the specific EDI standard mention. This is passed strictly for the
	 * purpose of forming the name of a plugin.
	 * 
	 */
	public LoopingState(String standard) {
		this.standard = standard;
	}

	/**
	 * Designate a particular document type (also known as Transaction Set or
	 * Message) for use in determining the internal segment looping structure.
	 * Plugins are expected to be in the com.berryworks.edireader.plugin package
	 * and have a class name with the name of the standard in caps (ANSI,
	 * EDIFACT, ...) followed by an userscore and finally the parsed document
	 * type (837, INVOICE, ...).
	 * 
	 * @param docType
	 *            implying a specific segment looping structure
	 * @return true if looping definitions available for this documentType,
	 *         false otherwise
	 */
	public boolean useLoopingStructure(String docType) {
		this.documentType = docType;

		String pluginName = "com.berryworks.edireader.plugin." + standard + "_"
				+ documentType;
		if (debug) {
			trace("useLoopingStructure looking for plugin " + pluginName);
		}
		try {
			Class pluginClass = Class.forName(pluginName);
			if (debug) {
				trace("plugin " + pluginName + " loaded");
			}
			plugin = (Plugin) pluginClass.newInstance();
			if (debug) {
				plugin.debug(true);
				trace("dcoument type " + documentType + " is a "
						+ plugin.getDocumentName());
			}
			enabled = true;
		} catch (ClassNotFoundException e) {
			if (debug) {
				trace("plugin " + pluginName + " not available");
			}
			enabled = false;
		} catch (InstantiationException e) {
			if (debug) {
				trace("plugin " + pluginName + " could not be instantiated");
			}
			enabled = false;
		} catch (IllegalAccessException e) {
			if (debug) {
				trace("plugin " + pluginName + " caused IllegalAccessException"
						+ e);
			}
			enabled = false;

		}

		return enabled;
	}

	/**
	 * Compute a state transition that may have occurred as the result of the
	 * presence of a particular segment type at this point in parsing the
	 * document.
	 * 
	 * @param segmentName
	 *            type of segment encountered, for example: 837
	 * @return true if there was a transition to a new loop, false otherwise
	 * @exception EDISyntaxException
	 *                Description of the Exception
	 */
	public boolean transition(String segmentName) throws EDISyntaxException {
		boolean result = false;
		if (enabled) {
			LoopDescriptor newDescriptor = plugin.query(segmentName,
					loopDescriptor.getName());
			if (newDescriptor == null) {
				if (debug) {
					trace("no loop transition for segment " + segmentName);
				}
			} else {
				result = true;
				if (debug) {
					trace("loop " + newDescriptor.getName()
							+ " established by segment " + segmentName);
				}

				int level = newDescriptor.getNestingLevel();
				if (level == 0) {
					// we do not close the outer loop
					numberOfLoopsClosed = loopDescriptor.getNestingLevel()
							- newDescriptor.getNestingLevel();
				} else {
					// but we do close other loops before entering a new loop at
					// the same or lower level
					numberOfLoopsClosed = 1 + loopDescriptor.getNestingLevel()
							- newDescriptor.getNestingLevel();
				}
				if ((numberOfLoopsClosed < 0)
						|| (numberOfLoopsClosed > loopDescriptor
								.getNestingLevel())) {
					if (debug) {
						System.err
								.println("Error: transitioning from nesting level "
										+ loopDescriptor.getNestingLevel()
										+ " to level "
										+ newDescriptor.getNestingLevel()
										+ ", therefore need to close "
										+ numberOfLoopsClosed + " loops");
					}
					throw new EDISyntaxException(
							"Improper sequencing noted with segment "
									+ segmentName);
				}
				if (debug) {
					System.err.println(numberOfLoopsClosed
							+ " loops closed by this transition");
					CommonUtil.writelog(numberOfLoopsClosed
							+ " loops closed by this transition");
				}

				loopDescriptor = newDescriptor;
			}
		} else {
			if (debug) {
				trace("transition ignored (looping structure analysis not enabled)");
			}
		}
		return result;
	}

	/**
	 * Return the name of a loop that was entered as the result of the most
	 * recent transition.
	 * 
	 * @return name of the entered loop, or null if no loop was entered
	 */
	public String getLoopEntered() {
		return loopDescriptor.getName();
	}

	/**
	 * Get the number of loops that were closed as the result of the most recent
	 * state transition. Re-entering the implicit outer loop does not count as a
	 * loop closing.
	 * 
	 * @return Description of the Return Value
	 */
	public int closedCount() {
		return numberOfLoopsClosed;
	}

	/**
	 * Get the nesting level of the current loop.
	 * 
	 * @return Description of the Return Value
	 */
	public int getNestingLevel() {
		return loopDescriptor.getNestingLevel();
	}

	/**
	 * Gets the enabled attribute of the LoopingState object
	 * 
	 * @return The enabled value
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Get the document name associated with this object (for example, "Health
	 * Care Claim")
	 * 
	 * @return String document name, or null if the document plugin is not
	 *         available
	 */
	public String getDocumentName() {
		if (enabled) {
			return plugin.getDocumentName();
		}
		return null;
	}

	/**
	 * Turn on or off debug tracing for this object.
	 * 
	 * @param debug
	 *            The new debug value
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Shorthand for EDIReader.trace(String)
	 */
	protected static void trace(String string) {
		EDIAbstractReader.trace(string);
	}

}
