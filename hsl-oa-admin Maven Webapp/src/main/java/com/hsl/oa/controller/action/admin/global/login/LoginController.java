/**
 * 
 */
package com.hsl.oa.controller.action.admin.global.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.api.OpenValidateApi;
import com.hsl.oa.common.BaseController;
import com.hsl.oa.common.global.GlobalPower;
import com.hsl.oa.consts.Const;
import com.hsl.oa.date.DateUtil;
import com.hsl.oa.domain.admin.system.buttonrights.ButtonPowerService;
import com.hsl.oa.domain.admin.system.fhbutton.FhbuttonService;
import com.hsl.oa.domain.admin.system.log.LogService;
import com.hsl.oa.domain.admin.system.menu.MenuService;
import com.hsl.oa.domain.admin.system.role.RoleService;
import com.hsl.oa.domain.admin.system.user.UserService;
import com.hsl.oa.domain.entity.admin.system.Menu;
import com.hsl.oa.domain.entity.admin.system.Role;
import com.hsl.oa.domain.entity.admin.system.User;
import com.hsl.oa.plugins.ViewData;
import com.hsl.oa.power.SetPower;
import com.hsl.oa.util.IpUtil;
import com.hsl.oa.util.Tools;


/**
 * @title:LoginController 
 * @description:系统总入口
 * @author:View
 * @date:2017-4-19 下午8:45:52
 */
@Controller
public class LoginController extends BaseController{
  
	@Resource(name="userService")
	private UserService userService;
	@Resource(name="menuService")
	private MenuService menuService;
	@Resource(name="roleService")
	private RoleService roleService;
	@Resource(name="buttonPowerService")
	private ButtonPowerService buttonrightsService;
	@Resource(name="fhbuttonService")
	private FhbuttonService fhbuttonService;
	@Resource(name="logService")
	private LogService logService;
	
