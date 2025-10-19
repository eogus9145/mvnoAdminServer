//package com.telesoft.mvnogen.filter;
//
//import jakarta.servlet.Filter;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpServletResponseWrapper;
//
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class CommonRequestFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//		
//    	HttpServletRequest hRequest = (HttpServletRequest) request;
//		
//		ModifiableHttpServletRequest nabsysRequest = new ModifiableHttpServletRequest((HttpServletRequest) request);
//		HttpServletResponseWrapper hResponse = new HttpServletResponseWrapper((HttpServletResponse) response);
//
//		String ip = hRequest.getHeader("X-FORWARDED-FOR");
//		if (ip == null) ip = request.getRemoteAddr();
//		nabsysRequest.setParameter("SESSION_ADM_IP", ip);
//		
//		String browser = "";
//		String userAgent = ((HttpServletRequest) request).getHeader("User-Agent");
//		browser = get_user_agent(userAgent);
//		
//		nabsysRequest.setParameter("SESSION_ADM_BROWSER",(String) browser);	
//		
//		nabsysRequest.setParameter("SESSION_ADM_SERVER_NAME", request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort());		
//		
//		nabsysRequest.setParameter("SESSION_ADM_CALLPAGEURL", ((HttpServletRequest) request).getRequestURI());
//		
//		if(hRequest.getSession().getAttribute("SESSION_ADM_ID") == null) {
//			nabsysRequest.setParameter("SESSION_ADM_ID","");
//		}else {
//			nabsysRequest.setParameter("SESSION_ADM_ID", hRequest.getSession().getAttribute("SESSION_ADM_ID").toString());
//		}
//		
//		if(hRequest.getSession().getAttribute("SESSION_ADM_NM") == null) {
//			nabsysRequest.setParameter("SESSION_ADM_NM","");
//		}else {
//			nabsysRequest.setParameter("SESSION_ADM_NM", hRequest.getSession().getAttribute("SESSION_ADM_NM").toString());
//		}
//		
//		if(hRequest.getSession().getAttribute("SESSION_ADM_AGENT_ID") == null) {
//			nabsysRequest.setParameter("SESSION_ADM_AGENT_ID","");
//		}else {
//			nabsysRequest.setParameter("SESSION_ADM_AGENT_ID", hRequest.getSession().getAttribute("SESSION_ADM_AGENT_ID").toString());
//		}
//		
//		if(hRequest.getSession().getAttribute("SESSION_ADM_DB_SESSION") == null) {
//			nabsysRequest.setParameter("SESSION_ADM_DB_SESSION","");
//		}else {
//			nabsysRequest.setParameter("SESSION_ADM_DB_SESSION", hRequest.getSession().getAttribute("SESSION_ADM_DB_SESSION").toString());
//		}
//		
//		if(hRequest.getSession().getAttribute("SESSION_ADM_MASKINGLIST") == null) {
//			nabsysRequest.setParameter("SESSION_ADM_MASKINGLIST","");
//		}else {
//			nabsysRequest.setParameter("SESSION_ADM_MASKINGLIST", hRequest.getSession().getAttribute("SESSION_ADM_MASKINGLIST").toString());
//		}
//		
//		if(hRequest.getSession().getAttribute("SESSION_ADM_ETC_AUTH") == null || !hRequest.getSession().getAttribute("SESSION_ADM_ETC_AUTH").toString().contains("|ETC_CHK|")) {
//			nabsysRequest.setParameter("SESSION_ADM_ETC_AUTH","");
//		}else {
//			nabsysRequest.setParameter("SESSION_ADM_ETC_AUTH",hRequest.getSession().getAttribute("SESSION_ADM_ETC_AUTH").toString());
//		}
//		
//		if(hRequest.getSession().getAttribute("SESSION_ADM_GRADE") == null) {
//			nabsysRequest.setParameter("SESSION_ADM_GRADE","");
//		}else {
//			nabsysRequest.setParameter("SESSION_ADM_GRADE",hRequest.getSession().getAttribute("SESSION_ADM_GRADE").toString());
//		}
//		
//		chain.doFilter(new ModifiableHttpServletRequest((HttpServletRequest)nabsysRequest), (HttpServletResponse) hResponse);
//    }
//
//	public String get_user_agent(String userAgent) {
//		String retVal = "";
//		if (userAgent.contains("Swing")) {
//			retVal = "SWING";
//		} else if (userAgent.contains("rv:11.0")) {
//			retVal = "IE11";
//		} else if (userAgent.contains("MSIE 11.0")) {
//			retVal = "IE11";
//		} else if (userAgent.contains("MSIE 10.0")) {
//			retVal = "IE10";
//		} else if (userAgent.contains("MSIE 9.0")) {
//			retVal = "IE9";
//		} else if (userAgent.contains("MSIE 8.0")) {
//			retVal = "IE8";
//		} else if (userAgent.contains("MSIE 7.0")) {
//			if (userAgent.contains("Trident")) {
//				retVal = "IE호환";
//			} else {
//				retVal = "IE7";
//			}
//		} else if (userAgent.contains("MSIE 6.0")) {
//			retVal = "IE6";
//		} else if (userAgent.contains("Edge")) {
//			retVal = "EDGE";
//		} else if (userAgent.contains("Android")) {
//			retVal = "ANDROID";
//		} else if (userAgent.contains("iPhone")) {
//			retVal = "IPHONE";
//		} else if (userAgent.contains("iPad")) {
//			retVal = "IPAD";
//		} else if (userAgent.contains("iPod")) {
//			retVal = "IPOD";
//		} else if (userAgent.contains("Firefox")) {
//			retVal = "FIREFOX";
//		} else if (userAgent.contains("OPR")) {
//			retVal = "OPERA";
//		} else if (userAgent.contains("Chrome")) {
//			retVal = "CHROME";
//		} else {
//			if(userAgent.length() > 20 ) {
//				retVal = userAgent.substring(0, 20);
//			}else {
//				retVal = userAgent;
//			}
//		}
//		return retVal;
//	}
//}
