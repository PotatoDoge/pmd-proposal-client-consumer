package com.pmdpcc.domain.service;

import com.pmdpcc.domain.port.in.PublishProposalClientPort;
import com.pmdpcc.domain.port.out.ProposalClientEventPublisher;

public class PublishProposalClient implements PublishProposalClientPort {

    private final ProposalClientEventPublisher proposalClientEventPublisher;

    public PublishProposalClient(ProposalClientEventPublisher proposalClientEventPublisher) {
        this.proposalClientEventPublisher = proposalClientEventPublisher;
    }

}
