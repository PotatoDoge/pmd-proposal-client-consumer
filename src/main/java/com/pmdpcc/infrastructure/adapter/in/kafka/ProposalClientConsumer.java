package com.pmdpcc.infrastructure.adapter.in.kafka;

import com.pmdpcc.application.port.in.PublishProposalClientPort;
import com.pmdpcc.domain.model.ProposalClient;
import com.pmdpcc.infrastructure.adapter.in.kafka.dto.ProposalClientEvent;
import com.pmdpcc.infrastructure.adapter.in.kafka.mapper.ProposalClientEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProposalClientConsumer {

    private final PublishProposalClientPort publishProposalClientPort;
    private final ProposalClientEventMapper mapper;

    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ProposalClientEvent event) {
        log.info("Received message from Kafka for client: {}", event.getClientName());

        try {
            ProposalClient domainModel = mapper.toDomain(event);
            publishProposalClientPort.publish(domainModel);
            log.info("Successfully processed message for client: {}", event.getClientName());
        }
        catch (Exception e) {
            log.error("Error processing Kafka message for client: {}", event.getClientName(), e);
        }
    }
}
