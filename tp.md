# Monopatines
* Para poder utilizar el monopatín el usuario deberá crearse una cuenta en la app, 
* asociada a una cuenta de Mercado Pago. 
* Previamente al uso del servicio, debe haber cargado en su cuenta un monto de dinero,
* se irá descontando en función del tiempo de uso del monopatín. 
* Se puede utilizar la misma cuenta de Mercado Pago para varias cuentas del servicio.
* Una cuenta podrá tener asociados varios usuarios que utilizarán los créditos cargados en la cuenta, 
* un usuario puede asociarse a más de una cuenta. 
* Cada usuario tendrá un nombre y debe registrar su número de celular, email válido, nombre y apellido.
* La cuenta tendrá un número identificatorio y una fecha de alta.
* se generará un viaje asociado a la cuenta del usuario
* registrando fecha y hora de inicio. 
* El uso del monopatín es por tiempo, comienza a consumirse el crédito cuando se activa el monopatín, y esto permitirá que se encienda en ese momento. 
* se va a registrar la fecha y hora de finalización y 
* los kilómetros recorridos. 
* Cabe aclarar que la app no debe permitir finalizar un viaje sino detecta mediante el GPS con el que cuenta el monopatín, que se encuentra en una parada permitida.
* el usuario lo puede indicar por la app, para poder encender
* Si pasaran los 15 min automáticamente se volverá a considerar en uso el monopatín, y se
comienza a cobrar un monto mayor de crédito hasta el final del viaje.
* monopatin: ID,
* se puede determinar dónde está cada monopatín. 
* se considera el tiempo de uso y los kilómetros para mantenimiento
* precios de tarifa normal y extras por reinicio de pausas extensas. 
* anular cuentas cuando por algún motivo que se considere necesario.
----

• Registrar monopatín en mantenimiento (debe marcarse como no disponible para su uso)
• Registrar fin de mantenimiento de monopatín
• Ubicar monopatín en parada (opcional)
• Agregar monopatín
• Quitar monopatín
• Registrar parada
• Quitar parada
• Definir precio
• Definir tarifa extra para reinicio por pausa extensa
• Anular cuenta
• Generar reporte de uso de monopatines por kilómetros
• Generar reporte de uso de monopatines por tiempo con pausas
• Generar reporte de uso de monopatines por tiempo sin pausas
El trabajo implica construir un backend de servicios REST para el problema con una arquitectura de
microservicios.
1ra ENTREGA
1. Realizar un modelamiento de los distintos datos que debe guardar el sistema. Este modelamiento
debe ser capturado en términos de (sub-dominios), apuntando a un diseño/implementación con
microservicios. Una vez validado el modelo, generas las entidades y relaciones correspondientes, y
2mapearlo a una base de datos SQL. En ciertos casos (es decir, para ciertos microservicios), puede
decidirse utilizar otro tipo de base de datos (por ej., MongoDB)
2. Diseñar un backend básico de (micro-)servicios que permita realizar el ABM de las entidades (para
así poblar y gestionar la(s) base(s) de datos) y dar soporte a las principales funcionalidades antes
mencionadas. En este diseño, considerar que cada microservicio contará (preferentemente) con una
base de datos separada.
3. Implementar los siguientes servicios/reportes:
a. Como encargado de mantenimiento quiero poder generar un reporte de uso de monopatines por
kilómetros para establecer si un monopatín requiere de mantenimiento. Este reporte debe poder
configurarse para incluir (o no) los tiempos de pausa.
b. Como administrador quiero poder anular cuentas para inhabilitar el uso momentáneo de la
misma.
c. Como administrador quiero consultar los monopatines con más de X viajes en un cierto año.
d. Como administrador quiero consultar el total facturado en un rango de meses de cierto año.
e. Como administrador quiero consultar la cantidad de monopatines actualmente en operación,
versus la cantidad de monopatines actualmente en mantenimiento.
f. Como administrador quiero hacer un ajuste de precios, y que a partir de cierta fecha el sistema
habilite los nuevos precios.
g. Como usuario quiero lun listado de los monopatines cercanos a mi zona, para poder encontrar
un monopatín cerca de mi ubicación
2da ENTREGA
4. En caso que no lo haya hecho en la entrega anterior, refactorizar el diseño (monolítico) para
adecuarlo a un diseño de microservicios. Esto implica
- que se desplieguen en forma independiente (por ej., en puertos distintos),
- que sus comunicaciones entre sí se realicen mediante servicios REST (para esto, puede
considerarse el uso de RestTemplate o WebClient)
- que se utilicen bases de datos separadas.
5. Segurizar los endpoints REST con JWT.
6. Incorporar tests de unidad e integración (Junit o Mockito). Documentar los endpoints REST con
Swagger (OpenAPI).
7. Utilizar una base NoSQL (MongoDB), o bien implementar una comunicación vía protocolo gRPC entre
2 microservicios.
8. (Opcional) Desplegar la aplicación mediante contenedores (Docker) en una nube.