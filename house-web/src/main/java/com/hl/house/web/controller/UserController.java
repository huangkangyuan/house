package com.hl.house.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.hl.house.dao.service.AgencyService;
import com.hl.house.dao.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hl.house.common.constants.CommonConstants;
import com.hl.house.common.model.User;
import com.hl.house.common.result.ResultMsg;
import com.hl.house.common.utils.HashUtils;

@Controller
public class UserController {

  @Autowired
  private UserService userService;
  
  @Autowired
  private AgencyService agencyService;

  /**
   * 注册提交:1.注册验证 2 发送邮件 3验证失败重定向到注册页面
   * 注册页获取:根据account对象为依据判断是否注册页获取请求
   * 
   * @param account
   * @param modelMap
   * @return
   */
  @RequestMapping("accounts/register")
  public String accountsRegister(User account, ModelMap modelMap) {
    if (account == null || account.getName() == null) {
      modelMap.put("agencyList",  agencyService.getAllAgency());
      return "/user/accounts/register";
    }
    // 用户验证
    ResultMsg resultMsg = UserHelper.validate(account);
    if (resultMsg.isSuccess() && userService.addAccount(account)) {
      modelMap.put("email", account.getEmail());
      return "/user/accounts/registerSubmit";
    } else {
      return "redirect:/accounts/register?" + resultMsg.asUrlParams();
    }
  }

  @RequestMapping("accounts/verify")
  public String verify(String key) {
    boolean result = userService.enable(key);
    if (result) {
      return "redirect:/index?" + ResultMsg.successMsg("激活成功").asUrlParams();
    } else {
      return "redirect:/accounts/register?" + ResultMsg.errorMsg("激活失败,请确认链接是否过期");
    }
  }

  // ----------------------------登录流程------------------------------------

  /**
   * 登录接口
   */
  @RequestMapping("/accounts/signin")
  public String signin(HttpServletRequest req) {
    String username = req.getParameter("username");
    String password = req.getParameter("password");
    String target = req.getParameter("target");
    if (username == null || password == null) {
      req.setAttribute("target", target);
      return "/user/accounts/signin";
    }
    User user = userService.auth(username, password);
    if (user == null) {
      return "redirect:/accounts/signin?" + "target=" + target + "&username=" + username + "&"
          + ResultMsg.errorMsg("用户名或密码错误").asUrlParams();
    } else {
      HttpSession session = req.getSession(true);
      session.setAttribute(CommonConstants.USER_ATTRIBUTE, user);
      // session.setAttribute(CommonConstants.PLAIN_USER_ATTRIBUTE, user);
      return StringUtils.isNoneBlank(target) ? "redirect:" + target : "redirect:/index";
    }
  }

  /**
   * 退出操作
   * 
   * @param request
   * @return
   */
  @RequestMapping("accounts/logout")
  public String logout(HttpServletRequest request) {
    //参数为true时，若存在会话，则返回该会话，否则新建一个会话；
    HttpSession session = request.getSession(true);
    session.invalidate();
    return "redirect:/index";
  }

  // ---------------------个人信息页-------------------------
  /**
   * 1.能够提供页面信息
   * 2.更新用户信息
   * 
   * @param updateUser
   * @param model
   * @return
   */
  @RequestMapping("accounts/profile")
  public String profile(HttpServletRequest req, User updateUser, ModelMap model) {
    if (updateUser.getEmail() == null) {
      //因为已经登录了,并且将信息保存到了session中,所以在页面是可以获取到的
      return "/user/accounts/profile";
    }
    userService.updateUser(updateUser, updateUser.getEmail());
    User query = new User();
    query.setEmail(updateUser.getEmail());
    List<User> users = userService.getUserByQuery(query);
    req.getSession(true).setAttribute(CommonConstants.USER_ATTRIBUTE, users.get(0));
    return "redirect:/accounts/profile?" + ResultMsg.successMsg("更新成功").asUrlParams();
  }

  /**
   * 修改密码操作
   * 
   * @param email
   * @param password
   * @param newPassword
   * @param confirmPassword
   * @param mode
   * @return
   */
  @RequestMapping("accounts/changePassword")
  public String changePassword(String email, String password, String newPassword,
    String confirmPassword, ModelMap mode) {
    User user = userService.auth(email, password);
    if (user == null || !confirmPassword.equals(newPassword)) {
      return "redirct:/accounts/profile?" + ResultMsg.errorMsg("密码错误").asUrlParams();
    }
    User updateUser = new User();
    updateUser.setPasswd(HashUtils.encryptPassword(newPassword));
    userService.updateUser(updateUser, email);
    return "redirect:/accounts/profile?" + ResultMsg.successMsg("更新成功").asUrlParams();
  }


  /**
   * 忘记密码
   * @param username
   * @param modelMap
   * @return
   */
  @RequestMapping("accounts/remember")
  public String remember(String username, ModelMap modelMap) {
    if (StringUtils.isBlank(username)) {
      return "redirect:/accounts/signin?" + ResultMsg.errorMsg("邮箱不能为空").asUrlParams();
    }
    userService.resetNotify(username);
    modelMap.put("email", username);
    return "/user/accounts/remember";
  }
  
  @RequestMapping("accounts/reset")
  public String reset(String key,ModelMap modelMap){
    String email = userService.getResetEmail(key);
    if (StringUtils.isBlank(email)) {
      return "redirect:/accounts/signin?" + ResultMsg.errorMsg("重置链接已过期").asUrlParams();
    }
    modelMap.put("email", email);
    modelMap.put("success_key", key);
    return "/user/accounts/reset";
  }
  
  @RequestMapping(value="accounts/resetSubmit")
  public String resetSubmit(HttpServletRequest req,User user){
    ResultMsg retMsg = UserHelper.validateResetPassword(user.getKey(), user.getPasswd(), user.getConfirmPasswd());
    if (!retMsg.isSuccess() ) {
      String suffix = "";
      if (StringUtils.isNotBlank(user.getKey())) {
         suffix = "email=" + userService.getResetEmail(user.getKey()) + "&key=" +  user.getKey() + "&";
      }
      return "redirect:/accounts/reset?"+ suffix  + retMsg.asUrlParams();
    }
    User updatedUser =  userService.reset(user.getKey(),user.getPasswd());
    req.getSession(true).setAttribute(CommonConstants.USER_ATTRIBUTE, updatedUser);
    return "redirect:/index?" + retMsg.asUrlParams();
  }

}
