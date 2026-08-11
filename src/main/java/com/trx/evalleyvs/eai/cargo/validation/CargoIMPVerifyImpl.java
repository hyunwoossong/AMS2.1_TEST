package com.trx.evalleyvs.eai.cargo.validation;

import com.trx.evalleyvs.eai.exception.ValidationException;
import com.trx.evalleyvs.eai.exception.SystemException;

public interface CargoIMPVerifyImpl {
	public  boolean verify(String strCargoImp) throws ValidationException, SystemException;

}
