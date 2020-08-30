package com.itheima.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.*;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import com.itheima.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service(interfaceClass = UserService.class)
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private MenuDao menuDao;

    public User findByUsername(String username) {

        // 获取User
        User user = userDao.findByUsername(username);
        if (user == null) {
            return null;
        }

        // 获取Role
        Integer userId = user.getId();
        Set<Role> roles = roleDao.findByUserId(userId);

        // 获取Permission
        if (roles != null && roles.size() > 0) {
            for (Role role : roles) {
                Integer roleId = role.getId();
                Set<Permission> permissions = permissionDao.findByRoleId(roleId);
                if (permissions != null && permissions.size() > 0) {
                    role.setPermissions(permissions);
                }
            }
            user.setRoles(roles);
        }

        return user;
    }

    //获取当前登录用户的用户名获取对应的模块信息
    public List<Map> findAllModuleByUsername(String username) {
        List<Map> menuList = menuDao.findAllModuleByUsername(username);
        // 最后的结果
        List<Map> newMenuList = new ArrayList<>();
        // 先找到所有的一级菜单
        for (int i = 0; i < menuList.size(); i++) {
            Map<String, Object> map = menuList.get(i);
            Integer parentMenuId = (Integer) map.get("parentMenuId");
            // 如果是顶级菜单，没有父菜单
            if (parentMenuId == null) {
                newMenuList.add(map);
            }
        }
        // 为一级菜单设置子菜单，getChild是递归调用的
        for (Map menu : newMenuList) {
            menu.put("children", getChild((Integer) menu.get("id"), menuList));
        }
        return newMenuList;
    }

    //    递归查找子菜单
    private List<Map> getChild(int id, List<Map> menuList) {
        // 子菜单
        List<Map> childList = new ArrayList<>();
        for (Map menu : menuList) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            Integer parentMenuId = (Integer) menu.get("parentMenuId");
            if (parentMenuId != null && id == parentMenuId) {
                childList.add(menu);
            }
        }
        // 把子菜单的子菜单再循环一遍
        for (Map menu : childList) {
            //如果子菜单的url为空代表还有子菜单。
            if (menu.get("linkUrl") == null) {
                // 递归
                menu.put("children", getChild((Integer) menu.get("id"), menuList));
            }
        } // 递归退出条件
        if (childList.size() == 0) {
            return null;
        }
        return childList;
    }
}










