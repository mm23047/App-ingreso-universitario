package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.UUID;

@Named
@ViewScoped
public class TurnosExamanFrm extends DefaultFrm<TurnosExaman> {

    @Inject
    TurnosExamanDAO turnosExamanDAO;

    @Inject
    PruebasAdmisionDAO pruebasAdmisionDAO;

    private List<PruebasAdmision> pruebasDisponibles = Collections.emptyList();

    public TurnosExamanFrm() {
        this.nombreBean = "Turnos de Examen";
    }

    /**
     * Carga la lista de pruebas de admisión disponibles para el dropdown de {@code idPrueba}.
     * Se invoca automáticamente desde {@code inicializar()} vía la clase base.
     */
    @Override
    public void inicializarListas() {
        try {
            this.pruebasDisponibles = pruebasAdmisionDAO.findRange(0, Integer.MAX_VALUE);
        } catch (Exception e) {
            this.pruebasDisponibles = Collections.emptyList();
            Logger.getLogger(TurnosExamanFrm.class.getName())
                    .log(Level.SEVERE, "Error al cargar pruebas disponibles", e);
        }
    }

    @Override
    protected String getIdAsText(TurnosExaman r) {
        if (r != null && r.getId() != null) {
            return r.getId().toString();
        }
        return null;
    }

    @Override
    protected TurnosExaman getIdByText(String id) {
        if (id != null) {
            try {
                UUID buscado = UUID.fromString(id);
                return turnosExamanDAO.leer(buscado);
            } catch (Exception e) {
                Logger.getLogger(TurnosExamanFrm.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected IngresoDAOInterface<TurnosExaman> getDao() {
        return turnosExamanDAO;
    }

    @Override
    protected TurnosExaman nuevoRegistro() {
        return createNewEntity();
    }

    @Override
    protected TurnosExaman buscarRegistroPorId(Object id) {
        if (id != null && turnosExamanDAO != null) {
            return turnosExamanDAO.leer(id);
        }
        return null;
    }

    @Override
    protected TurnosExaman createNewEntity() {
        TurnosExaman nuevo = new TurnosExaman();
        nuevo.setNombreTurno("");
        nuevo.setFecha(LocalDate.now());
        nuevo.setHoraInicio(LocalTime.of(7, 0));
        nuevo.setHoraFin(LocalTime.of(9, 0));
        return nuevo;
    }

    @Override
    protected Object getEntityId(TurnosExaman entity) {
        return entity != null ? entity.getId() : null;
    }

    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    @Override
    protected void configurarNuevoRegistro() {
        // La relación idPrueba la elige el usuario desde el dropdown
    }

    // ==================== Lista auxiliar para la vista ====================

    public List<PruebasAdmision> getPruebasDisponibles() {
        return pruebasDisponibles;
    }
}
