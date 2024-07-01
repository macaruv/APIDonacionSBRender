package com.ipn.mx.integration;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = {"http://localhost:4200"}, allowCredentials = "true")
public class SubidaArchivoController {

    // Directorio donde se guardarán los archivos
    private static String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        if (file.isEmpty()) {
            response.put("message", "Por favor, seleccione un archivo para subir");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            // Asegurarse de que el directorio exista
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Guardar el archivo en el directorio
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, bytes);

            response.put("message", "Archivo subido correctamente: " + file.getOriginalFilename());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("message", "Error al subir el archivo: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/list")
    public ResponseEntity<String[]> listFiles() {
        File folder = new File(UPLOAD_DIR);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String[] files = folder.list();
        if (files != null) {
            return new ResponseEntity<>(files, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new String[]{}, HttpStatus.OK);
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Object> downloadFile(@PathVariable String filename) throws IOException {
        String filePath = UPLOAD_DIR + filename;
        File file = new File(filePath);
        if (!file.exists()) {
            return new ResponseEntity<>("Archivo no encontrado", HttpStatus.NOT_FOUND);
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
    
    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        String filePath = UPLOAD_DIR + filename;
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                return new ResponseEntity<>("Archivo eliminado correctamente", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Error al eliminar el archivo", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Archivo no encontrado", HttpStatus.NOT_FOUND);
        }
    }
}
