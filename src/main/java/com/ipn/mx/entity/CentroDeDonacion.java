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
public class CentroDeDonacion {

    @JsonProperty("Nombre")
    @PropertyName("Nombre")
    private String nombre;

    @JsonProperty("Direccion")
    @PropertyName("Direccion")
    private String direccion;

    @JsonProperty("Contacto")
    @PropertyName("Contacto")
    private String contacto;
    
    @JsonIgnore
    private Integer id;
}
