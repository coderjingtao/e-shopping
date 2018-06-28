package com.eshop.service.impl;

import com.eshop.common.Constant;
import com.eshop.common.ServerResponse;
import com.eshop.common.TokenCache;
import com.eshop.dao.UserMapper;
import com.eshop.pojo.User;
import com.eshop.service.IUserService;
import com.eshop.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by Liujingtao on 2018/6/28.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int count = userMapper.checkUsername(username);
        if(count == 0){
            return ServerResponse.createByErrorMsg("Username does not exist.");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMsg("Password is incorrect.");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Login successfully",user);
    }

    @Override
    public ServerResponse<String> register(User user) {

//        int userCount = userMapper.checkUsername(user.getUsername());
//        if(userCount > 0){
//            return ServerResponse.createByErrorMsg("Username has existed.");
//        }
//        int emailCount = userMapper.checkEmail(user.getEmail());
//        if(emailCount > 0){
//            return ServerResponse.createByErrorMsg("Email has existed.");
//        }

        ServerResponse<String> validResponse = this.checkValid(user.getUsername(),Constant.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Constant.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }

        user.setRole(Constant.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMsg("Register failed.");
        }
        return ServerResponse.createBySuccessMsg("Register Successfully.");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if(StringUtils.isNotBlank(type)){
            if(Constant.USERNAME.equals(type)){
                int userCount = userMapper.checkUsername(str);
                if(userCount > 0){
                    return ServerResponse.createByErrorMsg("Username has existed.");
                }
            }
            if(Constant.EMAIL.equals(type)){
                int emailCount = userMapper.checkEmail(str);
                if(emailCount > 0){
                    return ServerResponse.createByErrorMsg("Email has existed.");
                }
            }
        }else{
            return ServerResponse.createByErrorMsg("Wrong parameters");
        }
        return ServerResponse.createBySuccessMsg("It is valid");
    }

    @Override
    public ServerResponse<String> getQuestionByUsername(String username) {
        ServerResponse validResponse = this.checkValid(username,Constant.USERNAME);
        if(validResponse.isSuccess()){
            return ServerResponse.createByErrorMsg("Username does not exist.");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccessData(question);
        }
        return ServerResponse.createByErrorMsg("Find password question is blank.");
    }

    @Override
    public ServerResponse<String> verifyAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0){
            String forgetToken = UUID.randomUUID().toString();
            //store this token in memory and set its time limit
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccessData(forgetToken);
        }
        return ServerResponse.createByErrorMsg("Find password answer is wrong.");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMsg("token is blank");
        }
        ServerResponse validResponse = this.checkValid(username,Constant.USERNAME);
        if(validResponse.isSuccess()){
            return ServerResponse.createByErrorMsg("Username does not exist.");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMsg("Token is invalid or expired.");
        }
        if(StringUtils.equals(forgetToken,token)){
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int updateCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(updateCount > 0){
                return ServerResponse.createBySuccessMsg("Reset Password Successfully");
            }
        }else{
            return ServerResponse.createByErrorMsg("Token is different from origin");
        }
        return ServerResponse.createByErrorMsg("Reset Password failed.");
    }

    @Override
    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user) {
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMsg("Old password is incorrect.");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMsg("Reset Password Successfully");
        }
        return ServerResponse.createByErrorMsg("Reset Password failed.");
    }

    @Override
    public ServerResponse<User> updateUserInfo(User user) {
        //username cannot be updated
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMsg("This Email exists.");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount>0){
            return ServerResponse.createBySuccess("Update Personal Info Successfully.",updateUser);
        }
        return ServerResponse.createByErrorMsg("Update Personal Info failed.");
    }

    @Override
    public ServerResponse<User> getUserInfoById(int userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMsg("Cannot find this user");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccessData(user);
    }


}
