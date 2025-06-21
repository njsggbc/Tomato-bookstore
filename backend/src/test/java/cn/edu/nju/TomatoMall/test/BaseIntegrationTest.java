package cn.edu.nju.TomatoMall.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.annotation.Commit;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 集成测试基础类
 * 提供通用的测试配置和工具方法
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected DataSource dataSource;

    protected MockMvc mockMvc;

    // 测试用户的令牌缓存
    protected String userToken;
    protected String adminToken;
    protected String managerToken;
    protected String staffToken;

    // 测试数据ID缓存
    protected Long testUserId;
    protected Long testAdminId;
    protected Long testStoreId;
    protected Long testProductId;

    // 用于生成唯一测试数据的计数器
    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 100000);

    // 保存实际创建的用户名
    private String actualUsername;

    @PostConstruct
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 等待数据库就绪
        waitForDatabase();

        setupTestData();
    }

    /**
     * 等待数据库就绪
     */
    protected void waitForDatabase() {
        for (int i = 0; i < 30; i++) {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeQuery("SELECT COUNT(*) FROM users");
                logInfo("数据库连接成功");
                return;
            } catch (Exception e) {
                logInfo("等待数据库就绪，尝试次数: " + (i + 1));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw new RuntimeException("数据库连接失败，超过最大重试次数");
    }

    /**
     * 初始化测试数据
     */
    protected void setupTestData() throws Exception {
        createTestUsers();
        loginTestUsers();
    }

    /**
     * 创建测试用户 - 使用唯一数据避免冲突
     */
    @Commit
    protected void createTestUsers() throws Exception {
        // 生成唯一的用户数据
        long id = counter.incrementAndGet();
        String username = "testuser" + id;
        String phone = "138" + String.format("%08d", id % 100000000L);
        String email = "test" + id + "@example.com";

        logInfo("创建测试用户: " + username + ", 手机号: " + phone);

        // 创建普通用户
        MvcResult userResult = performWithDebug(post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", username)
                        .param("phone", phone)
                        .param("password", "password123")
                        .param("email", email)
                        .param("name", "Test User")
                        .param("location", "Test Location"),
                "创建测试用户");

        // 验证用户是否真的被创建
        verifyUserCreated(username);

        // 将实际使用的用户名保存起来，供loginTestUsers使用
        this.actualUsername = username;

        logSuccess("测试用户创建成功: " + username);
    }

    /**
     * 验证用户是否被成功创建
     */
    private void verifyUserCreated(String username) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery("SELECT COUNT(*) as count FROM users WHERE username = '" + username + "'");
            if (rs.next()) {
                int count = rs.getInt("count");
                logInfo("数据库中用户 " + username + " 的记录数: " + count);
                if (count == 0) {
                    throw new RuntimeException("用户创建失败，数据库中没有找到记录");
                }
            }
        }
    }

    /**
     * 登录测试用户获取令牌
     */
    protected void loginTestUsers() throws Exception {
        if (actualUsername == null) {
            logWarning("actualUsername 为空，跳过登录测试");
            return;
        }

        logInfo("尝试登录用户: " + actualUsername);

        // 登录普通用户（使用实际创建的用户名）
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(actualUsername, "password123"));

        MvcResult userLoginResult = performWithExpectedStatus(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "用户登录");

        userToken = extractTokenFromResponse(userLoginResult);
        logSuccess("用户登录成功，token: " + (userToken != null ? userToken.substring(0, Math.min(20, userToken.length())) + "..." : "null"));

        // 尝试登录系统管理员
        tryLoginSystemAdmin();
    }

    /**
     * 尝试登录系统管理员
     */
    protected void tryLoginSystemAdmin() {
        // 常见的系统管理员凭据
        String[][] adminCredentials = {
                {"root", "123456"},
                {"admin", "admin"},
                {"admin", "password"},
                {"root", "password"}
        };

        for (String[] credential : adminCredentials) {
            try {
                String adminLoginRequest = objectMapper.writeValueAsString(
                        TestDataBuilder.createLoginRequest(credential[0], credential[1]));

                MvcResult adminLoginResult = performWithExpectedStatus(post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminLoginRequest),
                        200, "管理员登录-" + credential[0]);

                adminToken = extractTokenFromResponse(adminLoginResult);
                logSuccess("成功登录系统管理员: " + credential[0]);
                return;

            } catch (Exception e) {
                logInfo("管理员登录失败: " + credential[0] + ", 尝试下一个");
            }
        }

        // 如果没有系统管理员，创建一个测试管理员
        try {
            createTestAdmin();
        } catch (Exception e) {
            logError("无法创建测试管理员: " + e.getMessage());
        }
    }

    /**
     * 创建测试管理员
     */
    @Commit
    protected void createTestAdmin() throws Exception {
        long id = counter.incrementAndGet();
        String adminUsername = "admin" + id;
        String adminPhone = "139" + String.format("%08d", id % 100000000L);
        String adminEmail = "admin" + id + "@test.com";

        // 先创建普通用户
        performWithExpectedStatus(post("/api/users/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("username", adminUsername)
                        .param("phone", adminPhone)
                        .param("password", "password123")
                        .param("email", adminEmail)
                        .param("name", "Test Admin")
                        .param("location", "Admin Location"),
                200, "创建管理员用户");

        // 直接在数据库中更新为管理员角色
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(
                    "UPDATE users SET role = 'ADMIN' WHERE username = '" + adminUsername + "'");
            connection.commit();
        }

        // 登录管理员
        String loginRequest = objectMapper.writeValueAsString(
                TestDataBuilder.createLoginRequest(adminUsername, "password123"));

        MvcResult loginResult = performWithExpectedStatus(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest),
                200, "管理员登录");

        adminToken = extractTokenFromResponse(loginResult);
        logSuccess("创建并登录测试管理员: " + adminUsername);
    }

    /**
     * 从响应中提取令牌
     */
    protected String extractTokenFromResponse(MvcResult result) throws Exception {
        String responseContent = getResponseContent(result);
        logInfo("登录响应内容: " + responseContent);
        TestResponse response = objectMapper.readValue(responseContent, TestResponse.class);
        return response.getData().toString();
    }

    /**
     * 获取响应内容（处理编码问题）
     */
    protected String getResponseContent(MvcResult result) throws Exception {
        byte[] contentBytes = result.getResponse().getContentAsByteArray();
        return new String(contentBytes, StandardCharsets.UTF_8);
    }

    /**
     * 带认证的GET请求
     */
    protected MockHttpServletRequestBuilder authenticatedGet(String url, String token) {
        return get(url).header("Authorization", "Bearer " + token);
    }

    /**
     * 带认证的POST请求
     */
    protected MockHttpServletRequestBuilder authenticatedPost(String url, String token) {
        return post(url).header("Authorization", "Bearer " + token);
    }

    /**
     * 带认证的PUT请求
     */
    protected MockHttpServletRequestBuilder authenticatedPut(String url, String token) {
        return put(url).header("Authorization", "Bearer " + token);
    }

    /**
     * 带认证的PATCH请求
     */
    protected MockHttpServletRequestBuilder authenticatedPatch(String url, String token) {
        return patch(url).header("Authorization", "Bearer " + token);
    }

    /**
     * 带认证的DELETE请求
     */
    protected MockHttpServletRequestBuilder authenticatedDelete(String url, String token) {
        return delete(url).header("Authorization", "Bearer " + token);
    }

    /**
     * 验证API响应结构
     */
    protected void verifySuccessResponse(MvcResult result) throws Exception {
        String content = getResponseContent(result);
        logInfo("验证成功响应: " + content);

        try {
            TestResponse response = objectMapper.readValue(content, TestResponse.class);
            if (response.getCode() != 0) {
                String errorMsg = String.format("API调用失败 - 错误码: %d, 错误信息: %s, 完整响应: %s",
                        response.getCode(), response.getMsg(), content);
                logError(errorMsg);
                fail(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = String.format("响应JSON解析失败: %s, 原始响应: %s", e.getMessage(), content);
            logError(errorMsg);
            fail(errorMsg);
        }
    }

    /**
     * 验证API错误响应
     */
    protected void verifyErrorResponse(MvcResult result, int expectedCode) throws Exception {
        String content = getResponseContent(result);
        try {
            TestResponse response = objectMapper.readValue(content, TestResponse.class);
            if (response.getCode() != expectedCode) {
                String errorMsg = String.format("错误码验证失败 - 期望: %d, 实际: %d, 响应: %s",
                        expectedCode, response.getCode(), content);
                logError(errorMsg);
                fail(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = String.format("错误响应解析失败: %s, 原始响应: %s", e.getMessage(), content);
            logError(errorMsg);
            fail(errorMsg);
        }
    }

    /**
     * 等待指定时间（用于异步操作测试）
     */
    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 验证数据库记录数量
     */
    protected int countRecords(String tableName) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            var resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM " + tableName);
            resultSet.next();
            int count = resultSet.getInt("count");
            logInfo("表 " + tableName + " 的记录数: " + count);
            return count;
        }
    }

    /**
     * 验证数据库记录数量（带条件）
     */
    protected int countRecords(String tableName, String condition) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String sql = "SELECT COUNT(*) as count FROM " + tableName + " WHERE " + condition;
            logInfo("执行查询: " + sql);
            var resultSet = statement.executeQuery(sql);
            resultSet.next();
            int count = resultSet.getInt("count");
            logInfo("查询结果: " + count + " 条记录");
            return count;
        }
    }

    /**
     * 获取所有用户名
     */
    protected List<String> getAllUsernames() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery("SELECT username FROM users");
            List<String> usernames = new java.util.ArrayList<>();
            while (rs.next()) {
                usernames.add(rs.getString("username"));
            }
            return usernames;
        }
    }

    /**
     * 调试用：打印数据库中的所有用户
     */
    protected void printAllUsers() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery("SELECT id, username, phone, email, role FROM users");
            logInfo("=== 数据库中的所有用户 ===");
            while (rs.next()) {
                logInfo("ID: " + rs.getLong("id") +
                        ", Username: " + rs.getString("username") +
                        ", Phone: " + rs.getString("phone") +
                        ", Email: " + rs.getString("email") +
                        ", Role: " + rs.getString("role"));
            }
            logInfo("=========================");
        }
    }

    /**
     * 生成唯一标识符（供子类使用）
     */
    protected String generateUniqueId() {
        return String.valueOf(counter.incrementAndGet());
    }

    /**
     * 生成唯一用户名（供子类使用）
     */
    protected String generateUniqueUsername() {
        return "user" + counter.incrementAndGet();
    }

    /**
     * 生成唯一手机号（供子类使用）
     */
    protected String generateUniquePhone() {
        return "138" + String.format("%08d", counter.incrementAndGet() % 100000000L);
    }

    /**
     * 生成唯一邮箱（供子类使用）
     */
    protected String generateUniqueEmail() {
        return "test" + counter.incrementAndGet() + "@test.com";
    }

    /**
     * 执行请求并处理调试信息的通用方法
     */
    protected MvcResult performWithDebug(MockHttpServletRequestBuilder requestBuilder, String testName) throws Exception {
        logTestStart(testName);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        int statusCode = result.getResponse().getStatus();
        String responseContent = getResponseContent(result);

        logInfo("HTTP状态码: " + statusCode);
        logInfo("响应内容: " + responseContent);

        if (statusCode >= 400) {
            logError("请求失败详情:");
            logError("测试名称: " + testName);
            logError("请求URL: " + getRequestUrl(requestBuilder));
            logError("响应状态: " + statusCode);
            logError("响应体: " + responseContent);

            // 打印响应头
            logError("响应头:");
            for (String headerName : result.getResponse().getHeaderNames()) {
                logError("  " + headerName + ": " + result.getResponse().getHeader(headerName));
            }

            // 尝试解析错误响应
            try {
                JsonNode errorResponse = objectMapper.readTree(responseContent);
                if (errorResponse.has("msg")) {
                    logError("错误信息: " + errorResponse.get("msg").asText());
                }
                if (errorResponse.has("code")) {
                    logError("错误代码: " + errorResponse.get("code").asInt());
                }
            } catch (Exception e) {
                logError("无法解析错误响应JSON: " + e.getMessage());
            }
        } else {
            logSuccess("请求成功");
        }

        return result;
    }

    /**
     * 验证成功响应并在失败时打印详细信息
     */
    protected void verifySuccessWithDebug(MvcResult result, String testName) throws Exception {
        int statusCode = result.getResponse().getStatus();
        String responseContent = getResponseContent(result);

        if (statusCode != 200) {
            String errorMsg = String.format("%s失败 - 状态码: %d, 响应: %s", testName, statusCode, responseContent);
            logError(errorMsg);
            fail(errorMsg);
        }

        try {
            verifySuccessResponse(result);
            logSuccess(testName + " 响应验证通过");
        } catch (Exception e) {
            String errorMsg = String.format("%s响应验证失败 - %s, 响应: %s", testName, e.getMessage(), responseContent);
            logError(errorMsg);
            fail(errorMsg);
        }
    }

    /**
     * 验证数据库记录数量并在失败时打印详细信息
     */
    protected void verifyRecordCountWithDebug(String tableName, String condition, int expectedCount, String testName) throws Exception {
        int actualCount = countRecords(tableName, condition);
        logInfo("数据库验证 - 表: " + tableName + ", 条件: " + condition + ", 实际数量: " + actualCount + ", 期望数量: " + expectedCount);

        if (actualCount != expectedCount) {
            logError("数据库记录数量验证失败:");
            logError("测试: " + testName);
            logError("表名: " + tableName);
            logError("查询条件: " + condition);
            logError("期望数量: " + expectedCount);
            logError("实际数量: " + actualCount);

            // 打印相关表的所有记录用于调试
            printTableRecords(tableName);

            fail(String.format("%s - 数据库记录数量验证失败，期望: %d，实际: %d", testName, expectedCount, actualCount));
        }
    }

    /**
     * 验证JSON字段并在失败时打印详细信息
     */
    protected void verifyJsonFieldWithDebug(JsonNode jsonNode, String fieldPath, Object expectedValue, String testName) throws Exception {
        JsonNode fieldNode = getJsonField(jsonNode, fieldPath);

        if (fieldNode == null || fieldNode.isNull()) {
            String errorMsg = String.format("%s - JSON字段验证失败，字段 '%s' 不存在或为null，完整JSON: %s",
                    testName, fieldPath, jsonNode.toString());
            logError(errorMsg);
            fail(errorMsg);
        }

        String actualValue = fieldNode.asText();
        String expectedStr = expectedValue.toString();

        logInfo("JSON字段验证 - 字段: " + fieldPath + ", 期望: " + expectedStr + ", 实际: " + actualValue);

        if (!expectedStr.equals(actualValue)) {
            String errorMsg = String.format("%s - JSON字段验证失败，字段 '%s' 期望: %s，实际: %s，完整JSON: %s",
                    testName, fieldPath, expectedStr, actualValue, jsonNode.toString());
            logError(errorMsg);
            fail(errorMsg);
        }
    }

    /**
     * 获取JSON嵌套字段
     */
    private JsonNode getJsonField(JsonNode root, String fieldPath) {
        String[] paths = fieldPath.split("\\.");
        JsonNode current = root;

        for (String path : paths) {
            if (current == null || !current.has(path)) {
                return null;
            }
            current = current.get(path);
        }

        return current;
    }

    /**
     * 打印表记录用于调试
     */
    protected void printTableRecords(String tableName) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName + " LIMIT 10");
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            logInfo("=== 表 " + tableName + " 的记录 (最多10条) ===");

            // 打印列名
            StringBuilder header = new StringBuilder("| ");
            for (int i = 1; i <= columnCount; i++) {
                header.append(metaData.getColumnName(i)).append(" | ");
            }
            logInfo(header.toString());

            // 打印分隔线
            StringBuilder separator = new StringBuilder("| ");
            for (int i = 1; i <= columnCount; i++) {
                separator.append("--- | ");
            }
            logInfo(separator.toString());

            // 打印数据行
            int rowCount = 0;
            while (rs.next() && rowCount < 10) {
                StringBuilder row = new StringBuilder("| ");
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    row.append((value != null ? value.toString() : "NULL")).append(" | ");
                }
                logInfo(row.toString());
                rowCount++;
            }

            logInfo("=== 共 " + rowCount + " 条记录 ===");

        } catch (Exception e) {
            logError("打印表记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取请求URL（用于调试）
     */
    private String getRequestUrl(MockHttpServletRequestBuilder requestBuilder) {
        try {
            return requestBuilder.toString();
        } catch (Exception e) {
            return "无法获取请求URL";
        }
    }

    /**
     * 执行带期望状态码的请求
     */
    protected MvcResult performWithExpectedStatus(MockHttpServletRequestBuilder requestBuilder,
                                                  int expectedStatus, String testName) throws Exception {
        MvcResult result = performWithDebug(requestBuilder, testName);

        int actualStatus = result.getResponse().getStatus();
        if (actualStatus != expectedStatus) {
            String errorMsg = String.format("%s状态码验证失败 - 期望: %d，实际: %d", testName, expectedStatus, actualStatus);
            logError(errorMsg);
            fail(errorMsg);
        }

        logSuccess(testName + " 状态码验证通过: " + actualStatus);
        return result;
    }

    /**
     * 安全的JSON解析（避免解析异常）
     */
    protected JsonNode safeParseJson(String responseContent, String testName) throws Exception {
        try {
            return objectMapper.readTree(responseContent);
        } catch (Exception e) {
            String errorMsg = String.format("%s - JSON解析失败: %s, 原始响应: %s", testName, e.getMessage(), responseContent);
            logError(errorMsg);
            fail(errorMsg);
            return null; // 永远不会执行到这里
        }
    }

    /**
     * 验证API错误响应（增强版）
     */
    protected void verifyErrorResponseWithDebug(MvcResult result, int expectedCode, String testName) throws Exception {
        String content = getResponseContent(result);
        int statusCode = result.getResponse().getStatus();

        logInfo("验证错误响应 - 测试: " + testName + ", 状态码: " + statusCode + ", 内容: " + content);

        try {
            TestResponse response = objectMapper.readValue(content, TestResponse.class);
            if (response.getCode() != expectedCode) {
                String errorMsg = String.format("%s错误码验证失败，期望: %d，实际: %d，响应: %s",
                        testName, expectedCode, response.getCode(), content);
                logError(errorMsg);
                fail(errorMsg);
            }
            logSuccess(testName + " 错误响应验证通过");
        } catch (Exception e) {
            String errorMsg = String.format("%s错误响应解析失败: %s，原始响应: %s", testName, e.getMessage(), content);
            logError(errorMsg);
            fail(errorMsg);
        }
    }

    /**
     * 带重试的数据库验证（用于异步操作）
     */
    protected void verifyRecordCountWithRetry(String tableName, String condition, int expectedCount,
                                              String testName, int maxRetries, long retryIntervalMs) throws Exception {
        int actualCount = -1;
        Exception lastException = null;

        for (int retry = 0; retry <= maxRetries; retry++) {
            try {
                actualCount = countRecords(tableName, condition);
                if (actualCount == expectedCount) {
                    logSuccess(testName + " 数据库验证通过 (第" + (retry + 1) + "次尝试)");
                    return;
                }

                if (retry < maxRetries) {
                    logInfo(testName + " 数据库验证未通过，等待重试... (第" + (retry + 1) + "次，期望: " + expectedCount + "，实际: " + actualCount + ")");
                    Thread.sleep(retryIntervalMs);
                }
            } catch (Exception e) {
                lastException = e;
                if (retry < maxRetries) {
                    logWarning(testName + " 数据库查询异常，等待重试: " + e.getMessage());
                    Thread.sleep(retryIntervalMs);
                }
            }
        }

        // 所有重试都失败了
        String errorMsg = String.format("%s数据库验证失败 (已重试%d次)，期望: %d，实际: %d",
                testName, maxRetries + 1, expectedCount, actualCount);
        logError(errorMsg);

        if (lastException != null) {
            logError("最后一次异常: " + lastException.getMessage());
        }

        printTableRecords(tableName);
        fail(errorMsg);
    }

    /**
     * 检查前置条件并提供友好的错误信息
     */
    protected void checkPrecondition(Object value, String valueName, String testName) {
        if (value == null) {
            String errorMsg = String.format("%s前置条件检查失败 - %s为null，请确保前面的测试已成功执行", testName, valueName);
            logError(errorMsg);
            fail(errorMsg);
        }
        logInfo(testName + " 前置条件检查通过: " + valueName);
    }

    /**
     * 批量检查前置条件
     */
    protected void checkPreconditions(String testName, Object... nameValuePairs) {
        if (nameValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("参数必须成对出现：名称, 值, 名称, 值...");
        }

        for (int i = 0; i < nameValuePairs.length; i += 2) {
            String name = (String) nameValuePairs[i];
            Object value = nameValuePairs[i + 1];
            checkPrecondition(value, name, testName);
        }
    }

    /**
     * 测试开始前的标准化日志
     */
    protected void logTestStart(String testName) {
        logInfo("🚀 开始测试: " + testName);
    }

    /**
     * 测试开始前的标准化日志（带描述）
     */
    protected void logTestStart(String testName, String description) {
        logInfo("\n" + new String(new char[50]).replace("\0", "="));
        logInfo("🚀 开始测试: " + testName);
        logInfo("📝 描述: " + description);
        logInfo("⏰ 时间: " + new java.util.Date());
        logInfo(new String(new char[50]).replace("\0", "="));
    }

    /**
     * 测试结束的标准化日志
     */
    protected void logTestEnd(String testName, boolean success) {
        String status = success ? "✅ 通过" : "❌ 失败";
        logInfo("🏁 测试结束: " + testName + " - " + status);
        logInfo("⏰ 时间: " + new java.util.Date());
    }

    /**
     * 日志输出方法 - 信息级别
     */
    protected void logInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    /**
     * 日志输出方法 - 成功级别
     */
    protected void logSuccess(String message) {
        System.out.println("[SUCCESS] ✅ " + message);
    }

    /**
     * 日志输出方法 - 警告级别
     */
    protected void logWarning(String message) {
        System.out.println("[WARNING] ⚠️ " + message);
    }

    /**
     * 日志输出方法 - 错误级别
     */
    protected void logError(String message) {
        System.err.println("[ERROR] ❌ " + message);
    }

    /**
     * 断言相等（带详细错误信息）
     */
    protected void assertEqualsWithDebug(Object expected, Object actual, String fieldName, String testName) {
        if (!java.util.Objects.equals(expected, actual)) {
            String errorMsg = String.format("%s - 字段验证失败: %s, 期望: %s, 实际: %s",
                    testName, fieldName, expected, actual);
            logError(errorMsg);
            fail(errorMsg);
        }
        logInfo(testName + " - 字段验证通过: " + fieldName + " = " + actual);
    }

    /**
     * 断言不为空（带详细错误信息）
     */
    protected void assertNotNullWithDebug(Object object, String fieldName, String testName) {
        if (object == null) {
            String errorMsg = String.format("%s - 字段不能为空: %s", testName, fieldName);
            logError(errorMsg);
            fail(errorMsg);
        }
        logInfo(testName + " - 字段非空验证通过: " + fieldName);
    }

    /**
     * 断言为真（带详细错误信息）
     */
    protected void assertTrueWithDebug(boolean condition, String message, String testName) {
        if (!condition) {
            String errorMsg = String.format("%s - 条件验证失败: %s", testName, message);
            logError(errorMsg);
            fail(errorMsg);
        }
        logInfo(testName + " - 条件验证通过: " + message);
    }

    /**
     * 断言为假（带详细错误信息）
     */
    protected void assertFalseWithDebug(boolean condition, String message, String testName) {
        if (condition) {
            String errorMsg = String.format("%s - 条件验证失败（期望为false）: %s", testName, message);
            logError(errorMsg);
            fail(errorMsg);
        }
        logInfo(testName + " - 条件验证通过（为false）: " + message);
    }

    /**
     * 执行HTTP请求的统一方法（带完整调试信息）
     */
    protected MvcResult executeRequest(MockHttpServletRequestBuilder requestBuilder,
                                       int expectedStatus, String testName) throws Exception {
        logTestStart(testName);

        try {
            MvcResult result = mockMvc.perform(requestBuilder).andReturn();

            int actualStatus = result.getResponse().getStatus();
            String responseContent = getResponseContent(result);

            // 记录请求信息
            logInfo("请求方法: " + requestBuilder.toString());
            logInfo("期望状态码: " + expectedStatus);
            logInfo("实际状态码: " + actualStatus);
            logInfo("响应内容: " + responseContent);

            // 验证状态码
            if (actualStatus != expectedStatus) {
                String errorMsg = String.format("%s - 状态码验证失败，期望: %d，实际: %d，响应: %s",
                        testName, expectedStatus, actualStatus, responseContent);
                logError(errorMsg);

                // 打印更多调试信息
                logError("请求头信息:");
                Collections.list(result.getRequest().getHeaderNames()).forEach(headerName ->
                        logError("  " + headerName + ": " + result.getRequest().getHeader(headerName)));

                logError("响应头信息:");
                for (String headerName : result.getResponse().getHeaderNames()) {
                    logError("  " + headerName + ": " + result.getResponse().getHeader(headerName));
                }

                fail(errorMsg);
            }

            logSuccess(testName + " - HTTP请求执行成功");
            return result;

        } catch (Exception e) {
            String errorMsg = String.format("%s - HTTP请求执行异常: %s", testName, e.getMessage());
            logError(errorMsg);
            throw e;
        }
    }

    /**
     * 解析并验证JSON响应
     */
    protected JsonNode parseAndVerifyJsonResponse(MvcResult result, String testName) throws Exception {
        String responseContent = getResponseContent(result);

        try {
            JsonNode jsonNode = objectMapper.readTree(responseContent);
            logInfo(testName + " - JSON解析成功");
            return jsonNode;
        } catch (Exception e) {
            String errorMsg = String.format("%s - JSON解析失败: %s, 原始响应: %s",
                    testName, e.getMessage(), responseContent);
            logError(errorMsg);
            fail(errorMsg);
            return null;
        }
    }

    /**
     * 验证API成功响应的完整方法
     */
    protected JsonNode verifyApiSuccessResponse(MvcResult result, String testName) throws Exception {
        JsonNode response = parseAndVerifyJsonResponse(result, testName);

        if (!response.has("code")) {
            String errorMsg = testName + " - 响应缺少code字段: " + response.toString();
            logError(errorMsg);
            fail(errorMsg);
        }

        int code = response.get("code").asInt();
        if (code != 0) {
            String msg = response.has("msg") ? response.get("msg").asText() : "无错误信息";
            String errorMsg = String.format("%s - API调用失败，错误码: %d，错误信息: %s，完整响应: %s",
                    testName, code, msg, response.toString());
            logError(errorMsg);
            fail(errorMsg);
        }

        logSuccess(testName + " - API响应验证通过");
        return response;
    }

    /**
     * 从API响应中提取数据字段
     */
    protected JsonNode extractDataFromResponse(JsonNode response, String testName) throws Exception {
        if (!response.has("data")) {
            String errorMsg = testName + " - 响应缺少data字段: " + response.toString();
            logError(errorMsg);
            fail(errorMsg);
        }

        JsonNode data = response.get("data");
        logInfo(testName + " - 成功提取data字段");
        return data;
    }

    /**
     * 验证分页响应结构
     */
    protected JsonNode verifyPageResponse(JsonNode data, String testName) throws Exception {
        String[] requiredFields = {"content", "pageable", "totalElements", "totalPages", "size", "number"};

        for (String field : requiredFields) {
            if (!data.has(field)) {
                String errorMsg = String.format("%s - 分页响应缺少字段: %s，完整响应: %s",
                        testName, field, data.toString());
                logError(errorMsg);
                fail(errorMsg);
            }
        }

        JsonNode content = data.get("content");
        if (!content.isArray()) {
            String errorMsg = testName + " - content字段不是数组类型: " + data.toString();
            logError(errorMsg);
            fail(errorMsg);
        }

        logSuccess(testName + " - 分页响应结构验证通过，内容数量: " + content.size());
        return content;
    }

    /**
     * 执行数据库操作的安全方法
     */
    protected <T> T executeDatabaseOperation(String testName, DatabaseOperation<T> operation) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            T result = operation.execute(connection);
            logInfo(testName + " - 数据库操作执行成功");
            return result;
        } catch (Exception e) {
            String errorMsg = String.format("%s - 数据库操作失败: %s", testName, e.getMessage());
            logError(errorMsg);
            throw e;
        }
    }

    /**
     * 数据库操作接口
     */
    @FunctionalInterface
    protected interface DatabaseOperation<T> {
        T execute(Connection connection) throws Exception;
    }

    /**
     * 清理测试数据的方法
     */
    protected void cleanupTestData(String testName) {
        try {
            logInfo(testName + " - 开始清理测试数据");

            // 这里可以添加具体的清理逻辑
            // 例如删除测试过程中创建的数据

            logSuccess(testName + " - 测试数据清理完成");
        } catch (Exception e) {
            logWarning(testName + " - 测试数据清理失败: " + e.getMessage());
        }
    }

    /**
     * 创建multipart请求的辅助方法
     */
    protected MockMultipartHttpServletRequestBuilder createMultipartRequest(String url, String method) {
        switch (method.toUpperCase()) {
            case "POST":
                return multipart(url);
            case "PUT":
                return (MockMultipartHttpServletRequestBuilder) multipart(url).with(request -> {
                    request.setMethod("PUT");
                    return request;
                });
            case "PATCH":
                return (MockMultipartHttpServletRequestBuilder) multipart(url).with(request -> {
                    request.setMethod("PATCH");
                    return request;
                });
            default:
                throw new IllegalArgumentException("不支持的multipart方法: " + method);
        }
    }

    /**
     * 验证字符串包含指定内容
     */
    protected void assertContainsWithDebug(String actual, String expected, String fieldName, String testName) {
        if (actual == null || !actual.contains(expected)) {
            String errorMsg = String.format("%s - 字段包含验证失败: %s, 实际值: %s, 期望包含: %s",
                    testName, fieldName, actual, expected);
            logError(errorMsg);
            fail(errorMsg);
        }
        logInfo(testName + " - 字段包含验证通过: " + fieldName);
    }

    /**
     * 验证列表不为空
     */
    protected void assertListNotEmptyWithDebug(JsonNode listNode, String listName, String testName) {
        if (listNode == null || !listNode.isArray() || listNode.size() == 0) {
            String errorMsg = String.format("%s - 列表为空验证失败: %s, 实际: %s",
                    testName, listName, listNode);
            logError(errorMsg);
            fail(errorMsg);
        }
        logInfo(testName + " - 列表非空验证通过: " + listName + ", 大小: " + listNode.size());
    }

    /**
     * 通用响应类
     */
    @Setter
    @Getter
    public static class TestResponse {
        private int code;
        private String msg;
        private Object data;
    }
}