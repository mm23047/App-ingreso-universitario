Feature: Realizar un examen de admision

Scenario: Realizar un examen y registrar respuestas

Given se tiene un servidor corriendo con la aplicacion desplegada para examenes
And se tiene una inscripcion de aspirante en una etapa
When el aspirante realiza el examen de la primera etapa
And proporciona respuestas para todas las preguntas
Then el examen se registra exitosamente
And se pueden consultar las respuestas registradas
