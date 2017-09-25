/**
 * 
 */
package com.hsl.oa.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hsl.oa.common.BaseController;

/**
 * @title:LoginFilter 
 * @description:登录过滤器
 * @author:View
 * @date:2017-4-19 下午7:56:35
 */
public class LoginFilter extends BaseController implements Filter {

	/**
	 * 初始化
	 */
	public void init(FilterConfig fc) throws ServletException {
		//FileUtil.createDir("d:/FH/topic/");
	}
	
	/**
	 * 销毁
	 */
	public void destroy() {

	}

	/**
	 * 过滤器
	 */
	public void doFilter(ServletRequest postRequest, ServletResponse postResponse,FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) postRequest;
		HttpServletResponse response = (HttpServletResponse) postResponse;
		chain.doFilter(postRequest, postResponse); // 调用下一过滤器
	}
}
