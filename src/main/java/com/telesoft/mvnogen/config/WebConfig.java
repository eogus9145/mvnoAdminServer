package com.telesoft.mvnogen.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	// 로그인 인터셉터
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    
    	
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                Object loginState = request.getSession().getAttribute("SESSION_ADM_LOGINSTATE");
                if(loginState != "Y") {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("로그인 필요");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/**")
          .excludePathPatterns("/auth/**");
        
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            	String uri = request.getRequestURI();
            	Object user = request.getSession().getAttribute("user");
            	if(uri.startsWith("/auth") && user != null) {
            	    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            	    response.getWriter().write("이미 로그인됨");
            	    return false;
            	}
                return true;
            }
        }).addPathPatterns("/**");
        
        
    }
}