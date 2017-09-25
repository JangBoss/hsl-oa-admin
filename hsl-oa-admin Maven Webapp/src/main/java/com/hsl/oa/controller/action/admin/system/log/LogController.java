/**
 * 
 */
package com.hsl.oa.controller.action.admin.system.log;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.common.BaseController;
import com.hsl.oa.common.Page;
import com.hsl.oa.common.global.GlobalPower;
import com.hsl.oa.domain.admin.system.log.LogService;
import com.hsl.oa.plugins.ViewData;

/**
 * @title:LogController 
 * @description:日志记录
 * @author:View
 * @date:2017-8-31 下午5:05:51
 */
@Controller
@RequestMapping(value="/log")
public class LogController extends BaseController{

	String menuUrl = "log.do"; //菜单地址(权限用)
	@Resource(name="logService")
	private LogService logService;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"新增logService");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		pd.put("addreeIp", this.get32UUID());	//ip
		logService.add(pd);
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(Page page) throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"列表Log");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		page.setPd(pd);
		List<ViewData>	varList = logService.list(page);	//列出Log列表
		mv.setViewName("system/log/log_list");
		mv.addObject("varList", varList);
		mv.addObject("PowerButtom",GlobalPower.getHC());					//按钮权限
		return mv;
	}
	
}
