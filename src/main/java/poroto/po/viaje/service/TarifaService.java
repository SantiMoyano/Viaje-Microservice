package poroto.po.viaje.service;

import java.time.Duration;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Service
public class TarifaService {

    @Value("${tarifaURL}")
	private String tarifaURL;

    private final RestTemplate rest;

    @Autowired
    public TarifaService(RestTemplate rest){
        this.rest=rest;
    }

    @GetMapping
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

    private long calcularMinutosConInfraccion(LocalTime horaInfraccion) {
        Duration diferencia = Duration.between(horaInfraccion, LocalTime.now());
        return diferencia.getSeconds() / 60;
    }
}
