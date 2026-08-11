package com.trx.evalleyvs.eai.log;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;




/**
 * MSB Logger�� ���� FactoryŬ����
 */
public class LoggerFactory
{

    /**
     * Logger�� �����Ѵ�.
     * @param category String �������Ͽ� ��ϵ� ī�װ?��
     */
    public static Logger getLogger(String category)
    {
    	//BasicConfigurator.configure();
    	
    	//PropertyConfigurator.configure("C:\\bea\\user_projects\\domains\\msb_domain\\apacheLog4jCfg.xml");
    	
		Logger logger = null;

        return logger;
    }
  

    
    
    public static void writelog(String sLog)
    {
        Calendar cal = Calendar.getInstance();
        //String filePath = "/weblogic/bea92/user_projects/domains/msb_domain/logs/";
        String filePath = "D:\\TRAXON\\workspace\\MSB\\log";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd");
        String sToday = formatter.format(cal.getTime()); //for log-time
        String sDate = formatter2.format(cal.getTime()); //for file-name

        String sFileName = sDate +".log";
        sLog = "[" + sToday + "]:" + sLog;

        try
         {
        //FileOutputStream flWt = new FileOutputStream(filePath+sFileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(filePath+sFileName,true));
		bw.write(new String(sLog.getBytes("Euc-kr"),"ISO-8859-1"));
            bw.newLine();
            bw.close();
        }
        catch(IOException ie)
        {
            ie.printStackTrace();
        }
    }

}
