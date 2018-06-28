package com.eshop.service.impl;

import com.eshop.common.ServerResponse;
import com.eshop.dao.UserMapper;
import com.eshop.pojo.User;
import com.eshop.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        // TODO: 2018/6/28 md5

        User user = userMapper.selectLogin(username,password);
        if(user == null){
            return ServerResponse.createByErrorMsg("Password is incorrect.");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Login successfully",user);
    }
}
