package com.ipn.mx.integration;

import com.ipn.mx.entity.CentroDeDonacion;
import com.ipn.mx.service.CentroDeDonacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centros")
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"http://localhost:4200"}, allowCredentials = "true")
public class CentroDeDonacionController {

    @Autowired
    private CentroDeDonacionService centroDeDonacionService;

    @PostMapping
    public ResponseEntity<CentroDeDonacion> createCentro(@RequestBody CentroDeDonacion centro) {
        try {
            centro.setId(null); // Se asegura de que el ID sea asignado automáticamente
            CentroDeDonacion savedCentro = centroDeDonacionService.saveCentro(centro);
            return new ResponseEntity<>(savedCentro, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDeDonacion> getCentroById(@PathVariable Integer id) {
        CentroDeDonacion centro = centroDeDonacionService.getCentroById(id);
        if (centro != null) {
            return new ResponseEntity<>(centro, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentroDeDonacion> updateCentro(@PathVariable Integer id, @RequestBody CentroDeDonacion centro) {
        try {
            centroDeDonacionService.updateCentro(id, centro);
            return new ResponseEntity<>(centro, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCentroById(@PathVariable Integer id) {
        try {
            centroDeDonacionService.deleteCentroById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<CentroDeDonacion>> getAllCentros() {
        List<CentroDeDonacion> centros = centroDeDonacionService.getAllCentros();
        if (!centros.isEmpty()) {
            return new ResponseEntity<>(centros, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
