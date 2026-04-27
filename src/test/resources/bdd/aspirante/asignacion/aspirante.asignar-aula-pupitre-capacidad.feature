Feature: Asignar aula y pupitre con capacidad limitada

Scenario: Aula llena rechaza la segunda asignacion
    Given se inicializa el servidor para asignacion de aula con capacidad limitada
    And existen dos aspirantes inscritos en la prueba
    And existe un aula de examen con capacidad limitada
    When asigno el aula al primer aspirante
    And intento asignar el mismo aula al segundo aspirante
    Then el sistema rechaza la asignacion por capacidad
