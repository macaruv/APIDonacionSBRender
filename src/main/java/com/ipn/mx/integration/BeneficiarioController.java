package com.ipn.mx.integration;

import com.ipn.mx.entity.Beneficiario;
import com.ipn.mx.service.BeneficiarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centros/{centroId}/intermediarios/{intermediarioId}/beneficiarios")
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"https://apiprocesodonacion.netlify.app"}, allowCredentials = "true")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @PostMapping
    public ResponseEntity<Beneficiario> createBeneficiario(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @RequestBody Beneficiario beneficiario) {
        try {
            beneficiario.setId(null); // Se asegura de que el ID sea asignado automáticamente
            Beneficiario createdBeneficiario = beneficiarioService.saveBeneficiario(centroId, intermediarioId, beneficiario);
            return new ResponseEntity<>(createdBeneficiario, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beneficiario> getBeneficiarioById(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer id) {
        Beneficiario beneficiario = beneficiarioService.getBeneficiarioById(centroId, intermediarioId, id);
        if (beneficiario != null) {
            return new ResponseEntity<>(beneficiario, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Beneficiario> updateBeneficiario(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer id, @RequestBody Beneficiario beneficiario) {
        try {
            Beneficiario updatedBeneficiario = beneficiarioService.updateBeneficiario(centroId, intermediarioId, id, beneficiario);
            return new ResponseEntity<>(updatedBeneficiario, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBeneficiarioById(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer id) {
        try {
            beneficiarioService.deleteBeneficiarioById(centroId, intermediarioId, id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Beneficiario>> getBeneficiariosByIntermediarioId(@PathVariable Integer centroId, @PathVariable Integer intermediarioId) {
        List<Beneficiario> beneficiarios = beneficiarioService.getBeneficiariosByIntermediarioId(centroId, intermediarioId);
        if (!beneficiarios.isEmpty()) {
            return new ResponseEntity<>(beneficiarios, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
