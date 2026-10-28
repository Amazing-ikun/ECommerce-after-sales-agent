CREATE TABLE IF NOT EXISTS user_account (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_no    VARCHAR(32)     NOT NULL COMMENT '对外用户编号，如 U1001',
    nickname   VARCHAR(64)     NOT NULL,
    phone      VARCHAR(32)     NULL,
    created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_no (user_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '演示用户；已有用户中心可去掉';

CREATE TABLE IF NOT EXISTS sales_order (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_no       VARCHAR(32)     NOT NULL,
    user_id        BIGINT UNSIGNED NOT NULL,
    status         VARCHAR(32)     NOT NULL COMMENT 'PENDING_SHIPMENT/SHIPPED/DELIVERED/REFUNDING/CANCELLED',
    total_amount   DECIMAL(12, 2)  NOT NULL,
    receiver_name  VARCHAR(64)     NULL,
    receiver_phone VARCHAR(32)     NULL,
    receiver_addr  VARCHAR(255)    NULL,
    paid_at        DATETIME(3)     NULL,
    created_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_created (user_id, created_at),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES user_account (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单主表';

CREATE TABLE IF NOT EXISTS order_item (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id     BIGINT UNSIGNED NOT NULL,
    sku_code     VARCHAR(64)     NOT NULL,
    product_name VARCHAR(128)    NOT NULL,
    quantity     INT             NOT NULL,
    unit_price   DECIMAL(12, 2)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order (order_id),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES sales_order (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单商品明细';

CREATE TABLE IF NOT EXISTS logistics_carrier (
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    carrier_code          VARCHAR(32)     NOT NULL COMMENT 'SF/ZTO/JD',
    carrier_name          VARCHAR(64)     NOT NULL,
    tracking_url_template VARCHAR(255)    NULL COMMENT '如 https://.../{trackingNo}',
    PRIMARY KEY (id),
    UNIQUE KEY uk_carrier_code (carrier_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '承运商字典';

CREATE TABLE IF NOT EXISTS logistics_shipment (
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    shipment_no           VARCHAR(40)     NOT NULL COMMENT '包裹号，一单可多包裹',
    order_id              BIGINT UNSIGNED NOT NULL,
    carrier_id            BIGINT UNSIGNED NOT NULL,
    tracking_no           VARCHAR(64)     NOT NULL,
    direction             VARCHAR(16)     NOT NULL DEFAULT 'FORWARD' COMMENT 'FORWARD 正向/RETURN 退货',
    status                VARCHAR(32)     NOT NULL COMMENT 'PENDING_PICKUP/IN_TRANSIT/OUT_FOR_DELIVERY/DELIVERED/EXCEPTION/RETURNING/RETURNED',
    shipped_at            DATETIME(3)     NULL,
    estimated_delivery_at DATETIME(3)     NULL,
    delivered_at          DATETIME(3)     NULL,
    last_event_time       DATETIME(3)     NULL,
    last_event_desc       VARCHAR(255)    NULL,
    created_at            DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at            DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_shipment_no (shipment_no),
    UNIQUE KEY uk_carrier_tracking (carrier_id, tracking_no),
    KEY idx_order (order_id),
    KEY idx_status_time (status, last_event_time),
    CONSTRAINT fk_shipment_order FOREIGN KEY (order_id) REFERENCES sales_order (id),
    CONSTRAINT fk_shipment_carrier FOREIGN KEY (carrier_id) REFERENCES logistics_carrier (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '物流包裹';

CREATE TABLE IF NOT EXISTS logistics_tracking_event (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    shipment_id       BIGINT UNSIGNED NOT NULL,
    event_fingerprint CHAR(64)        NOT NULL COMMENT 'SHA-256(状态+时间+地点+描述)，用于轮询去重',
    event_time        DATETIME(3)     NOT NULL,
    event_status      VARCHAR(32)     NOT NULL,
    location          VARCHAR(128)    NULL,
    description       VARCHAR(512)    NOT NULL,
    courier_name      VARCHAR(64)     NULL,
    courier_phone     VARCHAR(32)     NULL,
    raw_payload       JSON            NULL COMMENT '承运商原始报文，排查问题用',
    created_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_shipment_fingerprint (shipment_id, event_fingerprint),
    KEY idx_shipment_time (shipment_id, event_time DESC),
    CONSTRAINT fk_event_shipment FOREIGN KEY (shipment_id) REFERENCES logistics_shipment (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '物流轨迹明细';
