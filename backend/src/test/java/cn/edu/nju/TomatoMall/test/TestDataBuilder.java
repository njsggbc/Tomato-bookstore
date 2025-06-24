package cn.edu.nju.TomatoMall.test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 测试数据构建工具类
 * 提供各种测试数据的构建方法
 */
public class TestDataBuilder {

    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 用户相关 ====================

    /**
     * 创建用户注册请求
     */
    public static Map<String, Object> createUserRequest(String username, String phone, String password,
                                                        String email, String name, String location) {
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("phone", phone);
        request.put("password", password);
        request.put("email", email);
        request.put("name", name);
        request.put("location", location);
        return request;
    }

    /**
     * 创建用户登录请求
     */
    public static Map<String, Object> createLoginRequest(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        return request;
    }

    /**
     * 创建密码更新请求
     */
    public static Map<String, Object> createPasswordUpdateRequest(String currentPassword, String newPassword) {
        Map<String, Object> request = new HashMap<>();
        request.put("currentPassword", currentPassword);
        request.put("newPassword", newPassword);
        return request;
    }

    /**
     * 创建随机用户数据
     */
    public static Map<String, Object> createRandomUser() {
        String suffix = String.valueOf(random.nextInt(10000));
        return createUserRequest(
                "user" + suffix,
                "138" + String.format("%08d", random.nextInt(100000000)),
                "password123",
                "user" + suffix + "@test.com",
                "Test User " + suffix,
                "Test Location " + suffix
        );
    }

    // ==================== 商店相关 ====================

