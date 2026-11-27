package br.com.aurora.labs.modelchoice;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Sends the 30 cases in cases.json to each model given as an argument and scores the first answer:
 * the right tool (or no tool), and the right arguments. Run it against the Ollama in the compose:
 *
 * <pre>./mvnw -q -ntp -pl labs/model-choice spring-boot:run -Dspring-boot.run.arguments="qwen3:8b"</pre>
 */
@SpringBootApplication
public class ModelChoiceApplication implements ApplicationRunner {

    // The cases say "yesterday", "today" and "tomorrow": the prompt fixes the date they mean.
    static final String SYSTEM = """
            Você é o atendente virtual da Aurora Seguros, seguro de automóvel. Hoje é 2026-11-25 (quarta-feira).
            Use as ferramentas quando o pedido do segurado tiver os dados que elas exigem.
            Se faltar algum dado obrigatório, não chame ferramenta: pergunte ao segurado.
            Se a pergunta não pede consulta nem ação, responda em texto, sem ferramenta.
            Converta datas para o formato AAAA-MM-DD.""";

    private final OllamaChatModel chatModel;
    private final JsonMapper json = JsonMapper.builder().build();
    private final ArgumentsMatcher matcher = new ArgumentsMatcher(json);

    ModelChoiceApplication(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public static void main(String[] args) {
        SpringApplication.run(ModelChoiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        JsonNode cases;
        try (InputStream in = new ClassPathResource("cases.json").getInputStream()) {
            cases = json.readTree(in);
        }
        for (String model : args.getNonOptionArgs()) {
            measure(model, cases);
        }
    }

    private void measure(String model, JsonNode cases) {
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(model)
                .temperature(0.0)
                .seed(42)
                // Ollama sizes its threads from the host CPUs, not from the container quota.
                .numThread(Integer.parseInt(System.getenv().getOrDefault("AURORA_OLLAMA_NUM_THREAD", "4")))
                .disableThinking()
                .toolCallbacks(AuroraTools.all())
                .build();
        // Warm-up: loads the model, so the load time stays out of the measurement.
        ask("Olá", options);

        int rightTool = 0;
        int rightArguments = 0;
        List<Long> latencies = new ArrayList<>();
        for (JsonNode testCase : cases) {
            String expected = testCase.get("tool").isNull()
                    ? "none"
                    : testCase.get("tool").asString();
            long start = System.nanoTime();
            AssistantMessage answer = ask(testCase.get("msg").asString(), options);
            long millis = (System.nanoTime() - start) / 1_000_000;
            latencies.add(millis);

            List<AssistantMessage.ToolCall> calls = answer.getToolCalls();
            String got = calls.isEmpty()
                    ? "none"
                    : String.join(
                            "+",
                            calls.stream().map(AssistantMessage.ToolCall::name).toList());
            boolean toolOk = got.equals(expected);
            boolean argumentsOk = toolOk
                    && (calls.isEmpty()
                            || matcher.matches(
                                    testCase.get("args"), calls.getFirst().arguments()));
            rightTool += toolOk ? 1 : 0;
            rightArguments += argumentsOk ? 1 : 0;
            System.out.printf(
                    "%s %6d ms  %-20s %-20s %s%n",
                    testCase.get("id").asString(),
                    millis,
                    expected,
                    got,
                    argumentsOk ? "ok" : toolOk ? "ARGS" : "TOOL");
            String text = answer.getText();
            if (!toolOk && calls.isEmpty() && text != null) {
                // What the model said instead of calling the tool.
                System.out.println("      \"" + abbreviate(text) + "\"");
            }
        }
        int n = latencies.size();
        latencies.sort(null);
        System.out.printf(
                "%s: right tool %d/%d, tool and arguments %d/%d, latency mean %.1f s, median %.1f s, max %.1f s%n",
                model,
                rightTool,
                n,
                rightArguments,
                n,
                latencies.stream().mapToLong(Long::longValue).average().orElse(0) / 1000,
                latencies.get(n / 2) / 1000.0,
                latencies.getLast() / 1000.0);
    }

    private static String abbreviate(String text) {
        String line = text.strip().replaceAll("\\s+", " ");
        return line.length() <= 110 ? line : line.substring(0, 110) + "…";
    }

    private AssistantMessage ask(String message, OllamaChatOptions options) {
        Prompt prompt = new Prompt(List.of(new SystemMessage(SYSTEM), new UserMessage(message)), options);
        // Spring AI marks getResult() as @Nullable: a response may come back without a generation.
        Generation result = chatModel.call(prompt).getResult();
        if (result == null) {
            throw new IllegalStateException("the model returned no answer");
        }
        return result.getOutput();
    }
}
