package com.trx.evalleyvs.eai.exception;


/**
 *	Pima 정보 조회시<br>
 *
 *  @since 	1.0
 *  @author 	bmchae
 */
public class CargoTransformationException extends ServiceException {
	/** 발생한 실재 Exception */
    private Exception exception;
	
	/** 
     * 1. 기능 : Constructor
     * 2. 처리 개요 : 
     *     - Constructor
     * 3. 주의사항 
     *
	 *	@param	message	Excetpion message
	 *	@param	Excetpion	발생한 Exception
	 */
    public CargoTransformationException(String message, Exception exception) {
        super(message);
        this.exception = exception;

        return;
    }

   /** 
     * 1. 기능 : Constructor
     * 2. 처리 개요 : 
     *     - Constructor
     * 3. 주의사항 
     *
	 *	@param	message	Excetpion message
	 */
    public CargoTransformationException(String message) {
        this(message, null);

        return;
    }

    /** 
     * 1. 기능 : Constructor
     * 2. 처리 개요 : 
     *     - Constructor
     * 3. 주의사항 
     *
	 *	@param	Excetpion	발생한 Exception
	 */
    public CargoTransformationException(Exception exception) {
        this(null, exception);

        return;
    }
	
	/** 
     * 1. 기능 : 실재 발생한 Exception을 반환하는 method
     * 2. 처리 개요 : 
     *     - 실재 발생한 Exception을 반환하는 method
     * 3. 주의사항 
     *
	 *	@return 실재 발생한 Exception
	 */
    public Exception getException() {
        return exception;
    }
	
    /** 
     * 1. 기능 : 실재 발생한 Exception을 반환하는 method
     * 2. 처리 개요 : 
     *     - 실재 발생한 Exception을 반환하는 method
     * 3. 주의사항 
     *
	 *	@return 실재 발생한 Exception
	 */
    public Exception getRootCause() {
        if (exception instanceof ServiceLocatorException) {
            return ((ServiceLocatorException) exception).getRootCause();
        }

        return (exception == null) ? this : exception;
    }
	
	/** 
     * 1. 기능 : 실재 발생한 Exception의 toString() method의 결과를 반환하는 method
     * 2. 처리 개요 : 
     *     - 실재 발생한 Exception의 toString() method의 결과를 반환하는 method
     * 3. 주의사항 
	 */
    public String toString() {
        if (exception instanceof ServiceLocatorException) {
            return ((ServiceLocatorException) exception).toString();
        }

        return (exception == null)       
               ? super.toString() : exception.toString();
    }
} 
