package com.trx.evalleyvs.eai.message;



import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.trx.evalleyvs.eai.log.LoggerFactory;
import com.trx.evalleyvs.eai.util.CommonUtil;

/**
 * 1. 기능 : Message Resource 정보를 관리하기 위한 Manager Class
 * 2. 처리 개요 :
 * *     - Message Rewource 관련 정보 처리를 위한 method 제공
 * 3. 주의사항
 *
 * @author  :
 * @version : v 1.0.0
 * @see :
 * @since   :
 */
public class MessageXMLResourceManager
{

	/**
	 * MessageResourceManager logger
	 */
	protected final static Logger logger = 	LoggerFactory.getLogger("MSB");



	/**
	 * Properties Object
	 */
	private Properties properties;

	/**
	 * MessageResourceManager 기동 여부
	 */
	private boolean started;

	/**
	 * message Resource File
	 */
	private String messageResource;

	private static MessageXMLResourceManager inst;

	private MessageXMLResourceManager(String fileName) {
		setMessageResource(fileName);
		init();
	}

	public static MessageXMLResourceManager getInstance(String fileName) {
		if (inst == null) {
			inst = new MessageXMLResourceManager(fileName);
		}

		return inst;
	}

	/**
	 * MessageResourceManager를 초기화 한다.
	 */
	public void init() {
		if(messageResource==null) throw new RuntimeException("messageResource is null.");

		if(started) throw new RuntimeException("Already started MessageResourceManager.");

		//CommonUtil.writelog("MessageResourceManager is initializing.");

		//System.getProperties().list(System.out);

		if(properties==null) this.properties = new Properties();
		this.properties.clear();

		ClassLoader c = this.getClass().getClassLoader();
		InputStream in = null;

		try {

			in = c.getResourceAsStream("com/trx/evalleyvs/eai/properties/" + messageResource );
			//in = c.getResourceAsStream("MessageResource.xml");

			loadXmlProperties(in);
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("MessageResourceManager Init Error. - Cannot load the messageResource File");
		}
		//this.properties.list(System.out);

		this.started = true;
		//CommonUtil.writelog("MessageResourceManager is initialized.");
	}

	/**
	 * MessageResourceManager를 clear 한다.
	 */
	public void destroy() {
		if(!started) throw new RuntimeException("Already stopped MessageResourceManager.");
		CommonUtil.writelog("MessageResourceManager is destroying.");

		this.properties.clear();
		this.properties = null;

		this.started = false;
		CommonUtil.writelog("MessageResourceManager is destroyed.");

	}

	/**
	 * property file setter method
	 *
	 * @param   messageResource  Properties File
	 */
	public void setMessageResource(String messageResource) {
		this.messageResource = messageResource;
	}

	/**
	 * property getter method
	 *
	 * @param   name    property Key
	 * @return  property value
	 */
	public String getProperty(String name) {
		return this.properties.getProperty(name);
	}


	/**
	 * property getter method
	 *
	 * @param   name    property Key
	 * @return  property value
	 */
	public String getProperty(String name , String[] args) {
		String msg = this.properties.getProperty(name);
		String retMsg = null;

		if(args != null) {

			retMsg = MessageFormat.format(msg, args);

		}
		return retMsg;
	}

	/**
	 * property getter method
	 *
	 * @param   name    property Key
	 * @param   defaultValue    property Default Value
	 * @return  property value
	 */
	public String getProperty(String name, String defaultValue) {
		String v = this.properties.getProperty(name);
		return v==null ? defaultValue : v;
	}

	/**
	 * property setter method
	 *
	 * @param   name    Property Key
	 * @param   value   Property Value
	 */
	public void setProperty(String name, String value) {
		this.properties.setProperty(name, value);
	}

	/**
	 * get All Properties
	 *
	 * @return  Properties Object
	 */
	public Properties getAllPropertys(){
		return this.properties;
	}

	/**
	 * XML Property 정보를 로드한다.
	 *
	 * @param   in  InputStream
	 */
	private void loadXmlProperties(InputStream in) throws IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			Element root = doc.getDocumentElement();
			NodeList lst = root.getChildNodes();
			for(int i=0;i<lst.getLength();i++) {
				Node n = lst.item(i);
				if(n.getNodeType()==Node.ELEMENT_NODE && n.getNodeName().equals("entry")) {

					Element e = (Element)n;
					String key = e.getAttribute("key");
					Node child = e.getFirstChild();

					if ((child != null) && (child.getNodeValue() != null)) {
						String value = child.getNodeValue();
						this.properties.put(key, value);
					} else {
						logger.warn(key + " MessageResource에 대한 Value 값이 설정되지 않았습니다.");
					}
				}
			}
		} catch(Exception e) {
			throw new IOException("Cannot load the xml messageResource. - "+e.getMessage());
		}
	}

	public Object[] toArray() {
		Object[] obj = new Object[this.properties.size()];
		Iterator it = this.properties.keySet().iterator();
		for(int i=0;it.hasNext();i++) {
			String k = (String)it.next();
			String v = this.properties.getProperty(k);
			obj[i] = k+"="+v;
		}
		return obj;
	}
}
