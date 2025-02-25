package com.ipn.mx.integration;

import com.ipn.mx.entity.Persona;
import com.ipn.mx.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas")
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"https://apiprocesodonacion.netlify.app"}, allowCredentials = "true")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    @PostMapping
    public ResponseEntity<Persona> createPersona(@RequestBody Persona persona) {
        try {
            persona.setId(null); // Se asegura de que el ID sea asignado automáticamente
            Persona createdPersona = personaService.savePersona(persona);
            return new ResponseEntity<>(createdPersona, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Persona> getPersonaById(@PathVariable Integer id) {
        Persona persona = personaService.getPersonaById(id);
        if (persona != null) {
            return new ResponseEntity<>(persona, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Persona> updatePersona(@PathVariable Integer id, @RequestBody Persona persona) {
        try {
            Persona updatedPersona = personaService.updatePersona(id, persona);
            return new ResponseEntity<>(updatedPersona, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePersonaById(@PathVariable Integer id) {
        try {
            personaService.deletePersonaById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Persona>> getAllPersonas() {
        List<Persona> personas = personaService.getAllPersonas();
        if (!personas.isEmpty()) {
            return new ResponseEntity<>(personas, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
    
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<Persona>> getPersonasPorRol(@PathVariable String rol) {
        List<Persona> personas = personaService.findByRol(rol);
        if (!personas.isEmpty()) {
            return new ResponseEntity<>(personas, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
