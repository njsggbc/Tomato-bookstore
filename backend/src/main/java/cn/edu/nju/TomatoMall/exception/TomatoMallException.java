package cn.edu.nju.TomatoMall.exception;

import lombok.Getter;
import lombok.ToString;

/**
 * Custom exception class for TomatoMall application, encapsulating HTTP status codes and error codes.
 * Provides factory methods for creating specific exception instances with consistent status and code.
 */
@Getter
@ToString
public class TomatoMallException extends RuntimeException {

    private final int status;
    private final int code;

    /**
     * Constructs an exception with the specified HTTP status, error code, and message.
     *
     * @param status  HTTP status code (e.g., 400, 404, 500)
     * @param code    Application-specific error code
     * @param message Error message
     */
    public TomatoMallException(int status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    // Global Exceptions (100–199)
    public static TomatoMallException unexpectedError() {
        return new TomatoMallException(500, 100, "意外错误!");
    }

    public static TomatoMallException unexpectedError(String message) {
        return new TomatoMallException(500, 100, message);
    }

    public static TomatoMallException operationFail() {
        return new TomatoMallException(500, 101, "操作失败!");
    }

    public static TomatoMallException operationFail(String message) {
        return new TomatoMallException(500, 101, message);
    }

    public static TomatoMallException pathOrParamError() {
        return new TomatoMallException(400, 102, "路径或传参错误！");
    }

    public static TomatoMallException invalidOperation() {
        return new TomatoMallException(400, 103, "无效操作!");
    }

    public static TomatoMallException invalidOperation(String message) {
        return new TomatoMallException(400, 103, message);
    }

    public static TomatoMallException invalidParameter() {
        return new TomatoMallException(400, 104, "无效参数!");
    }

    public static TomatoMallException invalidParameter(String message) {
        return new TomatoMallException(400, 104, message);
    }

    public static TomatoMallException fileUploadFail() {
        return new TomatoMallException(500, 105, "文件上传失败!");
    }

    public static TomatoMallException fileUploadFail(String message) {
        return new TomatoMallException(500, 105, message);
    }

    // User-related Exceptions (200–299)
    public static TomatoMallException phoneAlreadyExists() {
        return new TomatoMallException(409, 200, "手机号已经存在!");
    }

    public static TomatoMallException phoneAlreadyExists(String message) {
        return new TomatoMallException(409, 200, message);
    }

    public static TomatoMallException emailAlreadyExists() {
        return new TomatoMallException(409, 201, "邮箱已经存在!");
    }

    public static TomatoMallException emailAlreadyExists(String message) {
        return new TomatoMallException(409, 201, message);
    }

    public static TomatoMallException usernameAlreadyExists() {
        return new TomatoMallException(409, 202, "用户名已经存在!");
    }

    public static TomatoMallException usernameAlreadyExists(String message) {
        return new TomatoMallException(409, 202, message);
    }

    public static TomatoMallException userNotFound() {
        return new TomatoMallException(404, 203, "用户不存在!");
    }

    public static TomatoMallException userNotFound(String message) {
        return new TomatoMallException(404, 203, message);
    }

    public static TomatoMallException passwordError() {
        return new TomatoMallException(401, 204, "密码错误!");
    }

    public static TomatoMallException passwordError(String message) {
        return new TomatoMallException(401, 204, message);
    }

    public static TomatoMallException phoneOrPasswordError() {
        return new TomatoMallException(401, 205, "手机号或密码错误!");
    }

    public static TomatoMallException phoneOrPasswordError(String message) {
        return new TomatoMallException(401, 205, message);
    }

    public static TomatoMallException notLogin(String pathInfo) {
        return new TomatoMallException(401, 206, "未登录!: " + pathInfo);
    }

    public static TomatoMallException notLogin(String pathInfo, String message) {
        return new TomatoMallException(401, 206, message + ": " + pathInfo);
    }

    // Store-related Exceptions (300–399)
    public static TomatoMallException storeNameAlreadyExists() {
        return new TomatoMallException(409, 300, "店铺名已经存在!");
    }

    public static TomatoMallException storeNameAlreadyExists(String message) {
        return new TomatoMallException(409, 300, message);
    }

    public static TomatoMallException storeStaffAlreadyExists() {
        return new TomatoMallException(409, 301, "店员已经存在!");
    }

    public static TomatoMallException storeStaffAlreadyExists(String message) {
        return new TomatoMallException(409, 301, message);
    }

    public static TomatoMallException storeNotFound() {
        return new TomatoMallException(404, 302, "店铺不存在!");
    }

    public static TomatoMallException storeNotFound(String message) {
        return new TomatoMallException(404, 302, message);
    }

    public static TomatoMallException tokenInvalid() {
        return new TomatoMallException(403, 303, "无效token!");
    }

    public static TomatoMallException tokenInvalid(String message) {
        return new TomatoMallException(403, 303, message);
    }

    public static TomatoMallException permissionDenied() {
        return new TomatoMallException(403, 304, "无权限!");
    }

    public static TomatoMallException permissionDenied(String message) {
        return new TomatoMallException(403, 304, message);
    }

    // Product-related Exceptions (400–499)
    public static TomatoMallException productNotFound() {
        return new TomatoMallException(404, 400, "商品不存在!");
    }

    public static TomatoMallException productNotFound(String message) {
        return new TomatoMallException(404, 400, message);
    }

    public static TomatoMallException insufficientStock() {
        return new TomatoMallException(400, 401, "商品库存不足!");
    }

    public static TomatoMallException insufficientStock(String message) {
        return new TomatoMallException(400, 401, message);
    }

    public static TomatoMallException productInOrder() {
        return new TomatoMallException(400, 402, "商品仍有订单未完成!");
    }

    public static TomatoMallException productInOrder(String message) {
        return new TomatoMallException(400, 402, message);
    }

    // Order-related Exceptions (500–599)
    public static TomatoMallException orderNotFound() {
        return new TomatoMallException(404, 500, "订单不存在!");
    }

    public static TomatoMallException orderNotFound(String message) {
        return new TomatoMallException(404, 500, message);
    }

    public static TomatoMallException invalidOrderItem() {
        return new TomatoMallException(400, 501, "无效的订单项!");
    }

    public static TomatoMallException invalidOrderItem(String message) {
        return new TomatoMallException(400, 501, message);
    }

    public static TomatoMallException invalidCartItem() {
        return new TomatoMallException(400, 502, "无效的购物车项!");
    }

    public static TomatoMallException invalidCartItem(String message) {
        return new TomatoMallException(400, 502, message);
    }

    public static TomatoMallException paymentNotFound() {
        return new TomatoMallException(404, 503, "支付记录不存在!");
    }

    public static TomatoMallException paymentNotFound(String message) {
        return new TomatoMallException(404, 503, message);
    }

    public static TomatoMallException paymentFail() {
        return new TomatoMallException(500, 504, "支付失败!");
    }

    public static TomatoMallException paymentFail(String message) {
        return new TomatoMallException(500, 504, message);
    }

    public static TomatoMallException refundFail() {
        return new TomatoMallException(500, 505, "退款失败!");
    }

    public static TomatoMallException refundFail(String message) {
        return new TomatoMallException(500, 505, message);
    }

    public static TomatoMallException noValidAddress() {
        return new TomatoMallException(400, 506, "没有有效的地址!");
    }

    public static TomatoMallException noValidAddress(String message) {
        return new TomatoMallException(400, 506, message);
    }

    public static TomatoMallException shipmentRecordNotFound() {
        return new TomatoMallException(404, 507, "物流记录不存在!");
    }

    public static TomatoMallException shipmentRecordNotFound(String message) {
        return new TomatoMallException(404, 507, message);
    }

    // Advertisement-related Exceptions (600–699)
    public static TomatoMallException advertisementNotFound() {
        return new TomatoMallException(404, 600, "广告不存在!");
    }

    public static TomatoMallException advertisementNotFound(String message) {
        return new TomatoMallException(404, 600, message);
    }

    public static TomatoMallException adSpaceNotFound() {
        return new TomatoMallException(404, 601, "广告位不存在!");
    }

    public static TomatoMallException adSpaceNotFound(String message) {
        return new TomatoMallException(404, 601, message);
    }

    public static TomatoMallException labelAlreadyExists() {
        return new TomatoMallException(409, 602, "标签已经存在!");
    }

    public static TomatoMallException labelAlreadyExists(String message) {
        return new TomatoMallException(409, 602, message);
    }

    public static  TomatoMallException adPlacementNotFound() {
        return new TomatoMallException(404, 603, "广告投放不存在!");
    }

    public static TomatoMallException adPlacementNotFound(String message) {
        return new TomatoMallException(404, 603, message);
    }

    // Message-related Exceptions (700–799)
    public static TomatoMallException messageNotFound() {
        return new TomatoMallException(404, 700, "消息不存在!");
    }

    public static TomatoMallException messageNotFound(String message) {
        return new TomatoMallException(404, 700, message);
    }

    public static TomatoMallException messageTypeNotSupported() {
        return new TomatoMallException(400, 701, "不支持的消息类型!");
    }

    public static TomatoMallException messageTypeNotSupported(String message) {
        return new TomatoMallException(400, 701, message);
    }

    // Comment-related Exceptions (800–899)
    public static TomatoMallException commentNotFound() {
        return new TomatoMallException(404, 800, "评论不存在!");
    }

    public static TomatoMallException commentNotFound(String message) {
        return new TomatoMallException(404, 800, message);
    }
}