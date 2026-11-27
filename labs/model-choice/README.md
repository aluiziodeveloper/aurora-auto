# Lab: which local model calls the Aurora tools right?

Sends 30 requests from policyholders, in Portuguese (`src/main/resources/cases.json`), to each
model and scores the model's first answer. 26 cases expect one of the six Aurora tools
(`AuroraTools`), 4 expect no tool at all. No tool ever runs: Spring AI 2.1's `ChatModel` returns
the tool calls the model asked for, and the lab compares them with the expected ones.

- **Right tool:** the tool name matches, or no tool when the case expects none.
- **Tool and arguments:** the expected arguments match too (`ArgumentsMatcher`).

## Running it

With `postgres` and `ollama` up and the models pulled, inside the `dev` container:

```bash
./mvnw -q -ntp -pl labs/model-choice spring-boot:run -Dspring-boot.run.arguments="qwen3:1.7b qwen3:4b-instruct qwen3:8b"
```

Every model gets the same options: temperature 0, seed 42, thinking off, and `num_thread` from
`AURORA_OLLAMA_NUM_THREAD`. A warm-up call loads the model and stays out of the measurement.

## Results (2026-11-27)

Ollama 0.40.16 with 4 CPUs (`num_thread` 4), on a 20-CPU host.

| Model | Right tool | Tool and arguments | Latency mean / median / max |
|---|---|---|---|
| `qwen3:1.7b` | 12/30 | 12/30 | 3.9 s / 3.3 s / 8.6 s |
| `qwen3:4b-instruct` | 23/30 | 23/30 | 9.7 s / 10.3 s / 22.9 s |
| `qwen3:8b` | 30/30 | 30/30 | 11.7 s / 10.6 s / 19.2 s |

`qwen3:8b` is the course's chat model. `qwen3:4b-instruct` is the fallback for weaker machines.
`qwen3:1.7b` answers in text instead of calling the tool, and sometimes claims to have acted
("O guincho foi acionado…"). The `qwen3:4b` tag is left out: it always reasons, and with thinking
off the reasoning leaks into the answer.
