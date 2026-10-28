package com.pu.ecommerceaftersalesagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 演示用户白名单。生产环境必须从认证上下文获取用户身份，不能信任客户端传入的 userId。
 */
@Service
public class DemoUserService {

    private static final Map<String, String> USERS;

    static {
        Map<String, String> users = new LinkedHashMap<>();
        users.put("U1001", "张伟");
        users.put("U1002", "李娜");
        USERS = Collections.unmodifiableMap(users);
    }

    private final String defaultUserId;

    public DemoUserService(@Value("${app.demo-user-id:U1001}") String defaultUserId) {
        this.defaultUserId = USERS.containsKey(defaultUserId) ? defaultUserId : "U1001";
    }

    public String resolve(String requestedUserId) {
        String userId = requestedUserId == null || requestedUserId.isBlank()
                ? defaultUserId
                : requestedUserId.trim();
        if (!USERS.containsKey(userId)) {
            throw new IllegalArgumentException("不支持的演示用户：" + userId);
        }
        return userId;
    }

    public Map<String, String> users() {
        return USERS;
    }
}
