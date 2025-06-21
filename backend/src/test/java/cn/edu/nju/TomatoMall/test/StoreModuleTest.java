package cn.edu.nju.TomatoMall.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 商店模块集成测试
 * 测试商店创建、审核、管理、员工管理等功能
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("商店模块测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StoreModuleTest extends BaseIntegrationTest {

    private Long testStoreId;
    private String storeManagerToken;
    private String employeeToken;
    private Long employeeUserId;
    private String authToken;

    @Test
    @Order(1)
    @DisplayName("创建商店 - 成功案例")
    @Commit
    void testCreateStore_Success() throws Exception {
        logTestStart("创建商店测试", "测试普通用户创建商店的完整流程");

        // 检查前置条件
        checkPrecondition(userToken, "userToken", "创建商店测试");

        // 准备测试数据
        Map<String, String> storeData = TestDataBuilder.createStoreParams(
                "技术图书专营店", "北京市朝阳区中关村大街1号", "专业的技术图书销售店铺"
        );

        // 创建模拟文件
        MockMultipartFile logoFile = new MockMultipartFile(
                "logo", "logo.jpg", "image/jpeg", "fake logo content".getBytes()
        );
        MockMultipartFile qualificationFile = new MockMultipartFile(
                "qualification", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        // 执行创建店铺请求
        MvcResult result = executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", storeData.get("name"))
                        .param("address", storeData.get("address"))
                        .param("description", storeData.get("description"))
                        .param("merchantAccounts.ALIPAY", "alipay_account@alipay.com")
                        .header("Authorization", "Bearer " + userToken),
                200, "创建商店API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "创建商店响应验证");

        // 等待数据写入
        waitFor(200);

        // 验证数据库中的店铺记录
        verifyRecordCountWithDebug("stores", "name = '技术图书专营店'", 1, "创建商店数据库验证");

        // 验证店铺状态为待审核
        verifyRecordCountWithDebug("stores", "name = '技术图书专营店' AND status = 'PENDING'", 1, "店铺状态验证");

        logTestEnd("创建商店测试", true);
    }

    @Test
    @Order(2)
    @DisplayName("获取待审核店铺列表 - 管理员权限")
    @Commit
    void testGetAwaitingReviewStores() throws Exception {
        logTestStart("获取待审核店铺列表", "测试管理员获取待审核店铺列表功能");

        // 检查前置条件
        checkPrecondition(adminToken, "adminToken", "获取待审核店铺列表");

        Map<String, String> pageParams = TestDataBuilder.createDefaultPageParams();

        MvcResult result = executeRequest(
                authenticatedGet("/api/stores/awaiting-review", adminToken)
                        .param("page", pageParams.get("page"))
                        .param("size", pageParams.get("size"))
                        .param("field", pageParams.get("field"))
                        .param("order", pageParams.get("order")),
                200, "获取待审核店铺列表API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取待审核店铺列表响应验证");
        JsonNode data = extractDataFromResponse(response, "获取待审核店铺列表");
        JsonNode storeList = verifyPageResponse(data, "待审核店铺列表分页验证");

        // 验证店铺列表不为空
        assertListNotEmptyWithDebug(storeList, "待审核店铺列表", "获取待审核店铺列表");

        // 获取第一个店铺的ID和详细信息
        JsonNode firstStore = storeList.get(0);
        testStoreId = firstStore.get("id").asLong();

        assertEqualsWithDebug("技术图书专营店", firstStore.get("name").asText(), "店铺名称", "店铺信息验证");
        assertEqualsWithDebug("PENDING", firstStore.get("status").asText(), "店铺状态", "店铺状态验证");

        logInfo("获取到测试店铺ID: " + testStoreId);
        logTestEnd("获取待审核店铺列表", true);
    }

    @Test
    @Order(3)
    @DisplayName("获取店铺资质信息 - 管理员权限")
    @Commit
    void testGetStoreQualifications() throws Exception {
        logTestStart("获取店铺资质信息", "测试管理员获取店铺资质文件信息");

        // 检查前置条件
        checkPreconditions("获取店铺资质信息", "adminToken", adminToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/stores/" + testStoreId + "/qualifications", adminToken),
                200, "获取店铺资质信息API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取店铺资质信息响应验证");
        JsonNode qualifications = extractDataFromResponse(response, "获取店铺资质信息");

        // 验证资质信息格式
        assertTrueWithDebug(qualifications.isArray(), "资质信息应该是数组格式", "资质信息格式验证");
        assertTrueWithDebug(qualifications.size() > 0, "应该有资质文件信息", "资质文件存在验证");

        logTestEnd("获取店铺资质信息", true);
    }

    @Test
    @Order(4)
    @DisplayName("审核店铺 - 通过审核")
    @Commit
    void testReviewStore_Approve() throws Exception {
        logTestStart("审核店铺通过", "测试管理员审核通过店铺申请");

        // 检查前置条件
        checkPreconditions("审核店铺", "adminToken", adminToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedPost("/api/stores/review", adminToken)
                        .param("storeId", testStoreId.toString())
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"店铺资质完整，审核通过\""),
                200, "审核店铺API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "审核店铺响应验证");

        // 等待数据更新
        waitFor(200);

        // 验证店铺状态已更新为正常
        verifyRecordCountWithDebug("stores", "id = " + testStoreId + " AND status = 'NORMAL'", 1, "审核通过状态验证");

        logTestEnd("审核店铺通过", true);
    }

    @Test
    @Order(5)
    @DisplayName("获取用户管理的店铺列表")
    @Commit
    void testGetManagedStores() throws Exception {
        logTestStart("获取用户管理的店铺列表", "测试用户获取自己管理的店铺列表");

        // 检查前置条件
        checkPrecondition(userToken, "userToken", "获取管理店铺列表");

        MvcResult result = executeRequest(
                authenticatedGet("/api/stores/managed", userToken),
                200, "获取管理店铺列表API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取管理店铺列表响应验证");
        JsonNode storeList = extractDataFromResponse(response, "获取管理店铺列表");

        // 验证用户管理的店铺列表
        assertTrueWithDebug(storeList.isArray(), "管理店铺列表应该是数组", "管理店铺列表格式验证");
        assertEqualsWithDebug(1, storeList.size(), "管理店铺数量", "管理店铺数量验证");

        JsonNode firstStore = storeList.get(0);
        assertEqualsWithDebug("技术图书专营店", firstStore.get("name").asText(), "店铺名称", "管理店铺信息验证");
        assertEqualsWithDebug("NORMAL", firstStore.get("status").asText(), "店铺状态", "管理店铺状态验证");

        // 保存店长token用于后续测试
        storeManagerToken = userToken;

        logTestEnd("获取用户管理的店铺列表", true);
    }

    @Test
    @Order(6)
    @DisplayName("获取店铺详细信息")
    @Commit
    void testGetStoreDetails() throws Exception {
        logTestStart("获取店铺详细信息", "测试获取指定店铺的详细信息");

        // 检查前置条件
        checkPreconditions("获取店铺详细信息", "userToken", userToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/stores/" + testStoreId, userToken),
                200, "获取店铺详细信息API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取店铺详细信息响应验证");
        JsonNode storeData = extractDataFromResponse(response, "获取店铺详细信息");

        // 验证店铺详细信息
        assertEqualsWithDebug(testStoreId.longValue(), storeData.get("id").asLong(), "店铺ID", "店铺ID验证");
        assertEqualsWithDebug("技术图书专营店", storeData.get("name").asText(), "店铺名称", "店铺名称验证");
        assertEqualsWithDebug("北京市朝阳区中关村大街1号", storeData.get("address").asText(), "店铺地址", "店铺地址验证");
        assertEqualsWithDebug("专业的技术图书销售店铺", storeData.get("description").asText(), "店铺描述", "店铺描述验证");
        assertEqualsWithDebug("NORMAL", storeData.get("status").asText(), "店铺状态", "店铺状态验证");

        logTestEnd("获取店铺详细信息", true);
    }

    @Test
    @Order(7)
    @DisplayName("更新店铺信息")
    @Commit
    void testUpdateStoreInfo() throws Exception {
        logTestStart("更新店铺信息", "测试店长更新店铺基本信息");

        // 检查前置条件
        checkPreconditions("更新店铺信息", "storeManagerToken", storeManagerToken, "testStoreId", testStoreId);

        MockMultipartFile newLogoFile = new MockMultipartFile(
                "logo", "new_logo.jpg", "image/jpeg", "new fake logo content".getBytes()
        );

        MvcResult result = executeRequest(
                createMultipartRequest("/api/stores/" + testStoreId, "PATCH")
                        .file(newLogoFile)
                        .param("name", "技术图书专营店(更新版)")
                        .param("address", "北京市朝阳区中关村大街2号")
                        .param("description", "专业的技术图书销售店铺，现已更新")
                        .header("Authorization", "Bearer " + storeManagerToken),
                200, "更新店铺信息API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "更新店铺信息响应验证");

        // 等待数据更新
        waitFor(200);

        // 验证店铺状态变为更新中
        verifyRecordCountWithDebug("stores", "id = " + testStoreId + " AND status = 'UPDATING'", 1, "更新状态验证");

        logTestEnd("更新店铺信息", true);
    }

    @Test
    @Order(8)
    @DisplayName("管理员审核店铺更新 - 通过")
    @Commit
    void testReviewStoreUpdate_Approve() throws Exception {
        logTestStart("审核店铺更新", "测试管理员审核店铺更新申请");

        // 检查前置条件
        checkPreconditions("审核店铺更新", "adminToken", adminToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedPost("/api/stores/review", adminToken)
                        .param("storeId", testStoreId.toString())
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"店铺更新信息审核通过\""),
                200, "审核店铺更新API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "审核店铺更新响应验证");

        // 等待数据更新
        waitFor(200);

        // 验证店铺状态恢复为正常
        verifyRecordCountWithDebug("stores", "id = " + testStoreId + " AND status = 'NORMAL'", 1, "更新审核通过状态验证");

        // 验证店铺信息已更新
        MvcResult storeResult = executeRequest(
                authenticatedGet("/api/stores/" + testStoreId, storeManagerToken),
                200, "获取更新后店铺信息"
        );

        JsonNode response = verifyApiSuccessResponse(storeResult, "获取更新后店铺信息响应验证");
        JsonNode storeData = extractDataFromResponse(response, "获取更新后店铺信息");

        assertEqualsWithDebug("技术图书专营店(更新版)", storeData.get("name").asText(), "更新后店铺名称", "店铺名称更新验证");
        assertEqualsWithDebug("北京市朝阳区中关村大街2号", storeData.get("address").asText(), "更新后店铺地址", "店铺地址更新验证");

        logTestEnd("审核店铺更新", true);
    }

    @Test
    @Order(9)
    @DisplayName("创建员工授权Token")
    @Commit
    void testCreateEmployeeToken() throws Exception {
        logTestStart("创建员工授权Token", "测试店长创建员工授权Token");

        // 检查前置条件
        checkPreconditions("创建员工授权Token", "storeManagerToken", storeManagerToken, "testStoreId", testStoreId);

        String tokenRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createTokenRequest("店员授权Token", TestDataBuilder.getFutureTimeString(30))
        );

        MvcResult result = executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/tokens", storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest),
                200, "创建员工授权Token API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "创建员工授权Token响应验证");
        authToken = extractDataFromResponse(response, "创建员工授权Token").asText();

        assertNotNullWithDebug(authToken, "授权Token", "授权Token创建验证");
        assertFalseWithDebug(authToken.isEmpty(), "授权Token不应为空", "授权Token非空验证");

        logTestEnd("创建员工授权Token", true);
    }

 @Test
    @Order(10)
    @DisplayName("获取店铺授权Token列表")
    void testGetStoreTokens() throws Exception {
        logTestStart("获取店铺授权Token列表", "测试店长获取店铺的所有授权Token");

        // 检查前置条件
        checkPreconditions("获取店铺授权Token列表", "storeManagerToken", storeManagerToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/stores/" + testStoreId + "/tokens", storeManagerToken),
                200, "获取店铺授权Token列表API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取店铺授权Token列表响应验证");
        JsonNode tokenList = extractDataFromResponse(response, "获取店铺授权Token列表");

        // 验证token列表
        assertTrueWithDebug(tokenList.isArray(), "Token列表应该是数组", "Token列表格式验证");
        assertTrueWithDebug(tokenList.size() > 0, "应该有创建的授权token", "Token列表非空验证");

        JsonNode firstToken = tokenList.get(0);
        assertEqualsWithDebug("店员授权Token", firstToken.get("name").asText(), "Token名称", "Token信息验证");
        assertFalseWithDebug(firstToken.get("expired").asBoolean(), "token应该未过期", "Token过期状态验证");

        logTestEnd("获取店铺授权Token列表", true);
    }

 @Test
@Commit
    @Order(11)
    @DisplayName("创建新员工用户")
    void testCreateEmployeeUser() throws Exception {
        logTestStart("创建新员工用户", "创建一个新用户作为员工进行后续测试");

        // 创建一个新用户作为员工
        Map<String, Object> employeeData = TestDataBuilder.createUserRequest(
                "employee001", "13911112222", "password123",
                "employee@test.com", "Store Employee", "Beijing"
        );

        MvcResult result = executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", employeeData.get("username").toString())
                        .param("phone", employeeData.get("phone").toString())
                        .param("password", employeeData.get("password").toString())
                        .param("email", employeeData.get("email").toString())
                        .param("name", employeeData.get("name").toString())
                        .param("location", employeeData.get("location").toString()),
                200, "创建员工用户API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "创建员工用户响应验证");

        // 等待用户创建
        waitFor(100);

        // 员工登录获取token
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest("employee001", "password123")
        );

        MvcResult loginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "员工登录API调用"
        );

        JsonNode loginResponse = verifyApiSuccessResponse(loginResult, "员工登录响应验证");
        employeeToken = extractDataFromResponse(loginResponse, "员工登录").asText();

        // 获取员工用户ID
        MvcResult userResult = executeRequest(
                authenticatedGet("/api/users", employeeToken),
                200, "获取员工用户信息API调用"
        );

        JsonNode userResponse = verifyApiSuccessResponse(userResult, "获取员工用户信息响应验证");
        JsonNode userData = extractDataFromResponse(userResponse, "获取员工用户信息");
        employeeUserId = userData.get("id").asLong();

        logInfo("员工用户创建成功，ID: " + employeeUserId);
        logTestEnd("创建新员工用户", true);
    }

 @Test
