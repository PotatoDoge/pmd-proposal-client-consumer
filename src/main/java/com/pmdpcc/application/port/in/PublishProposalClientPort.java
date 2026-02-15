package com.pmdpcc.application.port.in;

import com.pmdpcc.domain.model.ProposalClient;

public interface PublishProposalClientPort {
    void publish(ProposalClient proposalClient);
}
