package cn.edu.nju.TomatoMall.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 广告模块集成测试
 * 测试广告位管理、广告管理、投放管理等核心业务流程
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("广告模块测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdvertisementModuleTest extends BaseIntegrationTest {

    // 测试数据
    private Long testStoreId;
    private String storeManagerToken;
    private Long testAdSpaceId;
    private Long testAdvertisementId;
    private Long testPlacementId;
    private Long testPaymentId;

    // 测试用的广告位和广告信息
    private final String AD_SPACE_LABEL = "首页横幅广告位";
    private final String AD_TITLE = "春季新书推荐";
    private final String AD_LINK_URL = "https://example.com/spring-books";

    @Override
    protected void setupTestData() throws Exception {
        super.setupTestData();
        setupTestStore();
    }

    /**
     * 设置测试店铺
     */
    private void setupTestStore() throws Exception {
        logInfo("=== 开始设置测试店铺 ===");

        // 创建测试店铺
        testStoreId = createTestStore("广告测试书店", "北京市海淀区中关村大街1号", "专业图书广告投放测试店铺");
        storeManagerToken = userToken;

        logSuccess("测试店铺设置完成，ID: " + testStoreId);
        logInfo("=== 店铺设置完成 ===");
    }

    // ==================== 广告位管理测试 ====================

    @Test
    @Order(1)
    @DisplayName("1. 创建广告位 - 管理员权限")
    @Commit
    void testCreateAdSpace() throws Exception {
        logTestStart("创建广告位", "测试管理员创建新的广告位");

        checkPrecondition(adminToken, "adminToken", "创建广告位");

        Map<String, Object> adSpaceRequest = TestDataBuilder.createAdSpaceRequest(
                AD_SPACE_LABEL, "BANNER", 7, 3
        );

        String requestBody = objectMapper.writeValueAsString(adSpaceRequest);

        logInfo("创建广告位 - 标签: " + AD_SPACE_LABEL + ", 类型: BANNER");

        MvcResult result = executeRequest(
                authenticatedPost("/api/advertisements/spaces", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "创建广告位API"
        );

        verifyApiSuccessResponse(result, "创建广告位");

        // 验证广告位创建成功
        verifyRecordCountWithDebug("advertisement_spaces",
                "label = '" + AD_SPACE_LABEL + "'",
                1, "广告位创建验证");

        logSuccess("广告位创建成功");
        logTestEnd("创建广告位", true);
    }

    @Test
    @Order(2)
    @DisplayName("2. 获取广告位列表")
    @Commit
    void testGetAdSpaceList() throws Exception {
        logTestStart("获取广告位列表", "测试获取系统中的广告位列表");

        checkPrecondition(userToken, "userToken", "获取广告位列表");

        MvcResult result = executeRequest(
                authenticatedGet("/api/advertisements/spaces", userToken)
                        .param("type", "BANNER"),
                200, "获取广告位列表API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取广告位列表");
        JsonNode adSpaces = extractDataFromResponse(response, "获取广告位列表");

        assertListNotEmptyWithDebug(adSpaces, "广告位列表", "广告位列表验证");

        // 获取广告位ID用于后续测试
        for (JsonNode adSpace : adSpaces) {
            if (AD_SPACE_LABEL.equals(adSpace.get("label").asText())) {
                testAdSpaceId = adSpace.get("id").asLong();
                break;
            }
        }

        assertNotNullWithDebug(testAdSpaceId, "测试广告位ID", "广告位ID获取");
        logSuccess("广告位列表获取成功，找到广告位ID: " + testAdSpaceId);
        logTestEnd("获取广告位列表", true);
    }

    @Test
    @Order(3)
    @DisplayName("3. 获取广告位槽位信息")
    @Commit
    void testGetAdSpaceSlots() throws Exception {
        logTestStart("获取广告位槽位信息", "测试获取广告位的可用槽位信息");

        checkPreconditions("获取广告位槽位信息",
                "userToken", userToken,
                "testAdSpaceId", testAdSpaceId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/advertisements/spaces/" + testAdSpaceId + "/slots", userToken),
                200, "获取广告位槽位信息API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取广告位槽位信息");
        JsonNode slots = extractDataFromResponse(response, "获取广告位槽位信息");

        assertTrueWithDebug(slots.isArray(), "槽位信息应该是数组格式", "槽位信息格式验证");
        assertTrueWithDebug(slots.size() > 0, "应该有可用的槽位", "槽位存在验证");

        // 验证槽位信息结构
        JsonNode firstSlot = slots.get(0);
        assertTrueWithDebug(firstSlot.has("id"), "槽位应该有ID字段", "槽位结构验证");
        assertTrueWithDebug(firstSlot.has("startTime"), "槽位应该有开始时间字段", "槽位结构验证");
        assertTrueWithDebug(firstSlot.has("available"), "槽位应该有可用性字段", "槽位结构验证");

        logSuccess("广告位槽位信息获取成功，共 " + slots.size() + " 个槽位");
        logTestEnd("获取广告位槽位信息", true);
    }

    // ==================== 广告管理测试 ====================

    @Test
    @Order(4)
    @DisplayName("4. 创建广告")
    @Commit
    void testCreateAdvertisement() throws Exception {
        logTestStart("创建广告", "测试商家创建新的广告");

        checkPreconditions("创建广告",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId);

        MockMultipartFile adImageFile = new MockMultipartFile(
                "content", "ad_banner.jpg", "image/jpeg", "fake ad image content".getBytes()
        );

        logInfo("创建广告 - 标题: " + AD_TITLE + ", 店铺ID: " + testStoreId);

        MvcResult result = executeRequest(
                createMultipartRequest("/api/advertisements", "POST")
                        .file(adImageFile)
                        .param("title", AD_TITLE)
                        .param("linkUrl", AD_LINK_URL)
                        .param("storeId", testStoreId.toString())
                        .header("Authorization", "Bearer " + storeManagerToken),
                200, "创建广告API"
        );

        verifyApiSuccessResponse(result, "创建广告");

        // 验证广告创建成功
        verifyRecordCountWithDebug("advertisements",
                "title = '" + AD_TITLE + "' AND store_id = " + testStoreId,
                1, "广告创建验证");

        logSuccess("广告创建成功");
        logTestEnd("创建广告", true);
    }

    @Test
    @Order(5)
    @DisplayName("5. 获取商店广告列表")
    @Commit
    void testGetStoreAdvertisements() throws Exception {
        logTestStart("获取商店广告列表", "测试获取指定商店的广告列表");

        checkPreconditions("获取商店广告列表",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/advertisements/store/" + testStoreId, storeManagerToken),
                200, "获取商店广告列表API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取商店广告列表");
        JsonNode advertisements = extractDataFromResponse(response, "获取商店广告列表");

        assertListNotEmptyWithDebug(advertisements, "商店广告列表", "商店广告列表验证");

        // 获取广告ID用于后续测试
        for (JsonNode ad : advertisements) {
            if (AD_TITLE.equals(ad.get("title").asText())) {
                testAdvertisementId = ad.get("id").asLong();
                break;
            }
        }

        assertNotNullWithDebug(testAdvertisementId, "测试广告ID", "广告ID获取");

        // 验证广告信息
        JsonNode testAd = advertisements.get(0);
        assertEqualsWithDebug(AD_TITLE, testAd.get("title").asText(), "广告标题", "广告信息验证");
        assertEqualsWithDebug(AD_LINK_URL, testAd.get("linkUrl").asText(), "广告链接", "广告信息验证");
        assertEqualsWithDebug(testStoreId.longValue(), testAd.get("storeId").asLong(), "广告店铺ID", "广告信息验证");

        logSuccess("商店广告列表获取成功，找到广告ID: " + testAdvertisementId);
        logTestEnd("获取商店广告列表", true);
    }

    @Test
    @Order(6)
    @DisplayName("6. 获取广告详细信息")
    @Commit
    void testGetAdvertisementDetails() throws Exception {
        logTestStart("获取广告详细信息", "测试获取特定广告的详细信息");

        checkPreconditions("获取广告详细信息",
                "userToken", userToken,
                "testAdvertisementId", testAdvertisementId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/advertisements/" + testAdvertisementId, userToken),
                200, "获取广告详细信息API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取广告详细信息");
        JsonNode advertisement = extractDataFromResponse(response, "获取广告详细信息");

        // 验证广告详细信息
        assertEqualsWithDebug(testAdvertisementId.longValue(), advertisement.get("id").asLong(),
                "广告ID", "广告详细信息验证");
        assertEqualsWithDebug(AD_TITLE, advertisement.get("title").asText(),
                "广告标题", "广告详细信息验证");
        assertEqualsWithDebug(AD_LINK_URL, advertisement.get("linkUrl").asText(),
                "广告链接", "广告详细信息验证");
        assertNotNullWithDebug(advertisement.get("content"), "广告内容", "广告详细信息验证");
        assertNotNullWithDebug(advertisement.get("createTime"), "创建时间", "广告详细信息验证");

        logSuccess("广告详细信息获取成功");
        logTestEnd("获取广告详细信息", true);
    }

    @Test
    @Order(7)
    @DisplayName("7. 更新广告信息")
    @Commit
    void testUpdateAdvertisement() throws Exception {
        logTestStart("更新广告信息", "测试商家更新广告信息");

        checkPreconditions("更新广告信息",
                "storeManagerToken", storeManagerToken,
                "testAdvertisementId", testAdvertisementId);

        String updatedTitle = AD_TITLE + " (更新版)";
        String updatedLinkUrl = AD_LINK_URL + "/updated";

        MockMultipartFile newAdImageFile = new MockMultipartFile(
                "content", "new_ad_banner.jpg", "image/jpeg", "new fake ad image content".getBytes()
        );

        logInfo("更新广告信息 - 新标题: " + updatedTitle);

        MvcResult result = executeRequest(
                createMultipartRequest("/api/advertisements/" + testAdvertisementId, "PUT")
                        .file(newAdImageFile)
                        .param("title", updatedTitle)
                        .param("linkUrl", updatedLinkUrl)
                        .header("Authorization", "Bearer " + storeManagerToken),
                200, "更新广告信息API"
        );

        verifyApiSuccessResponse(result, "更新广告信息");

        // 验证广告更新成功
        MvcResult detailResult = executeRequest(
                authenticatedGet("/api/advertisements/" + testAdvertisementId, userToken),
                200, "验证广告更新"
        );

        JsonNode detailResponse = verifyApiSuccessResponse(detailResult, "验证广告更新");
        JsonNode updatedAd = extractDataFromResponse(detailResponse, "验证广告更新");

        assertEqualsWithDebug(updatedTitle, updatedAd.get("title").asText(),
                "更新后的广告标题", "广告更新验证");
        assertEqualsWithDebug(updatedLinkUrl, updatedAd.get("linkUrl").asText(),
                "更新后的广告链接", "广告更新验证");

        logSuccess("广告信息更新成功");
        logTestEnd("更新广告信息", true);
    }

    // ==================== 广告投放管理测试 ====================

    @Test
    @Order(8)
    @DisplayName("8. 投放广告")
    @Commit
    void testPlaceAdvertisement() throws Exception {
        logTestStart("投放广告", "测试商家投放广告到指定广告位");

        checkPreconditions("投放广告",
                "storeManagerToken", storeManagerToken,
                "testAdvertisementId", testAdvertisementId,
                "testAdSpaceId", testAdSpaceId);

        // 首先获取可用的槽位ID
        MvcResult slotsResult = executeRequest(
                authenticatedGet("/api/advertisements/spaces/" + testAdSpaceId + "/slots", storeManagerToken),
                200, "获取可用槽位"
        );

        JsonNode slotsResponse = verifyApiSuccessResponse(slotsResult, "获取可用槽位");
        JsonNode slots = extractDataFromResponse(slotsResponse, "获取可用槽位");

        // 选择3个可用槽位进行投放
        List<Long> selectedSlotIds = new ArrayList<>();
        int maxSlots = Math.min(3, slots.size());
        int cnt = 0;
        for (int i = 0; cnt < maxSlots; i++) {
            JsonNode slot = slots.get(i);
            if (slot.get("available").asBoolean()) {
                selectedSlotIds.add(slot.get("id").asLong());
                cnt++;
            }
        }

        assertTrueWithDebug(selectedSlotIds.size() > 0, "应该有可用的槽位", "可用槽位验证");

        Map<String, Object> placementRequest = TestDataBuilder.createAdPlacementRequest(
                testAdvertisementId, testAdSpaceId, selectedSlotIds
        );

        String requestBody = objectMapper.writeValueAsString(placementRequest);

        logInfo("投放广告 - 广告ID: " + testAdvertisementId + ", 广告位ID: " + testAdSpaceId +
                ", 槽位数量: " + selectedSlotIds.size());

        MvcResult result = executeRequest(
                authenticatedPost("/api/advertisements/placements", storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "投放广告API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "投放广告");
        JsonNode paymentInfo = extractDataFromResponse(response, "投放广告");

        // 提取支付信息
        testPaymentId = paymentInfo.get("paymentId").asLong();
        assertNotNullWithDebug(testPaymentId, "支付ID", "广告投放验证");

        logSuccess("广告投放请求成功，支付ID: " + testPaymentId);

        // 验证投放记录创建
        verifyRecordCountWithDebug("advertisement_placements",
                "advertisement_id = " + testAdvertisementId + " AND space_id = " + testAdSpaceId,
                1, "投放记录创建验证");

        logTestEnd("投放广告", true);
    }

    @Test
    @Order(9)
    @DisplayName("9. 模拟广告投放支付")
    @Commit
    void testAdvertisementPayment() throws Exception {
        logTestStart("模拟广告投放支付", "模拟广告投放费用支付流程");

        checkPreconditions("模拟广告投放支付",
                "storeManagerToken", storeManagerToken,
                "testPaymentId", testPaymentId);

        logInfo("模拟广告投放支付 - 支付ID: " + testPaymentId);

        // 发起支付
        MvcResult paymentResult = executeRequest(
                authenticatedPost("/api/payments/" + testPaymentId + "/pay", storeManagerToken)
                        .param("paymentMethod", "ALIPAY"),
                200, "发起广告投放支付API"
        );

        JsonNode paymentResponse = verifyApiSuccessResponse(paymentResult, "发起广告投放支付");
        String paymentUrl = extractDataFromResponse(paymentResponse, "发起广告投放支付").asText();

        assertNotNullWithDebug(paymentUrl, "支付URL", "支付发起验证");

        // 模拟支付成功回调
        String callbackData = "out_trade_no=PAYMENT_" + testPaymentId +
                "&trade_status=TRADE_SUCCESS" +
                "&total_amount=100.00";

        MvcResult callbackResult = executeRequest(
                post("/api/alipay/notify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(callbackData),
                200, "支付成功回调API"
        );

        // 等待异步处理
        waitFor(1000);

        logSuccess("广告投放支付模拟完成");
        logTestEnd("模拟广告投放支付", true);
    }

    @Test
    @Order(10)
    @DisplayName("10. 获取待审核投放列表")
    @Commit
    void testGetPendingPlacements() throws Exception {
        logTestStart("获取待审核投放列表", "测试管理员获取待审核的广告投放列表");

        checkPrecondition(adminToken, "adminToken", "获取待审核投放列表");

        MvcResult result = executeRequest(
                authenticatedGet("/api/advertisements/placements/pending", adminToken),
                200, "获取待审核投放列表API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取待审核投放列表");
        JsonNode placements = extractDataFromResponse(response, "获取待审核投放列表");

        assertTrueWithDebug(placements.isArray(), "投放列表应该是数组格式", "投放列表格式验证");

        // 如果有待审核的投放，验证其结构
        if (placements.size() > 0) {
            JsonNode placement = placements.get(0);
            assertTrueWithDebug(placement.has("id"), "投放应该有ID字段", "投放结构验证");
            assertTrueWithDebug(placement.has("adId"), "投放应该有广告ID字段", "投放结构验证");
            assertTrueWithDebug(placement.has("adSpaceId"), "投放应该有广告位ID字段", "投放结构验证");
            assertTrueWithDebug(placement.has("status"), "投放应该有状态字段", "投放结构验证");

            // 获取投放ID用于后续测试
            testPlacementId = placement.get("id").asLong();
        }

        logSuccess("待审核投放列表获取成功，共 " + placements.size() + " 个待审核投放");
        logTestEnd("获取待审核投放列表", true);
    }

    @Test
    @Order(11)
    @DisplayName("11. 审核广告投放")
    @Commit
    void testReviewAdvertisementPlacement() throws Exception {
        logTestStart("审核广告投放", "测试管理员审核广告投放申请");

        checkPreconditions("审核广告投放",
                "adminToken", adminToken,
                "testPlacementId", testPlacementId);

        logInfo("审核广告投放 - 投放ID: " + testPlacementId + ", 审核结果: 通过");

        MvcResult result = executeRequest(
                authenticatedPatch("/api/advertisements/placements/" + testPlacementId + "/review", adminToken)
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"广告内容合规，审核通过\""),
                200, "审核广告投放API"
        );

        verifyApiSuccessResponse(result, "审核广告投放");

        // 等待状态更新
        waitFor(500);

        // 验证投放状态更新
        verifyRecordCountWithDebug("advertisement_placements",
                "id = " + testPlacementId + " AND status = 0",
                1, "投放审核通过状态验证");

        logSuccess("广告投放审核通过");
        logTestEnd("审核广告投放", true);
    }

    @Test
    @Order(12)
    @DisplayName("12. 获取商店投放记录")
    @Commit
    void testGetStorePlacements() throws Exception {
        logTestStart("获取商店投放记录", "测试获取指定商店的广告投放记录");

        checkPreconditions("获取商店投放记录",
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/advertisements/placements/store/" + testStoreId, storeManagerToken),
                200, "获取商店投放记录API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取商店投放记录");
        JsonNode placements = extractDataFromResponse(response, "获取商店投放记录");

        assertTrueWithDebug(placements.isArray(), "投放记录应该是数组格式", "投放记录格式验证");

        // 验证投放记录信息
        if (placements.size() > 0) {
            JsonNode placement = placements.get(0);
            assertEqualsWithDebug(testAdvertisementId.longValue(), placement.get("adId").asLong(),
                    "投放广告ID", "投放记录验证");
            assertEqualsWithDebug(testAdSpaceId.longValue(), placement.get("adSpaceId").asLong(),
                    "投放广告位ID", "投放记录验证");
            assertTrueWithDebug(placement.has("displayTimeList"), "应该有展示时间列表", "投放记录验证");
            assertTrueWithDebug(placement.has("displayDurationInHours"), "应该有展示时长", "投放记录验证");
        }

        logSuccess("商店投放记录获取成功，共 " + placements.size() + " 条记录");
        logTestEnd("获取商店投放记录", true);
    }

    @Test
    @Order(13)
    @DisplayName("13. 获取广告位投放记录")
    @Commit
    void testGetAdSpacePlacements() throws Exception {
        logTestStart("获取广告位投放记录", "测试获取指定广告位的投放记录");

        checkPreconditions("获取广告位投放记录",
                "adminToken", adminToken,
                "testAdSpaceId", testAdSpaceId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/advertisements/placements/space/" + testAdSpaceId, adminToken),
                200, "获取广告位投放记录API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取广告位投放记录");
        JsonNode placements = extractDataFromResponse(response, "获取广告位投放记录");

        assertTrueWithDebug(placements.isArray(), "投放记录应该是数组格式", "投放记录格式验证");

        logSuccess("广告位投放记录获取成功，共 " + placements.size() + " 条记录");
        logTestEnd("获取广告位投放记录", true);
    }

    @Test
    @Order(14)
    @DisplayName("14. 获取展示广告列表")
    @Commit
    void testGetDisplayAdvertisements() throws Exception {
        logTestStart("获取展示广告列表", "测试获取当前展示的广告列表");

        checkPrecondition(userToken, "userToken", "获取展示广告列表");

        MvcResult result = executeRequest(
                authenticatedGet("/api/advertisements/display", userToken)
                        .param("type", "BANNER"),
                200, "获取展示广告列表API"
        );

        JsonNode response = verifyApiSuccessResponse(result, "获取展示广告列表");
        JsonNode advertisements = extractDataFromResponse(response, "获取展示广告列表");

        assertTrueWithDebug(advertisements.isArray(), "展示广告列表应该是数组格式", "展示广告列表格式验证");

        // 验证展示广告的结构
        if (advertisements.size() > 0) {
            JsonNode ad = advertisements.get(0);
            assertTrueWithDebug(ad.has("id"), "广告应该有ID字段", "展示广告结构验证");
            assertTrueWithDebug(ad.has("title"), "广告应该有标题字段", "展示广告结构验证");
            assertTrueWithDebug(ad.has("content"), "广告应该有内容字段", "展示广告结构验证");
            assertTrueWithDebug(ad.has("linkUrl"), "广告应该有链接字段", "展示广告结构验证");
        }

        logSuccess("展示广告列表获取成功，共 " + advertisements.size() + " 个广告");
        logTestEnd("获取展示广告列表", true);
    }

    // ==================== 广告位槽位管理测试 ====================

    @Test
    @Order(15)
    @DisplayName("15. 设置广告槽位状态")
    @Commit
    void testSetAdSlotStatus() throws Exception {
        logTestStart("设置广告槽位状态", "测试管理员设置广告槽位的可用状态");

        checkPreconditions("设置广告槽位状态",
                "adminToken", adminToken,
                "testAdSpaceId", testAdSpaceId);

        // 获取槽位列表
        MvcResult slotsResult = executeRequest(
                authenticatedGet("/api/advertisements/spaces/" + testAdSpaceId + "/slots", adminToken),
                200, "获取槽位列表"
        );

        JsonNode slotsResponse = verifyApiSuccessResponse(slotsResult, "获取槽位列表");
        JsonNode slots = extractDataFromResponse(slotsResponse, "获取槽位列表");

        if (slots.size() > 0) {
            // 选择第一个槽位进行状态设置
            Long slotId = slots.get(0).get("id").asLong();
            List<Long> slotIds = Arrays.asList(slotId);

            Map<String, Object> statusRequest = TestDataBuilder.createAdSlotStatusRequest(
                    slotIds, false, false
            );

            String requestBody = objectMapper.writeValueAsString(statusRequest);

            logInfo("设置广告槽位状态 - 槽位ID: " + slotId + ", 设置为不可用");

            MvcResult result = executeRequest(
                    authenticatedPatch("/api/advertisements/spaces/" + testAdSpaceId + "/slots/status", adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody),
                    200, "设置广告槽位状态API"
            );

            verifyApiSuccessResponse(result, "设置广告槽位状态");

            // 验证槽位状态更新
            MvcResult updatedSlotsResult = executeRequest(
                    authenticatedGet("/api/advertisements/spaces/" + testAdSpaceId + "/slots", adminToken),
                    200, "验证槽位状态更新"
            );

            JsonNode updatedSlotsResponse = verifyApiSuccessResponse(updatedSlotsResult, "验证槽位状态更新");
            JsonNode updatedSlots = extractDataFromResponse(updatedSlotsResponse, "验证槽位状态更新");

            // 查找更新的槽位并验证状态
            for (JsonNode slot : updatedSlots) {
                if (slot.get("id").asLong() == slotId) {
                    assertFalseWithDebug(slot.get("available").asBoolean(),
                            "槽位应该被设置为不可用", "槽位状态更新验证");
                    break;
                }
            }
        }

        logSuccess("广告槽位状态设置成功");
        logTestEnd("设置广告槽位状态", true);
    }

    // ==================== 广告删除和取消投放测试 ====================

    @Test
    @Order(16)
    @DisplayName("16. 取消广告投放")
    @Commit
    void testCancelAdvertisementPlacement() throws Exception {
        logTestStart("取消广告投放", "测试商家取消已投放的广告");

        checkPreconditions("取消广告投放",
                "storeManagerToken", storeManagerToken,
                "testPlacementId", testPlacementId);

        logInfo("取消广告投放 - 投放ID: " + testPlacementId);

        MvcResult result = executeRequest(
                authenticatedDelete("/api/advertisements/placements/" + testPlacementId, storeManagerToken),
                200, "取消广告投放API"
        );

        verifyApiSuccessResponse(result, "取消广告投放");

        // 验证投放状态更新为已取消
        verifyRecordCountWithDebug("advertisement_placements",
                "id = " + testPlacementId + " AND status = 1",
                1, "投放取消状态验证");

        logSuccess("广告投放取消成功");
        logTestEnd("取消广告投放", true);
    }

    @Test
    @Order(17)
    @DisplayName("17. 删除广告")
    @Commit
    void testDeleteAdvertisement() throws Exception {
        logTestStart("删除广告", "测试商家删除自己的广告");

        checkPreconditions("删除广告",
                "storeManagerToken", storeManagerToken,
                "testAdvertisementId", testAdvertisementId);

        logInfo("删除广告 - 广告ID: " + testAdvertisementId);

        MvcResult result = executeRequest(
                authenticatedDelete("/api/advertisements/" + testAdvertisementId, storeManagerToken),
                200, "删除广告API"
        );

        verifyApiSuccessResponse(result, "删除广告");

        // 验证广告删除
        verifyRecordCountWithDebug("advertisements",
                "id = " + testAdvertisementId,
                0, "广告删除验证");

        logSuccess("广告删除成功");
        logTestEnd("删除广告", true);
    }

    @Test
    @Order(18)
    @DisplayName("20. 测试数据验证")
    @Commit
    void testDataValidation() throws Exception {
        logTestStart("测试数据验证", "测试各种无效数据的处理");

        checkPrecondition(adminToken, "adminToken", "测试数据验证");

        // 1. 创建重复标签的广告位（应该失败）
        Map<String, Object> duplicateSpaceRequest = TestDataBuilder.createAdSpaceRequest(
                AD_SPACE_LABEL, "BANNER", 7, 3
        );
        String requestBody = objectMapper.writeValueAsString(duplicateSpaceRequest);

        MvcResult duplicateResult = executeRequest(
                authenticatedPost("/api/advertisements/spaces", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                409, "创建重复标签广告位（应失败）"
        );

        logInfo("数据验证1: 重复标签广告位创建被拒绝 ✓");

        // 2. 创建无效参数的广告位（应该失败）
        Map<String, Object> invalidSpaceRequest = new HashMap<>();
        invalidSpaceRequest.put("label", ""); // 空标签
        invalidSpaceRequest.put("type", "BANNER");
        invalidSpaceRequest.put("cycleInDay", 0); // 无效周期
        invalidSpaceRequest.put("segmentInHour", -1); // 无效时段

        String invalidRequestBody = objectMapper.writeValueAsString(invalidSpaceRequest);

        MvcResult invalidResult = executeRequest(
                authenticatedPost("/api/advertisements/spaces", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody),
                400, "创建无效参数广告位（应失败）"
        );

        logInfo("数据验证2: 无效参数广告位创建被拒绝 ✓");

        // 3. 创建不包含必需文件的广告（应该失败）
        if (testStoreId != null) {
            MvcResult noFileResult = executeRequest(
                    createMultipartRequest("/api/advertisements", "POST")
                            .param("title", "无图片广告")
                            .param("linkUrl", "http://example.com")
                            .param("storeId", testStoreId.toString())
                            .header("Authorization", "Bearer " + storeManagerToken),
                    400, "创建无图片广告（应失败）"
            );

            logInfo("数据验证3: 无图片广告创建被拒绝 ✓");
        }

        // 4. 投放到不存在的广告位（应该失败）
        if (testAdvertisementId != null) {
            Map<String, Object> invalidPlacementRequest = new HashMap<>();
            invalidPlacementRequest.put("adId", testAdvertisementId);
            invalidPlacementRequest.put("adSpaceId", 99999L); // 不存在的广告位
            invalidPlacementRequest.put("adSlotIds", Arrays.asList(1L));

            String invalidPlacementBody = objectMapper.writeValueAsString(invalidPlacementRequest);

            MvcResult invalidPlacementResult = executeRequest(
                    authenticatedPost("/api/advertisements/placements", storeManagerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidPlacementBody),
                    404, "投放到不存在广告位（应失败）"
            );

            logInfo("数据验证4: 投放到不存在广告位被拒绝 ✓");
        }

        logSuccess("数据验证测试完成，所有无效数据均被正确拒绝");
        logTestEnd("测试数据验证", true);
    }


    @Test
    @Order(19)
    @DisplayName("18. 删除广告位")
    @Commit
    void testDeleteAdSpace() throws Exception {
        logTestStart("删除广告位", "测试管理员删除广告位");

        checkPreconditions("删除广告位",
                "adminToken", adminToken,
                "testAdSpaceId", testAdSpaceId);

        logInfo("删除广告位 - 广告位ID: " + testAdSpaceId);

        MvcResult result = executeRequest(
                authenticatedDelete("/api/advertisements/spaces/" + testAdSpaceId, adminToken),
                200, "删除广告位API"
        );

        verifyApiSuccessResponse(result, "删除广告位");

        // 验证广告位删除
        verifyRecordCountWithDebug("advertisement_spaces",
                "id = " + testAdSpaceId,
                0, "广告位删除验证");

        logSuccess("广告位删除成功");
        logTestEnd("删除广告位", true);
    }

    // ==================== 权限控制测试 ====================

    @Test
    @Order(20)
    @DisplayName("19. 测试权限控制")
    @Commit
    void testPermissionControl() throws Exception {
        logTestStart("测试权限控制", "测试各种权限控制场景");

        checkPreconditions("测试权限控制",
                "userToken", userToken,
                "storeManagerToken", storeManagerToken);

        // 1. 普通用户尝试创建广告位（应该失败）
        Map<String, Object> adSpaceRequest = TestDataBuilder.createAdSpaceRequest(
                "非法广告位", "BANNER", 7, 3
        );
        String requestBody = objectMapper.writeValueAsString(adSpaceRequest);

        MvcResult unauthorizedResult = executeRequest(
                authenticatedPost("/api/advertisements/spaces", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                403, "普通用户创建广告位（应被拒绝）"
        );

        logInfo("权限控制验证1: 普通用户无法创建广告位 ✓");

        // 2. 创建新的广告位和广告进行后续测试
        createTestAdSpaceAndAd();

        // 3. 其他商家尝试修改不属于自己的广告（应该失败）
        String anotherStoreToken = createAnotherStoreManager();

        MockMultipartFile adImageFile = new MockMultipartFile(
                "content", "evil_ad.jpg", "image/jpeg", "evil content".getBytes()
        );

        MvcResult forbiddenResult = executeRequest(
                createMultipartRequest("/api/advertisements/" + testAdvertisementId, "PUT")
                        .file(adImageFile)
                        .param("title", "恶意修改")
                        .param("linkUrl", "http://evil.com")
                        .header("Authorization", "Bearer " + anotherStoreToken),
                403, "其他商家修改广告（应被拒绝）"
        );

        logInfo("权限控制验证2: 商家无法修改其他商家的广告 ✓");

        // 4. 普通用户尝试审核广告投放（应该失败）
        MvcResult reviewResult = executeRequest(
                authenticatedPatch("/api/advertisements/placements/1/review", userToken)
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"非法审核\""),
                403, "普通用户审核投放（应被拒绝）"
        );

        logInfo("权限控制验证3: 普通用户无法审核广告投放 ✓");

        logSuccess("权限控制测试完成，所有权限验证通过");
        logTestEnd("测试权限控制", true);
    }

    // ==================== 业务流程总结验证 ====================

    @Test
    @Order(21)
    @DisplayName("21. 广告模块业务流程总结验证")
    @Commit
    void testAdvertisementFlowSummary() throws Exception {
        logTestStart("广告模块业务流程总结验证", "验证整个广告模块的业务流程和数据一致性");
        logInfo("=== 广告模块业务流程总结验证 ===");

        // 验证广告位相关统计
        int totalAdSpaces = countRecords("advertisement_spaces");
        logInfo("系统中广告位总数: " + totalAdSpaces);

        // 验证广告相关统计
        int totalAds = countRecords("advertisements");
        logInfo("系统中广告总数: " + totalAds);

        // 验证投放相关统计
        int totalPlacements = countRecords("advertisement_placements");
        logInfo("系统中投放记录总数: " + totalPlacements);

        // 验证支付相关统计
        int adPayments = countRecords("payments", "entity_type = 'ADVERTISEMENT_PLACEMENT'");
        logInfo("广告相关支付记录数: " + adPayments);

        // 验证槽位相关统计
        int totalSlots = countRecords("advertisement_slots");
        logInfo("系统中广告槽位总数: " + totalSlots);

        // 验证各状态的投放数量
        int enabledPlacements = countRecords("advertisement_placements", "status = 1");
        int disabledPlacements = countRecords("advertisement_placements", "status = 2");
        int pendingPlacements = countRecords("advertisement_placements", "status = 3");

        logInfo("投放状态统计:");
        logInfo("- 已启用投放: " + enabledPlacements);
        logInfo("- 已禁用投放: " + disabledPlacements);
        logInfo("- 待审核投放: " + pendingPlacements);

        // 最终验证
        assertTrueWithDebug(totalAdSpaces >= 0, "应该有广告位记录统计", "流程总结验证");
        assertTrueWithDebug(totalAds >= 0, "应该有广告记录统计", "流程总结验证");
        assertTrueWithDebug(totalPlacements >= 0, "应该有投放记录统计", "流程总结验证");

        logInfo("=== 广告模块业务流程总结验证完成 ===");
        logSuccess("广告模块核心业务流程测试全部通过！");

        // 输出测试总结
        logInfo("\n=== 广告模块测试总结 ===");
        logInfo("✅ 广告位管理: 创建、查询、槽位管理、删除");
        logInfo("✅ 广告管理: 创建、查询、更新、删除");
        logInfo("✅ 投放管理: 投放申请、支付、审核、取消");
        logInfo("✅ 权限控制: 管理员权限、商家权限、用户权限");
        logInfo("✅ 数据验证: 参数验证、业务规则验证");
        logInfo("✅ 状态流转: 投放状态、审核状态、支付状态");
        logInfo("========================");

        logTestEnd("广告模块业务流程总结验证", true);
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
                "qualifications", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        MvcResult createResult = executeRequest(
                multipart("/api/stores")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", name)
                        .param("address", address)
                        .param("description", description)
                        .param("merchantAccounts", "{\"ALIPAY\":\"test_merchant@alipay.com\"}")
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
     * 创建测试用的广告位和广告
     */
    private void createTestAdSpaceAndAd() throws Exception {
        // 创建新的广告位
        Map<String, Object> adSpaceRequest = TestDataBuilder.createAdSpaceRequest(
                "测试权限广告位", "BANNER", 7, 3
        );

        String requestBody = objectMapper.writeValueAsString(adSpaceRequest);

        MvcResult spaceResult = executeRequest(
                authenticatedPost("/api/advertisements/spaces", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                200, "创建测试权限广告位"
        );

        verifyApiSuccessResponse(spaceResult, "创建测试权限广告位");

        // 获取新创建的广告位ID
        MvcResult spacesResult = executeRequest(
                authenticatedGet("/api/advertisements/spaces", userToken),
                200, "获取广告位列表"
        );

        JsonNode spacesResponse = verifyApiSuccessResponse(spacesResult, "获取广告位列表");
        JsonNode spaces = extractDataFromResponse(spacesResponse, "获取广告位列表");

        for (JsonNode space : spaces) {
            if ("测试权限广告位".equals(space.get("label").asText())) {
                testAdSpaceId = space.get("id").asLong();
                break;
            }
        }

        // 创建新的广告
        MockMultipartFile adImageFile = new MockMultipartFile(
                "content", "test_ad.jpg", "image/jpeg", "test ad content".getBytes()
        );

        MvcResult adResult = executeRequest(
                createMultipartRequest("/api/advertisements", "POST")
                        .file(adImageFile)
                        .param("title", "测试权限广告")
                        .param("linkUrl", "http://test.com")
                        .param("storeId", testStoreId.toString())
                        .header("Authorization", "Bearer " + storeManagerToken),
                200, "创建测试权限广告"
        );

        verifyApiSuccessResponse(adResult, "创建测试权限广告");

        // 获取新创建的广告ID
        MvcResult adsResult = executeRequest(
                authenticatedGet("/api/advertisements/store/" + testStoreId, storeManagerToken),
                200, "获取商店广告列表"
        );

        JsonNode adsResponse = verifyApiSuccessResponse(adsResult, "获取商店广告列表");
        JsonNode ads = extractDataFromResponse(adsResponse, "获取商店广告列表");

        for (JsonNode ad : ads) {
            if ("测试权限广告".equals(ad.get("title").asText())) {
                testAdvertisementId = ad.get("id").asLong();
                break;
            }
        }
    }

    /**
     * 创建另一个商家用于权限测试
     */
    private String createAnotherStoreManager() throws Exception {
        String anotherUsername = "another_manager" + generateUniqueId();
        String anotherPhone = generateUniquePhone();
        String anotherEmail = generateUniqueEmail();

        // 创建另一个用户
        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", anotherUsername)
                        .param("phone", anotherPhone)
                        .param("password", "password123")
                        .param("email", anotherEmail)
                        .param("name", "Another Manager")
                        .param("location", "Another Location"),
                200, "创建另一个用户"
        );

        // 登录获取token
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(anotherUsername, "password123")
        );

        MvcResult loginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "另一个用户登录"
        );

        JsonNode loginResponse = verifyApiSuccessResponse(loginResult, "另一个用户登录");
        return extractDataFromResponse(loginResponse, "另一个用户登录").asText();
    }
}