package com.ipn.mx.integration;

import com.ipn.mx.entity.Donador;
import com.ipn.mx.service.DonadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centros/{centroId}/intermediarios/{intermediarioId}/donadores")
@CrossOrigin(origins = "*")
public class DonadorController {

    @Autowired
    private DonadorService donadorService;

    @PostMapping
    public ResponseEntity<Donador> createDonador(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @RequestBody Donador donador) {
        try {
            donador.setId(null); // Se asegura de que el ID sea asignado automáticamente
            Donador savedDonador = donadorService.saveDonador(centroId, intermediarioId, donador);
            return new ResponseEntity<>(savedDonador, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Donador> getDonadorById(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer id) {
        Donador donador = donadorService.getDonadorById(centroId, intermediarioId, id);
        if (donador != null) {
            return new ResponseEntity<>(donador, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Donador> updateDonador(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer id, @RequestBody Donador donador) {
        try {
            Donador updatedDonador = donadorService.updateDonador(centroId, intermediarioId, id, donador);
            if (updatedDonador != null) {
                return new ResponseEntity<>(updatedDonador, HttpStatus.OK);
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
    public ResponseEntity<HttpStatus> deleteDonadorById(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer id) {
        try {
            donadorService.deleteDonadorById(centroId, intermediarioId, id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Donador>> getAllDonadores(@PathVariable Integer centroId, @PathVariable Integer intermediarioId) {
        List<Donador> donadores = donadorService.getAllDonadores(centroId, intermediarioId);
        if (!donadores.isEmpty()) {
            return new ResponseEntity<>(donadores, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/{donadorId}/beneficiarios/{beneficiarioId}")
    public ResponseEntity<HttpStatus> addBeneficiarioToDonador(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer donadorId, @PathVariable Integer beneficiarioId) {
        try {
            donadorService.addBeneficiarioToDonador(centroId, intermediarioId, donadorId, beneficiarioId);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{donadorId}/beneficiarios/{beneficiarioId}")
    public ResponseEntity<HttpStatus> removeBeneficiarioFromDonador(@PathVariable Integer centroId, @PathVariable Integer intermediarioId, @PathVariable Integer donadorId, @PathVariable Integer beneficiarioId) {
        try {
            donadorService.removeBeneficiarioFromDonador(centroId, intermediarioId, donadorId, beneficiarioId);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
