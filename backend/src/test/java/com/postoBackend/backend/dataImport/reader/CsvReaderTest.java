package com.postoBackend.backend.dataImport.reader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvReaderTest {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    private final CsvReader csvReader = new CsvReader();

    @TempDir
    Path tempDir;

    @Test
    void readsUtf8Csv() throws IOException {
        Path file = tempDir.resolve("products-utf8.csv");
        String content = """
                Movimento;Tabela;Cod.Grupo;Nome Grupo;Cod.Produto;Codigo deBarras;Nome;NCM;PrecoVenda
                Grupo;A Vista;1.1.;COMBUSTIVEIS;2171;;DIESEL S10 ORIGINAL;27101921;7,69
                """;

        Files.writeString(file, content, StandardCharsets.UTF_8);

        List<String[]> rows = csvReader.read(file);

        assertEquals(2, rows.size());
        assertEquals("DIESEL S10 ORIGINAL", rows.get(1)[6]);
    }

    @Test
    void fallsBackToWindows1252WhenUtf8Fails() throws IOException {
        Path file = tempDir.resolve("products-cp1252.csv");
        String content = """
                Cadastro deProdutosEmiss\u00E3o:20/03/202613:16:26

                Movimento;Tabela;Cod.Grupo;Nome Grupo;Cod.Produto;Codigo deBarras;Nome;NCM;PrecoVenda
                Grupo;\u00C0 Vista;1.1.;COMBUST\u00CDVEIS;2171;;DIESEL S10 ORIGINAL;27101921;7,69
                """;

        Files.writeString(file, content, WINDOWS_1252);

        List<String[]> rows = csvReader.read(file);

        assertEquals(4, rows.size());
        assertEquals("\u00C0 Vista", rows.get(3)[1]);
        assertEquals("COMBUST\u00CDVEIS", rows.get(3)[3]);
    }
}
