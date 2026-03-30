package com.postoBackend.backend.dataImport.context;

import java.nio.file.Path;
import java.util.Objects;

public class ImportContext {

    private final String module;
    private final Path filePath;
    private final String fileName;

    public ImportContext(String module, Path filePath, String fileName) {
        this.module = Objects.requireNonNull(module, "module must not be null");
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null");
    }

    public String getModule() {
        return module;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }
}
