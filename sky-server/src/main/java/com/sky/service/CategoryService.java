package com.sky.service;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CategoryService {
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void add(CategoryDTO categoryDTO);

    void startOrStop(Integer status, Long id);

    void update(CategoryDTO categoryDTO);

    void delete(Long id);

    List<Category> list(Integer type);
}
