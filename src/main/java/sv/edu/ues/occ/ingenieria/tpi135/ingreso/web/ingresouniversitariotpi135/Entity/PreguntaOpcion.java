package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pregunta_opcion", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_pregunta", "id_respuesta_global"})
})
public class PreguntaOpcion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pregunta_opcion", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private BancoPregunta idPregunta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_respuesta_global", nullable = false)
    private BancoRespuesta idRespuestaGlobal;

    @NotNull
    @Column(name = "es_correcta", nullable = false)
    private Boolean esCorrecta;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BancoPregunta getIdPregunta() {
        return idPregunta;
    }

    public void setIdPregunta(BancoPregunta idPregunta) {
        this.idPregunta = idPregunta;
    }

    public BancoRespuesta getIdRespuestaGlobal() {
        return idRespuestaGlobal;
    }

    public void setIdRespuestaGlobal(BancoRespuesta idRespuestaGlobal) {
        this.idRespuestaGlobal = idRespuestaGlobal;
    }

    public Boolean getEsCorrecta() {
        return esCorrecta;
    }

    public void setEsCorrecta(Boolean esCorrecta) {
        this.esCorrecta = esCorrecta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreguntaOpcion that = (PreguntaOpcion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}