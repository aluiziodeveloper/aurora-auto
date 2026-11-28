# INC-01: Ollama opens 10 threads for 4 CPUs

- **Date:** 2026-11-27
- **Module:** 01, Environment and workflow
- **Reproduction:** lesson 01-07, the request to `http://ollama:11434/api/chat` from `dev`

## Summary

The Ollama container is limited to 4 CPUs (`cpus: 4` in `compose.yaml`). A request sent
without `num_thread` made Ollama run the model with 10 threads, sized from the 20 CPUs of the
machine. The threads waited for each other on 4 CPUs, and the same answer took more than
twice as long.

## Impact

Same prompt, `qwen3:8b`, temperature 0, seed 42, thinking off, model already loaded, the same
51 tokens generated:

| | Threads | `eval_duration` | `total_duration` |
|---|---|---|---|
| Healthy (`num_thread` 4) | 4 | 8.6 s | 8.8 s |
| Incident (no `num_thread`) | 10 | 19.8 s | 20.1 s |

The Ollama log, one line per model load:

```
common_param: system_info: n_threads = 4 (n_threads_batch = 4) / 20 | ...
common_param: system_info: n_threads = 10 (n_threads_batch = 10) / 20 | ...
```

No error and no warning: only a slower answer.

## Cause

Ollama does not read the container's CPU quota (the cgroup limit). Without `num_thread`, it
picks the thread count from the CPUs it sees on the host. With more threads than the quota
allows, the container spends its CPU time early in each period and waits for the next one.
The number 10 belongs to this machine; on another one it depends on its CPUs.

## Fix

One number drives both sides. `compose.yaml` defines `OLLAMA_CPUS` (4 by default) once, as
the anchor `x-ollama-cpus`: it is the `cpus` limit of `ollama` and the variable
`AURORA_OLLAMA_NUM_THREAD` of `dev`. Every request sends `num_thread` with that value,
including the lab in `labs/model-choice`.

## Regression check

`labs/model-choice` reads `AURORA_OLLAMA_NUM_THREAD` and sends it as `num_thread` on every
case; its 30-case run kept 4 threads and 11.7 s per case for `qwen3:8b` (lesson 01-08). The
model port of module 06 must do the same.

## Lessons

- A container limit is not a setting the program reads. Check what the program sees.
- A slow answer with no error is still an incident: measure the time, not only the result.
