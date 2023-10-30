package poroto.po.viaje.service;


import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.client.RestTemplate;


@Service
public class PausaService {

     @Value("${pausaURL}")
	private String pausaURL;

    private final RestTemplate rest;

    @Autowired
    public PausaService(RestTemplate rest){
        this.rest=rest;
    }

    @PostMapping
    public void empezarPausa(Long id_viaje) {
        String x = rest.exchange(pausaURL+"/"+id_viaje, HttpMethod.POST, null, String.class).getBody();
    }

    @PutMapping
    public LocalTime terminarPausa(Long id_viaje) {
        return  rest.exchange(pausaURL+"/"+id_viaje, HttpMethod.PUT, null, LocalTime.class).getBody();
    }
    

   

    
}
