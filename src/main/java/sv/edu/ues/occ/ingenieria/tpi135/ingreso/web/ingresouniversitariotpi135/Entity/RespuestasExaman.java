package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "respuestas_examen", schema = "public")
@NamedQueries({
        @NamedQuery(
                name = "RespuestasExaman.findByExamenId",
                query = "SELECT r FROM RespuestasExaman r WHERE r.idExamen.id = :idExamen"
        )
})
public class RespuestasExaman {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_respuesta", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_examen", nullable = false)
    private ExamenesRealizado idExamen;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private BancoPregunta idPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion_seleccionada")
    private OpcionesRespuesta idOpcionSeleccionada;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ExamenesRealizado getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(ExamenesRealizado idExamen) {
        this.idExamen = idExamen;
    }

    public BancoPregunta getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(BancoPregunta idPregunta) {
        this.idPregunta = idPregunta;
    }

    public OpcionesRespuesta getIdOpcionSeleccionada() {
        return idOpcionSeleccionada;
    }

    public void setIdOpcionSeleccionada(OpcionesRespuesta idOpcionSeleccionada) {
        this.idOpcionSeleccionada = idOpcionSeleccionada;
    }

}