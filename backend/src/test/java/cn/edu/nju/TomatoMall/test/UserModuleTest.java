package cn.edu.nju.TomatoMall.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 用户模块集成测试
 * 测试用户注册、登录、信息更新、密码修改等功能
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("用户模块测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserModuleTest extends BaseIntegrationTest {

    private String newUserToken;
    private Long newUserId;
    private String newUserUsername;
    private String newUserPhone;
    private String newUserEmail;

    @Test
    @Order(1)
    @DisplayName("用户注册 - 成功案例")
    @Commit
    void testUserRegistration_Success() throws Exception {
        logTestStart("用户注册成功", "测试用户注册的完整流程");

        // 准备测试数据 - 使用更唯一的数据
        long uniqueId = System.currentTimeMillis() % 1000000;
        newUserUsername = "newuser" + uniqueId;
        newUserPhone = "139" + String.format("%08d", uniqueId % 100000000L);
        newUserEmail = "newuser" + uniqueId + "@test.com";

        logInfo("准备注册用户: " + newUserUsername + ", 手机号: " + newUserPhone);

        Map<String, Object> userData = TestDataBuilder.createUserRequest(
                newUserUsername, newUserPhone, "password123",
                newUserEmail, "New User", "Shanghai"
        );

        // 执行注册
        MvcResult result = executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", userData.get("username").toString())
                        .param("phone", userData.get("phone").toString())
                        .param("password", userData.get("password").toString())
                        .param("email", userData.get("email").toString())
                        .param("name", userData.get("name").toString())
                        .param("location", userData.get("location").toString()),
                200, "用户注册API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "用户注册响应验证");

        // 等待数据写入
        waitFor(100);

        // 验证数据库中用户记录
        verifyRecordCountWithDebug("users", "username = '" + newUserUsername + "'", 1, "用户注册数据库验证");

        logTestEnd("用户注册成功", true);
    }

    @Test
    @Order(2)
    @DisplayName("用户登录 - 成功案例")
    void testUserLogin_Success() throws Exception {
        logTestStart("用户登录成功", "测试用户登录获取认证token");

        // 检查前置条件
        checkPrecondition(newUserUsername, "newUserUsername", "用户登录测试");

        // 如果前一个测试失败，重新创建用户
        if (newUserUsername == null) {
            testUserRegistration_Success();
        }

        // 再次验证用户是否存在
        verifyRecordCountWithDebug("users", "username = '" + newUserUsername + "'", 1, "登录前用户存在验证");

        logInfo("尝试登录用户: " + newUserUsername);

        // 使用用户名登录
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(newUserUsername, "password123")
        );

        MvcResult result = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "用户登录API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "用户登录响应验证");
        newUserToken = extractDataFromResponse(response, "用户登录").asText();

        assertNotNullWithDebug(newUserToken, "登录token", "登录token验证");
        assertFalseWithDebug(newUserToken.isEmpty(), "登录token不应为空", "登录token非空验证");

        logInfo("用户登录成功，token: " + newUserToken.substring(0, Math.min(20, newUserToken.length())) + "...");
        logTestEnd("用户登录成功", true);
    }

    @Test
    @Order(3)
    @DisplayName("获取当前用户信息")
    void testGetCurrentUserInfo() throws Exception {
        logTestStart("获取当前用户信息", "测试获取登录用户的详细信息");

        // 检查前置条件
        checkPrecondition(newUserToken, "newUserToken", "获取用户信息测试");

        // 确保已登录
        if (newUserToken == null) {
            testUserLogin_Success();
        }

        MvcResult result = executeRequest(
                authenticatedGet("/api/users", newUserToken),
                200, "获取当前用户信息API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取用户信息响应验证");
        JsonNode userData = extractDataFromResponse(response, "获取用户信息");

        // 验证返回的用户信息
        assertEqualsWithDebug(newUserUsername, userData.get("username").asText(), "用户名", "用户名验证");
        assertEqualsWithDebug(newUserPhone, userData.get("telephone").asText(), "手机号", "手机号验证");
        assertEqualsWithDebug("New User", userData.get("name").asText(), "姓名", "姓名验证");
        assertEqualsWithDebug("Shanghai", userData.get("location").asText(), "地址", "地址验证");
        assertEqualsWithDebug(newUserEmail, userData.get("email").asText(), "邮箱", "邮箱验证");

        // 保存用户ID用于后续测试
        newUserId = userData.get("id").asLong();

        logInfo("获取用户信息成功，用户ID: " + newUserId);
        logTestEnd("获取当前用户信息", true);
    }

    @Test
    @Order(4)
    @DisplayName("用户注册 - 重复用户名")
    void testUserRegistration_DuplicateUsername() throws Exception {
        logTestStart("用户注册重复用户名", "测试使用已存在用户名注册应该失败");

        // 检查前置条件
        checkPrecondition(newUserUsername, "newUserUsername", "重复用户名注册测试");

        // 确保测试用户存在
        if (newUserUsername == null) {
            testUserRegistration_Success();
        }

        // 使用已存在的用户名注册
        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", newUserUsername)  // 使用已存在的用户名
                        .param("phone", generateUniquePhone())
                        .param("password", "password123")
                        .param("email", generateUniqueEmail())
                        .param("name", "Duplicate User")
                        .param("location", "Beijing"),
                409, "重复用户名注册API调用（应失败）"
        );

        logTestEnd("用户注册重复用户名", true);
    }

    @Test
    @Order(5)
    @DisplayName("用户注册 - 重复手机号")
    void testUserRegistration_DuplicatePhone() throws Exception {
        logTestStart("用户注册重复手机号", "测试使用已存在手机号注册应该失败");

        // 检查前置条件
        checkPrecondition(newUserPhone, "newUserPhone", "重复手机号注册测试");

        // 确保测试用户存在
        if (newUserPhone == null) {
            testUserRegistration_Success();
        }

        // 使用已存在的手机号注册
        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", generateUniqueUsername())
                        .param("phone", newUserPhone)  // 使用已存在的手机号
                        .param("password", "password123")
                        .param("email", generateUniqueEmail())
                        .param("name", "New User 2")
                        .param("location", "Guangzhou"),
                409, "重复手机号注册API调用（应失败）"
        );

        logTestEnd("用户注册重复手机号", true);
    }

    @Test
    @Order(6)
    @DisplayName("用户注册 - 无效参数")
    void testUserRegistration_InvalidParameters() throws Exception {
        logTestStart("用户注册无效参数", "测试使用无效参数注册应该失败");

        // 检查前置条件
        checkPrecondition(userToken, "userToken", "无效参数注册测试");

        // 测试空用户名
        logInfo("测试空用户名验证");
        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", "")
                        .param("phone", generateUniquePhone())
                        .param("password", "password123"),
                400, "空用户名注册API调用（应失败）"
        );

        // 测试无效手机号
        logInfo("测试无效手机号验证");
        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", generateUniqueUsername())
                        .param("phone", "invalid_phone")
                        .param("password", "password123"),
                400, "无效手机号注册API调用（应失败）"
        );

        logTestEnd("用户注册无效参数", true);
    }

    @Test
    @Order(7)
    @DisplayName("用户登录 - 错误密码")
    void testUserLogin_WrongPassword() throws Exception {
        logTestStart("用户登录错误密码", "测试使用错误密码登录应该失败");

        // 检查前置条件
        checkPrecondition(newUserUsername, "newUserUsername", "错误密码登录测试");

        if (newUserUsername == null) {
            testUserRegistration_Success();
        }

        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(newUserUsername, "wrongpassword")
        );

        executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                401, "错误密码登录API调用（应失败）"
        );

        logTestEnd("用户登录错误密码", true);
    }

    @Test
    @Order(8)
    @DisplayName("用户登录 - 不存在的用户")
    void testUserLogin_NonExistentUser() throws Exception {
        logTestStart("用户登录不存在用户", "测试登录不存在的用户应该失败");

        String nonExistentUsername = "nonexistent" + System.currentTimeMillis();
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(nonExistentUsername, "password123")
        );

        executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                404, "不存在用户登录API调用（应失败）"
        );

        logTestEnd("用户登录不存在用户", true);
    }

    @Test
    @Order(9)
    @DisplayName("获取指定用户简略信息")
    void testGetUserBriefInfo() throws Exception {
        logTestStart("获取用户简略信息", "测试获取指定用户的简略信息");

        // 检查前置条件
        checkPreconditions("获取用户简略信息",
                "newUserId", newUserId,
                "userToken", userToken);

        // 确保有用户ID和token
        if (newUserId == null || userToken == null) {
            testGetCurrentUserInfo();
        }

        MvcResult result = executeRequest(
                authenticatedGet("/api/users/" + newUserId, userToken),
                200, "获取用户简略信息API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取用户简略信息响应验证");
        JsonNode userData = extractDataFromResponse(response, "获取用户简略信息");

        // 验证返回的简略信息
        assertEqualsWithDebug(newUserUsername, userData.get("username").asText(), "用户名", "简略信息用户名验证");
        assertEqualsWithDebug("USER", userData.get("role").asText(), "用户角色", "简略信息角色验证");

        // 敏感信息不应该被返回
        assertFalseWithDebug(userData.has("telephone"), "不应包含手机号", "敏感信息隐藏验证");
        assertFalseWithDebug(userData.has("email"), "不应包含邮箱", "敏感信息隐藏验证");

        logTestEnd("获取用户简略信息", true);
    }

    @Test
    @Order(10)
    @DisplayName("更新用户信息 - 成功案例")
    @Commit
    void testUpdateUserInfo_Success() throws Exception {
        logTestStart("更新用户信息成功", "测试更新用户基本信息");

        // 检查前置条件
        checkPrecondition(newUserToken, "newUserToken", "更新用户信息测试");

        // 确保已登录
        if (newUserToken == null) {
            testUserLogin_Success();
        }

        String updatedUsername = "updateduser" + System.currentTimeMillis() % 1000000;
        String updatedPhone = generateUniquePhone();
        String updatedEmail = generateUniqueEmail();

        logInfo("更新用户信息 - 新用户名: " + updatedUsername + ", 新手机号: " + updatedPhone);

        MvcResult result = executeRequest(
                authenticatedPut("/api/users", newUserToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", updatedUsername)
                        .param("name", "Updated User Name")
                        .param("phone", updatedPhone)
                        .param("location", "Beijing Updated")
                        .param("email", updatedEmail),
                200, "更新用户信息API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "更新用户信息响应验证");

        // 等待数据库更新
        waitFor(100);

        // 验证更新后的信息
        MvcResult getUserResult = executeRequest(
                authenticatedGet("/api/users", newUserToken),
                200, "获取更新后用户信息API调用"
        );

        JsonNode response = verifyApiSuccessResponse(getUserResult, "获取更新后用户信息响应验证");
        JsonNode userData = extractDataFromResponse(response, "获取更新后用户信息");

        assertEqualsWithDebug(updatedUsername, userData.get("username").asText(), "更新后用户名", "用户名更新验证");
        assertEqualsWithDebug("Updated User Name", userData.get("name").asText(), "更新后姓名", "姓名更新验证");
        assertEqualsWithDebug(updatedPhone, userData.get("telephone").asText(), "更新后手机号", "手机号更新验证");
        assertEqualsWithDebug("Beijing Updated", userData.get("location").asText(), "更新后地址", "地址更新验证");
        assertEqualsWithDebug(updatedEmail, userData.get("email").asText(), "更新后邮箱", "邮箱更新验证");

        // 更新测试数据
        newUserUsername = updatedUsername;
        newUserPhone = updatedPhone;
        newUserEmail = updatedEmail;

        logTestEnd("更新用户信息成功", true);
    }

    @Test
    @Order(11)
    @DisplayName("更新用户信息 - 重复用户名")
    void testUpdateUserInfo_DuplicateUsername() throws Exception {
        logTestStart("更新用户信息重复用户名", "测试更新为已存在用户名应该失败");

        // 检查前置条件
        checkPrecondition(newUserToken, "newUserToken", "重复用户名更新测试");

        if (newUserToken == null) {
            testUpdateUserInfo_Success();
        }

        // 尝试使用基础测试中的用户名（如果存在）
        String existingUsername = getAllUsernames().stream()
                .filter(username -> username.startsWith("testuser") && !username.equals(newUserUsername))
                .findFirst()
                .orElse("testuser");

        int count = countRecords("users", "username = '" + existingUsername + "'");
        if (count == 0) {
            // 如果不存在其他用户，跳过此测试
            logInfo("跳过重复用户名测试：没有其他用户可用于测试");
            return;
        }

        executeRequest(
                authenticatedPut("/api/users", newUserToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", existingUsername),
                409, "重复用户名更新API调用（应失败）"
        );

        logTestEnd("更新用户信息重复用户名", true);
    }

    @Test
    @Order(12)
    @DisplayName("更新用户密码 - 成功案例")
    @Commit
    void testUpdatePassword_Success() throws Exception {
        logTestStart("更新用户密码成功", "测试用户修改密码功能");

        // 检查前置条件
        checkPreconditions("更新用户密码测试",
                "newUserToken", newUserToken,
                "newUserUsername", newUserUsername);

        if (newUserToken == null || newUserUsername == null) {
            testUpdateUserInfo_Success();
        }

        String passwordUpdateRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createPasswordUpdateRequest("password123", "newpassword456")
        );

        MvcResult result = executeRequest(
                authenticatedPatch("/api/users/password", newUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordUpdateRequest),
                200, "更新用户密码API调用"
        );

        // 验证API响应
        verifyApiSuccessResponse(result, "更新用户密码响应验证");

        // 等待密码更新
        waitFor(100);

        // 验证旧密码不能再登录
        String oldPasswordLogin = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(newUserUsername, "password123")
        );

        executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oldPasswordLogin),
                401, "旧密码登录API调用（应失败）"
        );

        // 验证新密码可以登录
        String newPasswordLogin = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(newUserUsername, "newpassword456")
        );

        MvcResult loginResult = executeRequest(
                post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPasswordLogin),
                200, "新密码登录API调用"
        );

        JsonNode response = verifyApiSuccessResponse(loginResult, "新密码登录响应验证");

        newUserToken = extractDataFromResponse(response, "新密码登录").asText();

        logTestEnd("更新用户密码成功", true);
    }

    @Test
    @Order(13)
    @DisplayName("更新用户密码 - 错误的当前密码")
    void testUpdatePassword_WrongCurrentPassword() throws Exception {
        logTestStart("更新密码错误当前密码", "测试使用错误当前密码修改密码应该失败");

        // 检查前置条件
        checkPrecondition(newUserToken, "newUserToken", "错误当前密码测试");

        if (newUserToken == null) {
            testUpdatePassword_Success();
        }

        String passwordUpdateRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createPasswordUpdateRequest("wrongoldpassword", "newpassword789")
        );

        executeRequest(
                authenticatedPatch("/api/users/password", newUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passwordUpdateRequest),
                401, "错误当前密码更新API调用（应失败）"
        );

        logTestEnd("更新密码错误当前密码", true);
    }

    @Test
    @Order(15)
    @DisplayName("无认证访问受保护接口")
    void testUnauthorizedAccess() throws Exception {
        logTestStart("无认证访问测试", "测试不带认证token访问受保护接口应该失败");

        // 不带token访问受保护接口
        executeRequest(
                get("/api/users"),
                401, "无token获取用户信息API调用（应失败）"
        );

        executeRequest(
                put("/api/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("name", "Test"),
                401, "无token更新用户信息API调用（应失败）"
        );

        executeRequest(
                patch("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"),
                401, "无token修改密码API调用（应失败）"
        );

        logTestEnd("无认证访问测试", true);
    }

    @Test
    @Order(16)
    @DisplayName("无效token访问")
    void testInvalidTokenAccess() throws Exception {
        logTestStart("无效token访问测试", "测试使用无效token访问受保护接口应该失败");

        String invalidToken = "invalid.jwt.token";

        executeRequest(
                get("/api/users")
                        .header("Authorization", "Bearer " + invalidToken),
                401, "无效token访问API调用（应失败）"
        );

        logTestEnd("无效token访问测试", true);
    }

    @Test
    @Order(17)
    @DisplayName("批量用户操作压力测试")
    @Commit
    void testBatchUserOperations() throws Exception {
        logTestStart("批量用户操作测试", "测试批量创建用户的性能和稳定性");

        int batchSize = 3; // 减少批量大小以避免测试时间过长
        int initialUserCount = countRecords("users");
        logInfo("初始用户数量: " + initialUserCount);

        // 批量注册用户
        for (int i = 0; i < batchSize; i++) {
            Map<String, Object> userData = TestDataBuilder.createRandomUser();

            MvcResult result = executeRequest(
                    post("/api/users/register")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .param("username", userData.get("username").toString())
                            .param("phone", userData.get("phone").toString())
                            .param("password", userData.get("password").toString())
                            .param("email", userData.get("email").toString())
                            .param("name", userData.get("name").toString())
                            .param("location", userData.get("location").toString()),
                    200, "批量创建用户 " + (i + 1) + " API调用"
            );

            verifyApiSuccessResponse(result, "批量创建用户 " + (i + 1) + " 响应验证");
            logInfo("成功创建批量用户 " + (i + 1) + ": " + userData.get("username"));
        }

        // 等待数据写入
        waitFor(200);

        // 验证数据库中的用户数量
        int totalUsers = countRecords("users");
        logInfo("最终用户数量: " + totalUsers + ", 预期: " + (batchSize + initialUserCount));

        assertTrueWithDebug(totalUsers >= batchSize + initialUserCount,
                "应该创建了足够的用户记录", "批量用户创建验证");

        logTestEnd("批量用户操作测试", true);
    }

    @Test
    @Order(18)
    @DisplayName("用户信息边界值测试")
    void testUserInfoBoundaryValues() throws Exception {
        logTestStart("用户信息边界值测试", "测试用户信息的边界值处理");

        // 检查前置条件
        checkPrecondition(userToken, "userToken", "边界值测试");

        // 测试超长用户名（应该失败）
        logInfo("测试超长用户名验证");
        String tooLongUsername = "toolong" + System.currentTimeMillis() +
                new String(new char[500]).replace("\0", "a");

        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", tooLongUsername)
                        .param("phone", generateUniquePhone())
                        .param("password", "password123"),
                400, "超长用户名注册API调用（应失败）"
        );

        // 测试超长姓名
        logInfo("测试超长姓名验证");
        String tooLongName = new String(new char[200]).replace("\0", "N");
        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", generateUniqueUsername())
                        .param("phone", generateUniquePhone())
                        .param("password", "password123")
                        .param("name", tooLongName),
                400, "超长姓名注册API调用（应失败）"
        );

        // 测试无效邮箱格式
        logInfo("测试无效邮箱格式验证");
        executeRequest(
                post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", generateUniqueUsername())
                        .param("phone", generateUniquePhone())
                        .param("password", "password123")
                        .param("email", "invalid-email"),
                400, "无效邮箱格式注册API调用（应失败）"
        );

        logTestEnd("用户信息边界值测试", true);
    }

    @Test
    @Order(19)
    @DisplayName("用户数据完整性验证")
    void testUserDataIntegrity() throws Exception {
        logTestStart("用户数据完整性验证", "验证用户数据的一致性和完整性");

        // 检查前置条件
        checkPrecondition(newUserToken, "newUserToken", "用户数据完整性验证");

        // 确保有有效的token
        if (newUserToken == null) {
            testUserLogin_Success();
        }

        // 验证用户数据的一致性和完整性
        MvcResult result = executeRequest(
                authenticatedGet("/api/users", newUserToken),
                200, "获取用户信息API调用"
        );

        // 验证API响应
        JsonNode response = verifyApiSuccessResponse(result, "获取用户信息响应验证");
        JsonNode userData = extractDataFromResponse(response, "获取用户信息");

        // 验证所有必需字段都存在
        assertNotNullWithDebug(userData.get("id"), "用户ID", "ID存在验证");
        assertNotNullWithDebug(userData.get("username"), "用户名", "用户名存在验证");
        assertNotNullWithDebug(userData.get("telephone"), "手机号", "手机号存在验证");
        assertNotNullWithDebug(userData.get("role"), "用户角色", "角色存在验证");
        assertNotNullWithDebug(userData.get("regTime"), "创建时间", "创建时间存在验证");

        // 验证数据格式
        assertTrueWithDebug(userData.get("telephone").asText().matches("^1[3-9]\\d{9}$"),
                "手机号格式应该正确", "手机号格式验证");
        assertTrueWithDebug(userData.get("email").asText().contains("@"),
                "邮箱格式应该正确", "邮箱格式验证");
        assertEqualsWithDebug("USER", userData.get("role").asText(),
                "用户角色", "角色类型验证");

        logInfo("用户数据完整性验证通过: " + userData);
        logTestEnd("用户数据完整性验证", true);
    }
}