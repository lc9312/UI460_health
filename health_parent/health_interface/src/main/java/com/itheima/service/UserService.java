package com.itheima.service;

import com.itheima.pojo.User;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {
    public User findByUsername(String username);

    List<Map> findAllModuleByUsername(String username);
}
