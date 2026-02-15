package com.pmdpcc.application.port.out;

import com.pmdpcc.domain.model.ProposalClient;

public interface ProposalClientEventPublisher {
    void publish(ProposalClient proposalClient);
}
