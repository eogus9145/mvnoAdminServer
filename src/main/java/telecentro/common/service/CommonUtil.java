package telecentro.common.service;

import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

/**
 * 
 * @author user
 *  DB쿼리에서 Service 호출용
 */
@Service
public class CommonUtil {

	/**
	 * mybatis에서 날짜형식 생성
	 * @param str
	 * @return
	 */
	public static String get_section_query(String sField, String eField, String sDt, String eDt, String isDatetime, String dtType) {

		String dateType = "";
		String newSDt = sDt;
		String newEDt = eDt;		
		StringBuffer ret = new StringBuffer();

		// Oracle
		if ("1".equals(dtType)) {
			// 컬럼이 YYYYMMDDHH24MISS 형식의 VARCHAR인 경우
			if (newSDt != null && !"".equals(newSDt)) {
				newSDt = newSDt.replaceAll("[^0-9]", "");
			}
			if (newEDt != null && !"".equals(newEDt)) {
				newEDt = newEDt.replaceAll("[^0-9]", "") + ".999999";
			}

			if (eField != null && !"".equals(eField)) {
				if (newSDt != null && !"".equals(newSDt) && newEDt != null && !"".equals(newEDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + eField + " >= '" + newSDt + "' AND " + eField + " <= '" + newEDt + "' ");
				} else if (newSDt != null && !"".equals(newSDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + eField + " >= '" + newSDt + "' ");
				} else if (newEDt != null && !"".equals(newEDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + eField + " <= '" + newEDt + "' ");
				} 

			} else {

				if (newSDt != null && !"".equals(newSDt) && newEDt != null && !"".equals(newEDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + sField + " >= '" + newSDt + "' AND " + sField + " <= '" + newEDt + "' ");
				} else if (newSDt != null && !"".equals(newSDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + sField + " >= '" + newSDt + "' ");
				} else if (newEDt != null && !"".equals(newEDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + sField + " <= '" + newEDt + "' ");
				}

			}
		} else {

			if ("Y".equals(isDatetime)) {
				if (newSDt != null && !"".equals(newSDt)) {
					newSDt = newSDt + " 00:00:00";
				}
				if (newEDt != null && !"".equals(newEDt)) {
					newEDt = newEDt + " 23:59:59";
				}
				dateType = "YYYY-MM-DD HH24:MI:SS";
			} else {
				dateType = "YYYY-MM-DD";
			}

			if (eField != null && !"".equals(eField)) {
				if (newSDt != null && !"".equals(newSDt) && newEDt != null && !"".equals(newEDt)) {

					ret.append(" AND ( ");
					ret.append(" (" + sField + " >= TO_DATE('" + newSDt + "','" + dateType + "') AND " + sField + " <= TO_DATE('" + newEDt + "','" + dateType + "')) ");
					ret.append(" OR ");
					ret.append(" (" + eField + " >= TO_DATE('" + newSDt + "','" + dateType + "') AND " + eField + " <= TO_DATE('" + newEDt + "','" + dateType + "')) ");
					ret.append(" OR ");
					ret.append(" (" + sField + " <= TO_DATE('" + newSDt + "','" + dateType + "') AND " + eField + " >= TO_DATE('" + newEDt + "','" + dateType + "')) ");
					ret.append(" OR ");
					ret.append(" (" + sField + " >= TO_DATE('" + newSDt + "','" + dateType + "') AND " + eField + " <= TO_DATE('" + newEDt + "','" + dateType + "')) ");
					ret.append(" ) ");

				} else if (newEDt != null && !"".equals(newEDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + sField + " <= TO_DATE('" + newEDt + "',''" + dateType + "'') ");
				} else if (newSDt != null && !"".equals(newSDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + sField + " <= TO_DATE('" + newSDt + "',''" + dateType + "'') AND " + eField + " >= TO_DATE('" + newSDt + "',''" + dateType + "'') ");
				}

			} else {

				if (newSDt != null && !"".equals(newSDt) && newEDt != null && !"".equals(newEDt)) {

					ret.append(" AND " + sField + " >= TO_DATE('" + newSDt + "','" + dateType + "') ");
					ret.append(" AND " + sField + " < TO_DATE('" + newEDt + "','" + dateType + "') + INTERVAL '1 day' ");

				} else if (newSDt != null && !"".equals(newSDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + sField + " >= TO_DATE('" + newSDt + "','" + dateType + "') ");
				} else if (newEDt != null && !"".equals(newEDt)) {
					ret.delete(0,ret.length());
					ret.append(" AND " + sField + " <= TO_DATE('" + newEDt + "','" + dateType + "') ");
				}

			}
		}

		return ret.toString();

	}

	/**
	 * mybatis에서 달러사용시 영문, 숫자, 언더바를 제외하는 문자 제거용
	 * @param str
	 * @return
	 */
	public static String get_dollar_replace_query(String str) {
		return str.replaceAll("[^a-zA-Z0-9_]", "");
	}
	
	/**
	 * 멀티셀렉트박스 사용시 쿼리 생성
	 * @param column
	 * @param jsonStr
	 * @return
	*/
	public static String get_multi_select_query(String column, String jsonStr) throws Exception {
		
		String columnName = column;
		String keywords = jsonStr;
		StringBuffer ret = new StringBuffer();
		
		if("".equals(keywords) || keywords == null) return "";
		
		keywords = keywords.replaceAll("&quot;", "\"");
		JSONArray jsonArray = new JSONArray(keywords);
		
	    ret.append(" AND ").append(column).append(" IN (");

	    for (int i = 0; i < jsonArray.length(); i++) {
	        String keyword = value_check(jsonArray.getString(i));
	        if (i > 0) ret.append(", ");
	        ret.append("'").append(keyword).append("'");
	    }
	    ret.append(") ");

	    return ret.toString();
	}
	
	/**
	 * 동적으로 들어갈 값이 SQL인젝션 위험이 있는지 체크
	 * @param value
	 * @return
	*/
	public static String value_check(String value) throws Exception {
		String[] injectionPattern = {"--", ";", "/*", "*/", "DROP ", "INSERT INTO ", "UPDATE ", "DELETE FROM "};
		for (String pattern : injectionPattern) {
		    if (value.contains(pattern)) {
		        throw new IllegalArgumentException("부적절한 요청이 포함되어 있습니다.");
		    }
		}
		return value;
	}
	
}
