package com.ipn.mx.integration;

import com.ipn.mx.service.EmailService;
import com.ipn.mx.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfService pdfService;
    
    @GetMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestParam String to,
                                            @RequestParam String subject,
                                            @RequestParam String text,
                                            @RequestParam(required = false) Boolean attachReport) {
        if (attachReport != null && attachReport) {
            ByteArrayInputStream bis = pdfService.generarReporte();
            emailService.sendMessageWithAttachment(to, subject, text, bis, "reporte.pdf");
        } else {
            emailService.sendSimpleMessage(to, subject, text);
        }
        return ResponseEntity.ok("Email sent successfully");
    }
}
