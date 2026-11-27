package br.com.aurora.labs.modelchoice;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.function.FunctionToolCallback;

/**
 * The six Aurora tools the lab offers the model. Their JSON schemas come from the records. The
 * lab only reads the model's first answer, so no tool ever runs.
 */
final class AuroraTools {

    record ReportClaim(
            @ToolParam(description = "Número da apólice, no formato AUR-AAAA-NNNNNN")
            String policyNumber,

            @ToolParam(description = "Data do evento no formato ISO AAAA-MM-DD")
            String eventDate,

            @ToolParam(description = "Placa do veículo segurado")
            String licensePlate,

            @ToolParam(description = "Local do evento") String location,

            @ToolParam(description = "Descrição do que aconteceu, nas palavras do segurado")
            String description) {}

    enum CoverageType {
        COLLISION,
        THEFT,
        FIRE,
        GLASS,
        THIRD_PARTY
    }

    record CheckCoverage(
            @ToolParam(description = "Número da apólice, no formato AUR-AAAA-NNNNNN")
            String policyNumber,

            @ToolParam(
                    description = "Cobertura consultada: COLLISION (colisão), THEFT (roubo e furto), FIRE (incêndio),"
                            + " GLASS (vidros), THIRD_PARTY (danos a terceiros)")
            CoverageType coverage) {}

    record ScheduleInspection(
            @ToolParam(description = "Número do sinistro, no formato SIN-AAAA-NNNNNN")
            String claimNumber,

            @ToolParam(description = "Data desejada para a vistoria, no formato ISO AAAA-MM-DD")
            String date) {}

    record DispatchTow(
            @ToolParam(description = "Número da apólice, no formato AUR-AAAA-NNNNNN")
            String policyNumber,

            @ToolParam(description = "Endereço onde o veículo está")
            String pickupAddress) {}

    record CalculateIndemnity(
            @ToolParam(description = "Valor do reparo autorizado, em reais")
            BigDecimal repairCost,

            @ToolParam(description = "Valor da franquia, em reais")
            BigDecimal deductible) {}

    record GetClaimStatus(
            @ToolParam(description = "Número do sinistro, no formato SIN-AAAA-NNNNNN")
            String claimNumber) {}

    static List<ToolCallback> all() {
        return List.of(
                tool(
                        "reportClaim",
                        "Registra o aviso de sinistro de um veículo segurado. Use só quando tiver apólice, data,"
                                + " placa, local e descrição.",
                        ReportClaim.class),
                tool("checkCoverage", "Consulta se a apólice do segurado tem uma cobertura.", CheckCoverage.class),
                tool("scheduleInspection", "Agenda a vistoria de um sinistro já registrado.", ScheduleInspection.class),
                tool(
                        "dispatchTow",
                        "Aciona o guincho da assistência 24h para o veículo do segurado.",
                        DispatchTow.class),
                tool(
                        "calculateIndemnity",
                        "Calcula o valor que a Aurora paga num reparo, descontando a franquia.",
                        CalculateIndemnity.class),
                tool("getClaimStatus", "Consulta a situação de um sinistro.", GetClaimStatus.class));
    }

    private static <I> ToolCallback tool(String name, String description, Class<I> type) {
        return FunctionToolCallback.<I, String>builder(name, input -> {
                    throw new IllegalStateException("the lab never runs a tool: " + name);
                })
                .description(description)
                .inputType(type)
                .build();
    }

    private AuroraTools() {}
}
