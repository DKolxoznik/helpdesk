package ru.tushkov.helpdesk.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldViolation> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(
                        error.getField(),
                        error.getDefaultMessage() == null ? "Некорректное значение" : error.getDefaultMessage()))
                .toList();
        return respond(HttpStatus.BAD_REQUEST, "Проверьте поля запроса", request, details);
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ApiError> notFound(TicketNotFoundException exception, HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler({InvalidStatusTransitionException.class, TicketNotEditableException.class})
    public ResponseEntity<ApiError> conflict(RuntimeException exception, HttpServletRequest request) {
        return respond(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler({
        InvalidReportPeriodException.class,
        CategoryNotFoundException.class,
        InvalidPageRequestException.class,
        ConstraintViolationException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> badRequest(Exception exception, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, badRequestMessage(exception), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Необработанная ошибка {}", request.getRequestURI(), exception);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", request, List.of());
    }

    private static String badRequestMessage(Exception exception) {
        if (exception instanceof MissingServletRequestParameterException missing) {
            return "Не указан обязательный параметр " + missing.getParameterName();
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            return "Некорректное значение параметра " + mismatch.getName();
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return "Некорректное тело запроса";
        }
        return exception.getMessage() == null ? "Некорректный запрос" : exception.getMessage();
    }

    private ResponseEntity<ApiError> respond(
            HttpStatus status, String message, HttpServletRequest request, List<FieldViolation> details) {
        ApiError body = new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
