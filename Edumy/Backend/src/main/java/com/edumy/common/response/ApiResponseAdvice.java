package com.edumy.common.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

/**
 * Automatically wraps all module controller responses into the standardized
 * ApiResponse envelope while preserving external webhook contracts (SePay/VNPay IPN).
 */
@RestControllerAdvice(basePackages = "com.edumy.modules")
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        String path = request.getURI().getPath();

        // Preserve raw format for external payment gateway webhooks (SePay, Casso, VNPay IPN)
        // and static binary/resource endpoints
        if (path != null && (path.contains("/webhook/") || path.contains("/vnpay-ipn") || path.contains("/ipn") || path.startsWith("/uploads/"))) {
            return body;
        }

        if (body instanceof ApiResponse<?> || body instanceof Resource || body instanceof byte[]) {
            return body;
        }

        int status = 200;
        if (response instanceof ServletServerHttpResponse servletResponse && servletResponse.getServletResponse() != null) {
            status = servletResponse.getServletResponse().getStatus();
        }

        boolean isSuccess = status >= 200 && status < 400;
        String message = isSuccess ? "OK" : "Request failed";

        if (body instanceof Map<?, ?> mapBody) {
            Object msgObj = mapBody.get("message");
            if (msgObj != null) {
                message = msgObj.toString();
            }
            if (!isSuccess) {
                Object errObj = mapBody.containsKey("errors") ? mapBody.get("errors") : mapBody.get("error");
                return ApiResponse.error(message, errObj);
            }
        } else if (body instanceof String strBody) {
            if (!isSuccess) {
                message = strBody;
            }
            ApiResponse<Object> envelope = isSuccess
                    ? ApiResponse.ok(message, strBody)
                    : ApiResponse.error(message, null);
            if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
                try {
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return objectMapper.writeValueAsString(envelope);
                } catch (Exception e) {
                    return strBody;
                }
            }
            return envelope;
        }

        if (!isSuccess) {
            return ApiResponse.error(message, body);
        }

        return ApiResponse.ok(message, body);
    }
}
