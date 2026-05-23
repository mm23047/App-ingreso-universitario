package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoRespuesta;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class BancoRespuestaDAO extends IngresoDefaultDataAccess<BancoRespuesta> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public BancoRespuestaDAO() {
        super(BancoRespuesta.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void crear(BancoRespuesta entity) {
        if (entity == null || entity.getTextoRespuesta() == null || entity.getTextoRespuesta().isBlank()) {
            throw new IllegalArgumentException("El texto de la respuesta es requerido.");
        }

        String textoSaneado = entity.getTextoRespuesta().trim();
        boolean esGlobal = (entity.getAreaConocimiento() == null || entity.getAreaConocimiento().getIdAreaConocimiento() == null);

        if (esGlobal) {
            Long conteo = em.createNamedQuery("BancoRespuesta.countGlobalByTexto", Long.class)
                    .setParameter("textoRespuesta", textoSaneado)
                    .getSingleResult();
            if (conteo > 0) {
                throw new IllegalArgumentException("La respuesta global '" + textoSaneado + "' ya existe. Úsela en lugar de crear una nueva.");
            }
        } else {
            Long conteo = em.createNamedQuery("BancoRespuesta.countLocalByTexto", Long.class)
                    .setParameter("textoRespuesta", textoSaneado)
                    .setParameter("idArea", entity.getAreaConocimiento().getIdAreaConocimiento())
                    .getSingleResult();
            if (conteo > 0) {
                throw new IllegalArgumentException("Esta respuesta ya existe dentro de esta Área de Conocimiento.");
            }
        }

        super.crear(entity);
    }

    @Override
    public BancoRespuesta actualizar(BancoRespuesta entity) {
        if (entity == null || entity.getIdBancoRespuesta() == null) {
            throw new IllegalArgumentException("Entidad no válida para actualización.");
        }
        if (entity.getTextoRespuesta() == null || entity.getTextoRespuesta().isBlank()) {
            throw new IllegalArgumentException("El texto de la respuesta no puede quedar vacío.");
        }

        String textoSaneado = entity.getTextoRespuesta().trim();
        boolean esGlobal = (entity.getAreaConocimiento() == null || entity.getAreaConocimiento().getIdAreaConocimiento() == null);
        Long conteo;

        if (esGlobal) {
            conteo = em.createNamedQuery("BancoRespuesta.countGlobalByTextoAndNotId", Long.class)
                    .setParameter("textoRespuesta", textoSaneado)
                    .setParameter("idBancoRespuesta", entity.getIdBancoRespuesta())
                    .getSingleResult();
        } else {
            conteo = em.createNamedQuery("BancoRespuesta.countLocalByTextoAndNotId", Long.class)
                    .setParameter("textoRespuesta", textoSaneado)
                    .setParameter("idArea", entity.getAreaConocimiento().getIdAreaConocimiento())
                    .setParameter("idBancoRespuesta", entity.getIdBancoRespuesta())
                    .getSingleResult();
        }

        if (conteo > 0) {
            throw new IllegalArgumentException("El texto modificado colisiona con otra respuesta ya existente.");
        }

        return super.actualizar(entity);
    }

    /**
     * Obtiene respuestas aleatorias de una misma Área de Conocimiento para usarlas como distractores,
     * excluyendo opcionalmente la respuesta que ya se sabe que es la correcta.
     */
    @SuppressWarnings("unchecked")
    public List<BancoRespuesta> findRandomDistractoresByArea(UUID idArea, UUID idRespuestaCorrectaAExcluir, int limite) {
        if (idArea == null || limite <= 0) {
            throw new IllegalArgumentException("idArea no puede ser nulo y el límite debe ser mayor a 0.");
        }

        if (idRespuestaCorrectaAExcluir != null) {
            return em.createNamedQuery("BancoRespuesta.findRandomByAreaExcludingId")
                    .setParameter(1, idArea)
                    .setParameter(2, idRespuestaCorrectaAExcluir)
                    .setMaxResults(limite) // Reemplaza la necesidad del LIMIT ? en el SQL
                    .getResultList();
        } else {
            return em.createNamedQuery("BancoRespuesta.findRandomByArea")
                    .setParameter(1, idArea)
                    .setMaxResults(limite)
                    .getResultList();
        }
    }

    public List<BancoRespuesta> obtenerRespuestasGlobales() {
        return em.createNamedQuery("BancoRespuesta.findSoloGlobales", BancoRespuesta.class)
                .getResultList();
    }

    /**
     * Trae respuesatas de manera general, tanto globales como de X area de conocimiento
     * @param idArea si queremos de un X area
     * @return
     */
    public List<BancoRespuesta> obtenerRespuestasParaPregunta(UUID idArea) {
        return em.createNamedQuery("BancoRespuesta.findByAreaYGlobales", BancoRespuesta.class)
                .setParameter("idArea", idArea)
                .getResultList();
    }

    /**
     * Sobrescribimos el método leer del padre para evitar LazyInitializationException.
     * Usamos LEFT JOIN FETCH porque areaConocimiento puede ser nulo (respuestas globales).
     */
    @Override
    public BancoRespuesta leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("BancoRespuesta.findByIdConArea", BancoRespuesta.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null; // Replicamos el comportamiento de em.find()
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de BancoRespuesta con relaciones", ex);
        }
    }
}