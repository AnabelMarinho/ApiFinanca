package ufersa.dev.ApiFinanca.controller;

import io.swagger.v3.oas.annotations.Operation;
import ufersa.dev.ApiFinanca.dto.SendEmailRequest;
import ufersa.dev.ApiFinanca.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email/send")
    @Operation(summary = "Envia email de teste")
    public void sendEmail(@RequestBody SendEmailRequest request) {
        emailService.enviarEmail(
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );
    }
}

