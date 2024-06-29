package com.ipn.mx.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CentroDeDonacion {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @JsonProperty("Nombre")
    private String nombre;

    @JsonProperty("Direccion")
    private String direccion;

    @JsonProperty("Contacto")
    private String contacto;
}