@Commit
    @Order(12)
    @DisplayName("员工使用授权Token加入店铺")
    void testEmployeeJoinStore() throws Exception {
        logTestStart("员工加入店铺", "测试员工使用授权Token加入店铺");

        // 检查前置条件
        checkPreconditions("员工加入店铺",
                "employeeToken", employeeToken,
                "testStoreId", testStoreId,
                "authToken", authToken);

        MvcResult result = executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/auth", employeeToken)
                        .param("token", authToken),
                200, "员工加入店铺API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "员工加入店铺响应验证");

        // 等待数据更新
        waitFor(200);

        // 验证雇佣关系记录
        verifyRecordCountWithDebug("employments",
                "store_id = " + testStoreId + " AND employee_id = " + employeeUserId,
                1, "雇佣关系验证");

        logTestEnd("员工加入店铺", true);
    }

 @Test
@Commit
    @Order(13)
    @DisplayName("验证员工店铺权限")
    void testEmployeeStoreRole() throws Exception {
        logTestStart("验证员工店铺权限", "测试员工在店铺中的权限角色");

        // 检查前置条件
        checkPreconditions("验证员工店铺权限", "employeeToken", employeeToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/permissions/store/" + testStoreId, employeeToken),
                200, "获取员工店铺权限API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取员工店铺权限响应验证");
        String role = extractDataFromResponse(response, "获取员工店铺权限").asText();

        assertEqualsWithDebug("STAFF", role, "员工店铺角色", "员工权限验证");

        logTestEnd("验证员工店铺权限", true);
    }

 @Test
