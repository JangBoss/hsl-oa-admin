/**
 * 
 */
package com.hsl.oa.controller.action.admin.system.online;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.common.BaseController;
import com.hsl.oa.common.global.GlobalPower;

/**
 * @title:OnlineController 
 * @description:在线用户
 * @author:View
 * @date:2017-5-13 下午5:57:20
 */
@Controller
@RequestMapping(value="/onlinemanager")
public class OnlineController extends BaseController {
	
	String menuUrl = "onlinemanager/list.do"; //菜单地址(权限用)
	
	/**列表
	 * @return
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(){
		logBefore(logger, "列表OnlineManager");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		mv.setViewName("system/onlinemanager/onlinemanager_list");
		mv.addObject("PowerButtom",GlobalPower.getHC());	//按钮权限
		return mv;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
