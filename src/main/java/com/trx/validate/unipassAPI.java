package com.trx.validate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trx.Msgprocess.CreateAck;
import com.trx.util.CommonUtil;

public class unipassAPI {

	private static final Logger logger = LoggerFactory.getLogger(unipassAPI.class);

	public HashMap<String, Object> setFulfillepn(HashMap<String, Object> fhlmap) throws Exception{

		List<HashMap<String, String>> list = new ArrayList();

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date now = new Date();

		try{
			//		String expno = (String) maps.get("expno");
				//    String blno = (String) maps.get("blno");

					SAXBuilder parser = new SAXBuilder();
				    parser.setValidation(false);
				    parser.setIgnoringElementContentWhitespace(true);

				    String url = "https://unipass.customs.go.kr:38010/ext/rest/expDclrNoPrExpFfmnBrkdQry/retrieveExpDclrNoPrExpFfmnBrkd?crkyCn=z210v126h056i238e030u020c0&expDclrNo=";
				    url = url + fhlmap.get("OCI_EPN_NO");
				    Document doc = parser.build(url);
				    XMLOutputter outp = new XMLOutputter();
				    Element root = doc.getRootElement();

				    List namedChildren = root.getChildren("expDclrNoPrExpFfmnBrkdQryRsltVo");
				    List namedChildren1 = root.getChildren("expDclrNoPrExpFfmnBrkdDtlQryRsltVo");

				    HashMap<String, String> map = new HashMap();

				    List tCnt = root.getChildren("tCnt");

				    boolean flag = true;
				    String shpmPckUt = "";
				    String mnurConm = "";
				    String shpmCmplYn = "";
				    String acptDt = "";
				    String acptDt1 = "";
				    String acptDt2 = "";
				    String acptDt3 = "";
				    String shpmWght = "";
				    String exppnConm = "";
				    String loadDtyTmlm = "";
				    String expDclrNo = "";
				    String csclWght = "";
				    String shpmPckGcnt = "";
				    String csclPckUt = "";
				    String csclPckGcnt = "";
				    String cnt = "";
				    String loadDtyTmlm1 = "";
				    String loadDtyTmlm2 = "";
				    String loadDtyTmlm3 = "";

				    String shpmPckUt1 = null;
				    String tkofDt1 = null;
				    String tkofDt2 = null;
				    String tkofDt3 = null;
				    String tkofDt4 = null;
				    String shpmPckGcnt1 = null;
				    String blNo1 = null;

				    if (namedChildren.size() > 0)
				    {
				      Element element = (Element)namedChildren.get(0);

				      shpmPckUt = CommonUtil.nullChk(element.getChildText("shpmPckUt"));
				      mnurConm = CommonUtil.nullChk(element.getChildText("mnurConm"));
				      shpmCmplYn = CommonUtil.nullChk(element.getChildText("shpmCmplYn"));

				      acptDt = CommonUtil.nullChk(element.getChildText("acptDt"));
				      if (acptDt.length() > 0)
				      {
				        acptDt1 = CommonUtil.nullChk(acptDt.substring(0, 4));
				        acptDt2 = CommonUtil.nullChk(acptDt.substring(4, 6));
				        acptDt3 = CommonUtil.nullChk(acptDt.substring(6, 8));
				      }
				      shpmWght = CommonUtil.nullChk(element.getChildText("shpmWght"));
				      exppnConm = CommonUtil.nullChk(element.getChildText("exppnConm"));
				      loadDtyTmlm = CommonUtil.nullChk(element.getChildText("loadDtyTmlm"));
				      if (loadDtyTmlm.length() > 0)
				      {
				        loadDtyTmlm1 = loadDtyTmlm.substring(0, 4);
				        loadDtyTmlm2 = loadDtyTmlm.substring(4, 6);
				        loadDtyTmlm3 = loadDtyTmlm.substring(6, 8);
				      }
				      expDclrNo = CommonUtil.nullChk(element.getChildText("expDclrNo"));
				      csclWght = CommonUtil.nullChk(element.getChildText("csclWght"));
				      shpmPckGcnt = CommonUtil.nullChk(element.getChildText("shpmPckGcnt"));
				      csclPckUt = CommonUtil.nullChk(element.getChildText("csclPckUt"));
				      csclPckGcnt = CommonUtil.nullChk(element.getChildText("csclPckGcnt"));
				    }

				    map.put("shpmPckUt", CommonUtil.nullChk(shpmPckUt));
			        map.put("mnurConm", CommonUtil.nullChk(mnurConm));
			        map.put("shpmCmplYn", CommonUtil.nullChk(shpmCmplYn));
			        map.put("acptDt", CommonUtil.nullChk(acptDt));
			        map.put("acptDt1", CommonUtil.nullChk(acptDt1));
			        map.put("acptDt2", CommonUtil.nullChk(acptDt2));
			        map.put("acptDt3", CommonUtil.nullChk(acptDt3));
			        map.put("shpmWght", CommonUtil.nullChk(shpmWght));
			        map.put("exppnConm", CommonUtil.nullChk(exppnConm));
			        map.put("loadDtyTmlm1", CommonUtil.nullChk(loadDtyTmlm1));
			        map.put("loadDtyTmlm2", CommonUtil.nullChk(loadDtyTmlm2));
			        map.put("loadDtyTmlm3", CommonUtil.nullChk(loadDtyTmlm3));

			        map.put("expDclrNo", CommonUtil.nullChk(expDclrNo));
			        map.put("csclWght", CommonUtil.nullChk(csclWght));
			        map.put("shpmPckGcnt", CommonUtil.nullChk(shpmPckGcnt));
			        map.put("csclPckUt", CommonUtil.nullChk(csclPckUt));
			        map.put("csclPckGcnt", CommonUtil.nullChk(csclPckGcnt));
			        map.put("flag", "yes");

			        map.put("loadDtyTmlm", CommonUtil.nullChk(loadDtyTmlm));

				    if (tCnt.size() > 0)
				    {
				      Element eleCnt = (Element)tCnt.get(0);
				      cnt = eleCnt.getContent(0).getValue();
				      map.put("cnt", CommonUtil.nullChk(cnt));
				    }

				    if (cnt.equals("-1"))
				    {
				      map.put("cnt", CommonUtil.nullChk(cnt));
				      list.add(map);
				    }

				    list.add(map);

				    if (namedChildren1.size() > 0) {
				      for (int i = 0; i < Integer.parseInt(cnt); i++)
				      {
				        Element element1 = (Element)namedChildren1.get(i);

				        shpmPckUt1 = CommonUtil.nullChk(element1.getChildText("shpmPckUt"));
				        tkofDt1 = CommonUtil.nullChk(element1.getChildText("tkofDt"));
				        if (tkofDt1.length() > 0)
				        {
				          tkofDt2 = CommonUtil.nullChk(tkofDt1.substring(0, 4));
				          tkofDt3 = CommonUtil.nullChk(tkofDt1.substring(4, 6));
				          tkofDt4 = CommonUtil.nullChk(tkofDt1.substring(6, 8));
				        }
				        shpmPckGcnt1 = CommonUtil.nullChk(element1.getChildText("shpmPckGcnt"));
				        blNo1 = CommonUtil.nullChk(element1.getChildText("blNo"));

				        HashMap<String, String> map1 = new HashMap();

				        map1.put("shpmPckUt1", CommonUtil.nullChk(shpmPckUt1));
				        map1.put("tkofDt1", CommonUtil.nullChk(tkofDt1));
				        map1.put("tkofDt2", CommonUtil.nullChk(tkofDt2));
				        map1.put("tkofDt3", CommonUtil.nullChk(tkofDt3));
				        map1.put("tkofDt4", CommonUtil.nullChk(tkofDt4));
				        map1.put("shpmPckGcnt1", CommonUtil.nullChk(shpmPckGcnt1));
				        map1.put("blNo1", CommonUtil.nullChk(blNo1));

				        list.add(map1);
				      }
				    }

				    logger.info("=======EPN EXP VALIDATE START=======");

			        logger.info("EPN No : "+map.get("expDclrNo") );
			        logger.info("Loading Duty Deadline: "+map.get("loadDtyTmlm"));
			        logger.info("Shipment completed : "+map.get("shpmCmplYn") );

			        logger.info("==================================");

			        if(map.get("loadDtyTmlm") != null && map.get("loadDtyTmlm").toString().length() > 0){

			        	Date loadDty = format.parse(map.get("loadDtyTmlm"));
			        	Date current = format.parse(fhlmap.get("FLT").toString());

			        	//앞에 변수가 크면 1, 작으면 -1, 같으면 0
				        int compare = loadDty.compareTo(current);

				        if(compare < 0){

				        	logger.info("======Loading Duty Deadline Error======");
				        	fhlmap.put("IKAMS_SMI", "FNA");
				        	fhlmap.put("EPN_ERR_YN", "Y");
				        	fhlmap.put("EPN_CHK_MSG", "OUTDATED EPN");
				        	if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"),"").equals("")){
				        		fhlmap.put("IKAMS_ERRMSG","OUTDATED EPN "+ map.get("expDclrNo"));
				        		fhlmap.put("IKAMS_ERRMSG_KOR", "EPN NO 날짜 지남 "+ map.get("expDclrNo"));
							}
				        	flag = false;
				        	fhlmap.put("APIflag", flag);
				        }

				        if(map.get("shpmCmplYn").equals("Y")){

				        	logger.info("======Shipment completed Error======");
				        	fhlmap.put("IKAMS_SMI", "FNA");
				        	fhlmap.put("EPN_ERR_YN", "Y");
				        	fhlmap.put("EPN_CHK_MSG", "EPN ALREADY USED FOR SHIPMENT");
				        	if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"),"").equals("")){
				        		fhlmap.put("IKAMS_ERRMSG", "SHPM CMPL "+ map.get("expDclrNo"));
				        		fhlmap.put("IKAMS_ERRMSG_KOR", "EPN 선적 완료 "+ map.get("expDclrNo"));
							}
				        	flag = false;
				        	fhlmap.put("APIflag", flag);
				        }
			        }else{

			        	logger.info("======INVAILD EPN======");
			        	fhlmap.put("IKAMS_SMI", "FNA");
			        	fhlmap.put("EPN_ERR_YN", "Y");
			        	fhlmap.put("EPN_CHK_MSG", "INVAILD EPN");
			        	if(CommonUtil.nullChk(fhlmap.get("IKAMS_ERRMSG"),"").equals("")){
			        		fhlmap.put("IKAMS_ERRMSG", "INVAILD EPN "+ map.get("expDclrNo"));
			        		fhlmap.put("IKAMS_ERRMSG_KOR", "검증되지 않은 EPN "+ map.get("expDclrNo"));
						}
			        	flag = false;
			        	fhlmap.put("APIflag", flag);

			        }

			        if(flag == true){
			        	fhlmap.put("EPN_ERR_YN", "N");
			        	fhlmap.put("EPN_CHK_MSG", "VERIFIED EPN");
			        }
			        logger.info("========EPN EXP VALIDATE END========");
				}catch(IndexOutOfBoundsException e){
					logger.info(" === IndexOutOfBoundsException Error === ");
					logger.error("ERROR==>"+e.toString(), e);
				}catch(IOException e){
					logger.info(" === IOException Error === ");
					logger.error("ERROR==>"+e.toString(), e);
			    }
				return fhlmap;
			}
}
