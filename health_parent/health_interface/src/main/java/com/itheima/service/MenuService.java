package com.itheima.service;

import com.itheima.entity.PageResult;

public interface MenuService {

    PageResult pageQuery(Integer currentPage, Integer pageSize, String queryString);
}
