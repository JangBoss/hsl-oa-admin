/**
 * 
 */
package com.hsl.oa.controller.action.admin.system.funcbutton;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.api.OpenValidateApi;
import com.hsl.oa.common.BaseController;
import com.hsl.oa.common.Page;
import com.hsl.oa.common.global.GlobalPower;
import com.hsl.oa.domain.admin.system.fhbutton.FhbuttonService;
import com.hsl.oa.execl.CommonExcel;
import com.hsl.oa.plugins.ViewData;

/**
 * @title:FuncButtonController 
 * @description:按钮控制
 * @author:View
 * @date:2017-5-7 下午10:34:17
 */
@Controller
@RequestMapping(value="/fhbutton")
public class FuncButtonController extends BaseController{
   
	String menuUrl = "fhbutton/list.do"; //菜单地址(权限用)
	@Resource(name="fhbuttonService")
	private FhbuttonService fhbuttonService;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"新增Fhbutton");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		pd.put("buttonId", this.get32UUID());	//主键
		fhbuttonService.save(pd);
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**删除
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out) throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"删除Fhbutton");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		ViewData pd = new ViewData();
		pd = this.getViewData();
		fhbuttonService.delete(pd);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"修改Fhbutton");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		fhbuttonService.edit(pd);
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
		logBefore(logger, GlobalPower.getUsername()+"列表Fhbutton");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		page.setPd(pd);
		List<ViewData>	varList = fhbuttonService.list(page);	//列出Fhbutton列表
		mv.setViewName("system/fhbutton/fhbutton_list");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		mv.addObject("PowerButtom",GlobalPower.getHC());	//按钮权限
		return mv;
	}
	
	/**去新增页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goAdd")
	public ModelAndView goAdd()throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		mv.setViewName("system/fhbutton/fhbutton_edit");
		mv.addObject("msg", "save");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	 /**去修改页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goEdit")
	public ModelAndView goEdit()throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		pd = fhbuttonService.findById(pd);	//根据ID读取
		mv.setViewName("system/fhbutton/fhbutton_edit");
		mv.addObject("msg", "edit");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	 /**批量删除
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/deleteAll")
	@ResponseBody
	public Object deleteAll() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"批量删除Fhbutton");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		ViewData pd = new ViewData();		
		Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getViewData();
		List<ViewData> pdList = new ArrayList<ViewData>();
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			fhbuttonService.deleteAll(ArrayDATA_IDS);
			pd.put("msg", "ok");
		}else{
			pd.put("msg", "no");
		}
		pdList.add(pd);
		map.put("list", pdList);
		return OpenValidateApi.returnObject(pd, map);
	}
	
	 /**导出到excel
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/excel")
	public ModelAndView exportExcel() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"导出Fhbutton到excel");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "cha")){return null;}
		ModelAndView mv = new ModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("名称");	//1
		titles.add("权限标识");	//2
		titles.add("备注");	//3
		dataMap.put("titles", titles);
		List<ViewData> varOList = fhbuttonService.listAll(pd);
		List<ViewData> varList = new ArrayList<ViewData>();
		for(int i=0;i<varOList.size();i++){
			ViewData vpd = new ViewData();
			vpd.put("var1", varOList.get(i).getString("btnName"));	//1
			vpd.put("var2", varOList.get(i).getString("powerCode"));	//2
			vpd.put("var3", varOList.get(i).getString("remark"));	//3
			varList.add(vpd);
		}
		dataMap.put("varList", varList);
		CommonExcel erv = new CommonExcel();
		mv = new ModelAndView(erv,dataMap);
		return mv;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
