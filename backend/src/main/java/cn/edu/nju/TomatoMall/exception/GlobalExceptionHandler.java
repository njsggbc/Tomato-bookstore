package cn.edu.nju.TomatoMall.exception;

import cn.edu.nju.TomatoMall.models.vo.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(value = TomatoMallException.class)
    public ApiResponse<String> handleAIExternalException(TomatoMallException e) {
        return ApiResponse.failure(e.getStatus(), e.getMessage());
    }
}
