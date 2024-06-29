package com.ipn.mx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donador {

	@JsonIgnore
    private Integer id;

    @JsonProperty("TipoDeSangre")
    @PropertyName("TipoDeSangre")
    private String tipoDeSangre;

    @JsonProperty("Alergias")
    @PropertyName("Alergias")
    private List<String> alergias;

    @JsonProperty("Enfermedades")
    @PropertyName("Enfermedades")
    private List<String> enfermedades;

    @JsonProperty("UltimaVacuna")
    @PropertyName("UltimaVacuna")
    private String ultimaVacuna;

    @JsonProperty("BeneficiarioIds")
    @PropertyName("BeneficiarioIds")
    private List<Integer> beneficiarioIds; // Lista de IDs de Beneficiarios

    @JsonProperty("PersonaId")
    @PropertyName("PersonaId")
    private Integer personaId; // ID de la Persona asociada
}
