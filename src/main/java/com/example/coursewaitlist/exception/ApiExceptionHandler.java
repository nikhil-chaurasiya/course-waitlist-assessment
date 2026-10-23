package com.example.coursewaitlist.exception;

import com.example.coursewaitlist.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RegistrationService.CourseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(RuntimeException ex) { return Map.of("error", ex.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidRequest(MethodArgumentNotValidException ex) {
        return Map.of("error", "Invalid request: provide a non-blank name/learnerId and a positive capacity.");
    }
}
