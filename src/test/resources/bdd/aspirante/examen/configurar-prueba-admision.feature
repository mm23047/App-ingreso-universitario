Feature: Configurar y armar una prueba de admision con preguntas y respuestas

  Scenario: Armado exitoso de un examen con sus respectivas respuestas correctas y trampas
    Given el servidor esta inicializado para la administracion academica
    And existe un area de conocimiento llamada "Matematicas"
    When el administrador registra la pregunta "Si x=2, cual es el valor de x^2?" para el area de "Matematicas"
    And asigna "4" como la respuesta correcta
    And asigna "2", "6" y "8" como las respuestas trampa
    And crea una nueva configuracion de examen llamada "Clave A" para la prueba actual
    And asocia esta pregunta a la "Clave A"
    Then el sistema guarda la pregunta y sus opciones en el banco de preguntas exitosamente
    And al consultar la "Clave A" mediante HTTP, el sistema devuelve la estructura del examen lista para los aspirantes