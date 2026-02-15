package com.pmdpcc.infrastructure.adapter.in.kafka.mapper;

import com.pmdpcc.domain.model.ProposalClient;
import com.pmdpcc.infrastructure.adapter.in.kafka.dto.ProposalClientEvent;
import org.springframework.stereotype.Component;

@Component
public class ProposalClientEventMapper {

    public ProposalClient toDomain(ProposalClientEvent event) {
        if (event == null) {
            return null;
        }

        ProposalClient.BudgetRange budgetRange = null;
        if (event.getBudgetRange() != null) {
            budgetRange = new ProposalClient.BudgetRange(
                    event.getBudgetRange().getMin(),
                    event.getBudgetRange().getMax(),
                    event.getBudgetRange().getCurrency()
            );
        }

        return new ProposalClient(
                event.getClientName(),
                event.getIndustry(),
                event.getCompanySize(),
                event.getContext(),
                event.getPainPoints(),
                budgetRange,
                event.getAdditionalNotes()
        );
    }
}
