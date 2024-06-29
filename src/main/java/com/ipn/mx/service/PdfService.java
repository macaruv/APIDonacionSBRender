package com.ipn.mx.service;

import com.ipn.mx.entity.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    @Autowired
    private CentroDeDonacionService centroDeDonacionService;

    @Autowired
    private IntermediarioService intermediarioService;

    @Autowired
    private DonadorService donadorService;

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private PersonaService personaService;

    public ByteArrayInputStream generarReporte() {
        Document documento = new Document();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            // Obtener y añadir la información del Centro de Donación
            List<CentroDeDonacion> centros = centroDeDonacionService.getAllCentros();
            if (centros.isEmpty()) {
                documento.add(new Paragraph("No se registraron Centros de Donación"));
            } else {
                for (CentroDeDonacion centro : centros) {
                    documento.add(new Paragraph("Centro de Donación"));
                    documento.add(new Paragraph("Nombre: " + centro.getNombre()));
                    documento.add(new Paragraph("Dirección: " + centro.getDireccion()));
                    documento.add(new Paragraph("Contacto: " + centro.getContacto()));
                    documento.add(Chunk.NEWLINE);

                    // Obtener y añadir la información del Intermediario
                    List<Intermediario> intermediarios = intermediarioService.getIntermediariosByCentroId(centro.getId());
                    if (intermediarios.isEmpty()) {
                        documento.add(new Paragraph("No se registraron Intermediarios para el Centro de Donación: " + centro.getNombre()));
                    } else {
                        for (Intermediario intermediario : intermediarios) {
                            documento.add(new Paragraph("Intermediario"));
                            documento.add(new Paragraph("Nombre: " + intermediario.getNombre()));
                            documento.add(new Paragraph("Contacto: " + intermediario.getContacto()));
                            documento.add(new Paragraph("URL: " + intermediario.getUrl()));
                            documento.add(Chunk.NEWLINE);

                            // Obtener y añadir la información del Donador
                            List<Donador> donadores = donadorService.getDonadoresByIntermediarioId(centro.getId(), intermediario.getId());
                            if (donadores.isEmpty()) {
                                documento.add(new Paragraph("No se registraron Donadores para el Intermediario: " + intermediario.getNombre()));
                            } else {
                                for (Donador donador : donadores) {
                                    documento.add(new Paragraph("Donador"));
                                    documento.add(new Paragraph("ID del Donador: " + donador.getId()));
                                    documento.add(new Paragraph("Tipo de Sangre: " + donador.getTipoDeSangre()));
                                    documento.add(new Paragraph("Alergias: " + String.join(", ", donador.getAlergias())));
                                    documento.add(new Paragraph("Enfermedades: " + String.join(", ", donador.getEnfermedades())));
                                    documento.add(new Paragraph("Última Vacuna: " + donador.getUltimaVacuna()));
                                    documento.add(new Paragraph("Lista de Beneficirios por ID: " + donador.getBeneficiarioIds()));
                                    Persona personaDonador = personaService.getPersonaById(donador.getPersonaId());
                                    if (personaDonador != null) {
                                    	documento.add(new Paragraph("Datos personales del Donante"));
                                    	documento.add(new Paragraph("ID de la persona: " + personaDonador.getId()));
                                        documento.add(new Paragraph("Nombre: " + personaDonador.getNombre()));
                                        documento.add(new Paragraph("Teléfono: " + personaDonador.getTelefono()));
                                        documento.add(new Paragraph("Correo: " + personaDonador.getCorreo()));
                                        documento.add(new Paragraph("Edad: " + personaDonador.getEdad()));
                                        documento.add(new Paragraph("Peso: " + personaDonador.getPeso()));
                                        documento.add(new Paragraph("Género: " + personaDonador.getGenero()));
                                    } else {
                                        documento.add(new Paragraph("No se encontró información de la persona asociada al Donador con ID: " + donador.getId()));
                                    }
                                    documento.add(Chunk.NEWLINE);
                                }
                            }

                            // Obtener y añadir la información del Beneficiario
                            List<Beneficiario> beneficiarios = beneficiarioService.getBeneficiariosByIntermediarioId(centro.getId(), intermediario.getId());
                            if (beneficiarios.isEmpty()) {
                                documento.add(new Paragraph("No se registraron Beneficiarios para el Intermediario: " + intermediario.getNombre()));
                            } else {
                                for (Beneficiario beneficiario : beneficiarios) {
                                    documento.add(new Paragraph("Beneficiario"));
                                    documento.add(new Paragraph("ID del Beneficiario: " + beneficiario.getId()));
                                    documento.add(new Paragraph("Tipo de Sangre: " + beneficiario.getTipoDeSangre()));
                                    for (Integer donadorId : beneficiario.getDonadorIds()) {
                                        Donador donadorAsociado = donadorService.getDonadorById(centro.getId(), intermediario.getId(), donadorId);
                                        if (donadorAsociado != null) {
                                            documento.add(new Paragraph("Donador Asociado ID: " + donadorAsociado.getId()));
                                        } else {
                                            documento.add(new Paragraph("No se encontró información del Donador Asociado con ID: " + donadorId));
                                        }
                                    }
                                    Persona personaBeneficiario = personaService.getPersonaById(beneficiario.getPersonaId());
                                    if (personaBeneficiario != null) {
                                    	documento.add(new Paragraph("Datos personales del Beneficiario"));
                                    	documento.add(new Paragraph("ID de la persona: " + personaBeneficiario.getId()));
                                        documento.add(new Paragraph("Nombre: " + personaBeneficiario.getNombre()));
                                        documento.add(new Paragraph("Teléfono: " + personaBeneficiario.getTelefono()));
                                        documento.add(new Paragraph("Correo: " + personaBeneficiario.getCorreo()));
                                        documento.add(new Paragraph("Edad: " + personaBeneficiario.getEdad()));
                                        documento.add(new Paragraph("Peso: " + personaBeneficiario.getPeso()));
                                        documento.add(new Paragraph("Género: " + personaBeneficiario.getGenero()));
                                    } else {
                                        documento.add(new Paragraph("No se encontró información de la persona asociada al Beneficiario con ID: " + beneficiario.getId()));
                                    }
                                    documento.add(Chunk.NEWLINE);
                                }
                            }
                        }
                    }
                }
            }

            documento.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(salida.toByteArray());
    }
}
