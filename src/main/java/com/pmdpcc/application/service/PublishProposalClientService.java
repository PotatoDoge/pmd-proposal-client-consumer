package com.pmdpcc.application.service;

import com.pmdpcc.application.port.in.PublishProposalClientPort;
import com.pmdpcc.application.port.out.ProposalClientEventPublisher;
import com.pmdpcc.domain.model.ProposalClient;
import com.pmdpcc.domain.service.ProposalPriorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublishProposalClientService implements PublishProposalClientPort {

    private final ProposalClientEventPublisher proposalClientEventPublisher;
    private final ProposalPriorityService priorityService;

    @Override
    public void publish(ProposalClient proposalClient) {
        log.info("Processing proposal for client: {}", proposalClient.getClientName());

        proposalClient.validate();
        log.info("Proposal validated successfully");

        /**
         * 1. Check if client is already saved in DB
         * 1.1. If not saved, save it. If already exists, do nothing
         * 2. Generate UUID for proposal and save in DB
         * 2.1. Send event to external service with proposal
         */

        proposalClientEventPublisher.publish(proposalClient);

        log.info("Proposal published successfully for client: {}", proposalClient.getClientName());
    }

}