@Commit
    @Order(14)
    @DisplayName("验证店长权限")
    void testManagerStoreRole() throws Exception {
        logTestStart("验证店长权限", "测试店长在店铺中的权限角色");

        // 检查前置条件
        checkPreconditions("验证店长权限", "storeManagerToken", storeManagerToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/permissions/store/" + testStoreId, storeManagerToken),
                200, "获取店长店铺权限API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取店长店铺权限响应验证");
        String role = extractDataFromResponse(response, "获取店长店铺权限").asText();

        assertEqualsWithDebug("MANAGER", role, "店长店铺角色", "店长权限验证");

        logTestEnd("验证店长权限", true);
    }

 @Test
@Commit
    @Order(15)
    @DisplayName("获取员工工作的店铺列表")
    void testGetWorkedStores() throws Exception {
        logTestStart("获取员工工作店铺", "测试员工获取自己工作的店铺列表");

        // 检查前置条件
        checkPrecondition(employeeToken, "employeeToken", "获取员工工作店铺");

        MvcResult result = executeRequest(
                authenticatedGet("/api/stores/worked", employeeToken),
                200, "获取员工工作店铺API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取员工工作店铺响应验证");
        JsonNode storeList = extractDataFromResponse(response, "获取员工工作店铺");

        // 验证员工工作的店铺列表
        assertTrueWithDebug(storeList.isArray(), "工作店铺列表应该是数组", "工作店铺列表格式验证");
        assertEqualsWithDebug(1, storeList.size(), "工作店铺数量", "工作店铺数量验证");
        assertEqualsWithDebug(testStoreId.longValue(), storeList.get(0).get("id").asLong(), "工作店铺ID", "工作店铺ID验证");

        logTestEnd("获取员工工作店铺", true);
    }

 @Test
