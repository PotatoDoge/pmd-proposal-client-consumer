package com.pmdpcc.infrastructure.config;

import com.pmdpcc.domain.port.in.PublishProposalClientPort;
import com.pmdpcc.domain.port.out.ProposalClientEventPublisher;
import com.pmdpcc.domain.service.PublishProposalClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainWiringConfig {

    @Bean
    public PublishProposalClientPort publishProposalClient(
            ProposalClientEventPublisher proposalClientEventPublisher
    ) {
        return new PublishProposalClient(proposalClientEventPublisher);
    }

}
