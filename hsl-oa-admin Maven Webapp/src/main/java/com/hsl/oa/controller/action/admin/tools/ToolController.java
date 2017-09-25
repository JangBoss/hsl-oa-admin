/**
 * 
 */
package com.hsl.oa.controller.action.admin.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.api.OpenValidateApi;
import com.hsl.oa.common.BaseController;
import com.hsl.oa.plugins.ViewData;

/**
 * @title:ToolController 
 * @description:工具控制器
 * @author:View
 * @date:2017-9-14 上午10:50:23
 */
@Controller
@RequestMapping(value="/tool")
public class ToolController extends BaseController {

	/**去接口测试页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/interfaceTest")
	public ModelAndView editEmail() throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		mv.setViewName("system/tools/interfaceTest");
		mv.addObject("pd", pd);
		return mv;
	}
	
	/**
	 *	接口内部请求
	 * @param 
	 * @throws UnsupportedEncodingException 
	 * @throws Exception
	 */
	@RequestMapping(value="/severTest")
	@ResponseBody
	public Object severTest() throws UnsupportedEncodingException{
		Map<String,String> map = new HashMap<String,String>();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		String errInfo = "success",str = "",rTime="";
		try{
			long startTime = System.currentTimeMillis(); 					//请求起始时间_毫秒
			URL url = new URL(pd.getString("serverUrl"));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(pd.getString("requestMethod"));		//请求类型  POST or GET	
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
			long endTime = System.currentTimeMillis(); 						//请求结束时间_毫秒
			String temp = "";
			while((temp = in.readLine()) != null){ 
				str = str + temp;
			}
			rTime = String.valueOf(endTime - startTime); 
		}
		catch(Exception e){
			errInfo = "error";
		}
		map.put("errInfo", errInfo);//状态信息
		map.put("result", str);		//返回结果
		map.put("rTime", rTime);	//服务器请求时间 毫秒
		return OpenValidateApi.returnObject(new ViewData(), map);
	}	
}
