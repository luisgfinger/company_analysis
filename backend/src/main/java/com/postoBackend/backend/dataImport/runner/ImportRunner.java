package com.postoBackend.backend.dataImport.runner;

import com.postoBackend.backend.dataImport.service.ImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Profile("!test")
public class ImportRunner implements CommandLineRunner {

    private final ImportService importService;

    public ImportRunner(ImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(String... args) {

        Path basePath = Path.of("data");

        importService.executeImport(basePath);
    }
}
