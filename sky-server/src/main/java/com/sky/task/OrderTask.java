package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrdersMapper orderMapper;
    @Scheduled(cron = "0 * * * * ?")
    public void poccesTimeoutOrder() {
        log.info("定时处理超时订单" + LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().minusMinutes(15);

        List<Orders> ordersList = orderMapper.getByStatusAndTimeLT(Orders.PENDING_PAYMENT, time);

        List<Long> idList = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
        orderMapper.cancelBatch(
                Orders.CANCELLED,
                "订单超时，自动取消",
                LocalDateTime.now(),
                idList
        );

        log.info("成功批量取消了 {} 个超时订单", idList.size());
    }


    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨 1 点执行
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中状态的超时订单：{}", LocalDateTime.now());

        // 1. 计算时间边界（当前时间前推1小时，即前一天的 23:59:59 之前的订单）
        LocalDateTime time = LocalDateTime.now().minusHours(1);

        // 2. 查询派送中的订单
        List<Orders> ordersList = orderMapper.getByStatusAndTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        // 3. 【新增】必须加上判空校验！防止给 MyBatis 传空集合导致 SQL 报错
        if (ordersList != null && !ordersList.isEmpty()) {

            // 提取订单 ID 集合
            List<Long> idList = ordersList.stream().map(Orders::getId).collect(Collectors.toList());

            // 4. 【修改】批量更新时，建议除了状态，把“送达时间”也一起更新进去
            // 注意：这需要你去修改 Mapper 接口和 XML，给 updateStatusBatch 增加一个时间参数
            orderMapper.updateStatusBatch(idList, Orders.COMPLETED, LocalDateTime.now());

            // 5. 【新增】打印成功处理了多少条的日志
            log.info("成功将 {} 个派送超时的订单自动流转为已完成状态", idList.size());

        } else {
            log.info("凌晨巡检完毕，当前无派送超时的订单需要处理");
        }
    }
}
