package com.log.view.application.service;

import com.log.view.application.dto.LogResponseDto;

import java.util.List;

public interface LogService {
    List<String> allComponents();

    boolean syncLogs(List<String> components);

    List<LogResponseDto> logsFilter(String traceId, String message, int pageNumber);
}
