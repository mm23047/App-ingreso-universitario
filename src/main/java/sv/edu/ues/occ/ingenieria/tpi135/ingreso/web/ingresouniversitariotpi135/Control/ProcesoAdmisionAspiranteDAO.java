package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Stateless
@LocalBean
public class ProcesoAdmisionAspiranteDAO extends IngresoDefaultDataAccess<ProcesoAdmisionAspirante> implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "ingresoPU")
    EntityManager em;

    public ProcesoAdmisionAspiranteDAO() {
        super(ProcesoAdmisionAspirante.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Asigna la carrera final a un aspirante según prioridad de carreras elegidas
     * y cupos disponibles en la etapa actual del proceso.
     *
     * Reglas:
     * - Se recorren las carreras elegidas ordenadas por prioridad ascendente.
     * - Se asigna la primera carrera con cupos > 0 (para la prueba y etapa actual).
     * - Se decrementa el cupo consumido.
     * - Si ninguna tiene cupo, se marca como NO_ADMITIDO.
     *
     * @param idInscripcion id del proceso/inscripción
     * @return Proceso actualizado, o null si no existe
     */
    public ProcesoAdmisionAspirante asignarCarreraFinal(UUID idInscripcion) {
        if (idInscripcion == null) {
            throw new IllegalArgumentException("idInscripcion");
        }

        ProcesoAdmisionAspirante proceso = em.find(ProcesoAdmisionAspirante.class, idInscripcion);
        if (proceso == null) {
            return null;
        }

        UUID idPrueba = proceso.getInscripcionesPrueba().getPruebaAdmision().getIdPruebaAdmision();
        UUID idEtapa = proceso.getIdEtapaActual().getIdEtapaAdmision();

        List<CarrerasElegida> elegidas = em.createNamedQuery("ProcesoAdmisionAspirante.findCarrerasElegidas", CarrerasElegida.class)
                .setParameter("idInscripcion", idInscripcion)
                .getResultList();

        for (CarrerasElegida elegida : elegidas) {
            String idCarrera = elegida.getCatalogoCarrera().getIdCarrera();
            CuposCarrera cupos = buscarCupos(idPrueba, idCarrera, idEtapa);

            if (cupos != null && cupos.getCupos() != null && cupos.getCupos() > 0) {
                cupos.setCupos(cupos.getCupos() - 1);
                proceso.setCarreraAsignada(em.getReference(CatalogoCarrera.class, idCarrera));
                proceso.setEstado("ADMITIDO");
                return proceso;
            }
        }

        proceso.setCarreraAsignada(null);
        proceso.setEstado("NO_ADMITIDO");
        return proceso;
    }

    private CuposCarrera buscarCupos(UUID idPrueba, String idCarrera, UUID idEtapa) {
        try {
            return em.createNamedQuery("ProcesoAdmisionAspirante.findCuposCarrera", CuposCarrera.class)
                    .setParameter("idPrueba", idPrueba)
                    .setParameter("idCarrera", idCarrera)
                    .setParameter("idEtapa", idEtapa)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * REGLA DE NEGOCIO PRINCIPAL (FASE 2):
     * Procesa en lote a todos los estudiantes de una etapa en específico, garantizando que
     * los mejores puntajes consuman los cupos de las carreras primero.
     */
    public void procesarAsignacionMasiva(UUID idEtapa) {
        if (idEtapa == null) {
            throw new IllegalArgumentException("El id de la etapa es requerido.");
        }

        // 1. Obtener los aspirantes ordenados de manera estricta por nota descenente
        List<ProcesoAdmisionAspirante> aspirantesOrdenados = em.createNamedQuery(
                        "ProcesoAdmisionAspirante.findPendientesPorPuntaje", ProcesoAdmisionAspirante.class)
                .setParameter("idEtapa", idEtapa)
                .getResultList();

        // 2. Iterar sobre ellos aplicando secuencialmente la asignación individual
        for (ProcesoAdmisionAspirante aspirante : aspirantesOrdenados) {
            this.asignarCarreraFinal(aspirante.getIdProcesoAdmisionAspirante());

            // Separar de la memoria de primer nivel periódicamente si el lote es masivo (evita saturar la RAM)
            em.flush();
        }
    }

}