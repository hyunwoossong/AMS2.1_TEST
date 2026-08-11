package com.trx.evalleyvs.eai.exception;



import org.apache.log4j.Logger;

import com.trx.evalleyvs.eai.log.LoggerFactory;


public abstract class GeneralException extends Exception
{
    protected static Logger log = LoggerFactory.getLogger("MSB");



    /**ErrorMessage클래스를 사용하지 않는 경우, 기본적인 에러메시지를 문자열로 입력한다.
     * @param errorMessage String 에러 메시지
     */
    public GeneralException(String errorMessage)
    {
        super(errorMessage);
    }

    /** 애플리케이션 내부 중요비즈니스 로직에서 Exception이 발생한경우 Throwable객체를 획득하여
     * 파라메터로 입력한다. Throwable t = exception.getCause();
     * @param cause Throwable 에러 메시지
     */
    public GeneralException(Throwable cause)
    {
        super(cause.getMessage(), cause);
    }

    /** 애플리케이션 내부 중요비즈니스 로직에서 Exception이 발생한경우 Throwable객체를 획득하여
     * 파라메터로 입력한다. Throwable t = exception.getCause();
     * @param errorMessage String
     * @param cause Throwable
     */
    public GeneralException(String errorMessage, Throwable cause)
    {
    	super(errorMessage, cause);
    }

    /** 애플리케이션 내부 중요비즈니스 로직에서 Exception이 발생한경우 Exception객체를 획득하여
     * 파라메터로 입력한다. Throwable t = exception.getCause();
     * @param e Exception
     */
    public GeneralException(Exception e)
    {
    	super(e);
    }
}
