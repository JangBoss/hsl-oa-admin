/**
 * 
 */
package com.hsl.oa.controller.action.admin.system.user;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.fileupload.FileUpload;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.api.OpenValidateApi;
import com.hsl.oa.common.BaseController;
import com.hsl.oa.common.Page;
import com.hsl.oa.common.global.GlobalPower;
import com.hsl.oa.consts.Const;
import com.hsl.oa.date.DateUtil;
import com.hsl.oa.domain.admin.system.menu.MenuService;
import com.hsl.oa.domain.admin.system.role.RoleService;
import com.hsl.oa.domain.admin.system.user.UserService;
import com.hsl.oa.domain.entity.admin.system.Menu;
import com.hsl.oa.domain.entity.admin.system.Role;
import com.hsl.oa.domain.entity.admin.system.User;
import com.hsl.oa.execl.CommonExcel;
import com.hsl.oa.execl.ReadExcel;
import com.hsl.oa.file.FileDownload;
import com.hsl.oa.path.PathUtil;
import com.hsl.oa.plugins.ViewData;
import com.hsl.oa.util.PinYin;
import com.hsl.oa.util.Tools;

/**
 * @title:UserActionController 
 * @description:用户操作控制器（新增、修改、删除）
 * @author:View
 * @date:2017-4-19 下午5:16:36
 */
@Controller
@RequestMapping(value="/user")
public class UserActionController extends BaseController{

	String menuUrl = "user/listUsers.do"; //菜单地址(权限用)
	@Resource(name="userService")
	private UserService userService;
	@Resource(name="roleService")
	private RoleService roleService;
	@Resource(name="menuService")
	private MenuService menuService;
	