	/**访问登录页
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/login_toLogin")
	public ModelAndView toLogin()throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		pd.put("sysName", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
		mv.setViewName("system/index/login");
		mv.addObject("pd",pd);
		return mv;
	}
	
	/**请求登录，验证用户
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/login_login" ,produces="application/json;charset=UTF-8")
	@ResponseBody
	public Object login()throws Exception{
		Map<String,String> map = new HashMap<String,String>();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		String errInfo = "";
		String loginParams[] = pd.getString("KEYDATA").replaceAll("qViewfh", "").replaceAll("QQ978336446fh", "").split(",fh,");
		if(null != loginParams && loginParams.length == 3){
			Session session = GlobalPower.getSession();
			String sessionCode = (String)session.getAttribute(Const.SESSION_SECURITY_CODE);		//获取session中的验证码
			String code = loginParams[2];
			if(null == code || "".equals(code)){//判断效验码
				errInfo = "nullcode"; 			//效验码为空
			}else{
				String userName = loginParams[0];	//登录过来的用户名
				String passWord = loginParams[1];	//登录过来的密码
				pd.put("loginNm", userName);
				if(Tools.notEmpty(sessionCode) && sessionCode.equalsIgnoreCase(code)){		//判断登录验证码
					String passwd = new SimpleHash("SHA-1", userName, passWord).toString();	//密码加密
					pd.put("loginPwd", passwd);
					pd = userService.getUserByNameAndPwd(pd);	//根据用户名和密码去读取用户信息
					if(pd != null){
						pd.put("lastLogin",DateUtil.getTime().toString());
						userService.updateLastLogin(pd);
						User user = new User();
						user.setUserId(pd.getString("userId"));
						user.setLoginNm(pd.getString("loginNm"));
						user.setLoginPwd(pd.getString("loginPwd"));
						user.setUserNm(pd.getString("userNm"));
						user.setPowerRights(pd.getString("powerRights"));
						user.setRoleId(pd.getString("roleId"));
						user.setLastLogin(pd.getDate("lastLogin"));
						user.setAddreeIp(pd.getString("addreeIp"));
						user.setStates(Integer.parseInt(pd.getString("states")));
						session.setAttribute(Const.SESSION_USER, user);			//把用户信息放session中
						session.removeAttribute(Const.SESSION_SECURITY_CODE);	//清除登录验证码的session
						//shiro加入身份验证
						Subject subject = SecurityUtils.getSubject(); 
					    UsernamePasswordToken token = new UsernamePasswordToken(userName, passWord); 
					    try { 
					        subject.login(token); 
					    } catch (AuthenticationException e) { 
					    	errInfo = "身份验证失败！";
					    }
					}else{
						errInfo = "usererror"; 				//用户名或密码有误
						logBefore(logger, userName+"登录系统密码或用户名错误");
					}
				}else{
					errInfo = "codeerror";				 	//验证码输入有误
				}
				if(Tools.isEmpty(errInfo)){
					errInfo = "success";					//验证成功
					logBefore(logger, userName+"登录系统");
					ViewData log = new  ViewData();
					log.put("addreeIp",IpUtil.getIp());      //登录Ip
					log.put("optCont",userName+"[登录成功]"); 
					logService.add(log);                     //登录日志信息
				}
			}
		}else{
			errInfo = "error";	//缺少参数
		}
		map.put("result", errInfo);
		return OpenValidateApi.returnObject(new ViewData(), map);
	}
	
	/**访问系统首页
	 * @param changeMenu：切换菜单参数
	 * @return
	 */
	@RequestMapping(value="/main/{changeMenu}")
	@SuppressWarnings("unchecked")
	public ModelAndView login_index(@PathVariable("changeMenu") String changeMenu){
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		try{
			Session session = GlobalPower.getSession();
			User user = (User)session.getAttribute(Const.SESSION_USER);				//读取session中的用户信息(单独用户信息)
			if (user != null) {
				User userInfo = (User)session.getAttribute(Const.SESSION_USERROL);		//读取session中的用户信息(含角色信息)
				if(null == userInfo || userInfo.getUserId()==null){
					user = userService.getUserAndRoleById(user.getUserId());		//通过用户ID读取用户信息和角色信息
					session.setAttribute(Const.SESSION_USERROL, user);				//存入session	
				}else{
					user = userInfo;
				}
				String userName = user.getLoginNm();  //user.getUserNm();
				Role role = user.getRole();											//获取用户角色
				String roleRights = role!=null ? role.getPowerRights() : "";				//角色权限(菜单权限)
				session.setAttribute(userName + Const.SESSION_ROLE_RIGHTS, roleRights); //将角色权限存入session
				session.setAttribute(Const.SESSION_USERNAME, userName);				//放入用户名到session
				List<Menu> allmenuList = new ArrayList<Menu>();
				if(null == session.getAttribute(userName + Const.SESSION_allmenuList)){	
					allmenuList = menuService.listAllMenuQx("0");					//获取所有菜单
					if(Tools.notEmpty(roleRights)){
						allmenuList = this.readMenu(allmenuList, roleRights);		//根据角色权限获取本权限的菜单列表
					}
					session.setAttribute(userName + Const.SESSION_allmenuList, allmenuList);//菜单权限放入session中
				}else{
					allmenuList = (List<Menu>)session.getAttribute(userName + Const.SESSION_allmenuList);
				}
				//切换菜单处理=====start
				List<Menu> menuList = new ArrayList<Menu>();
				if(null == session.getAttribute(userName + Const.SESSION_menuList) || ("yes".equals(changeMenu))){
					List<Menu> menuListOne = new ArrayList<Menu>();
					List<Menu> menuListTwo = new ArrayList<Menu>();
					//拆分菜单
					for(int i=0;i<allmenuList.size();i++){
						Menu menu = allmenuList.get(i);
						if("1".equals(menu.getMenuType())){
							menuListOne.add(menu);
						}else{
							menuListTwo.add(menu);
						}
					}
					session.removeAttribute(userName + Const.SESSION_menuList);
					if("2".equals(session.getAttribute("changeMenu"))){
						session.setAttribute(userName + Const.SESSION_menuList, menuListOne);
						session.removeAttribute("changeMenu");
						session.setAttribute("changeMenu", "1");
						menuList = menuListOne;
					}else{
						session.setAttribute(userName + Const.SESSION_menuList, menuListTwo);
						session.removeAttribute("changeMenu");
						session.setAttribute("changeMenu", "2");
						menuList = menuListTwo;
					}
				}else{
					menuList = (List<Menu>)session.getAttribute(userName + Const.SESSION_menuList);
				}
				//切换菜单处理=====end
				if(null == session.getAttribute(userName + Const.SESSION_POWERBUTTOM)){
					session.setAttribute(userName + Const.SESSION_POWERBUTTOM, this.getUserPowerButton(userName));	//按钮权限放到session中
				}
				this.getRemortIP(userName);	//更新登录IP
				mv.setViewName("system/index/main");
				mv.addObject("user", user);
				mv.addObject("menuList", menuList);
			}else {
				mv.setViewName("system/index/login");//session失效后跳转登录页面
			}
		} catch(Exception e){
			mv.setViewName("system/index/login");
			logger.error(e.getMessage(), e);
		}
		pd.put("sysName", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
		mv.addObject("pd",pd);
		return mv;
	}
	
	/**根据角色权限获取本权限的菜单列表(递归处理)
	 * @param menuList：传入的总菜单
	 * @param roleRights：加密的权限字符串
	 * @return
	 */
	public List<Menu> readMenu(List<Menu> menuList,String roleRights){
		for(int i=0;i<menuList.size();i++){
			menuList.get(i).setHasMenu(SetPower.testRights(roleRights, menuList.get(i).getMenuId()));
			if(menuList.get(i).isHasMenu()){		//判断是否有此菜单权限
				this.readMenu(menuList.get(i).getSubMenu(), roleRights);//是：继续排查其子菜单
			}
		}
		return menuList;
	}
	
	/**
	 * 进入tab标签
	 * @return
	 */
	@RequestMapping(value="/tab")
	public String tab(){
		return "system/index/tab";
	}
	
	/**
	 * 进入首页后的默认页面
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/login_default")
	public ModelAndView defaultPage() throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd.put("userCount", Integer.parseInt(userService.getUserCount("").get("userCount").toString())-1);				//系统用户数
		pd.put("appUserCount",0);	//会员数
		mv.addObject("pd",pd);
		mv.setViewName("system/index/default");
		return mv;
	}
	
	/**
	 * 用户注销
	 * @param session
	 * @return
	 */
	@RequestMapping(value="/logout")
	public ModelAndView logout(){
		String userName = GlobalPower.getUsername();	//当前登录的用户名
		logBefore(logger, userName+"退出系统");
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		Session session = GlobalPower.getSession();	//以下清除session缓存
		session.removeAttribute(Const.SESSION_USER);
		session.removeAttribute(userName + Const.SESSION_ROLE_RIGHTS);
		session.removeAttribute(userName + Const.SESSION_allmenuList);
		session.removeAttribute(userName + Const.SESSION_menuList);
		session.removeAttribute(userName + Const.SESSION_POWERBUTTOM);
		session.removeAttribute(Const.SESSION_userpds);
		session.removeAttribute(Const.SESSION_USERNAME);
		session.removeAttribute(Const.SESSION_USERROL);
		session.removeAttribute("changeMenu");
		//shiro销毁登录
		Subject subject = SecurityUtils.getSubject(); 
		subject.logout();
		pd = this.getViewData();
		pd.put("msg", pd.getString("msg"));
		pd.put("sysName", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
		mv.setViewName("system/index/login");
		mv.addObject("pd",pd);
		return mv;
	}
	
	/**获取用户权限
	 * @param session
	 * @return
	 */
	public Map<String, String> getUserPowerButton(String userName){
		ViewData pd = new ViewData();
		Map<String, String> map = new HashMap<String, String>();
		try {
			pd.put(Const.SESSION_USERNAME, userName);
			pd.put("roleId", userService.findByUsername(pd).get("roleId").toString());//获取角色ID
			pd = roleService.findObjectById(pd);										//获取角色信息														
			map.put("addPower", pd.getString("addPower"));	//增
			map.put("delPower", pd.getString("delPower"));	//删
			map.put("editPower", pd.getString("editPower"));	//改
			map.put("seachPower", pd.getString("seachPower"));	//查
			List<ViewData> buttonPowerButtomnamelist = new ArrayList<ViewData>();
			if("admin".equals(userName)){
				buttonPowerButtomnamelist = fhbuttonService.listAll(pd);					//admin用户拥有所有按钮权限
			}else{
				buttonPowerButtomnamelist = buttonrightsService.listAllBrAndQxname(pd);	//此角色拥有的按钮权限标识列表
			}
			for(int i=0;i<buttonPowerButtomnamelist.size();i++){
				map.put(buttonPowerButtomnamelist.get(i).getString("powerCode"),"1");		//按钮权限
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}	
		return map;
	}
	
	/** 更新登录用户的IP
	 * @param userName
	 * @throws Exception
	 */
	public void getRemortIP(String userName) throws Exception {  
		ViewData pd = new ViewData();
		HttpServletRequest request = this.getRequest();
		String ip = "";
		if (request.getHeader("x-forwarded-for") == null) {  
			ip = request.getRemoteAddr();  
	    }else{
	    	ip = request.getHeader("x-forwarded-for");  
	    }
		pd.put("loginNm", userName);
		pd.put("addreeIp", ip);
		userService.saveIP(pd);
	}  
}
