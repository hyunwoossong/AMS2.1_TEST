package com.trx.validate;

import java.util.HashMap;
import java.util.regex.Pattern;
import com.trx.util.CommonUtil;

import org.springframework.beans.factory.annotation.Autowired;


public class ValidationBiz {

	public static boolean BIZcheck(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		boolean flag = true;

		try{
			if(CommonUtil.nullChk(map.get("CNE_ISOCOUNTRYCODE")).equals("UK")&&!CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I")){

				if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
					map.put("ERRORMSG", "UK IS NOT USED ISO CODE CHANEGED GB");
					map.put("ERRORMSG_KOR", "ISO CODE UK는 GB로 변경 되어야 합니다");
				}
				flag = false;
			}else if(CommonUtil.nullChk(map.get("CNE_ISOCOUNTRYCODE")).equals("MX")&&!CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I")){

				if(!Pattern.matches("([A-Z]{2})", CommonUtil.nullChk(map.get("CNE_STATEPROVINCE"),""))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG", "MX STATE CODE INPUT ERROR");
						map.put("ERRORMSG_KOR", "멕시코 행 화물 주 코드 입력 오류 ");
					}
					flag = false;
				}

			}else if(CommonUtil.nullChk(map.get("CNE_ISOCOUNTRYCODE")).equals("BR")&&!CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I")){

				if(!Pattern.matches("([A-Z]{2})", CommonUtil.nullChk(map.get("CNE_STATEPROVINCE"),""))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG", "BR STATE CODE INPUT ERROR");
						map.put("ERRORMSG_KOR", "브라질 행 화물 주 코드 입력 오류 ");
					}
					flag = false;
				}

			}else if(CommonUtil.nullChk(map.get("CNE_ISOCOUNTRYCODE")).equals("CA")&&!CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I")){

				if(!Pattern.matches("([A-Z]{2})", CommonUtil.nullChk(map.get("CNE_STATEPROVINCE"),""))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG", "CA STATE CODE INPUT ERROR");
						map.put("ERRORMSG_KOR", "캐나다 행 화물 주 코드 입력 오류");
					}
					 flag = false;

				}else if(!Pattern.matches("[A-Z]{1}[0-9]{1}[A-Z]{1}([ ]{1})?[0-9]{1}[A-Z]{1}[0-9]{1}", CommonUtil.nullChk(map.get("CNE_POSTCODE"),""))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG", "CA POST CODE INPUT ERROR");
						map.put("ERRORMSG_KOR", "미주행 화물 우편번호 입력 오류 ");

					}
					flag = false;
				}

			}else if(CommonUtil.nullChk(map.get("CNE_ISOCOUNTRYCODE")).equals("US")&&!CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I")){

				if(!Pattern.matches("([A-Z]{2})", CommonUtil.nullChk(map.get("CNE_STATEPROVINCE"),""))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
							//FNA
						map.put("ERRORMSG", "US STATE CODE INPUT ERROR");
						map.put("ERRORMSG_KOR", "미주 행 화물 주 코드 입력 오류");

					}
					 flag = false;

				}else if(!Pattern.matches("([0-9]{5})", CommonUtil.nullChk(map.get("CNE_POSTCODE"),""))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG","US POST CODE INPUT ERROR");
						map.put("ERRORMSG_KOR","미주행 화물 우편번호 입력 오류 ");
					}
					flag = false;
				}else if(map.get("CNE_CONTACTNUMBER") != null){

					if(!Pattern.matches("[0-9A-Z]+", CommonUtil.nullChk(map.get("CNE_CONTACTNUMBER"),""))){

						if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
							map.put("ERRORMSG","DE CONTACT CODE INPUT ERROR");
							map.put("ERRORMSG_KOR","독일행 연락처 입력 오류 ");
						}
						flag = false;
					}
				}
			}else if(CommonUtil.nullChk(map.get("CNE_ISOCOUNTRYCODE")).equals("DE")&&!CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I")){

				if(!Pattern.matches("([0-9]{5})", CommonUtil.nullChk(map.get("CNE_POSTCODE"),""))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG","DE POST CODE INPUT ERROR");
						map.put("ERRORMSG_KOR","독일행 화물 우편번호 입력 오류 ");
					}
					flag = false;

				}else if(map.get("CNE_CONTACTNUMBER") != null){

					if(!Pattern.matches("[0-9A-Z]+", CommonUtil.nullChk(map.get("CNE_CONTACTNUMBER"),""))){

						if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
							map.put("ERRORMSG","DE CONTACT CODE INPUT ERROR");
							map.put("ERRORMSG_KOR","독일행 연락처 입력 오류 ");
						}
						flag = false;
					}
				}
			}else if(CommonUtil.nullChk(map.get("CNE_ISOCOUNTRYCODE")).equals("AT")&&!CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I")){

				if(!Pattern.matches("([0-9]{4})", CommonUtil.nullChk(map.get("CNE_POSTCODE"),""))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG","AT POST CODE INPUT ERROR");
						map.put("ERRORMSG_KOR","오스트리아행 화물 우편번호 입력 오류 ");
					}
					flag = false;

				}else if(map.get("CNE_CONTACTNUMBER") != null){

					if(!Pattern.matches("[0-9A-Z]+", CommonUtil.nullChk(map.get("CNE_CONTACTNUMBER"),""))){


						if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
							map.put("ERRORMSG","AT CONTACT CODE INPUT ERROR");
							map.put("ERRORMSG_KOR","오스트리아행 연락처 입력 오류 ");
						}
						flag = false;
					}
				}
			}else if(CommonUtil.nullChk(map.get("TGT_PIMA")).equals("AZ")&&!CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I")){
				//[\w\s\-\.][0-9\\sA-Z]+
				if(!Pattern.matches("[\\w\\s.]+", CommonUtil.nullChk(map.get("SHP_ADDRESS"),""))){
					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG","IN CASE OF AZ AIRLINE SHP ADDRESS INPUT ERROR");
						map.put("ERRORMSG_KOR","AZ 항공사 송하인 주소 입력 오류(특수기호 입력불가)");
					}
					flag = false;
				}

				if(!Pattern.matches("[\\w\\s]+", CommonUtil.nullChk(map.get("CNE_ADDRESS"),""))){
					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG","IN CASE OF AZ AIRLINE CNE ADDRESS INPUT ERROR");
						map.put("ERRORMSG_KOR","AZ 항공사 수하인 주소 입력 오류(특수기호 입력불가)");
					}
					flag = false;
				}
				if(CommonUtil.nullChk(map.get("OCI_TYPE")).equals("EXP") && (CommonUtil.nullChk(map.get("OCI_FLAG")).equals("M") || CommonUtil.nullChk(map.get("OCI_FLAG")).equals("A"))){
					if(!Pattern.matches("[0-9A-Z]+", CommonUtil.nullChk(map.get("CNE_POSTCODE"),""))){

						if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
							map.put("ERRORMSG","IN CASE OF AZ AIRLINE CNE POST CODE INPUT ERROR");
							map.put("ERRORMSG_KOR","AZ 항공사 수하인 우편번호 입력 오류(띄어쓰기, 특수기호 입력 불가)");
						}
						flag = false;
					}
				}
			}
			/*cyyeo 2023.07.19 [LO] 폴란드항공 FWB/FHL 전송시 우편번호 표기 관련 (메일참조)으로 아래 로직 삭제
			else if(CommonUtil.nullChk(map.get("TGT_PIMA")).equals("LO") && CommonUtil.nullChk(map.get("OCI_TYPE")).equals("EXP")
					&& !CommonUtil.nullChk(map.get("OCI_FLAG")).equals("I") && !CommonUtil.nullChk(map.get("OCI_CARGO_TYPE")).equals("R")
					&& CommonUtil.nullChk(map.get("M_ORG")).equals("ICN") && (CommonUtil.nullChk(map.get("M_DST")).equals("WAW")
							|| CommonUtil.nullChk(map.get("M_DST")).equals("GDN")	|| CommonUtil.nullChk(map.get("M_DST")).equals("LCJ")
							|| CommonUtil.nullChk(map.get("M_DST")).equals("POZ")	|| CommonUtil.nullChk(map.get("M_DST")).equals("KRK")
							|| CommonUtil.nullChk(map.get("M_DST")).equals("WRO") || CommonUtil.nullChk(map.get("M_DST")).equals("KTW")
							|| CommonUtil.nullChk(map.get("M_DST")).equals("SZZ")	|| CommonUtil.nullChk(map.get("M_DST")).equals("RZE"))){
				//LO 항공사 BIZ
				//우편번호 ##-###, 중간에 하이픈 필수
				//ICN 출발 - WAW, GDN, LCJ, POZ, KRK, WRO, KTW, SZZ, RJE 도착만 해당
				//환적 제외

				if(!Pattern.matches("[0-9]{2}(-)[0-9]{3}", CommonUtil.nullChk(map.get("CNE_POSTCODE")))){

					if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
						map.put("ERRORMSG","LO CNE POST CODE INPUT ERROR");
						map.put("ERRORMSG_KOR","LO 항공사 CNE 화물 우편번호 입력 오류 ");
					}
					flag = false;
				}
			}*/
		}catch(Exception e){
			e.printStackTrace();
			if(CommonUtil.nullChk(map.get("ERRORMSG"), "").equals("")){
				map.put("ERRORMSG","FHL BUSINESS SPC ERROR EXCEPTION");
				map.put("ERRORMSG_KOR","");
			}
			flag = false;
		}

		return flag;
	}

}
