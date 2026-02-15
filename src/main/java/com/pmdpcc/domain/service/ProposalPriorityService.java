package com.pmdpcc.domain.service;

import com.pmdpcc.domain.model.ProposalClient;

/**
 * ProposalPriorityService - Domain Service
 *
 * Domain services are used when:
 * - Business logic involves MULTIPLE domain objects
 * - Logic doesn't naturally belong to a single entity
 * - You need to coordinate between different domain models
 *
 * Domain services:
 * - Live in domain layer
 * - Are stateless
 * - Contain pure business logic
 * - Have NO infrastructure dependencies
 * - Work only with domain models
 */
public class ProposalPriorityService {

    /**
     * Business rule: Check if proposal should be auto-approved
     * Complex rule involving multiple factors
     */
    public boolean shouldAutoApprove(ProposalClient proposal) {
        // Auto-approve if:
        // - Client is complete
        // - Budget is under $10k
        // - Not urgent (to avoid missing important details)
        return proposal.isComplete()
            && proposal.getBudgetRange().getMax() != null
            && proposal.getBudgetRange().getMax() <= 10_000
            && !proposal.requiresUrgentAttention();
    }

    /**
     * Business rule: Determine review team based on proposal characteristics
     */
    public String assignReviewTeam(ProposalClient proposal) {
        if (proposal.isEnterpriseClient()) {
            return "ENTERPRISE_TEAM";
        }

        String budgetCategory = proposal.getBudgetCategory();
        switch (budgetCategory) {
            case "PREMIUM":
                return "SENIOR_TEAM";
            case "STANDARD":
                return "STANDARD_TEAM";
            case "BASIC":
            default:
                return "JUNIOR_TEAM";
        }
    }

    /**
     * Business rule: Calculate estimated response time in hours
     */
    public int calculateEstimatedResponseTime(ProposalClient proposal) {
        int baseTime = 24; // Default 24 hours

        if (proposal.requiresUrgentAttention()) {
            baseTime = 4; // Urgent: 4 hours
        } else if (proposal.isEnterpriseClient()) {
            baseTime = 8; // Enterprise: 8 hours
        }

        // Add complexity factor based on pain points
        if (proposal.getPainPoints() != null) {
            baseTime += proposal.getPainPoints().size() * 2;
        }

        return baseTime;
    }
}
