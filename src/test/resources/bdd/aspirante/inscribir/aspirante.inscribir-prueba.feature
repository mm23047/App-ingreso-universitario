Feature: Inscribir a un aspirante en una prueba de admision y turno especifico

	Scenario: Aspirante se inscribe correctamente a una prueba de admision
		Given se tiene un servidor corriendo con la aplicacion desplegada
		And existe un aspirante con perfil creado y una carrera asociada
		And existe una prueba de admision disponible con un turno habilitado
		When solicito inscribir al aspirante en la prueba de admision y turno seleccionados
		Then se registra una nueva inscripcion a la prueba para ese aspirante
		And puedo consultar la inscripcion de ese aspirante a la prueba
		And la inscripcion muestra el turno de examen y la carrera elegida correctamente