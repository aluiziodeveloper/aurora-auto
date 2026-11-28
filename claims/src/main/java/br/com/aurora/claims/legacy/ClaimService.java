package br.com.aurora.claims.legacy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * The claims service of the legacy system, imported as it was. Every step of a claim goes through {@link #process},
 * with the action as text.
 */
public class ClaimService {

    private final ClaimDao dao;

    public ClaimService(ClaimDao dao) {
        this.dao = dao;
    }

    public long report(
            String policyNumber,
            String plate,
            LocalDate eventDate,
            LocalDate noticeDate,
            BigDecimal referenceValue,
            BigDecimal deductible) {
        ClaimRecord c = new ClaimRecord();
        c.setId(dao.nextId());
        c.setPolicyNumber(policyNumber);
        c.setPlate(plate);
        c.setEventDate(eventDate);
        c.setNoticeDate(noticeDate);
        c.setReferenceValue(referenceValue);
        c.setDeductible(deductible);
        c.setStatus("REPORTED");
        dao.save(c);
        return c.getId();
    }

    public ClaimRecord find(long id) {
        return dao.find(id);
    }

    public void process(long id, String action, BigDecimal amount) {
        ClaimRecord c = dao.find(id);
        if (c == null) {
            throw new IllegalArgumentException("claim not found: " + id);
        }
        if (action.equals("INSPECT")) {
            c.setStatus("IN_INSPECTION");
        } else if (action.equals("ESTIMATE")) {
            if (amount != null) {
                c.setRepairEstimate(amount);
                c.setStatus("IN_ADJUSTMENT");
            } else {
                throw new IllegalArgumentException("estimate without amount");
            }
        } else if (action.equals("DECIDE")) {
            BigDecimal value = calculateIndemnity(c);
            c.setIndemnity(value);
            if (c.getLossType().equals("TOTAL")) {
                c.setStatus("TOTAL_LOSS");
            } else {
                c.setStatus("REPAIR_AUTHORIZED");
            }
        } else if (action.equals("START_REPAIR")) {
            c.setStatus("IN_REPAIR");
        } else if (action.equals("FINISH_REPAIR")) {
            c.setStatus("COMPLETED");
        } else if (action.equals("PAY")) {
            c.getPayments().add(c.getIndemnity());
            c.setStatus("INDEMNIFIED");
        } else if (action.equals("DENY")) {
            c.setStatus("DENIED");
        } else {
            throw new IllegalArgumentException("unknown action: " + action);
        }
        dao.save(c);
    }

    public BigDecimal calculateIndemnity(ClaimRecord c) {
        BigDecimal result = BigDecimal.ZERO;
        if (c.getStatus() != null && !c.getStatus().equals("DENIED")) {
            if (c.getRepairEstimate() != null && c.getReferenceValue() != null) {
                BigDecimal limit = c.getReferenceValue().multiply(new BigDecimal("0.75"));
                if (c.getRepairEstimate().compareTo(limit) > 0) {
                    c.setLossType("TOTAL");
                    result = c.getReferenceValue();
                } else {
                    c.setLossType("PARTIAL");
                    if (c.getDeductible() != null) {
                        if (c.getRepairEstimate().compareTo(c.getDeductible()) > 0) {
                            result = c.getRepairEstimate().subtract(c.getDeductible());
                        } else {
                            result = BigDecimal.ZERO;
                        }
                    } else {
                        result = c.getRepairEstimate();
                    }
                }
            }
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal totalPaid(long id) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal p : dao.find(id).getPayments()) {
            total = total.add(p);
        }
        return total;
    }
}
