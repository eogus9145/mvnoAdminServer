package telecentro.common.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import telecentro.common.dto.CommonResponseDto;
import telecentro.common.service.CommonMenuService;
import telecentro.common.service.CommonService;
import telecentro.login.dto.LoginRequestDto;
import telecentro.login.service.LoginService;

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
