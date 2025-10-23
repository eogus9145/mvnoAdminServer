package telecentro.common.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.annotation.Resource;
import telecentro.common.dto.CommonResponseDto;

@Service
public class CommonListService {

	@Resource
	private CommonService commonService;
	
	@Resource
	private CommonSQLFunction commonSQLFunction;
	
	private String[] encodeColumNames = {
		"ssn", "cssn", "ussn"
	};
	
	public CommonResponseDto selectPageList(Map<String, Object> paramMap) throws Exception {	
		
		// 컬럼이 1개 이상 있는지 체크
		List<Map<String, Object>> colList = (List<Map<String, Object>>) paramMap.get("columns");
		if(colList.size() == 0) return commonService.response("2000", "적어도 한개 이상의 컬럼명이 필요합니다.");
		
		List<String[]> formatColumns = new ArrayList<>();
		
		for(Map<String, Object> col : colList) {
			if(col.containsKey("format") && col.get("format") != null) formatColumns.add(new String[]{ col.get("id").toString(), col.get("format").toString() });
		}		
		
		// 조회결과를 가져온다.
		Map<String, Object> resultMap = new HashMap<>();
		int resultListTotCnt = commonService.selectListTotCnt(paramMap);
		if ("true".equals(paramMap.get("totSum"))) resultMap.put("resultListTotSum", commonService.selectListTotSum(paramMap));
		int selectedPageNum = Integer.parseInt(paramMap.get("selectedPageNum").toString());
		int rowPerPage = Integer.parseInt(paramMap.get("rowPerPage").toString());
		int startRowNum = (selectedPageNum - 1) * rowPerPage;
		int endRowNum = rowPerPage;
		paramMap.put("startRowNum", startRowNum);
		paramMap.put("endRowNum", endRowNum);
		List<Map<String, Object>> resultList = commonService.selectList(paramMap);
		
		// 데이터 후처리
		resultList = formatList(formatColumns, resultList);		
		
		resultMap.put("resultList", resultList);
		resultMap.put("resultListTotCnt", resultListTotCnt);
		resultMap.put("selectedPageNum", selectedPageNum);		
		return commonService.response("0000", "Success", resultMap);
	}
	
	public List<Map<String, Object>> formatList(List<String[]> formatColumns, List<Map<String, Object>> resultList) throws Exception {
		if(formatColumns.size() > 0) {
			for (Map<String, Object> row : resultList) {
	    		for (String[] col : formatColumns) {    	    			
	    			String colName = col[0];
	    			String formatType = col[1];    	    			
	    			Object val = row.get(colName);
	    			if (val != null) {
	    				Method method = commonSQLFunction.getClass().getMethod("format_" + formatType, Object.class);
	    				Object newVal = method.invoke(commonSQLFunction, new Object[] { val });
	    				row.put(colName, newVal);
	    			}
	    		}
	    	}
	    }        
        return resultList;
	}
	
}
