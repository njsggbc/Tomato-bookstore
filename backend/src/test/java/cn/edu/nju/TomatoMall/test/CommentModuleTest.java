package cn.edu.nju.TomatoMall.test;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.var;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 评论模块集成测试
 * 测试用户评论、商家回复、评分更新、权限控制等功能
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("评论模块测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentModuleTest extends BaseIntegrationTest {

    private Long testStoreId;
    private Long testProductId;
    private String storeManagerToken;
    private String customerToken;
    private String anotherCustomerToken;
    private Long customerId;
    private Long commentId;
    private Long replyCommentId;
    private Long orderId;

    @Test
    @Order(1)
    @DisplayName("创建测试环境：店铺、商品、顾客、已完成订单")
    @Commit
    void testSetupTestEnvironment() throws Exception {
        logTestStart("创建测试环境", "创建店铺、商品、顾客和已完成订单以支持评论测试");

        checkPreconditions("创建测试环境",
                "userToken", userToken,
                "adminToken", adminToken);

        // 创建店铺
        Map<String, String> storeData = TestDataBuilder.createStoreParams(
                "评论测试书店", "北京市朝阳区评论街1号", "专业的图书销售和评论管理"
        );

        MockMultipartFile logoFile = new MockMultipartFile(
                "logo", "logo.jpg", "image/jpeg", "fake logo content".getBytes()
        );
        MockMultipartFile qualificationFile = new MockMultipartFile(
                "qualification", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        logInfo("创建测试店铺: " + storeData.get("name"));

        MvcResult createStoreResult = executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", storeData.get("name"))
                        .param("address", storeData.get("address"))
                        .param("description", storeData.get("description"))
                        .param("merchantAccounts.ALIPAY", "comment@bookstore.com")
                        .header("Authorization", "Bearer " + userToken),
                200, "创建测试店铺"
        );

        verifyApiSuccessResponse(createStoreResult, "创建测试店铺响应验证");
        waitFor(200);

        // 获取店铺ID并审核通过
        MvcResult listResult = executeRequest(
                authenticatedGet("/api/stores/awaiting-review", adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "id")
                        .param("order", "true"),
                200, "获取待审核店铺列表"
        );

        JsonNode listResponse = verifyApiSuccessResponse(listResult, "获取待审核店铺列表响应验证");
        JsonNode listData = extractDataFromResponse(listResponse, "获取待审核店铺列表");
        JsonNode pendingStores = verifyPageResponse(listData, "待审核店铺列表分页验证");

        testStoreId = pendingStores.get(0).get("id").asLong();
        logInfo("获取到测试店铺ID: " + testStoreId);

        // 管理员审核通过店铺
        MvcResult reviewResult = executeRequest(
                authenticatedPost("/api/stores/review", adminToken)
                        .param("storeId", testStoreId.toString())
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"审核通过\""),
                200, "审核店铺"
        );

        verifyApiSuccessResponse(reviewResult, "审核店铺响应验证");
        waitFor(200);

        storeManagerToken = userToken;

        // 创建商品
        logInfo("创建测试商品");
        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "java_book.jpg", "image/jpeg", "fake image content".getBytes()
        );

        MvcResult productResult = executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", "Java核心技术")
                        .param("description", "Java编程的经典教材，适合评论测试")
                        .param("price", "128.00")
                        .param("storeId", testStoreId.toString())
                        .param("specifications[author]", "Cay S. Horstmann")
                        .param("specifications[publisher]", "机械工业出版社")
                        .param("specifications[isbn]", "978-7-111-21382-7")
                        .param("specifications[pages]", "1200")
                        .header("Authorization", "Bearer " + storeManagerToken),
                200, "创建测试商品"
        );

        verifyApiSuccessResponse(productResult, "创建测试商品响应验证");
        waitFor(200);

        // 获取商品ID
        testProductId = executeDatabaseOperation("获取测试商品ID", connection -> {
            try (var statement = connection.createStatement()) {
                var resultSet = statement.executeQuery(
                        "SELECT id FROM products WHERE store_id = " + testStoreId + " ORDER BY create_time DESC LIMIT 1"
                );
                return resultSet.next() ? resultSet.getLong("id") : null;
            }
        });

        assertNotNullWithDebug(testProductId, "测试商品ID", "商品创建验证");
        logInfo("获取到测试商品ID: " + testProductId);

        // 设置库存
        Map<String, Integer> inventoryRequest = TestDataBuilder.createInventoryRequest(50);
        String requestBody = objectMapper.writeValueAsString(inventoryRequest);

        executeRequest(
                authenticatedPatch("/api/products/stockpile/" + testProductId, storeManagerToken)
                        .param("stockpile", "50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "设置商品库存"
        );

        // 创建顾客
        createTestCustomers();

        logTestEnd("创建测试环境", true);
    }

    /**
     * 创建测试顾客
     */
    private void createTestCustomers() throws Exception {
        logInfo("创建测试顾客");

        // 第一个顾客
        Map<String, Object> customerData = TestDataBuilder.createUserRequest(
                "reviewer001", "13988776655", "password123",
                "reviewer@test.com", "Book Reviewer", "Shanghai"
        );

        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", customerData.get("username").toString())
                        .param("phone", customerData.get("phone").toString())
                        .param("password", customerData.get("password").toString())
                        .param("email", customerData.get("email").toString())
                        .param("name", customerData.get("name").toString())
                        .param("location", customerData.get("location").toString()),
                200, "创建第一个顾客"
        );

        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest("reviewer001", "password123")
        );

        MvcResult loginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "第一个顾客登录"
        );

        JsonNode loginResponse = verifyApiSuccessResponse(loginResult, "第一个顾客登录响应验证");
        customerToken = extractDataFromResponse(loginResponse, "第一个顾客登录").asText();

        MvcResult customerInfoResult = executeRequest(
                authenticatedGet("/api/users", customerToken),
                200, "获取顾客信息"
        );

        JsonNode customerResponse = verifyApiSuccessResponse(customerInfoResult, "获取顾客信息响应验证");
        JsonNode customerData1 = extractDataFromResponse(customerResponse, "获取顾客信息");
        customerId = customerData1.get("id").asLong();
        logInfo("第一个顾客创建成功，ID: " + customerId);

        // 第二个顾客
        Map<String, Object> anotherCustomerData = TestDataBuilder.createUserRequest(
                "reviewer002", "13977665544", "password123",
                "reviewer2@test.com", "Another Reviewer", "Guangzhou"
        );

        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", anotherCustomerData.get("username").toString())
                        .param("phone", anotherCustomerData.get("phone").toString())
                        .param("password", anotherCustomerData.get("password").toString())
                        .param("email", anotherCustomerData.get("email").toString())
                        .param("name", anotherCustomerData.get("name").toString())
                        .param("location", anotherCustomerData.get("location").toString()),
                200, "创建第二个顾客"
        );

        String anotherLoginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest("reviewer002", "password123")
        );

        MvcResult anotherLoginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(anotherLoginRequest),
                200, "第二个顾客登录"
        );

        JsonNode anotherLoginResponse = verifyApiSuccessResponse(anotherLoginResult, "第二个顾客登录响应验证");
        anotherCustomerToken = extractDataFromResponse(anotherLoginResponse, "第二个顾客登录").asText();
        logInfo("第二个顾客创建成功");
    }

    /**
     * 创建已完成的订单
     */
    private void createCompletedOrder() throws Exception {
        logInfo("创建已完成订单以支持评论权限");

        // 添加到购物车
        executeRequest(
                authenticatedPost("/api/carts", customerToken)
                        .param("productId", testProductId.toString())
                        .param("quantity", "1"),
                200, "添加商品到购物车"
        );

        // 获取购物车项
        MvcResult cartResult = executeRequest(
                authenticatedGet("/api/carts", customerToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "timestamp")
                        .param("order", "false"),
                200, "获取购物车项"
        );

        JsonNode cartResponse = verifyApiSuccessResponse(cartResult, "获取购物车项响应验证");
        JsonNode cartData = extractDataFromResponse(cartResponse, "获取购物车项");
        JsonNode cartItems = verifyPageResponse(cartData, "购物车项分页验证");
        Long cartItemId = cartItems.get(0).get("id").asLong();

        // 提交订单
        List<Long> cartItemIds = Arrays.asList(cartItemId);
        Map<String, Object> submitRequest = TestDataBuilder.createSubmitOrderRequest(
                cartItemIds, "张三", "13912345678", TestDataBuilder.generateRandomAddress(), null
        );

        String requestBody = objectMapper.writeValueAsString(submitRequest);

        MvcResult submitResult = executeRequest(
                authenticatedPost("/api/orders", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "提交订单"
        );

        JsonNode submitResponse = verifyApiSuccessResponse(submitResult, "提交订单响应验证");
        JsonNode paymentInfo = extractDataFromResponse(submitResponse, "提交订单");

        long paymentId = paymentInfo.get("paymentId").asLong();
        String paymentNo = paymentInfo.get("paymentNo").asText();
        orderId = paymentInfo.get("orderList").get(0).get("orderId").asLong();

        // 模拟支付成功
        executeRequest(
                authenticatedPost("/api/payments/" + paymentId + "/pay", customerToken)
                        .param("paymentMethod", "ALIPAY"),
                200, "发起支付"
        );

        String callbackData = "out_trade_no=" + paymentNo + "&trade_status=TRADE_SUCCESS&total_amount=128.00";
        executeRequest(
                post("/api/alipay/notify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(callbackData),
                200, "支付回调"
        );

        waitFor(1000);

        // 商家确认并发货
        executeRequest(
                authenticatedPost("/api/orders/store/" + testStoreId + "/" + orderId + "/confirm", storeManagerToken),
                200, "商家确认订单"
        );

        Map<String, Object> shipRequest = TestDataBuilder.createShipRequest(
                TestDataBuilder.generateTrackingNumber(), "TEST_COMPANY",
                "评论测试书店", "13888888888", "北京市朝阳区评论街1号"
        );

        String shipRequestBody = objectMapper.writeValueAsString(shipRequest);

        executeRequest(
                authenticatedPost("/api/orders/store/" + testStoreId + "/" + orderId + "/ship", storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shipRequestBody),
                200, "商家发货"
        );

        // 模拟送达并确认收货
        String trackingNumber = shipRequest.get("trackingNo").toString();

        Map<String, Object> deliveryRequest = TestDataBuilder.createDeliveryConfirmRequest(
                TestDataBuilder.getCurrentTimeString(), "上海市收货地址", "张三", "13912345678", null
        );

        String deliveryRequestBody = objectMapper.writeValueAsString(deliveryRequest);

        executeRequest(
                post("/api/shipping/" + trackingNumber + "/confirm-delivery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deliveryRequestBody),
                200, "确认送达"
        );

        executeRequest(
                authenticatedPost("/api/orders/" + orderId + "/confirm", customerToken),
                200, "确认收货"
        );

        waitFor(1000);
        logSuccess("已完成订单创建成功，订单ID: " + orderId);
    }

    @Test
    @Order(2)
    @DisplayName("用户评论商品 - 成功案例")
    @Commit
    void testCreateProductComment_Success() throws Exception {
        logTestStart("用户评论商品", "测试用户对已购买商品进行评论");

        checkPreconditions("用户评论商品",
                "customerToken", customerToken,
                "testProductId", testProductId);

        Map<String, Object> commentRequest = TestDataBuilder.createCommentRequest(
                "这本书非常棒！内容详实，讲解清晰，推荐给所有Java学习者。", 8
        );

        String requestBody = objectMapper.writeValueAsString(commentRequest);
        logInfo("创建商品评论，评分: 8/10");

        MvcResult result = executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "创建商品评论"
        );

        verifyApiSuccessResponse(result, "创建商品评论响应验证");
        waitFor(200);

        // 验证评论记录
        verifyRecordCountWithDebug("comments",
                "entity_type = 'PRODUCT' AND entity_id = " + testProductId,
                1, "商品评论记录验证");

        // 验证商品评分更新
        MvcResult productResult = executeRequest(
                get("/api/products/" + testProductId),
                200, "获取商品信息验证评分"
        );

        JsonNode productResponse = verifyApiSuccessResponse(productResult, "获取商品信息响应验证");
        JsonNode productData = extractDataFromResponse(productResponse, "获取商品信息");

        assertTrueWithDebug(productData.get("rate").asDouble() > 0,
                "商品评分应该大于0", "商品评分更新验证");

        logTestEnd("用户评论商品", true);
    }

    @Test
    @Order(3)
    @DisplayName("获取商品评论列表")
    @Commit
    void testGetProductComments() throws Exception {
        logTestStart("获取商品评论列表", "测试获取商品的评论列表");

        checkPreconditions("获取商品评论列表",
                "testProductId", testProductId);

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                authenticatedGet("/api/comments/product/" + testProductId, customerToken)
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", "createTime")
                        .param("order", "false"),
                200, "获取商品评论列表"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取商品评论列表响应验证");
        JsonNode data = extractDataFromResponse(response, "获取商品评论列表");
        JsonNode comments = verifyPageResponse(data, "商品评论列表分页验证");

        assertEqualsWithDebug(1, comments.size(), "评论数量", "评论数量验证");

        JsonNode comment = comments.get(0);
        commentId = comment.get("id").asLong();

        assertEqualsWithDebug("PRODUCT", comment.get("entityType").asText(), "实体类型", "评论实体类型验证");
        assertEqualsWithDebug(testProductId.longValue(), comment.get("entityId").asLong(), "实体ID", "评论实体ID验证");
        assertContainsWithDebug(comment.get("content").asText(), "这本书非常棒", "评论内容", "评论内容验证");
        assertEqualsWithDebug(8, comment.get("rating").asInt(), "评分", "评论评分验证");
        assertEqualsWithDebug(0, comment.get("likes").asInt(), "点赞数", "评论点赞数验证");
        assertFalseWithDebug(comment.get("liked").asBoolean(), "是否点赞", "评论点赞状态验证");

        logInfo("获取到评论ID: " + commentId);
        logTestEnd("获取商品评论列表", true);
    }

    @Test
    @Order(4)
    @DisplayName("用户评论店铺 - 成功案例")
    @Commit
    void testCreateStoreComment_Success() throws Exception {
        logTestStart("用户评论店铺", "测试用户对店铺进行评论");

        checkPreconditions("用户评论店铺",
                "customerToken", customerToken,
                "testStoreId", testStoreId);

        Map<String, Object> storeCommentRequest = TestDataBuilder.createCommentRequest(
                "店铺服务很好，发货速度快，包装仔细，值得推荐！", 9
        );

        String requestBody = objectMapper.writeValueAsString(storeCommentRequest);
        logInfo("创建店铺评论，评分: 9/10");

        MvcResult result = executeRequest(
                authenticatedPost("/api/comments/store/" + testStoreId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "创建店铺评论"
        );

        verifyApiSuccessResponse(result, "创建店铺评论响应验证");
        waitFor(200);

        // 验证店铺评论记录
        verifyRecordCountWithDebug("comments",
                "entity_type = 'STORE' AND entity_id = " + testStoreId,
                1, "店铺评论记录验证");

        // 验证店铺评分更新
        MvcResult storeResult = executeRequest(
                authenticatedGet("/api/stores/" + testStoreId, userToken),
                200, "获取店铺信息验证评分"
        );

        JsonNode storeResponse = verifyApiSuccessResponse(storeResult, "获取店铺信息响应验证");
        JsonNode storeData = extractDataFromResponse(storeResponse, "获取店铺信息");

        assertTrueWithDebug(storeData.get("score").asDouble() > 0,
                "店铺评分应该大于0", "店铺评分更新验证");

        logTestEnd("用户评论店铺", true);
    }

    @Test
    @Order(5)
    @DisplayName("获取店铺评论列表")
    @Commit
    void testGetStoreComments() throws Exception {
        logTestStart("获取店铺评论列表", "测试获取店铺的评论列表");

        checkPreconditions("获取店铺评论列表",
                "testStoreId", testStoreId);

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                authenticatedGet("/api/comments/store/" + testStoreId, customerToken)
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", "createTime")
                        .param("order", "false"),
                200, "获取店铺评论列表"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取店铺评论列表响应验证");
        JsonNode data = extractDataFromResponse(response, "获取店铺评论列表");
        JsonNode comments = verifyPageResponse(data, "店铺评论列表分页验证");

        assertEqualsWithDebug(1, comments.size(), "店铺评论数量", "店铺评论数量验证");

        JsonNode comment = comments.get(0);
        assertEqualsWithDebug("STORE", comment.get("entityType").asText(), "实体类型", "店铺评论实体类型验证");
        assertEqualsWithDebug(testStoreId.longValue(), comment.get("entityId").asLong(), "实体ID", "店铺评论实体ID验证");
        assertContainsWithDebug(comment.get("content").asText(), "店铺服务很好", "评论内容", "店铺评论内容验证");
        assertEqualsWithDebug(9, comment.get("rating").asInt(), "评分", "店铺评论评分验证");

        logTestEnd("获取店铺评论列表", true);
    }

    @Test
    @Order(6)
    @DisplayName("商家回复评论")
    @Commit
    void testMerchantReplyToComment() throws Exception {
        logTestStart("商家回复评论", "测试商家对用户评论进行回复");

        checkPreconditions("商家回复评论",
                "storeManagerToken", storeManagerToken,
                "commentId", commentId);

        Map<String, Object> replyRequest = TestDataBuilder.createCommentRequest(
                "感谢您的好评！我们会继续努力提供更好的服务。", null
        );

        String requestBody = objectMapper.writeValueAsString(replyRequest);
        logInfo("商家回复评论ID: " + commentId);

        MvcResult result = executeRequest(
                authenticatedPost("/api/comments/" + commentId + "/reply", storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "商家回复评论"
        );

        verifyApiSuccessResponse(result, "商家回复评论响应验证");
        waitFor(200);

        // 验证回复记录
        verifyRecordCountWithDebug("comments", "parent_id = " + commentId, 1, "评论回复记录验证");

        logTestEnd("商家回复评论", true);
    }

    @Test
    @Order(7)
    @DisplayName("获取评论回复列表")
    @Commit
    void testGetCommentReplies() throws Exception {
        logTestStart("获取评论回复列表", "测试获取评论的回复列表");

        checkPreconditions("获取评论回复列表",
                "commentId", commentId);

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                authenticatedGet("/api/comments/" + commentId + "/reply", customerToken)
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", "createTime")
                        .param("order", "true"),
                200, "获取评论回复列表"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取评论回复列表响应验证");
        JsonNode data = extractDataFromResponse(response, "获取评论回复列表");
        JsonNode replies = verifyPageResponse(data, "评论回复列表分页验证");

        assertEqualsWithDebug(1, replies.size(), "回复数量", "回复数量验证");

        JsonNode reply = replies.get(0);
        replyCommentId = reply.get("id").asLong();

        assertEqualsWithDebug(commentId.longValue(), reply.get("parentId").asLong(), "父评论ID", "回复父评论验证");
        assertContainsWithDebug(reply.get("content").asText(), "感谢您的好评", "回复内容", "回复内容验证");
        assertTrueWithDebug(reply.get("rating") == null, "回复评分应为空", "回复评分验证");

        logInfo("获取到回复ID: " + replyCommentId);
        logTestEnd("获取评论回复列表", true);
    }

    @Test
    @Order(8)
    @DisplayName("用户点赞评论")
    @Commit
    void testLikeComment() throws Exception {
        logTestStart("用户点赞评论", "测试用户对评论进行点赞和取消点赞");

        checkPreconditions("用户点赞评论",
                "anotherCustomerToken", anotherCustomerToken,
                "commentId", commentId);

        // 第一次点赞
        logInfo("第一次点赞评论ID: " + commentId);
        MvcResult result = executeRequest(
                authenticatedPost("/api/comments/" + commentId + "/like", anotherCustomerToken),
                200, "点赞评论"
        );

        verifyApiSuccessResponse(result, "点赞评论响应验证");
        waitFor(200);

        // 验证点赞数量增加
        MvcResult commentResult = executeRequest(
                authenticatedGet("/api/comments/product/" + testProductId, anotherCustomerToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "createTime")
                        .param("order", "false"),
                200, "获取评论验证点赞"
        );

        JsonNode commentResponse = verifyApiSuccessResponse(commentResult, "获取评论验证点赞响应验证");
        JsonNode commentData = extractDataFromResponse(commentResponse, "获取评论验证点赞");
        JsonNode comments = verifyPageResponse(commentData, "评论列表分页验证");

        JsonNode likedComment = null;
        for (JsonNode comment : comments) {
            if (comment.get("id").asLong() == commentId) {
                likedComment = comment;
                break;
            }
        }

        assertNotNullWithDebug(likedComment, "被点赞的评论", "点赞评论查找验证");
        assertEqualsWithDebug(1, likedComment.get("likes").asInt(), "点赞数", "点赞数量验证");

        // 再次点赞应该取消点赞
        logInfo("再次点赞取消点赞");
        executeRequest(
                authenticatedPost("/api/comments/" + commentId + "/like", anotherCustomerToken),
                200, "取消点赞评论"
        );

        waitFor(200);

        // 验证点赞取消
        commentResult = executeRequest(
                authenticatedGet("/api/comments/product/" + testProductId, anotherCustomerToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "createTime")
                        .param("order", "false"),
                200, "获取评论验证取消点赞"
        );

        JsonNode cancelResponse = verifyApiSuccessResponse(commentResult, "获取评论验证取消点赞响应验证");
        JsonNode cancelData = extractDataFromResponse(cancelResponse, "获取评论验证取消点赞");
        JsonNode cancelComments = verifyPageResponse(cancelData, "取消点赞评论列表分页验证");

        for (JsonNode comment : cancelComments) {
            if (comment.get("id").asLong() == commentId) {
                assertEqualsWithDebug(0, comment.get("likes").asInt(), "取消点赞后点赞数", "取消点赞验证");
                break;
            }
        }

        logTestEnd("用户点赞评论", true);
    }

    @Test
    @Order(9)
    @DisplayName("更新评论内容")
    @Commit
    void testUpdateComment() throws Exception {
        logTestStart("更新评论内容", "测试用户修改自己的评论内容");

        checkPreconditions("更新评论内容",
                "customerToken", customerToken,
                "commentId", commentId);

        Map<String, Object> updateRequest = TestDataBuilder.createCommentUpdateRequest(
                "这本书非常棒！内容详实，讲解清晰，推荐给所有Java学习者。更新：读完后感觉更好了！", 9
        );

        String requestBody = objectMapper.writeValueAsString(updateRequest);
        logInfo("更新评论ID: " + commentId + ", 新评分: 9/10");

        MvcResult result = executeRequest(
                authenticatedPut("/api/comments/" + commentId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "更新评论内容"
        );

        verifyApiSuccessResponse(result, "更新评论内容响应验证");
        waitFor(200);

        // 验证评论更新
        MvcResult commentResult = executeRequest(
                authenticatedGet("/api/comments/product/" + testProductId, customerToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "createTime")
                        .param("order", "false"),
                200, "获取更新后评论"
        );

        JsonNode commentResponse = verifyApiSuccessResponse(commentResult, "获取更新后评论响应验证");
        JsonNode commentData = extractDataFromResponse(commentResponse, "获取更新后评论");
        JsonNode comments = verifyPageResponse(commentData, "更新后评论列表分页验证");

        JsonNode updatedComment = null;
        for (JsonNode comment : comments) {
            if (comment.get("id").asLong() == commentId) {
                updatedComment = comment;
                break;
            }
        }

        assertNotNullWithDebug(updatedComment, "更新的评论", "更新评论查找验证");
        assertContainsWithDebug(updatedComment.get("content").asText(), "更新：读完后感觉更好了",
                "更新后评论内容", "评论内容更新验证");
        assertEqualsWithDebug(9, updatedComment.get("rating").asInt(), "更新后评分", "评论评分更新验证");

        logTestEnd("更新评论内容", true);
    }

    @Test
    @Order(10)
    @DisplayName("添加更多评论测试评分计算")
    @Commit
    void testRatingCalculation() throws Exception {
        logTestStart("测试评分计算", "添加多个评论测试平均评分计算");

        checkPreconditions("测试评分计算",
                "anotherCustomerToken", anotherCustomerToken,
                "testProductId", testProductId);

        // 第二个用户添加评论
        Map<String, Object> comment2Request = TestDataBuilder.createCommentRequest(
                "书的质量不错，但内容有些过时。", 6
        );

        String requestBody = objectMapper.writeValueAsString(comment2Request);
        logInfo("第二个用户添加评论，评分: 6/10");

        MvcResult result = executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, anotherCustomerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "第二个用户评论商品"
        );

        verifyApiSuccessResponse(result, "第二个用户评论商品响应验证");
        waitFor(200);

        // 验证商品评分更新（应该是 (9+6)/2 = 7.5）
        MvcResult productResult = executeRequest(
                get("/api/products/" + testProductId),
                200, "获取商品验证评分计算"
        );

        JsonNode productResponse = verifyApiSuccessResponse(productResult, "获取商品验证评分计算响应验证");
        JsonNode productData = extractDataFromResponse(productResponse, "获取商品验证评分计算");

        double averageRating = productData.get("rate").asDouble();
        logInfo("当前商品平均评分: " + averageRating);
        assertTrueWithDebug(averageRating >= 7.0 && averageRating <= 8.0,
                "商品平均评分应在7.0-8.0之间", "平均评分计算验证");

        // 创建第三个用户并添加评论
        Map<String, Object> reviewer3Data = TestDataBuilder.createUserRequest(
                "reviewer003", "13966554433", "password123",
                "reviewer3@test.com", "Third Reviewer", "Shenzhen"
        );

        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", reviewer3Data.get("username").toString())
                        .param("phone", reviewer3Data.get("phone").toString())
                        .param("password", reviewer3Data.get("password").toString())
                        .param("email", reviewer3Data.get("email").toString())
                        .param("name", reviewer3Data.get("name").toString())
                        .param("location", reviewer3Data.get("location").toString()),
                200, "创建第三个评论用户"
        );

        String reviewer3LoginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest("reviewer003", "password123")
        );

        MvcResult reviewer3LoginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewer3LoginRequest),
                200, "第三个用户登录"
        );

        JsonNode loginResponse = verifyApiSuccessResponse(reviewer3LoginResult, "第三个用户登录响应验证");
        String reviewer3Token = extractDataFromResponse(loginResponse, "第三个用户登录").asText();

        // 第三个评论
        Map<String, Object> comment3Request = TestDataBuilder.createCommentRequest(
                "非常好的书，强烈推荐！", 10
        );

        requestBody = objectMapper.writeValueAsString(comment3Request);
        logInfo("第三个用户添加评论，评分: 10/10");

        executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, reviewer3Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "第三个用户评论商品"
        );

        waitFor(200);

        // 验证最终评分 ((9+6+10)/3 = 8.33)
        productResult = executeRequest(
                get("/api/products/" + testProductId),
                200, "获取商品验证最终评分"
        );

        JsonNode finalProductResponse = verifyApiSuccessResponse(productResult, "获取商品验证最终评分响应验证");
        JsonNode finalProductData = extractDataFromResponse(finalProductResponse, "获取商品验证最终评分");

        double finalRating = finalProductData.get("rate").asDouble();
        logInfo("最终商品平均评分: " + finalRating);
        assertTrueWithDebug(finalRating >= 8.0 && finalRating <= 9.0,
                "商品最终平均评分应在8.0-9.0之间", "最终平均评分验证");

        logTestEnd("测试评分计算", true);
    }

    @Test
    @Order(11)
    @DisplayName("测试评论权限控制 - 商家不能评论自己的商品")
    @Commit
    void testMerchantCannotCommentOwnProduct() throws Exception {
        logTestStart("测试商家评论权限", "测试商家不能评论自己店铺的商品");

        checkPreconditions("测试商家评论权限",
                "storeManagerToken", storeManagerToken,
                "testProductId", testProductId);

        Map<String, Object> merchantCommentRequest = TestDataBuilder.createCommentRequest(
                "我们的商品确实很好！", 10
        );

        String requestBody = objectMapper.writeValueAsString(merchantCommentRequest);
        logInfo("商家尝试评论自己的商品（应被拒绝）");

        executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                403, "商家评论自己商品（应被拒绝）"
        );

        logTestEnd("测试商家评论权限", true);
    }

    @Test
    @Order(12)
    @DisplayName("测试评论权限控制 - 商家不能评论自己的店铺")
    @Commit
    void testMerchantCannotCommentOwnStore() throws Exception {
        logTestStart("测试商家店铺评论权限", "测试商家不能评论自己的店铺");

        checkPreconditions("测试商家店铺评论权限",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId);

        Map<String, Object> merchantStoreCommentRequest = TestDataBuilder.createCommentRequest(
                "我们店铺服务确实很棒！", 10
        );

        String requestBody = objectMapper.writeValueAsString(merchantStoreCommentRequest);
        logInfo("商家尝试评论自己的店铺（应被拒绝）");

        executeRequest(
                authenticatedPost("/api/comments/store/" + testStoreId, storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                403, "商家评论自己店铺（应被拒绝）"
        );

        logTestEnd("测试商家店铺评论权限", true);
    }

    @Test
    @Order(13)
    @DisplayName("测试回复评论的回复")
    @Commit
    void testReplyToReply() throws Exception {
        logTestStart("测试回复评论的回复", "测试对回复进行二级回复");

        checkPreconditions("测试回复评论的回复",
                "customerToken", customerToken,
                "replyCommentId", replyCommentId);

        // 用户回复商家的回复
        Map<String, Object> replyToReplyRequest = TestDataBuilder.createCommentRequest(
                "谢谢商家的回复，期待继续改进！", null
        );

        String requestBody = objectMapper.writeValueAsString(replyToReplyRequest);
        logInfo("用户回复商家的回复ID: " + replyCommentId);

        MvcResult result = executeRequest(
                authenticatedPost("/api/comments/" + replyCommentId + "/reply", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "回复商家回复"
        );

        verifyApiSuccessResponse(result, "回复商家回复响应验证");
        waitFor(200);

        // 验证二级回复
        verifyRecordCountWithDebug("comments", "parent_id = " + replyCommentId, 1, "二级回复记录验证");

        logTestEnd("测试回复评论的回复", true);
    }

    @Test
    @Order(14)
    @DisplayName("测试用户只能修改自己的评论")
    @Commit
    void testUserCanOnlyUpdateOwnComment() throws Exception {
        logTestStart("测试评论修改权限", "测试用户只能修改自己的评论");

        checkPreconditions("测试评论修改权限",
                "anotherCustomerToken", anotherCustomerToken,
                "commentId", commentId);

        Map<String, Object> updateRequest = TestDataBuilder.createCommentUpdateRequest(
                "恶意修改他人评论", 1
        );

        String requestBody = objectMapper.writeValueAsString(updateRequest);
        logInfo("其他用户尝试修改他人评论（应被拒绝）");

        // 其他用户尝试修改评论应该被拒绝
        executeRequest(
                authenticatedPut("/api/comments/" + commentId, anotherCustomerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                403, "其他用户修改评论（应被拒绝）"
        );

        logTestEnd("测试评论修改权限", true);
    }

    @Test
    @Order(15)
    @DisplayName("测试用户只能删除自己的评论")
    @Commit
    void testUserCanOnlyDeleteOwnComment() throws Exception {
        logTestStart("测试评论删除权限", "测试用户只能删除自己的评论");

        checkPreconditions("测试评论删除权限",
                "anotherCustomerToken", anotherCustomerToken,
                "customerToken", customerToken,
                "commentId", commentId);

        // 其他用户尝试删除评论应该被拒绝
        logInfo("其他用户尝试删除他人评论（应被拒绝）");
        executeRequest(
                authenticatedDelete("/api/comments/" + commentId, anotherCustomerToken),
                403, "其他用户删除评论（应被拒绝）"
        );

        // 评论作者可以删除自己的评论
        logInfo("评论作者删除自己的评论");
        MvcResult result = executeRequest(
                authenticatedDelete("/api/comments/" + commentId, customerToken),
                200, "评论作者删除自己评论"
        );

        verifyApiSuccessResponse(result, "评论作者删除自己评论响应验证");
        waitFor(200);

        // 验证评论已删除
        verifyRecordCountWithDebug("comments", "id = " + commentId, 0, "评论删除验证");

        // 验证商品评分重新计算（删除9分评论后，剩余6分和10分，平均8分）
        waitFor(1000); // 等待评分重新计算

        MvcResult productResult = executeRequest(
                get("/api/products/" + testProductId),
                200, "获取商品验证删除后评分"
        );

        JsonNode productResponse = verifyApiSuccessResponse(productResult, "获取商品验证删除后评分响应验证");
        JsonNode productData = extractDataFromResponse(productResponse, "获取商品验证删除后评分");

        double recalculatedRating = productData.get("rate").asDouble();
        logInfo("删除评论后重新计算的评分: " + recalculatedRating);
        assertTrueWithDebug(recalculatedRating >= 7.0 && recalculatedRating <= 9.0,
                "删除评论后商品评分应重新计算", "删除后评分重新计算验证");

        logTestEnd("测试评论删除权限", true);
    }

    @Test
    @Order(16)
    @DisplayName("测试评论参数验证")
    @Commit
    void testCommentValidation() throws Exception {
        logTestStart("测试评论参数验证", "测试各种无效参数的验证");

        checkPreconditions("测试评论参数验证",
                "customerToken", customerToken,
                "testProductId", testProductId);

        // 测试空评论内容
        logInfo("测试空评论内容验证");
        Map<String, Object> emptyContentRequest = TestDataBuilder.createCommentRequest("", 5);
        String requestBody = objectMapper.writeValueAsString(emptyContentRequest);

        executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                400, "空评论内容测试（应失败）"
        );

        // 测试无效评分（超出范围）
        logInfo("测试无效评分验证");
        Map<String, Object> invalidRatingRequest = TestDataBuilder.createCommentRequest("测试评论", 11);
        requestBody = objectMapper.writeValueAsString(invalidRatingRequest);

        executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                400, "无效评分测试（应失败）"
        );

        // 测试负评分
        logInfo("测试负评分验证");
        Map<String, Object> negativeRatingRequest = TestDataBuilder.createCommentRequest("测试评论", 0);
        requestBody = objectMapper.writeValueAsString(negativeRatingRequest);

        executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                400, "负评分测试（应失败）"
        );

        // 测试过长评论
        logInfo("测试长评论验证");
        String longContent = "这是一条非常长的评论，" + new String(new char[5000]).replace("\0", "a");
        Map<String, Object> longContentRequest = TestDataBuilder.createCommentRequest(longContent, 5);
        requestBody = objectMapper.writeValueAsString(longContentRequest);

        executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "长评论测试"
        );

        logTestEnd("测试评论参数验证", true);
    }

    @Test
    @Order(19)
    @DisplayName("测试管理员删除不当评论")
    @Commit
    void testAdminDeleteInappropriateComment() throws Exception {
        logTestStart("测试管理员删除不当评论", "测试管理员权限删除不当评论");

        checkPreconditions("测试管理员删除不当评论",
                "customerToken", customerToken,
                "adminToken", adminToken,
                "testProductId", testProductId);

        // 设上一个测试的长评论位不当评论，获取评论ID
        MvcResult commentsResult = executeRequest(
                authenticatedGet("/api/comments/product/" + testProductId, adminToken)
                        .param("page", "0")
                        .param("size", "20")
                        .param("field", "createTime")
                        .param("order", "false"),
                200, "获取评论列表查找不当评论"
        );

        JsonNode commentsResponse = verifyApiSuccessResponse(commentsResult, "获取评论列表查找不当评论响应验证");
        JsonNode commentsData = extractDataFromResponse(commentsResponse, "获取评论列表查找不当评论");
        JsonNode comments = verifyPageResponse(commentsData, "查找不当评论列表分页验证");

        Long inappropriateCommentId = null;
        for (JsonNode comment : comments) {
            if (comment.get("content").asText().contains("aaaaaaa")) {
                inappropriateCommentId = comment.get("id").asLong();
                break;
            }
        }

        assertNotNullWithDebug(inappropriateCommentId, "不当评论ID", "不当评论查找验证");
        logInfo("找到不当评论ID: " + inappropriateCommentId);

        // 管理员删除评论
        logInfo("管理员删除不当评论");
        MvcResult deleteResult = executeRequest(
                authenticatedDelete("/api/comments/" + inappropriateCommentId, adminToken),
                200, "管理员删除不当评论"
        );

        verifyApiSuccessResponse(deleteResult, "管理员删除不当评论响应验证");
        waitFor(200);

        // 验证评论已被删除
        verifyRecordCountWithDebug("comments", "id = " + inappropriateCommentId, 0, "不当评论删除验证");

        logTestEnd("测试管理员删除不当评论", true);
    }

    @Test
    @Order(20)
    @DisplayName("测试重复评论限制")
    @Commit
    void testDuplicateCommentRestriction() throws Exception {
        logTestStart("测试重复评论限制", "测试用户不能对同一实体重复评论");

        checkPreconditions("测试重复评论限制",
                "anotherCustomerToken", customerToken,
                "testProductId", testProductId);

        // 第一次评论应该成功
        Map<String, Object> firstCommentRequest = TestDataBuilder.createCommentRequest(
                "第一次评论，应该成功", 7
        );

        String requestBody = objectMapper.writeValueAsString(firstCommentRequest);
        logInfo("用户第一次评论商品");

        executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "用户第一次评论商品"
        );

        waitFor(200);

        // 第二次评论应该失败
        Map<String, Object> secondCommentRequest = TestDataBuilder.createCommentRequest(
                "第二次评论，应该失败", 8
        );

        requestBody = objectMapper.writeValueAsString(secondCommentRequest);
        logInfo("用户尝试第二次评论同一商品（应被拒绝）");

        executeRequest(
                authenticatedPost("/api/comments/product/" + testProductId, customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                403, "用户重复评论商品（应被拒绝）"
        );

        logTestEnd("测试重复评论限制", true);
    }

    @Test
    @Order(24)
    @DisplayName("测试清理资源")
    @Commit
    void testCleanupResources() throws Exception {
        logTestStart("测试清理资源", "清理测试过程中创建的资源并输出统计信息");

        try {
            logInfo("开始清理测试数据并输出统计信息");

            if (testStoreId != null && testProductId != null) {
                // 统计评论相关数据
                int productCommentCount = countRecords("comments",
                        "entity_type = 'PRODUCT' AND entity_id = " + testProductId);
                int storeCommentCount = countRecords("comments",
                        "entity_type = 'STORE' AND entity_id = " + testStoreId);
                int replyCount = countRecords("comments", "parent_id IS NOT NULL");
                int totalCommentCount = countRecords("comments", "1=1");
                int totalLikeCount = executeDatabaseOperation("统计点赞总数", connection -> {
                    try (var statement = connection.createStatement()) {
                        var resultSet = statement.executeQuery(
                                "SELECT COUNT(*) FROM comment_likes"
                        );
                        return resultSet.next() ? resultSet.getInt(1) : 0;
                    }
                });

                logInfo("评论模块测试统计信息:");
                logInfo("- 测试店铺ID: " + testStoreId);
                logInfo("- 测试商品ID: " + testProductId);
                logInfo("- 商品评论数量: " + productCommentCount);
                logInfo("- 店铺评论数量: " + storeCommentCount);
                logInfo("- 回复评论数量: " + replyCount);
                logInfo("- 评论总数量: " + totalCommentCount);
                logInfo("- 点赞总数量: " + totalLikeCount);

                // 获取最终评分
                MvcResult productResult = mockMvc.perform(authenticatedGet("/api/products/" + testProductId, userToken)).andReturn();
                if (productResult.getResponse().getStatus() == 200) {
                    String productContent = getResponseContent(productResult);
                    JsonNode productResponse = objectMapper.readTree(productContent);
                    if (productResponse.get("code").asInt() == 0) {
                        JsonNode productData = productResponse.get("data");
                        logInfo("- 商品最终评分: " + productData.get("rate").asDouble());
                    }
                }

                MvcResult storeResult = mockMvc.perform(authenticatedGet("/api/stores/" + testStoreId, userToken)).andReturn();
                if (storeResult.getResponse().getStatus() == 200) {
                    String storeContent = getResponseContent(storeResult);
                    JsonNode storeResponse = objectMapper.readTree(storeContent);
                    if (storeResponse.get("code").asInt() == 0) {
                        JsonNode storeData = storeResponse.get("data");
                        logInfo("- 店铺最终评分: " + storeData.get("score").asDouble());
                    }
                }
            }

            logSuccess("评论模块测试数据统计完成");
        } catch (Exception e) {
            logWarning("资源清理统计过程中发生异常: " + e.getMessage());
        }

        logTestEnd("测试清理资源", true);
    }
}