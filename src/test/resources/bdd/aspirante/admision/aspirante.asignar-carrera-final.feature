Feature: Asignar carrera final al aspirante según cupos disponibles y etapa de admisión

	Scenario: Aspirante obtiene carrera asignada dentro de cupo disponible en etapa de admisión
		Given se tiene un servidor corriendo para ejecutar la asignacion de carrera final
		And existe un aspirante con perfil creado
		And existe una inscripcion a prueba con carrera elegida asociada
		And existe una etapa de admision vigente con cupos disponibles para una carrera
		When asigno la carrera final al aspirante verificando cupos y etapa
		Then se crea el proceso admision aspirante con carrera asignada
		And puedo consultar el proceso de admision del aspirante
		And la carrera asignada coincide con la carrera elegida dentro del cupo disponible
