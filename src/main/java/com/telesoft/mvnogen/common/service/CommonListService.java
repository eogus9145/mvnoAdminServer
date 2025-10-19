package com.telesoft.mvnogen.common.service;

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

import com.telesoft.mvnogen.common.dto.CommonResponseDto;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.annotation.Resource;

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
		
		List<String> encodedColumns = new ArrayList<>();
		List<Object[]> funcitonColumns = new ArrayList<>();
		List<String[]> formatColumns = new ArrayList<>();
		
		for(Map<String, Object> col : colList) {
			// 허용된 컬림인지 확인
			
			// 복호화 대상 컬럼들을 구한다.
			if(Arrays.asList(encodeColumNames).contains(col.get("id").toString())) encodedColumns.add(col.get("id").toString());
			
			// fn속성을 가지고 있는 컬럼들을 구한다.
			if(col.containsKey("fn") && col.get("fn") != null) funcitonColumns.add(new Object[]{ col.get("id").toString(), col.get("fn") });
			
			// format속성을 가지고 있는 컬럼들을 구한다.
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
		resultList = processList(encodedColumns, funcitonColumns, formatColumns, resultList);
		
		
		resultMap.put("resultList", resultList);
		resultMap.put("resultListTotCnt", resultListTotCnt);
		resultMap.put("selectedPageNum", selectedPageNum);		
		return commonService.response("0000", "Success", resultMap);
	}
	
	
	
	public List<Map<String, Object>> processList(List<String> encodedColumns, List<Object[]> functionColumns, List<String[]> formatColumns, List<Map<String, Object>> resultList) throws Exception {
		
		Logger sqlLogger = (Logger) LoggerFactory.getLogger("log4jdbc.log4j2");
        Level prevLevel = sqlLogger.getLevel();
        
        try {
        	sqlLogger.setLevel(Level.OFF);
        	
        	for (Map<String, Object> row : resultList) {
        		
        		// 복호화
        		if(encodedColumns.size() > 0) {
        			for(String colName : encodedColumns) {
        				Object val = row.get(colName);
        				if(val != null) {
        					row.put(colName, commonService.aes256Dec(val.toString()));
        				}
        			}
        		}
    	    	
    	    	// 함수사용
    	    	if(functionColumns.size() > 0) {
    	    		for(Object[] col : functionColumns) {
    	    			String colName = col[0].toString();
    	    			Object val = row.get(colName);
    	    			Map<String, Object> functionMap = (Map<String, Object>) col[1];
    	    			String functionName = functionMap.get("name").toString();
    	    			List<Object> functionParamList = new ArrayList<>((List<Object>) functionMap.get("params"));
    	    			functionParamList.add(val); // 가공할 데이터는 파라미터의 항상 마지막 순서.
    	    			Object[] functionParams = functionParamList.toArray();
    	    			if (val != null) {
    	    				Class<?>[] paramTypes = Arrays.stream(functionParams).map(Object::getClass).toArray(Class<?>[]::new);
    	    				Method method = commonSQLFunction.getClass().getMethod(functionName, paramTypes);
    	    				Object newVal = method.invoke(commonSQLFunction, functionParams);
    	    				row.put(colName, newVal);
    	    			}
    	    		}
    	    	}
    	    	
    	    	
    	    	// 포매팅(단순 포매팅이면 위 함수보다 가볍게 처리하기위해 따로 뺌)
    	    	if(formatColumns.size() > 0) {
    	    		for (String[] col : formatColumns) {    	    			
    	    			String colName = col[0];
    	    			String formatType = col[1];    	    			
    	    			Object val = row.get(colName);
    	    			if (val != null) {
    	    				
    	    				// 날짜
    	    				if("date".equals(formatType) || "timestamp".equals(formatType) || "kr-date".equals(formatType) || "kr-timestamp".equals(formatType) || "month".equals(formatType)  || "year".equals(formatType)) {
    	    					row.put(colName, commonSQLFunction.date_format(val, formatType));
    	    				}
    	    				
    	    				// 전화번호
    	    				if("phone".equals(formatType)) {
    	    					row.put(colName, commonSQLFunction.format_ph(val));
    	    				}
    	    				
    	    				// 숫자
    	    				if("number".equals(formatType)) {
    	    					row.put(colName, commonSQLFunction.format_number(val));
    	    				}
    	    				
    	    				// 주민번호
    	    				if("ssn".equals(formatType)) {
    	    					row.put(colName, commonSQLFunction.format_ssn(val));
    	    				}
    	    				
    	    				
    	    			}    	    			
    	    		}	    		
    	    	}
    	    }
        	
        } finally {
            if (prevLevel != null) {
                sqlLogger.setLevel(prevLevel);
            } else {
                sqlLogger.setLevel(Level.INFO);
            }
        }
        
        return resultList;	    
	    
	} 
	
	
}
