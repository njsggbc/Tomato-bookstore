package cn.edu.nju.TomatoMall.test;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.var;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 购物模块集成测试
 * 专注于核心购物业务流程：购物车 -> 订单 -> 支付 -> 商家处理 -> 用户确认
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("购物模块核心业务测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ShoppingModuleTest extends BaseIntegrationTest {

    // 测试数据
    private Long testStoreId;
    private Long testProductId;
    private String storeManagerToken;
    private String customerToken;
    private Long customerId;

    // 购物车相关
    private Long cartItemId;

    // 订单相关
    private Long orderId;
    private String orderNo;
    private String trackingNumber;

    // 支付相关
    private Long paymentId;
    private String paymentNo;

    // 商品信息
    private final String PRODUCT_TITLE = "Spring Boot实战指南";
    private final BigDecimal PRODUCT_PRICE = new BigDecimal("88.88");
    private final int INITIAL_INVENTORY = 100;

    @Override
    protected void setupTestData() throws Exception {
        super.setupTestData();
        setupStoreAndProduct();
        setupCustomer();
    }

    /**
     * 设置测试店铺和商品
     */
    private void setupStoreAndProduct() throws Exception {
        logInfo("=== 开始设置测试店铺和商品 ===");

        // 1. 创建店铺
        testStoreId = createTestStore("图书商城", "北京市朝阳区书店街1号", "专业图书销售商城");
        storeManagerToken = userToken;
        logSuccess("测试店铺创建完成，ID: " + testStoreId);

        // 2. 创建商品
        testProductId = createTestProduct();
        logSuccess("测试商品创建完成，ID: " + testProductId + ", 标题: " + PRODUCT_TITLE);

        // 3. 设置商品库存
        setProductInventory(testProductId, INITIAL_INVENTORY);
        logSuccess("商品库存设置完成，数量: " + INITIAL_INVENTORY);

        logInfo("=== 店铺和商品设置完成 ===");
    }

    /**
     * 设置测试顾客
     */
    private void setupCustomer() throws Exception {
        logInfo("=== 开始设置测试顾客 ===");

        String customerUsername = "customer" + generateUniqueId();
        String customerPhone = generateUniquePhone();
        String customerEmail = generateUniqueEmail();

        // 1. 创建顾客账户
        MvcResult createResult = executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", customerUsername)
                        .param("phone", customerPhone)
                        .param("password", "password123")
                        .param("email", customerEmail)
                        .param("name", "Test Customer")
                        .param("location", "Shanghai"),
                200, "创建测试顾客"
        );
        verifyApiSuccessResponse(createResult, "创建测试顾客");

        // 2. 顾客登录
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(customerUsername, "password123")
        );

        MvcResult loginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "测试顾客登录"
        );

        JsonNode loginResponse = verifyApiSuccessResponse(loginResult, "测试顾客登录");
        customerToken = extractDataFromResponse(loginResponse, "测试顾客登录").asText();

        // 3. 获取顾客ID
        MvcResult userInfoResult = executeRequest(
                authenticatedGet("/api/users", customerToken),
                200, "获取顾客用户信息"
        );

        JsonNode userResponse = verifyApiSuccessResponse(userInfoResult, "获取顾客用户信息");
        JsonNode userData = extractDataFromResponse(userResponse, "获取顾客用户信息");
        customerId = userData.get("id").asLong();

        logSuccess("测试顾客设置完成，ID: " + customerId + ", 用户名: " + customerUsername);
        logInfo("=== 顾客设置完成 ===");
    }

    // ==================== 购物车相关测试 ====================

    @Test
    @Order(1)
    @DisplayName("1. 添加商品到购物车")
    @Commit
    void testAddToCart() throws Exception {
        logTestStart("添加商品到购物车", "测试用户将商品添加到购物车的核心功能");

        checkPreconditions("添加商品到购物车",
                "customerToken", customerToken,
                "testProductId", testProductId);

        int quantity = 2;
        logInfo("添加商品到购物车 - 商品ID: " + testProductId + ", 数量: " + quantity);

        MvcResult result = executeRequest(
                authenticatedPost("/api/carts", customerToken)
                        .param("productId", testProductId.toString())
                        .param("quantity", String.valueOf(quantity)),
                200, "添加商品到购物车API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "添加商品到购物车");
        cartItemId = extractDataFromResponse(response, "添加商品到购物车").asLong();

        assertNotNullWithDebug(cartItemId, "购物车项ID", "添加商品到购物车");
        logSuccess("商品已成功添加到购物车，购物车项ID: " + cartItemId);

        // 验证数据库记录
        verifyRecordCountWithDebug("cart_items",
                "id = " + cartItemId,
                1, "购物车数据库记录验证");

        logTestEnd("添加商品到购物车", true);
    }

    @Test
    @Order(2)
    @DisplayName("2. 查看购物车内容")
    @Commit
    void testViewCart() throws Exception {
        logTestStart("查看购物车内容", "验证用户可以正确查看购物车中的商品");

        checkPreconditions("查看购物车内容",
                "customerToken", customerToken,
                "cartItemId", cartItemId);

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                authenticatedGet("/api/carts", customerToken)
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", pageParams.get("field"))
                        .param("order", pageParams.get("order")),
                200, "获取购物车列表API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取购物车列表");
        JsonNode data = extractDataFromResponse(response, "获取购物车列表");
        JsonNode cartItems = verifyPageResponse(data, "购物车列表分页验证");

        assertListNotEmptyWithDebug(cartItems, "购物车商品列表", "购物车内容验证");

        // 验证购物车项详情
        JsonNode cartItem = cartItems.get(0);
        assertEqualsWithDebug(cartItemId.longValue(), cartItem.get("id").asLong(), "购物车项ID", "购物车内容验证");
        assertEqualsWithDebug(2, cartItem.get("quantity").asInt(), "商品数量", "购物车内容验证");

        JsonNode product = cartItem.get("product");
        assertEqualsWithDebug(testProductId.longValue(), product.get("id").asLong(), "商品ID", "购物车内容验证");
        assertEqualsWithDebug(PRODUCT_TITLE, product.get("title").asText(), "商品标题", "购物车内容验证");
        assertEqualsWithDebug(PRODUCT_PRICE.doubleValue(), product.get("price").asDouble(), "商品价格", "购物车内容验证");

        logSuccess("购物车内容验证通过");
        logTestEnd("查看购物车内容", true);
    }

    @Test
    @Order(3)
    @DisplayName("3. 修改购物车商品数量")
    @Commit
    void testUpdateCartQuantity() throws Exception {
        logTestStart("修改购物车商品数量", "测试用户修改购物车中商品数量的功能");

        checkPreconditions("修改购物车商品数量",
                "customerToken", customerToken,
                "cartItemId", cartItemId);

        int newQuantity = 5;
        logInfo("修改购物车商品数量 - 购物车项ID: " + cartItemId + ", 新数量: " + newQuantity);

        MvcResult result = executeRequest(
                authenticatedPatch("/api/carts/" + cartItemId, customerToken)
                        .param("quantity", String.valueOf(newQuantity)),
                200, "修改购物车商品数量API"
        );

        verifyApiSuccessResponse(result, "修改购物车商品数量");

        // 验证数量更新 - 重新获取购物车
        MvcResult cartResult = executeRequest(
                authenticatedGet("/api/carts", customerToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "timestamp")
                        .param("order", "false"),
                200, "验证数量更新"
        );

        JsonNode cartResponse = verifyApiSuccessResponse(cartResult, "验证数量更新");
        JsonNode cartData = extractDataFromResponse(cartResponse, "验证数量更新");
        JsonNode cartItems = cartData.get("content");

        assertEqualsWithDebug(newQuantity, cartItems.get(0).get("quantity").asInt(),
                "更新后的商品数量", "修改购物车商品数量验证");

        logSuccess("购物车商品数量修改成功，新数量: " + newQuantity);
        logTestEnd("修改购物车商品数量", true);
    }

    // ==================== 订单相关测试 ====================

    @Test
    @Order(4)
    @DisplayName("4. 购物车结算检查")
    @Commit
    void testCartCheckout() throws Exception {
        logTestStart("购物车结算检查", "验证购物车中的商品是否可以正常结算");

        checkPreconditions("购物车结算检查",
                "customerToken", customerToken,
                "cartItemId", cartItemId);

        List<Long> cartItemIds = Arrays.asList(cartItemId);
        String checkoutRequest = objectMapper.writeValueAsString(cartItemIds);

        logInfo("进行购物车结算检查 - 购物车项: " + cartItemIds);

        MvcResult result = executeRequest(
                authenticatedPost("/api/carts/checkout", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutRequest),
                200, "购物车结算检查API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "购物车结算检查");
        JsonNode checkoutResults = extractDataFromResponse(response, "购物车结算检查");

        assertEqualsWithDebug(1, checkoutResults.size(), "结算结果数量", "结算检查验证");

        JsonNode checkoutResult = checkoutResults.get(0);
        assertEqualsWithDebug(cartItemId.longValue(), checkoutResult.get("cartItemId").asLong(),
                "购物车项ID", "结算检查验证");
        assertTrueWithDebug(checkoutResult.get("available").asBoolean(),
                "商品可用性", "结算检查验证");

        logSuccess("购物车结算检查通过，商品可用");
        logTestEnd("购物车结算检查", true);
    }

    @Test
    @Order(5)
    @DisplayName("5. 提交订单")
    @Commit
    void testSubmitOrder() throws Exception {
        logTestStart("提交订单", "测试用户从购物车提交订单的完整流程");

        checkPreconditions("提交订单",
                "customerToken", customerToken,
                "cartItemId", cartItemId,
                "testStoreId", testStoreId);

        // 构建订单提交请求
        List<Long> cartItemIds = Arrays.asList(cartItemId);
        Map<String, String> storeRemarks = new HashMap<>();
        storeRemarks.put(testStoreId.toString(), "请尽快发货，谢谢！");

        Map<String, Object> submitRequest = TestDataBuilder.createSubmitOrderRequest(
                cartItemIds,
                "张三",
                "13912345678",
                "上海市浦东新区陆家嘴环路1000号",
                storeRemarks
        );

        String requestBody = objectMapper.writeValueAsString(submitRequest);

        logInfo("提交订单 - 购物车项: " + cartItemIds.size() + "个");

        MvcResult result = executeRequest(
                authenticatedPost("/api/orders", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "提交订单API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "提交订单");
        JsonNode paymentInfo = extractDataFromResponse(response, "提交订单");

        // 提取支付和订单信息
        paymentId = paymentInfo.get("paymentId").asLong();

        JsonNode orderList = paymentInfo.get("orderList");
        assertEqualsWithDebug(1, orderList.size(), "订单数量", "提交订单验证");

        JsonNode order = orderList.get(0);
        orderId = order.get("orderId").asLong();
        orderNo = order.get("orderNo").asText();

        // 验证订单状态和金额
        assertEqualsWithDebug("AWAITING_PAYMENT", order.get("status").asText(),
                "订单状态", "提交订单验证");

        BigDecimal expectedAmount = PRODUCT_PRICE.multiply(new BigDecimal("5")); // 5个商品
        assertEqualsWithDebug(expectedAmount.doubleValue(), paymentInfo.get("totalAmount").asDouble(),
                "支付总金额", "提交订单验证");

        logSuccess("订单提交成功 - 订单ID: " + orderId + ", 支付ID: " + paymentId + ", 总金额: ¥" + expectedAmount);

        // 验证购物车已清空
        verifyRecordCountWithDebug("order_items",
                "id = " + cartItemId + " AND order_id IS NULL",
                0, "购物车清空验证");

        // 验证订单记录创建
        verifyRecordCountWithDebug("orders",
                "id = " + orderId,
                1, "订单记录创建验证");

        // 验证库存锁定
        int lockedInventory = getLockedInventoryQuantity(testProductId);
        assertEqualsWithDebug(5, lockedInventory, "锁定库存数量", "库存锁定验证");

        logTestEnd("提交订单", true);
    }

    // ==================== 支付相关测试 ====================

    @Test
    @Order(6)
    @DisplayName("6. 查询待支付订单")
    @Commit
    void testGetPendingPayments() throws Exception {
        logTestStart("查询待支付订单", "验证用户可以查询自己的待支付订单");

        checkPreconditions("查询待支付订单",
                "customerToken", customerToken,
                "paymentId", paymentId);

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                authenticatedGet("/api/payments/pending", customerToken)
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", pageParams.get("field"))
                        .param("order", pageParams.get("order")),
                200, "查询待支付订单API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "查询待支付订单");
        JsonNode data = extractDataFromResponse(response, "查询待支付订单");
        JsonNode pendingPayments = verifyPageResponse(data, "待支付订单分页验证");

        assertListNotEmptyWithDebug(pendingPayments, "待支付订单列表", "待支付订单验证");

        JsonNode payment = pendingPayments.get(0);
        assertEqualsWithDebug(paymentId.longValue(), payment.get("paymentId").asLong(),
                "支付ID", "待支付订单验证");

        logSuccess("待支付订单查询成功，找到 " + pendingPayments.size() + " 个待支付订单");
        logTestEnd("查询待支付订单", true);
    }

    @Test
    @Order(7)
    @DisplayName("7. 发起支付")
    @Commit
    void testInitiatePayment() throws Exception {
        logTestStart("发起支付", "测试用户发起支付宝支付的流程");

        checkPreconditions("发起支付",
                "customerToken", customerToken,
                "paymentId", paymentId);

        logInfo("发起支付 - 支付ID: " + paymentId + ", 支付方式: ALIPAY");

        MvcResult result = executeRequest(
                authenticatedPost("/api/payments/" + paymentId + "/pay", customerToken)
                        .param("paymentMethod", "ALIPAY"),
                200, "发起支付API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "发起支付");
        String paymentUrl = extractDataFromResponse(response, "发起支付").asText();

        assertNotNullWithDebug(paymentUrl, "支付URL", "发起支付验证");
        assertTrueWithDebug(paymentUrl.startsWith("http"),
                "支付URL格式正确", "发起支付验证");

        logSuccess("支付发起成功，支付URL: " + paymentUrl.substring(0, Math.min(50, paymentUrl.length())) + "...");

        MvcResult paymentInfo = executeRequest(
                authenticatedGet("/api/payments/info", customerToken)
                        .param("paymentId", String.valueOf(paymentId)),
                200, "获取支付信息API"
        );

        paymentNo = verifyApiSuccessResponse(paymentInfo, "获取支付信息").get("data").get("paymentNo").asText();

        logTestEnd("发起支付", true);
    }

    @Test
    @Order(8)
    @DisplayName("8. 模拟支付成功")
    @Commit
    void testPaymentSuccess() throws Exception {
        logTestStart("模拟支付成功", "模拟支付宝支付成功回调，验证订单状态更新");

        checkPreconditions("模拟支付成功",
                "paymentNo", paymentNo,
                "orderId", orderId);

        // 计算总金额
        BigDecimal totalAmount = PRODUCT_PRICE.multiply(new BigDecimal("5"));

        // 模拟支付宝回调
        String callbackData = "out_trade_no=" + paymentNo +
                "&trade_status=TRADE_SUCCESS" +
                "&total_amount=" + totalAmount;

        logInfo("模拟支付成功回调 - 支付单号: " + paymentNo + ", 金额: ¥" + totalAmount);

        MvcResult result = executeRequest(
                post("/api/alipay/notify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(callbackData),
                200, "支付成功回调API"
        );

        // 等待异步处理
        waitFor(1000);

        // 验证订单状态更新
        MvcResult orderResult = executeRequest(
                authenticatedGet("/api/orders/" + orderId, customerToken),
                200, "验证订单状态更新"
        );

        JsonNode orderResponse = verifyApiSuccessResponse(orderResult, "验证订单状态更新");
        JsonNode orderData = extractDataFromResponse(orderResponse, "验证订单状态更新");

        assertEqualsWithDebug("PROCESSING", orderData.get("status").asText(),
                "支付后订单状态", "支付成功验证");

        logSuccess("支付成功处理完成 - 订单状态: PROCESSING");
        logTestEnd("模拟支付成功", true);
    }

    // ==================== 商家处理相关测试 ====================

    @Test
    @Order(9)
    @DisplayName("9. 商家确认订单")
    @Commit
    void testMerchantConfirmOrder() throws Exception {
        logTestStart("商家确认订单", "测试商家确认用户订单的流程");

        checkPreconditions("商家确认订单",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId,
                "orderId", orderId);

        logInfo("商家确认订单 - 店铺ID: " + testStoreId + ", 订单ID: " + orderId);

        MvcResult result = executeRequest(
                authenticatedPost("/api/orders/store/" + testStoreId + "/" + orderId + "/confirm", storeManagerToken),
                200, "商家确认订单API"
        );

        verifyApiSuccessResponse(result, "商家确认订单");

        // 验证订单状态更新
        MvcResult orderResult = executeRequest(
                authenticatedGet("/api/orders/" + orderId, customerToken),
                200, "验证订单状态更新"
        );

        JsonNode orderResponse = verifyApiSuccessResponse(orderResult, "验证订单状态更新");
        JsonNode orderData = extractDataFromResponse(orderResponse, "验证订单状态更新");

        assertEqualsWithDebug("AWAITING_SHIPMENT", orderData.get("status").asText(),
                "确认后订单状态", "商家确认订单验证");

        logSuccess("商家确认订单成功 - 订单状态: AWAITING_SHIPMENT");
        logTestEnd("商家确认订单", true);
    }

    @Test
    @Order(10)
    @DisplayName("10. 商家发货")
    @Commit
    void testMerchantShipOrder() throws Exception {
        logTestStart("商家发货", "测试商家发货并提供物流信息的流程");

        checkPreconditions("商家发货",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId,
                "orderId", orderId);

        trackingNumber = TestDataBuilder.generateTrackingNumber();

        Map<String, Object> shipRequest = TestDataBuilder.createShipRequest(
                trackingNumber,
                "TEST_COMPANY",
                "图书商城",
                "13888888888",
                "北京市朝阳区书店街1号"
        );

        String requestBody = objectMapper.writeValueAsString(shipRequest);

        logInfo("商家发货 - 物流单号: " + trackingNumber);

        MvcResult result = executeRequest(
                authenticatedPost("/api/orders/store/" + testStoreId + "/" + orderId + "/ship", storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "商家发货API"
        );

        verifyApiSuccessResponse(result, "商家发货");

        // 验证订单状态更新
        MvcResult orderResult = executeRequest(
                authenticatedGet("/api/orders/" + orderId, customerToken),
                200, "验证发货后订单状态"
        );

        JsonNode orderResponse = verifyApiSuccessResponse(orderResult, "验证发货后订单状态");
        JsonNode orderData = extractDataFromResponse(orderResponse, "验证发货后订单状态");

        assertEqualsWithDebug("IN_TRANSIT", orderData.get("status").asText(),
                "发货后订单状态", "商家发货验证");

        // 验证库存扣减
        int currentInventory = getInventoryQuantity(testProductId) - getLockedInventoryQuantity(testProductId);
        int expectedInventory = INITIAL_INVENTORY - 5; // 扣减5个
        assertEqualsWithDebug(expectedInventory, currentInventory,
                "支付后库存数量", "库存扣减验证");

        // 验证锁定库存释放
        int lockedInventory = getLockedInventoryQuantity(testProductId);
        assertEqualsWithDebug(0, lockedInventory,
                "锁定库存数量", "锁定库存释放验证");

        // 验证物流信息（可选验证）
        if (orderData.has("shippingInfo") && !orderData.get("shippingInfo").isEmpty()) {
            JsonNode shippingInfo = orderData.get("shippingInfo").get(0);
            assertEqualsWithDebug(trackingNumber, shippingInfo.get("trackingNumber").asText(),
                    "物流单号", "物流信息验证");
            logInfo("物流信息验证通过 - 单号: " + trackingNumber);
        }

        logSuccess("商家发货成功 - 订单状态: IN_TRANSIT, 物流单号: " + trackingNumber);
        logTestEnd("商家发货", true);
    }

    // ==================== 用户确认相关测试 ====================

    @Test
    @Order(11)
    @DisplayName("11. 模拟物流送达")
    @Commit
    void testDeliveryComplete() throws Exception {
        logTestStart("模拟物流送达", "模拟物流公司确认送达，订单状态变为待收货");

        checkPreconditions("模拟物流送达",
                "orderId", orderId);

        logInfo("模拟物流送达 - 订单ID: " + orderId);

        Map<String, Object> deliveryRequest = new HashMap<>();
        deliveryRequest.put("deliveryTime", LocalDateTime.now().toString());
        deliveryRequest.put("deliveryLocation", "北京市朝阳区书店街1号");
        deliveryRequest.put("signedBy", "张三");
        deliveryRequest.put("phone", "13888888888");
        deliveryRequest.put("remark", "物流送达无异常");

        String requestBody = objectMapper.writeValueAsString(deliveryRequest);

        MvcResult result = executeRequest(
                post("/api/shipping/" + trackingNumber + "/confirm-delivery")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON),
                200, "模拟物流送达API"
        );

        MvcResult orderResult = executeRequest(
                authenticatedGet("/api/orders/" + orderId, customerToken),
                200, "验证订单送达状态"
        );

        JsonNode orderResponse = verifyApiSuccessResponse(orderResult, "验证订单送达状态");
        JsonNode orderData = extractDataFromResponse(orderResponse, "验证订单送达状态");

        assertEqualsWithDebug("AWAITING_RECEIPT", orderData.get("status").asText(),
                "送达后订单状态", "模拟物流送达验证");

        logInfo("模拟物流送达完成，订单进入待收货状态");

        logSuccess("物流送达模拟完成");
        logTestEnd("模拟物流送达", true);
    }

    @Test
    @Order(12)
    @DisplayName("12. 用户确认收货")
    @Commit
    void testCustomerConfirmReceipt() throws Exception {
        logTestStart("用户确认收货", "测试用户确认收货，完成整个购物流程");

        checkPreconditions("用户确认收货",
                "customerToken", customerToken,
                "orderId", orderId);

        logInfo("用户确认收货 - 订单ID: " + orderId);

        MvcResult result = executeRequest(
                authenticatedPost("/api/orders/" + orderId + "/confirm", customerToken),
                200, "用户确认收货API"
        );

        verifyApiSuccessResponse(result, "用户确认收货");

        // 验证订单最终状态
        MvcResult orderResult = executeRequest(
                authenticatedGet("/api/orders/" + orderId, customerToken),
                200, "验证订单最终状态"
        );

        JsonNode orderResponse = verifyApiSuccessResponse(orderResult, "验证订单最终状态");
        JsonNode orderData = extractDataFromResponse(orderResponse, "验证订单最终状态");

        assertEqualsWithDebug("COMPLETED", orderData.get("status").asText(),
                "确认收货后订单状态", "用户确认收货验证");

        // 验证商品销量增加
        MvcResult productResult = executeRequest(
                get("/api/products/" + testProductId),
                200, "验证商品销量更新"
        );

        JsonNode productResponse = verifyApiSuccessResponse(productResult, "验证商品销量更新");
        JsonNode productData = extractDataFromResponse(productResponse, "验证商品销量更新");

        assertEqualsWithDebug(5, productData.get("sales").asInt(),
                "商品销量", "商品销量更新验证");

        logSuccess("用户确认收货成功 - 订单状态: COMPLETED, 商品销量增加: 5");
        logTestEnd("用户确认收货", true);
    }

    // ==================== 订单查询相关测试 ====================

    @Test
    @Order(13)
    @DisplayName("13. 用户查询订单列表")
    @Commit
    void testGetUserOrders() throws Exception {
        logTestStart("用户查询订单列表", "测试用户查询自己的订单列表");

        checkPreconditions("用户查询订单列表",
                "customerToken", customerToken,
                "orderId", orderId);

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                authenticatedGet("/api/orders", customerToken)
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", pageParams.get("field"))
                        .param("order", pageParams.get("order")),
                200, "用户查询订单列表API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "用户查询订单列表");
        JsonNode data = extractDataFromResponse(response, "用户查询订单列表");
        JsonNode orders = verifyPageResponse(data, "订单列表分页验证");

        assertListNotEmptyWithDebug(orders, "用户订单列表", "用户订单列表验证");

        JsonNode order = orders.get(0);
        assertEqualsWithDebug(orderId.longValue(), order.get("orderId").asLong(),
                "订单ID", "用户订单列表验证");
        assertEqualsWithDebug("COMPLETED", order.get("status").asText(),
                "订单状态", "用户订单列表验证");

        logSuccess("用户订单列表查询成功，找到 " + orders.size() + " 个订单");
        logTestEnd("用户查询订单列表", true);
    }

    @Test
    @Order(14)
    @DisplayName("14. 商家查询订单列表")
    @Commit
    void testGetStoreOrders() throws Exception {
        logTestStart("商家查询订单列表", "测试商家查询自己店铺的订单列表");

        checkPreconditions("商家查询订单列表",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId,
                "orderId", orderId);

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                authenticatedGet("/api/store/" + testStoreId + "/orders", storeManagerToken)
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", pageParams.get("field"))
                        .param("order", pageParams.get("order")),
                200, "商家查询订单列表API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "商家查询订单列表");
        JsonNode data = extractDataFromResponse(response, "商家查询订单列表");
        JsonNode orders = verifyPageResponse(data, "店铺订单列表分页验证");

        assertListNotEmptyWithDebug(orders, "店铺订单列表", "店铺订单列表验证");

        JsonNode order = orders.get(0);
        assertEqualsWithDebug(orderId.longValue(), order.get("orderId").asLong(),
                "订单ID", "店铺订单列表验证");

        logSuccess("店铺订单列表查询成功，找到 " + orders.size() + " 个订单");
        logTestEnd("商家查询订单列表", true);
    }

    @Test
    @Order(15)
    @DisplayName("15. 测试订单状态过滤查询")
    @Commit
    void testOrderStatusFilter() throws Exception {
        logTestStart("测试订单状态过滤查询", "测试按不同状态过滤查询订单的功能");

        checkPreconditions("测试订单状态过滤查询",
                "customerToken", customerToken);

        // 1. 查询已完成订单
        MvcResult completedResult = executeRequest(
                authenticatedGet("/api/orders", customerToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "createTime")
                        .param("order", "false")
                        .param("status", "COMPLETED"),
                200, "查询已完成订单API"
        );

        JsonNode completedResponse = verifyApiSuccessResponse(completedResult, "查询已完成订单");
        JsonNode completedData = extractDataFromResponse(completedResponse, "查询已完成订单");
        JsonNode completedOrders = completedData.get("content");

        for (JsonNode order : completedOrders) {
            assertEqualsWithDebug("COMPLETED", order.get("status").asText(),
                    "订单状态", "已完成订单过滤验证");
        }

        logInfo("已完成订单查询验证通过，找到 " + completedOrders.size() + " 个已完成订单");

        logSuccess("订单状态过滤查询测试完成");
        logTestEnd("测试订单状态过滤查询", true);
    }

    @Test
    @Order(19)
    @DisplayName("19. 购物流程总结验证")
    @Commit
    void testShoppingFlowSummary() throws Exception {
        logTestStart("购物流程总结验证", "验证整个购物流程的最终状态和数据一致性");
        logInfo("=== 购物流程总结验证 ===");

        // 验证最终库存状态
        int finalInventory = getInventoryQuantity(testProductId);
        logInfo("商品最终库存: " + finalInventory);

        // 验证商品销量
        MvcResult productResult = executeRequest(
                get("/api/products/" + testProductId),
                200, "获取商品最终信息"
        );

        JsonNode productResponse = verifyApiSuccessResponse(productResult, "获取商品最终信息");
        JsonNode productData = extractDataFromResponse(productResponse, "获取商品最终信息");

        int finalSales = productData.get("sales").asInt();
        logInfo("商品最终销量: " + finalSales);

        // 验证用户订单数量
        MvcResult userOrdersResult = executeRequest(
                authenticatedGet("/api/orders", customerToken)
                        .param("page", "0")
                        .param("size", "50")
                        .param("field", "createTime")
                        .param("order", "false"),
                200, "获取用户所有订单"
        );

        JsonNode userOrdersResponse = verifyApiSuccessResponse(userOrdersResult, "获取用户所有订单");
        JsonNode userOrdersData = extractDataFromResponse(userOrdersResponse, "获取用户所有订单");
        JsonNode userOrders = userOrdersData.get("content");

        int completedOrdersCount = 0;
        int cancelledOrdersCount = 0;

        for (JsonNode order : userOrders) {
            String status = order.get("status").asText();
            if ("COMPLETED".equals(status)) {
                completedOrdersCount++;
            } else if ("CANCELLED".equals(status)) {
                cancelledOrdersCount++;
            }
        }

        logInfo("用户订单统计:");
        logInfo("- 总订单数: " + userOrders.size());
        logInfo("- 已完成订单: " + completedOrdersCount);
        logInfo("- 已取消订单: " + cancelledOrdersCount);

        // 验证店铺订单数量
        MvcResult storeOrdersResult = executeRequest(
                authenticatedGet("/api/store/" + testStoreId + "/orders", storeManagerToken)
                        .param("page", "0")
                        .param("size", "50")
                        .param("field", "createTime")
                        .param("order", "false"),
                200, "获取店铺所有订单"
        );

        JsonNode storeOrdersResponse = verifyApiSuccessResponse(storeOrdersResult, "获取店铺所有订单");
        JsonNode storeOrdersData = extractDataFromResponse(storeOrdersResponse, "获取店铺所有订单");
        JsonNode storeOrders = storeOrdersData.get("content");

        logInfo("店铺订单数量: " + storeOrders.size());

        // 最终验证
        assertTrueWithDebug(userOrders.size() > 0, "用户应该有订单记录", "流程总结验证");
        assertTrueWithDebug(storeOrders.size() > 0, "店铺应该有订单记录", "流程总结验证");
        assertTrueWithDebug(finalSales > 0, "商品应该有销量", "流程总结验证");

        logInfo("=== 购物流程总结验证完成 ===");
        logSuccess("购物模块核心业务流程测试全部通过！");
        logTestEnd("购物流程总结验证", true);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试店铺
     */
    private Long createTestStore(String name, String address, String description) throws Exception {
        MockMultipartFile logoFile = new MockMultipartFile(
                "logo", "logo.jpg", "image/jpeg", "fake logo content".getBytes()
        );
        MockMultipartFile qualificationFile = new MockMultipartFile(
                "qualification", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        MvcResult createResult = executeRequest(
                multipart("/api/stores")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", name)
                        .param("address", address)
                        .param("description", description)
                        .param("merchantAccounts.ALIPAY", "test_merchant@alipay.com")
                        .header("Authorization", "Bearer " + userToken),
                200, "创建测试店铺"
        );

        verifyApiSuccessResponse(createResult, "创建测试店铺");

        // 获取店铺ID并审核通过
        MvcResult listResult = executeRequest(
                authenticatedGet("/api/stores/awaiting-review", adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "id")
                        .param("order", "true"),
                200, "获取待审核店铺列表"
        );

        JsonNode listResponse = verifyApiSuccessResponse(listResult, "获取待审核店铺列表");
        JsonNode listData = extractDataFromResponse(listResponse, "获取待审核店铺列表");
        JsonNode pendingStores = listData.get("content");

        Long storeId = pendingStores.get(0).get("id").asLong();

        // 管理员审核通过
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

    /**
     * 创建测试商品
     */
    private Long createTestProduct() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "spring_book.jpg", "image/jpeg", "fake image content".getBytes()
        );

        MvcResult result = executeRequest(
                multipart("/api/products")
                        .file(imageFile)
                        .param("title", PRODUCT_TITLE)
                        .param("description", "详细介绍Spring Boot框架的实战教程")
                        .param("price", PRODUCT_PRICE.toString())
                        .param("storeId", testStoreId.toString())
                        .param("specifications[author]", "Spring专家")
                        .param("specifications[publisher]", "技术出版社")
                        .param("specifications[isbn]", "978-7-111-12345-6")
                        .param("specifications[pages]", "500")
                        .header("Authorization", "Bearer " + storeManagerToken),
                200, "创建测试商品"
        );

        verifyApiSuccessResponse(result, "创建测试商品");

        // 获取商品ID
        return getLatestProductId();
    }

    /**
     * 设置商品库存
     */
    private void setProductInventory(Long productId, int quantity) throws Exception {
        Map<String, Integer> inventoryRequest = TestDataBuilder.createInventoryRequest(quantity);
        String requestBody = objectMapper.writeValueAsString(inventoryRequest);

        executeRequest(
                authenticatedPatch("/api/products/stockpile/" + productId, storeManagerToken)
                        .param("stockpile", String.valueOf(quantity)),
                200, "设置商品库存"
        );
    }

    /**
     * 获取商品库存数量
     */
    private int getInventoryQuantity(Long productId) throws Exception {
        return executeDatabaseOperation("获取商品库存", connection -> {
            try (var statement = connection.prepareStatement("SELECT quantity FROM inventories WHERE product_id = ?")) {
                statement.setLong(1, productId);
                var resultSet = statement.executeQuery();
                return resultSet.next() ? resultSet.getInt("quantity") : 0;
            }
        });
    }

    /**
     * 获取锁定库存数量
     */
    private int getLockedInventoryQuantity(Long productId) throws Exception {
        return executeDatabaseOperation("获取锁定库存", connection -> {
            try (var statement = connection.prepareStatement("SELECT locked_quantity FROM inventories WHERE product_id = ?")) {
                statement.setLong(1, productId);
                var resultSet = statement.executeQuery();
                return resultSet.next() ? resultSet.getInt("locked_quantity") : 0;
            }
        });
    }

    /**
     * 获取最新创建的商品ID
     */
    private Long getLatestProductId() throws Exception {
        return executeDatabaseOperation("获取最新商品ID", connection -> {
            try (var statement = connection.createStatement()) {
                var resultSet = statement.executeQuery(
                        "SELECT id FROM products WHERE store_id = " + testStoreId + " ORDER BY create_time DESC LIMIT 1"
                );
                return resultSet.next() ? resultSet.getLong("id") : null;
            }
        });
    }
}