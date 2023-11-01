Grupo 1: Trabajo Práctico Especial de Arquitecturas Web
Carrizo Noelia
Centeno Manuela
Moyano Santiago
Rodriguez Diego

# Consultas POST y funcionalidades detectadas

### a) Crear una parada

Ejemplo:

```http
POST localhost:8089
{
    "nombre": "Axion",
    "longitud": 800,
    "latitud": 900
}
```

### b) Crear un Monopatin

Ejemplo:

```http
POST localhost:8082/monopatin
{
    "latitud": 900,
    "longitud": 800,
    "encendido": false,
    "en_taller": false,
    "id_parada": 11,
    "km_ultimo_service": 100,
    "kmts": 70
}
```

Tambien tiene tiempo de uso con pausa y tiempo de uso sin pausa, que se va sumando y registrando con cada viaje terminado.

### c) Crear una cuenta

```http
POST http://192.168.1.115:8083/
{
    "mercado_pago": 3,
    "saldo": 40,
    "fecha_alta": null,
    "habilitada": true
}
```

### d) Crear un usuario

```http
POST localhost:8098/usuarios
{
    "nombre": "armando",
    "apellido": "montoya",
    "telefono": 298399999,
    "email": "usuario@admin.com",
    "rol": "a"
}
```

Siendo 'a' administrador.

### Viajes) Iniciar un viaje

Ejemplo:

```http
POST localhost:8081/iniciar
{
  "idMono": 1,
  "idCuenta": 1
}
```

Para iniciar un viaje unicamente se le pasa el id del monopatin que se esta usando, y el id de la cuenta que requirió el viaje.
Dentro del controller se asegura que la cuenta tenga saldo preguntandole al microservicio 'Cuenta' (mediante un servicio):

```java
@GetMapping
public Float dameSaldo(Long cuenta) {
    return rest.getForEntity(cuentaURL + "/tieneSaldo/" + cuenta, Float.class).getBody();
}
```

Al confirmar que tenga saldo, se completan los campos faltantes y se marca como encendido el monopatin (mediante otro servicio). De esta forma otro usuario ya no puede solicitar el uso de este monopatin hasta que termine el viaje:

```java
if (monoService.estaListoParaUsar(idMono)) {
    LocalDate fecha = LocalDate.now();
    LocalTime hora = LocalTime.now();
    Viaje viaje = new Viaje(idCuenta, fecha, null, hora, null, (long) 0, idMono, true);
    viajeRepo.save(viaje);
    // Devuele encendido con exito o si hubo un inconveniente
    String r = monoService.encender(idMono);
    return r;
}
```

### Viajes) Terminar un viaje

```http
PUT localhost:8081/terminar/1/30
```

1 siendo el ID del monopatin y 30 siendo los KMTS que recorrió.

En el controller de viaje, primero confirma si hay un monopatin con ese id en viaje, es decir busca un viaje que este_en_viaje == true y corresponda al ID del monopatin.

```java
{
    Viaje v = viajeRepo.dameViajeXMono(idMono);
    if (v == null) return "No existe tal viaje";
    v.setEstaEnViaje(false);
}
```

Luego realiza multiples calculos para guardar en el viaje los campos como fecha, hora de fin y la duracion del viaje con pausas y sin pausas.

```java
{
    v.setFecha_fin(fecha);
    v.setHora_fin(hora);
    v.setKms(kmts);

    Duration diferencia = Duration.between(v.getHora_inicio(), hora);
    LocalTime tiempo = LocalTime.of(horas, minutos, segundos);
    v.setTiempoSinPausa(tiempo);

    LocalTime horaReincorporacion = v.getHoraDeReincorporacion();

    Duration diferencia2 = Duration.between(horaReincorporacion, hora);

    LocalTime tiempo2 = LocalTime.of(horas2, minutos2, segundos2);
    v.setTiempoConPausa(v.getTiempoConPausa().plusHours(tiempo2.getHour()).plusMinutes(tiempo2.getMinute())
            .plusSeconds(tiempo2.getSecond()));
    v.setHoraInicioParcial(hora);
}
```

Ademas calcula el costo del viaje consultandole al microservicio 'Tarifa' cual es la tarifa normal y extra vigente. Posteriormente se descuenta del viaje.

