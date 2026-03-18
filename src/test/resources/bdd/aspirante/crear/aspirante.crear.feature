Feature: Crear un servicio para que los clientes puedan crear un perfil de aspirante y asociarlo a una opcion de carrera o curso

Scenario: Crear un perfil de aspirante y asociación a una carrera o curso

Given se tiene un servidor corriendo con la aplicacion desplegada
When puedo crear un aspirante
And puedo asociarle a una opcion de carrera, por ejemplo I30515
Then puedo consular el perfil del aspirante recien creado
And verificar la opcion de carrera a la que fue asociado