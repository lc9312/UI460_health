package com.itheima.service;

import com.itheima.pojo.User;

/**
 * 用户服务接口
 */
public interface UserService {
    public User findByUsername(String username);
}
