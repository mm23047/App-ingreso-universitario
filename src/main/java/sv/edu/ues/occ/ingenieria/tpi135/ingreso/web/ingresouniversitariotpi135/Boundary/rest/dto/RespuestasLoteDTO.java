package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.dto;

import java.util.List;
import java.util.UUID;

/**
 * Payload optimizado para recibir un lote de respuestas.
 */
public class RespuestasLoteDTO {

    private UUID idExamen;
    private List<UUID> opcionesSeleccionadas; // Solo los IDs de las burbujas que marcó

    public RespuestasLoteDTO() {}

    public UUID getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(UUID idExamen) {
        this.idExamen = idExamen;
    }

    public List<UUID> getOpcionesSeleccionadas() {
        return opcionesSeleccionadas;
    }

    public void setOpcionesSeleccionadas(List<UUID> opcionesSeleccionadas) {
        this.opcionesSeleccionadas = opcionesSeleccionadas;
    }
}