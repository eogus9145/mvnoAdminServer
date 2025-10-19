package com.telesoft.mvnogen.login.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.telesoft.mvnogen.common.dto.CommonResponseDto;
import com.telesoft.mvnogen.common.service.CommonService;
import com.telesoft.mvnogen.login.dto.AuthRequestDto;
import com.telesoft.mvnogen.login.dto.LoginRequestDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService extends CommonService {
	
	public CommonResponseDto loginCheck(HttpSession session) throws Exception {
		Boolean loggedIn = session.getAttribute("SESSION_ADM_LOGINSTATE") == "Y";
		if(loggedIn) {
			return response("0000", "true");
		} else {
			return response("9999", "false");
		}
	}
	
    public CommonResponseDto getSession(HttpSession session) throws Exception {
    	Boolean loggedIn = session.getAttribute("SESSION_ADM_LOGINSTATE") == "Y";
    	if(loggedIn) {
    		Map<String, Object> sessionMap = new HashMap<>();
    		
    		Enumeration<String> attrNames = session.getAttributeNames();
    		while(attrNames.hasMoreElements()) {
    			String key = attrNames.nextElement();
    			Object value = session.getAttribute(key);
    			if(
    				value instanceof String ||
    		        value instanceof Number ||
    		        value instanceof Boolean ||
    		        value instanceof Map ||
    		        value instanceof Collection
    			) sessionMap.put(key, value);
    		}
    		return response("0000", "true", sessionMap);
    	} else {
    		return response("9999", "false");
    	}
    }
    
    public CommonResponseDto getRsaPublicKey(HttpServletRequest request) throws Exception {
    	String publicKey = generateSessionRsaKey(request);
    	return response("0000", "true", publicKey);
    }
    
    public CommonResponseDto login(HttpServletRequest request, LoginRequestDto req) throws Exception {
    	    	
    	String id = req.getId();
    	String pw = decodeRsa(request.getSession(), req.getPw());
    	if(setNull(id) == null || setNull(pw) == null) return response("9996", "접속정보를 얻을 수 없습니다.");
    	
    	Map<String, String> paramMap = new HashMap<>();
    	paramMap.put("queryId", "loginMapper_loginInfo");
    	paramMap.put("login_id", id);
    	Map<String, Object> adminInfo = selectOne(paramMap);
    	
    	req.setIp((request.getHeader("X-FORWARDED-FOR") != null) ? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr());
    	req.setServerName(request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort());
    	req.setBrowser(getUserAgent(request));
    	req.setAdminName( adminInfo == null ? "" : adminInfo.get("admin_name").toString() );
    	req.setAdminType( adminInfo == null ? "" : adminInfo.get("admin_type").toString() );
    	    	
    	if(setNull(request.getRemoteAddr()) == null) return loginResponse(req, "1003", "접속정보가 없습니다.");
    	
    	if(adminInfo == null) return loginResponse(req, "2000", "로그인에 실패하였습니다.");
    	
    	if(setNull(req.getIp()) == null || "".equals(req.getIp())) return loginResponse(req, "1003", "접속정보가 없습니다.");
    	
    	String[] allowedServes = { "localhost:9090" };
    	boolean isAllowedServer = Arrays.asList(allowedServes).contains(req.getServerName().replaceFirst("^https?://", ""));
    	if(!isAllowedServer) return loginResponse(req, "-558", "브라우저 종료 후 재접속 바랍니다. 계속되면 관리자에게 문의 바랍니다. URL=" + req.getServerName());
    	
    	String[] allowedIps = { "111.111.111.111" };
    	boolean isAllowedIp = Arrays.asList(allowedIps).contains(req.getIp());
    	if(!isAllowedIp) {
    		paramMap.put("queryId", "loginMapper_accessInfo");
    		paramMap.put("login_ip", req.getIp());
    		Map<String, Object> accessInfo = selectOne(paramMap);
    		long accessCnt = (long) accessInfo.get("access_cnt");
    		if(accessCnt > 100) return loginResponse(req, "1000", "접속을 제한합니다. 고객센터로 문의바랍니다.");
    	}
    	
    	switch(adminInfo.get("status").toString()) {
    		case "R": return loginResponse(req, "3000", "승인대기 중인 계정입니다. 관리자에게 문의 바랍니다.");
    		case "S99": return loginResponse(req, "3000", "90일 이상 미접속하여 정지된 계정입니다. 고객센터로 문의 바랍니다.");
    		case "A": break;
    		default: return loginResponse(req, "3000", "정지된 계정입니다. 고객센터로 문의 바랍니다.");
    	}
    	
    	boolean pwMatch = sha512Equals(adminInfo.get("admin_pw").toString(), pw, adminInfo.get("admin_pw_salt").toString());
    	if(!pwMatch) {
    		paramMap.put("queryId", "loginMapper_updateFailCnt");
    		update(paramMap);
    		int maxTryCnt = 5;
    		if(Integer.parseInt(adminInfo.get("fail_cnt").toString()) + 1 >= maxTryCnt) {
    			paramMap.put("queryId", "loginMapper_stopStatus");
    			update(paramMap);
    			paramMap.put("queryId", "loginMapper_accessBlock");
    			insert(paramMap);
    		}
    		return loginResponse(req, "2000", "로그인에 실패하였습니다.");
    	} else {
    		
    		boolean smsAgreeFlag = true; // 업체 문자사용 여부
    		String resCd = "0000";
    		String resMsg = "Success";
    		boolean smsFlag = false;
    		boolean pwFlag = false;
    		
    		long pwDate = getTime(adminInfo.get("passwd_date"));
    		long maxPwDate = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);
    		if(pwDate < maxPwDate) {
				resCd = "5000";
				resMsg = "비밀번호를 변경한지 90일이 지났습니다. 비밀번호를 변경하여 주시기 바랍니다.";
				pwFlag = true;
    		}
    		
    		boolean isInitPw = sha512Equals(adminInfo.get("admin_pw").toString(), adminInfo.get("mobile").toString() + "@!", adminInfo.get("admin_pw_salt").toString());
    		if(isInitPw) {
				resCd = "5000";
				resMsg = "초기 비빌번호를 사용하고 계십니다. 비밀번호를 변경하여 주시기 바랍니다.";
				pwFlag = true;
    		}
    		
    		String oldLoginIp = adminInfo.get("login_ip").toString();
    		if(setNull(oldLoginIp) == null) {
    			paramMap.put("queryId", "loginMapper_updateIp");
    			update(paramMap);
    		} else {
    			if(!oldLoginIp.equals(req.getIp()) && smsAgreeFlag) {
    				resCd = "6000";
    				resMsg = "휴대폰 인증을 수행하겠습니다.1";
    				smsFlag = true;
    			}
    		}
    		
    		long lastDate = adminInfo.get("last_date") != null ? getTime(adminInfo.get("last_date")) : System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000);
    		long maxLastDate = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000);
    		if(lastDate < maxLastDate && smsAgreeFlag) {
				resCd = "6000";
				resMsg = "휴대폰 인증을 수행하겠습니다.2";
				smsFlag = true;
    		}
    		
    		if(smsFlag && pwFlag) resCd = "7000";
    		else if(smsFlag) resCd = "6000";
    		else if(pwFlag) resCd = "5000";
    		
    		List<Map<String, Object>> maskingList = selectList(paramMap);
    		String admMaskingList = maskingList.get(0).toString();
    		
    		request.getSession().setAttribute("SESSION_ADM_ID", id);
    		request.getSession().setAttribute("SESSION_ADM_NM", adminInfo.get("admin_name"));
    		request.getSession().setAttribute("SESSION_ADM_GRADE", adminInfo.get("grade"));
    		request.getSession().setAttribute("SESSION_ADM_TYPE", adminInfo.get("admin_type"));
    		request.getSession().setAttribute("SESSION_ADM_AGENT_ID", adminInfo.get("agent_id"));
    		request.getSession().setAttribute("SESSION_ADM_LANG", "KOR");
    		request.getSession().setAttribute("SESSION_ADM_MASKINGLIST", admMaskingList);
    		
    		Map<String, String> authMap = new HashMap<>(); 
    		
    		switch(resCd) {
	    		case "0000":
	    			paramMap.put("queryId", "loginMapper_updateAdmin");
	    			paramMap.put("ret_cd", "0000");
	    			update(paramMap);
	    			request.getSession().setAttribute("SESSION_ADM_LOGINSTATE", "Y");
	    			request.getSession().setAttribute("SESSION_ADM_PASSWORD_FLAG", "N");
	    		break;
	    		case "5000":
	    			paramMap.put("queryId", "loginMapper_updateAdmin");
	    			paramMap.put("ret_cd", "5000");
	    			update(paramMap);
	    			request.getSession().setAttribute("SESSION_ADM_LOGINSTATE", "Y");
	    			request.getSession().setAttribute("SESSION_ADM_PASSWORD_FLAG", "Y");
	    		break;
	    		case "6000":
	    			authMap = authReqService(id, req.getIp(), setNull(adminInfo.get("mobile")), setNull(adminInfo.get("phone")));
	    			if(!"0000".equals(authMap.get("cd").toString())) return loginResponse(req, "9999", "인증번호 발송 도중 오류가 발생하였습니다.");
	    			request.getSession().setAttribute("SESSION_ADM_PASSWORD_FLAG", "N");
	    		break;
	    		case "7000":
	    			authMap = authReqService(id, req.getIp(), setNull(adminInfo.get("mobile")), setNull(adminInfo.get("phone")));
	    			if(!"0000".equals(authMap.get("cd").toString())) return loginResponse(req, "9999", "인증번호 발송 도중 오류가 발생하였습니다.");
	    			request.getSession().setAttribute("SESSION_ADM_PASSWORD_FLAG", "Y");
	    		break;
    		}
    		
    		return loginResponse(req, resCd, resMsg);
    	}
    }
    
    public Map<String, String> authReqService(String id, String ip, String mobile, String phone) throws Exception {
    	Map<String, String> resultMap = new HashMap<>();
    	Map<String, String> paramMap = new HashMap<>();
    	Map<String, Object> queryMap = new HashMap<>();
    	
    	if(mobile == null && phone != null) {
    		if(phone.substring(0, 2).equals("01")) mobile = phone.replaceAll("-", "");;
    	}
    	
    	if(mobile == null) {
    		resultMap.put("cd", "1010");
    		resultMap.put("msg", "전송할 핸드폰 번호가 존재하지 않습니다. 고객센터로 문의 후 휴대폰 번호를 등록해주시기 바랍니다.");
    		return resultMap;
    	}
    	
    	paramMap.put("queryId", "loginMapper_authReqCheck");
    	paramMap.put("id", id);
    	paramMap.put("mobile", mobile);
    	queryMap = selectOne(paramMap);    	
    	int existCnt = queryMap.get("exist_cnt") == null ? 0 : Integer.parseInt(queryMap.get("exist_cnt").toString());
    	boolean isExpired = queryMap.get("req_date") == null ? false : System.currentTimeMillis() - getTime(queryMap.get("req_date")) >= 3 * 60 * 1000; 
    	
    	if(existCnt > 0 || isExpired) {
    		paramMap.put("queryId", "loginMapper_authReqDelete");
    		delete(paramMap);
    	}
    	
    	String reqNo = randomNumber();
    	paramMap.put("ip", ip);
    	paramMap.put("reqno", reqNo);
    	
    	try {
    		paramMap.put("queryId", "loginMapper_authReqInsert");
    		insert(paramMap);
    	} catch(DuplicateKeyException e) {
    		paramMap.put("queryId", "loginMapper_authReqUpdate");
    		update(paramMap);
    	}
    	
    	// 실제 문자 발송 로직 구현
    	
    	// 최종 결과 반환
    	resultMap.put("cd", "0000");    	
    	return resultMap;
    }
    
    public CommonResponseDto authReq(HttpServletRequest request, @RequestParam Map<String, Object> paramMap) throws Exception {
    	if(paramMap.get("id") == null || paramMap.get("id").toString().trim().length() == 0) return response("1000", "올바르지 않은 요청입니다");
    	String id = paramMap.get("id").toString();
    	String ip = (request.getHeader("X-FORWARDED-FOR") != null) ? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr();
    	
    	paramMap.put("queryId", "loginMapper_authReqCheck2");
    	Map<String, Object> queryMap = selectOne(paramMap);
    	String mobile = setNull(queryMap.get("mobile")) != null ? queryMap.get("mobile").toString() : null;
    	String phone = setNull(queryMap.get("phone")) != null ? queryMap.get("phone").toString() : null;
    	
    	Map<String, String> resultMap = authReqService(id, ip, mobile, phone);
    	return response(resultMap.get("cd"));
    }
    
    public CommonResponseDto authRes(HttpServletRequest request, AuthRequestDto req) throws Exception {
    	
    	Map<String, String> paramMap = new HashMap<>();
    	paramMap.put("id", req.getId());
    	paramMap.put("resno", req.getAuthNo());
    	
    	paramMap.put("queryId", "loginMapper_authResCheck");
    	List<Map<String, Object>> list = selectList(paramMap);
    	if(list.size() > 1) return response("1004", "중복오류");
    	if(list.size() < 1) return response("1004", "인증번호가 다릅니다. 다시 요청해 주세요");
    	
    	paramMap.put("queryId", "loginMapper_authResUpdate");
    	update(paramMap);
    	
    	paramMap.put("queryId", "loginMapper_authResInsert");
    	try {
    		insert(paramMap);    		
    	} catch(DuplicateKeyException e) {
    		return response("1007", "중복오류");
    	}
    	
    	paramMap.put("queryId", "loginMapper_authResDelete");
    	delete(paramMap);
    	
    	if(!list.get(0).get("req_auth_no").toString().trim().equals(req.getAuthNo().trim())) return response("2000", "인증번호가 다릅니다. 다시 요청해 주세요");
    	if(System.currentTimeMillis() - getTime(list.get(0).get("req_date")) >= 3 * 60 * 1000) return response("8000", "[TIME OVER] 인증을 다시 요청해 주세요.");
    	
    	paramMap.put("queryId", "loginMapper_authResUpdate2");
    	paramMap.put("ip", (request.getHeader("X-FORWARDED-FOR") != null) ? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr());
    	update(paramMap);
    	
    	paramMap.put("queryId", "loginMapper_authResUpdate3");
    	update(paramMap);
    	
    	request.getSession().setAttribute("SESSION_ADM_LOGINSTATE", "Y");
    	
    	return response("0000", "인증성공! 로그인이 가능한 IP입니다.");
    }
    
    

    public CommonResponseDto loginResponse(LoginRequestDto req, String cd, String msg) throws Exception {
    	
    	Map<String, String> paramMap = new HashMap<>();
    	paramMap.put("queryId", "loginMapper_accessLog");
    	paramMap.put("id", req.getId());
    	paramMap.put("adminName", req.getAdminName());
    	paramMap.put("adminType", req.getAdminType());
    	paramMap.put("browser", req.getBrowser());
    	paramMap.put("serverName", req.getServerName());
    	paramMap.put("ip", req.getIp());
    	paramMap.put("resultCd", cd);
    	insert(paramMap);
    	return response(cd, msg);
    }
    
    public CommonResponseDto logout(HttpSession session) throws Exception {
    	session.invalidate();    	
    	return response("0000", "success");
    }
    
    public String getUserAgent(HttpServletRequest request) throws Exception {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent.contains("Swing")) return "SWING";
		if (userAgent.contains("rv:11.0")) return "IE11";
		if (userAgent.contains("MSIE 11.0")) return "IE11";
		if (userAgent.contains("MSIE 10.0")) return "IE10";
		if (userAgent.contains("MSIE 9.0")) return "IE9";
		if (userAgent.contains("MSIE 8.0")) return "IE8";
		if (userAgent.contains("MSIE 7.0")) return (userAgent.contains("Trident")) ? "IE호환" : "IE7"; 			
		if (userAgent.contains("MSIE 6.0")) return "IE6";
		if (userAgent.contains("Edge")) return "EDGE";
		if (userAgent.contains("Android")) return "ANDROID";
		if (userAgent.contains("iPhone")) return "IPHONE";
		if (userAgent.contains("iPad")) return "IPAD";
		if (userAgent.contains("iPod")) return "IPOD";
		if (userAgent.contains("Firefox")) return "FIREFOX";
		if (userAgent.contains("OPR")) return "OPERA";
		if (userAgent.contains("Chrome")) return "CHROME";
		if(userAgent.length() > 20 ) return userAgent.substring(0, 20);
		return userAgent;
	}
	
}