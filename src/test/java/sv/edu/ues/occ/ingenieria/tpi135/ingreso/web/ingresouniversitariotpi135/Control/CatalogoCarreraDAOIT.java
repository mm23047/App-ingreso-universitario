package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CatalogoCarreraDAOIT extends AbstractBaseIT {

    //Creamos el ID tipo String
    private static String idCatalogo;


    public CatalogoCarreraDAOIT() {
    }

    @Test
    @Order(1)
    public void testCount(){
        System.out.println("CatalogoCarreraDAOIT.count()");
        assertTrue(postgres.isRunning());

        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em=emf.createEntityManager();

        int resultado = cut.count();

        assertTrue(resultado>=0);

    }

    @Test
    @Order(2)
    public void testFindRange(){
        System.out.println("CatalogoCarreraDAOIT.findRange()");
        CatalogoCarreraDAO cut= new CatalogoCarreraDAO();

        cut.em=emf.createEntityManager();
        List<CatalogoCarrera> resultado = cut.findRange(0,5);
        assertNotNull(resultado);
    }

    @Test
    @Order(3)
    public void testCrear(){

        System.out.println("CatalogoCarreraDAOIT.create()");
        EntityManager em = emf.createEntityManager();
        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em=em;
        CatalogoCarrera nuevaCarrera = new CatalogoCarrera();

        //EL ID se debe de generar manualmente para poder identificar de mejor la carrera
        nuevaCarrera.setIdCarrera("INGSO-98");
        nuevaCarrera.setNombre("Ingenieria en Sistemas Informaticos") ;

        //Abrimos transaccion
        cut.em.getTransaction().begin();
        cut.crear(nuevaCarrera);
        cut.em.getTransaction().commit();

        //Guardamos el ID para las siguientes pruebsa
        idCatalogo = nuevaCarrera.getIdCarrera();

        assertNotNull(idCatalogo);

    }
    @Test
    @Order(4)
    public void testLeer(){
        System.out.println("CatalogoCarreraDAOIT.leer()");
        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em = emf.createEntityManager();

        CatalogoCarrera resultado = cut.leer(idCatalogo);

        assertNotNull(resultado, "El ID del catalogo no puede ser nulo porque ya debe de existir");
        assertEquals("INGSO-98",resultado.getIdCarrera() );
        assertEquals("Ingenieria en Sistemas Informaticos", resultado.getNombre() );

    }
    @Test
    @Order(5)
    public void testActualizar(){
        System.out.println("CatalogoCarreraDAOIT.actualizar()");
        EntityManager em = emf.createEntityManager();
        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em=em;

        //Leemos la carrera que existe actualmente
        CatalogoCarrera catalogoCarrera = cut.leer(idCatalogo);
        assertNotNull(catalogoCarrera);

        //Cambiamos el nombre
        catalogoCarrera.setNombre("Ingenieria en Sistemas Informaticos - 2025");

        //Guardamos cambios
        em.getTransaction().begin();
        CatalogoCarrera resultado=cut.actualizar(catalogoCarrera);
        em.getTransaction().commit();

        //Verificamos cambios
        assertEquals("Ingenieria en Sistemas Informaticos - 2025", resultado.getNombre());

    }

    @Test
    @Order(6)
    public void testEliminar(){
        System.out.println("CatalogoCarreraDAOIT.eliminar()");
        EntityManager em = emf.createEntityManager();
        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em=em;

        CatalogoCarrera carrera = cut.leer(idCatalogo);
        assertNotNull(carrera);

        em.getTransaction().begin();
        cut.eliminar(carrera);
        em.getTransaction().commit();

        //Verficar que ya no existe el registro

        assertNull(cut.leer(idCatalogo), "El registro no debe de existir y su retorno debe ser NULO");
        System.out.println(cut.leer(idCatalogo) + " No existe el registro");


    }

}
