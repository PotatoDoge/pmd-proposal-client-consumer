package com.pmdpcc.domain.model;

import com.pmdpcc.domain.exception.InvalidProposalClientException;

import java.util.List;

/**
 * ProposalClient - Domain Model (Rich Domain Object)
 *
 * This is where BUSINESS LOGIC lives:
 * - Validation rules
 * - Business calculations
 * - State transitions
 * - Invariant enforcement
 *
 * Domain models are:
 * - Pure POJOs (no framework annotations)
 * - Immutable (final fields, no setters)
 * - Self-validating
 * - Framework-independent
 */
public class ProposalClient {
    private final String clientName;
    private final String industry;
    private final String companySize;
    private final String context;
    private final List<String> painPoints;
    private final BudgetRange budgetRange;
    private final String additionalNotes;

    public ProposalClient(String clientName, String industry, String companySize, String context,
                         List<String> painPoints, BudgetRange budgetRange, String additionalNotes) {
        this.clientName = clientName;
        this.industry = industry;
        this.companySize = companySize;
        this.context = context;
        this.painPoints = painPoints;
        this.budgetRange = budgetRange;
        this.additionalNotes = additionalNotes;
    }

    // ========================================
    // BUSINESS LOGIC - Validation
    // ========================================

    /**
     * Validates business rules for proposal client.
     * This is called by application service before processing.
     */
    public void validate() {
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new InvalidProposalClientException("Client name is required");
        }
        if (industry == null || industry.trim().isEmpty()) {
            throw new InvalidProposalClientException("Industry is required");
        }
        if (budgetRange == null) {
            throw new InvalidProposalClientException("Budget range is required");
        }
        budgetRange.validate();
    }

    // ========================================
    // BUSINESS LOGIC - Domain Behavior
    // ========================================

    /**
     * Business rule: Check if client is enterprise level
     * Enterprise = Large company with budget > 100k
     */
    public boolean isEnterpriseClient() {
        return "LARGE".equalsIgnoreCase(companySize)
            && budgetRange.getMax() != null
            && budgetRange.getMax() > 100_000;
    }

    /**
     * Business rule: Check if proposal needs urgent attention
     * Urgent = Enterprise client OR multiple pain points
     */
    public boolean requiresUrgentAttention() {
        return isEnterpriseClient()
            || (painPoints != null && painPoints.size() >= 3);
    }

    /**
     * Business rule: Calculate priority score
     * Used for proposal queue ordering
     */
    public int calculatePriorityScore() {
        int score = 0;

        // Budget weight
        if (budgetRange.getMax() != null) {
            if (budgetRange.getMax() > 100_000) score += 50;
            else if (budgetRange.getMax() > 50_000) score += 30;
            else score += 10;
        }

        // Pain points weight
        if (painPoints != null) {
            score += painPoints.size() * 5;
        }

        // Company size weight
        if ("LARGE".equalsIgnoreCase(companySize)) score += 20;
        else if ("MEDIUM".equalsIgnoreCase(companySize)) score += 10;

        return score;
    }

    /**
     * Business rule: Get budget category for reporting
     */
    public String getBudgetCategory() {
        if (budgetRange.getMax() == null) {
            return "UNSPECIFIED";
        }
        if (budgetRange.getMax() > 100_000) {
            return "ENTERPRISE";
        }
        if (budgetRange.getMax() > 50_000) {
            return "PREMIUM";
        }
        if (budgetRange.getMax() > 10_000) {
            return "STANDARD";
        }
        return "BASIC";
    }

    /**
     * Business rule: Check if proposal is complete
     * Complete = Has all required information
     */
    public boolean isComplete() {
        return clientName != null && !clientName.trim().isEmpty()
            && industry != null && !industry.trim().isEmpty()
            && budgetRange != null && budgetRange.isValid()
            && painPoints != null && !painPoints.isEmpty();
    }

    public String getClientName() {
        return clientName;
    }

    public String getIndustry() {
        return industry;
    }

    public String getCompanySize() {
        return companySize;
    }

    public String getContext() {
        return context;
    }

    public List<String> getPainPoints() {
        return painPoints;
    }

    public BudgetRange getBudgetRange() {
        return budgetRange;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    // ========================================
    // Value Object - BudgetRange
    // ========================================

    /**
     * BudgetRange - Value Object with its own business logic
     * Value objects are immutable and define equality by value, not identity
     */
    public static class BudgetRange {
        private final Integer min;
        private final Integer max;
        private final String currency;

        public BudgetRange(Integer min, Integer max, String currency) {
            this.min = min;
            this.max = max;
            this.currency = currency;
        }

        // Business validation
        public void validate() {
            if (min != null && max != null && min > max) {
                throw new InvalidProposalClientException("Budget min cannot be greater than max");
            }
            if (min != null && min < 0) {
                throw new InvalidProposalClientException("Budget min cannot be negative");
            }
            if (max != null && max < 0) {
                throw new InvalidProposalClientException("Budget max cannot be negative");
            }
        }

        // Business logic - Check if range is valid
        public boolean isValid() {
            return (min != null || max != null)
                && (min == null || min >= 0)
                && (max == null || max >= 0)
                && (min == null || max == null || min <= max);
        }

        // Business logic - Get average budget
        public Integer getAverage() {
            if (min != null && max != null) {
                return (min + max) / 2;
            }
            if (max != null) {
                return max / 2;
            }
            if (min != null) {
                return min;
            }
            return null;
        }

        // Business logic - Format budget for display
        public String formatForDisplay() {
            if (min != null && max != null) {
                return String.format("%s %,d - %,d", currency != null ? currency : "USD", min, max);
            }
            if (max != null) {
                return String.format("%s up to %,d", currency != null ? currency : "USD", max);
            }
            if (min != null) {
                return String.format("%s from %,d", currency != null ? currency : "USD", min);
            }
            return "Budget not specified";
        }

        public Integer getMin() {
            return min;
        }

        public Integer getMax() {
            return max;
        }

        public String getCurrency() {
            return currency;
        }
    }
}
