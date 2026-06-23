package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class BancoPreguntaDAO extends IngresoDefaultDataAccess<BancoPregunta> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PARAM_ENUNCIADO = "enunciado";

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public BancoPreguntaDAO() {
        super(BancoPregunta.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(BancoPregunta entity) {
        if (entity == null || entity.getEnunciado() == null || entity.getEnunciado().isBlank()) {
            throw new IllegalArgumentException("El enunciado de la pregunta es requerido.");
        }
        if (entity.getTema() == null || entity.getTema().getAreaConocimiento() == null) {
            throw new IllegalArgumentException("La pregunta debe estar asociada a un Tema y Área válidos.");
        }

        String enunciadoSaneado = entity.getEnunciado().trim();

        // 1. VALIDACIÓN SEMÁNTICA: Si ya existe en otra área, arroja un error descriptivo de negocio
        Long areasEnConflicto = em.createNamedQuery("BancoPregunta.countConflictosArea", Long.class)
                .setParameter(PARAM_ENUNCIADO, enunciadoSaneado)
                .setParameter("idAreaActual", entity.getTema().getAreaConocimiento().getIdAreaConocimiento())
                .getSingleResult();

        if (areasEnConflicto > 0) {
            throw new IllegalArgumentException("Inconsistencia de Dominio: Este enunciado ya se encuentra registrado bajo un Área de Conocimiento diferente.");
        }

        // 2. VALIDACIÓN DE UNICIDAD ESTÁNDAR: Evita cualquier tipo de duplicado general antes del persist
        if (existsByEnunciado(enunciadoSaneado)) {
            throw new IllegalArgumentException("Ya existe una pregunta registrada con este mismo enunciado en este tema.");
        }

        super.crear(entity);
    }

    @Override
    public BancoPregunta actualizar(BancoPregunta entity) {
        if (entity == null || entity.getIdBancoPregunta() == null) {
            throw new IllegalArgumentException("Entidad no válida para actualización.");
        }
        if (entity.getEnunciado() == null || entity.getEnunciado().isBlank()) {
            throw new IllegalArgumentException("El enunciado no puede quedar vacío al actualizar.");
        }

        // PERMISIVIDAD DE EDICIÓN: El query ignora el ID actual permitiendo correcciones de área/tema sin romper el UNIQUE
        if (countByEnunciadoDiferenteId(entity.getEnunciado(), entity.getIdBancoPregunta()) > 0) {
            throw new IllegalArgumentException("El enunciado modificado ya existe en otra pregunta del banco.");
        }

        return super.actualizar(entity);
    }

    public List<BancoPregunta> findByTema(UUID idTema) {
        if (idTema == null) {
            throw new IllegalArgumentException("El idTema suministrado no debe ser nulo.");
        }
        try {
            return em.createNamedQuery("BancoPregunta.findByTema", BancoPregunta.class)
                    .setParameter("idTema", idTema)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al recuperar las preguntas por tema.", e);
        }
    }

    /**
     * Se sobrescribe el método leer del padre para incluir el JOIN FETCH
     * y prevenir el LazyInitializationException al serializar en REST.
     */
    @Override
    public BancoPregunta leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("BancoPregunta.findByIdConTema", BancoPregunta.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            // em.find() devuelve null si no existe, replicamos ese comportamiento
            return null;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de BancoPregunta con relaciones", ex);
        }
    }

    private boolean existsByEnunciado(String enunciado) {
        Long count = em.createNamedQuery("BancoPregunta.countByEnunciado", Long.class)
                .setParameter(PARAM_ENUNCIADO, enunciado.trim())
                .getSingleResult();
        return count > 0;
    }
    private long countByEnunciadoDiferenteId(String enunciado, UUID id) {
        return em.createNamedQuery("BancoPregunta.countByEnunciadoAndNotId", Long.class)
                .setParameter(PARAM_ENUNCIADO, enunciado.trim())
                .setParameter("idBancoPregunta", id)
                .setFlushMode(jakarta.persistence.FlushModeType.COMMIT)
                .getSingleResult();
    }
}