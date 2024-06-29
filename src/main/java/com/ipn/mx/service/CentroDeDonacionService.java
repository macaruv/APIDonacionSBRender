package com.ipn.mx.service;

import com.ipn.mx.entity.CentroDeDonacion;
import java.util.List;

public interface CentroDeDonacionService {
    void saveCentro(CentroDeDonacion centro);
    CentroDeDonacion getCentroById(Integer id);
    List<CentroDeDonacion> getAllCentros();
    CentroDeDonacion updateCentro(Integer id, CentroDeDonacion centro);
    void deleteCentroById(Integer id);
}
