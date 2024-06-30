package com.ipn.mx.integration;

import com.ipn.mx.service.PdfService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/reportes")
@Api(value = "PdfController", description = "Controlador para la generación de reportes PDF")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @ApiOperation(value = "Generar reporte PDF de centros de donación", response = InputStreamResource.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Reporte generado exitosamente", response = InputStreamResource.class),
        @ApiResponse(code = 500, message = "Error al generar el reporte")
    })
    @GetMapping(value = "/centros/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> generarReporte() throws IOException {
        ByteArrayInputStream bis = pdfService.generarReporte();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=reporte.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }
}