```java
{
    LocalTime tiempoConPausas = v.getTiempoConPausa();
    LocalTime horaInfraccion = v.getHoraDeInfraccion();
    Double costoViaje = tarifaService.calcularCostoViaje(tiempoConPausas, horaInfraccion);
    v.setFacturado(costoViaje);

    // Facturar en la cuenta
    cuentaService.descontarCostoViaje(v.getId_cuenta(), costoViaje);
}
```

... Luego se estaciona el monopatin y se le pasa un DTO para que actualice sus campos:

```java
{
    TerminarViajeDTO finDelViaje = new TerminarViajeDTO(kmts, v.getTiempoConPausa(),tiempo);
    String x = monoService.apagar(idMono, finDelViaje);
    if (x.equals("se estaciono correctamente")) {
        this.viajeRepo.save(v);
        return "viaje finalizado con exito";
    }
    return "";
}
```

```java
{
    LocalTime tiempoConPausas = v.getTiempoConPausa();
    LocalTime horaInfraccion = v.getHoraDeInfraccion();
    Double costoViaje = tarifaService.calcularCostoViaje(tiempoConPausas, horaInfraccion);
    v.setFacturado(costoViaje);
}
```

### Pausas) Empezar una pausa

Las pausas son iniciadas desde el viaje, al cual le llega el ID del monopatin que se quiere pausar.

```http
GET localhost:8081/pausar/1
```

```java
@GetMapping("/pausar/{idMono}")
public void pausarMono(@PathVariable Long idMono) {
    // Obtiene el viaje actual que se esta realizando con ese monopatin
    Viaje viaje = viajeRepo.dameViajeXMono(idMono);
    Long id_viaje = viaje.getId_viaje();

    pausaService.empezarPausa(id_viaje); // Registra una pausa con hora_fin en null
    monoService.setStandBy(idMono); // Togglea y suspende el monopatin

    // Realiza calculos para setear la hora parcial de inicio, y el tiempo con pausa hasta ahora.
    viajeRepo.save(viaje);
}
```

### Pausas) Terminar una pausa y detectar infracción

```http
GET localhost:8081/terminarPausa/1
```

A pausa se le envia el ID del viaje que termino su pausa. Y se comprueba si la pausa excedio los 15 minutos.

```java
@GetMapping("/terminarPausa/{idMono}")
public String terminarPausaMono(@PathVariable Long idMono) {
    Viaje viaje = viajeRepo.dameViajeXMono(idMono);
    Long id_viaje = viaje.getId_viaje();

    // togglea y se vuelve a iniciar el monopatin'
    monoService.setStandBy(idMono);

    // Pausa devuelve la hora de infraccion en caso de que la pausa haya excedido los 15 minutos. Sino no retorna nada.
    LocalTime tiempoDeInfraccion = pausaService.terminarPausa(id_viaje);

    // Si hubo infraccion se setea en el viaje la hora de la infraccion.
    if (viaje.getHoraDeInfraccion() == null) {
        viaje.setHoraDeInfraccion(tiempoDeInfraccion);
        viajeRepo.save(viaje);
    }

    return "";
}
```

# Consultas del ejercicio 3

### a) Como encargado de mantenimiento quiero poder generar un reporte de uso de monopatines por kilómetros para establecer si un monopatín requiere de mantenimiento. Este reporte debe poder configurarse para incluir (o no) los tiempos de pausa.

```http
GET localhost:8099/mantenimientos/pedirReporteMonopatines
```

```java
@GetMapping("/pedirReporteMonopatines")
public List<ReporteMonopatinesDTO> pedirReporte() {
    List<ReporteMonopatinesDTO> reporte = monopatinService.getReporte();
    return reporte;
}
```

pedirReporte() se comunica con 'Monopatin-Microservicio' mediante un servicio donde le pide un reporte.

Monopatines se encarga de devolver un reporte en forma de DTO, ordenados por kmts.

### b. Como administrador quiero poder anular cuentas para inhabilitar el uso momentáneo de la misma.

```http
GET localhost:8098/usuarios/anularCuenta/2/1
```

```java
@GetMapping("/anularCuenta/{idUsuario}/{idCuenta}")
public String anularCuenta(@PathVariable Long idUsuario, @PathVariable Long idCuenta) {

    // Busca usuario y comprueba si es administrador
    if (esAdmin(idUsuario)) {
        String intentarAnularCuenta = cuentaServicio.anularCuenta(idCuenta);
        return intentarAnularCuenta;
    }

    return "El usuario no es administrador";
}
```

