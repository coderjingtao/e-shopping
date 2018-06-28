package com.eshop.service;

import com.eshop.common.ServerResponse;
import com.eshop.pojo.User;

/**
 * Created by Liujingtao on 2018/6/28.
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);
}
