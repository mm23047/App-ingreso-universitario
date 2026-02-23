package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.validator.ValidatorException;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DefaultFrm<T> implements Serializable {

    protected ESTADO_CRUD estado = ESTADO_CRUD.NADA;
    protected String nombreBean;
    protected LazyDataModel<T> modelo;
    protected T registro;
    protected int pageSize = 5;

    // Métodos abstractos que cada bean debe implementar
    protected abstract FacesContext getFacesContext();

    protected abstract IngresoDAOInterface<T> getDao();

    protected abstract T nuevoRegistro();

    protected abstract T buscarRegistroPorId(Object id);

    protected abstract String getIdAsText(T r);

    protected abstract T getIdByText(String id);

    protected abstract T createNewEntity();

    protected abstract Object getEntityId(T entity);

    protected abstract String getEntityName();

    @PostConstruct
    public void inicializar() {
        inicializarRegistros();
        inicializarListas();
    }

    public void inicializarListas() {
        // Implementación por defecto - puede ser sobrescrita
    }

    public void inicializarRegistros() {
        try {
            this.modelo = new LazyDataModel<T>() {

                @Override
                public String getRowKey(T object) {
                    if (object != null) {
                        try {
                            return getIdAsText(object);
                        } catch (Exception ex) {
                            Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return null;
                }

                @Override
                public T getRowData(String rowKey) {
                    if (rowKey != null) {
                        try {
                            return getIdByText(rowKey);
                        } catch (Exception ex) {
                            Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return null;
                }

                @Override
                public int count(Map<String, FilterMeta> map) {
                    try {
                        return getDao().count();
                    } catch (Exception e) {
                        Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, "Error al contar registros", e);
                        return 0;
                    }
                }

                @Override
                public List<T> load(int first, int max, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {
                    try {
                        return getDao().findRange(first, max);
                    } catch (Exception e) {
                        Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, "Error al cargar registros", e);
                        return Collections.emptyList();
                    }
                }
            };
        } catch (Exception e) {
            enviarMensajeError("Error al inicializar registros: " + e.getMessage());
            Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, "Error en inicializarRegistros", e);
        }
    }

    public void seleccionarRegistro(SelectEvent<T> event) {
        if (event != null && event.getObject() != null) {
            this.registro = event.getObject();
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
    }

    public void btnNuevoHandler(ActionEvent actionEvent) {
        try {
            limpiarFormulario();
            this.estado = ESTADO_CRUD.CREAR;
            this.registro = nuevoRegistro();
            configurarNuevoRegistro();
            enviarMensajeExito(getFacesContext().getApplication()
                    .getResourceBundle(getFacesContext(), "crud")
                    .getString("frm.botones.formListo"));
        } catch (Exception e) {
            enviarMensajeError("Error al preparar nuevo registro: " + e.getMessage());
            Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, "Error en btnNuevoHandler", e);
            limpiarFormulario();
        }
    }

    public void btnGuardarHandler(ActionEvent actionEvent) {
        try {
            if (registro != null) {
                getDao().crear(registro);
                enviarMensajeExito(getFacesContext().getApplication()
                        .getResourceBundle(getFacesContext(), "crud")
                        .getString("frm.botones.creado"));
                limpiarFormulario();
                this.inicializarRegistros();
                return;
            }
        } catch (Exception e) {
            enviarMensaje("Error al crear el registro: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
            return;
        }
        enviarMensaje("El registro a crear no puede ser nulo", FacesMessage.SEVERITY_ERROR);
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
    }

    public void btnModificarHandler(ActionEvent event) {
        try {
            if (this.registro == null || getEntityId(registro) == null) {
                enviarMensajeError("No hay registro seleccionado para modificar");
                return;
            }
            T registroExistente = buscarRegistroPorId(getEntityId(registro));
            if (registroExistente == null) {
                enviarMensajeError("El registro seleccionado no existe en la base de datos");
                return;
            }
            getDao().actualizar(registro);
            inicializarRegistros();
            enviarMensajeExito(getFacesContext().getApplication()
                    .getResourceBundle(getFacesContext(), "crud")
                    .getString("frm.botones.opModificar"));
            limpiarFormulario();
        } catch (Exception e) {
            enviarMensajeError("Error al modificar el registro: " + e.getMessage());
            Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, "Error en btnModificarHandler", e);
        }
    }

    public void btnEliminarHandler(ActionEvent actionEvent) {
        try {
            if (registro != null && getEntityId(registro) != null) {
                getDao().eliminar(registro);
                inicializarRegistros();
                enviarMensajeExito(getFacesContext().getApplication()
                        .getResourceBundle(getFacesContext(), "crud")
                        .getString("frm.botones.opEliminar"));
                limpiarFormulario();
            } else {
                enviarMensajeError("No hay registro seleccionado para eliminar");
            }
        } catch (Exception e) {
            enviarMensajeError("Error al eliminar: " + e.getMessage());
        }
    }

    public void btnCancelarHandler(ActionEvent actionEvent) {
        try {
            limpiarFormulario();
            enviarMensajeExito(getFacesContext().getApplication()
                    .getResourceBundle(getFacesContext(), "crud")
                    .getString("frm.botones.opCancelar"));
        } catch (Exception e) {
            enviarMensajeError("Error al cancelar operación: " + e.getMessage());
            Logger.getLogger(DefaultFrm.class.getName()).log(Level.SEVERE, "Error en btnCancelarHandler", e);
            limpiarFormulario();
        }
    }

    // Validadores
    public void validarNombre(FacesContext facesContext, UIComponent uiComponent, Object nombre) {
        if (nombre == null || nombre.toString().isEmpty()) {
            throw new ValidatorException(new FacesMessage("El nombre no puede estar vacío"));
        }
        String nom = nombre.toString();
        if (nom.trim().length() < 3 || nom.trim().length() > 155) {
            throw new ValidatorException(new FacesMessage("El nombre debe tener entre 3 y 155 caracteres"));
        }
    }

    // Métodos de utilidad
    protected void limpiarFormulario() {
        this.registro = null;
        this.estado = ESTADO_CRUD.NADA;
    }

    protected void configurarNuevoRegistro() {
        // Implementación por defecto vacía - puede ser sobrescrita
    }

    protected void enviarMensaje(String mensaje, FacesMessage.Severity severity) {
        String summary;
        if (FacesMessage.SEVERITY_ERROR.equals(severity)) {
            summary = "Error";
        } else if (FacesMessage.SEVERITY_WARN.equals(severity)) {
            summary = "Advertencia";
        } else {
            summary = "Éxito";
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, mensaje));
    }

    protected void enviarMensajeExito(String mensaje) {
        enviarMensaje(mensaje, FacesMessage.SEVERITY_INFO);
    }

    protected void enviarMensajeError(String mensaje) {
        enviarMensaje(mensaje, FacesMessage.SEVERITY_ERROR);
    }

    protected void enviarMensajeAdvertencia(String mensaje) {
        enviarMensaje(mensaje, FacesMessage.SEVERITY_WARN);
    }

    // Getters y Setters
    public String getNombreBean() {
        return nombreBean;
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }

    public LazyDataModel<T> getModelo() {
        return modelo;
    }

    public void setModelo(LazyDataModel<T> modelo) {
        this.modelo = modelo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
