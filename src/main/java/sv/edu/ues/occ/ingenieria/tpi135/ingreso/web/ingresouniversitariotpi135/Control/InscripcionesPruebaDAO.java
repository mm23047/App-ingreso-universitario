package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class InscripcionesPruebaDAO extends IngresoDefaultDataAccess<InscripcionesPrueba> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public InscripcionesPruebaDAO() {
        super(InscripcionesPrueba.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(InscripcionesPrueba entity) {
        validarReglasDeInscripcion(entity);

        // REGLA DE NEGOCIO: Bloqueo preventivo de doble registro activo por aspirante y prueba
        if (existsByAspiranteAndPrueba(entity.getIdAspirante().getId(), entity.getIdPrueba().getIdPruebaAdmision())) {
            throw new IllegalArgumentException("Violación de integridad: El aspirante ya cuenta con una inscripción activa registrada para esta prueba.");
        }
        super.crear(entity);
    }

    @Override
    public InscripcionesPrueba actualizar(InscripcionesPrueba entity) {
        validarReglasDeInscripcion(entity);
        if (entity.getIdInscripcionPrueba() == null) {
            throw new IllegalArgumentException("No se puede actualizar un registro de inscripción carente de ID primario.");
        }

        // REGLA DE NEGOCIO: Impedir colisiones de duplicidad con registros de terceros durante modificaciones
        if (existsByAspiranteAndPruebaExcludingId(entity.getIdAspirante().getId(), entity.getIdPrueba().getIdPruebaAdmision(), entity.getIdInscripcionPrueba())) {
            throw new IllegalArgumentException("Conflicto en actualización: Los cambios asignados colisionan con otra inscripción ya registrada en el sistema.");
        }
        return super.actualizar(entity);
    }

    private void validarReglasDeInscripcion(InscripcionesPrueba entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad de inscripción de pruebas no puede ser nula.");
        }
        if (entity.getIdAspirante() == null || entity.getIdAspirante().getId() == null) {
            throw new IllegalArgumentException("Toda inscripción requiere asociar una entidad Aspirante válida.");
        }
        if (entity.getIdPrueba() == null || entity.getIdPrueba().getIdPruebaAdmision() == null) {
            throw new IllegalArgumentException("Toda inscripción requiere asociar una entidad Prueba de Admisión válida.");
        }
    }

    public List<InscripcionesPrueba> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("El identificador del aspirante es obligatorio.");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByAspiranteId", InscripcionesPrueba.class)
                    .setParameter("idAspirante", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al obtener inscripciones por aspirante.", e);
        }
    }

    public List<InscripcionesPrueba> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("El identificador de la prueba es obligatorio.");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByPruebaId", InscripcionesPrueba.class)
                    .setParameter("idPrueba", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al obtener inscripciones por prueba.", e);
        }
    }

    public boolean existsByAspiranteAndPrueba(UUID aspiranteId, UUID pruebaId) {
        if (aspiranteId == null || pruebaId == null) {
            throw new IllegalArgumentException("Los identificadores de aspirante y prueba son estrictamente mandatorios.");
        }
        try {
            Long count = em.createNamedQuery("InscripcionesPrueba.countByAspiranteAndPrueba", Long.class)
                    .setParameter("idAspirante", aspiranteId)
                    .setParameter("idPrueba", pruebaId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Error al verificar la existencia previa de la inscripción.", e);
        }
    }

    public boolean existsByAspiranteAndPruebaExcludingId(UUID aspiranteId, UUID pruebaId, UUID excludeId) {
        if (aspiranteId == null || pruebaId == null || excludeId == null) {
            throw new IllegalArgumentException("aspiranteId, pruebaId y excludeId NO deben ser null");
        }
        try {
            Long count = em.createNamedQuery("InscripcionesPrueba.countByAspiranteAndPruebaExcludingId", Long.class)
                    .setParameter("idAspirante", aspiranteId)
                    .setParameter("idPrueba", pruebaId)
                    .setParameter("excludeId", excludeId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("sin acceso a la BD", e);
        }
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Recupera las inscripciones pertenecientes a una prueba filtradas estrictamente bajo un estado operativo uniforme.
     */
    public List<InscripcionesPrueba> findByPruebaAndEstado(UUID idPrueba, String estado) {
        if (idPrueba == null) {
            throw new IllegalArgumentException("El ID de la prueba es requerido para segmentar el listado.");
        }
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado de búsqueda no puede ser nulo o vacío.");
        }
        try {
            return em.createNamedQuery("InscripcionesPrueba.findByPruebaAndEstado", InscripcionesPrueba.class)
                    .setParameter("idPrueba", idPrueba)
                    .setParameter("estado", estado)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error en la extracción filtrada de matrículas por estado.", e);
        }
    }

}