    /**
     * 创建商店注册请求参数
     */
    public static Map<String, String> createStoreParams(String name, String address, String description) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("address", address);
        params.put("description", description);
        return params;
    }

    /**
     * 创建随机商店数据
     */
    public static Map<String, String> createRandomStore() {
        String suffix = String.valueOf(random.nextInt(10000));
        return createStoreParams(
                "书店" + suffix,
                "测试地址" + suffix + "号",
                "这是一家专业的书店，提供各类优质图书 " + suffix
        );
    }

    /**
     * 创建员工授权Token请求
     */
    public static Map<String, Object> createTokenRequest(String name, String expireTime) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        if (expireTime != null) {
            request.put("expireTime", expireTime);
        }
        return request;
    }

    /**
     * 创建商店审核请求
     */
    public static Map<String, Object> createStoreReviewRequest(boolean pass, String comment) {
        Map<String, Object> request = new HashMap<>();
        request.put("pass", pass);
        request.put("comment", comment);
        return request;
    }

    // ==================== 商品相关 ====================

    /**
     * 创建商品请求参数
     */
    public static Map<String, String> createProductParams(String title, String description,
                                                          String price, Long storeId) {
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("description", description);
        params.put("price", price);
        params.put("storeId", storeId.toString());
        // 添加规格参数
        params.put("specifications.author", "测试作者");
        params.put("specifications.publisher", "测试出版社");
        params.put("specifications.isbn", generateISBN());
        params.put("specifications.pages", String.valueOf(200 + random.nextInt(800)));
        return params;
    }

    /**
     * 创建随机图书商品
     */
    public static Map<String, String> createRandomBook(Long storeId) {
        String[] bookTitles = {
                "Java编程思想", "Spring Boot实战", "算法导论", "设计模式", "重构改善既有代码的设计",
                "深入理解JVM虚拟机", "Effective Java", "Spring Cloud微服务", "MySQL技术内幕", "Redis设计与实现"
        };
        String[] authors = {
                "Bruce Eckel", "Craig Walls", "Thomas H. Cormen", "Erich Gamma", "Martin Fowler",
                "周志明", "Joshua Bloch", "翟永超", "姜承尧", "黄健宏"
        };

        int index = random.nextInt(bookTitles.length);
        String suffix = String.valueOf(random.nextInt(1000));

        return createProductParams(
                bookTitles[index] + " " + suffix,
                "这是一本优秀的技术书籍，详细介绍了相关技术的核心概念和实践方法。",
                String.valueOf(29.99 + random.nextDouble() * 100),
                storeId
        );
    }

    /**
     * 创建库存调整请求
     */
    public static Map<String, Integer> createInventoryRequest(int quantity) {
        Map<String, Integer> request = new HashMap<>();
        request.put("stockpile", quantity);
        return request;
    }

    /**
     * 生成随机ISBN
     */
    private static String generateISBN() {
        return "978-7-" + String.format("%06d", random.nextInt(1000000)) + "-" + random.nextInt(10);
    }

    // ==================== 购物相关 ====================

    /**
     * 创建购物车添加请求
     */
    public static Map<String, Object> createCartAddRequest(Long productId, int quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("productId", productId);
        request.put("quantity", quantity);
        return request;
    }

    /**
     * 创建订单提交请求
     */
    public static Map<String, Object> createSubmitOrderRequest(List<Long> cartItemIds,
                                                               String recipientName, String recipientPhone,
                                                               String recipientAddress, Map<String, String> storeRemarks) {
        Map<String, Object> request = new HashMap<>();
        request.put("cartItemIds", cartItemIds);
        request.put("recipientName", recipientName);
        request.put("recipientPhone", recipientPhone);
        request.put("recipientAddress", recipientAddress);
        if (storeRemarks != null) {
            request.put("storeRemarks", storeRemarks);
        }
        return request;
    }

    /**
     * 创建发货请求
     */
    public static Map<String, Object> createShipRequest(String trackingNo, String shippingCompany,
                                                        String senderName, String senderPhone, String senderAddress) {
        Map<String, Object> request = new HashMap<>();
        request.put("trackingNo", trackingNo);
        request.put("shippingCompany", shippingCompany);
        request.put("senderName", senderName);
        request.put("senderPhone", senderPhone);
        request.put("senderAddress", senderAddress);
        return request;
    }

    /**
     * 创建物流更新请求
     */
    public static Map<String, Object> createShippingUpdateRequest(String logMessage, String logTime,
                                                                  String location, String operatorName) {
        Map<String, Object> request = new HashMap<>();
        request.put("logMessage", logMessage);
        request.put("logTime", logTime);
        request.put("location", location);
        request.put("operatorName", operatorName);
        return request;
    }

    /**
     * 创建送达确认请求
     */
    public static Map<String, Object> createDeliveryConfirmRequest(String deliveryTime, String deliveryLocation,
                                                                   String signedBy, String phone, String remark) {
        Map<String, Object> request = new HashMap<>();
        request.put("deliveryTime", deliveryTime);
        request.put("deliveryLocation", deliveryLocation);
        request.put("signedBy", signedBy);
        request.put("phone", phone);
        if (remark != null) {
            request.put("remark", remark);
        }
        return request;
    }

    // ==================== 广告相关 ====================

    /**
     * 创建广告位请求
     */
    public static Map<String, Object> createAdSpaceRequest(String label, String type, Integer cycleInDay, Integer segmentInHour) {
        Map<String, Object> request = new HashMap<>();
        request.put("label", label);
        request.put("type", type);
        if (cycleInDay != null) {
            request.put("cycleInDay", cycleInDay);
        }
        if (segmentInHour != null) {
            request.put("segmentInHour", segmentInHour);
        }
        return request;
    }

    /**
     * 创建广告投放请求
     */
    public static Map<String, Object> createAdPlacementRequest(Long adId, Long adSpaceId, List<Long> adSlotIds) {
        Map<String, Object> request = new HashMap<>();
        request.put("adId", adId);
        request.put("adSpaceId", adSpaceId);
        request.put("adSlotIds", adSlotIds);
        return request;
    }

    /**
     * 创建广告位状态更新请求
     */
    public static Map<String, Object> createAdSlotStatusRequest(List<Long> slotIds, Boolean available, Boolean active) {
        Map<String, Object> request = new HashMap<>();
        request.put("slotIds", slotIds);
        if (available != null) {
            request.put("available", available);
        }
        if (active != null) {
            request.put("active", active);
        }
        return request;
    }

    // ==================== 评论相关 ====================

    /**
     * 创建评论请求
     */
    public static Map<String, Object> createCommentRequest(String content, Integer rating) {
        Map<String, Object> request = new HashMap<>();
        request.put("content", content);
        if (rating != null) {
            request.put("rating", rating);
        }
        return request;
    }

    /**
     * 创建评论更新请求
     */
    public static Map<String, Object> createCommentUpdateRequest(String content, Integer rating) {
        Map<String, Object> request = new HashMap<>();
        if (content != null) {
            request.put("content", content);
        }
        if (rating != null) {
            request.put("rating", rating);
        }
        return request;
    }

    /**
     * 创建随机评论内容
     */
    public static String createRandomComment() {
        String[] comments = {
                "这本书非常好，内容详实，推荐购买！",
                "书的质量很好，物流也很快，满意！",
                "内容很专业，对学习很有帮助。",
                "书籍包装完好，内容也符合期望。",
                "作者写得很用心，值得一读。",
                "这本书帮助我解决了很多实际问题。",
                "印刷质量不错，内容也很充实。",
                "性价比很高的一本书，推荐给大家。"
        };
        return comments[random.nextInt(comments.length)];
    }

    // ==================== 工具方法 ====================

    /**
     * 生成当前时间字符串
     */
    public static String getCurrentTimeString() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }

    /**
     * 生成未来时间字符串
     */
    public static String getFutureTimeString(int daysLater) {
        return LocalDateTime.now().plusDays(daysLater).toString();
    }

    /**
     * 生成随机手机号
     */
    public static String generateRandomPhone() {
        return "138" + String.format("%08d", random.nextInt(100000000));
    }

    /**
     * 生成随机邮箱
     */
    public static String generateRandomEmail() {
        return "test" + random.nextInt(10000) + "@example.com";
    }

    /**
     * 生成随机地址
     */
    public static String generateRandomAddress() {
        String[] provinces = {"北京市", "上海市", "广东省", "浙江省", "江苏省"};
        String[] cities = {"朝阳区", "浦东新区", "天河区", "西湖区", "玄武区"};
        String[] streets = {"中山路", "解放路", "人民路", "建设路", "文化路"};

        return provinces[random.nextInt(provinces.length)] +
                cities[random.nextInt(cities.length)] +
                streets[random.nextInt(streets.length)] +
                (random.nextInt(999) + 1) + "号";
    }

    /**
     * 生成随机物流单号
     */
    public static String generateTrackingNumber() {
        return "YTO" + System.currentTimeMillis() + random.nextInt(1000);
    }

    /**
     * 生成随机价格
     */
    public static BigDecimal generateRandomPrice() {
        return BigDecimal.valueOf(10 + random.nextDouble() * 200).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 生成随机数量
     */
    public static int generateRandomQuantity() {
        return 1 + random.nextInt(10);
    }

    /**
     * 创建分页参数
     */
    public static Map<String, String> createPageParams(int page, int size, String field, boolean order) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        params.put("field", field);
        params.put("order", String.valueOf(order));
        return params;
    }

    /**
     * 创建默认分页参数
     */
    public static Map<String, String> createDefaultPageParams() {
        return createPageParams(0, 10, "id", true);
    }
}