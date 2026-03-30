package com.postoBackend.backend.dataImport.context;

import org.springframework.stereotype.Component;

import java.nio.file.Path;


@Component
public class ImportContextResolver {
    public ImportContext resolve(Path filePath) {

        if (filePath.getNameCount() < 2) {
            throw new RuntimeException("Invalid file path structure: " + filePath);
        }

        String module = filePath.getName(1).toString();
        String fileName = filePath.getFileName().toString();

        return new ImportContext(module, filePath, fileName);
    }
}
