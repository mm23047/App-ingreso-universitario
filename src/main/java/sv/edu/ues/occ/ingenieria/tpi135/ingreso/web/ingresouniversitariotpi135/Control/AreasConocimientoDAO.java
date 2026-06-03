package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class AreasConocimientoDAO extends IngresoDefaultDataAccess<AreasConocimiento> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public AreasConocimientoDAO(){
        super(AreasConocimiento.class);
    }

    @Override
    public EntityManager getEntityManager(){
        return em;
    }

    public boolean existePorNombre(String nombreArea) {
        if (nombreArea == null || nombreArea.trim().isEmpty()) {
            return false;
        }
        try {
            // Se invoca el NamedQuery de la Entidad
            Long count = em.createNamedQuery("AreasConocimiento.countByNombre", Long.class)
                    .setParameter("nombreArea", nombreArea.trim())
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Error al verificar la existencia del área", e);
        }
    }

    public List<AreasConocimiento> buscarPorNombreSimilar(String patron) {
        if (patron == null || patron.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // Se invoca el NamedQuery de la Entidad
            return em.createNamedQuery("AreasConocimiento.findByNombreLike", AreasConocimiento.class)
                    .setParameter("patron", "%" + patron.trim() + "%")
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al buscar áreas de conocimiento", e);
        }
    }

    /**
     * Es necesario saber si ya existe el area de conocimiento antes de crearla
     * @param entity
     */
    @Override
    public void crear(AreasConocimiento entity) {
        if (entity == null || entity.getNombreArea() == null) {
            throw new IllegalArgumentException("El área de conocimiento y su nombre son obligatorios");
        }
        if (existePorNombre(entity.getNombreArea())) {
            throw new IllegalArgumentException("Ya existe un Área de Conocimiento con el nombre: " + entity.getNombreArea().trim());
        }
        // Si pasa la validación, delegamos la persistencia al padre
        super.crear(entity);
    }
    /**
     * Es necesario saber si ya existe el area de conocimiento antes de actualizar
     * @param entity
     */
    @Override
    public AreasConocimiento actualizar(AreasConocimiento entity) {
        if (entity == null || entity.getIdAreaConocimiento() == null) {
            throw new IllegalArgumentException("Entidad inválida o sin ID para actualizar");
        }
        if (existePorNombreDiferenteId(entity.getNombreArea(), entity.getIdAreaConocimiento())) {
            throw new IllegalArgumentException("Ya existe otra Área de Conocimiento con el nombre: " + entity.getNombreArea().trim());
        }
        // Si pasa la validación, delegamos la persistencia al padre
        return super.actualizar(entity);
    }
    private boolean existePorNombreDiferenteId(String nombreArea, UUID idArea) {
        if (nombreArea == null || nombreArea.trim().isEmpty()) return false;
        try {
            Long count = em.createNamedQuery("AreasConocimiento.countByNombreAndNotId", Long.class)
                    .setParameter("nombreArea", nombreArea.trim())
                    .setParameter("idArea", idArea)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Error al verificar la existencia del área", e);
        }
    }
    /* ==========================================================
     * 2. REGLA: PROTECCIÓN DE INTEGRIDAD REFERENCIAL (BORRADO)
     * ========================================================== */

    @Override
    public void eliminar(AreasConocimiento entity) {
        if (entity == null || entity.getIdAreaConocimiento() == null) {
            throw new IllegalArgumentException("El área proporcionada no es válida para eliminar");
        }
        if (verificarDependencias(entity.getIdAreaConocimiento())) {
            throw new IllegalStateException("No se puede eliminar el Área de Conocimiento porque tiene Temas o Preguntas asociadas.");
        }
        super.eliminar(entity);
    }

    public boolean verificarDependencias(UUID idAreaConocimiento) {
        try {
            // CORREGIDO: Ahora invoca el NamedQuery de la entidad de forma limpia
            Long count = em.createNamedQuery("AreasConocimiento.countDependencias", Long.class)
                    .setParameter("idArea", idAreaConocimiento)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Error al verificar dependencias del área", e);
        }
    }

    /* ==========================================================
     * 3. REGLA: FILTROS AVANZADOS DE NEGOCIO
     * ========================================================== */

    public List<AreasConocimiento> findAreasConPreguntasDisponibles() {
        try {
            return em.createNamedQuery("AreasConocimiento.findConPreguntas", AreasConocimiento.class)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al obtener las áreas con preguntas", e);
        }
    }


}