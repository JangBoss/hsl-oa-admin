/**
 * 
 */
package com.hsl.oa.controller.action.admin.system.role;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.api.OpenValidateApi;
import com.hsl.oa.common.BaseController;
import com.hsl.oa.common.global.GlobalPower;
import com.hsl.oa.domain.admin.system.menu.MenuService;
import com.hsl.oa.domain.admin.system.role.RoleService;
import com.hsl.oa.domain.admin.system.user.UserService;
import com.hsl.oa.domain.entity.admin.system.Menu;
import com.hsl.oa.domain.entity.admin.system.Role;
import com.hsl.oa.plugins.ViewData;
import com.hsl.oa.power.RightsPower;
import com.hsl.oa.util.Tools;

/**
 * @title:RoleController 
 * @description:角色操作控制器（新增、修改、删除）
 * @author:View
 * @date:2017-5-7 下午10:18:33
 */
@Controller
@RequestMapping(value="/role")
public class RoleController  extends BaseController {

	String menuUrl = "role.do"; //菜单地址(权限用)
	@Resource(name="menuService")
	private MenuService menuService;
	@Resource(name="roleService")
	private RoleService roleService;
	@Resource(name="userService")
	private UserService userService;
	
	/** 进入权限首页
	 * @param 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping
	public ModelAndView list()throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		try{
			pd = this.getViewData();
			if(pd.getString("roleId") == null || "".equals(pd.getString("roleId").trim())){
				pd.put("roleId", "1");										//默认列出第一组角色(初始设计系统用户和会员组不能删除)
			}
			ViewData fpd = new ViewData();
			fpd.put("roleId", "0");
			List<Role> roleList = roleService.listAllRolesByPId(fpd);		//列出组(页面横向排列的一级组)
			List<Role> roleList_z = roleService.listAllRolesByPId(pd);		//列出此组下架角色
			pd = roleService.findObjectById(pd);							//取得点击的角色组(横排的)
			mv.addObject("pd", pd);
			mv.addObject("roleList", roleList);
			mv.addObject("roleList_z", roleList_z);
			mv.addObject("PowerButtom",GlobalPower.getHC());	//按钮权限
			mv.setViewName("system/role/role_list");
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/**去新增页面
	 * @param 
	 * @return
	 */
	@RequestMapping(value="/toAdd")
	public ModelAndView toAdd(){
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		try{
			pd = this.getViewData();
			mv.addObject("msg", "add");
			mv.setViewName("system/role/role_edit");
			mv.addObject("pd", pd);
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/**保存新增角色
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/add",method=RequestMethod.POST)
	public ModelAndView add()throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"新增角色");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		try{
			pd = this.getViewData();
			String parentId = pd.getString("parentId");		//父类角色id
			pd.put("roleId", parentId);			
			if("0".equals(parentId)){
				pd.put("powerRights", "");							//菜单权限
			}else{
				String powerRights = roleService.findObjectById(pd).getString("powerRights");
				pd.put("powerRights", (null == powerRights)?"":powerRights);	//组菜单权限
			}
			pd.put("roleId", this.get32UUID());				//主键
			pd.put("addPower", "0");	//初始新增权限为否
			pd.put("delPower", "0");	//删除权限
			pd.put("editPower", "0");	//修改权限
			pd.put("seachPower", "0");	//查看权限
			roleService.add(pd);
		} catch(Exception e){
			logger.error(e.toString(), e);
			mv.addObject("msg","failed");
		}
		mv.setViewName("save_result");
		return mv;
	}
	
	/**请求编辑
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/toEdit")
	public ModelAndView toEdit( String roleId )throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		try{
			pd = this.getViewData();
			pd.put("roleId", roleId);
			pd = roleService.findObjectById(pd);
			mv.addObject("msg", "edit");
			mv.addObject("pd", pd);
			mv.setViewName("system/role/role_edit");
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/**保存修改
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit()throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"修改角色");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		try{
			pd = this.getViewData();
			roleService.edit(pd);
			mv.addObject("msg","success");
		} catch(Exception e){
			logger.error(e.toString(), e);
			mv.addObject("msg","failed");
		}
		mv.setViewName("save_result");
		return mv;
	}
	
	/**删除角色
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	@ResponseBody
	public Object deleteRole(@RequestParam String roleId)throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"删除角色");
		Map<String,String> map = new HashMap<String,String>();
		ViewData pd = new ViewData();
		String errInfo = "";
		try{
			pd.put("roleId", roleId);
			List<Role> roleList_z = roleService.listAllRolesByPId(pd);		//列出此部门的所有下级
			if(roleList_z.size() > 0){
				errInfo = "false";											//下级有数据时，删除失败
			}else{
				List<ViewData> userlist = userService.listAllUserByRoldId(pd);			//此角色下的用户
				if(userlist.size() > 0 ){						//此角色已被使用就不能删除
					errInfo = "false2";
				}else{
				roleService.deleteRoleById(roleId);	//执行删除
				errInfo = "success";
				}
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		map.put("result", errInfo);
		return OpenValidateApi.returnObject(new ViewData(), map);
	}
	
	/**
	 * 显示菜单列表ztree(菜单授权菜单)
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/menuQx")
	public ModelAndView listAllMenu(Model model,String roleId)throws Exception{
		ModelAndView mv = this.getModelAndView();
		try{
			Role role = roleService.getRoleById(roleId);			//根据角色ID获取角色对象
			String roleRights = role.getPowerRights();				//取出本角色菜单权限
			List<Menu> menuList = menuService.listAllMenuQx("0");	//获取所有菜单
			menuList = this.readMenu(menuList, roleRights);			//根据角色权限处理菜单权限状态(递归处理)
			JSONArray arr = JSONArray.fromObject(menuList);
			String json = arr.toString();
			json = json.replaceAll("menuId", "id").replaceAll("parentId", "pId").replaceAll("menuName", "name").replaceAll("subMenu", "nodes").replaceAll("hasMenu", "checked");
			model.addAttribute("zTreeNodes", json);
			mv.addObject("roleId",roleId);
			mv.setViewName("system/role/menuqx");
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/**保存角色菜单权限
	 * @param roleId 角色ID
	 * @param menuIds 菜单ID集合
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/saveMenuqx")
	public void saveMenuPowerButtom(@RequestParam String roleId,@RequestParam String menuIds,PrintWriter out)throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"修改菜单权限");
		ViewData pd = new ViewData();
		try{
			if(null != menuIds && !"".equals(menuIds.trim())){
				BigInteger powerRights = RightsPower.sumRights(Tools.str2StrArray(menuIds));//用菜单ID做权处理
				Role role = roleService.getRoleById(roleId);	//通过id获取角色对象
				role.setPowerRights(powerRights.toString());
				roleService.updateRoleRights(role);				//更新当前角色菜单权限
				pd.put("powerRights",powerRights.toString());
			}else{
				Role role = new Role();
				role.setPowerRights("");
				role.setRoleId(roleId);
				roleService.updateRoleRights(role);				//更新当前角色菜单权限(没有任何勾选)
				pd.put("powerRights","");
			}
				pd.put("roleId", roleId);
				roleService.setAllRights(pd);					//更新此角色所有子角色的菜单权限
			out.write("success");
			out.close();
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
	}

	/**请求角色按钮授权页面(增删改查)
	 * @param roleId： 角色ID
	 * @param msg： 区分增删改查
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/b4Button")
	public ModelAndView b4Button(@RequestParam String roleId,@RequestParam String msg,Model model)throws Exception{
		ModelAndView mv = this.getModelAndView();
		try{
			List<Menu> menuList = menuService.listAllMenuQx("0"); //获取所有菜单
			Role role = roleService.getRoleById(roleId);		  //根据角色ID获取角色对象
			String roleRights = "";
			if("add_PowerButtom".equals(msg)){
				roleRights = role.getAddPower();	//新增权限
			}else if("del_PowerButtom".equals(msg)){
				roleRights = role.getDelPower();	//删除权限
			}else if("edit_PowerButtom".equals(msg)){
				roleRights = role.getEditPower();	//修改权限
			}else if("cha_PowerButtom".equals(msg)){
				roleRights = role.getSeachPower();	//查看权限
			}
			menuList = this.readMenu(menuList, roleRights);		//根据角色权限处理菜单权限状态(递归处理)
			JSONArray arr = JSONArray.fromObject(menuList);
			String json = arr.toString();
			json = json.replaceAll("menuId", "id").replaceAll("parentId", "pId").replaceAll("menuName", "name").replaceAll("subMenu", "nodes").replaceAll("hasMenu", "checked");
			model.addAttribute("zTreeNodes", json);
			mv.addObject("roleId",roleId);
			mv.addObject("msg", msg);
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		mv.setViewName("system/role/b4Button");
		return mv;
	}
	
	/**根据角色权限处理权限状态(递归处理)
	 * @param menuList：传入的总菜单
	 * @param roleRights：加密的权限字符串
	 * @return
	 */
	public List<Menu> readMenu(List<Menu> menuList,String roleRights){
		for(int i=0;i<menuList.size();i++){
			menuList.get(i).setHasMenu(RightsPower.testRights(roleRights, menuList.get(i).getMenuId()));
			this.readMenu(menuList.get(i).getSubMenu(), roleRights);					//是：继续排查其子菜单
		}
		return menuList;
	}
	
	/**
	 * 保存角色按钮权限
	 * @param roleId
	 * @param menuIds
	 * @param msg
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/saveB4Button")
	public void saveB4Button(@RequestParam String roleId,@RequestParam String menuIds,@RequestParam String msg,PrintWriter out)throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"修改"+msg+"权限");
		ViewData pd = new ViewData();
		pd = this.getViewData();
		try{
			if(null != menuIds && !"".equals(menuIds.trim())){
				BigInteger powerRights = RightsPower.sumRights(Tools.str2StrArray(menuIds));
				pd.put("value",powerRights.toString());
			}else{
				pd.put("value","");
			}
			pd.put("roleId", roleId);
			roleService.saveB4Button(msg,pd);
			out.write("success");
			out.close();
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
	}	
}