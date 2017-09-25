package com.hsl.oa.controller.action.admin.base.projectGroup;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.annotation.Resource;
import net.sf.json.JSONArray;

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
import com.hsl.oa.plugins.ViewData;
import com.hsl.oa.execl.CommonExcel;
import com.hsl.oa.domain.admin.base.basestaff.BaseStaffService;
import com.hsl.oa.domain.admin.base.depinfo.DepartmentService;
import com.hsl.oa.domain.admin.base.projectGroup.BaseProjectGroupService;
import com.hsl.oa.domain.entity.admin.system.Role;

/** 
 * @title:BaseProjectGroupController
 * @description:项目组成员 控制器（新增、修改、删除）
 * @author:View
 * @date:2017-09-13
 */
@Controller
@RequestMapping(value="/baseprojectgroup")
public class BaseProjectGroupController extends BaseController {
	
	String menuUrl = "projectGroup/list.do"; //菜单地址(权限用)
	@Resource(name="baseProjectGroupService")
	private BaseProjectGroupService baseprojectgroupService;
	@Resource(name="baseStaffService")
	private BaseStaffService baseStaffService;
	@Resource(name="departmentService")
	private DepartmentService departmentService;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"新增BaseProjectGroup");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		views.put("groupId", this.get32UUID());	//主键
		baseprojectgroupService.add(views);
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
		logBefore(logger, GlobalPower.getUsername()+"删除BaseProjectGroup");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		ViewData views = new ViewData();
		views = this.getViewData();
		baseprojectgroupService.delete(views);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"修改BaseProjectGroup");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		baseprojectgroupService.edit(views);
		modelAndView.addObject("msg","success");
		modelAndView.setViewName("save_result");
		return modelAndView;
	}
	
	/**列表
	 * @throws Exception
	 */
	@RequestMapping(value="/getlist")
	public ModelAndView getlist() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"列表BaseProjectGroup");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		if(pd.getString("depId") == null || "".equals(pd.getString("depId").trim())){
			pd.put("depId", "a6c6695217ba4a4dbfe9f7e9d2c06730");//hsl depId										//默认列出第一组角色(初始设计系统用户和会员组不能删除)
		}
		
	 	ViewData depInfo = departmentService.findById(pd);  //部门信息
		ViewData fpd = new ViewData();
		fpd.put("isDepId",depInfo.getString("depId"));
		
		List<ViewData> staffProjectList = baseStaffService.listStaffProject(fpd);	//列出所有项目成员
		//List<ViewData> baseProjectDepList = baseprojectgroupService.listAll(pd);	//列出所有部门项目成员关联数据

		mv.addObject("pd", pd);
		mv.addObject("parentId",depInfo.getString("parentId"));
		mv.addObject("depName",depInfo.getString("depNm"));
		mv.addObject("depInfoId",depInfo.getString("depId"));
		mv.addObject("projectStafflist", staffProjectList);
		//mv.addObject("projectDeplist", baseProjectDepList);
		mv.addObject("PowerButton",GlobalPower.getHC());	//按钮权限
		mv.setViewName("base/projectGroup/baseprojectgroup_list");
		return mv;
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list(Page page) throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"列表BaseProjectGroup");
		//if(!GlobalPower.buttonJurisdiction(menuUrl, "seach")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		String keywords = views.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			views.put("keywords", keywords.trim());
		}
		page.setPd(views);
		List<ViewData>	varList = baseprojectgroupService.list(page);	//列出BaseProjectGroup列表
		modelAndView.setViewName("base/projectGroup/baseprojectgroup_list");
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
		modelAndView.setViewName("base/projectGroup/baseprojectgroup_edit");
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
		views = baseprojectgroupService.findById(views);	//根据ID读取
		modelAndView.setViewName("base/projectGroup/baseprojectgroup_edit");
		modelAndView.addObject("msg", "edit");
		modelAndView.addObject("views", views);
		return modelAndView;
	}	
	
	/**关联项目组成员
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/powerState")
	@ResponseBody
	public Object powerState()throws Exception{
		//if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"分配项目组成员");
		Map<String,String> map = new HashMap<String,String>();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		String errInfo = "success";
		
		if(null != baseprojectgroupService.findById(pd)){	//判断关联表是否有数据 是:删除/否:新增
			baseprojectgroupService.delete(pd);		//删除
		}else{
			pd.put("groupId", this.get32UUID());	//主键
			pd.put("valid","1"); //默认：有效
			baseprojectgroupService.add(pd);		//新增
		}
		map.put("result", errInfo);
		return OpenValidateApi.returnObject(new ViewData(), map);
	}
	
	 /**批量删除
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/deleteAll")
	@ResponseBody
	public Object deleteAll() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"批量删除BaseProjectGroup");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		ViewData views = new ViewData();		
		Map<String,Object> map = new HashMap<String,Object>();
		views = this.getViewData();
		List<ViewData> dataList = new ArrayList<ViewData>();
		String arrayIds = views.getString("arrayIds");
		if(null != arrayIds && !"".equals(arrayIds)){
			String arrayDataIds[] = arrayIds.split(",");
			baseprojectgroupService.deleteAll(arrayDataIds);
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
		logBefore(logger, GlobalPower.getUsername()+"导出BaseProjectGroup到excel");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "seach")){return null;}
		ModelAndView modelAndView = new ModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("ID");	//1
		titles.add("部门ID");	//2
		titles.add("组员ID");	//3
		titles.add("更新时间");	//4
		titles.add("使用状态（1 有效 0 失效）");	//5
		dataMap.put("titles", titles);
		List<ViewData> varOList = baseprojectgroupService.listAll(views);
		List<ViewData> varList = new ArrayList<ViewData>();
		for(int i=0;i<varOList.size();i++){
			ViewData vpd = new ViewData();
			vpd.put("var1", varOList.get(i).getString("groupId"));	    //1
			vpd.put("var2", varOList.get(i).getString("depId"));	    //2
			vpd.put("var3", varOList.get(i).getString("userId"));	    //3
			vpd.put("var4", varOList.get(i).getString("updDt"));	    //4
			vpd.put("var5", varOList.get(i).getString("valid"));	    //5
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
