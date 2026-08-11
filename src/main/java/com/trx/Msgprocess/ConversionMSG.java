package com.trx.Msgprocess;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibatis.sqlmap.client.SqlMapClient;

public class ConversionMSG {

	private static final Logger logger = LoggerFactory.getLogger(ConversionMSG.class);

	@Autowired
	SqlMapClient sqlMapClient;

	public HashMap<String, Object> ktnetHeader(HashMap<String, Object> ktConmap) throws SQLException{
		// TODO Auto-generated method stub

		String header = "";
		String body = "";
		String footer = "";
		String version = "";
		String accessRef = "";
		String edifact = "";

		String ktnetfhl = (String) ktConmap.get("DIVISION_MSG");
		try{
			String str = null;
			StringTokenizer token = new StringTokenizer(ktnetfhl, "'");

			while (token.hasMoreTokens()) {
				str = token.nextToken();
				if (str.startsWith("UNB")) {
					header += "'" + str;
					String[] head = str.split(":");
					String[] sendpima = head[1].split("\\+");
					ktConmap.put("KTSENDPIMA", sendpima[1]);
					String[] rcvd = head[3].split("\\+");
					ktConmap.put("KTREROUTING", rcvd[0]);
					ktConmap.put("KTRCVD_PIMA", rcvd[1]);
				} else if (str.startsWith("UNH")) {
					header += "'" + str;
					String[] items = str.split("\\+");
					ktConmap.put("KTCOMPANY_CODE", items[1]);
					if (items != null && items.length > 2) {
						version = items[2].split(":")[1];
					}
					if (items != null && items.length > 3) {
						accessRef = items[3];
					}
				} else if (str.startsWith("UNT")){
					footer += "'" + str; // Message Trailer
					ktConmap.put("UNT", footer);
				}else if (str.startsWith("UNZ")) {
					footer += "'" + str + "'"; // Interchange Trailer
				} else
					body += "'" + str;
			}

			//body 정보
			body = body.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("<br>", "\r\n");
			if (body.endsWith("\r\n"))
				body = body.substring(0, body.length() - "\r\n".length());
				ktConmap.put("KTBODY", body);

		}catch(Exception e){

			e.printStackTrace();
			System.out.println("FHL PARSER ERROR");
			//Log.cerr("FMA ACK PARSER ERROR : \n", e);

		}

		return ktConmap;

	}


	public HashMap<String, Object> ktnetACKpaserFMA(HashMap<String, Object> ktConmap) throws SQLException{

		String header = "";
		String body = "";
		String footer = "";
		String version = "";
		String accessRef = "";
		String edifact = "";
		String ktnetfhl = (String) ktConmap.get("DIVISION_MSG");
		try{
			String str = null;
			StringTokenizer token = new StringTokenizer(ktnetfhl, "'");

			while (token.hasMoreTokens()) {
				str = token.nextToken();
				if (str.startsWith("UNB")) {
					header += "'" + str;
					String[] head = str.split(":");

					//SEND PIMA
					String[] sendpima = head[1].split("\\+");
					ktConmap.put("KTSENDPIMA", sendpima[1]);

					if(sendpima[1].equals("RKRCCS77KTNET")){
						ktConmap.put("KTACK_STATUS", "KTNET_OK");
					}else if(sendpima[1].endsWith("RKRCCS77MFCS")){
						ktConmap.put("KTACK_STATUS", "MFCS_OK");
					}else{
						ktConmap.put("KTACK_STATUS", "KTNET_OK");
					}

					//RCVD PIMA & REVERSE ROUTING
					String[] rcvd = head[2].split("\\+");
					String TRAXON_PIMA = ktnetpimaChange(rcvd[1]);
					ktConmap.put("KTRCVD_PIMA", TRAXON_PIMA);

					//Reverse Routing
					String[] rerouting = head[4].split("\\+");
					ktConmap.put("KTREROUTING", rerouting[0]);


				} else if (str.startsWith("UNH")) {

				} else if (str.startsWith("UNT")){

				}else if (str.startsWith("UNZ")) {
					footer += "'" + str + "'"; // Interchange Trailer
				} else
					body += "'" + str;
			}

			//ktnetfhl = ktnetfhl.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("<br>", "\r\n");
			body = body.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("<br>", "\r\n");

			//Body 정보
			String[] bodyline = body.split("\r\n");
			if(bodyline[1].startsWith("ACK/")){
				String[] ackbody = bodyline[1].split("-");
				if(ackbody.length==2){
					String[] awbno = ackbody[1].split("\\.");
					ktConmap.put("MAWB_NO", awbno[0].substring(0,3));
					ktConmap.put("HWB_NO", awbno[1].substring(3));
				}
			}

			if (body.endsWith("\r\n"))
				body = body.substring(0, body.length() - "\r\n".length());
				ktConmap.put("KTBODY", body);

		}catch(Exception e){

			e.printStackTrace();
			System.out.println("FMA ACK PARSER ERROR");
			//Log.cerr("FMA ACK PARSER ERROR : \n", e);

		}
		return ktConmap;

	}

