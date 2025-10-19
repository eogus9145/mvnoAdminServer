package com.telesoft.mvnogen.login.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.telesoft.mvnogen.common.dto.CommonResponseDto;
import com.telesoft.mvnogen.login.dto.AuthRequestDto;
import com.telesoft.mvnogen.login.dto.LoginRequestDto;
import com.telesoft.mvnogen.login.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {
	
	private final LoginService loginService;
	
	@GetMapping("/loginCheck.ajax")
	public CommonResponseDto loginCheck(HttpServletRequest request) throws Exception {
		CommonResponseDto res = loginService.loginCheck(request.getSession());
		return res;
	}	
	
    @GetMapping("/getSession.ajax")
    public CommonResponseDto getSession(HttpServletRequest request) throws Exception {
    	CommonResponseDto res = loginService.getSession(request.getSession());
        return res;
    }
    
    @GetMapping("/getRsaPublicKey.ajax")
    public CommonResponseDto getRsaPublicKey(HttpServletRequest request) throws Exception {
    	CommonResponseDto res = loginService.getRsaPublicKey(request);
    	return res;
    }
	
	@ResponseBody
	@PostMapping("/login.ajax")
	public CommonResponseDto login(HttpServletRequest request, @ModelAttribute LoginRequestDto req) throws Exception {
	    CommonResponseDto res = loginService.login(request, req);
	    return res;
	}
	
	@ResponseBody
	@PostMapping("/authRes.ajax")
	public CommonResponseDto authRes(HttpServletRequest request, @ModelAttribute AuthRequestDto req) throws Exception {
		CommonResponseDto res = loginService.authRes(request, req);
		return res;
	}
	
	@ResponseBody
	@PostMapping("/authReq.ajax")
	public CommonResponseDto authReq(HttpServletRequest request, @RequestParam Map<String, Object> paramMap) throws Exception {
		CommonResponseDto res = loginService.authReq(request, paramMap);
		return res;
	}
	
	@ResponseBody
	@PostMapping("/logout.ajax")
	public CommonResponseDto logout(HttpServletRequest request) throws Exception {
		CommonResponseDto res = loginService.logout(request.getSession());
		return res;
	}
	
	/*
	@ResponseBody
	@PostMapping("/signup.ajax")
	public CommonResponseDto signup(HttpServletRequest request, @ModelAttribute SignupRequestDto req, Model model) throws Exception {
		CommonResponseDto res = loginService.signup(req);
		return res;
	}
	*/
}
