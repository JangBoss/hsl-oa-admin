/**
 * 
 */
package com.hsl.oa.common.global;

import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;

import com.hsl.oa.consts.Const;
import com.hsl.oa.domain.entity.admin.system.Menu;
import com.hsl.oa.power.SetPower;

/**
 * @title:GlobalPower 
 * @description:权限处理
 * @author:View
 * @date:2017-4-19 下午8:23:44
 */
public class GlobalPower {
    
	/**
	 * 访问权限及初始化按钮权限(控制按钮的显示 增删改查)
	 * @param menuUrl  菜单路径
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean hasJurisdiction(String menuUrl){
		//判断是否拥有当前点击菜单的权限（内部过滤,防止通过url进入跳过菜单权限）
		/**
		 * 根据点击的菜单的xxx.do去菜单中的URL去匹配，当匹配到了此菜单，判断是否有此菜单的权限，没有的话跳转到404页面
		 * 根据按钮权限，授权按钮(当前点的菜单和角色中各按钮的权限匹对)
		 */
		String loginName = getUsername();	//获取当前登录者loginname
		Session session = getSession();
		List<Menu> menuList = (List<Menu>)session.getAttribute(loginName + Const.SESSION_allmenuList); //获取菜单列表
		return readMenu(menuList,menuUrl,session,loginName);
	}
	
	/**校验菜单权限并初始按钮权限用于页面按钮显示与否(递归处理)
	 * @param menuList:传入的总菜单(设置菜单时，.do前面的不要重复)
	 * @param menuUrl:访问地址
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean readMenu(List<Menu> menuList,String menuUrl,Session session,String loginName){
		for(int i=0;i<menuList.size();i++){
			if(menuList.get(i).getMenuUrl().split(".do")[0].equals(menuUrl.split(".do")[0])){ //访问地址与菜单地址循环匹配，如何匹配到就进一步验证，如果没匹配到就不处理(可能是接口链接或其它链接)
				if(!menuList.get(i).isHasMenu()){				//判断有无此菜单权限
					return false;
				}else{											//按钮判断
					Map<String, String> map = (Map<String, String>)session.getAttribute(loginName + Const.SESSION_POWERBUTTOM);//按钮权限(增删改查)
					map.remove("add");
					map.remove("del");
					map.remove("edit");
					map.remove("seach");
					String menuId =  menuList.get(i).getMenuId();
					Boolean isAdmin = "admin".equals(loginName);
					map.put("add", (SetPower.testRights(map.get("addPower"), menuId)) || isAdmin?"1":"0");
					map.put("del",  SetPower.testRights(map.get("delPower"), menuId) || isAdmin?"1":"0");
					map.put("edit", SetPower.testRights(map.get("editPower"), menuId) || isAdmin?"1":"0");
					map.put("seach",  SetPower.testRights(map.get("seachPower"), menuId) || isAdmin?"1":"0");
					session.removeAttribute(loginName + Const.SESSION_POWERBUTTOM);
					session.setAttribute(loginName + Const.SESSION_POWERBUTTOM, map);	//重新分配按钮权限
					return true;
				}
			}else{
				if(!readMenu(menuList.get(i).getSubMenu(),menuUrl,session,loginName)){//继续排查其子菜单
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 按钮权限(方法中校验)
	 * @param menuUrl  菜单路径
	 * @param type  类型(add、del、edit、seach)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean buttonJurisdiction(String menuUrl, String type){
		//判断是否拥有当前点击菜单的权限（内部过滤,防止通过url进入跳过菜单权限）
		/**
		 * 根据点击的菜单的xxx.do去菜单中的URL去匹配，当匹配到了此菜单，判断是否有此菜单的权限，没有的话跳转到404页面
		 * 根据按钮权限，授权按钮(当前点的菜单和角色中各按钮的权限匹对)
		 */
		String loginName = getUsername();	//获取当前登录者loginname
		Session session = getSession();
		List<Menu> menuList = (List<Menu>)session.getAttribute(loginName + Const.SESSION_allmenuList); //获取菜单列表
		return readMenuButton(menuList,menuUrl,session,loginName,type);
	}
	
	/**校验按钮权限(递归处理)
	 * @param menuList:传入的总菜单(设置菜单时，.do前面的不要重复)
	 * @param menuUrl:访问地址
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean readMenuButton(List<Menu> menuList,String menuUrl,Session session,String loginName, String type){
		for(int i=0;i<menuList.size();i++){
			if(menuList.get(i).getMenuUrl().split(".do")[0].equals(menuUrl.split(".do")[0])){ //访问地址与菜单地址循环匹配，如何匹配到就进一步验证，如果没匹配到就不处理(可能是接口链接或其它链接)
				if(!menuList.get(i).isHasMenu()){				//判断有无此菜单权限
					return false;
				}else{											//按钮判断
					Map<String, String> map = (Map<String, String>)session.getAttribute(loginName + Const.SESSION_POWERBUTTOM);//按钮权限(增删改查)
					String menuId =  menuList.get(i).getMenuId();
					Boolean isAdmin = "admin".equals(loginName);
					if("add".equals(type)){
						return ((SetPower.testRights(map.get("addPower"), menuId)) || isAdmin);
					}else if("del".equals(type)){
						return ((SetPower.testRights(map.get("delPower"), menuId)) || isAdmin);
					}else if("edit".equals(type)){
						return ((SetPower.testRights(map.get("editPower"), menuId)) || isAdmin);
					}else if("seach".equals(type)){
						return ((SetPower.testRights(map.get("seachPower"), menuId)) || isAdmin);
					}
				}
			}else{
				if(!readMenuButton(menuList.get(i).getSubMenu(),menuUrl,session,loginName,type)){//继续排查其子菜单
					return false;
				};
			}
		}
		return true;
	}
	
	/**获取当前登录的用户名
	 * @return
	 */
	public static String getUsername(){
		return getSession().getAttribute(Const.SESSION_USERNAME).toString();
	}
	
	/**获取当前按钮权限(增删改查)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getHC(){
		return (Map<String, String>)getSession().getAttribute(getUsername() + Const.SESSION_POWERBUTTOM);
	}
	
	/**shiro管理的session
	 * @return
	 */
	public static Session getSession(){
		//Subject currentUser = SecurityUtils.getSubject();  
		return SecurityUtils.getSubject().getSession();
	}
}
