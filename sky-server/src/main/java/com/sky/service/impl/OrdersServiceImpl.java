package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.DistanceUtil;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DistanceUtil distanceUtil;



    @Override
    public OrdersPaymentVO updatePayMentStatus(OrdersPaymentDTO ordersPaymentDTO) {

        String orderNumber = ordersPaymentDTO.getOrderNumber();
        Long UserId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();

        Orders orders = Orders.builder()
                .userId(UserId)
                .number(orderNumber)
                .payStatus(Orders.PAID)
                .status(Orders.TO_BE_CONFIRMED)
                .checkoutTime(now)
                .build();

        orderMapper.update(orders);

        OrdersPaymentVO ordersPaymentVO = new OrdersPaymentVO();
        ordersPaymentVO.setEstimatedDeliveryTime(now.plusHours(1));
        return ordersPaymentVO;
    }

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {




        //处理业务异常（地址簿为空、 购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        String userAddress = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        System.out.println("用户收货地址: " + userAddress);
        try {
            boolean flag = distanceUtil.checkDistance(userAddress);
            if(!flag){
                throw new OrderBusinessException(MessageConstant.NOT_DELIVER_TO_THIS_ADDRESS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();

        shoppingCart.setUserId(userId);
        List<ShoppingCart> carts = shoppingCartMapper.list(shoppingCart);

        if (carts == null || carts.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向我们订单表添加1条数据
        Orders orders = new Orders();

        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
/*        orders.setPackAmount(ordersSubmitDTO.getPackAmount());
        orders.setEstimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime());*/

        orderMapper.insert(orders);


        //向订单明细表添加n条数据
        List<OrderDetail> orderDetailList = carts.stream().map(x -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(x, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailMapper.insertBatch(orderDetailList);


        //清空购物车数据

        shoppingCartMapper.delete(shoppingCart);

        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO dto) {

        // ① 只对 orders 主表分页
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        List<Orders> ordersList = orderMapper.page(dto);

        // ✅ PageHelper 生效时，ordersList 实际类型是 Page<Orders>
        Page<Orders> page = (Page<Orders>) ordersList;

        if (ordersList == null || ordersList.isEmpty()) {
            return new PageResult(0L, Collections.emptyList());
        }

        // ② 拿这一页订单id
        List<Long> orderIds = ordersList.stream()
                .map(Orders::getId)
                .collect(Collectors.toList());

        // ③ 一次性查出这一页所有订单明细
        List<OrderDetail> detailList = orderDetailMapper.listByOrderIds(orderIds);

        // ④ 明细转 VO 后按 orderId 分组：Map<订单id, List<明细VO>>
        Map<Long, List<OrderDetail>> detailMap = detailList.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        // ⑤ 组装 OrdersVO
        List<OrdersVO> voList = new ArrayList<>(ordersList.size());
        for (Orders o : ordersList) {
            OrdersVO vo = new OrdersVO();
            BeanUtils.copyProperties(o, vo);

            // ✅ 这里不会报错：两边都是 List<OrderDetailVO>
            vo.setOrderDetailList(
                    detailMap.getOrDefault(o.getId(), Collections.emptyList())
            );

            voList.add(vo);
        }

        // ⑥ 返回：total 来自 Page<Orders>，records 用 voList
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    @Transactional
    public OrdersVO getOrderDetail(Long id) {

        Orders orders = orderMapper.getById(id);
        OrdersVO res = new OrdersVO();
        BeanUtils.copyProperties(orders, res);
        if (orders != null) {
            List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(id);
            res.setOrderDetailList(orderDetailList);
        }
        return res;
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        if (orderDetailList == null || orderDetailList.size() == 0) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<ShoppingCart> carts = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart cart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, cart);
            cart.setUserId(userId);
            cart.setCreateTime(LocalDateTime.now());
            carts.add(cart);
        }

        shoppingCartMapper.insertBatch(carts);

    }

    @Override
    public void cancel(Long id) {
        Long UserId = BaseContext.getCurrentId();
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setUserId(UserId);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        List<Orders> page = orderMapper.page(ordersPageQueryDTO);

        Page<Orders> p = (Page<Orders>) page;

        List<OrderVO> orderVOList = getOrderVOList(p);
        PageResult pageResult = new PageResult(p.getTotal(), orderVOList);
        return pageResult;
    }

    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void admincancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        if(ordersDB.getPayStatus() == Orders.PAID){
            log.info("订单已支付，不能取消");
        }
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }

    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            log.info("申请退款");
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();

        if(!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailMapper.listByOrderId(orders.getId()));
                String orderDishes = getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }

        return orderVOList;


    }

    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.listByOrderId(orders.getId());
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }


}
