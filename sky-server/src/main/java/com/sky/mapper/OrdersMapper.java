package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper {
    void insert(Orders order);

    Orders getByNumber(String number, Long userId);

    void update(Orders orders);


    List<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Integer countStatus(Integer deliveryInProgress);

    List<Orders> getByStatusAndTimeLT(Integer status, LocalDateTime time);

    void cancelBatch(@Param("status") Integer status,
                     @Param("cancelReason") String cancelReason,
                     @Param("cancelTime") LocalDateTime cancelTime,
                     @Param("ids") List<Long> ids);

    void updateStatusBatch(@Param("ids") List<Long> ids,
                           @Param("status") Integer status,
                           @Param("deliveryTime") LocalDateTime deliveryTime);
}
