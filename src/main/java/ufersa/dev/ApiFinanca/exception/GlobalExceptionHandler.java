package ufersa.dev.ApiFinanca.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import ufersa.dev.ApiFinanca.dto.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais inválidas",
                "Email ou senha incorretos",
                path
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Erro de autenticação",
                ex.getMessage() != null ? ex.getMessage() : "Falha na autenticação",
                path
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                ex.getMessage(),
                path
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Estado inválido",
                ex.getMessage(),
                path
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage() != null ? ex.getMessage() : "O recurso solicitado não foi encontrado",
                path
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        
        // Verifica se é uma exceção de "não encontrado"
        if (ex.getMessage() != null && ex.getMessage().contains("não encontrado")) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Recurso não encontrado",
                    ex.getMessage(),
                    path
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                ex.getMessage() != null ? ex.getMessage() : "Ocorreu um erro inesperado",
                path
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatusCode().value(),
                ex.getReason() != null ? ex.getReason() : "Erro na requisição",
                ex.getReason() != null ? ex.getReason() : ex.getMessage(),
                path
        );
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erro de validação")
                .message("Dados de entrada inválidos")
                .path(path)
                .details(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erro de validação")
                .message("Dados de entrada inválidos")
                .path(path)
                .details(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                path
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

