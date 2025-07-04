package cn.edu.nju.TomatoMall.models.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiResponse<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;

    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(0, null, result);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(0, null, null);
    }

    public static <T> ApiResponse<T> failure(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}