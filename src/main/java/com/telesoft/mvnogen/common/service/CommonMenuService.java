package com.telesoft.mvnogen.common.service;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.telesoft.mvnogen.common.dao.CommonDAO;
import com.telesoft.mvnogen.common.dto.CommonResponseDto;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class CommonMenuService {
	
	@Resource
	private CommonService commonService;
	
	public CommonResponseDto menuList(HttpServletRequest request, Map<String, Object> paramMap) throws Exception {
		
		paramMap.put("queryId", "CommonMapper_get_first_menu");
		paramMap.put("adminId", request.getSession().getAttribute("SESSION_ADM_ID").toString());
		Map<String, Object> resultMap = commonService.selectOne(paramMap);
		String firstMenu = (resultMap != null) ? resultMap.get("auth_url").toString() : "";
		paramMap.clear();
		
		paramMap.put("queryId", "CommonMapper_admin_auth_menu");
		paramMap.put("SESSION_ADM_ID", request.getSession().getAttribute("SESSION_ADM_ID").toString());
		List<Map<String, Object>> menuList = commonService.selectList(paramMap);
		
		for(Map<String, Object> menu : menuList) {
			Object authUrl = menu.get("auth_url");
			String url = authUrl != null ? authUrl.toString() : "";
			menu.put("isFirst", url.equals(firstMenu));
		}
		
		return commonService.response("0000", "Success", menuList);
	}
	
	public CommonResponseDto searchMenuList(HttpServletRequest request, Map<String, Object> paramMap) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();
		if(paramMap.get("keyword") != null && paramMap.get("keyword").toString().trim().length() > 0) {
			paramMap.put("queryId", "CommonMapper_get_search_menu_list");
			paramMap.put("adminId", request.getSession().getAttribute("SESSION_ADM_ID").toString());
			resultList = commonService.selectList(paramMap);
		}
		return commonService.response("0000", "Success", resultList);			
	}
}
