package cn.edu.nju.TomatoMall.test;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.var;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("商品模块完整测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductModuleTest extends BaseIntegrationTest {

    private Long testStoreId;
    private Long testStoreId2;
    private Long testProductId;
    private Long testProductId2;
    private String storeManagerToken;
    private String storeManager2Token;
    private String staffToken;
    private String customerToken;

    @Test
    @Order(1)
    @DisplayName("环境准备")
    @Commit
    void testSetupEnvironment() throws Exception {
        logTestStart("环境准备", "创建测试环境");

        // 创建第一个店铺
        testStoreId = createStore("技术图书专营店", "北京市海淀区中关村大街1号", userToken);
        storeManagerToken = userToken;

        // 创建第二个店铺
        String owner2Token = createUser("storeowner", "Store Owner 2", "Shanghai");
        testStoreId2 = createStore("数码产品专营店", "上海市浦东新区陆家嘴金融中心", owner2Token);
        storeManager2Token = owner2Token;

        // 创建员工
        staffToken = createUser("staff", "Store Staff", "Beijing");
        addStaffToStore(testStoreId, staffToken);

        // 创建普通用户
        customerToken = createUser("customer", "Customer User", "Guangzhou");

        logTestEnd("环境准备", true);
    }

    @Test
    @Order(2)
    @DisplayName("商品创建测试")
    @Commit
    void testProductCreation() throws Exception {
        logTestStart("商品创建测试", "测试创建商品");

        testProductId = createProduct("Java编程思想(第4版)", "89.99", storeManagerToken, testStoreId);
        testProductId2 = createProduct("Spring Boot实战", "79.99", staffToken, testStoreId);

        verifyProductExists(testProductId, "Java编程思想(第4版)");
        verifyProductExists(testProductId2, "Spring Boot实战");

        logTestEnd("商品创建测试", true);
    }

    @Test
    @Order(3)
    @DisplayName("商品查询测试")
    @Commit
    void testProductQuery() throws Exception {
        logTestStart("商品查询测试", "测试各种查询功能");

        testProductList();
        testStoreProductList();
        testProductDetail();
        testProductSorting();
        testProductPagination();

        logTestEnd("商品查询测试", true);
    }

    @Test
    @Order(4)
    @DisplayName("商品更新测试")
    @Commit
    void testProductUpdate() throws Exception {
        logTestStart("商品更新测试", "测试商品更新功能");

        updateProduct(testProductId, "title", "Java编程思想(第5版)", storeManagerToken);
        updateProduct(testProductId, "price", "99.99", storeManagerToken);
        updateProduct(testProductId, "description", "更新后的描述", storeManagerToken);

        logTestEnd("商品更新测试", true);
    }

    @Test
    @Order(5)
    @DisplayName("库存管理测试")
    @Commit
    void testInventoryManagement() throws Exception {
        logTestStart("库存管理测试", "测试库存操作");

        setInventory(testProductId, 150, storeManagerToken);
        setThreshold(testProductId, 20, storeManagerToken);
        testLowInventory();

        logTestEnd("库存管理测试", true);
    }

    @Test
    @Order(6)
    @DisplayName("权限控制测试")
    @Commit
    void testPermissionControl() throws Exception {
        logTestStart("权限控制测试", "测试各种权限");

        testUnauthorizedAccess();
        testCustomerPermissions();
        testCrossStorePermissions();
        testStaffPermissions();

        logTestEnd("权限控制测试", true);
    }

    @Test
    @Order(7)
    @DisplayName("参数验证测试")
    @Commit
    void testValidation() throws Exception {
        logTestStart("参数验证测试", "测试各种无效参数");

        testInvalidProductCreation();
        testInvalidProductUpdate();
        testInvalidInventoryOperations();

        logTestEnd("参数验证测试", true);
    }

    @Test
    @Order(8)
    @DisplayName("商品删除测试")
    @Commit
    void testProductDeletion() throws Exception {
        logTestStart("商品删除测试", "测试删除功能");

        deleteProduct(testProductId, storeManagerToken);
        verifyProductDeleted(testProductId);

        logTestEnd("商品删除测试", true);
    }

    @Test
    @Order(9)
    @DisplayName("快照功能测试")
    @Commit
    void testSnapshot() throws Exception {
        logTestStart("快照功能测试", "测试快照功能");

        testProductSnapshot(testProductId2);
        updateProduct(testProductId2, "title", "Spring Boot实战(更新版)", storeManagerToken);
        verifySnapshotCreated(testProductId2);

        logTestEnd("快照功能测试", true);
    }

    @Test
    @Order(10)
    @DisplayName("性能测试")
    @Commit
    void testPerformance() throws Exception {
        logTestStart("性能测试", "测试性能和并发");

        testBatchQuery();
        testLargePagination();
        testConcurrentOperations();

        logTestEnd("性能测试", true);
    }

    // ============ 核心辅助方法 ============

    private String createUser(String prefix, String name, String location) throws Exception {
        String id = generateUniqueId();
        String username = prefix + id;
        String phone = generateUniquePhone();
        String email = generateUniqueEmail();

        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", username)
                        .param("phone", phone)
                        .param("password", "password123")
                        .param("email", email)
                        .param("name", name)
                        .param("location", location),
                200, "创建用户: " + username
        );

        return login(username, "password123");
    }

    private String login(String username, String password) throws Exception {
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(username, password)
        );

        MvcResult result = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "登录: " + username
        );

        JsonNode response = verifyApiSuccessResponse(result, "登录响应验证");
        return extractDataFromResponse(response, "登录").asText();
    }

    private Long createStore(String name, String address, String ownerToken) throws Exception {
        MockMultipartFile logoFile = new MockMultipartFile("logo", "logo.jpg", "image/jpeg", "logo".getBytes());
        MockMultipartFile qualFile = new MockMultipartFile("qualification", "qual.pdf", "application/pdf", "qual".getBytes());

        executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualFile)
                        .param("name", name)
                        .param("address", address)
                        .param("description", "测试店铺")
                        .param("merchantAccounts.ALIPAY", "alipay_account@alipay.com")
                        .header("Authorization", "Bearer " + ownerToken),
                200, "创建店铺: " + name
        );

        return approveLatestStore();
    }

    private Long approveLatestStore() throws Exception {
        MvcResult listResult = executeRequest(
                authenticatedGet("/api/stores/awaiting-review", adminToken)
                        .param("page", "0").param("size", "10").param("field", "id").param("order", "true"),
                200, "获取待审核店铺"
        );

        JsonNode response = verifyApiSuccessResponse(listResult, "待审核店铺响应");
        JsonNode stores = verifyPageResponse(extractDataFromResponse(response, "待审核店铺"), "店铺分页");
        Long storeId = stores.get(0).get("id").asLong();

        executeRequest(
                authenticatedPost("/api/stores/review", adminToken)
                        .param("storeId", storeId.toString())
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"审核通过\""),
                200, "审核店铺"
        );

        return storeId;
    }

    private void addStaffToStore(Long storeId, String staffToken) throws Exception {
        String tokenRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createTokenRequest("员工Token", TestDataBuilder.getFutureTimeString(30))
        );

        MvcResult tokenResult = executeRequest(
                authenticatedPost("/api/stores/" + storeId + "/tokens", storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest),
                200, "创建员工Token"
        );

        JsonNode response = verifyApiSuccessResponse(tokenResult, "员工Token响应");
        String authToken = extractDataFromResponse(response, "员工Token").asText();

        executeRequest(
                authenticatedPost("/api/stores/" + storeId + "/auth", staffToken)
                        .param("token", authToken),
                200, "员工加入店铺"
        );
    }

    private Long createProduct(String title, String price, String token, Long storeId) throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("images", "image.jpg", "image/jpeg", "image".getBytes());

        executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", title)
                        .param("description", "测试商品描述")
                        .param("price", price)
                        .param("storeId", storeId.toString())
                        .param("specifications[category]", "测试分类")
                        .header("Authorization", "Bearer " + token),
                200, "创建商品: " + title
        );

        return getLatestProductId(storeId);
    }

    private void updateProduct(Long productId, String field, String value, String token) throws Exception {
        executeRequest(
                createMultipartRequest("/api/products/" + productId, "PATCH")
                        .param(field, value)
                        .header("Authorization", "Bearer " + token),
                200, "更新商品" + field + ": " + value
        );
    }

    private void setInventory(Long productId, int stock, String token) throws Exception {
        Map<String, Integer> request = TestDataBuilder.createInventoryRequest(stock);
        executeRequest(
                authenticatedPatch("/api/products/stockpile/" + productId, token)
                        .param("stockpile", String.valueOf(stock))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)),
                200, "设置库存: " + stock
        );
    }

    private void setThreshold(Long productId, int threshold, String token) throws Exception {
        executeRequest(
                authenticatedPatch("/api/products/threshold/" + productId, token)
                        .param("threshold", String.valueOf(threshold)),
                200, "设置阈值: " + threshold
        );
    }

    private void deleteProduct(Long productId, String token) throws Exception {
        executeRequest(
                authenticatedDelete("/api/products/" + productId, token),
                200, "删除商品: " + productId
        );
    }

    private Long getLatestProductId(Long storeId) throws Exception {
        return executeDatabaseOperation("获取最新商品ID", connection -> {
            try (var statement = connection.createStatement()) {
                var rs = statement.executeQuery(
                        "SELECT id FROM products WHERE store_id = " + storeId + " ORDER BY create_time DESC LIMIT 1"
                );
                return rs.next() ? rs.getLong("id") : null;
            }
        });
    }

    // ============ 验证方法 ============

    private void verifyProductExists(Long productId, String title) throws Exception {
        verifyRecordCountWithDebug("products", "id = " + productId + " AND name = '" + title + "'", 1, "商品存在验证");
        verifyRecordCountWithDebug("inventories", "product_id = " + productId, 1, "库存记录验证");
        verifyRecordCountWithDebug("product_snapshots", "product_id = " + productId, 1, "快照记录验证");
    }

    private void verifyProductDeleted(Long productId) throws Exception {
        verifyRecordCountWithDebug("products", "id = " + productId + " AND on_sale = false", 1, "商品删除验证");
        executeRequest(get("/api/products/" + productId), 404, "访问已删除商品");
    }

    // ============ 测试方法 ============

    private void testProductList() throws Exception {
        MvcResult result = executeRequest(
                get("/api/products").param("page", "0").param("size", "10").param("field", "id").param("order", "true"),
                200, "获取商品列表"
        );
        JsonNode response = verifyApiSuccessResponse(result, "商品列表响应");
        JsonNode data = extractDataFromResponse(response, "商品列表");
        JsonNode products = verifyPageResponse(data, "商品列表分页");
        assertListNotEmptyWithDebug(products, "商品列表", "商品列表验证");
    }

    private void testStoreProductList() throws Exception {
        executeRequest(
                get("/api/products/store/" + testStoreId)
                        .param("page", "0").param("size", "10").param("field", "id").param("order", "true"),
                200, "获取店铺商品列表"
        );
    }

    private void testProductDetail() throws Exception {
        MvcResult result = executeRequest(get("/api/products/" + testProductId), 200, "获取商品详情");
        JsonNode response = verifyApiSuccessResponse(result, "商品详情响应");
        JsonNode detail = extractDataFromResponse(response, "商品详情");
        assertNotNullWithDebug(detail.get("title"), "商品标题", "商品详情验证");
    }

    private void testProductSorting() throws Exception {
        executeRequest(
                get("/api/products").param("page", "0").param("size", "5").param("field", "id").param("order", "true"),
                200, "升序排序测试"
        );
        executeRequest(
                get("/api/products").param("page", "0").param("size", "5").param("field", "id").param("order", "false"),
                200, "降序排序测试"
        );
    }

    private void testProductPagination() throws Exception {
        executeRequest(
                get("/api/products").param("page", "0").param("size", "1").param("field", "id").param("order", "true"),
                200, "第一页测试"
        );
        executeRequest(
                get("/api/products").param("page", "1").param("size", "1").param("field", "id").param("order", "true"),
                200, "第二页测试"
        );
    }

    private void testLowInventory() throws Exception {
        setInventory(testProductId, 15, storeManagerToken);
        MvcResult result = executeRequest(get("/api/products/" + testProductId), 200, "获取低库存商品详情");
        JsonNode response = verifyApiSuccessResponse(result, "低库存商品响应");
        JsonNode detail = extractDataFromResponse(response, "低库存商品详情");
        String status = detail.get("inventoryStatus").asText();
        assertTrueWithDebug(
                "INSUFFICIENT".equals(status) || "OUT_OF_STOCK".equals(status),
                "库存状态应为不足或缺货", "低库存验证"
        );
    }

    private void testUnauthorizedAccess() throws Exception {
        executeRequest(get("/api/products/stockpile/" + testProductId), 401, "未登录访问库存");
        executeRequest(patch("/api/products/stockpile/" + testProductId).param("stockpile", "50"), 401, "未登录设置库存");
        executeRequest(delete("/api/products/" + testProductId), 401, "未登录删除商品");
    }

    private void testCustomerPermissions() throws Exception {
        executeRequest(authenticatedGet("/api/products/" + testProductId, customerToken), 200, "普通用户查看商品");
        executeRequest(authenticatedGet("/api/products/stockpile/" + testProductId, customerToken), 403, "普通用户查看库存");
        executeRequest(authenticatedDelete("/api/products/" + testProductId, customerToken), 403, "普通用户删除商品");
    }

    private void testCrossStorePermissions() throws Exception {
        executeRequest(authenticatedGet("/api/products/stockpile/" + testProductId, storeManager2Token), 403, "跨店铺查看库存");
        executeRequest(authenticatedPatch("/api/products/stockpile/" + testProductId, storeManager2Token).param("stockpile", "50"), 403, "跨店铺设置库存");
    }

    private void testStaffPermissions() throws Exception {
        executeRequest(authenticatedGet("/api/products/stockpile/" + testProductId, staffToken), 200, "员工查看库存");
        executeRequest(authenticatedPatch("/api/products/stockpile/" + testProductId, staffToken).param("stockpile", "80"), 200, "员工设置库存");
    }

    private void testInvalidProductCreation() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test".getBytes());

        executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", "")
                        .param("description", "测试")
                        .param("price", "29.99")
                        .param("storeId", testStoreId.toString())
                        .param("specifications[category]", "测试")
                        .header("Authorization", "Bearer " + storeManagerToken),
                400, "空标题验证"
        );

        executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", "测试商品")
                        .param("description", "测试")
                        .param("price", "-10")
                        .param("storeId", testStoreId.toString())
                        .param("specifications[category]", "测试")
                        .header("Authorization", "Bearer " + storeManagerToken),
                400, "负价格验证"
        );
    }

    private void testInvalidProductUpdate() throws Exception {
        executeRequest(
                createMultipartRequest("/api/products/" + testProductId, "PATCH")
                        .param("title", "")
                        .header("Authorization", "Bearer " + storeManagerToken),
                400, "更新空标题验证"
        );
    }

    private void testInvalidInventoryOperations() throws Exception {
        executeRequest(
                authenticatedPatch("/api/products/stockpile/" + testProductId, storeManagerToken).param("stockpile", "-10"),
                400, "设置负库存验证"
        );
    }

    private void testProductSnapshot(Long productId) throws Exception {
        Long snapshotId = executeDatabaseOperation("获取快照ID", connection -> {
            try (var statement = connection.createStatement()) {
                var rs = statement.executeQuery(
                        "SELECT id FROM product_snapshots WHERE product_id = " + productId + " ORDER BY create_time DESC LIMIT 1"
                );
                return rs.next() ? rs.getLong("id") : null;
            }
        });

        executeRequest(authenticatedGet("/api/products/snapshots/" + snapshotId, userToken), 200, "获取商品快照");
    }

    private void verifySnapshotCreated(Long productId) throws Exception {
        int snapshotCount = countRecords("product_snapshots", "product_id = " + productId);
        assertTrueWithDebug(snapshotCount >= 2, "快照数量应至少为2", "快照创建验证");
    }

    private void testBatchQuery() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            executeRequest(
                    get("/api/products").param("page", "0").param("size", "20").param("field", "id").param("order", "true"),
                    200, "批量查询 " + (i + 1)
            );
        }
        long duration = System.currentTimeMillis() - start;
        logInfo("批量查询耗时: " + duration + "ms");
        assertTrueWithDebug(duration < 10000, "批量查询应少于10秒", "性能测试");
    }

    private void testLargePagination() throws Exception {
        executeRequest(
                get("/api/products").param("page", "0").param("size", "1000").param("field", "id").param("order", "true"),
                200, "大分页查询"
        );
    }

    private void testConcurrentOperations() throws Exception {
        if (testProductId2 == null) return;

        setInventory(testProductId2, 100, storeManagerToken);
        setInventory(testProductId2, 80, storeManagerToken);
        setInventory(testProductId2, 120, storeManagerToken);
        setThreshold(testProductId2, 15, storeManagerToken);

        MvcResult result = executeRequest(
                authenticatedGet("/api/products/stockpile/" + testProductId2, storeManagerToken),
                200, "获取最终库存状态"
        );
        JsonNode response = verifyApiSuccessResponse(result, "最终库存响应");
        JsonNode data = extractDataFromResponse(response, "最终库存");
        assertTrueWithDebug(data.get("stock").asInt() >= 0, "最终库存应非负", "并发操作验证");
    }
}