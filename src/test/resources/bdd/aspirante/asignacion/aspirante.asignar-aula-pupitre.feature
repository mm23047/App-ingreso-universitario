Feature: Asignar aula y pupitre a un aspirante inscrito

Scenario: Asignación exitosa de aula y pupitre a un aspirante
	Given se tiene un servidor corriendo con la aplicación desplegada
	And existe un aspirante inscrito en una prueba de admisión y turno específico
	And existe un aula de examen con pupitres disponibles
	When asigno un aula y un pupitre al aspirante para esa prueba
	Then se registra la asignación de aula y pupitre para el aspirante
	And puedo consultar la asignación de aula y pupitre del aspirante
	And la asignación muestra el aula y el pupitre asignados correctamente
