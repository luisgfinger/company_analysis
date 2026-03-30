package com.postoBackend.backend.dataImport.service;

import com.postoBackend.backend.dataImport.context.ImportContext;
import com.postoBackend.backend.dataImport.processor.ImportProcessor;
import com.postoBackend.backend.dataImport.context.ImportContextResolver;
import com.postoBackend.backend.dataImport.discovery.FileDiscoveryService;
import com.postoBackend.backend.dataImport.processor.ImportProcessorResolver;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;


@Service
public class ImportService {
    private final FileDiscoveryService fileDiscoveryService;
    private final ImportContextResolver contextResolver;
    private final ImportProcessorResolver processorResolver;

    public ImportService(
            FileDiscoveryService fileDiscoveryService,
            ImportContextResolver contextResolver,
            ImportProcessorResolver processorResolver
    ) {
        this.fileDiscoveryService = fileDiscoveryService;
        this.contextResolver = contextResolver;
        this.processorResolver = processorResolver;
    }

    public void executeImport(Path basePath) {

        List<Path> files = fileDiscoveryService.findCsvFiles(basePath);
        List<ResolvedImport> imports = files.stream()
                .map(contextResolver::resolve)
                .map(context -> new ResolvedImport(context, processorResolver.resolve(context)))
                .sorted(Comparator
                        .comparingInt((ResolvedImport resolvedImport) -> resolvedImport.processor().getOrder())
                        .thenComparing(resolvedImport -> resolvedImport.context().getFilePath().toString()))
                .toList();

        for (ResolvedImport resolvedImport : imports) {
            resolvedImport.processor().process(resolvedImport.context());
        }
    }

    private record ResolvedImport(ImportContext context, ImportProcessor processor) {
    }
}
