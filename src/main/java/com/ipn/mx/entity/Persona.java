package com.ipn.mx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

	@JsonIgnore
    private Integer id;

    @JsonProperty("Nombre")
    @PropertyName("Nombre")
    private String nombre;

    @JsonProperty("Telefono")
    @PropertyName("Telefono")
    private String telefono;

    @JsonProperty("Correo")
    @PropertyName("Correo")
    private String correo;

    @JsonProperty("Edad")
    @PropertyName("Edad")
    private Integer edad;

    @JsonProperty("Peso")
    @PropertyName("Peso")
    private Double peso;

    @JsonProperty("Genero")
    @PropertyName("Genero")
    private String genero;

    @JsonProperty("Rol")
    @PropertyName("Rol")
    private String rol; // "Donador" o "Beneficiario"
}
