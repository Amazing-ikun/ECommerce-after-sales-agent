package com.pu.ecommerceaftersalesagent.controller;

import com.pu.ecommerceaftersalesagent.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException exception) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException exception) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "请求格式错误，请检查 JSON 内容", Instant.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception exception) {
        log.error("请求处理失败", exception);
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "服务暂时不可用，请稍后重试", Instant.now());
    }
}
