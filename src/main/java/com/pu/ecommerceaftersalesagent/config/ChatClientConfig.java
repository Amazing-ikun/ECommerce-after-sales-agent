package com.pu.ecommerceaftersalesagent.config;

import com.pu.ecommerceaftersalesagent.tool.AfterSalesTool;
import com.pu.ecommerceaftersalesagent.tool.HandoffTool;
import com.pu.ecommerceaftersalesagent.tool.LogisticsTool;
import com.pu.ecommerceaftersalesagent.tool.OrderQueryTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient afterSalesChatClient(ChatClient.Builder builder,
                                           OrderQueryTool orderQueryTool,
                                           LogisticsTool logisticsTool,
                                           AfterSalesTool afterSalesTool,
                                           HandoffTool handoffTool,
                                           @Value("classpath:agent/prompt/system-prompt.st") Resource systemPrompt)
            throws IOException {
        return builder
                .defaultSystem(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultTools(orderQueryTool, logisticsTool, afterSalesTool, handoffTool)
                .build();
    }
}
