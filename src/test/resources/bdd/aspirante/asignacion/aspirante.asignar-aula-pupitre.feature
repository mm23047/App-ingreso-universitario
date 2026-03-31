# language: es
Característica: Asignar aula y pupitre a un aspirante inscrito

	Escenario: Asignación exitosa de aula y pupitre a un aspirante
		Dado se tiene un servidor corriendo con la aplicación desplegada
		Y existe un aspirante inscrito en una prueba de admisión y turno específico
		Y existe un aula de examen con pupitres disponibles
		Cuando asigno un aula y un pupitre al aspirante para esa prueba
		Entonces se registra la asignación de aula y pupitre para el aspirante
		Y puedo consultar la asignación de aula y pupitre del aspirante
		Y la asignación muestra el aula y el pupitre asignados correctamente
