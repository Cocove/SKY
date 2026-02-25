package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.vo.OrderDetailVO;
import com.sky.vo.OrdersVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void insertBatch(List<OrderDetail> orderDetailList);

    List<OrderDetail> listByOrderId(Long orderId);

    List<OrderDetail> listByOrderIds(@Param("orderIds") List<Long> orderIds);

    List<OrderDetail> getByOrderId(Long orderId);
}
