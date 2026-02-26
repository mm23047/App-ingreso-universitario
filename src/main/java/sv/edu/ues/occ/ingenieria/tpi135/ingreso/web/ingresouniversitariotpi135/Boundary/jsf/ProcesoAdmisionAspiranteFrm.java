package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ProcesoAdmisionAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class ProcesoAdmisionAspiranteFrm extends DefaultFrm<ProcesoAdmisionAspirante> {

    @Inject
    ProcesoAdmisionAspiranteDAO procesoAdmisionAspiranteDAO;

    @Inject
    InscripcionesPruebaDAO inscripcionesPruebaDAO;

    @Inject
    EtapasAdmisionDAO etapasAdmisionDAO;

    @Inject
    CatalogoCarreraDAO catalogoCarreraDAO;

    private List<InscripcionesPrueba> inscripcionesDisponibles = Collections.emptyList();
    private List<EtapasAdmision> etapasDisponibles = Collections.emptyList();
    private List<CatalogoCarrera> carrerasDisponibles = Collections.emptyList();

    public ProcesoAdmisionAspiranteFrm() {
        this.nombreBean = "Proceso Admision Aspirante";
    }

    /**
     * Carga las listas auxiliares de inscripciones, etapas y carreras para los
     * dropdowns del formulario. Cada bloque maneja su excepcion de forma
     * independiente para que un fallo no bloquee las demas listas.
     */
    @Override
    public void inicializarListas() {
        try {
            this.inscripcionesDisponibles = inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.inscripcionesDisponibles = Collections.emptyList();
            Logger.getLogger(ProcesoAdmisionAspiranteFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar inscripciones disponibles", e);
        }
        try {
            this.etapasDisponibles = etapasAdmisionDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.etapasDisponibles = Collections.emptyList();
            Logger.getLogger(ProcesoAdmisionAspiranteFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar etapas disponibles", e);
        }
        try {
            this.carrerasDisponibles = catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.carrerasDisponibles = Collections.emptyList();
            Logger.getLogger(ProcesoAdmisionAspiranteFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar carreras disponibles", e);
        }
    }

    /**
     * El id de ProcesoAdmisionAspirante se mapea desde InscripcionesPrueba (@MapsId),
     * por lo que se serializa como el Integer del id de inscripcion.
     */
    @Override
    protected String getIdAsText(ProcesoAdmisionAspirante r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected ProcesoAdmisionAspirante getIdByText(String id) {
        if (id != null) {
            try {
                Integer buscado = Integer.parseInt(id);
                return procesoAdmisionAspiranteDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(ProcesoAdmisionAspiranteFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<ProcesoAdmisionAspirante> getDao() {
        return procesoAdmisionAspiranteDAO;
    }

    @Override
    protected ProcesoAdmisionAspirante nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected ProcesoAdmisionAspirante buscarRegistroPorId(Object id) {
        if (id != null && procesoAdmisionAspiranteDAO != null) {
            return procesoAdmisionAspiranteDAO.leer(id);
        }
        return null;
    }

    @Override
    protected ProcesoAdmisionAspirante createNewEntity() {
        ProcesoAdmisionAspirante nuevo = new ProcesoAdmisionAspirante();
        nuevo.setEstado("");
        // inscripcionesPrueba e idEtapaActual son obligatorios y se asignan desde dropdowns.
        // carreraAsignada es nullable: el usuario puede dejarlo vacio.
        return nuevo;
    }

    @Override
    protected Object getEntityId(ProcesoAdmisionAspirante entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // Las relaciones se eligen desde los dropdowns en la vista
    }

    // ==================== Listas auxiliares para la vista ====================

    public List<InscripcionesPrueba> getInscripcionesDisponibles() {
        return inscripcionesDisponibles;
    }

    public List<EtapasAdmision> getEtapasDisponibles() {
        return etapasDisponibles;
    }

    public List<CatalogoCarrera> getCarrerasDisponibles() {
        return carrerasDisponibles;
    }
}