@Commit
    @Order(16)
    @DisplayName("获取店铺员工列表")
    void testGetStoreStaff() throws Exception {
        logTestStart("获取店铺员工列表", "测试店长获取店铺的员工列表");

        // 检查前置条件
        checkPreconditions("获取店铺员工列表", "storeManagerToken", storeManagerToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/api/stores/" + testStoreId + "/staff", storeManagerToken),
                200, "获取店铺员工列表API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取店铺员工列表响应验证");
        JsonNode staffList = extractDataFromResponse(response, "获取店铺员工列表");

        // 验证员工列表
        assertTrueWithDebug(staffList.isArray(), "员工列表应该是数组", "员工列表格式验证");
        assertEqualsWithDebug(1, staffList.size(), "员工数量", "员工数量验证");
        assertEqualsWithDebug("employee001", staffList.get(0).get("username").asText(), "员工用户名", "员工信息验证");

        logTestEnd("获取店铺员工列表", true);
    }

 @Test
@Commit
    @Order(17)
    @DisplayName("员工主动辞职")
    void testEmployeeResign() throws Exception {
        logTestStart("员工主动辞职", "测试员工主动从店铺辞职");

        // 检查前置条件
        checkPreconditions("员工主动辞职", "employeeToken", employeeToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedDelete("/api/stores/" + testStoreId + "/resign", employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"个人原因，申请辞职\""),
                200, "员工主动辞职API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "员工主动辞职响应验证");

        // 等待数据更新
        waitFor(200);

        // 验证雇佣关系已结束
        verifyRecordCountWithDebug("employments",
                "store_id = " + testStoreId + " AND employee_id = " + employeeUserId,
                0, "辞职后雇佣关系验证");

        logTestEnd("员工主动辞职", true);
    }

 @Test
@Commit
    @Order(18)
    @DisplayName("验证辞职后员工权限")
    void testResignedEmployeeRole() throws Exception {
        logTestStart("验证辞职后员工权限", "测试员工辞职后的权限变化");

        // 检查前置条件
        checkPreconditions("验证辞职后员工权限", "employeeToken", employeeToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/permissions/store/" + testStoreId, employeeToken),
                200, "获取辞职后员工权限API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取辞职后员工权限响应验证");
        String role = extractDataFromResponse(response, "获取辞职后员工权限").asText();

        assertEqualsWithDebug("CUSTOMER", role, "辞职后员工角色", "辞职后权限验证");

        logTestEnd("验证辞职后员工权限", true);
    }

     @Test
    @Commit
    @Order(19)
    @DisplayName("重新雇佣员工并测试店长解雇功能")
    void testManagerFireEmployee() throws Exception {
        logTestStart("店长解雇员工", "测试重新雇佣员工后店长主动解雇员工");

        // 重新生成授权Token
         testCreateEmployeeToken();

        // 检查前置条件
        checkPreconditions("店长解雇员工",
                "employeeToken", employeeToken,
                "storeManagerToken", storeManagerToken,
                "testStoreId", testStoreId,
                "authToken", authToken);

        // 重新雇佣员工
        MvcResult joinResult = executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/auth", employeeToken)
                        .param("token", authToken),
                200, "重新雇佣员工API调用"
        );

        verifyApiSuccessResponse(joinResult, "重新雇佣员工响应验证");
        waitFor(200);

        // 店长解雇员工
        MvcResult fireResult = executeRequest(
                authenticatedDelete("/api/stores/" + testStoreId + "/staff/" + employeeUserId, storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"工作表现不佳，予以解雇\""),
                200, "店长解雇员工API调用"
        );

        verifyApiSuccessResponse(fireResult, "店长解雇员工响应验证");
        waitFor(200);

        // 验证雇佣关系已结束
        verifyRecordCountWithDebug("employments",
                "store_id = " + testStoreId + " AND employee_id = " + employeeUserId,
                0, "解雇后雇佣关系验证");

        logTestEnd("店长解雇员工", true);
    }

 @Test
