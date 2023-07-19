package com.log.view.presentation;

import com.log.view.application.dto.LogResponseDto;
import com.log.view.application.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/component-list")
    public List<String> getComponents() {
        return logService.allComponents();
    }

    @PostMapping("/sync-logs")
    public boolean syncLogs(@RequestBody List<String> components) {
        return logService.syncLogs(components);
    }

    @GetMapping("/filter")
    public List<LogResponseDto> getLogsFilter(@RequestParam(required = false) String traceId, @RequestParam(required = false) String message, @RequestParam int pageNumber) {
        return logService.logsFilter(traceId, message, pageNumber);
    }
}
