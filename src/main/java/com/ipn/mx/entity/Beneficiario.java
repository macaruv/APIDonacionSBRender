package com.ipn.mx.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
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
public class Beneficiario {

    @JsonProperty(value = "id", access = Access.READ_ONLY)
    @PropertyName("id")
    private Integer id;

    @JsonProperty("TipoDeSangre")
    @PropertyName("TipoDeSangre")
    private String tipoDeSangre;

    @JsonProperty("DonadorIds")
    @PropertyName("DonadorIds")
    private List<Integer> donadorIds; // Lista de IDs de Donadores

    @JsonProperty("PersonaId")
    @PropertyName("PersonaId")
    private Integer personaId; // ID de la Persona asociada
}
