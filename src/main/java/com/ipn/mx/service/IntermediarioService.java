package com.ipn.mx.service;

import com.ipn.mx.entity.Intermediario;
import java.util.List;

public interface IntermediarioService {
    void saveIntermediario(Integer centroId, Intermediario intermediario);
    Intermediario getIntermediarioById(Integer centroId, Integer id);
    List<Intermediario> getAllIntermediarios(Integer centroId);
    Intermediario updateIntermediario(Integer centroId, Integer id, Intermediario intermediario);
    void deleteIntermediarioById(Integer centroId, Integer id);
    List<Intermediario> getIntermediariosByCentroId(Integer centroId);
}