@Commit
    @Order(20)
    @DisplayName("验证被解雇员工权限")
    void testFiredEmployeeRole() throws Exception {
        logTestStart("验证被解雇员工权限", "测试员工被解雇后的权限变化");

        // 检查前置条件
        checkPreconditions("验证被解雇员工权限", "employeeToken", employeeToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedGet("/permissions/store/" + testStoreId, employeeToken),
                200, "获取被解雇员工权限API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取被解雇员工权限响应验证");
        String role = extractDataFromResponse(response, "获取被解雇员工权限").asText();

        assertEqualsWithDebug("CUSTOMER", role, "被解雇后员工角色", "被解雇后权限验证");

        logTestEnd("验证被解雇员工权限", true);
    }

 @Test
@Commit
    @Order(21)
    @DisplayName("删除授权Token")
    void testDeleteAuthToken() throws Exception {
        logTestStart("删除授权Token", "测试店长删除员工授权Token");

        // 检查前置条件
        checkPreconditions("删除授权Token", "storeManagerToken", storeManagerToken, "testStoreId", testStoreId);

        // 首先获取token列表找到token ID
        MvcResult listResult = executeRequest(
                authenticatedGet("/api/stores/" + testStoreId + "/tokens", storeManagerToken),
                200, "获取Token列表API调用"
        );

        JsonNode listResponse = verifyApiSuccessResponse(listResult, "获取Token列表响应验证");
        JsonNode tokenList = extractDataFromResponse(listResponse, "获取Token列表");

        assertTrueWithDebug(tokenList.size() > 0, "应该有Token可以删除", "Token列表验证");
        Long tokenId = tokenList.get(0).get("id").asLong();

        // 删除token
        MvcResult deleteResult = executeRequest(
                authenticatedDelete("/api/stores/" + testStoreId + "/tokens/" + tokenId, storeManagerToken),
                200, "删除授权Token API调用"
        );

        verifyApiSuccessResponse(deleteResult, "删除授权Token响应验证");
        waitFor(200);

        // 验证token已被删除
        verifyRecordCountWithDebug("employment_tokens", "id = " + tokenId, 0, "Token删除验证");

        logTestEnd("删除授权Token", true);
    }

 @Test
