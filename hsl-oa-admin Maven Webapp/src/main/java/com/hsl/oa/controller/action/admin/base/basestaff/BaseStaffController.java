package com.hsl.oa.controller.action.admin.base.basestaff;

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
import com.hsl.oa.domain.admin.base.basestaff.BaseStaffService;
import com.hsl.oa.plugins.ViewData;
import com.hsl.oa.execl.CommonExcel;

/** 
 * @title:BaseStaffController
 * @description:员工信息 控制器（新增、修改、删除）
 * @author:View
 * @date:2017-09-18
 */
@Controller
@RequestMapping(value="/basestaff")
public class BaseStaffController extends BaseController {
	
	String menuUrl = "basestaff/list.do"; //菜单地址(权限用)
	@Resource(name="baseStaffService")
	private BaseStaffService basestaffService;
	
	/**保存
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"新增BaseStaff");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		views.put("BASESTAFFId", this.get32UUID());	//主键
		basestaffService.add(views);
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
		logBefore(logger, GlobalPower.getUsername()+"删除BaseStaff");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		ViewData views = new ViewData();
		views = this.getViewData();
		basestaffService.delete(views);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"修改BaseStaff");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		basestaffService.edit(views);
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
		logBefore(logger, GlobalPower.getUsername()+"列表BaseStaff");
		//if(!GlobalPower.buttonJurisdiction(menuUrl, "seach")){return null;} //校验权限(无权查看时页面会有提示,如果不注释掉这句代码就无法进入列表页面,所以根据情况是否加入本句代码)
		ModelAndView modelAndView = this.getModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		String keywords = views.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			views.put("keywords", keywords.trim());
		}
		page.setPd(views);
		List<ViewData>	varList = basestaffService.list(page);	//列出BaseStaff列表
		modelAndView.setViewName("base/staff/basestaff_list");
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
		modelAndView.setViewName("base/staff/basestaff_edit");
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
		views = basestaffService.findById(views);	//根据ID读取
		modelAndView.setViewName("base/staff/basestaff_edit");
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
		logBefore(logger, GlobalPower.getUsername()+"批量删除BaseStaff");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		ViewData views = new ViewData();		
		Map<String,Object> map = new HashMap<String,Object>();
		views = this.getViewData();
		List<ViewData> dataList = new ArrayList<ViewData>();
		String arrayIds = views.getString("arrayIds");
		if(null != arrayIds && !"".equals(arrayIds)){
			String arrayDataIds[] = arrayIds.split(",");
			basestaffService.deleteAll(arrayDataIds);
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
		logBefore(logger, GlobalPower.getUsername()+"导出BaseStaff到excel");
		if(!GlobalPower.buttonJurisdiction(menuUrl, "seach")){return null;}
		ModelAndView modelAndView = new ModelAndView();
		ViewData views = new ViewData();
		views = this.getViewData();
		Map<String,Object> dataMap = new HashMap<String,Object>();
		List<String> titles = new ArrayList<String>();
		titles.add("员工ID");	//1
		titles.add("员工姓名");	//2
		titles.add("固定ID，终身ID，胸牌ID");	//3
		titles.add("部门");	//4
		titles.add("职位名称：取数据字典");	//5
		titles.add("性别(0男，1女)");	//6
		titles.add("工龄");	//7
		titles.add("政治面貌：数据字典");	//8
		titles.add("民族");	//9
		titles.add("婚否(0否，1是)");	//10
		titles.add("出生日期");	//11
		titles.add("身份证号");	//12
		titles.add("手机号码");	//13
		titles.add("固定电话");	//14
		titles.add("Email");	//15
		titles.add("员工照片");	//16
		titles.add("身份证地址");	//17
		titles.add("现居地");	//18
		titles.add("籍贯");	//19
		titles.add("职称：取数据字典");	//20
		titles.add("文化程度：取数据字典");	//21
		titles.add("毕业院校");	//22
		titles.add("所学专业");	//23
		titles.add("毕业日期");	//24
		titles.add("入职日期");	//25
		titles.add("预计转正时间：取数据字典");	//26
		titles.add("当前状态(0离职，1在职)");	//27
		titles.add("工作地点");	//28
		titles.add("职业资格");	//29
		titles.add("备注信息");	//30
		titles.add("创建时间");	//31
		titles.add("更新时间");	//32
		dataMap.put("titles", titles);
		List<ViewData> varOList = basestaffService.listAll(views);
		List<ViewData> varList = new ArrayList<ViewData>();
		for(int i=0;i<varOList.size();i++){
			ViewData vpd = new ViewData();
			vpd.put("var1", varOList.get(i).getString("staffId"));	    //1
			vpd.put("var2", varOList.get(i).getString("staffNm"));	    //2
			vpd.put("var3", varOList.get(i).getString("staffNo"));	    //3
			vpd.put("var4", varOList.get(i).getString("depId"));	    //4
			vpd.put("var5", varOList.get(i).getString("postName"));	    //5
			vpd.put("var6", varOList.get(i).get("sex").toString());	//6
			vpd.put("var7", varOList.get(i).get("workYear").toString());	//7
			vpd.put("var8", varOList.get(i).getString("politics"));	    //8
			vpd.put("var9", varOList.get(i).getString("nation"));	    //9
			vpd.put("var10", varOList.get(i).get("married").toString());	//10
			vpd.put("var11", varOList.get(i).getString("birthday"));	    //11
			vpd.put("var12", varOList.get(i).getString("idCard"));	    //12
			vpd.put("var13", varOList.get(i).getString("mobile"));	    //13
			vpd.put("var14", varOList.get(i).getString("telphone"));	    //14
			vpd.put("var15", varOList.get(i).getString("email"));	    //15
			vpd.put("var16", varOList.get(i).getString("picture"));	    //16
			vpd.put("var17", varOList.get(i).getString("idAddr"));	    //17
			vpd.put("var18", varOList.get(i).getString("nowAddr"));	    //18
			vpd.put("var19", varOList.get(i).getString("originInfo"));	    //19
			vpd.put("var20", varOList.get(i).getString("jobTitile"));	    //20
			vpd.put("var21", varOList.get(i).getString("degree"));	    //21
			vpd.put("var22", varOList.get(i).getString("finishSchool"));	    //22
			vpd.put("var23", varOList.get(i).getString("major"));	    //23
			vpd.put("var24", varOList.get(i).getString("leaveDd"));	    //24
			vpd.put("var25", varOList.get(i).getString("tryDate"));	    //25
			vpd.put("var26", varOList.get(i).getString("expectDate"));	    //26
			vpd.put("var27", varOList.get(i).get("status").toString());	//27
			vpd.put("var28", varOList.get(i).getString("workplace"));	    //28
			vpd.put("var29", varOList.get(i).getString("vocation"));	    //29
			vpd.put("var30", varOList.get(i).getString("remarkDesc"));	    //30
			vpd.put("var31", varOList.get(i).getString("recDt"));	    //31
			vpd.put("var32", varOList.get(i).getString("updDt"));	    //32
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
