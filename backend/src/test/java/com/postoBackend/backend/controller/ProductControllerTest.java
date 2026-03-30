package com.postoBackend.backend.controller;

import com.postoBackend.backend.service.ProductPdfExportService;
import com.postoBackend.backend.service.ProductService;
import com.postoBackend.backend.service.dto.ProductPdfExportRequest;
import com.postoBackend.backend.service.dto.ProductPdfExportResult;
import com.postoBackend.backend.service.dto.ProductResponse;
import com.postoBackend.backend.service.dto.ProductSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductPdfExportService productPdfExportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();
        PageableHandlerMethodArgumentResolver pageableResolver =
                new PageableHandlerMethodArgumentResolver(sortResolver);

        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService, productPdfExportService))
                .setCustomArgumentResolvers(pageableResolver, sortResolver)
                .build();
    }

    @Test
    void findPagedReturnsPageAndForwardsFilters() throws Exception {
        ProductResponse response = new ProductResponse(
                1L,
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                "COMBUSTIVEIS",
                "123"
        );

        when(productService.findPaged(any(ProductSearchCriteria.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 5, Sort.by("name")), 1));

        mockMvc.perform(get("/api/v1/products")
                        .param("q", "diesel")
                        .param("category", "comb")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("DIESEL S10"))
                .andExpect(jsonPath("$.content[0].code").value("2171"))
                .andExpect(jsonPath("$.content[0].cost").value(6.04))
                .andExpect(jsonPath("$.content[0].profitMargin").value(21.51))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<ProductSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(productService).findPaged(criteriaCaptor.capture(), pageableCaptor.capture());

        assertEquals("diesel", criteriaCaptor.getValue().q());
        assertEquals("comb", criteriaCaptor.getValue().category());
        assertEquals(1, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void findAllReturnsListAndForwardsSort() throws Exception {
        ProductResponse response = new ProductResponse(
                1L,
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                "COMBUSTIVEIS",
                "123"
        );

        when(productService.findAll(any(ProductSearchCriteria.class), any(Sort.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/products/all")
                        .param("barcode", "123")
                .param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].barcode").value("123"))
                .andExpect(jsonPath("$[0].cost").value(6.04))
                .andExpect(jsonPath("$[0].profitMargin").value(21.51))
                .andExpect(jsonPath("$[0].price").value(7.69));

        ArgumentCaptor<ProductSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        verify(productService).findAll(criteriaCaptor.capture(), sortCaptor.capture());

        assertEquals("123", criteriaCaptor.getValue().barcode());
        assertEquals("price: DESC", sortCaptor.getValue().toString());
    }

    @Test
    void findByIdReturnsSingleProduct() throws Exception {
        ProductResponse response = new ProductResponse(
                1L,
                "2171",
                "DIESEL S10",
                new BigDecimal("7.69"),
                new BigDecimal("6.04"),
                new BigDecimal("21.51"),
                "COMBUSTIVEIS",
                "123"
        );

        when(productService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cost").value(6.04))
                .andExpect(jsonPath("$.profitMargin").value(21.51))
                .andExpect(jsonPath("$.name").value("DIESEL S10"));
    }

    @Test
    void exportPdfReturnsAttachmentAndForwardsPayload() throws Exception {
        byte[] pdfBytes = "fake-pdf".getBytes(StandardCharsets.US_ASCII);

        when(productPdfExportService.export(any(ProductPdfExportRequest.class)))
                .thenReturn(new ProductPdfExportResult("Produtos Filtrados", pdfBytes));

        mockMvc.perform(post("/api/v1/products/export/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Produtos Filtrados",
                                  "products": [
                                    {
                                      "code": "2171",
                                      "name": "DIESEL S10",
                                      "price": 7.69,
                                      "cost": 6.04,
                                      "profitMargin": 21.51,
                                      "category": "COMBUSTIVEIS",
                                      "barcode": "123"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("produtos-filtrados.pdf")))
                .andExpect(content().bytes(pdfBytes));

        ArgumentCaptor<ProductPdfExportRequest> requestCaptor =
                ArgumentCaptor.forClass(ProductPdfExportRequest.class);

        verify(productPdfExportService).export(requestCaptor.capture());

        assertEquals("Produtos Filtrados", requestCaptor.getValue().title());
        assertEquals(1, requestCaptor.getValue().products().size());
        assertEquals("2171", requestCaptor.getValue().products().get(0).code());
    }
}
