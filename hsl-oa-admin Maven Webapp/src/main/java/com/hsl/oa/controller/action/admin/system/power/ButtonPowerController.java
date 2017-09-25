/**
 * 
 */
package com.hsl.oa.controller.action.admin.system.power;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.hsl.oa.common.global.GlobalPower;
import com.hsl.oa.domain.admin.system.buttonrights.ButtonPowerService;
import com.hsl.oa.domain.admin.system.fhbutton.FhbuttonService;
import com.hsl.oa.domain.admin.system.role.RoleService;
import com.hsl.oa.domain.entity.admin.system.Role;
import com.hsl.oa.plugins.ViewData;

/**
 * @title:ButtonPower 
 * @description:按钮权限
 * @author:View
 * @date:2017-5-7 下午10:49:14
 */
@Controller
@RequestMapping(value="/buttonrights")
public class ButtonPowerController extends BaseController{
	
	String menuUrl = "buttonrights/list.do"; //菜单地址(权限用)
	@Resource(name="buttonPowerService")
	private ButtonPowerService buttonrightsService;
	@Resource(name="roleService")
	private RoleService roleService;
	@Resource(name="fhbuttonService")
	private FhbuttonService fhbuttonService;
	
	/**列表
	 * @throws Exception
	 */
	@RequestMapping(value="/list")
	public ModelAndView list() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"列表Buttonrights");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		if(pd.getString("roleId") == null || "".equals(pd.getString("roleId").trim())){
			pd.put("roleId", "1");										//默认列出第一组角色(初始设计系统用户和会员组不能删除)
		}
		ViewData fpd = new ViewData();
		fpd.put("roleId", "0");
		List<Role> roleList = roleService.listAllRolesByPId(fpd);			//列出组(页面横向排列的一级组)
		List<Role> roleList_z = roleService.listAllRolesByPId(pd);			//列出此组下架角色
		List<ViewData> buttonlist = fhbuttonService.listAll(pd);			//列出所有按钮
		List<ViewData> roleFhbuttonlist = buttonrightsService.listAll(pd);	//列出所有角色按钮关联数据
		pd = roleService.findObjectById(pd);								//取得点击的角色组(横排的)
		mv.addObject("pd", pd);
		mv.addObject("roleList", roleList);
		mv.addObject("roleList_z", roleList_z);
		mv.addObject("buttonlist", buttonlist);
		mv.addObject("roleFhbuttonlist", roleFhbuttonlist);
		mv.addObject("PowerButtom",GlobalPower.getHC());	//按钮权限
		mv.setViewName("system/buttonrights/buttonrights_list");
		return mv;
	}
	
	/**点击按钮处理关联表
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/upRb")
	@ResponseBody
	public Object updateRolebuttonrightd()throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"分配按钮权限");
		Map<String,String> map = new HashMap<String,String>();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		String errInfo = "success";
		if(null != buttonrightsService.findById(pd)){	//判断关联表是否有数据 是:删除/否:新增
			buttonrightsService.delete(pd);		//删除
		}else{
			pd.put("rolebtnId", this.get32UUID());	//主键
			pd.put("infoStatus","0"); //默认：分类信息
			buttonrightsService.save(pd);		//新增
		}
		map.put("result", errInfo);
		return OpenValidateApi.returnObject(new ViewData(), map);
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
