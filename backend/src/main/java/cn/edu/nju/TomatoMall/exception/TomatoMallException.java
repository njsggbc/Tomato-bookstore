package cn.edu.nju.TomatoMall.exception;

import lombok.Getter;

@Getter
public class TomatoMallException extends RuntimeException{

    private final int status;

    public TomatoMallException(int status, String message){
        super(message); this.status = status;
    }

    public static TomatoMallException phoneAlreadyExists(){
        return new TomatoMallException(409, "手机号已经存在!");
    }

    public static TomatoMallException emailAlreadyExists(){
        return new TomatoMallException(409, "邮箱已经存在!");
    }

    public static TomatoMallException usernameAlreadyExists(){
        return new TomatoMallException(409, "用户名已经存在!");
    }

    public static TomatoMallException userNotFound(){
        return new TomatoMallException(404, "用户不存在!");
    }

    public static TomatoMallException passwordError(){
        return new TomatoMallException(401, "密码错误!");
    }

    public static TomatoMallException notLogin(String pathInfo){
        return new TomatoMallException(401, "未登录!: " + pathInfo);
    }

    public static TomatoMallException phoneOrPasswordError(){
        return new TomatoMallException(401, "手机号或密码错误!");
    }

    public static TomatoMallException storeNameAlreadyExists(){
        return new TomatoMallException(409, "店铺名已经存在!");
    }

    public static TomatoMallException storeNotFound(){
        return new TomatoMallException(404, "店铺不存在!");
    }

    public static TomatoMallException tokenInvalid() {
        return new TomatoMallException(403, "无效token!");
    }

    public static TomatoMallException storeStaffAlreadyExists() {
        return new TomatoMallException(409, "店员已经存在!");
    }

    public static TomatoMallException permissionDenied() {
        return new TomatoMallException(403, "无权限!");
    }

    public static TomatoMallException invalidOperation() {
        return new TomatoMallException(400, "无效操作!");
    }

    public static TomatoMallException productNotFound() {
        return new TomatoMallException(404, "商品不存在!");
    }

    public static TomatoMallException fileUploadFail() {
        return new TomatoMallException(500, "文件上传失败!");
    }

    public static TomatoMallException unexpectedError() {
        return new TomatoMallException(500, "意外错误!");
    }
}
