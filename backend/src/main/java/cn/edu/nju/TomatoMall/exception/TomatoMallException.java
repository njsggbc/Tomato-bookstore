package cn.edu.nju.TomatoMall.exception;

import lombok.Getter;

@Getter
public class TomatoMallException extends RuntimeException{

    private final int status;

    public TomatoMallException(int status, String message){
        super(message); this.status = status;
    }

    public static TomatoMallException phoneAlreadyExists() {
        return new TomatoMallException(409, "手机号已经存在!");
    }

    public static TomatoMallException phoneAlreadyExists(String message) {
        return new TomatoMallException(409, message);
    }

    public static TomatoMallException emailAlreadyExists() {
        return new TomatoMallException(409, "邮箱已经存在!");
    }

    public static TomatoMallException emailAlreadyExists(String message) {
        return new TomatoMallException(409, message);
    }

    public static TomatoMallException usernameAlreadyExists() {
        return new TomatoMallException(409, "用户名已经存在!");
    }

    public static TomatoMallException usernameAlreadyExists(String message) {
        return new TomatoMallException(409, message);
    }

    public static TomatoMallException userNotFound() {
        return new TomatoMallException(404, "用户不存在!");
    }

    public static TomatoMallException userNotFound(String message) {
        return new TomatoMallException(404, message);
    }

    public static TomatoMallException passwordError() {
        return new TomatoMallException(401, "密码错误!");
    }

    public static TomatoMallException passwordError(String message) {
        return new TomatoMallException(401, message);
    }

    public static TomatoMallException notLogin(String pathInfo) {
        return new TomatoMallException(401, "未登录!: " + pathInfo);
    }

    public static TomatoMallException notLogin(String pathInfo, String message) {
        return new TomatoMallException(401, message + ": " + pathInfo);
    }

    public static TomatoMallException phoneOrPasswordError() {
        return new TomatoMallException(401, "手机号或密码错误!");
    }

    public static TomatoMallException phoneOrPasswordError(String message) {
        return new TomatoMallException(401, message);
    }

    public static TomatoMallException storeNameAlreadyExists() {
        return new TomatoMallException(409, "店铺名已经存在!");
    }

    public static TomatoMallException storeNameAlreadyExists(String message) {
        return new TomatoMallException(409, message);
    }

    public static TomatoMallException storeNotFound() {
        return new TomatoMallException(404, "店铺不存在!");
    }

    public static TomatoMallException storeNotFound(String message) {
        return new TomatoMallException(404, message);
    }

    public static TomatoMallException tokenInvalid() {
        return new TomatoMallException(403, "无效token!");
    }

    public static TomatoMallException tokenInvalid(String message) {
        return new TomatoMallException(403, message);
    }

    public static TomatoMallException storeStaffAlreadyExists() {
        return new TomatoMallException(409, "店员已经存在!");
    }

    public static TomatoMallException storeStaffAlreadyExists(String message) {
        return new TomatoMallException(409, message);
    }

    public static TomatoMallException permissionDenied() {
        return new TomatoMallException(403, "无权限!");
    }

    public static TomatoMallException permissionDenied(String message) {
        return new TomatoMallException(403, message);
    }

    public static TomatoMallException invalidOperation() {
        return new TomatoMallException(400, "无效操作!");
    }

    public static TomatoMallException invalidOperation(String message) {
        return new TomatoMallException(400, message);
    }

    public static TomatoMallException productNotFound() {
        return new TomatoMallException(404, "商品不存在!");
    }

    public static TomatoMallException productNotFound(String message) {
        return new TomatoMallException(404, message);
    }

    public static TomatoMallException fileUploadFail() {
        return new TomatoMallException(500, "文件上传失败!");
    }

    public static TomatoMallException fileUploadFail(String message) {
        return new TomatoMallException(500, message);
    }

    public static TomatoMallException unexpectedError() {
        return new TomatoMallException(500, "意外错误!");
    }

    public static TomatoMallException unexpectedError(String message) {
        return new TomatoMallException(500, message);
    }

    public static TomatoMallException noValidAddress() {
        return new TomatoMallException(400, "没有有效的地址!");
    }

    public static TomatoMallException noValidAddress(String message) {
        return new TomatoMallException(400, message);
    }

    public static TomatoMallException invalidOrderItem() {
        return new TomatoMallException(400, "无效的订单项!");
    }

    public static TomatoMallException invalidOrderItem(String message) {
        return new TomatoMallException(400, message);
    }

    public static TomatoMallException insufficientStock() {
        return new TomatoMallException(400, "商品库存不足!");
    }

    public static TomatoMallException insufficientStock(String message) {
        return new TomatoMallException(400, message);
    }

    public static TomatoMallException productInOrder() {
        return new TomatoMallException(400, "商品仍有订单未完成!");
    }

    public static TomatoMallException productInOrder(String message) {
        return new TomatoMallException(400, message);
    }

    public static TomatoMallException orderNotFound() {
        return new TomatoMallException(404, "订单不存在!");
    }

    public static TomatoMallException orderNotFound(String message) {
        return new TomatoMallException(404, message);
    }

    public static TomatoMallException paymentNotFound() {
        return new TomatoMallException(404, "支付记录不存在!");
    }

    public static TomatoMallException paymentNotFound(String message) {
        return new TomatoMallException(404, message);
    }

    public static TomatoMallException paymentFail() {
        return new TomatoMallException(500, "支付失败!");
    }

    public static TomatoMallException paymentFail(String message) {
        return new TomatoMallException(500, message);
    }

    public static TomatoMallException refundFail() {
        return new TomatoMallException(500, "退款失败!");
    }

    public static TomatoMallException refundFail(String message) {
        return new TomatoMallException(500, message);
    }

    public static TomatoMallException operationFail() {
        return new TomatoMallException(500, "操作失败!");
    }

    public static TomatoMallException operationFail(String message) {
        return new TomatoMallException(500, message);
    }

    public static TomatoMallException invalidParameter() {
        return new TomatoMallException(400, "无效参数!");
    }
}
