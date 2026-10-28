package com.pu.ecommerceaftersalesagent.mapper;

import com.pu.ecommerceaftersalesagent.mapper.row.OrderRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    List<OrderRow> listByUserNo(@Param("userNo") String userNo);

    OrderRow findByOrderNoAndUserNo(@Param("orderNo") String orderNo, @Param("userNo") String userNo);
}
