package com.itheima.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.MemberDao;
import com.itheima.dao.PermissionDao;
import com.itheima.dao.RoleDao;
import com.itheima.dao.UserDao;
import com.itheima.pojo.Menu;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import com.itheima.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private MemberDao memberDao;

    public User findByUsername(String username) {

        // 获取User
        User user = userDao.findByUsername(username);
        if(user == null){
            return null;
        }

        // 获取Role
        Integer userId = user.getId();
        Set<Role> roles = roleDao.findByUserId(userId);

        // 获取Permission
        if(roles != null && roles.size() > 0){
            for(Role role : roles){
                Integer roleId = role.getId();
                Set<Permission> permissions = permissionDao.findByRoleId(roleId);
                if(permissions != null && permissions.size() > 0){
                    role.setPermissions(permissions);
                }
            }
            user.setRoles(roles);
        }

        return user;
    }

    //获取当前登录用户的用户名以及对应的模块信息
    @Override
    public List<Menu> findAllModuleByUsername(String username) {
        return memberDao.findAllModuleByUsername(username);
    }
}