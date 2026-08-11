package com.trx.evalleyvs.eai.exception;


/**
 *	Socket에러시
 *
 *  @since 	1.0
 *  @author 	bmchae
 */
public class NotCreateSocketException extends SystemException {
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
    public NotCreateSocketException(String message, Exception exception) {
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
    public NotCreateSocketException(String message) {
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
    public NotCreateSocketException(Exception exception) {
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
