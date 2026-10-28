INSERT IGNORE INTO user_account (id, user_no, nickname, phone) VALUES
    (1, 'U1001', '张伟', '13800000001'),
    (2, 'U1002', '李娜', '13800000002');

INSERT IGNORE INTO logistics_carrier (id, carrier_code, carrier_name, tracking_url_template) VALUES
    (1, 'SF', '顺丰速运', 'https://www.sf-express.com/chn/sc/waybill/{trackingNo}'),
    (2, 'ZTO', '中通快递', 'https://www.zto.com/express/{trackingNo}'),
    (3, 'JD', '京东物流', 'https://www.jdl.com/order/{trackingNo}');

INSERT IGNORE INTO sales_order
    (id, order_no, user_id, status, total_amount, receiver_name, receiver_phone, receiver_addr, paid_at, created_at)
VALUES
    (1, 'ORD202610001', 1, 'DELIVERED', 899.00, '张伟', '13800000001', '上海市浦东新区张江路 100 号', '2026-10-07 13:20:00', '2026-10-07 13:00:00'),
    (2, 'ORD202610002', 1, 'SHIPPED', 399.00, '张伟', '13800000001', '上海市浦东新区张江路 100 号', '2026-10-20 09:10:00', '2026-10-20 09:00:00'),
    (3, 'ORD202610003', 1, 'PENDING_SHIPMENT', 658.00, '张伟', '13800000001', '上海市浦东新区张江路 100 号', '2026-10-23 10:05:00', '2026-10-23 10:00:00'),
    (4, 'ORD202610004', 1, 'REFUNDING', 1299.00, '张伟', '13800000001', '上海市浦东新区张江路 100 号', '2026-10-02 08:40:00', '2026-10-02 08:30:00'),
    (5, 'ORD202610005', 2, 'DELIVERED', 1599.00, '李娜', '13800000002', '杭州市西湖区文三路 200 号', '2026-10-15 12:10:00', '2026-10-15 12:00:00'),
    (6, 'ORD202610006', 2, 'SHIPPED', 499.00, '李娜', '13800000002', '杭州市西湖区文三路 200 号', '2026-10-23 15:10:00', '2026-10-23 15:00:00');

INSERT IGNORE INTO order_item (id, order_id, sku_code, product_name, quantity, unit_price) VALUES
    (1, 1, 'SKU-EARPHONE-001', '星河无线降噪耳机 Pro', 1, 899.00),
    (2, 2, 'SKU-AIRFRYER-5L', '家用多功能空气炸锅 5L', 1, 399.00),
    (3, 3, 'SKU-RUNSHOE-01', '轻量缓震跑步鞋', 2, 329.00),
    (4, 4, 'SKU-WATCH-S2', '星河智能手表 S2', 1, 1299.00),
    (5, 5, 'SKU-COFFEE-01', '意式半自动咖啡机', 1, 1599.00),
    (6, 6, 'SKU-KEYBOARD-01', '罗技机械键盘 K845', 1, 499.00);

INSERT IGNORE INTO logistics_shipment
    (id, shipment_no, order_id, carrier_id, tracking_no, direction, status,
     shipped_at, estimated_delivery_at, delivered_at, last_event_time, last_event_desc)
VALUES
    (1, 'SHP20261007001', 1, 1, 'SF1438792650012', 'FORWARD', 'DELIVERED',
     '2026-10-07 14:30:00', '2026-10-09 18:00:00', '2026-10-08 10:12:00',
     '2026-10-08 10:12:00', '已签收，签收人：本人'),
    (2, 'SHP20261022002', 2, 2, 'ZTO785412369', 'FORWARD', 'IN_TRANSIT',
     '2026-10-22 11:05:00', '2026-10-25 18:00:00', NULL,
     '2026-10-23 09:40:00', '快件已到达上海转运中心，准备派送'),
    (3, 'SHP20261020004', 4, 3, 'JDVA00627815421', 'RETURN', 'RETURNING',
     '2026-10-20 19:42:00', '2026-10-23 18:00:00', NULL,
     '2026-10-22 16:25:00', '退货商品已签收，等待质检'),
    (4, 'SHP20261016005', 5, 1, 'SF9988776655443', 'FORWARD', 'DELIVERED',
     '2026-10-16 10:00:00', '2026-10-18 18:00:00', '2026-10-17 14:20:00',
     '2026-10-17 14:20:00', '已签收，签收人：李娜'),
    (5, 'SHP20261024006', 6, 2, 'ZTO998877665', 'FORWARD', 'IN_TRANSIT',
     '2026-10-24 10:00:00', '2026-10-27 18:00:00', NULL,
     '2026-10-25 08:30:00', '快件已到达上海转运中心');

