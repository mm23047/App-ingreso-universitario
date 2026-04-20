Feature: Consultar expediente completo del aspirante

	Scenario: Aspirante puede consultar su expediente consolidado con todos los datos
		Given se tiene un servidor corriendo para consultar el expediente del aspirante
		And existe un aspirante con perfil creado para expediente
		And existe una inscripcion a prueba para expediente
		And existe una carrera elegida para expediente
		When consulto el expediente completo del aspirante
		Then se retorna el expediente con status 200
		And el expediente contiene los datos del aspirante
		And el expediente contiene los datos de la inscripcion
		And el expediente contiene los datos de la carrera elegida
