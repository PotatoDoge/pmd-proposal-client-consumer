package com.pmdpcc.infrastructure.adapter.in.kafka;

import com.pmdpcc.application.usecase.PublishProposalClientUseCase;
import com.pmdpcc.infrastructure.adapter.in.kafka.dto.ProposalClientEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProposalClientConsumer {

    private final PublishProposalClientUseCase publishProposalClientUseCase;

    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ProposalClientEvent proposalClient) {
        log.info("Received message from Kafka for client: {}", proposalClient.getClientName());

        try {
            processMessage(proposalClient);
            log.info("Successfully processed message for client: {}", proposalClient.getClientName());
        } catch (Exception e) {
            log.error("Error processing Kafka message for client: {}", proposalClient.getClientName(), e);
            // TODO: Implement error handling strategy (DLQ, retry, etc.)
        }
    }

    private void processMessage(ProposalClientEvent proposalClient) {
        log.info("Processing proposal for client: {}", proposalClient.getClientName());
        log.info("Industry: {}", proposalClient.getIndustry());
        log.info("Company Size: {}", proposalClient.getCompanySize());
        log.info("Budget Range: {} - {} {}",
                proposalClient.getBudgetRange().getMin(),
                proposalClient.getBudgetRange().getMax(),
                proposalClient.getBudgetRange().getCurrency());
        log.info("Pain Points: {}", proposalClient.getPainPoints());
    }
}
