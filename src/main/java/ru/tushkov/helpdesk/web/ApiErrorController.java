package ru.tushkov.helpdesk.web;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class ApiErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiError> error(HttpServletRequest request) {
        int statusCode = statusCode(request);
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message =
                switch (statusCode) {
                    case 404 -> "Ресурс не найден";
                    case 400 -> "Некорректный запрос";
                    case 405 -> "Метод не поддерживается";
                    default -> status.is5xxServerError() ? "Внутренняя ошибка сервера" : status.getReasonPhrase();
                };
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path == null ? request.getRequestURI() : path.toString(),
                List.of());
        return ResponseEntity.status(status).body(body);
    }

    private static int statusCode(HttpServletRequest request) {
        Object value = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (value instanceof Integer status) {
            return status;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
