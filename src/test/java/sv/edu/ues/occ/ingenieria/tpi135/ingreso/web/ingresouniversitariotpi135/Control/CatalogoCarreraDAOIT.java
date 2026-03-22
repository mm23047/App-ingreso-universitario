package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogoCarreraDAOIT extends AbstractBaseIT {



    public CatalogoCarreraDAOIT() {
    }

    @Test
    public void testCount(){
        System.out.println("CatalogoCarreraDAOIT.count()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em ->{

            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em=em;

            int resultado = cut.count();

            assertTrue(resultado>=0);
            assertEquals(resultado,4);
            return null;
        });


    }

    @Test
    public void testFindRange(){
        System.out.println("CatalogoCarreraDAOIT.findRange()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            CatalogoCarreraDAO cut= new CatalogoCarreraDAO();
            cut.em=em;

            List<CatalogoCarrera> resultado = cut.findRange(0,5);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(resultado.size(),4);
            System.out.println("Mi resultado del test FindRange: "+resultado);

            return null;
        });


    }

    @Test
    public void testCrear(){

        System.out.println("CatalogoCarreraDAOIT.create()");

        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em ->{

            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em=em;

            CatalogoCarrera nuevaCarrera = new CatalogoCarrera();

            //EL ID se debe de generar manualmente para poder identificar de mejor manera la carrera
            nuevaCarrera.setIdCarrera("INGSO-98");
            nuevaCarrera.setNombre("Ingenieria en Sistemas Informaticos") ;

            //Creamos sin abrir o cerrar transacciones
            cut.crear(nuevaCarrera);

            //Verificar que hay un dato mas
            assertEquals(5, cut.count());
            System.out.println("Mi resultado del test Crear: "+cut.count());

            return null;
        } );

        //Verificar el rollback debe de quedar limpia la BD
        ejecutarEnTransaccion(em->{

            CatalogoCarreraDAO cut= new CatalogoCarreraDAO();
            cut.em=em;
            //Deben de existir solamente 4 datos en la BD
            assertEquals(cut.count(),4);

            return null;
        });



    }
    @Test
    public void testLeer(){
        System.out.println("CatalogoCarreraDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em = em;

            //TOmar una carrera que ya existe en la D
            CatalogoCarrera CarrerExistente = cut.findRange(0,1).get(0);

            //Probar el metodo leer con la carrera que existe
            CatalogoCarrera resultado = cut.leer(CarrerExistente.getIdCarrera());

            assertNotNull(resultado, "El ID del catalogo no puede ser nulo porque ya debe de existir");
            assertEquals(CarrerExistente.getIdCarrera(),resultado.getIdCarrera() );
            assertEquals(CarrerExistente.getNombre(), resultado.getNombre() );

            return null;
        });



    }
    @Test
    public void testActualizar(){
        System.out.println("CatalogoCarreraDAOIT.actualizar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em ->{

            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em=em;

            //Leemos la carrera que existe en la BD
            CatalogoCarrera catalogoCarrera = cut.findRange(0,2).get(0);
            assertNotNull(catalogoCarrera);

            //Cambiamos el nombre
            catalogoCarrera.setNombre("Ingenieria NOMBRE Modificada - 2026");

            //Guardamos cambios
            CatalogoCarrera resultado=cut.actualizar(catalogoCarrera);

            //Verificamos cambios
            assertEquals("Ingenieria NOMBRE Modificada - 2026", resultado.getNombre());

            return null;
        });
        

    }

    @Test
    public void testEliminar(){
        System.out.println("CatalogoCarreraDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em ->{

            CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
            cut.em=em;

            //Creamos un dato TEMPORAL
            CatalogoCarrera carreraTemporal = new  CatalogoCarrera();
            carreraTemporal.setIdCarrera("INGSO-98");
            carreraTemporal.setNombre("Ingenieria en Sistemas Informaticos");
            cut.crear(carreraTemporal);

            //Deben de existir 5 carreras
            assertEquals(5, cut.count());

            //Eliminamos la carrera temporal
            cut.eliminar(carreraTemporal);

            //Verficar que ya no existe el registro
            assertEquals(4, cut.count());
            System.out.println(cut.leer("INGSO-98") + " No existe el registro");

            return null;
        });


    }

}
