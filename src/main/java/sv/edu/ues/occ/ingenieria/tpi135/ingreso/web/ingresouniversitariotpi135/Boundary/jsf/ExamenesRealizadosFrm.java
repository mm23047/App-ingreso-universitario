package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("examenesRealizadosFrm")
@ViewScoped
public class ExamenesRealizadosFrm extends DefaultFrm<ExamenesRealizado> implements Serializable {
    //DAO para acceder a la BD y hacer CRUD
    @Inject
    ExamenesRealizadoDAO examenesRealizadoDAO;

    //Injectamos los DAO de las tablas relacionadas
    @Inject
    ClavesExamanDAO clavesExamanDAO;
    @Inject
    AsignacionesAulaPupitreDAO asignacionesAulaPupitreDAO;
    @Inject
    EtapasAdmisionDAO etapasAdmisionDAO;

    // Creamos un listado de los datos de las tablas relacionadas
    private List<ClavesExaman> listClavesExaman;
    private List<EtapasAdmision> listEtapasAdmision ;
    private List<AsignacionesAulaPupitre> listAsignacionesAulaPupitre;

    @Override
    public void inicializarListas(){
        super.inicializarListas();
        if(clavesExamanDAO!=null) this.listClavesExaman= clavesExamanDAO.findRange(0,100);
        if(asignacionesAulaPupitreDAO!=null) this.listAsignacionesAulaPupitre = asignacionesAulaPupitreDAO.findRange(0,100);
        if(etapasAdmisionDAO!=null) this.listEtapasAdmision = etapasAdmisionDAO.findRange(0,100);

    }

    // FC para poder saber que esta haciendo el usuario
    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }


    /**
     *
     */
    public ExamenesRealizadosFrm(){
        this.nombreBean="examenesRealizadosFrm";
    }

    /**
     * DAO manejado por el padre para poder hacer CRUD
     * @return La entidad actual
     */
    @Override
    protected IngresoDAOInterface<ExamenesRealizado> getDao() {
        return examenesRealizadoDAO;
    }

    /**
     * Creamos una entidad para que el usuario pueda hacer nuevo registro
     * @return entidad a utilizar
     */
    @Override
    protected ExamenesRealizado nuevoRegistro() {
        return createNewEntity();
    }

    /**
     * BUscamos la entidad en la BD para poder hacer CRUD
     * @param id id del registro de la entidad actual
     * @return entidad encontra actual
     */
    @Override
    protected ExamenesRealizado buscarRegistroPorId(Object id) {
        if(id != null && examenesRealizadoDAO!=null){
            return examenesRealizadoDAO.leer(id);
        }
        return null;
    }

    /**
     * COnvertimos el registro de la BD a texto para que sea utilizado el fronENde
     * @param r entidad actual
     * @return ID en string de ExamenesRealizados
     */
    @Override
    protected String getIdAsText(ExamenesRealizado r) {
        if(r!=null && r.getId() != null){
            return r.getId().toString();
        }

        return null;
    }

    /**
     * Convertimos el trxto a objeto para ser utilizado por java
     * @param id del registro que recivimos desde la vista
     * @return el obhjeto para ser utilizado en un CRUD
     */
    @Override
    protected ExamenesRealizado getIdByText(String id) {
        if(id!=null && !id.isEmpty()){
            try {
                UUID idCovertidoDeString = UUID.fromString(id);
                return examenesRealizadoDAO.leer(idCovertidoDeString);
            }catch (IllegalArgumentException e){
                Logger.getLogger(ExamenesRealizadosFrm.class.getName()).log(Level.SEVERE, "Error al convertir String a UUID", e);
                return null;
            }

        }
        return null;
    }

    /**
     * Creamos un registro vacio para que el usuario lo pueda llenar
     * @return nueva entidad actual
     */
    @Override
    protected ExamenesRealizado createNewEntity() {
        ExamenesRealizado nuevoDatoExamenesRealizados = new ExamenesRealizado();

        //Inicializamos las claves foraneas para que JSF las inyecte
        nuevoDatoExamenesRealizados.setIdAsignacion(new AsignacionesAulaPupitre());
        nuevoDatoExamenesRealizados.setIdClave(new ClavesExaman());
        nuevoDatoExamenesRealizados.setIdEtapa(new EtapasAdmision());

        nuevoDatoExamenesRealizados.setFechaRealizacion(OffsetDateTime.now());

        return nuevoDatoExamenesRealizados;
    }

    /**
     * Entidad actual para que el padre le pueda hacer CRUD
     * @param entity ENtidad actual ExamenesRealizados
     * @return el ID de la entidad actual
     */
    @Override
    protected Object getEntityId(ExamenesRealizado entity) {
        if(entity!=null){
            return entity.getId();
        }
        return null;
    }

    // EL nombre de la entidad que estamos utilizando actualemte
    @Override
    protected String getEntityName() {
        return this.nombreBean;
    }

    //GETTERS para poder leer las listas de los datos de las otras tablas
    public List<AsignacionesAulaPupitre> getListAsignacionesAula() {
        return listAsignacionesAulaPupitre;
    }
    public List<EtapasAdmision> getListEtapasAdmision() {
        return listEtapasAdmision;
    }
    public List<ClavesExaman> getListClavesExaman() {
        return listClavesExaman;
    }
}
