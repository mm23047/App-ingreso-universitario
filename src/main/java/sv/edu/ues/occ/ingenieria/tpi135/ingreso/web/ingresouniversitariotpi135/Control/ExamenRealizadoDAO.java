package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ExamenRealizadoDAO extends IngresoDefaultDataAccess<ExamenRealizado> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ExamenRealizadoDAO() {
        super(ExamenRealizado.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * NUEVO MÉTODO DE NEGOCIO ENCAPSULADO
     * Cuenta cuántos exámenes han sido generados usando una clave específica.
     */
    public long countByClaveExamen(UUID idClave) {
        if (idClave == null) {
            throw new IllegalArgumentException("El ID de la clave no puede ser nulo.");
        }
        return em.createNamedQuery("ExamenRealizado.countByClave", Long.class)
                .setParameter("idClave", idClave)
                .getSingleResult();
    }
    @Override
    public void crear(ExamenRealizado entity) {
        validarConsistenciaEntidad(entity);
        super.crear(entity);
    }

    @Override
    public ExamenRealizado actualizar(ExamenRealizado entity) {
        validarConsistenciaEntidad(entity);
        return super.actualizar(entity);
    }

    private void validarConsistenciaEntidad(ExamenRealizado entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad ExamenRealizado no puede ser nula.");
        }
        if (entity.getPuntajeFinal() != null && entity.getPuntajeFinal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El puntaje final calculado no puede poseer valores negativos.");
        }
    }

    public List<ExamenRealizado> findByAspiranteId(UUID aspiranteId) {
        if (aspiranteId == null) {
            throw new IllegalArgumentException("El ID del aspirante no puede ser nulo.");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByAspiranteId", ExamenRealizado.class)
                    .setParameter("aspiranteId", aspiranteId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar los exámenes del aspirante.", e);
        }
    }

    public List<ExamenRealizado> findByPruebaId(UUID pruebaId) {
        if (pruebaId == null) {
            throw new IllegalArgumentException("El ID de la prueba no puede ser nulo.");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByPruebaId", ExamenRealizado.class)
                    .setParameter("pruebaId", pruebaId)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al recuperar los exámenes por ID de prueba.", e);
        }
    }

    public ExamenRealizado calificarExamen(UUID examenId) {
        if (examenId == null) {
            throw new IllegalArgumentException("El ID del examen NO debe de ser NULL");
        }
        try {
            ExamenRealizado examen = em.find(ExamenRealizado.class, examenId);
            if (examen == null) {
                return null;
            }

            // NUEVO: Escudos contra NullPointerException
            if (examen.getClaveExamen() == null || examen.getClaveExamen().getIdClaveExaman() == null) {
                throw new IllegalStateException("El examen no tiene una clave asignada para poder calificarse.");
            }
            if (examen.getInscripcionesPrueba() == null) {
                throw new IllegalStateException("El examen no tiene una inscripción vinculada.");
            }

            // CORRECCIÓN: Extracción correcta del ID de la clave
            UUID claveId = examen.getClaveExamen().getIdClaveExaman();

            // 1. Llamada al NamedQuery para el total de preguntas
            Long totalPreguntasClave = em.createNamedQuery("ExamenRealizado.countPreguntasByClave", Long.class)
                    .setParameter("idClave", claveId)
                    .getSingleResult();

            // 2. Llamada al NamedQuery para las respuestas correctas
            Long preguntasCorrectas = em.createNamedQuery("ExamenRealizado.countRespuestasCorrectas", Long.class)
                    .setParameter("idExamen", examenId)
                    .setParameter("idClave", claveId)
                    .getSingleResult();

            BigDecimal puntajeMaximo = examen.getEtapaAdmision() != null && examen.getEtapaAdmision().getPuntajeMaximo() != null
                    ? examen.getEtapaAdmision().getPuntajeMaximo()
                    : BigDecimal.TEN;

            BigDecimal puntajeCalculado = BigDecimal.ZERO;
            if (totalPreguntasClave != null && totalPreguntasClave > 0) {
                puntajeCalculado = puntajeMaximo
                        .multiply(BigDecimal.valueOf(preguntasCorrectas != null ? preguntasCorrectas : 0L))
                        .divide(BigDecimal.valueOf(totalPreguntasClave), 2, RoundingMode.HALF_UP);
            }

            examen.setPuntajeFinal(puntajeCalculado);

            if (examen.getInscripcionesPrueba() != null) {
                InscripcionesPrueba inscripcion = examen.getInscripcionesPrueba();
                inscripcion.setEstado("CALIFICADO");
                em.merge(inscripcion);
            }

            ExamenRealizado actualizado = em.merge(examen);
            em.flush();
            em.refresh(actualizado);
            return actualizado;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot access db", e);
        }
    }

    /**
     * MÉTODOS DE NEGOCIO (FASE 1 COMPLETADA)
     * Retorna el listado de calificaciones en orden descendente para la asignación competitiva de plazas físicas.
     */
    public List<ExamenRealizado> findRankingByPruebaAndEtapa(UUID idPrueba, UUID idEtapa, int offset, int limit) {
        if (idPrueba == null || idEtapa == null) {
            throw new IllegalArgumentException("Los IDs de prueba y etapa son mandatorios.");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findRankingByPruebaAndEtapa", ExamenRealizado.class)
                    .setParameter("idPrueba", idPrueba)
                    .setParameter("idEtapa", idEtapa)
                    .setFirstResult(offset) // <-- ESTA ES LA MAGIA DEL OFFSET
                    .setMaxResults(limit)   // <-- ESTE ES EL LIMIT
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular el orden de mérito de los exámenes.", e);
        }
    }

    /**
     * Se sobrescribe el método leer del padre para incluir el JOIN FETCH de las 3 relaciones
     * (Inscripción, Clave y Etapa) y prevenir el LazyInitializationException al enviar a REST.
     */
    @Override
    public ExamenRealizado leer(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByIdConRelaciones", ExamenRealizado.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null; // Comportamiento esperado si no existe el ID
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al leer registro de ExamenRealizado con relaciones", ex);
        }
    }

    /**
     * MÉTODO DE NEGOCIO CORREGIDO Y VALIDADO CON NAMEDQUERIES
     */
    public ExamenRealizado iniciarExamenAspirante(UUID idInscripcion, UUID idEtapa) {
        if (idInscripcion == null || idEtapa == null) {
            throw new IllegalArgumentException("La inscripción y la etapa son campos obligatorios.");
        }

        // REGLA DE NEGOCIO PROTECTORA: Verificar si ya existe un intento previo para no romper el Unique Constraint
        try {
            em.createNamedQuery("ExamenRealizado.findByInscripcionAndEtapa", ExamenRealizado.class)
                    .setParameter("idInscripcion", idInscripcion)
                    .setParameter("idEtapa", idEtapa)
                    .getSingleResult();
            throw new IllegalStateException("Ya existe un examen registrado para la inscripción y etapa indicadas.");
        } catch (NoResultException e) {
            // El comportamiento es correcto: No existe, procedemos a crearlo.
        }

        InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, idInscripcion);
        if (inscripcion == null) {
            throw new IllegalArgumentException("La inscripción proveída no existe.");
        }
        UUID idAspirante = inscripcion.getAspiranteDato().getId();

        TurnosExamen turnoActivo;
        try {
            turnoActivo = em.createNamedQuery("TurnosExamen.findTurnoActivoAspirante", TurnosExamen.class)
                    .setParameter("idAspirante", idAspirante)
                    .setParameter("fechaActual", LocalDate.now())
                    .setParameter("horaActual", LocalTime.now())
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalStateException("El aspirante no posee un turno asignado para este horario o el aula no está disponible.");
        }

        UUID idPrueba = turnoActivo.getPruebaAdmision().getIdPruebaAdmision();

        // CORRECCIÓN DE ACOPLAMIENTO: Consumo a través del NamedQuery estático de la Entidad
        List<ClavesExamen> clavesDisponibles = em.createNamedQuery("ExamenRealizado.findClavesByPrueba", ClavesExamen.class)
                .setParameter("idPrueba", idPrueba)
                .getResultList();

        if (clavesDisponibles.isEmpty()) {
            throw new IllegalStateException("No se han configurado claves de examen para esta prueba de admisión.");
        }

        // ALGORITMO DE ASIGNACIÓN ALEATORIO
        Collections.shuffle(clavesDisponibles);

        // REGLA DE NEGOCIO ADICIONAL: Validar completitud antes de asignar
        ClavesExamen claveAsignada = null;
        for (ClavesExamen clave : clavesDisponibles) {
            Long cantidadPreguntas = em.createNamedQuery("ExamenRealizado.countPreguntasByClave", Long.class)
                    .setParameter("idClave", clave.getIdClaveExaman())
                    .getSingleResult();

            if (cantidadPreguntas != null && cantidadPreguntas > 0) {
                claveAsignada = clave;
                break;
            }
        }

        if (claveAsignada == null) {
            throw new IllegalStateException("Las claves disponibles carecen de preguntas configuradas.");
        }

        ExamenRealizado nuevoExamen = new ExamenRealizado();
        nuevoExamen.setInscripcionesPrueba(inscripcion);
        nuevoExamen.setClaveExamen(claveAsignada);

        EtapasAdmision etapa = em.find(EtapasAdmision.class, idEtapa);
        nuevoExamen.setEtapaAdmision(etapa);

        em.persist(nuevoExamen);
        em.flush();

        return nuevoExamen;
    }
    public List<ExamenRealizado> findByAspiranteDui(String dui) {
        if (dui == null || dui.isBlank()) {
            throw new IllegalArgumentException("El DUI no puede ser nulo o vacío.");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByAspiranteDui", ExamenRealizado.class)
                    .setParameter("dui", dui)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar exámenes por DUI.", e);
        }
    }

    public List<ExamenRealizado> findByAspiranteCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo no puede ser nulo o vacío.");
        }
        try {
            return em.createNamedQuery("ExamenRealizado.findByAspiranteCorreo", ExamenRealizado.class)
                    .setParameter("correo", correo)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalStateException("Error al consultar exámenes por correo.", e);
        }
    }
}