@Commit
    @Order(22)
    @DisplayName("申请删除店铺")
    void testDeleteStore() throws Exception {
        logTestStart("申请删除店铺", "测试店长申请删除店铺");

        // 检查前置条件
        checkPreconditions("申请删除店铺", "storeManagerToken", storeManagerToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedDelete("/api/stores/" + testStoreId, storeManagerToken),
                200, "申请删除店铺API调用"
        );

        verifyApiSuccessResponse(result, "申请删除店铺响应验证");
        waitFor(200);

        // 验证店铺状态变为删除中
        verifyRecordCountWithDebug("stores", "id = " + testStoreId + " AND status = 'DELETING'", 1, "删除申请状态验证");

        logTestEnd("申请删除店铺", true);
    }

 @Test
@Commit
    @Order(23)
    @DisplayName("管理员审核店铺删除 - 通过")
    void testReviewStoreDelete_Approve() throws Exception {
        logTestStart("审核店铺删除", "测试管理员审核通过店铺删除申请");

        // 检查前置条件
        checkPreconditions("审核店铺删除", "adminToken", adminToken, "testStoreId", testStoreId);

        MvcResult result = executeRequest(
                authenticatedPost("/api/stores/review", adminToken)
                        .param("storeId", testStoreId.toString())
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"同意删除店铺\""),
                200, "审核店铺删除API调用"
        );

        verifyApiSuccessResponse(result, "审核店铺删除响应验证");
        waitFor(200);

        // 验证店铺已被删除
        verifyRecordCountWithDebug("stores", "id = " + testStoreId + "AND status = 'DELETED'", 1, "店铺删除验证");

        logTestEnd("审核店铺删除", true);
    }

 @Test
@Commit
    @Order(24)
    @DisplayName("验证删除后无法访问店铺")
    void testAccessDeletedStore() throws Exception {
        logTestStart("验证删除后店铺访问", "测试删除后的店铺无法被访问");

        // 检查前置条件
        checkPreconditions("验证删除后店铺访问", "storeManagerToken", storeManagerToken, "testStoreId", testStoreId);

        // 尝试访问已删除的店铺应该返回404
        executeRequest(
                authenticatedGet("/api/stores/" + testStoreId, storeManagerToken),
                404, "访问已删除店铺API调用"
        );

        logTestEnd("验证删除后店铺访问", true);
    }

 @Test
@Commit
    @Order(25)
    @DisplayName("测试店铺审核拒绝流程")
    void testStoreReviewReject() throws Exception {
        logTestStart("测试店铺审核拒绝", "测试管理员拒绝店铺申请的完整流程");

        // 检查前置条件
        checkPrecondition(adminToken, "adminToken", "测试店铺审核拒绝");

        // 创建另一个店铺用于测试拒绝流程
        Map<String, String> storeData = TestDataBuilder.createRandomStore();

        MockMultipartFile logoFile = new MockMultipartFile(
                "logo", "logo.jpg", "image/jpeg", "fake logo content".getBytes()
        );
        MockMultipartFile qualificationFile = new MockMultipartFile(
                "qualification", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", storeData.get("name"))
                        .param("address", storeData.get("address"))
                        .param("description", storeData.get("description"))
                        .param("merchantAccounts.ALIPAY", "test_alipay@alipay.com")
                        .header("Authorization", "Bearer " + userToken),
                200, "创建待拒绝店铺API调用"
        );

        waitFor(200);

        // 获取新创建的店铺ID
        MvcResult listResult = executeRequest(
                authenticatedGet("/api/stores/awaiting-review", adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "id")
                        .param("order", "true"),
                200, "获取待审核店铺列表API调用（拒绝测试）"
        );

        JsonNode listResponse = verifyApiSuccessResponse(listResult, "获取待审核店铺列表响应验证（拒绝测试）");
        JsonNode listData = extractDataFromResponse(listResponse, "获取待审核店铺列表（拒绝测试）");
        JsonNode pendingStores = verifyPageResponse(listData, "待审核店铺列表分页验证（拒绝测试）");

        assertTrueWithDebug(pendingStores.size() > 0, "应该有待审核的店铺", "待审核店铺存在验证");
        Long newStoreId = pendingStores.get(0).get("id").asLong();

        // 拒绝审核
        MvcResult rejectResult = executeRequest(
                authenticatedPost("/api/stores/review", adminToken)
                        .param("storeId", newStoreId.toString())
                        .param("pass", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"资质文件不完整，审核不通过\""),
                200, "拒绝店铺审核API调用"
        );

        verifyApiSuccessResponse(rejectResult, "拒绝店铺审核响应验证");
        waitFor(200);

        // 验证店铺状态变为删除
        verifyRecordCountWithDebug("stores", "id = " + newStoreId + "AND status = 'SUSPENDED'", 1, "审核拒绝状态验证");

        logTestEnd("测试店铺审核拒绝", true);
    }

    @Test
    @Commit
    @Order(27)
    @DisplayName("测试权限控制")
    void testPermissionControl() throws Exception {
        logTestStart("测试权限控制", "测试非店长用户无权限访问店铺管理功能");

        // 检查前置条件
        checkPrecondition(employeeToken, "employeeToken", "测试权限控制");

        // 重新创建一个店铺进行权限测试
        createTestStoreForPermissionTest();

        // 使用普通员工token访问需要店长权限的接口应该被拒绝
        String tokenRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createTokenRequest("店员授权Token", TestDataBuilder.getFutureTimeString(30))
        );

        MvcResult result = executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/tokens", employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest),
                403, "创建员工授权Token API调用"
        );

        executeRequest(
                authenticatedDelete("/api/stores/" + testStoreId + "/staff/" + employeeUserId, employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"test\""),
                403, "员工尝试解雇员工（应被拒绝）"
        );

        logTestEnd("测试权限控制", true);
    }

 @Test
