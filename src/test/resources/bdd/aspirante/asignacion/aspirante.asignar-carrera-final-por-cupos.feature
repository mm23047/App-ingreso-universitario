Feature: Asignar carrera final por cupos y prioridad

  # ESCENARIO 1: Camino feliz
  Scenario: Asignación exitosa de carrera final
    Given se inicializa el servidor para asignación de carrera
    And existe una inscripción con proceso en etapa de asignación
    And existen carreras elegidas con prioridades y cupos configurados
    When ejecuto la asignación final de carrera para la inscripción
    Then se obtiene estado ADMITIDO con carrera asignada por cupos y prioridad
    And el cupo de la carrera asignada se decrementa

  # ESCENARIO 2: Camino triste (Sin cupos)
  Scenario: Rechazo de aspirante por falta de cupos
    Given se inicializa el servidor para asignación de carrera
    And existe una inscripción con proceso en etapa de asignación
    And existen carreras elegidas con prioridades pero sin cupos disponibles
    When ejecuto la asignación final de carrera para la inscripción
    Then se obtiene estado NO_ADMITIDO y el aspirante queda sin carrera asignada