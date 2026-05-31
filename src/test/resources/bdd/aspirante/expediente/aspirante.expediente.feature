Feature: Consultar expediente de aspirante

Scenario: Consultar expediente completo del aspirante

Given se tiene un servidor corriendo con la aplicacion desplegada para expedientes
And se tiene un aspirante con examenes realizados
When consulto el expediente del aspirante
Then se visualizan los datos personales
And se visualizan las inscripciones en pruebas
And se visualizan los examenes realizados y sus respuestas
