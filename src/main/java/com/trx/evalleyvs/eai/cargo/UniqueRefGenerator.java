/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.cargo;

/*
 * I'll using DB later
 */
public class UniqueRefGenerator {

	private int refNo;
	private static UniqueRefGenerator instance = new UniqueRefGenerator();
	
	private UniqueRefGenerator() {
		refNo = 51000;
	}
	
	public static UniqueRefGenerator getInstance() {
		return instance;
	}
	
	public synchronized String getNextRef() {
		return String.valueOf(++refNo);
	}
}