Primero se confirma si ese usuario es Admin, luego anularCuenta() se comunica con 'Cuenta-Microservicio' mediante un servicio donde le pide que anule esa cuenta.
Si existe la cuenta y hubo exito o no, devuelve el mensaje correspondiente a lo que paso.

### c. Como administrador quiero consultar los monopatines con más de X viajes en un cierto año.

```http
GET localhost:8098/usuarios/reporteMonopatines/4/2023
```

```java
@GetMapping("/reporteMonopatines/{cantViajes}/{anio}")
public List<ReporteMonopatinesPorViajeDTO> obtenerReportePorViaje(
    @PathVariable int cantViajes, @PathVariable int anio) {
    List<ReporteMonopatinesPorViajeDTO> reporte = viajeServicio.obtenerReportePorViaje(cantViajes, anio);
    return reporte;
}
```

obtenerReportePorViaje() se comunica con 'Viaje-Microservicio' mediante un servicio donde le pide un DTO listando los ids de los monopatines que superen la cantidad de viajes en ese año.

### d. Como administrador quiero consultar el total facturado en un rango de meses de cierto año.

```http
GET localhost:8098/usuarios/totalFacturadoEnRangoDeMeses/9/11/2023
```

```java
@GetMapping("/totalFacturadoEnRangoDeMeses/{mesInicio}/{mesFin}/{anio}")
public String obtenerTotalFacturadoEnRangoDeMeses(@PathVariable int mesInicio, @PathVariable int mesFin,
    @PathVariable int anio) {
    Double totalFacturado = viajeServicio.getTotalFacturadoEnRangoDeMeses(mesInicio, mesFin, anio);
    return "El total facturado fue:" + totalFacturado;
}
```

obtenerTotalFacturadoEntreRangoDeMeses() se comunica con 'Viaje-Microservicio' mediante un servicio donde le pide un Double, resultado del total facturado entre esos meses.

### e. Como administrador quiero consultar la cantidad de monopatines actualmente en operación, versus la cantidad de monopatines actualmente en mantenimiento.

```http
GET localhost:8098/usuarios/cantidadMonopatines
```

```java
@GetMapping("/cantidadMonopatines")
public Map<String, Integer> obtenerMonopatinesEnTaller() {
    Map<String, Integer> resultado = monoServicio.obtenerMonopatinesEnTaller();
    return resultado;
}
```

obtenerMonopatinesEnTaller() se comunica con 'Monopatin-Microservicio' mediante un servicio donde le pide un Map<String, Integer>, indicando "Operando" y la cantidad, seguido de "Mantenimiento" y la cantidad correspondiente.

### f. Como administrador quiero hacer un ajuste de precios, y que a partir de cierta fecha el sistemahabilite los nuevos precios.

```http
GET localhost:8098/usuarios/ajustarTarifa/1
{
    "tarifaNormal": 30.0,
    "tarifaExtra": 40.0,
    "fechaDesde": "2023-11-29T14:30:00"
}
```

```java
@GetMapping("/ajustarTarifa/{idUsuario}")
public String ajustarTarifa(@PathVariable Long idUsuario, @RequestBody Tarifa tarifa) {
    if (esAdmin(idUsuario)) {
        tarifaServicio.aplicarTarifa(tarifa);
        return "Tarifa aplicada";
    }
    return "El usuario no es admin";
}
```

ajustarTarifa() se comunica con 'Tarifa-Microservicio' mediante un servicio el cual le solicita aplicar una nueva tarifa, enviandole una tarifa.

### g. Como usuario quiero un listado de los monopatines cercanos a mi zona, para poder encontrar un monopatín cerca de mi ubicación

```http
GET localhost:8098/usuarios/paradasCercanas/500/500
```

```java
@GetMapping("/paradasCercanas/{latitud}/{longitud}")
public List<Parada> obtenerParadasCercanas(@PathVariable Double latitud, @PathVariable Double longitud) {
    List<Parada> paradas = paradasServicio.getParadas(latitud, longitud);
    return paradas;
}
```

obtenerParadasCercanas() se comunica con 'Paradas-Microservicio' mediante un servicio el cual le solicita un listado de Paradas enviandole la ubicacion actual del Usuario.
