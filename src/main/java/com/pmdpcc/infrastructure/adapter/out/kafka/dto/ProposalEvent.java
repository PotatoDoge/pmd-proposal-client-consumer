package com.pmdpcc.infrastructure.adapter.out.kafka.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProposalEvent {
    private String industry;
    private String companySize;
    private String context;
    private List<String> painPoints;
    private BudgetRange budgetRange;
    private String additionalNotes;

    @Data
    public static class BudgetRange {
        private Integer min;
        private Integer max;
        private String currency;
    }
}
