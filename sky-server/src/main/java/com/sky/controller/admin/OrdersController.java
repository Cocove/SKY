package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrdersVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrdersController")
@RequestMapping("/admin/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/conditionSearch")

    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {

        log.info("查询订单：{}", ordersPageQueryDTO);
        PageResult pageResult = ordersService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO orderStatisticsVO = ordersService.statistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    public Result<OrdersVO> getOrderDetail(@PathVariable Long id) {
        OrdersVO ordersVO = ordersService.getOrderDetail(id);
        return Result.success(ordersVO);
    }

    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        ordersService.confirm(ordersConfirmDTO);
        return Result.success();
    }


    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        ordersService.admincancel(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    public Result reject(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        ordersService.reject(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id) {
        ordersService.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id) {
        ordersService.complete(id);
        return Result.success();
    }






}
