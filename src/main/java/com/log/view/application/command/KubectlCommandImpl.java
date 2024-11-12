package com.log.view.application.command;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KubectlCommandImpl implements KubectlCommand {

    @Value("${command.deploymentFileName}")
    private String deploymentFileName;

    @Value("${command.deploymentComponents}")
    private String deploymentComponentsCommand;

    @Value("${command.logComponents}")
    private String logComponentsCommand;

    @Value("${folder.path}")
    private String folderPath;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void deploymentComponentsExecute() {
        executeCommand(deploymentComponentsCommand + " > " + deploymentFileName);
    }

    @Override
    public void logComponentsExecute(List<String> components) {
        long startTime = System.currentTimeMillis();

        List<Future<?>> futures = components.stream()
                .map(component -> executorService.submit(() ->
                        executeCommand(logComponentsCommand + component + " > " + folderPath + "/" + component + ".json")
                ))
                .collect(Collectors.toList());

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("Error esperando que el comando termine", e);
            }
        }

        long endTime = System.currentTimeMillis();
        long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime);
        log.info("Sincronizacion completada en {} segundos", durationSeconds);
    }

    private void executeCommand(String command) {
         try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.info("Comando ejecutado. Código de salida: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error ejecutando comando: {}", command, e);
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        log.info("Apagando CachedThreadPool Executor...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        log.info("CachedThreadPool Executor apagado correctamente.");
    }

}
