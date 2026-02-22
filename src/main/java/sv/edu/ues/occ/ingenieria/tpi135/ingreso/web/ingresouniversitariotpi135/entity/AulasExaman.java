package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "aulas_examen")
public class AulasExaman {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turno", nullable = false)
    private TurnosExaman idTurno;

    @Size(max = 50)
    @NotNull
    @Column(name = "id_aula_api", nullable = false, length = 50)
    private String idAulaApi;

    @NotNull
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "cupos_ocupados")
    private Integer cuposOcupados;

    @NotNull
    @Column(name = "accesible_silla_ruedas", nullable = false)
    private Boolean accesibleSillaRuedas;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TurnosExaman getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(TurnosExaman idTurno) {
        this.idTurno = idTurno;
    }

    public String getIdAulaApi() {
        return idAulaApi;
    }

    public void setIdAulaApi(String idAulaApi) {
        this.idAulaApi = idAulaApi;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Integer getCuposOcupados() {
        return cuposOcupados;
    }

    public void setCuposOcupados(Integer cuposOcupados) {
        this.cuposOcupados = cuposOcupados;
    }

    public Boolean getAccesibleSillaRuedas() {
        return accesibleSillaRuedas;
    }

    public void setAccesibleSillaRuedas(Boolean accesibleSillaRuedas) {
        this.accesibleSillaRuedas = accesibleSillaRuedas;
    }

}