	public HashMap<String, Object> ktnetACKpaserFNA(HashMap<String, Object> ktConmap) throws SQLException{

		String header = "";
		String body = "";
		String footer = "";
		String version = "";
		String accessRef = "";
		String edifact = "";
		String ktnetfhl = (String) ktConmap.get("DIVISION_MSG");
		try{
			String str = null;
			StringTokenizer token = new StringTokenizer(ktnetfhl, "'");

			while (token.hasMoreTokens()) {
				str = token.nextToken();
				if (str.startsWith("UNB")) {
					header += "'" + str;
					String[] head = str.split(":");

					//SEND PIMA
					String[] sendpima = head[1].split("\\+");
					ktConmap.put("KTSENDPIMA", sendpima[1]);

					if(sendpima[1].equals("RKRCCS77KTNET")){
						ktConmap.put("KTACK_STATUS", "KTNET_OK");
					}else if(sendpima[1].endsWith("RKRCCS77MFCS")){
						ktConmap.put("KTACK_STATUS", "MFCS_OK");
					}else{
						ktConmap.put("KTACK_STATUS", "KTNET_OK");
					}

					//RCVD PIMA & REVERSE ROUTING
					String[] rcvd = head[2].split("\\+");
					String TRAXON_PIMA = ktnetpimaChange(rcvd[1]);
					ktConmap.put("KTRCVD_PIMA", TRAXON_PIMA);

					//Reverse Routing
					String[] rerouting = head[4].split("\\+");
					ktConmap.put("KTREROUTING", rerouting[0]);


				} else if (str.startsWith("UNH")) {

				} else if (str.startsWith("UNT")){

				}else if (str.startsWith("UNZ")) {
					footer += "'" + str + "'"; // Interchange Trailer
				} else
					body += "'" + str;

			}

			body = body.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("<br>", "\r\n");
			String[] bodyline = body.split("\r\n");
			for(int i=1; i<bodyline.length; i++){

				if(bodyline[i].startsWith("ACK")){
					if(!bodyline[i].startsWith("FHL")){
						ktConmap.put("KTACK_CONTENTS", bodyline[i].replace("ACK/", "").trim()+bodyline[i+1].replace("/", "").trim());
					}else{
						ktConmap.put("KTACK_CONTENTS", bodyline[i].replace("ACK/", "").trim());
					}
				}else if(bodyline[i].startsWith("MBI")){
					String[] mbi = bodyline[i].split("/");
					ktConmap.put("MAWB_NO", mbi[1].substring(0,3)+mbi[1].substring(4,12));
					ktConmap.put("MAWB_PRIFIX", mbi[1].substring(0,3));
					ktConmap.put("MAWB_SERIAL", mbi[1].substring(4,12));
				}else if(bodyline[i].startsWith("HBS")){
					String[] hbs = bodyline[i].split("/");
					ktConmap.put("HWB_NO", hbs[1]);
				}
			}

			if (body.endsWith("\r\n"))
				body = body.substring(0, body.length() - "\r\n".length());
				ktConmap.put("KTBODY", body);

				System.out.println("KTACK CONTENTS :"+ktConmap.get("KTACK_CONTENTS"));
				System.out.println("KTACK MAWB_NO :"+ktConmap.get("MAWB_NO"));
				System.out.println("KTACK MAWB_PRIFIX :"+ktConmap.get("MAWB_PRIFIX"));
				System.out.println("KTACK MAWB_SERIAL :"+ktConmap.get("MAWB_SERIAL"));
				System.out.println("KTACK HWB_NO :"+ktConmap.get("HWB_NO"));
				System.out.println("KT BODY :"+ktConmap.get("KTBODY"));
		}catch(Exception e){

			e.printStackTrace();
			System.out.println("FNA ACK PARSER ERROR");
			//Log.cerr("FMA ACK PARSER ERROR : \n", e);

		}
		return ktConmap;

	}

	public String ktnetpimaChange(String rcvdid) throws SQLException{

		String TRAXONPIMA ="";

		String KTNET_CODE = rcvdid.substring(2,6);
		HashMap map = new HashMap();
		map.put("KTNET_CODE", KTNET_CODE);
		try {
			List<HashMap<String, Object>> result = sqlMapClient.queryForList("TM_TRACE.ktnetpimachange", map);

			for (int i = 0; i < result.size(); i++) {
				HashMap<String,Object> pimaCode = new HashMap<String,Object>();
				pimaCode = (HashMap) result.get(i);
				TRAXONPIMA = (String) pimaCode.get("TRAXONPIMA");
				System.out.println(TRAXONPIMA);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return TRAXONPIMA;

	}

	public String ktnetcodeChange(String sendid) throws SQLException{

		String KTNET_CODE ="";

		HashMap map = new HashMap();
		map.put("COMPANYCODE", sendid);
		try {
			List<HashMap<String, Object>> result = sqlMapClient.queryForList("TM_TRACE.ktnetcodechange", map);

			for (int i = 0; i < result.size(); i++) {
				HashMap<String,Object> pimaCode = new HashMap<String,Object>();
				pimaCode = (HashMap) result.get(i);
				KTNET_CODE = (String) pimaCode.get("COMPANYKTNET");
				System.out.println("KTNET CODE : "+KTNET_CODE);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return KTNET_CODE;

	}
}
