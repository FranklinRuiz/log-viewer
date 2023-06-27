package com.log.view.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.log.view.infrastructure.model.LogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataReaderImpl implements DataReader {

    @Value("${folder.path}")
    private String folderPath;

    @Value("${command.deploymentFileName}")
    private String deploymentFileName;

    @Override
    public List<LogModel> jsonLogReader() {
        createLogsFolder();
        List<LogModel> logs = new ArrayList<>();
        List<File> logsFile = filesDirectory();
        logsFile.forEach(log -> logs.addAll(jsonReader(log)));
        return logs;
    }

    @Override
    public List<String> deploymentTextReader() {
        try {
            return Files.lines(Path.of(deploymentFileName))
                    .skip(1)
                    .map(line -> line.split("\\s+")[0].trim())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return List.of();
    }

    @Override
    public boolean isDeploymentTextPresent() {
        Path file = Path.of(deploymentFileName);

        if (Files.exists(file)) {
            try {
                return Files.lines(file)
                        .anyMatch(line -> !line.isBlank());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private List<File> filesDirectory() {
        File directory = new File(folderPath);

        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".json"))
                .collect(Collectors.toList());
    }

    private List<LogModel> jsonReader(File jsonFile) {
        List<LogModel> logModels = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                try {
                    JsonNode logNode = mapper.readTree(linea);

                    LogModel logModel = LogModel.builder()
                            .message(logNode.has("message") ? logNode.get("message").asText() : "")
                            .timestamp(logNode.has("timestamp") ? logNode.get("timestamp").asLong() : 0)
                            .logLevel(logNode.has("log.level") ? logNode.get("log.level").asText() : "")
                            .loggerName(logNode.has("logger.name") ? logNode.get("logger.name").asText() : "")
                            .traceId(logNode.has("traceId") ? logNode.get("traceId").asText() : "")
                            .spanId(logNode.has("spanId") ? logNode.get("spanId").asText() : "")
                            .entityName(logNode.has("entity.name") ? logNode.get("entity.name").asText() : "")
                            .errorClass(logNode.has("error.class") ? logNode.get("error.class").asText() : "")
                            .errorMessage(logNode.has("error.message") ? logNode.get("error.message").asText() : "")
                            .errorStack(logNode.has("error.stack") ? logNode.get("error.stack").asText() : "")
                            .build();

                    if (!logModel.getTraceId().isEmpty()) {
                        logModels.add(logModel);
                    }
                } catch (JsonProcessingException e) {
                    //log.error("La línea no es un objeto JSON válido: {}", linea);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logModels;
    }

    private void createLogsFolder() {
        File folder = new File(folderPath);

        if (!folder.exists()) {

            boolean created = folder.mkdirs();

            if (created) {
                log.info("Carpeta 'logs' creada correctamente.");
            } else {
                log.error("No se pudo crear la carpeta 'logs'.");
            }
        } else {
            log.info("La carpeta 'logs' ya existe.");
        }
    }
}
