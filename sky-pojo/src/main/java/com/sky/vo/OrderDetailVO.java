package com.sky.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderDetailVO {

    private Long id;
    private String name;
    private Long orderId;
    private Long dishId;
    private Long setmealId;      // 你文档是 null，实际建议 Long 可空
    private String dishFlavor;   // null / string
    private Integer number;
    private BigDecimal amount;
    private String image;


}

