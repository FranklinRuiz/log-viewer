package com.log.view.application.service.serviceImpl;

import com.log.view.application.command.KubectlCommand;
import com.log.view.application.dto.DataDiagramDto;
import com.log.view.application.dto.LogResponseDto;
import com.log.view.application.service.LogService;
import com.log.view.infrastructure.DataReader;
import com.log.view.persistence.LogRepository;
import com.log.view.persistence.enums.ComponentType;
import com.log.view.persistence.model.DataModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            logCurrentUpdate("updateinfo.txt");
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

    @Override
    public List<DataDiagramDto> generateDiagram(String traceId) {
        List<DataDiagramDto> dataDiagrams = new ArrayList<>();

        dataDiagrams.add(DataDiagramDto.builder()
                .component("SQL")
                .fill(ComponentType.SQL_DATABASE.getFill())
                .stroke(ComponentType.SQL_DATABASE.getStroke())
                .shape(ComponentType.SQL_DATABASE.getShape())
                .icon(ComponentType.SQL_DATABASE.getIcon())
                .refs("")
                .text("")
                .build());

        dataDiagrams.add(DataDiagramDto.builder()
                .component("external")
                .fill(ComponentType.MAINFRAME.getFill())
                .stroke(ComponentType.MAINFRAME.getStroke())
                .shape(ComponentType.MAINFRAME.getShape())
                .icon(ComponentType.MAINFRAME.getIcon())
                .refs("")
                .text("")
                .build());


        List<DataModel> listNoDuplicates = logRepository.dataDiagram(traceId).stream().distinct().collect(Collectors.toList());
        listNoDuplicates.forEach(item -> {
            Matcher matcherHttp = Pattern.compile("--->(.*?)HTTP/1\\.1").matcher(item.getService());
            if (matcherHttp.find()) item.setService(matcherHttp.group(1).trim());

            Matcher matcherSoap = Pattern.compile("SOAPAction:(.*?)Content-Type").matcher(item.getService());
            if (matcherSoap.find()) item.setService(matcherSoap.group(1).trim());

            Matcher matcherBoot = Pattern.compile("//(.+?-boot)").matcher(item.getService());
            if (matcherBoot.find()) item.setDestination(matcherBoot.group(1));

            if (item.getDestination().isEmpty()) item.setDestination("external");

            ComponentType type = item.getOrigin().equals("frontend") ? ComponentType.WEBSITE : ComponentType.POD;

            dataDiagrams.add(DataDiagramDto.builder()
                    .component(item.getOrigin())
                    .fill(type.getFill())
                    .stroke(type.getStroke())
                    .shape(type.getShape())
                    .icon(type.getIcon())
                    .refs(item.getDestination())
                    .text(item.getService())
                    .build());

        });

        return dataDiagrams;
    }

    public static void logCurrentUpdate(String fileName) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        try {
            Path path = Paths.get(fileName);
            Files.write(path, formattedDateTime.getBytes());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
