package com.postoBackend.backend.dataImport.processor;

import com.postoBackend.backend.dataImport.context.ImportContext;

public interface ImportProcessor {
    default int getOrder() {
        return 100;
    }

    void process(ImportContext context);
}
