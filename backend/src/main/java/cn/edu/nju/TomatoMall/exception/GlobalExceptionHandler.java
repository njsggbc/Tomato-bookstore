package cn.edu.nju.TomatoMall.exception;

import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = TomatoMallException.class)
    public ResponseEntity<ApiResponse<String>> handleAIExternalException(TomatoMallException e) {
        if (e.getStatus() == 500) {
            e.printStackTrace();
        }
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.failure(e.getCode(), e.getMessage()));
    }
}
