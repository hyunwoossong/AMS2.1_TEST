/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import org.xml.sax.helpers.AttributesImpl;

/**
 * Provide convenience methods to simplify the construction
 * of XML attributes.
 *
 */
public class EDIAttributes extends AttributesImpl {

	void addCDATA(String name, String value) {
		addAttribute("", name, name, "CDATA", value);
	}

}
