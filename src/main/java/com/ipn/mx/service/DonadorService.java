package com.ipn.mx.service;

import com.ipn.mx.entity.Donador;
import java.util.List;

public interface DonadorService {
    void saveDonador(Integer centroId, Integer intermediarioId, Donador donador);
    Donador getDonadorById(Integer centroId, Integer intermediarioId, Integer id);
    List<Donador> getAllDonadores(Integer centroId, Integer intermediarioId);
    Donador updateDonador(Integer centroId, Integer intermediarioId, Integer id, Donador donador);
    void deleteDonadorById(Integer centroId, Integer intermediarioId, Integer id);
    void addBeneficiarioToDonador(Integer centroId, Integer intermediarioId, Integer donadorId, Integer beneficiarioId);
    void removeBeneficiarioFromDonador(Integer centroId, Integer intermediarioId, Integer donadorId, Integer beneficiarioId);
    
    List<Donador> getDonadoresByIntermediarioId(Integer centroId, Integer intermediarioId);
}
