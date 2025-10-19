package com.telesoft.mvnogen.common.web;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.telesoft.mvnogen.common.dto.CommonResponseDto;
import com.telesoft.mvnogen.common.service.CommonListService;
import com.telesoft.mvnogen.common.service.CommonService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommonListController {
	
	private final CommonListService commonListService;
	private final CommonService commonService;
	
	@ResponseBody
	@PostMapping("/selectPageList.ajax")
	public CommonResponseDto selectPageList(HttpServletRequest request, @RequestParam Map<String, Object> paramMap) throws Exception {
	    return commonListService.selectPageList(commonService.inParamMap(request, paramMap));
	}
	
}
