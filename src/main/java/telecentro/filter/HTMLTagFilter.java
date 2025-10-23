/*
 * Copyright 2008-2009 MOPAS(MINISTRY OF SECURITY AND PUBLIC ADMINISTRATION).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package telecentro.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HTMLTagFilter implements Filter{

	@SuppressWarnings("unused")
	private FilterConfig config;
	private ArrayList<String> urlList;
	
	//악성코드 목록
	static private String[] badTag = { 
		"script","onmouse","alert","Onmouseover","onclick","onblur", "onfocus", "onload", "onselect",
		"onsubmit", "onunload", "onabort", "onerror", "onmouseout", "onreset", "ondbclick", "ondragdrop",
		"onkeydown", "onkeypress", "onkeyup", "onmousedown", "onmousemove", "onmouseup", "onmove", "onresize","onMouseWheel","onmessage","ondblclick"
	};
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		String url = req.getServletPath();
		boolean allowedRequest = false;
		
		if(urlList.contains(url)) allowedRequest = true;
		
		if(!allowedRequest){
			chain.doFilter(new HTMLTagFilterRequestWrapper((HttpServletRequest)request), response);		
		}else{
					     
			Enumeration enums = req.getParameterNames();
			ModifiableHttpServletRequest nabsysRequest = new ModifiableHttpServletRequest((HttpServletRequest) request);
			while (enums.hasMoreElements()) {
				String paramName = (String) enums.nextElement();
				String[] parameters = request.getParameterValues(paramName);
				String value = request.getParameter(paramName);				
				for(String data : badTag){					
					if(value.contains(data)){
						value = value.replaceAll(data, "바뀌었습니다");
						nabsysRequest.setParameter(paramName,(String) value);
					}
				}
			}
			chain.doFilter(new ModifiableHttpServletRequest((HttpServletRequest)nabsysRequest), (HttpServletResponse) response);
		}
	}

	public void init(FilterConfig config) throws ServletException {
		String urls = config.getInitParameter("excludePatterns");
		StringTokenizer token = new StringTokenizer(urls, ",");
		urlList = new ArrayList<String>();
		while(token.hasMoreTokens()){
			urlList.add(token.nextToken());
		}
		this.config = config;
	}

	public void destroy() {
		//
	}
}
