package com.hsl.oa.controller.action.admin.my.mytask;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import net.sf.json.JSONArray;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.api.OpenValidateApi;
import com.hsl.oa.common.BaseController;
import com.hsl.oa.common.Page;
import com.hsl.oa.common.global.GlobalPower;
import com.hsl.oa.domain.admin.my.mytask.MyTaskService;
import com.hsl.oa.execl.CommonExcel;
import com.hsl.oa.plugins.ViewData;

/** 
 * @title:我的项目任务 控制器
 * @description:
 * @author:View
 * @date:2017-09-12
 */
@Controller
@RequestMapping(value="/mytask")
public class MyTaskController extends BaseController {
	
	String menuUrl = "mytask/list.do"; //菜单地址(权限用)
	@Resource(name="myTaskService")
	private MyTaskService mytaskService;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"新增MyTask");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		views.put("MYTASKId", this.get32UUID());	//主键
		//mytaskService.add(views);
		modelAndView.addObject("msg","success");
		modelAndView.setViewName("save_result");
		return modelAndView;
	}
	
	/**删除
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out) throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"删除MyTask");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		ViewData views = new ViewData();
		views = this.getViewData();
		//mytaskService.delete(views);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"修改MyTask");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		//mytaskService.edit(views);
		modelAndView.addObject("msg","success");
		modelAndView.setViewName("save_result");
		return modelAndView;
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(Page page) throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"列表MyTask");
		//if(!GlobalPower.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		String keywords = views.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			views.put("keywords", keywords.trim());
		}
		page.setPd(views);
		List<ViewData>	varList = mytaskService.list(page);	//列出MyTask列表
		modelAndView.setViewName("my/mytask/mytask_list");
		modelAndView.addObject("varList", varList);
		modelAndView.addObject("views", views);
		modelAndView.addObject("PowerButton",GlobalPower.getHC());	//按钮权限
		return modelAndView;
	}
	
	/**去新增页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goAdd")
	public ModelAndView goAdd()throws Exception{
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		modelAndView.setViewName("admin/mytask/mytask_edit");
		modelAndView.addObject("msg", "save");
		modelAndView.addObject("views", views);
		return modelAndView;
	}	
	
	 /**去修改页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goEdit")
	public ModelAndView goEdit()throws Exception{
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		//views = mytaskService.findById(views);	//根据ID读取
		modelAndView.setViewName("admin/mytask/mytask_edit");
		modelAndView.addObject("msg", "edit");
		modelAndView.addObject("views", views);
		return modelAndView;
	}	
	
	 /**批量删除
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/deleteAll")
	@ResponseBody
	public Object deleteAll() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"批量删除MyTask");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		ViewData views = new ViewData();		
		Map<String,Object> map = new HashMap<String,Object>();
		views = this.getViewData();
		List<ViewData> dataList = new ArrayList<ViewData>();
		String arrayIds = views.getString("arrayIds");
		if(null != arrayIds && !"".equals(arrayIds)){
			String arrayDataIds[] = arrayIds.split(",");
			mytaskService.deleteAll(arrayDataIds);
			views.put("msg", "ok");
		}else{
			views.put("msg", "no");
		}
		dataList.add(views);
		map.put("list", dataList);
		return OpenValidateApi.returnObject(views, map);
	}
	
	 /**导出到excel
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/excel")
	public ModelAndView exportExcel() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"导出MyTask到excel");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "cha")){return null;}
		ModelAndView modelAndView = new ModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("任务ID");	//1
		titles.add("任务标题");	//2
		titles.add("任务内容");	//3
		titles.add("上级id");	//4
		titles.add("子任务层级：默认伟0 ，目前最多支持1层");	//5
		titles.add("负责人ID");	//6
		titles.add("创建用户ID");	//7
		titles.add("开始时间");	//8
		titles.add("结束时间");	//9
		titles.add("分配日期");	//10
		titles.add("完成日期");	//11
		titles.add("更新时间");	//12
		titles.add("使用状态：1已完成、2 未完成");	//13
		titles.add("使用状态（1 有效 0 失效）");	//14
		dataMap.put("titles", titles);
		List<ViewData> varOList = mytaskService.listAll(views);
		List<ViewData> varList = new ArrayList<ViewData>();
		for(int i=0;i<varOList.size();i++){
			ViewData vpd = new ViewData();
			vpd.put("var1", varOList.get(i).getString("taskId"));	    //1
			vpd.put("var2", varOList.get(i).getString("taskTitle"));	    //2
			vpd.put("var3", varOList.get(i).getString("taskContent"));	    //3
			vpd.put("var4", varOList.get(i).getString("parentId"));	    //4
			vpd.put("var5", varOList.get(i).get("taskLevel").toString());	//5
			vpd.put("var6", varOList.get(i).getString("bossMan"));	    //6
			vpd.put("var7", varOList.get(i).getString("userId"));	    //7
			vpd.put("var8", varOList.get(i).getString("startDt"));	    //8
			vpd.put("var9", varOList.get(i).getString("endDt"));	    //9
			vpd.put("var10", varOList.get(i).getString("recDt"));	    //10
			vpd.put("var11", varOList.get(i).getString("completeDt"));	    //11
			vpd.put("var12", varOList.get(i).getString("updDt"));	    //12
			vpd.put("var13", varOList.get(i).getString("states"));	    //13
			vpd.put("var14", varOList.get(i).getString("valid"));	    //14
			varList.add(vpd);
		}
		dataMap.put("varList", varList);
		CommonExcel erv = new CommonExcel();
		modelAndView = new ModelAndView(erv,dataMap);
		return modelAndView;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