INSERT IGNORE INTO logistics_tracking_event
    (id, shipment_id, event_fingerprint, event_time, event_status, location, description)
VALUES
    (1, 1, SHA2('seed-sf-delivered', 256), '2026-10-08 10:12:00', 'DELIVERED', '上海市浦东新区', '已签收，签收人：本人'),
    (2, 1, SHA2('seed-sf-delivering', 256), '2026-10-08 08:02:00', 'OUT_FOR_DELIVERY', '上海市浦东新区', '快件正在派送中'),
    (3, 1, SHA2('seed-sf-shanghai-hub', 256), '2026-10-07 21:15:00', 'IN_TRANSIT', '上海转运中心', '快件已到达上海转运中心'),
    (4, 1, SHA2('seed-sf-picked-up', 256), '2026-10-07 14:30:00', 'IN_TRANSIT', '杭州收件点', '顺丰速运已收取快件'),
    (5, 2, SHA2('seed-zto-shanghai-hub', 256), '2026-10-23 09:40:00', 'IN_TRANSIT', '上海转运中心', '快件已到达上海转运中心，准备派送'),
    (6, 2, SHA2('seed-zto-hangzhou-hub', 256), '2026-10-22 18:20:00', 'IN_TRANSIT', '杭州分拨中心', '快件已由杭州分拨中心发出'),
    (7, 2, SHA2('seed-zto-picked-up', 256), '2026-10-22 11:05:00', 'IN_TRANSIT', '杭州西湖区', '中通快递已揽收'),
    (8, 3, SHA2('seed-jd-return-received', 256), '2026-10-22 16:25:00', 'RETURNING', '上海退货仓', '退货商品已签收，等待质检'),
    (9, 3, SHA2('seed-jd-return-picked', 256), '2026-10-21 10:08:00', 'RETURNING', '上海市浦东新区', '快递员已取走退货包裹'),
    (10, 3, SHA2('seed-jd-return-booked', 256), '2026-10-20 19:42:00', 'RETURNING', '上海市浦东新区', '已预约上门取件'),
    (11, 4, SHA2('seed-u1002-sf-delivered', 256), '2026-10-17 14:20:00', 'DELIVERED', '杭州市西湖区', '已签收，签收人：李娜'),
    (12, 4, SHA2('seed-u1002-sf-delivering', 256), '2026-10-17 09:10:00', 'OUT_FOR_DELIVERY', '杭州市西湖区', '快件正在派送中'),
    (13, 4, SHA2('seed-u1002-sf-hangzhou-hub', 256), '2026-10-16 20:15:00', 'IN_TRANSIT', '杭州转运中心', '快件已到达杭州转运中心'),
    (14, 4, SHA2('seed-u1002-sf-picked-up', 256), '2026-10-16 10:00:00', 'IN_TRANSIT', '杭州收件点', '顺丰速运已收取快件'),
    (15, 5, SHA2('seed-u1002-zto-shanghai-hub', 256), '2026-10-25 08:30:00', 'IN_TRANSIT', '上海转运中心', '快件已到达上海转运中心'),
    (16, 5, SHA2('seed-u1002-zto-hangzhou-hub', 256), '2026-10-24 18:00:00', 'IN_TRANSIT', '杭州分拨中心', '快件已由杭州分拨中心发出'),
    (17, 5, SHA2('seed-u1002-zto-picked-up', 256), '2026-10-24 10:00:00', 'IN_TRANSIT', '杭州西湖区', '中通快递已揽收');
