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
public class Intermediario {
	@JsonIgnore
    private Integer id;
	
	@JsonProperty("Nombre")
    @PropertyName("Nombre")
    private String nombre;
	
	@JsonProperty("Contacto")
    @PropertyName("Contacto")
    private String contacto;
	
	@JsonProperty("url")
    @PropertyName("url")
    private String url;
	
	@JsonIgnore
    private Integer centroId;
}
