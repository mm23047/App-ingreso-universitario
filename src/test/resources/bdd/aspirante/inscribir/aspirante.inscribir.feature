Feature: Inscribir un aspirante en una prueba de admision

Scenario: Inscribir un aspirante en prueba de admision

Given se tiene un servidor corriendo con la aplicacion desplegada para inscribir aspirantes
And se tiene un aspirante creado
When el aspirante se inscribe en una prueba de admision 2026
Then la inscripcion se registra exitosamente
And el estado de la inscripcion es INSCRITO
