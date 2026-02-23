package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;


    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("查询分类下的菜品");
        return Result.success(dishService.listWithFlavor(categoryId));
    }



}
