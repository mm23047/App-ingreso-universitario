package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
// 1. ELIMINADO: @UniqueConstraint global (ahora lo maneja la BD con índices parciales)
@Table(name = "banco_respuesta", schema = "public")
@NamedQueries({
        // 2. NUEVAS CONSULTAS: Para validar duplicados separando la lógica Global vs Local
        @NamedQuery(
                name = "BancoRespuesta.countGlobalByTexto",
                query = "SELECT COUNT(b) FROM BancoRespuesta b WHERE UPPER(TRIM(b.textoRespuesta)) = UPPER(TRIM(:textoRespuesta)) AND b.idArea IS NULL"
        ),
        @NamedQuery(
                name = "BancoRespuesta.countLocalByTexto",
                query = "SELECT COUNT(b) FROM BancoRespuesta b WHERE UPPER(TRIM(b.textoRespuesta)) = UPPER(TRIM(:textoRespuesta)) AND b.idArea.idAreaConocimiento = :idArea"
        ),
        @NamedQuery(
                name = "BancoRespuesta.countGlobalByTextoAndNotId",
                query = "SELECT COUNT(b) FROM BancoRespuesta b WHERE UPPER(TRIM(b.textoRespuesta)) = UPPER(TRIM(:textoRespuesta)) AND b.idArea IS NULL AND b.idBancoRespuesta <> :idBancoRespuesta"
        ),
        @NamedQuery(
                name = "BancoRespuesta.countLocalByTextoAndNotId",
                query = "SELECT COUNT(b) FROM BancoRespuesta b WHERE UPPER(TRIM(b.textoRespuesta)) = UPPER(TRIM(:textoRespuesta)) AND b.idArea.idAreaConocimiento = :idArea AND b.idBancoRespuesta <> :idBancoRespuesta"
        )
})
@NamedNativeQueries({
        // 3. ACTUALIZACIÓN: Ahora los distractores jalan respuestas del área elegida OR respuestas globales (id_area IS NULL)
        @NamedNativeQuery(
                name = "BancoRespuesta.findRandomByArea",
                query = "SELECT * FROM banco_respuesta WHERE id_area = ?1 OR id_area IS NULL ORDER BY RANDOM()",
                resultClass = BancoRespuesta.class
        ),
        @NamedNativeQuery(
                name = "BancoRespuesta.findRandomByAreaExcludingId",
                query = "SELECT * FROM banco_respuesta WHERE (id_area = ?1 OR id_area IS NULL) AND id_respuesta_global != ?2 ORDER BY RANDOM()",
                resultClass = BancoRespuesta.class
        )
})
public class BancoRespuesta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_respuesta_global", nullable = false)
    private UUID idBancoRespuesta;

    @NotNull
    // ELIMINADO: @Lob para evitar OIDs en Postgres. Usamos TEXT directamente.
    @Column(name = "texto_respuesta", nullable = false, columnDefinition = "TEXT")
    private String textoRespuesta;

    // ELIMINADO: @NotNull.
    // ACTUALIZADO: optional = true y nullable = true. ¡Esto hace la magia de las respuestas globales!
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_area", nullable = true)
    private AreasConocimiento idArea;

    public UUID getIdBancoRespuesta() {
        return idBancoRespuesta;
    }

    public void setIdBancoRespuesta(UUID id) {
        this.idBancoRespuesta = id;
    }

    public String getTextoRespuesta() {
        return textoRespuesta;
    }

    public void setTextoRespuesta(String textoRespuesta) {
        this.textoRespuesta = textoRespuesta;
    }

    public AreasConocimiento getIdArea() {
        return idArea;
    }

    public void setIdArea(AreasConocimiento idArea) {
        this.idArea = idArea;
    }

    @PrePersist
    @PreUpdate
    private void normalizarDatos() {
        if (this.textoRespuesta != null) {
            this.textoRespuesta = this.textoRespuesta.trim();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BancoRespuesta)) return false;
        BancoRespuesta that = (BancoRespuesta) o;
        return idBancoRespuesta != null && idBancoRespuesta.equals(that.getIdBancoRespuesta());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}