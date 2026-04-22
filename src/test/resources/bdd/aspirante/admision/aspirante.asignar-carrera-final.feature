Feature: Asignar carrera final automaticamente por prioridad y cupos

	Scenario: El sistema asigna carrera respetando prioridad y cupos
		Given se inicializa el servidor para asignacion automatica de carrera
		And se crea un aspirante unico
		And se crea una inscripcion a prueba para el aspirante
		And se asocian carreras elegidas con prioridad
		And se define una etapa y cupos por carrera
		And se crea el proceso de admision en estado EN_PROCESO sin carrera asignada
		When ejecuto el endpoint de asignacion de carrera final
		Then el sistema cambia el estado a ADMITIDO y asigna la carrera disponible por prioridad
		And el sistema decrementa el cupo de la carrera asignada
