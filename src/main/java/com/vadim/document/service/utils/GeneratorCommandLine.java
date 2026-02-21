package com.vadim.document.service.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratorCommandLine implements CommandLineRunner {

    private final DocumentGenerator documentGenerator;

    @Override
    public void run(String... args) throws Exception {
        for (String arg : args) {
            if (arg.startsWith("--generate=")) {
                String filePath = arg.substring("--generate=".length());
                generateFromFile(filePath);
            }
        }
    }

    private void generateFromFile(String filePath) {
        try {
            String content = Files.readString(Paths.get(filePath));
            int count = Integer.parseInt(content.trim());

            log.info("Reading from file {}: {} documents to generate", filePath, count);

            documentGenerator.generateDocuments(
                    count,
                    "generator-user"
            );

        } catch (Exception e) {
            log.error("Failed to generate documents from file: {}", e.getMessage());
        }
    }
}

