package me.rgunny.marketpulse.common.aspect;

import me.rgunny.marketpulse.common.response.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String packageName = returnType.getDeclaringClass().getPackageName();
        return !packageName.contains("springdoc") &&
                !packageName.contains("actuator") &&
                !packageName.contains("swagger");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 이미 Result 타입이면 메타데이터만 추가
        if (body instanceof Result<?> result) {
            response.setStatusCode(result.httpStatus());
            return new ApiResponse<>(result, generateMetadata(request));
        }

        // Result 타입이 아닌 경우 Success로 감싸기
        var successResult = Result.success(body);
        response.setStatusCode(successResult.httpStatus());
        return new ApiResponse<>(successResult, generateMetadata(request));
    }

    private ResponseMetadata generateMetadata(ServerHttpRequest request) {
        return new ResponseMetadata(
                LocalDateTime.now(),
                request.getURI().getPath(),
                request.getMethod().name()
        );
    }

    private record ApiResponse<T>(
            Result<T> result,
            ResponseMetadata metadata
    ) {}

    private record ResponseMetadata(
            LocalDateTime timestamp,
            String path,
            String method
    ) {}
}
