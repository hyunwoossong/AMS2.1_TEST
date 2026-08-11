package com.trx.evalleyvs.eai.exception;


/**
 * 사용자에게 제공하는 서비스 에러가 발생하는 경우에 발생
 * @version 
 * @author
 */
public class ServiceException extends GeneralException
{
    /**
     * 서비스 Exception은 대체로 Service Layer에서 발생하므로 Service 카테고리의 기본 생성자를 지원한다.
     * @param errorMessage

    public ServiceException(ErrorMessage errorMessage){
        super(ApplicationContext.CATEGORY_SERVICE,errorMessage);
    }*/

    /**
     * ErrorMessage 객체를 이용하여 기본 Exception을 생성하는 것으로 한다.
     * @param category String
     * @param errorMessage ErrorMessage

    public ServiceException(String category, ErrorMessage errorMessage) {
        super(category, errorMessage);
    }*/

    /**
     * 에러 메시지를 String형태로 받아 Exception을 생성한다.
     * @param errorMessage String
     */
    public ServiceException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Throwable객체를 받아 Exception을 생성한다.
     * @param cause Throwable
     */
    public ServiceException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Exception객체를 받아 Exception을 생성한다.
     * @param e Exception
     */
    public ServiceException(Exception e)
    {
    	super(e);
    }

    /**
     * 에러메시지와 Throwable 객체를 받아 Exception을 생성
     * @param errorMessage
     * @param cause
     */
    public ServiceException(String errorMessage, Throwable cause)
    {
    	super(errorMessage, cause);
    }

    /**
     * ErrorMessage 객체를 이용하여 기본 Exception을 생성하는 것으로 한다.
     * @param category
     * @param errorMessage
     * @param cause 원인이 되는 Exception을 설정하여 exception chain을 생성한다.

    public ServiceException(String category, ErrorMessage errorMessage, Throwable cause){
        super(category, errorMessage,cause);
    }*/
}

