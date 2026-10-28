package com.pu.ecommerceaftersalesagent.tool;

import com.pu.ecommerceaftersalesagent.rag.KnowledgeService;
import com.pu.ecommerceaftersalesagent.rag.PolicySnippet;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AfterSalesTool {

    private final KnowledgeService knowledgeService;

    public AfterSalesTool(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Tool(name = "searchPolicy",
            description = "检索星河商城售后政策知识库，用于回答退货、换货、退款时效、退货运费、价保、保修、发票、破损补发等政策问题。传入用户的问题或核心关键词。")
    public String searchPolicy(
            @ToolParam(description = "用户关于售后政策的问题或关键词，例如“七天无理由退货的条件”") String question) {
        if (question == null || question.isBlank()) {
            return "请提供需要查询的售后政策问题。";
        }
        List<PolicySnippet> snippets = knowledgeService.search(question);
        if (snippets.isEmpty()) {
            return "知识库中未检索到与「" + question + "」相关的政策条款。请换个说法再试，或建议用户转人工客服确认。";
        }
        StringBuilder result = new StringBuilder("检索到以下售后政策条款：\n");
        for (int index = 0; index < snippets.size(); index++) {
            PolicySnippet snippet = snippets.get(index);
            result.append("\n[").append(index + 1).append("] 来源：")
                    .append(snippet.source()).append(" · ").append(snippet.section());
            if (snippet.score() != null) {
                result.append("（相似度 ").append(String.format("%.2f", snippet.score())).append("）");
            }
            result.append("\n").append(snippet.content()).append("\n");
        }
        return result.toString().strip();
    }
}
