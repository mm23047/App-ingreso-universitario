package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class TurnosExamenDAO extends IngresoDefaultDataAccess<TurnosExamen> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public TurnosExamenDAO() {
        super(TurnosExamen.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<TurnosExamen> findByPrueba(UUID idPrueba) {
        if (idPrueba == null) {
            throw new IllegalArgumentException("idPrueba no debe ser nulo");
        }
        try {
            return em.createNamedQuery("TurnosExamen.findByPrueba", TurnosExamen.class)
                    .setParameter("idPrueba", idPrueba)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder a la base de datos", e);
        }
    }

    public List<TurnosExamen> findByFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("fecha no debe ser nula");
        }
        try {
            return em.createNamedQuery("TurnosExamen.findByFecha", TurnosExamen.class)
                    .setParameter("fecha", fecha)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al acceder a la base de datos", e);
        }
    }
    /**
     * REGLA DE NEGOCIO (LOGÍSTICA):
     * Verifica si un nuevo turno (o una actualización) se cruza con los horarios de un turno existente.
     * * @param idIgnorado En caso de UPDATE, pasa el ID del turno actual para que no se traslape consigo mismo. Para INSERT, pasa null.
     */
    public boolean existeTraslape(UUID idPrueba, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, UUID idIgnorado) {
        if (idPrueba == null || fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Ningún parámetro de tiempo puede ser nulo");
        }

        Long coincidencias = em.createNamedQuery("TurnosExamen.countTraslapes", Long.class)
                .setParameter("idPrueba", idPrueba)
                .setParameter("fecha", fecha)
                .setParameter("horaInicio", horaInicio)
                .setParameter("horaFin", horaFin)
                .setParameter("idIgnorado", idIgnorado)
                .getSingleResult();

        return coincidencias > 0;
    }

    /**
     * REGLA DE NEGOCIO (EJECUCIÓN DEL EXAMEN):
     * Busca si el aspirante tiene permitido ingresar al sistema en el momento exacto de la petición.
     */
    public TurnosExamen findTurnoActivoParaAspirante(UUID idAspirante, LocalDate fechaActual, LocalTime horaActual) {
        if (idAspirante == null || fechaActual == null || horaActual == null) {
            throw new IllegalArgumentException("Parámetros de validación incompletos");
        }
        try {
            return em.createNamedQuery("TurnosExamen.findTurnoActivoAspirante", TurnosExamen.class)
                    .setParameter("idAspirante", idAspirante)
                    .setParameter("fechaActual", fechaActual)
                    .setParameter("horaActual", horaActual)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // El estudiante no tiene permiso de acceso en esta ventana de tiempo
        }
    }
}