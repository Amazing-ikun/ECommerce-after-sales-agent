package com.pu.ecommerceaftersalesagent.guard;

import org.springframework.stereotype.Component;

@Component
public class FallbackHandler {

    public String fallbackReply() {
        return "抱歉，智能客服暂时无法响应，可能是模型服务未连接或繁忙。请稍后重试；如需人工协助，请回复“转人工”。";
    }
}
