package com.telesoft.mvnogen.common.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.telesoft.mvnogen.common.dto.CommonResponseDto;
import com.telesoft.mvnogen.common.service.CommonMenuService;
import com.telesoft.mvnogen.common.service.CommonService;
import com.telesoft.mvnogen.login.dto.LoginRequestDto;
import com.telesoft.mvnogen.login.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommonMenuController {
	
	private final CommonMenuService commonMenuService;
	
	@ResponseBody
	@PostMapping("/menuList.ajax")
	public CommonResponseDto selectListAjax(HttpServletRequest request, @RequestParam Map<String, Object> paramMap) throws Exception {
	    return commonMenuService.menuList(request, paramMap);
	}
	
	@ResponseBody
	@PostMapping("/searchMenuList.ajax")
	public CommonResponseDto searchMenuList(HttpServletRequest request, @RequestParam Map<String, Object> paramMap) throws Exception {
	    return commonMenuService.searchMenuList(request, paramMap);
	}
		
}
