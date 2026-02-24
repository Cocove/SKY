package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    void add(ShoppingCart ShoppingCart);

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    void update(ShoppingCart cart);

    void delete(ShoppingCart shoppingCart);
}
