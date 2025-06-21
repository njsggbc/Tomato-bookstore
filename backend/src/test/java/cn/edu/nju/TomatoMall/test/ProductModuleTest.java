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

/**
 * 商品模块集成测试
 * 测试商品创建、更新、库存管理、删除等功能
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("商品模块测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductModuleTest extends BaseIntegrationTest {

    private Long testStoreId;
    private Long testProductId;
    private String storeManagerToken;
    private String staffToken;
    private Long staffUserId;

    @Test
    @Order(1)
    @DisplayName("创建测试店铺和员工")
    @Commit
    void testSetupStoreAndStaff() throws Exception {
        logTestStart("创建测试店铺和员工", "为商品测试创建必要的店铺和员工环境");

        checkPreconditions("创建测试店铺和员工",
                "userToken", userToken,
                "adminToken", adminToken);

        // 创建店铺
        Map<String, String> storeData = TestDataBuilder.createStoreParams(
                "技术图书专营店", "北京市海淀区中关村大街1号", "专业的技术图书销售店铺"
        );

        MockMultipartFile logoFile = new MockMultipartFile(
                "logo", "logo.jpg", "image/jpeg", "fake logo content".getBytes()
        );
        MockMultipartFile qualificationFile = new MockMultipartFile(
                "qualification", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        logInfo("创建店铺: " + storeData.get("name"));

        MvcResult createStoreResult = executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", storeData.get("name"))
                        .param("address", storeData.get("address"))
                        .param("description", storeData.get("description"))
                        .param("merchantAccounts.ALIPAY", "alipay@bookstore.com")
                        .header("Authorization", "Bearer " + userToken),
                200, "创建店铺API调用"
        );

        verifyApiSuccessResponse(createStoreResult, "创建店铺响应验证");
        waitFor(200);

        // 获取店铺ID并审核通过
        logInfo("获取待审核店铺列表");
        MvcResult listResult = executeRequest(
                authenticatedGet("/api/stores/awaiting-review", adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "id")
                        .param("order", "true"),
                200, "获取待审核店铺列表API调用"
        );

        JsonNode listResponse = verifyApiSuccessResponse(listResult, "获取待审核店铺列表响应验证");
        JsonNode listData = extractDataFromResponse(listResponse, "获取待审核店铺列表");
        JsonNode pendingStores = verifyPageResponse(listData, "待审核店铺列表分页验证");

        assertListNotEmptyWithDebug(pendingStores, "待审核店铺列表", "获取待审核店铺列表");
        testStoreId = pendingStores.get(0).get("id").asLong();
        logInfo("获取到测试店铺ID: " + testStoreId);

        // 管理员审核通过
        logInfo("管理员审核店铺");
        MvcResult reviewResult = executeRequest(
                authenticatedPost("/api/stores/review", adminToken)
                        .param("storeId", testStoreId.toString())
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"商品测试店铺审核通过\""),
                200, "审核店铺API调用"
        );

        verifyApiSuccessResponse(reviewResult, "审核店铺响应验证");
        waitFor(200);

        verifyRecordCountWithDebug("stores", "id = " + testStoreId + " AND status = 'NORMAL'", 1, "店铺审核通过状态验证");
        storeManagerToken = userToken;

        // 创建员工
        logInfo("创建店铺员工");
        Map<String, Object> staffData = TestDataBuilder.createUserRequest(
                "bookstaff", "13912345678", "password123",
                "staff@bookstore.com", "Book Staff", "Beijing"
        );

        MvcResult staffCreateResult = executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", staffData.get("username").toString())
                        .param("phone", staffData.get("phone").toString())
                        .param("password", staffData.get("password").toString())
                        .param("email", staffData.get("email").toString())
                        .param("name", staffData.get("name").toString())
                        .param("location", staffData.get("location").toString()),
                200, "创建员工用户API调用"
        );

        verifyApiSuccessResponse(staffCreateResult, "创建员工用户响应验证");
        waitFor(100);

        // 员工登录
        logInfo("员工登录");
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest("bookstaff", "password123")
        );

        MvcResult loginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "员工登录API调用"
        );

        JsonNode loginResponse = verifyApiSuccessResponse(loginResult, "员工登录响应验证");
        staffToken = extractDataFromResponse(loginResponse, "员工登录").asText();

        // 获取员工ID
        MvcResult staffInfoResult = executeRequest(
                authenticatedGet("/api/users", staffToken),
                200, "获取员工用户信息API调用"
        );

        JsonNode userResponse = verifyApiSuccessResponse(staffInfoResult, "获取员工用户信息响应验证");
        JsonNode userData = extractDataFromResponse(userResponse, "获取员工用户信息");
        staffUserId = userData.get("id").asLong();
        logInfo("员工用户创建成功，ID: " + staffUserId);

        // 创建员工token并加入店铺
        logInfo("创建员工授权Token");
        String tokenRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createTokenRequest("商品管理员工Token", TestDataBuilder.getFutureTimeString(30))
        );

        MvcResult tokenResult = executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/tokens", storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest),
                200, "创建员工授权Token API调用"
        );

        JsonNode tokenResponse = verifyApiSuccessResponse(tokenResult, "创建员工授权Token响应验证");
        String authToken = extractDataFromResponse(tokenResponse, "创建员工授权Token").asText();

        // 员工加入店铺
        logInfo("员工加入店铺");
        MvcResult joinResult = executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/auth", staffToken)
                        .param("token", authToken),
                200, "员工加入店铺API调用"
        );

        verifyApiSuccessResponse(joinResult, "员工加入店铺响应验证");
        waitFor(200);

        verifyRecordCountWithDebug("employments",
                "store_id = " + testStoreId + " AND employee_id = " + staffUserId,
                1, "员工雇佣关系验证");

        logTestEnd("创建测试店铺和员工", true);
    }

    @Test
    @Order(2)
    @DisplayName("店长创建商品 - 成功案例")
    @Commit
    void testCreateProduct_ByManager_Success() throws Exception {
        logTestStart("店长创建商品", "测试店长创建商品的完整流程");

        checkPreconditions("店长创建商品",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId);

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "java_book.jpg", "image/jpeg", "fake image content".getBytes()
        );

        logInfo("创建商品: Java编程思想(第4版), 价格: ¥89.99");

        MvcResult result = executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", "Java编程思想(第4版)")
                        .param("description", "这是一本经典的Java编程教材，适合初学者和进阶者")
                        .param("price", "89.99")
                        .param("storeId", testStoreId.toString())
                        .param("specifications[author]", "Bruce Eckel")
                        .param("specifications[publisher]", "机械工业出版社")
                        .param("specifications[isbn]", "978-7-111-21382-7")
                        .param("specifications[pages]", "880")
                        .header("Authorization", "Bearer " + storeManagerToken),
                200, "创建商品API调用"
        );

        verifyApiSuccessResponse(result, "创建商品响应验证");
        waitFor(200);

        // 验证数据库中的商品记录 - 使用正确的字段名 name
        verifyRecordCountWithDebug("products", "name = 'Java编程思想(第4版)'", 1, "创建商品数据库验证");

        // 验证库存记录也被创建
        verifyRecordCountWithDebug("inventories",
                "product_id IN (SELECT id FROM products WHERE name = 'Java编程思想(第4版)')",
                1, "商品库存记录验证");

        logTestEnd("店长创建商品", true);
    }

    @Test
    @Order(3)
    @DisplayName("员工创建商品 - 成功案例")
    @Commit
    void testCreateProduct_ByStaff_Success() throws Exception {
        logTestStart("员工创建商品", "测试员工创建商品的权限和流程");

        checkPreconditions("员工创建商品",
                "staffToken", staffToken,
                "testStoreId", testStoreId);

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "spring_book.jpg", "image/jpeg", "fake image content".getBytes()
        );

        logInfo("员工创建商品: Spring Boot实战");

        MvcResult result = executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", "Spring Boot实战")
                        .param("description", "Spring Boot开发实战教程")
                        .param("price", "79.99")
                        .param("storeId", testStoreId.toString())
                        .param("specifications[author]", "Craig Walls")
                        .param("specifications[publisher]", "人民邮电出版社")
                        .param("specifications[category]", "编程技术")
                        .header("Authorization", "Bearer " + staffToken),
                200, "员工创建商品API调用"
        );

        verifyApiSuccessResponse(result, "员工创建商品响应验证");
        waitFor(200);

        verifyRecordCountWithDebug("products", "name = 'Spring Boot实战'", 1, "员工创建商品数据库验证");

        logTestEnd("员工创建商品", true);
    }

    @Test
    @Order(4)
    @DisplayName("获取商品列表")
    @Commit
    void testGetProductList() throws Exception {
        logTestStart("获取商品列表", "测试获取所有商品的分页列表");

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                get("/api/products")
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", pageParams.get("field"))
                        .param("order", pageParams.get("order")),
                200, "获取商品列表API调用"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取商品列表响应验证");
        JsonNode data = extractDataFromResponse(response, "获取商品列表");
        JsonNode productList = verifyPageResponse(data, "商品列表分页验证");

        assertListNotEmptyWithDebug(productList, "商品列表", "获取商品列表");

        testProductId = productList.get(0).get("id").asLong();
        logInfo("获取到测试商品ID: " + testProductId);

        JsonNode firstProduct = productList.get(0);
        assertNotNullWithDebug(firstProduct.get("title"), "商品标题", "商品信息验证");
        assertNotNullWithDebug(firstProduct.get("price"), "商品价格", "商品信息验证");
        assertNotNullWithDebug(firstProduct.get("cover"), "商品封面", "商品信息验证");
        assertNotNullWithDebug(firstProduct.get("sales"), "商品销量", "商品信息验证");
        assertNotNullWithDebug(firstProduct.get("inventoryStatus"), "库存状态", "商品信息验证");

        logTestEnd("获取商品列表", true);
    }

    @Test
    @Order(5)
    @DisplayName("设置商品库存")
    @Commit
    void testSetProductInventory() throws Exception {
        logTestStart("设置商品库存", "测试店长设置商品库存数量");

        checkPreconditions("设置商品库存",
                "storeManagerToken", storeManagerToken,
                "testProductId", testProductId);

        Map<String, Integer> inventoryRequest = TestDataBuilder.createInventoryRequest(100);
        String requestBody = objectMapper.writeValueAsString(inventoryRequest);

        logInfo("设置商品库存为: 100");

        MvcResult result = executeRequest(
                authenticatedPatch("/api/products/stockpile/" + testProductId, storeManagerToken)
                        .param("stockpile", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "设置商品库存API调用"
        );

        verifyApiSuccessResponse(result, "设置商品库存响应验证");
        waitFor(200);

        // 使用API验证库存设置成功
        MvcResult inventoryResult = executeRequest(
                authenticatedGet("/api/products/stockpile/" + testProductId, storeManagerToken),
                200, "获取库存信息验证设置"
        );

        JsonNode inventoryResponse = verifyApiSuccessResponse(inventoryResult, "获取库存信息响应验证");
        JsonNode inventoryData = extractDataFromResponse(inventoryResponse, "获取库存信息");

        assertEqualsWithDebug(100, inventoryData.get("stock").asInt(), "库存数量", "库存设置验证");

        logTestEnd("设置商品库存", true);
    }

    @Test
    @Order(6)
    @DisplayName("测试商品参数验证")
    @Commit
    void testProductValidation() throws Exception {
        logTestStart("测试商品参数验证", "测试各种无效参数的验证");

        checkPreconditions("测试商品参数验证",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId);

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "fake image content".getBytes()
        );

        // 测试空标题
        logInfo("测试空标题验证");
        executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", "")
                        .param("description", "测试描述")
                        .param("price", "29.99")
                        .param("storeId", testStoreId.toString())
                        .param("specifications[category]", "测试")
                        .header("Authorization", "Bearer " + storeManagerToken),
                400, "空标题测试（应失败）"
        );

        // 测试缺少specifications参数
        logInfo("测试缺少specifications参数验证");
        executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", "测试商品")
                        .param("description", "测试描述")
                        .param("price", "29.99")
                        .param("storeId", testStoreId.toString())
                        .header("Authorization", "Bearer " + storeManagerToken),
                400, "缺少specifications参数测试（应失败）"
        );

        logTestEnd("测试商品参数验证", true);
    }

    @Test
    @Order(7)
    @DisplayName("删除商品")
    @Commit
    void testDeleteProduct() throws Exception {
        logTestStart("删除商品", "测试店长删除商品功能");

        checkPreconditions("删除商品",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId);

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "delete_test.jpg", "image/jpeg", "fake image content".getBytes()
        );

        logInfo("创建待删除商品");

        MvcResult createResult = executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", "待删除测试商品")
                        .param("description", "这是一个用于删除测试的商品")
                        .param("price", "39.99")
                        .param("storeId", testStoreId.toString())
                        .param("specifications[category]", "测试商品")
                        .header("Authorization", "Bearer " + storeManagerToken),
                200, "创建待删除商品API调用"
        );

        verifyApiSuccessResponse(createResult, "创建待删除商品响应验证");
        waitFor(200);

        // 获取新创建的商品ID
        Long newProductId = executeDatabaseOperation("获取最新商品ID", connection -> {
            try (var statement = connection.createStatement()) {
                var resultSet = statement.executeQuery(
                        "SELECT id FROM products WHERE store_id = " + testStoreId + " ORDER BY create_time DESC LIMIT 1"
                );
                return resultSet.next() ? resultSet.getLong("id") : null;
            }
        });

        assertNotNullWithDebug(newProductId, "新商品ID", "新商品创建验证");
        logInfo("获取到新商品ID: " + newProductId);

        // 删除商品
        logInfo("删除商品ID: " + newProductId);
        MvcResult deleteResult = executeRequest(
                authenticatedDelete("/api/products/" + newProductId, storeManagerToken),
                200, "删除商品API调用"
        );

        verifyApiSuccessResponse(deleteResult, "删除商品响应验证");
        waitFor(200);

        // 验证商品已被删除
        executeRequest(
                get("/api/products/" + newProductId),
                404, "访问已删除商品API调用（应返回404）"
        );

        logTestEnd("删除商品", true);
    }

    @Test
    @Order(8)
    @DisplayName("测试清理资源")
    @Commit
    void testCleanupResources() throws Exception {
        logTestStart("测试清理资源", "清理测试过程中创建的资源");

        try {
            logInfo("开始清理测试数据");

            if (testStoreId != null) {
                int productCount = countRecords("products", "store_id = " + testStoreId);
                int inventoryCount = countRecords("inventory", "product_id IN (SELECT id FROM products WHERE store_id = " + testStoreId + ")");

                logInfo("测试店铺 " + testStoreId + " 的最终统计:");
                logInfo("- 商品数量: " + productCount);
                logInfo("- 库存记录数量: " + inventoryCount);
            }

            logSuccess("测试资源清理完成");
        } catch (Exception e) {
            logWarning("资源清理过程中发生异常: " + e.getMessage());
        }

        logTestEnd("测试清理资源", true);
    }
}