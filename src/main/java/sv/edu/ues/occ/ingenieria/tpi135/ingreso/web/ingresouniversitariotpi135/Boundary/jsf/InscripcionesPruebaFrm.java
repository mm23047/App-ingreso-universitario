package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.io.Serializable;

@Named("inscripcionesPruebasFrm")
@ViewScoped
public class InscripcionesPruebaFrm extends DefaultFrm<InscripcionesPrueba> implements Serializable {

    //DAO para interactuar con la BD y hacer CRUD.
    @Inject
    InscripcionesPruebaDAO inscripcionesPruebaDAO;

    // FC Para saber el ciclo de vida de la vista actual que utiliza el usuario
    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Lo utiliza el padre para poder hacer CRUD
     * @return El DAO de la entidad utilizada por el formulario de la vista
     */
    @Override
    protected IngresoDAOInterface<InscripcionesPrueba> getDao() {
        return inscripcionesPruebaDAO;
    }

    /**
     * CRea una instancia vacia para que el usuario haga nuevo registro
     * @return la entidad a utilizar
     */
    @Override
    protected InscripcionesPrueba nuevoRegistro() {
        return createNewEntity();
    }

    /**
     * BUsca la entidad por su ID
     * @param id del registro de la ENtity
     * @return el registro de la entidad buscada
     */
    @Override
    protected InscripcionesPrueba buscarRegistroPorId(Object id) {
        if(id!=null && inscripcionesPruebaDAO!=null){
            return inscripcionesPruebaDAO.leer(id);

        }
        return null;
    }

    /**
     * COnvertidos dato de la BD a String
     * @param r entidad utilizada inscripcion Prueba
     * @return id de la entity en String
     */
    @Override
    protected String getIdAsText(InscripcionesPrueba r) {
        if(r!=null && r.getId()!=null){
            return r.getId().toString();
        }
        return null;
    }

    /**
     * COnvertimos el texto que nos entrega el JSF para poder manejarlo con java
     * @param id recibido desde JSF
     * @return objeto para ser utilizado
     */
    @Override
    protected InscripcionesPrueba getIdByText(String id) {
        if(id!=null && !id.isEmpty()){
            return inscripcionesPruebaDAO.leer(id);
        }
        return null;
    }

    /**
     * Creamos una entidad para que el usuario pueda llenarla
     * @return nueva instancia de la entidad actual
     */
    @Override
    protected InscripcionesPrueba createNewEntity() {
        return new  InscripcionesPrueba();
    }

    /**
     * Utilizamos la entitidad actual para que el padre la pueda usar en el CRUD
     * @param entity entidad actual
     * @return ID de la entidad actual
     */
    @Override
    protected Object getEntityId(InscripcionesPrueba entity) {
        if(entity!=null){
            return entity.getId();
        }

        return null;
    }

    //EL nombre de la entidad utilizada en este momento
    @Override
    protected String getEntityName() {
        return "Inscripciones de Prueba";
    }
}
