package com.ipn.mx.integration;

import com.ipn.mx.entity.Intermediario;
import com.ipn.mx.service.IntermediarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centros/{centroId}/intermediarios")
@CrossOrigin(origins = "*")
public class IntermediarioController {

    @Autowired
    private IntermediarioService intermediarioService;

    @PostMapping
    public ResponseEntity<Intermediario> createIntermediario(@PathVariable Integer centroId, @RequestBody Intermediario intermediario) {
        try {
        	intermediario.setId(null); // Se asegura de que el ID sea asignado automáticamente
            intermediarioService.saveIntermediario(centroId, intermediario);
            return new ResponseEntity<>(intermediario, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Intermediario> getIntermediarioById(@PathVariable Integer centroId, @PathVariable Integer id) {
        Intermediario intermediario = intermediarioService.getIntermediarioById(centroId, id);
        if (intermediario != null) {
            return new ResponseEntity<>(intermediario, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Intermediario> updateIntermediario(@PathVariable Integer centroId, @PathVariable Integer id, @RequestBody Intermediario intermediario) {
        try {
            intermediarioService.updateIntermediario(centroId, id, intermediario);
            return new ResponseEntity<>(intermediario, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteIntermediarioById(@PathVariable Integer centroId, @PathVariable Integer id) {
        try {
            intermediarioService.deleteIntermediarioById(centroId, id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Intermediario>> getIntermediariosByCentroId(@PathVariable Integer centroId) {
        List<Intermediario> intermediarios = intermediarioService.getIntermediariosByCentroId(centroId);
        if (!intermediarios.isEmpty()) {
            return new ResponseEntity<>(intermediarios, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
