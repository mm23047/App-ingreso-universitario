package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenesRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.OpcionesRespuestaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class RespuestasExamanFrm extends DefaultFrm<RespuestasExaman> {

    @Inject
    RespuestasExamanDAO respuestasExamanDAO;

    @Inject
    ExamenesRealizadoDAO examenesRealizadoDAO;

    @Inject
    BancoPreguntaDAO bancoPreguntaDAO;

    @Inject
    OpcionesRespuestaDAO opcionesRespuestaDAO;

    private List<ExamenesRealizado> examenesDisponibles = Collections.emptyList();
    private List<BancoPregunta> preguntasDisponibles = Collections.emptyList();
    private List<OpcionesRespuesta> opcionesDisponibles = Collections.emptyList();

    public RespuestasExamanFrm() {
        this.nombreBean = "Respuestas de Examen";
    }

    /**
     * Carga las listas auxiliares de exámenes, preguntas y opciones necesarias
     * para los dropdowns del formulario. Cada bloque maneja su excepción de forma
     * independiente para evitar que un fallo bloquee las demás listas.
     */
    @Override
    public void inicializarListas() {
        try {
            this.examenesDisponibles = examenesRealizadoDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.examenesDisponibles = Collections.emptyList();
            Logger.getLogger(RespuestasExamanFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar exámenes disponibles", e);
        }
        try {
            this.preguntasDisponibles = bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.preguntasDisponibles = Collections.emptyList();
            Logger.getLogger(RespuestasExamanFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar preguntas disponibles", e);
        }
        try {
            this.opcionesDisponibles = opcionesRespuestaDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.opcionesDisponibles = Collections.emptyList();
            Logger.getLogger(RespuestasExamanFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar opciones disponibles", e);
        }
    }

    @Override
    protected String getIdAsText(RespuestasExaman r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected RespuestasExaman getIdByText(String id) {
        if (id != null) {
            try {
                Integer buscado = Integer.parseInt(id);
                return respuestasExamanDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(RespuestasExamanFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<RespuestasExaman> getDao() {
        return respuestasExamanDAO;
    }

    @Override
    protected RespuestasExaman nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected RespuestasExaman buscarRegistroPorId(Object id) {
        if (id != null && respuestasExamanDAO != null) {
            return respuestasExamanDAO.leer(id);
        }
        return null;
    }

    @Override
    protected RespuestasExaman createNewEntity() {
        // idExamen e idPregunta son obligatorios y se asignan desde dropdowns en la vista.
        // idOpcionSeleccionada es nullable: el usuario puede dejarlo vacío.
        return new RespuestasExaman();
    }

    @Override
    protected Object getEntityId(RespuestasExaman entity) {
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

    public List<ExamenesRealizado> getExamenesDisponibles() {
        return examenesDisponibles;
    }

    public List<BancoPregunta> getPreguntasDisponibles() {
        return preguntasDisponibles;
    }

    public List<OpcionesRespuesta> getOpcionesDisponibles() {
        return opcionesDisponibles;
    }
}
