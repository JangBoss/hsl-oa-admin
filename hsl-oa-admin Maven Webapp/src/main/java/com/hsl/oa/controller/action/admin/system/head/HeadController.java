/**
 * 
 */
package com.hsl.oa.controller.action.admin.system.head;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hsl.oa.api.OpenValidateApi;
import com.hsl.oa.common.BaseController;
import com.hsl.oa.common.global.GlobalPower;
import com.hsl.oa.consts.Const;
import com.hsl.oa.domain.admin.system.msg.MsgService;
import com.hsl.oa.domain.admin.system.user.UserService;
import com.hsl.oa.email.MailSender;
import com.hsl.oa.images.WaterMark;
import com.hsl.oa.plugins.ViewData;
import com.hsl.oa.util.Tools;

/**
 * @title:HeadController 
 * @description:头部控制器
 * @author:View
 * @date:2017-5-7 下午11:28:16
 */
@Controller
@RequestMapping(value="/head")
public class HeadController extends BaseController{
	
	@Resource(name="userService")
	private UserService userService;	
	@Resource(name="msgService")
	private MsgService msgService;
	
	/**获取头部信息
	 * @return
	 */
	@RequestMapping(value="/getList")
	@ResponseBody
	public Object getList() {
		ViewData pd = new ViewData();
		Map<String,Object> map = new HashMap<String,Object>();
		try {
			pd = this.getViewData();
			List<ViewData> pdList = new ArrayList<ViewData>();
			Session session = GlobalPower.getSession();
			ViewData pds = new ViewData();
			pds = (ViewData)session.getAttribute(Const.SESSION_userpds);
			if(null == pds){
				pd.put("USERNAME", GlobalPower.getUsername());//当前登录者用户名
				pds = userService.findByUsername(pd);
				session.setAttribute(Const.SESSION_userpds, pds);
			}
			pdList.add(pds);
			map.put("list", pdList);
			map.put("fhsmsCount",0);//msgService.findFhsmsCount(GlobalPower.getUsername()).get("fhsmsCount").toString());//站内信未读总数
			String strWEBSOCKET = Tools.readTxtFile(Const.WEBSOCKET);//读取WEBSOCKET配置
			if(null != strWEBSOCKET && !"".equals(strWEBSOCKET)){
				String strIW[] = strWEBSOCKET.split(",fh,");
				if(strIW.length == 5){
					map.put("wimadress", strIW[0]+":"+strIW[1]);	//即时聊天服务器IP和端口
					map.put("oladress", strIW[2]+":"+strIW[3]);		//在线管理和站内信服务器IP和端口
					map.put("FHsmsSound", strIW[4]);				//站内信提示音效配置
				}
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			logAfter(logger);
		}
		return OpenValidateApi.returnObject(pd, map);
	}
	
	/**获取站内信未读总数
	 * @return
	 */
	@RequestMapping(value="/getFhsmsCount")
	@ResponseBody
	public Object getFhsmsCount() {
		ViewData pd = new ViewData();
		Map<String,Object> map = new HashMap<String,Object>();
		try {
			map.put("fhsmsCount", msgService.findFhsmsCount(GlobalPower.getUsername()).get("fhsmsCount").toString());//站内信未读总数
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			logAfter(logger);
		}
		return OpenValidateApi.returnObject(pd, map);
	}
	
	/**去发送邮箱页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/editEmail")
	public ModelAndView editEmail() throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		mv.setViewName("system/head/edit_email");
		mv.addObject("pd", pd);
		return mv;
	}

	/**去发送电子邮件页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goSendEmail")
	public ModelAndView goSendEmail() throws Exception{
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		mv.setViewName("system/head/send_email");
		mv.addObject("pd", pd);
		return mv;
	}
	
	/**发送电子邮件
	 * @return
	 */
	@RequestMapping(value="/sendEmail")
	@ResponseBody
	public Object sendEmail(){
		ViewData pd = new ViewData();
		pd = this.getViewData();
		Map<String,Object> map = new HashMap<String,Object>();
		String msg = "ok";		//发送状态
		int count = 0;			//统计发送成功条数
		int zcount = 0;			//理论条数
		String strEMAIL = Tools.readTxtFile(Const.EMAIL);		//读取邮件配置
		List<ViewData> pdList = new ArrayList<ViewData>();
		String toEMAIL = pd.getString("EMAIL");					//对方邮箱
		String TITLE = pd.getString("TITLE");					//标题
		String CONTENT = pd.getString("CONTENT");				//内容
		String TYPE = pd.getString("TYPE");						//类型
		String isAll = pd.getString("isAll");					//是否发送给全体成员 yes or no
		String fmsg = pd.getString("fmsg");						//判断是系统用户还是会员 "appuser"为会员用户
		if(null != strEMAIL && !"".equals(strEMAIL)){
			String strEM[] = strEMAIL.split(",fh,");
			if(strEM.length == 4){
				if("yes".endsWith(isAll)){
					try {
						List<ViewData> userList = new ArrayList<ViewData>();
						//userList = "appuser".equals(fmsg) ? appuserService.listAllUser(pd):userService.listAllUser(pd);
						zcount = userList.size();
						try {
							for(int i=0;i<userList.size();i++){
								if(Tools.checkEmail(userList.get(i).getString("EMAIL"))){		//邮箱格式不对就跳过
									//MailSender.sendEmail(strEM[0], strEM[1], strEM[2], strEM[3], userList.get(i).getString("EMAIL"), TITLE, CONTENT, TYPE);//调用发送邮件函数
									count++;
								}else{
									continue;
								}
							}
							msg = "ok";
						} catch (Exception e) {
							msg = "error";
						}
					} catch (Exception e) {
						msg = "error";
					}
				}else{
					toEMAIL = toEMAIL.replaceAll("；", ";");
					toEMAIL = toEMAIL.replaceAll(" ", "");
					String[] arrTITLE = toEMAIL.split(";");
					zcount = arrTITLE.length;
					try {
						for(int i=0;i<arrTITLE.length;i++){
							if(Tools.checkEmail(arrTITLE[i])){		//邮箱格式不对就跳过
								//MailSender.sendEmail(strEM[0], strEM[1], strEM[2], strEM[3], arrTITLE[i], TITLE, CONTENT, TYPE);//调用发送邮件函数
								count++;
							}else{
								continue;
							}
						}
						msg = "ok";
					} catch (Exception e) {
						msg = "error";
					} 
				}	
			}else{
				msg = "error";
			}
		}else{
			msg = "error";
		}
		pd.put("msg", msg);
		pd.put("count", count);						//成功数
		pd.put("ecount", zcount-count);				//失败数
		pdList.add(pd);
		map.put("list", pdList);
		return OpenValidateApi.returnObject(pd, map);
	}
	
	/**去系统设置页面
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/goSystem")
	public ModelAndView goEditEmail() throws Exception{
		if(!"admin".equals(GlobalPower.getUsername())){return null;}	//非admin用户不能修改
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		pd.put("YSYNAME", Tools.readTxtFile(Const.SYSNAME));	 //读取系统名称
		pd.put("COUNTPAGE", Tools.readTxtFile(Const.PAGE));		 //读取每页条数
		String strEMAIL = Tools.readTxtFile(Const.EMAIL);		 //读取邮件配置
		String strSMS1 = Tools.readTxtFile(Const.SMS1);			 //读取短信1配置
		String strSMS2 = Tools.readTxtFile(Const.SMS2);			 //读取短信2配置
		String strFWATERM = Tools.readTxtFile(Const.FWATERM);	 //读取文字水印配置
		String strIWATERM = Tools.readTxtFile(Const.IWATERM);	 //读取图片水印配置
		pd.put("Token", Tools.readTxtFile(Const.WEIXIN));		 //读取微信配置
		String strWEBSOCKET = Tools.readTxtFile(Const.WEBSOCKET);//读取WEBSOCKET配置
		if(null != strEMAIL && !"".equals(strEMAIL)){
			String strEM[] = strEMAIL.split(",fh,");
			if(strEM.length == 4){
				pd.put("SMTP", strEM[0]);
				pd.put("PORT", strEM[1]);
				pd.put("EMAIL", strEM[2]);
				pd.put("PAW", strEM[3]);
			}
		}
		if(null != strSMS1 && !"".equals(strSMS1)){
			String strS1[] = strSMS1.split(",fh,");
			if(strS1.length == 2){
				pd.put("SMSU1", strS1[0]);
				pd.put("SMSPAW1", strS1[1]);
			}
		}
		if(null != strSMS2 && !"".equals(strSMS2)){
			String strS2[] = strSMS2.split(",fh,");
			if(strS2.length == 2){
				pd.put("SMSU2", strS2[0]);
				pd.put("SMSPAW2", strS2[1]);
			}
		}
		if(null != strFWATERM && !"".equals(strFWATERM)){
			String strFW[] = strFWATERM.split(",fh,");
			if(strFW.length == 5){
				pd.put("isCheck1", strFW[0]);
				pd.put("fcontent", strFW[1]);
				pd.put("fontSize", strFW[2]);
				pd.put("fontX", strFW[3]);
				pd.put("fontY", strFW[4]);
			}
		}
		if(null != strIWATERM && !"".equals(strIWATERM)){
			String strIW[] = strIWATERM.split(",fh,");
			if(strIW.length == 4){
				pd.put("isCheck2", strIW[0]);
				pd.put("imgUrl", strIW[1]);
				pd.put("imgX", strIW[2]);
				pd.put("imgY", strIW[3]);
			}
		}
		if(null != strWEBSOCKET && !"".equals(strWEBSOCKET)){
			String strIW[] = strWEBSOCKET.split(",fh,");
			if(strIW.length == 5){
				pd.put("WIMIP", strIW[0]);
				pd.put("WIMPORT", strIW[1]);
				pd.put("OLIP", strIW[2]);
				pd.put("OLPORT", strIW[3]);
				pd.put("FHsmsSound", strIW[4]);
			}
		}
		mv.setViewName("system/head/sys_edit");
		mv.addObject("pd", pd);
		return mv;
	}
	
	/**保存系统设置1
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/saveSys")
	public ModelAndView saveSys() throws Exception{
		if(!"admin".equals(GlobalPower.getUsername())){return null;}	//非admin用户不能修改
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		Tools.writeFile(Const.SYSNAME,pd.getString("YSYNAME"));	//写入系统名称
		Tools.writeFile(Const.PAGE,pd.getString("COUNTPAGE"));	//写入每页条数
		Tools.writeFile(Const.EMAIL,pd.getString("SMTP")+",fh,"+pd.getString("PORT")+",fh,"+pd.getString("EMAIL")+",fh,"+pd.getString("PAW"));	//写入邮件服务器配置
		Tools.writeFile(Const.SMS1,pd.getString("SMSU1")+",fh,"+pd.getString("SMSPAW1"));	//写入短信1配置
		Tools.writeFile(Const.SMS2,pd.getString("SMSU2")+",fh,"+pd.getString("SMSPAW2"));	//写入短信2配置
		mv.addObject("msg","OK");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**保存系统设置2
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/saveSys2")
	public ModelAndView saveSys2() throws Exception{
		if(!"admin".equals(GlobalPower.getUsername())){return null;}	//非admin用户不能修改
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		Tools.writeFile(Const.FWATERM,pd.getString("isCheck1")+",fh,"+pd.getString("fcontent")+",fh,"+pd.getString("fontSize")+",fh,"+pd.getString("fontX")+",fh,"+pd.getString("fontY"));	//文字水印配置
		Tools.writeFile(Const.IWATERM,pd.getString("isCheck2")+",fh,"+pd.getString("imgUrl")+",fh,"+pd.getString("imgX")+",fh,"+pd.getString("imgY"));	//图片水印配置
		//WaterMark.fushValue();
		mv.addObject("msg","OK");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**保存系统设置3
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/saveSys3")
	public ModelAndView saveSys3() throws Exception{
		if(!"admin".equals(GlobalPower.getUsername())){return null;}	//非admin用户不能修改
		ModelAndView mv = this.getModelAndView();
		ViewData pd = new ViewData();
		pd = this.getViewData();
		Tools.writeFile(Const.WEIXIN,pd.getString("Token"));	//写入微信配置
		Tools.writeFile(Const.WEBSOCKET,pd.getString("WIMIP")+",fh,"+pd.getString("WIMPORT")+",fh,"+pd.getString("OLIP")+",fh,"+pd.getString("OLPORT")+",fh,"+pd.getString("FHsmsSound"));	//websocket配置
		mv.addObject("msg","OK");
		mv.setViewName("save_result");
		return mv;
	}
	
}