Feature: Asignar aula a aspirante para examen

Scenario: Asignar aula disponible para examen del aspirante

Given se tiene un servidor corriendo con la aplicacion desplegada para asignacion de aulas
And se tiene un aspirante inscrito en una prueba con turno asignado
When se asigna una aula disponible al aspirante para el turno
Then la asignacion se registra exitosamente
And se puede consultar el aula asignada al aspirante
And el cupo disponible en el aula disminuye en uno
