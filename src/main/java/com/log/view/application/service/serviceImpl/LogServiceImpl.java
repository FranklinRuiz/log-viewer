package com.log.view.application.service.serviceImpl;

import com.log.view.application.command.KubectlCommand;
import com.log.view.application.dto.LogResponseDto;
import com.log.view.application.service.LogService;
import com.log.view.infrastructure.DataReader;
import com.log.view.persistence.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;
    private final KubectlCommand kubectlCommand;
    private final DataReader dataReader;

    @Override
    public List<String> allComponents() {
        if (!dataReader.isDeploymentTextPresent()) {
            kubectlCommand.deploymentComponentsExecute();
        }
        return logRepository.allComponents();
    }

    @Override
    public boolean syncLogs(List<String> components) {
        try {
            if (components.isEmpty()) {
                components = logRepository.allComponents();
            }
            kubectlCommand.logComponentsExecute(components);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public List<LogResponseDto> logsFilter(String traceId, String message, int pageNumber) {
        return logRepository.logsFilter(traceId, message, pageNumber);
    }
}
