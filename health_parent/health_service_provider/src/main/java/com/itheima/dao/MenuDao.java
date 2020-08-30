package com.itheima.dao;

import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

public interface MenuDao {

    Page<Map> selectByCondition(String queryString);

    List<Map> findAllModuleByUsername(String username);
}
