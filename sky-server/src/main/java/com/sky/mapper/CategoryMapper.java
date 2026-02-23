package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    public Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    @AutoFill(OperationType.INSERT)
    void insert(Category category);

    @AutoFill(OperationType.UPDATE)
    void update(Category category);

    void deleteById(Long id);


    List<Category> list(Integer type);
}
