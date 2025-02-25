package com.ipn.mx.service;

import com.ipn.mx.entity.Persona;

import java.util.List;

public interface PersonaService {
	Persona savePersona(Persona persona);
    Persona getPersonaById(Integer id);
    List<Persona> getAllPersonas();
    Persona updatePersona(Integer id, Persona persona);
    void deletePersonaById(Integer id);
    List<Persona> findByRol(String rol);
}
