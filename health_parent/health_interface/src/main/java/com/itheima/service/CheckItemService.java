package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.pojo.CheckItem;

import java.util.List;

/**
 * 检查项服务接口
 */
public interface CheckItemService {

    public void add(CheckItem checkItem);

    public PageResult pageQuery(Integer currentPage, Integer pageSize, String queryString);

    public void delete(Integer id);

    public CheckItem findById(Integer id);

    public void edit(CheckItem checkItem);

    public List<CheckItem> findAll();

}
