package com.eshop.service;

import com.eshop.common.ServerResponse;
import com.eshop.pojo.User;

/**
 * Created by Liujingtao on 2018/6/28.
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkValid(String str, String type);
    ServerResponse<String> getQuestionByUsername(String username);
    ServerResponse<String> verifyAnswer(String username,String question, String answer);
    ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);
    ServerResponse<String> resetPassword(String oldPassword,String newPassword,User user);
    ServerResponse<User> updateUserInfo(User user);
    ServerResponse<User> getUserInfoById(int userId);
}
