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
@CrossOrigin(origins = "*")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @PostMapping
    public ResponseEntity<Beneficiario> createBeneficiario(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @RequestBody Beneficiario beneficiario) {
        try {
        	beneficiario.setId(null); // Se asegura de que el ID sea asignado automáticamente
            beneficiarioService.saveBeneficiario(centroId, intermediarioId, beneficiario);
            return new ResponseEntity<>(beneficiario, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
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

    @GetMapping
    public ResponseEntity<List<Beneficiario>> getAllBeneficiarios(@PathVariable Integer centroId, @PathVariable Integer intermediarioId) {
        List<Beneficiario> beneficiarios = beneficiarioService.getAllBeneficiarios(centroId, intermediarioId);
        if (!beneficiarios.isEmpty()) {
            return new ResponseEntity<>(beneficiarios, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Beneficiario> updateBeneficiario(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer id, @RequestBody Beneficiario beneficiario) {
        try {
            Beneficiario updatedBeneficiario = beneficiarioService.updateBeneficiario(centroId, intermediarioId, id, beneficiario);
            if (updatedBeneficiario != null) {
                return new ResponseEntity<>(updatedBeneficiario, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
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

    @PostMapping("/{beneficiarioId}/donadores/{donadorId}")
    public ResponseEntity<HttpStatus> addDonadorToBeneficiario(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer beneficiarioId, @PathVariable Integer donadorId) {
        try {
            beneficiarioService.addDonadorToBeneficiario(centroId, intermediarioId, beneficiarioId, donadorId);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{beneficiarioId}/donadores/{donadorId}")
    public ResponseEntity<HttpStatus> removeDonadorFromBeneficiario(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer beneficiarioId, @PathVariable Integer donadorId) {
        try {
            beneficiarioService.removeDonadorFromBeneficiario(centroId, intermediarioId, beneficiarioId, donadorId);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
