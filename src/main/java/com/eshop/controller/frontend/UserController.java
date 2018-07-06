package com.eshop.controller.frontend;

import com.eshop.common.Constant;
import com.eshop.common.ResponseCode;
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
 * Created by Liujingtao on 2018/6/28.
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value="login.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
            session.setAttribute(Constant.CURRENT_USER,response.getData());
        }
        return response;
    }

    @RequestMapping(value="logout.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Constant.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value="register.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    @RequestMapping(value="check_valid.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){
        return iUserService.checkValid(str,type);
    }

    @RequestMapping(value="get_current_user.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getCurrentUser(HttpSession session){
        User user = (User) session.getAttribute(Constant.CURRENT_USER);
        if(user != null){
            return ServerResponse.createBySuccessData(user);
        }
        return ServerResponse.createByErrorMsg("User has not logged in.");
    }
    //return user's own question when he forgets his password
    @RequestMapping(value="forget_get_question.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.getQuestionByUsername(username);
    }

    //if the answer is correct, return a time-limited token
    @RequestMapping(value="forget_verify_answer.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> verifyForgetAnswer(String username, String question, String answer){
        return iUserService.verifyAnswer(username,question,answer);
    }

    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String token){
        return iUserService.forgetResetPassword(username,newPassword,token);
    }

    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMsg("User has not logged in.");
        }
        return iUserService.resetPassword(oldPassword,newPassword,currentUser);
    }

    @RequestMapping(value = "update_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInfo(User pageUser, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMsg("User has not logged in.");
        }
        pageUser.setId(currentUser.getId());
        pageUser.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateUserInfo(pageUser);
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Constant.CURRENT_USER,response.getData());
        }
        return response;
    }

    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInfo(HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeAndMsg(ResponseCode.NEED_LOGIN.getCode(),"User has not logged in.");
        }
        return iUserService.getUserInfoById(currentUser.getId());
    }

}
