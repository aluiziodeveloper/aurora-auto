package br.com.aurora.claims.legacy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A claim as the claims service keeps it. Imported from the legacy claims service as it was: one mutable bean, every
 * field with a getter and a setter.
 */
public class ClaimRecord {

    private long id;
    private String policyNumber;
    private String plate;
    private LocalDate eventDate;
    private LocalDate noticeDate;
    private String status;
    private String lossType;
    private BigDecimal repairEstimate;
    private BigDecimal referenceValue;
    private BigDecimal deductible;
    private BigDecimal indemnity;
    private List<BigDecimal> payments = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalDate getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(LocalDate noticeDate) {
        this.noticeDate = noticeDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLossType() {
        return lossType;
    }

    public void setLossType(String lossType) {
        this.lossType = lossType;
    }

    public BigDecimal getRepairEstimate() {
        return repairEstimate;
    }

    public void setRepairEstimate(BigDecimal repairEstimate) {
        this.repairEstimate = repairEstimate;
    }

    public BigDecimal getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(BigDecimal referenceValue) {
        this.referenceValue = referenceValue;
    }

    public BigDecimal getDeductible() {
        return deductible;
    }

    public void setDeductible(BigDecimal deductible) {
        this.deductible = deductible;
    }

    public BigDecimal getIndemnity() {
        return indemnity;
    }

    public void setIndemnity(BigDecimal indemnity) {
        this.indemnity = indemnity;
    }

    public List<BigDecimal> getPayments() {
        return payments;
    }

    public void setPayments(List<BigDecimal> payments) {
        this.payments = payments;
    }
}
