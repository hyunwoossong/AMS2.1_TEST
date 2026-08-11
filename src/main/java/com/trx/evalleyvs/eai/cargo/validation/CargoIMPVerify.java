package com.trx.evalleyvs.eai.cargo.validation;

public class CargoIMPVerify {
	
	public static CargoIMPVerifyImpl getInstance(String msgType)  {
		
		CargoIMPVerifyImpl msgIF = null;
		try {
			msgIF =  (CargoIMPVerifyImpl)Class.forName("com.glsk.evalleyvs.eai.cargo.validation."+msgType).newInstance();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return msgIF;
		
	}

}
