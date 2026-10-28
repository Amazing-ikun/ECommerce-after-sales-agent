package com.pu.ecommerceaftersalesagent.mapper;

import com.pu.ecommerceaftersalesagent.mapper.row.ShipmentRow;
import com.pu.ecommerceaftersalesagent.mapper.row.TrackingEventRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LogisticsMapper {

    List<ShipmentRow> findShipments(@Param("orderNo") String orderNo, @Param("userNo") String userNo);

    List<TrackingEventRow> findRecentEvents(@Param("shipmentId") Long shipmentId);
}
