Feature: Consulta ejecutiva integral del aspirante

  Scenario: Recuperar la informacion completa y consistente de un aspirante en proceso
    Given se tiene un servidor corriendo con la aplicacion desplegada para consultar datos de un aspirante
    And se crea el expediente de un aspirante con inscripcion, carrera y aula asignada
    When consulto la informacion ejecutiva del aspirante mediante HTTP
    Then el sistema responde exitosamente
    And la informacion personal devuelta coincide con los datos originales
    And los datos de su inscripcion y carrera elegida son consistentes
    And la asignacion de aula y pupitre corresponde a la inscripcion