	/**显示用户列表
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/listUsers")
	public ModelAndView listUsers(Page page)throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		String keywords = pd.getString("keywords");				//关键词检索条件
		if(null != keywords && !"".equals(keywords)){
			pd.put("keywords", keywords.trim());
		}
		String lastLoginStart = pd.getString("lastLoginStart");	//开始时间
		String lastLoginEnd = pd.getString("lastLoginEnd");		//结束时间
		if(lastLoginStart != null && !"".equals(lastLoginStart)){
			pd.put("lastLoginStart", lastLoginStart+" 00:00:00");
		}
		if(lastLoginEnd != null && !"".equals(lastLoginEnd)){
			pd.put("lastLoginEnd", lastLoginEnd+" 00:00:00");
		} 
		page.setPd(pd);
		List<ViewData>	userList = userService.listUsers(page);	//列出用户列表
		pd.put("roleId", "1");
		List<Role> roleList = roleService.listAllRolesByPId(pd);//列出所有系统用户角色
		mv.setViewName("system/user/user_list");
		mv.addObject("userList", userList);
		mv.addObject("roleList", roleList);
		mv.addObject("pd", pd);
		mv.addObject("PowerButtom",GlobalPower.getHC());	//按钮权限
		return mv;
	}
	
	/**删除用户
	 * @param out
	 * @throws Exception 
	 */
	@RequestMapping(value="/deleteU")
	public void deleteU(PrintWriter out) throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return;} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"删除user");
		ViewData pd = new ViewData();
		pd = this.getViewData();
		userService.deleteU(pd);
		out.write("success");
		out.close();
	}
	
	/**去新增用户页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goAddU")
	public ModelAndView goAddU()throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		pd.put("roleId", "1");
		List<Role> roleList = roleService.listAllRolesByPId(pd);//列出所有系统用户角色
		mv.setViewName("system/user/user_edit");
		mv.addObject("msg", "saveU");
		mv.addObject("pd", pd);
		mv.addObject("roleList", roleList);
		return mv;
	}
	
	/**保存用户
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/saveU")
	public ModelAndView saveU() throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "add")){return null;} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"新增user");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		pd.put("userId", this.get32UUID());	    //ID 主键
		pd.put("lastLogin", "");				//最后登录时间
		pd.put("addreeIp", "");				    //IP
		pd.put("states", "0");					//状态
		pd.put("recDt", DateUtil.getTime().toString()); //获取程序当前时间作为创建时间
		pd.put("skin", "default");
		pd.put("powerRights", "");		
		pd.put("loginPwd", new SimpleHash("SHA-1", pd.getString("loginNm"), pd.getString("loginPwd")).toString());	//密码加密
		if(null == userService.findByUsername(pd)){	//判断用户名是否存在
			userService.saveStaffInfo(pd); 		    //执行保存用户+员工
			mv.addObject("msg","success");
		}else{
			mv.addObject("msg","failed");
		}
		mv.setViewName("save_result");
		return mv;
	}
	
	/**判断用户名是否存在
	 * @return
	 */
	@RequestMapping(value="/hasU")
	@ResponseBody
	public Object hasU(){
		Map<String,String> map = new HashMap<String,String>();
		String errInfo = "success";
		ViewData pd = new ViewData();
		try{
			pd = this.getViewData();
			if(userService.findByUsername(pd) != null){
				errInfo = "error";
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		map.put("result", errInfo);				//返回结果
		return OpenValidateApi.returnObject(new ViewData(), map);
	}
	
	/**判断邮箱是否存在
	 * @return
	 */
	@RequestMapping(value="/hasE")
	@ResponseBody
	public Object hasE(){
		Map<String,String> map = new HashMap<String,String>();
		String errInfo = "success";
		ViewData pd = new ViewData();
		try{
			pd = this.getViewData();
			if(userService.findByUE(pd) != null){
				errInfo = "error";
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		map.put("result", errInfo);				//返回结果
		return OpenValidateApi.returnObject(new ViewData(), map);
	}
	
	/**判断编码是否存在
	 * @return
	 */
	@RequestMapping(value="/hasN")
	@ResponseBody
	public Object hasN(){
		Map<String,String> map = new HashMap<String,String>();
		String errInfo = "success";
		ViewData pd = new ViewData();
		try{
			pd = this.getViewData();
			if(userService.findByUN(pd) != null){
				errInfo = "error";
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		map.put("result", errInfo);				//返回结果
		return OpenValidateApi.returnObject(new ViewData(), map);
	}
	
	/**去修改用户页面(系统用户列表修改)
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goEditU")
	public ModelAndView goEditU() throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		if("1".equals(pd.getString("userId"))){return null;}		//不能修改admin用户
		pd.put("roleId", "1");
		List<Role> roleList = roleService.listAllRolesByPId(pd);	//列出所有系统用户角色
		mv.addObject("fx", "user");
		pd = userService.findById(pd);								//根据ID读取
		mv.setViewName("system/user/user_edit");
		mv.addObject("msg", "editU");
		mv.addObject("pd", pd);
		mv.addObject("roleList", roleList);
		return mv;
	}
	
	/**去修改用户页面(个人修改)
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goEditMyInfo")
	public ModelAndView goEditMyInfo() throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		mv.addObject("fx", "head");
		pd.put("roleId", "1");
		List<Role> roleList = roleService.listAllRolesByPId(pd);	//列出所有系统用户角色
		pd.put("loginNm", GlobalPower.getUsername());
		pd = userService.findByUsername(pd);						//根据用户名读取
		mv.setViewName("system/user/user_edit");
		mv.addObject("msg", "editU");
		mv.addObject("pd", pd);
		mv.addObject("roleList", roleList);
		return mv;
	}
	
	/**查看用户
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/view")
	public ModelAndView view() throws Exception{
		if(!GlobalPower.buttonJurisdiction(menuUrl, "cha")){return null;} //校验权限
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		if("admin".equals(pd.getString("loginNm"))){return null;}	//不能查看admin用户
		pd.put("roleId", "1");
		List<Role> roleList = roleService.listAllRolesByPId(pd);	//列出所有系统用户角色
		pd = userService.findByUsername(pd);						//根据ID读取
		mv.setViewName("system/user/user_view");
		mv.addObject("msg", "editU");
		mv.addObject("pd", pd);
		mv.addObject("roleList", roleList);
		return mv;
	}
	
	/**去修改用户页面(在线管理页面打开)
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goEditUfromOnline")
	public ModelAndView goEditUfromOnline() throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		if("admin".equals(pd.getString("loginNm"))){return null;}	//不能查看admin用户
		pd.put("roleId", "1");
		List<Role> roleList = roleService.listAllRolesByPId(pd);	//列出所有系统用户角色
		pd = userService.findByUsername(pd);						//根据ID读取
		mv.setViewName("system/user/user_edit");
		mv.addObject("msg", "editU");
		mv.addObject("pd", pd);
		mv.addObject("roleList", roleList);
		return mv;
	}
	
	/**
	 * 修改用户
	 */
	@RequestMapping(value="/editU")
	public ModelAndView editU() throws Exception{
		logBefore(logger, GlobalPower.getUsername()+"修改ser");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		if(!GlobalPower.getUsername().equals(pd.getString("loginNm"))){		//如果当前登录用户修改用户资料提交的用户名非本人
			if(!GlobalPower.buttonJurisdiction(menuUrl, "cha")){return null;}  //校验权限 判断当前操作者有无用户管理查看权限
			if(!GlobalPower.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限判断当前操作者有无用户管理修改权限
			if("admin".equals(pd.getString("loginNm")) && !"admin".equals(GlobalPower.getUsername())){return null;}	//非admin用户不能修改admin
		}else{	//如果当前登录用户修改用户资料提交的用户名是本人，则不能修改本人的角色ID
			pd.put("roleId", userService.findByUsername(pd).getString("roleId")); //对角色ID还原本人角色ID
		}
		if(pd.getString("loginPwd") != null && !"".equals(pd.getString("loginPwd"))){
			pd.put("loginPwd", new SimpleHash("SHA-1", pd.getString("loginNm"), pd.getString("loginPwd")).toString());
		}
		userService.editU(pd);	//执行修改
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**
	 * 显示菜单列表ztree(项目成员)
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/getProjectUserInfo")
	public ModelAndView getProjectUserInfo(Model model,String depId)throws Exception{
		ModelAndView mv = this.getModelAndView();
		try{
			List<User> projectUserList = userService.listProjectUserInfo(); //获取用户信息
			JSONArray arr = JSONArray.fromObject(projectUserList);
			String json = arr.toString();
			json = json.replaceAll("userId", "id").replaceAll("parentId", "pId").replaceAll("userNm", "name").replaceAll("subMenu", "nodes").replaceAll("hasMenu", "checked");
			model.addAttribute("zTreeNodes", json);
			mv.setViewName("base/projectGroup/baseprojectgroup_ztree");
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/**
	 * 批量删除
	 * @throws Exception 
	 */
	@RequestMapping(value="/deleteAllU")
	@ResponseBody
	public Object deleteAllU() throws Exception {
		if(!GlobalPower.buttonJurisdiction(menuUrl, "del")){return null;} //校验权限
		logBefore(logger, GlobalPower.getUsername()+"批量删除user");
		ViewData pd = new ViewData();
		Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getViewData();
		List<ViewData> pdList = new ArrayList<ViewData>();
		String USER_IDS = pd.getString("USER_IDS");
		if(null != USER_IDS && !"".equals(USER_IDS)){
			String ArrayUSER_IDS[] = USER_IDS.split(",");
			userService.deleteAllU(ArrayUSER_IDS);
			pd.put("msg", "ok");
		}else{
			pd.put("msg", "no");
		}
		pdList.add(pd);
		map.put("list", pdList);
		return OpenValidateApi.returnObject(pd, map);
	}
	
	/**导出用户信息到EXCEL
	 * @return
	 */
	@RequestMapping(value="/excel")
	public ModelAndView exportExcel(){
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		try{
			if(GlobalPower.buttonJurisdiction(menuUrl, "cha")){
				String keywords = pd.getString("keywords");				//关键词检索条件
				if(null != keywords && !"".equals(keywords)){
					pd.put("keywords", keywords.trim());
				}
				String lastLoginStart = pd.getString("lastLoginStart");	//开始时间
				String lastLoginEnd = pd.getString("lastLoginEnd");		//结束时间
				if(lastLoginStart != null && !"".equals(lastLoginStart)){
					pd.put("lastLoginStart", lastLoginStart+" 00:00:00");
				}
				if(lastLoginEnd != null && !"".equals(lastLoginEnd)){
					pd.put("lastLoginEnd", lastLoginEnd+" 00:00:00");
				} 
				Map<String,Object> dataMap = new HashMap<String,Object>();
				List<String> titles = new ArrayList<String>();
				titles.add("用户名"); 		//1
				titles.add("编号");  		//2
				titles.add("姓名");			//3
				titles.add("职位");			//4
				titles.add("手机");			//5
				titles.add("邮箱");			//6
				titles.add("最近登录");		//7
				titles.add("上次登录IP");	//8
				dataMap.put("titles", titles);
				List<ViewData> userList = userService.listAllUser(pd);
				List<ViewData> varList = new ArrayList<ViewData>();
				for(int i=0;i<userList.size();i++){
					ViewData vpd = new ViewData();
					vpd.put("var1", userList.get(i).getString("loginNm"));		//1
					vpd.put("var2", userList.get(i).getString("NUMBER"));		//2
					vpd.put("var3", userList.get(i).getString("NAME"));			//3
					vpd.put("var4", userList.get(i).getString("ROLE_NAME"));	//4
					vpd.put("var5", userList.get(i).getString("PHONE"));		//5
					vpd.put("var6", userList.get(i).getString("EMAIL"));		//6
					vpd.put("var7", userList.get(i).getString("LAST_LOGIN"));	//7
					vpd.put("var8", userList.get(i).getString("IP"));			//8
					varList.add(vpd);
				}
				dataMap.put("varList", varList);
				CommonExcel erv = new CommonExcel();					//执行excel操作
				mv = new ModelAndView(erv,dataMap);
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/**打开上传EXCEL页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goUploadExcel")
	public ModelAndView goUploadExcel()throws Exception{
		ModelAndView mv = this.getModelAndView();
		mv.setViewName("system/user/uploadexcel");
		return mv;
	}
	
	/**下载模版
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/downExcel")
	public void downExcel(HttpServletResponse response)throws Exception{
		FileDownload.fileDownload(response, PathUtil.getClasspath() + Const.FILEPATHFILE + "Users.xls", "Users.xls");
	}
	
	/**从EXCEL导入到数据库
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/readExcel")
	public ModelAndView readExcel(
			@RequestParam(value="excel",required=false) MultipartFile file
			) throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		if(!GlobalPower.buttonJurisdiction(menuUrl, "add")){return null;}
		if (null != file && !file.isEmpty()) {
			String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE;								//文件上传路径
			String fileName =  com.hsl.oa.file.FileUpload.fileUp(file, filePath, "userexcel");							//执行上传
			List<ViewData> listPd = (List)ReadExcel.readExcel(filePath, fileName, 2, 0, 0);		//执行读EXCEL操作,读出的数据导入List 2:从第3行开始；0:从第A列开始；0:第0个sheet
			/*存入数据库操作======================================*/
			pd.put("RIGHTS", "");					//权限
			pd.put("LAST_LOGIN", "");				//最后登录时间
			pd.put("IP", "");						//IP
			pd.put("STATUS", "0");					//状态
			pd.put("SKIN", "default");				//默认皮肤
			pd.put("roleId", "1");
			List<Role> roleList = roleService.listAllRolesByPId(pd);//列出所有系统用户角色
			pd.put("roleId", roleList.get(0).getRoleId());		//设置角色ID为随便第一个
			/**
			 * var0 :编号
			 * var1 :姓名
			 * var2 :手机
			 * var3 :邮箱
			 * var4 :备注
			 */
			for(int i=0;i<listPd.size();i++){		
				pd.put("USER_ID", this.get32UUID());										//ID
				pd.put("NAME", listPd.get(i).getString("var1"));							//姓名
				
				String loginNm = PinYin.getPingYin(listPd.get(i).getString("var1"));	//根据姓名汉字生成全拼
				pd.put("loginNm", loginNm);	
				if(userService.findByUsername(pd) != null){									//判断用户名是否重复
					loginNm = PinYin.getPingYin(listPd.get(i).getString("var1"))+Tools.getRandomNum();
					pd.put("loginNm", loginNm);
				}
				pd.put("BZ", listPd.get(i).getString("var4"));								//备注
				if(Tools.checkEmail(listPd.get(i).getString("var3"))){						//邮箱格式不对就跳过
					pd.put("EMAIL", listPd.get(i).getString("var3"));						
					if(userService.findByUE(pd) != null){									//邮箱已存在就跳过
						continue;
					}
				}else{
					continue;
				}
				pd.put("NUMBER", listPd.get(i).getString("var0"));							//编号已存在就跳过
				pd.put("PHONE", listPd.get(i).getString("var2"));							//手机号
				
				pd.put("loginPwd", new SimpleHash("SHA-1", loginNm, "123").toString());	//默认密码123
				if(userService.findByUN(pd) != null){
					continue;
				}
				userService.saveU(pd);
			}
			/*存入数据库操作======================================*/
			mv.addObject("msg","success");
		}
		mv.setViewName("save_result");
		return mv;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}

}
