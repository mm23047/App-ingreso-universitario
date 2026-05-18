package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import java.util.UUID;

@Entity
@Table(name = "etapa_admision", schema = "public")
public class EtapasAdmision {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_etapa", nullable = false)
    private UUID id;

    @Size(max = 50)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "puntaje_minimo", precision = 5, scale = 2)
    private BigDecimal puntajeMinimo;

    @Column(name = "puntaje_maximo", precision = 5, scale = 2)
    private BigDecimal puntajeMaximo;

    @Lob
    @Column(name = "descripcion")
    private String descripcion;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPuntajeMinimo() {
        return puntajeMinimo;
    }

    public void setPuntajeMinimo(BigDecimal puntajeMinimo) {
        this.puntajeMinimo = puntajeMinimo;
    }

    public BigDecimal getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public void setPuntajeMaximo(BigDecimal puntajeMaximo) {
        this.puntajeMaximo = puntajeMaximo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}