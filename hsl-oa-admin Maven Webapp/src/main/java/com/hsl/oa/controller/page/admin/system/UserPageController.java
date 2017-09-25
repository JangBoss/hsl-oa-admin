/**
 * 
 */
package com.hsl.oa.controller.page.admin.system;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.hsl.oa.common.BaseController;
import com.hsl.oa.domain.admin.system.user.UserService;

/**
 * @title:UserPageController 
 * @description:用户信息(查询)
 * @author:View
 * @date:2017-4-19 下午5:18:12
 */
@Controller
public class UserPageController extends BaseController{
   
	/*@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/admin/user/gridlist", method = RequestMethod.GET)
	private String gridList(Model model, HttpServletRequest request, @RequestParam(value="page",required=false,defaultValue="1") int page) {
		
		Page<User> pageModel = new Page<User>();
		pageModel.setPageNo(page);
		pageModel.setPageSize(8);
		List<User> gridList = null;
		try {
			gridList = userService.list(pageModel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("gridList", gridList);
		return "list";
	}*/
}
