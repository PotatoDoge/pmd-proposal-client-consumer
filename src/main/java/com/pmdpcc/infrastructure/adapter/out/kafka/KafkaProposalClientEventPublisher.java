package com.pmdpcc.infrastructure.adapter.out.kafka;

import com.pmdpcc.application.port.out.ProposalClientEventPublisher;
import com.pmdpcc.domain.model.ProposalClient;
import com.pmdpcc.infrastructure.adapter.out.kafka.dto.ProposalEvent;
import com.pmdpcc.infrastructure.adapter.out.kafka.mapper.ProposalEventMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProposalClientEventPublisher implements ProposalClientEventPublisher {

    private final KafkaTemplate<String, ProposalEvent> kafkaTemplate;
    private final ProposalEventMapper mapper;

    @Value("${spring.kafka.producer.topic}")
    private String topicName;

    @Override
    public void publish(ProposalClient proposalClient) {
        log.info("Publishing proposal to Kafka topic '{}' for client: {}", topicName, proposalClient.getClientName());
        ProposalEvent proposalEvent = mapper.toDTO(proposalClient);

        kafkaTemplate.send(topicName, proposalEvent);

        log.info("Proposal published to Kafka topic '{}' successfully for client: {}", topicName, proposalClient.getClientName());
    }
}
