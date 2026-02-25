package com.sky.controller.user;


import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrdersPaymentVO;
import com.sky.vo.OrdersVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService orderService;



    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PutMapping("/payment")
    public Result paySuccess(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("用户支付成功：{}", ordersPaymentDTO);
        OrdersPaymentVO vo = orderService.updatePayMentStatus(ordersPaymentDTO);
        return Result.success(vo);
    }

    @GetMapping("/historyOrders")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单：page={}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    public Result<OrdersVO> getOrderDetail(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        OrdersVO orderDetail = orderService.getOrderDetail(id);
        return Result.success(orderDetail);
    }

    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单：{}", id);
        orderService.repetition(id);
        return Result.success();
    }


    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) {
        log.info("取消订单：{}", id);
        orderService.cancel(id);
        return Result.success();
    }

}
