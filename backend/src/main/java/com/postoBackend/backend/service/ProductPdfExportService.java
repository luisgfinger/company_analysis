package com.postoBackend.backend.service;

import com.postoBackend.backend.service.dto.ProductPdfExportItemRequest;
import com.postoBackend.backend.service.dto.ProductPdfExportRequest;
import com.postoBackend.backend.service.dto.ProductPdfExportResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class ProductPdfExportService {

    private static final Charset PDF_TEXT_CHARSET = Charset.forName("windows-1252");
    private static final String DEFAULT_TITLE = "Relatorio de Produtos";
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int PAGE_MARGIN = 40;
    private static final int CONTENT_WIDTH = PAGE_WIDTH - (PAGE_MARGIN * 2);
    private static final int FIRST_PAGE_ROWS_TOP = 748;
    private static final int FOLLOWING_PAGE_ROWS_TOP = 796;

    private static final int PRODUCT_ROW_HEIGHT = 18;
    private static final int CATEGORY_HEADER_HEIGHT = 18;
    private static final int EMPTY_STATE_HEIGHT = 44;
    private static final int ROW_GAP = 4;
    private static final int CONTENT_BOTTOM_LIMIT = 58;
    private static final int FOOTER_LINE_Y = 46;
    private static final int FOOTER_TEXT_Y = 28;
    private static final int FOOTER_PAGE_X = 440;
    private static final int NAME_COLUMN_X = 52;
    private static final int COST_COLUMN_X = 330;
    private static final int PRICE_COLUMN_X = 410;
    private static final int MARGIN_COLUMN_X = 490;

    private static final int PAGE_CONTENT_HEIGHT_FIRST = FIRST_PAGE_ROWS_TOP - CONTENT_BOTTOM_LIMIT;
    private static final int PAGE_CONTENT_HEIGHT_FOLLOWING = FOLLOWING_PAGE_ROWS_TOP - CONTENT_BOTTOM_LIMIT;

    private static final byte[] PDF_HEADER = new byte[] {
            0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34, 0x0A,
            0x25, (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, 0x0A
    };

    public ProductPdfExportResult export(ProductPdfExportRequest request) {
        if (request == null || request.products() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Products list is required to export the report"
            );
        }

        if (request.products().stream().anyMatch(Objects::isNull)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Products list cannot contain null items"
            );
        }

        String title = resolveTitle(request.title());
        List<ProductPdfExportItemRequest> sortedProducts = sortProductsByCategory(request.products());
        List<ReportRow> rows = buildRows(sortedProducts);
        List<List<ReportRow>> pages = paginateRows(rows);
        List<String> pageContents = buildPageContents(title, request.products().size(), pages);

        return new ProductPdfExportResult(title, buildPdf(pageContents));
    }

    private List<ProductPdfExportItemRequest> sortProductsByCategory(List<ProductPdfExportItemRequest> products) {
        return products.stream()
                .sorted(
                        Comparator.comparing(
                                        (ProductPdfExportItemRequest p) -> normalizeCategory(p.category()),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                                .thenComparing(
                                        p -> sanitizeText(p.name()),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .toList();
    }

    private List<ReportRow> buildRows(List<ProductPdfExportItemRequest> products) {
        List<ReportRow> rows = new ArrayList<>();
        String currentCategory = null;

        for (ProductPdfExportItemRequest product : products) {
            String category = normalizeCategory(product.category());

            if (!category.equalsIgnoreCase(currentCategory)) {
                rows.add(ReportRow.category(category));
                currentCategory = category;
            }

            rows.add(ReportRow.product(product));
        }

        if (rows.isEmpty()) {
            rows.add(ReportRow.empty());
        }

        return rows;
    }

    private List<List<ReportRow>> paginateRows(List<ReportRow> rows) {
        List<List<ReportRow>> pages = new ArrayList<>();
        List<ReportRow> currentPage = new ArrayList<>();
        int remainingHeight = PAGE_CONTENT_HEIGHT_FIRST;
        boolean firstPage = true;
        String activeCategory = null;

        for (ReportRow row : rows) {
            if (row.type == ReportRowType.CATEGORY) {
                activeCategory = row.category;
            }

            int rowHeight = row.height();

            if (!currentPage.isEmpty() && rowHeight > remainingHeight) {
                pages.add(currentPage);
                currentPage = new ArrayList<>();
                firstPage = false;
                remainingHeight = PAGE_CONTENT_HEIGHT_FOLLOWING;
            }

            if (!firstPage && currentPage.isEmpty() && row.type == ReportRowType.PRODUCT && activeCategory != null) {
                ReportRow repeatedCategory = ReportRow.category(activeCategory);
                currentPage.add(repeatedCategory);
                remainingHeight -= repeatedCategory.height();
            }

            currentPage.add(row);
            remainingHeight -= rowHeight;
        }

        if (currentPage.isEmpty()) {
            currentPage.add(ReportRow.empty());
        }

        pages.add(currentPage);
        return pages;
    }

    private List<String> buildPageContents(
            String title,
            int totalProducts,
            List<List<ReportRow>> rowsByPage
    ) {
        int totalPages = rowsByPage.size();
        List<String> pages = new ArrayList<>();

        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            StringBuilder content = new StringBuilder();
            int pageNumber = pageIndex + 1;
            int topY = pageIndex == 0 ? FIRST_PAGE_ROWS_TOP : FOLLOWING_PAGE_ROWS_TOP;
            List<ReportRow> pageRows = rowsByPage.get(pageIndex);

            if (pageIndex == 0) {
                appendFirstPageHeader(content, title, totalProducts);
            }

            for (ReportRow row : pageRows) {
                if (row.type == ReportRowType.CATEGORY) {
                    appendCategoryHeader(content, row.category, topY);
                    topY -= CATEGORY_HEADER_HEIGHT + ROW_GAP;
                    continue;
                }

                if (row.type == ReportRowType.EMPTY) {
                    appendEmptyState(content, topY);
                    topY -= EMPTY_STATE_HEIGHT + ROW_GAP;
                    continue;
                }

                appendProductRow(content, row.product, topY);
                topY -= PRODUCT_ROW_HEIGHT + ROW_GAP;
            }

            appendFooter(content, pageNumber, totalPages);
            pages.add(content.toString());
        }

        return pages;
    }

    private void appendFirstPageHeader(StringBuilder content, String title, int totalProducts) {
        appendText(content, "F1", 18, PAGE_MARGIN, 800, title);
        appendText(content, "F2", 10, PAGE_MARGIN, 782, "Total de produtos: " + totalProducts);
        appendHorizontalLine(content, 768);
    }

    private void appendCategoryHeader(StringBuilder content, String category, int topY) {
        int bottomY = topY - CATEGORY_HEADER_HEIGHT;

        content.append("q\n");
        content.append("0.90 g\n");
        content.append(PAGE_MARGIN).append(' ')
                .append(bottomY).append(' ')
                .append(CONTENT_WIDTH).append(' ')
                .append(CATEGORY_HEADER_HEIGHT).append(" re\n");
        content.append("f\n");
        content.append("Q\n");

        appendText(content, "F1", 10, NAME_COLUMN_X, topY - 12, truncate(category, 38));
        appendText(content, "F1", 8, COST_COLUMN_X, topY - 11, "Custo");
        appendText(content, "F1", 8, PRICE_COLUMN_X, topY - 11, "Preco");
        appendText(content, "F1", 8, MARGIN_COLUMN_X, topY - 11, "Margem");
    }

    private void appendEmptyState(StringBuilder content, int topY) {
        appendEmptyStateBackground(content, topY);
        appendText(content, "F1", 12, 52, topY - 18, "Nenhum produto informado");
        appendText(content, "F2", 10, 52, topY - 34, "Nenhum produto informado para exportacao.");
    }

    private void appendProductRow(StringBuilder content, ProductPdfExportItemRequest product, int topY) {
        appendText(content, "F2", 9, NAME_COLUMN_X, topY - 12, truncate(product.name(), 40));
        appendText(content, "F2", 9, COST_COLUMN_X, topY - 12, formatCurrency(product.cost()));
        appendText(content, "F2", 9, PRICE_COLUMN_X, topY - 12, formatCurrency(product.price()));
        appendText(content, "F2", 9, MARGIN_COLUMN_X, topY - 12, formatPercentage(product.profitMargin()));
        appendHorizontalLine(content, topY - PRODUCT_ROW_HEIGHT);
    }

    private void appendEmptyStateBackground(StringBuilder content, int topY) {
        int bottomY = topY - EMPTY_STATE_HEIGHT;

        content.append("q\n");
        content.append("0.96 g\n");
        content.append("0.82 G\n");
        content.append("0.8 w\n");
        content.append(PAGE_MARGIN).append(' ')
                .append(bottomY).append(' ')
                .append(CONTENT_WIDTH).append(' ')
                .append(EMPTY_STATE_HEIGHT).append(" re\n");
        content.append("B\n");
        content.append("Q\n");
    }

    private void appendFooter(StringBuilder content, int pageNumber, int totalPages) {
        appendHorizontalLine(content, FOOTER_LINE_Y);
        appendText(content, "F2", 9, FOOTER_PAGE_X, FOOTER_TEXT_Y, "Pagina " + pageNumber + " de " + totalPages);
    }

    private void appendHorizontalLine(StringBuilder content, int y) {
        content.append("q\n");
        content.append("0.75 G\n");
        content.append("0.8 w\n");
        content.append(PAGE_MARGIN).append(' ').append(y).append(" m\n");
        content.append(PAGE_WIDTH - PAGE_MARGIN).append(' ').append(y).append(" l\n");
        content.append("S\n");
        content.append("Q\n");
    }

    private void appendText(StringBuilder content, String fontAlias, int fontSize, int x, int y, String text) {
        content.append("BT\n");
        content.append('/').append(fontAlias).append(' ').append(fontSize).append(" Tf\n");
        content.append(x).append(' ').append(y).append(" Td\n");
        content.append(toPdfLiteralString(text)).append(" Tj\n");
        content.append("ET\n");
    }

    private byte[] buildPdf(List<String> pageContents) {
        int objectCount = 2 + (pageContents.size() * 2) + 2;
        int fontTitleObjectNumber = objectCount - 1;
        int fontBodyObjectNumber = objectCount;

        List<byte[]> objects = new ArrayList<>();
        objects.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        objects.add(ascii(buildPagesObject(pageContents.size())));

        for (int pageIndex = 0; pageIndex < pageContents.size(); pageIndex++) {
            int pageObjectNumber = 3 + (pageIndex * 2);
            int contentObjectNumber = pageObjectNumber + 1;

            objects.add(ascii(buildPageObject(contentObjectNumber, fontTitleObjectNumber, fontBodyObjectNumber)));
            objects.add(buildContentObject(pageContents.get(pageIndex)));
        }

        objects.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"));
        objects.add(ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));

        return writePdfDocument(objects);
    }

    private String buildPagesObject(int pageCount) {
        StringBuilder kids = new StringBuilder();

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int pageObjectNumber = 3 + (pageIndex * 2);
            kids.append(pageObjectNumber).append(" 0 R ");
        }

        return "<< /Type /Pages /Count " + pageCount + " /Kids [" + kids + "] >>";
    }

    private String buildPageObject(int contentObjectNumber, int fontTitleObjectNumber, int fontBodyObjectNumber) {
        return "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT + "] "
                + "/Resources << /Font << /F1 " + fontTitleObjectNumber + " 0 R /F2 " + fontBodyObjectNumber
                + " 0 R >> >> /Contents " + contentObjectNumber + " 0 R >>";
    }

    private byte[] buildContentObject(String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        output.writeBytes(ascii("<< /Length " + contentBytes.length + " >>\nstream\n"));
        output.writeBytes(contentBytes);
        output.writeBytes(ascii("endstream"));

        return output.toByteArray();
    }

    private byte[] writePdfDocument(List<byte[]> objects) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        output.writeBytes(PDF_HEADER);
        offsets.add(0);

        for (int objectIndex = 0; objectIndex < objects.size(); objectIndex++) {
            offsets.add(output.size());
            output.writeBytes(ascii((objectIndex + 1) + " 0 obj\n"));
            output.writeBytes(objects.get(objectIndex));
            output.writeBytes(ascii("\nendobj\n"));
        }

        int xrefStart = output.size();
        output.writeBytes(ascii("xref\n0 " + (objects.size() + 1) + "\n"));
        output.writeBytes(ascii("0000000000 65535 f \n"));

        for (int objectNumber = 1; objectNumber <= objects.size(); objectNumber++) {
            output.writeBytes(ascii(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(objectNumber))));
        }

        output.writeBytes(ascii("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n"));
        output.writeBytes(ascii("startxref\n" + xrefStart + "\n%%EOF"));

        return output.toByteArray();
    }

    private String resolveTitle(String title) {
        return StringUtils.hasText(title) ? sanitizeText(title) : DEFAULT_TITLE;
    }

    private String truncate(String value, int maxLength) {
        String sanitized = sanitizeText(value);

        if (sanitized.length() <= maxLength) {
            return sanitized;
        }

        return sanitized.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String sanitizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "-";
        }

        return value.replaceAll("\\s+", " ").trim();
    }

    private String normalizeCategory(String value) {
        String sanitized = sanitizeText(value);
        return "-".equals(sanitized) ? "Sem categoria" : sanitized;
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        DecimalFormat format = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(new Locale("pt", "BR")));
        return format.format(value);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        return "R$ " + formatDecimal(value);
    }

    private String formatPercentage(BigDecimal value) {
        if (value == null) {
            return "-";
        }

        return formatDecimal(value) + "%";
    }

    private String toPdfLiteralString(String value) {
        byte[] encoded = value.getBytes(PDF_TEXT_CHARSET);
        StringBuilder builder = new StringBuilder("(");

        for (byte current : encoded) {
            int unsigned = current & 0xFF;

            if (unsigned == '(' || unsigned == ')' || unsigned == '\\') {
                builder.append('\\').append((char) unsigned);
                continue;
            }

            if (unsigned < 32 || unsigned > 126) {
                builder.append('\\').append(String.format(Locale.ROOT, "%03o", unsigned));
                continue;
            }

            builder.append((char) unsigned);
        }

        builder.append(')');
        return builder.toString();
    }

    private byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private enum ReportRowType {
        CATEGORY,
        PRODUCT,
        EMPTY
    }

    private static final class ReportRow {
        private final ReportRowType type;
        private final String category;
        private final ProductPdfExportItemRequest product;

        private ReportRow(ReportRowType type, String category, ProductPdfExportItemRequest product) {
            this.type = type;
            this.category = category;
            this.product = product;
        }

        static ReportRow category(String category) {
            return new ReportRow(ReportRowType.CATEGORY, category, null);
        }

        static ReportRow product(ProductPdfExportItemRequest product) {
            return new ReportRow(ReportRowType.PRODUCT, null, product);
        }

        static ReportRow empty() {
            return new ReportRow(ReportRowType.EMPTY, null, null);
        }

        int height() {
            return switch (type) {
                case CATEGORY -> CATEGORY_HEADER_HEIGHT + ROW_GAP;
                case PRODUCT -> PRODUCT_ROW_HEIGHT + ROW_GAP;
                case EMPTY -> EMPTY_STATE_HEIGHT + ROW_GAP;
            };
        }
    }
}
