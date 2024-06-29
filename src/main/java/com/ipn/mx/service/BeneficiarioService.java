package com.ipn.mx.service;

import com.ipn.mx.entity.Beneficiario;
import java.util.List;

public interface BeneficiarioService {
    Beneficiario saveBeneficiario(Integer centroId, Integer intermediarioId, Beneficiario beneficiario);
    Beneficiario getBeneficiarioById(Integer centroId, Integer intermediarioId, Integer id);
    List<Beneficiario> getAllBeneficiarios(Integer centroId, Integer intermediarioId);
    Beneficiario updateBeneficiario(Integer centroId, Integer intermediarioId, Integer id, Beneficiario beneficiario);
    void deleteBeneficiarioById(Integer centroId, Integer intermediarioId, Integer id);
    void addDonadorToBeneficiario(Integer centroId, Integer intermediarioId, Integer beneficiarioId, Integer donadorId);
    void removeDonadorFromBeneficiario(Integer centroId, Integer intermediarioId, Integer beneficiarioId, Integer donadorId);
    
    List<Beneficiario> getBeneficiariosByIntermediarioId(Integer centroId, Integer intermediarioId);
}
