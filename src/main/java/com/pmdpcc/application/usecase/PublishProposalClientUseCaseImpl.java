package com.pmdpcc.application.usecase;

import com.pmdpcc.domain.port.in.PublishProposalClientPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PublishProposalClientUseCaseImpl implements PublishProposalClientUseCase {

    private final PublishProposalClientPort publishProposalClientPort;

}
