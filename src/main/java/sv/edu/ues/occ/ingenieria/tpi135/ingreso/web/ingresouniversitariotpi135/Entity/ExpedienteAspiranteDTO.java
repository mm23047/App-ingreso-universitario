package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity;

/**
 * DTO para consolidar el expediente completo del aspirante en una sola consulta.
 * 
 * Agrupa información de múltiples entidades:
 * - Datos personales del aspirante
 * - Inscripción a prueba
 * - Carrera elegida
 * - Asignación de aula y pupitre
 * - Examen realizado (si existe)
 * - Proceso de admisión (si existe)
 * 
 * Reduce el número de llamadas HTTP desde el frontend.
 */
public class ExpedienteAspiranteDTO {
    
    private AspirantesDato aspirante;
    private InscripcionesPrueba inscripcion;
    private CarrerasElegida carrera;
    private AsignacionesAulaPupitre asignacion;
    private ExamenesRealizado examen;
    private ProcesoAdmisionAspirante proceso;

    // ============================================
    // CONSTRUCTORES
    // ============================================

    public ExpedienteAspiranteDTO() {
    }

    public ExpedienteAspiranteDTO(
            AspirantesDato aspirante,
            InscripcionesPrueba inscripcion,
            CarrerasElegida carrera,
            AsignacionesAulaPupitre asignacion,
            ExamenesRealizado examen,
            ProcesoAdmisionAspirante proceso) {
        this.aspirante = aspirante;
        this.inscripcion = inscripcion;
        this.carrera = carrera;
        this.asignacion = asignacion;
        this.examen = examen;
        this.proceso = proceso;
    }

    // ============================================
    // GETTERS Y SETTERS
    // ============================================

    public AspirantesDato getAspirante() {
        return aspirante;
    }

    public void setAspirante(AspirantesDato aspirante) {
        this.aspirante = aspirante;
    }

    public InscripcionesPrueba getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(InscripcionesPrueba inscripcion) {
        this.inscripcion = inscripcion;
    }

    public CarrerasElegida getCarrera() {
        return carrera;
    }

    public void setCarrera(CarrerasElegida carrera) {
        this.carrera = carrera;
    }

    public AsignacionesAulaPupitre getAsignacion() {
        return asignacion;
    }

    public void setAsignacion(AsignacionesAulaPupitre asignacion) {
        this.asignacion = asignacion;
    }

    public ExamenesRealizado getExamen() {
        return examen;
    }

    public void setExamen(ExamenesRealizado examen) {
        this.examen = examen;
    }

    public ProcesoAdmisionAspirante getProceso() {
        return proceso;
    }

    public void setProceso(ProcesoAdmisionAspirante proceso) {
        this.proceso = proceso;
    }

    // ============================================
    // MÉTODOS ÚTILES
    // ============================================

    @Override
    public String toString() {
        return "ExpedienteAspiranteDTO{" +
                "aspirante=" + (aspirante != null ? aspirante.getId() : "null") +
                ", inscripcion=" + (inscripcion != null ? inscripcion.getId() : "null") +
                ", carrera=" + (carrera != null ? carrera.getId() : "null") +
                ", asignacion=" + (asignacion != null ? asignacion.getId() : "null") +
                ", examen=" + (examen != null ? examen.getId() : "null") +
                ", proceso=" + (proceso != null ? proceso.getId() : "null") +
                '}';
    }
}
