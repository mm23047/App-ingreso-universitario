Feature: Resolver examen y validar puntaje final

	Scenario: Se registra una respuesta de examen y se valida el puntaje final
		Given se tiene un servidor corriendo con la aplicacion desplegada para resolver examen
		And existe un examen realizado disponible
		When registro una respuesta de examen para una pregunta existente
		Then la respuesta queda persistida y consultable por examen
		And la opcion seleccionada muestra detalle y si es correcta
		And el examen realizado muestra puntaje final consistente con la etapa
		And el filtro por examenId invalido es rechazado
