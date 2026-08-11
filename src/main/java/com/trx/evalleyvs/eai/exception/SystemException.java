package com.trx.evalleyvs.eai.exception;



public class SystemException extends GeneralException
{

    /**
     * 에러 메시지를 String형태로 받아 Exception을 생성한다.
     * @param errorMessage String
     */
    public SystemException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Throwable객체를 받아 Exception을 생성한다.
     * @param cause Throwable
     */
    public SystemException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Exception객체를 받아 Exception을 생성한다.
     * @param e Exception
     */
    public SystemException(Exception e)
    {
    	super(e);
    }

    /**
     * 에러메시지와 Throwable 객체를 받아 Exception을 생성
     * @param errorMessage String
     * @param cause Throwable
     */
    public SystemException(String errorMessage, Throwable cause)
    {
    	super(errorMessage, cause);
    }

}
