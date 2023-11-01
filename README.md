Grupo X: Repositorio del Trabajo Práctico Especial de Arquitecturas Web

# App

A continuacion vamos a enumerar las funcionalidades detectadas y como se implementaron.

# Consultas POST (Como agregamos elementos a las base de datos de cada microservicio)

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

Para iniciar un viaje unicamente se le pasa el id del monopatin que se esta usando, y el id de la cuenta que requirio el viaje.
Dentro del controller se asegura que la cuenta tenga saldo preguntandole al microservicio 'Cuenta' (mediante un servicio):

```
@GetMapping
public Float dameSaldo(Long cuenta) {
    return rest.getForEntity(cuentaURL + "/tieneSaldo/" + cuenta, Float.class).getBody();
}
```

Al confirmar que tenga saldo, se completan los campos faltantes y se marca como encendido el monopatin (mediante otro servicio). De esta forma otro usuario ya no puede solicitar el uso de este monopatin hasta que termine el viaje:

```
if (monoService.estaListoParaUsar(idMono)) {
    LocalDate fecha = LocalDate.now();
    LocalTime hora = LocalTime.now();
    Viaje viaje = new Viaje(idCuenta, fecha, null, hora, null, (long) 0, idMono, true);
    viajeRepo.save(viaje);
    String r = monoService.encender(idMono);
    return r;
}
```

### Viajes) Terminar un viaje

```http
PUT localhost:8081/terminar/1/30
```

1 siendo el ID del monopatin y 30 siendo los KMTS que recorrió.

Primero confirma si hay un monopatin con ese id en viaje, es decir busca un viaje que este_en_viaje == true y corresponda al ID del monopatin.

```
{
    Viaje v = viajeRepo.dameViajeXMono(idMono);
    if (v == null) return "No existe tal viaje";
    v.setEstaEnViaje(false);
}
```

Luego realiza multiples calculos para guardar en el viaje los campos como fecha, hora de fin y la duracion del viaje con pausas y sin pausas.

```
{
    LocalDate fecha = LocalDate.now();
    LocalTime hora = LocalTime.now();
    v.setFecha_fin(fecha);
    v.setHora_fin(hora);
    v.setKms(kmts);

    Duration diferencia = Duration.between(v.getHora_inicio(), hora);

    int horas = (int) diferencia.toHours();
    int minutos = (int) diferencia.toMinutes() % 60;
    int segundos = (int) diferencia.getSeconds() % 60;

    LocalTime tiempo = LocalTime.of(horas, minutos, segundos);
    v.setTiempoSinPausa(tiempo);

    LocalTime horaReincorporacion = v.getHoraDeReincorporacion();

    Duration diferencia2 = Duration.between(horaReincorporacion, hora);
    int horas2 = (int) diferencia2.toHours();
    int minutos2 = (int) diferencia2.toMinutes() % 60;
    int segundos2 = (int) diferencia2.getSeconds() % 60;

    LocalTime tiempo2 = LocalTime.of(horas2, minutos2, segundos2);
    v.setTiempoConPausa(v.getTiempoConPausa().plusHours(tiempo2.getHour()).plusMinutes(tiempo2.getMinute())
            .plusSeconds(tiempo2.getSecond()));
    v.setHoraInicioParcial(hora);
}
```

Ademas calcula el costo del viaje consultandole al microservicio 'Tarifa' cual es la tarifa normal y extra vigente.

```
{
    LocalTime tiempoConPausas = v.getTiempoConPausa();
    LocalTime horaInfraccion = v.getHoraDeInfraccion();
    Double costoViaje = tarifaService.calcularCostoViaje(tiempoConPausas, horaInfraccion);
    v.setFacturado(costoViaje);
}
```

... Luego en el servicio de tarifa:

```
public Double calcularCostoViaje(LocalTime tiempoConPausas, LocalTime horaInfraccion) {
    // Busca cuanto vale la tarifa normal y la extra
    Double costoTarifaNormal = rest.exchange(tarifaURL+"/tarifa-normal", HttpMethod.GET, null, Double.class).getBody();
    Double costoTarifaExtra = rest.exchange(tarifaURL+"/tarifa-extra", HttpMethod.GET, null, Double.class).getBody();
    Double costoViaje = null;
    int minutosTotales = tiempoConPausas.getMinute();

    // Multiplica los minutos por la tarifa normal si no hubo pausas mayores a 15 mins.
    if (horaInfraccion == null) {
        costoViaje = costoTarifaNormal * minutosTotales;

    // Aplica tarifa normal a los minutos no penalizados y tarifa extra a los minutos penalizados.
    } else {
        Double minutosPenalizados = (double) calcularMinutosConInfraccion(horaInfraccion);
        Double minutosNoPenalizados = minutosTotales - minutosPenalizados;
        costoViaje = minutosNoPenalizados * costoTarifaNormal;
        costoViaje += minutosPenalizados * costoTarifaExtra;
    }
    return costoViaje;
}
```
