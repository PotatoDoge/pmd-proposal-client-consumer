package com.pmdpcc.application.config;

import com.pmdpcc.domain.service.ProposalPriorityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainWiringConfig {

    @Bean
    public ProposalPriorityService proposalPriorityService() {
        return new ProposalPriorityService();
    }

}
