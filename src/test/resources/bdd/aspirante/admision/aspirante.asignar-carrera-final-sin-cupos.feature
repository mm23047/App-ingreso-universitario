Feature: Asignar carrera final sin cupos

Scenario: Sin cupos disponibles
    Given se inicializa el servidor para asignacion de carrera sin cupos
    And se crea un aspirante para asignacion sin cupos
    And se crea una inscripcion a prueba para asignacion sin cupos
    And se asocian carreras elegidas sin cupos
    And se define una etapa y cupos en cero
    And se crea el proceso de admision sin cupos
    When ejecuto el endpoint de asignacion de carrera final sin cupos
    Then el sistema cambia el estado a NO_ADMITIDO y no asigna carrera
    And los cupos se mantienen en cero
