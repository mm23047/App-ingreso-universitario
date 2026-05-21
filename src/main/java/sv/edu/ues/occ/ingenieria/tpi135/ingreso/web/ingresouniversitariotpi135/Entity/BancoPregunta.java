package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
// CORRECCIÓN: Se agrega explícitamente el esquema "public" para consistencia con el DDL de PostgreSQL
@Table(name = "banco_pregunta", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "banco_pregunta_enunciado_key", columnNames = {"enunciado"})
})
@NamedQueries({
        @NamedQuery(
                name = "BancoPregunta.findByTema",
                query = "SELECT b FROM BancoPregunta b WHERE b.idTema.idTema = :idTema"
        ),
        @NamedQuery(
                name = "BancoPregunta.countByEnunciado",
                query = "SELECT COUNT(b) FROM BancoPregunta b WHERE UPPER(TRIM(b.enunciado)) = UPPER(TRIM(:enunciado))"
        ),
        @NamedQuery(
                name = "BancoPregunta.countByEnunciadoAndNotId",
                query = "SELECT COUNT(b) FROM BancoPregunta b WHERE UPPER(TRIM(b.enunciado)) = UPPER(TRIM(:enunciado)) AND b.idBancoPregunta <> :idBancoPregunta"
        ),
        @NamedQuery(
                name = "BancoPregunta.countConflictosArea",
                query = "SELECT COUNT(bp) FROM BancoPregunta bp WHERE UPPER(TRIM(bp.enunciado)) = UPPER(TRIM(:enunciado)) AND bp.idTema.idArea.idAreaConocimiento <> :idAreaActual"
        )
})
public class BancoPregunta implements Serializable {

    // CORRECCIÓN: Firma de serialización mandatoria para la gestión de ciclo de vida en el servidor
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pregunta", nullable = false)
    private UUID idBancoPregunta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tema", nullable = false)
    private Tema idTema;

    @NotNull
    // CORRECCIÓN: Se elimina @Lob (que genera OIDs problemáticos en Postgres) y se define explícitamente TEXT
    @Column(name = "enunciado", nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    public UUID getIdBancoPregunta() {
        return idBancoPregunta;
    }

    public void setIdBancoPregunta(UUID id) {
        this.idBancoPregunta = id;
    }

    public Tema getIdTema() {
        return idTema;
    }

    public void setIdTema(Tema idTema) {
        this.idTema = idTema;
    }

    // TODO: FASE 2 - Bridge temporal por migración. Revisar eliminación tras refactor REST.
    @Transient
    public AreasConocimiento getIdArea() {
        return idTema != null ? idTema.getIdArea() : null;
    }

    // TODO: FASE 2 - Bridge temporal por migración. Revisar eliminación tras refactor REST.
    @Transient
    public void setIdArea(AreasConocimiento idArea) {
        if (idArea == null) {
            return;
        }
        if (this.idTema == null) {
            this.idTema = new Tema();
        }
        this.idTema.setIdArea(idArea);
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    @PrePersist
    @PreUpdate
    private void normalizarTextos() {
        if (this.enunciado != null) {
            this.enunciado = this.enunciado.trim();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BancoPregunta)) return false;
        BancoPregunta that = (BancoPregunta) o;
        return idBancoPregunta != null && idBancoPregunta.equals(that.getIdBancoPregunta());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}