@Commit
    @Order(28)
    @DisplayName("测试数据验证")
    void testDataValidation() throws Exception {
        logTestStart("测试数据验证", "测试各种边界值和无效数据的处理");

        // 检查前置条件
        checkPrecondition(userToken, "userToken", "测试数据验证");

        MockMultipartFile logoFile = new MockMultipartFile(
                "logo", "logo.jpg", "image/jpeg", "fake logo content".getBytes()
        );
        MockMultipartFile qualificationFile = new MockMultipartFile(
                "qualification", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        // 测试超长店铺名称应该失败
        String longName = new String(new char[101]).replace("\0", "a");
        executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", longName)
                        .param("address", "Test Address")
                        .param("description", "Test Description")
                        .param("merchantAccounts.ALIPAY", "test_alipay@alipay.com")
                        .header("Authorization", "Bearer " + userToken),
                400, "超长店铺名称测试（应失败）"
        );

        // 测试空店铺名称应该失败
        executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", "")
                        .param("address", "Test Address")
                        .param("description", "Test Description")
                        .param("merchantAccounts.ALIPAY", "test_alipay@alipay.com")
                        .header("Authorization", "Bearer " + userToken),
                400, "空店铺名称测试（应失败）"
        );

        // 测试缺少必需文件应该失败
        executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(qualificationFile)
                        .param("name", "Test Store")
                        .param("address", "Test Address")
                        .param("description", "Test Description")
                        .header("Authorization", "Bearer " + userToken),
                400, "缺少logo文件测试（应失败）"
        );

        logTestEnd("测试数据验证", true);
    }

 @Test
