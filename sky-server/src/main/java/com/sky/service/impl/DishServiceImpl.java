package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void add(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //获取菜品id

        
        dishMapper.insert(dish);
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();

        if(flavors != null && flavors.size() > 0){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            //保存菜品口味数据到菜品口味表
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        Integer page = dishPageQueryDTO.getPage();
        Integer pageSize = dishPageQueryDTO.getPageSize();

        // 先 count
        long total = PageHelper.count(() -> dishMapper.page(dishPageQueryDTO));

        if (total == 0) {
            return new PageResult(0L, new ArrayList());
        }

        long pages = (total + pageSize - 1) / pageSize;
        int safePage = (page == null || page < 1) ? 1 : page;
        if (safePage > pages) safePage = (int) pages;

        PageHelper.startPage(safePage, pageSize);
        Page<DishVO> p = (Page<DishVO>) dishMapper.page(dishPageQueryDTO);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {

        //判断菜品是否能够删除 --- 启售中不能删除
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断菜品是否能够删除 --- 是否和套餐关联

        List<Long> setmealIdsByDishId = setmealDishMapper.getSetmealIdsByDishIds(ids);

        if(setmealIdsByDishId != null && setmealIdsByDishId.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品表中数据
        dishMapper.deleteByIds(ids);
        //删除口味表中数据

        dishFlavorMapper.deleteByDishIds(ids);

    }

    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        dishVO.setFlavors(dishFlavorMapper.getByDishId(id));
        return dishVO;
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        Long dishId = dishDTO.getId();

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            //保存菜品口味数据到菜品口味表
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if(status == StatusConstant.DISABLE){
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if(setmealIds != null && setmealIds.size() > 0){
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    @Override
    public List<Dish> list(Long categoryId) {

        List<Dish> setmealIds = dishMapper.getByCategoryId(categoryId);

        return setmealIds;
    }

    @Override
    public List<DishVO> listWithFlavor(Long categoryId) {
        List<Dish> dishes = dishMapper.getByCategoryId(categoryId);
        if (dishes == null || dishes.isEmpty()) return new ArrayList<>();

        List<Long> dishIds = dishes.stream().map(Dish::getId).collect(Collectors.toList());

        List<DishFlavor> flavors = dishFlavorMapper.listByDishIds(dishIds);

        Map<Long, List<DishFlavor>> flavorMap = flavors.stream()
                .collect(java.util.stream.Collectors.groupingBy(DishFlavor::getDishId));

        List<DishVO> dishVOList = new ArrayList<>(dishes.size());
        for (Dish dish : dishes) {
            DishVO vo = new DishVO();
            // BeanUtils 可以用，但性能一般；手写/MapStruct 更快更清晰（看你项目）
            org.springframework.beans.BeanUtils.copyProperties(dish, vo);
            vo.setFlavors(flavorMap.getOrDefault(dish.getId(), Collections.emptyList()));
            if(dish.getStatus() == StatusConstant.DISABLE){
                break;
            }else if(dish.getStatus() == StatusConstant.ENABLE){
                dishVOList.add(vo);
            }

        }
        return dishVOList;
    }

}
