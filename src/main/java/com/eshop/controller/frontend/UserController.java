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
 * Description: User Controller for the front-end users
 * Created by Jingtao Liu on 2018/6/28.
 */
@Controller
@RequestMapping("/user/")
class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * User Login
     */
    @RequestMapping(value="login.do",method= RequestMethod.POST)
    @ResponseBody //convert response to json, using a SpringMVC plugin configured in dispatcher-servlet.xml
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
            session.setAttribute(Constant.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * User Logout
     */
    @RequestMapping(value="logout.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Constant.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * User Register
     */
    @RequestMapping(value="register.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     * Check the username or email whether is valid or not
     * when type is Constant.USERNAME, it checks the username
     * when type is Constant.EMAIL, it checks the email
     */
    @RequestMapping(value="check_valid.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){
        return iUserService.checkValid(str,type);
    }

    /**
     * Get the current user who has logged in the system
     */
    @RequestMapping(value="get_current_user.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getCurrentUser(HttpSession session){
        User user = (User) session.getAttribute(Constant.CURRENT_USER);
        if(user != null){
            return ServerResponse.createBySuccessData(user);
        }
        return ServerResponse.createByErrorMsg("User has not logged in.");
    }

    /**
     * Get users own password-protected questions when they forgets their passwords
     */
    @RequestMapping(value="forget_get_question.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.getQuestionByUsername(username);
    }

    /**
     * Validate the password-protected answer
     * When a user's password-protected answer is correct, return a time-limited token
     * which is for the validation of next reset-password method
     */
    @RequestMapping(value="forget_verify_answer.do",method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> verifyForgetAnswer(String username, String question, String answer){
        return iUserService.verifyAnswer(username,question,answer);
    }

    /**
     * Reset user's password with new password and one token when he forgets his password
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String token){
        return iUserService.forgetResetPassword(username,newPassword,token);
    }

    /**
     * Reset user's password with new password when he has logged in the system
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMsg("User has not logged in.");
        }
        return iUserService.resetPassword(oldPassword,newPassword,currentUser);
    }

    /**
     * Update user's information when he has logged in the system
     * PageUserInfo is the incoming user info which come from the frontend web page
     * and there are no id and username in it.
     */
    @RequestMapping(value = "update_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInfo(User pageUserInfo, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMsg("User has not logged in.");
        }
        pageUserInfo.setId(currentUser.getId());
        pageUserInfo.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateUserInfo(pageUserInfo);
        if(response.isSuccess()){
            //response.getData() is the updateUser which has only 5 updated fields and no username
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Constant.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * Get user's information when user has logged in the system
     * If user has not logged, the page is forced to redirect to the login page.
     * The frontend JS programmer needs to handle the page redirection when he gets the NEED_LOGIN code.
     */
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
