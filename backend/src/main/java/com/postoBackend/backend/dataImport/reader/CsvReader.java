package com.postoBackend.backend.dataImport.reader;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvReader {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    private static final List<Charset> SUPPORTED_CHARSETS = List.of(
            StandardCharsets.UTF_8,
            WINDOWS_1252
    );

    public List<String[]> read(Path filePath) {
        IOException lastException = null;

        for (Charset charset : SUPPORTED_CHARSETS) {
            try {
                return readWithCharset(filePath, charset);
            } catch (MalformedInputException e) {
                lastException = e;
            } catch (IOException e) {
                throw new RuntimeException("Error reading CSV file: " + filePath, e);
            }
        }

        throw new RuntimeException(
                "Error reading CSV file: " + filePath + ". Supported charsets tried: " + SUPPORTED_CHARSETS,
                lastException
        );
    }

    private List<String[]> readWithCharset(Path filePath, Charset charset) throws IOException {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
            String line;

            while ((line = reader.readLine()) != null) {
                String normalizedLine = stripBom(line);
                String[] columns = normalizedLine.split(";", -1);
                rows.add(columns);
            }
        }

        return rows;
    }

    private String stripBom(String line) {
        if (line != null && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }
}
