package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionesAulaPupitreDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.UUID;

@Named
@ViewScoped
public class AsignacionesAulaPupitreFrm extends DefaultFrm<AsignacionesAulaPupitre> {

    @Inject
    AsignacionesAulaPupitreDAO asignacionesAulaPupitreDAO;

    @Inject
    InscripcionesPruebaDAO inscripcionesPruebaDAO;

    @Inject
    AulasExamanDAO aulasExamanDAO;

    private List<InscripcionesPrueba> inscripcionesDisponibles = Collections.emptyList();
    private List<AulasExaman> aulasDisponibles = Collections.emptyList();

    public AsignacionesAulaPupitreFrm() {
        this.nombreBean = "Asignaciones de Aula y Pupitre";
    }

    /**
     * Carga las listas auxiliares (inscripciones y aulas) necesarias para los
     * dropdowns del formulario. Se invoca automáticamente desde {@code inicializar()}
     * vía la implementación base.
     */
    @Override
    public void inicializarListas() {
        try {
            this.inscripcionesDisponibles = inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.inscripcionesDisponibles = Collections.emptyList();
            Logger.getLogger(AsignacionesAulaPupitreFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar inscripciones disponibles", e);
        }
        try {
            this.aulasDisponibles = aulasExamanDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.aulasDisponibles = Collections.emptyList();
            Logger.getLogger(AsignacionesAulaPupitreFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar aulas disponibles", e);
        }
    }

    @Override
    protected String getIdAsText(AsignacionesAulaPupitre r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected AsignacionesAulaPupitre getIdByText(String id) {
        if (id != null) {
            try {
                UUID buscado = UUID.fromString(id);
                return asignacionesAulaPupitreDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(AsignacionesAulaPupitreFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<AsignacionesAulaPupitre> getDao() {
        return asignacionesAulaPupitreDAO;
    }

    @Override
    protected AsignacionesAulaPupitre nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected AsignacionesAulaPupitre buscarRegistroPorId(Object id) {
        if (id != null && asignacionesAulaPupitreDAO != null) {
            return asignacionesAulaPupitreDAO.leer(id);
        }
        return null;
    }

    @Override
    protected AsignacionesAulaPupitre createNewEntity() {
        AsignacionesAulaPupitre nueva = new AsignacionesAulaPupitre();
        nueva.setPupitre("");
        return nueva;
    }

    @Override
    public void seleccionarRegistro(AsignacionesAulaPupitre registroSeleccionado) {
        if (registroSeleccionado != null) {
            this.registro = registroSeleccionado;
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
    }

    @Override
    protected Object getEntityId(AsignacionesAulaPupitre entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // Sin inicialización de relaciones — el usuario las elige desde los dropdowns
    }

    // ==================== Listas auxiliares para la vista ====================

    public List<InscripcionesPrueba> getInscripcionesDisponibles() {
        return inscripcionesDisponibles;
    }

    public List<AulasExaman> getAulasDisponibles() {
        return aulasDisponibles;
    }
}
