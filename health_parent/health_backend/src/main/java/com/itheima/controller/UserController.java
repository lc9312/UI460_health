package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.Result;
import com.itheima.pojo.Menu;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;
    //获取当前登录用户的用户名以及对应的模块信息
    @RequestMapping("/getUsernameAndMenu")
    public Result getUsernameAndMenu()throws Exception{
        try{
            User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map map = new HashMap();
            //根据用户名称获取角色对应的所有模块
            List<Map> menuList = userService.findAllModuleByUsername(user.getUsername());
            map.put("menuList", menuList);
            map.put("username", user.getUsername());
            return new Result(true, MessageConstant.GET_MENU_LIST_SUCCESS,map);
        }catch (Exception e){
            return new Result(false, MessageConstant.GET_MENU_LIST_FAIL);
        }
    }
}
