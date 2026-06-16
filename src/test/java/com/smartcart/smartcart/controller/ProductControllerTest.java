package com.smartcart.smartcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.smartcart.config.JacksonConfig;
import com.smartcart.smartcart.dto.ProductDTO;
import com.smartcart.smartcart.security.JwtAuthFilter;
import com.smartcart.smartcart.security.JwtUtil;
import com.smartcart.smartcart.service.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonConfig.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ProductService productService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .description("Apple smartphone")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .category("Electronics")
                .build();
    }

    @Test
    @DisplayName("GET /api/products - returns paginated list")
    @WithMockUser
    void getAllProducts_ReturnsList() throws Exception {
        Page<ProductDTO> page = new PageImpl<>(
                List.of(testProductDTO),
                PageRequest.of(0, 10), 1);
        when(productService.getAllProducts(0, 10, "id"))
                .thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - returns product")
    @WithMockUser
    void getProductById_ReturnsProduct() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(testProductDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("POST /api/products - creates product")
    @WithMockUser(roles = "ADMIN")
    void createProduct_ReturnsCreated() throws Exception {
        when(productService.createProduct(any()))
                .thenReturn(testProductDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(testProductDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - updates product")
    @WithMockUser(roles = "ADMIN")
    void updateProduct_ReturnsUpdated() throws Exception {
        when(productService.updateProduct(eq(1L), any()))
                .thenReturn(testProductDTO);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(testProductDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - deletes product")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ReturnsNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/products/search - returns results")
    @WithMockUser
    void searchProducts_ReturnsResults() throws Exception {
        Page<ProductDTO> page = new PageImpl<>(
                List.of(testProductDTO),
                PageRequest.of(0, 10), 1);
        when(productService.searchProducts(
                eq("iPhone"), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/products/search")
                        .param("name", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("GET /api/products/category - returns filtered")
    @WithMockUser
    void getByCategory_ReturnsFiltered() throws Exception {
        Page<ProductDTO> page = new PageImpl<>(
                List.of(testProductDTO),
                PageRequest.of(0, 10), 1);
        when(productService.getProductsByCategory(
                eq("Electronics"), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get(
                        "/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category")
                        .value("Electronics"));
    }
}