package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Stateless
@LocalBean
public class EtapasAdmisionDAO extends IngresoDefaultDataAccess<EtapasAdmision> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public EtapasAdmisionDAO() {
        super(EtapasAdmision.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(EtapasAdmision entity) {
        // Obligatorio validar null aquí ya que inspeccionaremos sus propiedades inmediatamente
        validarReglasNegocio(entity);
        if (findByNombre(entity.getNombre().trim()) != null) {
            throw new IllegalArgumentException("Ya existe una etapa de admisión registrada bajo el nombre: " + entity.getNombre());
        }
        super.crear(entity);
    }

    @Override
    public EtapasAdmision actualizar(EtapasAdmision entity) {
        if (entity == null || entity.getIdEtapaAdmision() == null) {
            throw new IllegalArgumentException("La entidad o su identificador no pueden ser nulos para la actualización.");
        }
        validarReglasNegocio(entity);

        Long duplicados = em.createNamedQuery("EtapasAdmision.countByNombreNotId", Long.class)
                .setParameter("nombre", entity.getNombre().trim())
                .setParameter("idEtapa", entity.getIdEtapaAdmision())
                .getSingleResult();

        if (duplicados > 0) {
            throw new IllegalArgumentException("No se puede actualizar. El nombre de etapa '" + entity.getNombre() + "' ya está en uso.");
        }
        return super.actualizar(entity);
    }

    private void validarReglasNegocio(EtapasAdmision entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad de etapa de admisión no puede ser nula.");
        }
        if (entity.getNombre() == null || entity.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la etapa es un campo obligatorio.");
        }
        if (entity.getCantidadPreguntasRequeridas() == null || entity.getCantidadPreguntasRequeridas() <= 0) {
            throw new IllegalArgumentException("La etapa debe requerir al menos 1 pregunta válida.");
        }
        if (entity.getPuntajeMinimo() != null && entity.getPuntajeMaximo() != null) {
            if (entity.getPuntajeMinimo().compareTo(entity.getPuntajeMaximo()) > 0) {
                throw new IllegalArgumentException("Inconsistencia en el rango de notas: El puntaje mínimo ("
                        + entity.getPuntajeMinimo() + ") no puede superar al puntaje máximo (" + entity.getPuntajeMaximo() + ").");
            }
        }
    }

    public EtapasAdmision findByNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o estar en blanco");
        }
        try {
            return em.createNamedQuery("EtapasAdmision.findByNombre", EtapasAdmision.class)
                    .setParameter("nombre", nombre.trim())
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar la etapa por nombre.", e);
        }
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Recupera el listado de etapas cuyos rangos de notas permiten la clasificación del puntaje proveído.
     */
    public List<EtapasAdmision> findEtapasAprobadasPorPuntaje(BigDecimal puntajeObtenido) {
        if (puntajeObtenido == null) {
            throw new IllegalArgumentException("El puntaje del aspirante es requerido para calcular las etapas aprobadas.");
        }
        try {
            return em.createNamedQuery("EtapasAdmision.findEtapasAprobadasPorPuntaje", EtapasAdmision.class)
                    .setParameter("puntajeObtenido", puntajeObtenido)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error de infraestructura al calcular la admisión por puntaje.", e);
        }
    }
}