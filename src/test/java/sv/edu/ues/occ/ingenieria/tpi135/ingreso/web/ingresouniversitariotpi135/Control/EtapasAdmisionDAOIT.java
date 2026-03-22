package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EtapasAdmisionDAOIT extends AbstractBaseIT {


    public EtapasAdmisionDAOIT() {
    }

    @Test
    public void testCount() {
        System.out.println("TEST DAOIT COUNT");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em =em;

            int Resultado = cut.count();

            //Tenemos 3 registros en la base de datos
            assertEquals(3, Resultado);

            return null;
        });

    }

    @Test
    public void testFindRange() {
        System.out.println("TEST EtapasAdmision DAOIT FIND RANGE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            List<EtapasAdmision> resultado = cut.findRange(0, 2);
            System.out.println("RESULTADO: " + resultado);
            assertNotNull(resultado);

            return null;
        });


    }


    @Test
    public void testCrear() {
        System.out.println("TEST EtapasAdmision DAOIT CREAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            EtapasAdmision nuevo = new EtapasAdmision();
            nuevo.setNombre("Primer VUELTA");
            nuevo.setPuntajeMaximo(new BigDecimal(100));
            nuevo.setPuntajeMinimo(new BigDecimal(50));
            nuevo.setDescripcion("Primer etapa del examen de ADMISION");

            cut.crear(nuevo);

            assertNotNull(nuevo.getId());
            //Para verificar en la consola
            System.out.println(nuevo.getId());

            return null;
        });

        //Verificamos de no ensuciar la BD
        ejecutarEnTransaccion(em->{
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            //Verificamos la cantidad tal cual el metodo de count
            assertEquals(cut.count(),3);

            return null;
        });


    }

    @Test
    public void testLeer() {
        System.out.println("TEST EtapasAdmision DAOIT READ");

        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            //Utilizamos el primer registro para hacer el test
            EtapasAdmision resultadoDesdeLaBD = cut.findRange(0,3).get(0);

            EtapasAdmision resultado = cut.leer(resultadoDesdeLaBD.getId());

            System.out.println("RESULTADO: " + resultado);
            assertNotNull(resultado, "El ID del catalogo no puede ser nulo porque ya debe de existir");
            assertEquals(resultadoDesdeLaBD.getId(),resultado.getId());

            return null;
        });

    }

    @Test
    public void testActualizar() {
        System.out.println("TEST EtapasAdmision DAOIT UPDATE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em ->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            //Leemos un dato de la bd
            EtapasAdmision etapaActualizada = cut.findRange(0,3).get(0);
            assertNotNull(etapaActualizada, "No se encontró la etapa de admisión con el ID proporcionado");

            //Actualizamos la descripción de la etapa
            etapaActualizada.setDescripcion("Descripción actualizada para la etapa 1");

            EtapasAdmision resultadp = cut.actualizar(etapaActualizada);

            assertNotNull(resultadp, "No se encontró la etapa de admisión después de la actualización");
            assertEquals("Descripción actualizada para la etapa 1", resultadp.getDescripcion());
            System.out.println("Etapa actualizada: " + resultadp);

            return null;
        });



    }

    @Test
    public void testEliminar() {
        System.out.println("TEST EtapasAdmision DAOIT DELETE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            //Creamos un dato TEMPORAL

            EtapasAdmision etapa = new  EtapasAdmision();

            etapa.setNombre("Primer VUELTA");
            etapa.setPuntajeMaximo(new BigDecimal(100));
            etapa.setPuntajeMinimo(new BigDecimal(50));
            etapa.setDescripcion("Primer etapa del examen de ADMISION");
            //GUARDAR la temporal
            cut.crear(etapa);

            //VERIFICAR un dato NUEVO en la D
            assertEquals(cut.count(),4);


            cut.eliminar(etapa);

            assertEquals(cut.count(),3);
            assertNull(cut.leer(etapa.getId()), "EL registro debbe de ser null");
            System.out.println("Etapa eliminada: " + cut.leer(etapa.getId()));

            return null;
        });

    }
}
