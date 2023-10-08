package com.log.view.presentation;

import com.log.view.application.dto.LogResponseDto;
import com.log.view.application.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @GetMapping("/generate-diagram")
    public String getDataDiagram(@RequestParam(required = false) String traceId) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("template.txt"), StandardCharsets.UTF_8);
        logService.generateDiagram(traceId).forEach(item -> {
            lines.add(item.getComponent() + "," + item.getFill() + "," + item.getStroke() + "," + item.getShape() + "," + item.getIcon() + "," + item.getRefs() + "," + item.getText());
        });
        return String.join("\n", lines);
    }

    @GetMapping("/info-update")
    public String getUpdate() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("updateinfo.txt"), StandardCharsets.UTF_8);
        return String.join("\n", lines);
    }

}
