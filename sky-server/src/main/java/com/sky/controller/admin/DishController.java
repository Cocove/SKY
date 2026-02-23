package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @CacheEvict(cacheNames = "dishCache", key = " 'dish_' +  #dishDTO.categoryId")
    public Result add(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.add(dishDTO);

        return Result.success();
    }


    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询");
        return Result.success(dishService.page(dishPageQueryDTO));
    }

    @DeleteMapping()
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result delete(@RequestParam  List<Long> ids) {
        log.info("批量删除菜品{}", ids);
        dishService.delete(ids);

        return Result.success();
    }


    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品{}", id);
        return Result.success(dishService.getById(id));
    }

    @PutMapping
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("编辑菜品{}", dishDTO);
        dishService.update(dishDTO);

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("{}菜品{}", status == 1 ? "启售" : "停售", id);
        dishService.startOrStop(status, id);


        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("查询分类下的菜品");
        return Result.success(dishService.list(categoryId));
    }



}
