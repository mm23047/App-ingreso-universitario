package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class OpcionesRespuestaDAO extends IngresoDefaultDataAccess<OpcionesRespuesta> implements Serializable {

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public OpcionesRespuestaDAO() {
        super(OpcionesRespuesta.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public OpcionesRespuesta leer(Object id) {
        OpcionesRespuesta entity = super.leer(id);
        if (entity != null) {
            // Asegura atributos completos aunque el cache tenga una referencia parcial (id-only)
            em.refresh(entity);
        }
        return entity;
    }

    public List<OpcionesRespuesta> findByPreguntaId(UUID preguntaId, int first, int max) {
        return em.createNamedQuery("OpcionesRespuesta.findByPreguntaId", OpcionesRespuesta.class)
                .setParameter("preguntaId", preguntaId)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }

    public int countByPreguntaId(UUID preguntaId) {
        Long total = em.createNamedQuery("OpcionesRespuesta.countByPreguntaId", Long.class)
                .setParameter("preguntaId", preguntaId)
                .getSingleResult();
        return total != null ? total.intValue() : 0;
    }

}
