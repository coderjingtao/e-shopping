package com.eshop.controller.backend;

import com.eshop.common.Constant;
import com.eshop.common.ServerResponse;
import com.eshop.pojo.User;
import com.eshop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Description: User Management Controller for the back-end managers
 * Created by Jingtao Liu on 2018/6/29.
 */
@Controller
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value="login.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
            User user = response.getData();
            if(user.getRole() == Constant.Role.ROLE_ADMINISTRATOR){
                session.setAttribute(Constant.CURRENT_USER,user);
                return response;
            }else{
                return ServerResponse.createByErrorMsg("User is not administrator. Cannot login.");
            }
        }
        return response;
    }
}
