package com.itheima.service;

import com.itheima.pojo.Menu;
import com.itheima.pojo.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    public User findByUsername(String username);

    List<Menu> findAllModuleByUsername(String username);
}
