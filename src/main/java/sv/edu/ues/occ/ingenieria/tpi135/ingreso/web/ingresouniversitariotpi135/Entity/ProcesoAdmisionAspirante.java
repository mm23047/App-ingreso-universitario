package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "ProcesoAdmisionAspirante.findCarrerasElegidas",
                query = "SELECT ce FROM CarrerasElegida ce WHERE ce.inscripcionesPrueba.idInscripcionPrueba = :idInscripcion ORDER BY ce.prioridad ASC"
        ),
        @NamedQuery(
                name = "ProcesoAdmisionAspirante.findCuposCarrera",
                query = "SELECT cc FROM CuposCarrera cc WHERE cc.idCupoCarrera.idPrueba = :idPrueba AND cc.idCupoCarrera.idCarrera = :idCarrera AND cc.idCupoCarrera.idEtapa = :idEtapa"
        ),
        // NUEVA CONSULTA: Trae los procesos de aspirantes pendientes filtrados por nota final (ExamenRealizado) de mayor a menor
        @NamedQuery(
                name = "ProcesoAdmisionAspirante.findPendientesPorPuntaje",
                query = "SELECT p FROM ProcesoAdmisionAspirante p " +
                        "JOIN ExamenRealizado er ON er.inscripcionesPrueba.idInscripcionPrueba = p.idProcesoAdmisionAspirante " +
                        "WHERE p.etapaAdmision.idEtapaAdmision = :idEtapa AND p.estado = 'PENDIENTE' " +
                        "ORDER BY er.puntajeFinal DESC"
        ),
        // NUEVA CONSULTA: Carga el proceso y sus relaciones para REST.
        // Se usa LEFT JOIN en carreraAsignada porque puede ser NULL si aún no ha sido admitido.
        @NamedQuery(
                name = "ProcesoAdmisionAspirante.findByIdConRelaciones",
                query = "SELECT p FROM ProcesoAdmisionAspirante p " +
                        "JOIN FETCH p.inscripcionesPrueba " +
                        "JOIN FETCH p.etapaAdmision " +
                        "LEFT JOIN FETCH p.carreraAsignada " +
                        "WHERE p.idProcesoAdmisionAspirante = :idProceso"
        )
})
public class ProcesoAdmisionAspirante implements Serializable {

    private static final long serialVersionUID = 1L; // CORRECCIÓN: Agregado serialVersionUID

    @Id
    @Column(name = "id_inscripcion", nullable = false)
    private UUID idProcesoAdmisionAspirante;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private InscripcionesPrueba inscripcionesPrueba;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_etapa_actual", nullable = false)
    private EtapasAdmision etapaAdmision;

    @Size(max = 20)
    @NotNull
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_asignada")
    private CatalogoCarrera carreraAsignada;

    public UUID getIdProcesoAdmisionAspirante() {
        return idProcesoAdmisionAspirante;
    }

    public void setIdProcesoAdmisionAspirante(UUID id) {
        this.idProcesoAdmisionAspirante = id;
    }

    public InscripcionesPrueba getInscripcionesPrueba() {
        return inscripcionesPrueba;
    }

    public void setInscripcionesPrueba(InscripcionesPrueba inscripcionesPrueba) {
        this.inscripcionesPrueba = inscripcionesPrueba;
    }

    public EtapasAdmision getEtapaAdmision() {
        return etapaAdmision;
    }

    public void setEtapaAdmision(EtapasAdmision idEtapaActual) {
        this.etapaAdmision = idEtapaActual;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public CatalogoCarrera getCarreraAsignada() {
        return carreraAsignada;
    }

    public void setCarreraAsignada(CatalogoCarrera carreraAsignada) {
        this.carreraAsignada = carreraAsignada;
    }

    // CORRECCIÓN: Métodos equals y hashCode basados en la PK relacional
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcesoAdmisionAspirante)) return false;
        ProcesoAdmisionAspirante that = (ProcesoAdmisionAspirante) o;
        return idProcesoAdmisionAspirante != null && idProcesoAdmisionAspirante.equals(that.idProcesoAdmisionAspirante);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProcesoAdmisionAspirante);
    }
}