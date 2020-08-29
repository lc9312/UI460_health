package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.pojo.Setmeal;

import java.util.List;
import java.util.Map;

/**
 * 体检套餐服务接口
 */
public interface SetmealService {

    public void add(Setmeal setmeal, Integer[] checkgroupIds);

    public PageResult pageQuery(Integer currentPage, Integer pageSize, String queryString);

    public List<Setmeal> findAll();

    public Setmeal findById(int id);

    public List<Map<String,Object>> findSetmealCount();
}
