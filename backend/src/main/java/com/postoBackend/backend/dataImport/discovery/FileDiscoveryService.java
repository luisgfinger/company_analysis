package com.postoBackend.backend.dataImport.discovery;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileDiscoveryService {

    public List<Path> findCsvFiles(Path basePath) {
        try (Stream<Path> paths = Files.walk(basePath)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Error scanning directory: " + basePath, e);
        }
    }
}
