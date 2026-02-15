package com.pmdpcc.infrastructure.adapter.out.kafka;

import com.pmdpcc.application.port.out.ProposalClientEventPublisher;
import com.pmdpcc.domain.model.ProposalClient;
import com.pmdpcc.infrastructure.adapter.out.kafka.dto.ProposalEvent;
import com.pmdpcc.infrastructure.adapter.out.kafka.mapper.ProposalEventMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class KafkaProposalClientEventPublisher implements ProposalClientEventPublisher {

    private final KafkaTemplate<String, ProposalEvent> kafkaTemplate;
    private final ProposalEventMapper mapper;

    @Override
    public void publish(ProposalClient proposalClient) {
        log.info("Publishing proposal to Kafka for client: {}", proposalClient.getClientName());
        ProposalEvent proposalEvent = mapper.toDTO(proposalClient);

        // Publish to Kafka
        // kafkaTemplate.send("output-topic", event);

        log.info("Proposal published to Kafka successfully for client: {}", proposalClient.getClientName());
    }
}
