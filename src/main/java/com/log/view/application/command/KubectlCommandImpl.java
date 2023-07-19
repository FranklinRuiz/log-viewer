package com.log.view.application.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Override
    public void deploymentComponentsExecute() {
        executeCommand(deploymentComponentsCommand + " > " + deploymentFileName);
    }

    @Override
    public void logComponentsExecute(List<String> components) {
        long startTime = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();
        components.forEach(component -> {
            Thread thread = new Thread(() -> executeCommand(logComponentsCommand + component + " > " + folderPath + "/" + component + ".json"));
            threads.add(thread);
            thread.start();
        });

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threads.clear();
        long endTime = System.currentTimeMillis();
        long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime);
        log.info("Sincronización completada: {} segundos", durationSeconds);
    }

    private void executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("cmd.exe", "/c", command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.info("Comando ejecutado. Codigo de salida: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
