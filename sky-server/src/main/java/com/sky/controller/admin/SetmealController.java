package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;


    @PostMapping
    public Result add(@RequestBody SetmealDTO setmealDTO){

        setmealService.add(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(@ModelAttribute SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询{}" , setmealPageQueryDTO);
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除{}" , ids);
        setmealService.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("查询{}" , id);
        SetmealVO SetmealVO = setmealService.getById(id);
        return Result.success(SetmealVO);
    }

    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("编辑套餐{}" , setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,@RequestParam Long id){
        log.info("{}状态{}" , id, status);
        setmealService.startOrStop(status, id);
        return Result.success();
    }



}
