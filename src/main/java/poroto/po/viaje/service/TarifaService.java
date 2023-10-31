package poroto.po.viaje.service;

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
        // Busca cuanto vale la tarifa normal
        Double costoTarifaNormal = rest.exchange(tarifaURL+"/tarifa-normal", HttpMethod.GET, null, Double.class).getBody();
        Double costoViaje = null;
        // Multiplica los minutos por la tarifa normal si no hubo pausas mayores a 15 mins.
        if (horaInfraccion == null) {
            costoViaje = costoTarifaNormal * tiempoConPausas.getMinute();
        }
        // Multiplica los minutos por la tarifa normal si no hubo pausas mayores a 15 mins.
        return costoViaje;
    }
}
