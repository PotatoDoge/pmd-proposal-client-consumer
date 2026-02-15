package com.pmdpcc.infrastructure.adapter.out.kafka.mapper;

import com.pmdpcc.domain.model.ProposalClient;
import com.pmdpcc.infrastructure.adapter.out.kafka.dto.ProposalEvent;
import org.springframework.stereotype.Component;

@Component
public class ProposalEventMapper {

    public ProposalEvent toDTO(ProposalClient domain) {
        if (domain == null) {
            return null;
        }

        ProposalEvent event = new ProposalEvent();
        event.setIndustry(domain.getIndustry());
        event.setCompanySize(domain.getCompanySize());
        event.setContext(domain.getContext());
        event.setPainPoints(domain.getPainPoints());
        event.setAdditionalNotes(domain.getAdditionalNotes());

        if (domain.getBudgetRange() != null) {
            ProposalEvent.BudgetRange budgetRange = new ProposalEvent.BudgetRange();
            budgetRange.setMin(domain.getBudgetRange().getMin());
            budgetRange.setMax(domain.getBudgetRange().getMax());
            budgetRange.setCurrency(domain.getBudgetRange().getCurrency());
            event.setBudgetRange(budgetRange);
        }

        return event;
    }
}
