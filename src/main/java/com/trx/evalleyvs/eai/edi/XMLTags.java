/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

public interface XMLTags {
	/**
	 * XML tags used when generating XML from EDI
	 */
	public final static String ROOT_TAG = "ediroot";

	public final static String INTERCHANGE_TAG = "interchange";

	public final static String SENDER_TAG = "sender";

	public final static String RECEIVER_TAG = "receiver";

	public final static String ADDRESS_TAG = "address";

	public final static String GROUP_TAG = "group";

	public final static String DOCUMENT_TAG = "transaction";

	public final static String LOOP_TAG = "loop";

	public final static String SEG_TAG = "segment";

	public final static String ELEMENT_TAG = "element";

	public final static String COMPOSITE_TAG = "composite";

	public final static String SUB_ELEMENT_TAG = "subelement";

	/**
	 * XML attributes used when generating XML from EDI
	 */
	public final static String ID_ATTRIBUTE = "Id";

	public final static String QUALIFIER_ATTRIBUTE = "Qual";

	public final static String ADDRESS_EXTRA_ATTRIBUTE = "Extra";

	public final static String SUB_ELEMENT_SEQUENCE = "Sequence";

	public final static String COMPOSITE_INDICATOR = "Composite";

	public static final String CONTROL = "Control";

	public static final String TIME = "Time";

	public static final String DATE = "Date";

	public static final String APPL_RECEIVER = "ApplReceiver";

	public static final String APPL_SENDER = "ApplSender";

	public static final String GROUP_TYPE = "GroupType";

	public static final String STANDARD_VERSION = "StandardVersion";

	public static final String STANDARD_CODE = "StandardCode";

	public static final String STANDARD = "Standard";

}
