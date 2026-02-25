package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrdersPaymentVO;
import com.sky.vo.OrdersVO;

public interface OrdersService {
    OrdersPaymentVO updatePayMentStatus(OrdersPaymentDTO ordersPaymentDTO);

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);


    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    OrdersVO getOrderDetail(Long id);

    void repetition(Long id);

    void cancel(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void admincancel(OrdersCancelDTO ordersCancelDTO);

    void reject(OrdersRejectionDTO ordersRejectionDTO);

    void delivery(Long id);

    void complete(Long id);
}
