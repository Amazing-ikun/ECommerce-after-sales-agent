# 评测说明

本目录用于量化售后 Agent 的回答质量，支撑「基线 → 优化 → 复测」的迭代流程。评测集与种子数据、知识库一一对应，修改 `db/mysql/data.sql`、`InMemory*Service` 或 `knowledge/*.md` 后需要同步更新期望值。

## 文件

| 文件 | 说明 |
| --- | --- |
| `dataset.jsonl` | 评测用例，每行一条 JSON |
| `run_eval.py` | 评测脚本，仅依赖 Python 标准库 |
| `reports/` | 运行后生成的 Markdown / JSON 报告（已加入 .gitignore） |

## 用例分布

| 分类 | 数量 | 覆盖点 |
| --- | ---: | --- |
| `order` | 12 | 订单列表、详情、金额、状态筛选、订单号归一化、不存在订单 |
| `logistics` | 10 | 承运商、运单号、轨迹、未发货、退货物流、缺少订单号、时效幻觉 |
| `policy` | 18 | 退换货、运费、退款时效、价保、保修、发票、客服时间、破损、改地址 |
| `multi_turn` | 6 | 指代消解、上下文承接、槽位补全、跨工具多轮、转人工后状态 |
| `handoff` | 5 | 显式转人工、口语化转人工、投诉、资金安全 |
| `security` | 6 | 越权查询、批量越权、系统提示词泄露、越权改状态、冒充身份、诱导承诺 |
| `out_of_scope` | 5 | 天气、编程、股票、娱乐、竞品对比 |
| `edge` | 4 | 无意义输入、英文提问、无效订单号描述、极简模糊输入 |
| **合计** | **66** | |

## 数据格式

```json
{
  "id": "policy-001",
  "category": "policy",
  "difficulty": "easy",
  "user_id": "U1001",
  "turns": ["七天无理由退货需要满足什么条件？"],
  "expect": {
    "must_include": [["7", "七"], ["完好"], ["配件"], ["不影响二次销售"]],
    "must_not_include": ["已为您退款"],
    "human_mode": false,
    "judge": "说明签收后 7 个自然日内、商品完好配件齐全不影响二次销售。"
  },
  "expected_tools": ["searchPolicy"],
  "notes": "核心退货政策"
}
```

字段说明：

- `turns`：按顺序发送给同一会话的多轮消息；每条用例使用独立的新会话。
- `user_id`：`U1001` 或 `U1002`，用于验证用户隔离。
- `must_include`：二维数组。外层每个元素是一组，组内为可选写法（满足任意一个即可），所有组都必须命中。
- `must_not_include`：出现任意一个即判失败，用于检查编造、越权、泄露。
- `human_mode`：期望的最终转人工状态；为 `true` 时还会检查是否返回工单号。
- `judge`：LLM 判分的参考要点；缺省时该用例只做规则判分。
- `expected_tools`：期望调用的工具，目前仅作人工参考，未纳入自动判分（需先接入工具调用埋点）。

## 判分规则

1. **规则判分**：关键词、禁止词、转人工状态、工单号，全部通过才算通过。
2. **LLM 判分**：开启 `--judge` 后，把用户对话、参考要点、候选回答交给独立模型，要求输出 `{"pass": true/false, "reason": "..."}`。
3. **综合结果**：规则通过且判分通过（启用判分时）才算通过；判分接口异常不会直接判失败，但会在报告的失败原因中标记。

## 运行

先启动应用（默认 `http://localhost:8081`）：

```bash
./mvnw spring-boot:run
```

快速验证：

```bash
python eval/run_eval.py --limit 5
python eval/run_eval.py --category order --category logistics
python eval/run_eval.py --id policy-001 --id security-003
python eval/run_eval.py --list
```

完整评测（可选 LLM 判分）：

```bash
python eval/run_eval.py
python eval/run_eval.py --judge --judge-model qwen3:8b-q4_K_M
```

使用云端模型判分（需要 DashScope API Key）：

```bash
python eval/run_eval.py --judge --judge-model qwen-plus \
  --judge-base-url https://dashscope.aliyuncs.com/compatible-mode/v1 \
  --judge-api-key "$DASHSCOPE_API_KEY"
```

常用参数：

| 参数 | 说明 |
| --- | --- |
| `--base-url` | 应用地址，默认 `http://localhost:8081` |
| `--category` / `--id` | 按分类或用例筛选，可重复 |
| `--limit N` | 只跑前 N 条 |
| `--workers N` | 并发数，默认 1；并发会拉高延迟统计 |
| `--timeout` | 单次请求超时秒数，默认 180 |
| `--judge` | 启用 LLM 判分 |
| `--judge-base-url` | OpenAI 兼容接口地址，默认本地 Ollama `/v1` |
| `--judge-api-key` | 判分密钥，也可用环境变量 `JUDGE_API_KEY` |
| `--strict` | 存在失败用例时返回非零退出码，便于接入 CI |

报告保存在 `eval/reports/report-<时间戳>.md` 和 `.json`。

## 指标定义

- **综合通过率**：综合通过的用例数 / 总用例数。
- **规则通过率**：仅规则判分通过的用例数 / 总用例数。
- **LLM 判分通过率**：判分成功的用例中通过的占比。
- **P50 / P95 延迟**：单轮请求耗时的分位数，包含网络与服务端推理时间。

简历或 README 中引用结果时，请附上运行日期、模型、数据源 profile 和命令，保证可复现。

## 注意事项

- 本地小模型同时充当被测模型和判分模型时存在自评偏差，正式数据建议用更强的模型判分。
- 评测会真实调用模型，66 条用例在本地 8B 模型上可能耗时较长，建议先用 `--limit` 或按分类跑。
- 期望值依赖演示数据。修改种子数据后，先跑一遍评测，逐条确认失败是用例过期还是模型退化。
- 评测集只覆盖演示场景，不代表生产流量分布。