@Commit
    @Order(29)
    @DisplayName("测试重复店铺名称")
    void testDuplicateStoreName() throws Exception {
        logTestStart("测试重复店铺名称", "测试创建同名店铺应该失败");

        // 检查前置条件
        checkPrecondition(userToken, "userToken", "测试重复店铺名称");

        // 先创建一个店铺
        Map<String, String> storeData = TestDataBuilder.createStoreParams(
                "重复名称测试店", "地址1", "描述1"
        );

        MockMultipartFile logoFile = new MockMultipartFile(
                "logo", "logo.jpg", "image/jpeg", "fake logo content".getBytes()
        );
        MockMultipartFile qualificationFile = new MockMultipartFile(
                "qualification", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", storeData.get("name"))
                        .param("address", storeData.get("address"))
                        .param("description", storeData.get("description"))
                        .param("merchantAccounts.ALIPAY", "test_alipay@alipay.com")
                        .header("Authorization", "Bearer " + userToken),
                200, "创建第一个店铺"
        );

        waitFor(200);

        // 尝试创建同名店铺应该失败
        executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", storeData.get("name"))
                        .param("address", "地址2")
                        .param("description", "描述2")
                        .param("merchantAccounts.ALIPAY", "test_alipay@alipay.com")
                        .header("Authorization", "Bearer " + userToken),
                409, "创建重复名称店铺（应失败）"
        );

        logTestEnd("测试重复店铺名称", true);
    }

    @Test
    @Commit
    @Order(30)
    @DisplayName("测试Token过期功能")
    void testTokenExpiration() throws Exception {
        logTestStart("测试Token过期功能", "测试过期Token无法使用");

        // 重新创建测试环境
         checkPreconditions("测试Token过期功能",
                 "storeManagerToken", storeManagerToken,
                 "testStoreId", testStoreId);

        // 创建一个短期过期的token
        String tokenRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createTokenRequest("短期Token", LocalDateTime.now().plusSeconds(3).toString())
        );

        MvcResult result = executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/tokens", storeManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest),
                200, "创建短期Token"
        );

        JsonNode response = verifyApiSuccessResponse(result, "创建短期Token响应验证");
        String expiredToken = extractDataFromResponse(response, "创建短期Token").asText();

        // 等待token过期
        waitFor(5000);

        // 尝试使用过期token应该失败
        executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/auth", employeeToken)
                        .param("token", expiredToken),
                403, "使用过期Token（应失败）"
        );

        logTestEnd("测试Token过期功能", true);
    }

    @Test
    @Commit
    @Order(31)
    @DisplayName("测试店铺权限隔离")
    void testStorePermissionIsolation() throws Exception {
        logTestStart("测试店铺权限隔离", "测试用户只能管理自己的店铺");

        // 检查前置条件
        checkPrecondition(userToken, "userToken", "测试店铺权限隔离");

        // 创建另一个用户和店铺
        Map<String, Object> anotherUserData = TestDataBuilder.createRandomUser();

        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", anotherUserData.get("username").toString())
                        .param("phone", anotherUserData.get("phone").toString())
                        .param("password", anotherUserData.get("password").toString())
                        .param("email", anotherUserData.get("email").toString())
                        .param("name", anotherUserData.get("name").toString())
                        .param("location", anotherUserData.get("location").toString()),
                200, "创建另一个用户"
        );

        waitFor(100);

        // 登录获取token
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(anotherUserData.get("username").toString(), "password123")
        );

        MvcResult loginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "另一个用户登录"
        );

        JsonNode loginResponse = verifyApiSuccessResponse(loginResult, "另一个用户登录响应验证");
        String anotherUserToken = extractDataFromResponse(loginResponse, "另一个用户登录").asText();

        // 重新创建测试店铺
        createTestStoreForPermissionTest();

        // 另一个用户尝试访问我们的店铺应该被拒绝
        executeRequest(
                authenticatedGet("/api/stores/" + testStoreId + "/tokens", anotherUserToken),
                403, "其他用户访问店铺Token（应被拒绝）"
        );

        String tokenRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createTokenRequest("店员授权Token", TestDataBuilder.getFutureTimeString(30))
        );

        MvcResult result = executeRequest(
                authenticatedPost("/api/stores/" + testStoreId + "/tokens", anotherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest),
                403, "创建员工授权Token API调用"
        );

        logTestEnd("测试店铺权限隔离", true);
    }

    @Test
    @Commit
    @Order(32)
    @DisplayName("测试店铺收款账户信息")
    void testStoreMerchantAccounts() throws Exception {
        logTestStart("测试店铺收款账户信息", "测试获取店铺的收款账户信息");

        // 重新创建测试店铺
        createTestStoreForAccountTest();

        MvcResult result = executeRequest(
                authenticatedGet("/api/stores/" + testStoreId + "/merchant-accounts", storeManagerToken),
                200, "获取店铺收款账户信息"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取店铺收款账户信息响应验证");
        JsonNode accountData = extractDataFromResponse(response, "获取店铺收款账户信息");

        // 验证收款账户信息格式
        assertTrueWithDebug(accountData.isObject(), "收款账户信息应该是对象格式", "收款账户信息格式验证");

        logTestEnd("测试店铺收款账户信息", true);
    }

    /**
     * 辅助方法：为权限测试创建测试店铺
     */
    private void createTestStoreForPermissionTest() throws Exception {
        Map<String, String> storeData = TestDataBuilder.createRandomStore();

        MockMultipartFile logoFile = new MockMultipartFile(
                "logo", "logo.jpg", "image/jpeg", "fake logo content".getBytes()
        );
        MockMultipartFile qualificationFile = new MockMultipartFile(
                "qualification", "qualification.pdf", "application/pdf", "fake qualification content".getBytes()
        );

        executeRequest(
                createMultipartRequest("/api/stores", "POST")
                        .file(logoFile)
                        .file(qualificationFile)
                        .param("name", storeData.get("name"))
                        .param("address", storeData.get("address"))
                        .param("description", storeData.get("description"))
                        .param("merchantAccounts.ALIPAY", "test_alipay@alipay.com")
                        .header("Authorization", "Bearer " + userToken),
                200, "创建权限测试店铺"
        );

        waitFor(200);

        // 获取创建的店铺ID
        MvcResult listResult = executeRequest(
                authenticatedGet("/api/stores/awaiting-review", adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("field", "id")
                        .param("order", "true"),
                200, "获取权限测试店铺ID"
        );

        JsonNode listResponse = verifyApiSuccessResponse(listResult, "获取权限测试店铺ID响应验证");
        JsonNode listData = extractDataFromResponse(listResponse, "获取权限测试店铺ID");
        JsonNode pendingStores = verifyPageResponse(listData, "权限测试店铺列表验证");
        testStoreId = pendingStores.get(0).get("id").asLong();

        // 管理员审核通过
        executeRequest(
                authenticatedPost("/api/stores/review", adminToken)
                        .param("storeId", testStoreId.toString())
                        .param("pass", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"审核通过\""),
                200, "权限测试店铺审核通过"
        );

        waitFor(200);
        storeManagerToken = userToken;
    }

    /**
     * 辅助方法：为Token测试创建测试店铺
     */
    private void createTestStoreForTokenTest() throws Exception {
        createTestStoreForPermissionTest(); // 复用权限测试的店铺创建逻辑
    }

    /**
     * 辅助方法：为账户测试创建测试店铺
     */
    private void createTestStoreForAccountTest() throws Exception {
        createTestStoreForPermissionTest(); // 复用权限测试的店铺创建逻辑
    }
}