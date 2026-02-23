package com.sky.mapper;

import com.sky.entity.SetmealDish;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    List<Long> getSetmealIdsByDishIds(List<Long> ids);

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBySetmealIds(List<Long> ids);

    List<SetmealDish> getBySetmealId(Long setmealId);

    void deleteBySetmealId(Long setmealId);

    List<DishItemVO> getDishesBySetmealId(Long id);
}
