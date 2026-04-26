Feature: Resolver examen y validar puntaje final

	Scenario: Se califica un examen y se persiste el resultado final
		Given se tiene un servidor corriendo con la aplicacion desplegada para resolver examen
		And existe un examen realizado con respuestas registradas
		When solicito la calificacion del examen
		Then el examen queda con puntaje final recalculado
		And la inscripcion asociada al examen queda en estado CALIFICADO
		And el examen calificado es consultable por su id
