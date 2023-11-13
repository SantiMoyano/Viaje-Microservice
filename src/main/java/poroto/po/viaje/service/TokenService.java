package poroto.po.viaje.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenService {

    @Value("${token}")
    private String token;

    private final RestTemplate rest;
    
    public TokenService(RestTemplate rest) {
        this.rest = rest;
    }
    
    public String autorizado(String authorizationHeader){
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String tokenSinBearer = authorizationHeader.substring(7);
            return rest.getForEntity(token+"/usuarios/verificarToken/"+tokenSinBearer, String.class).getBody();
        } else {
           // "No se proporcionó un token Bearer válido en el encabezado Authorization.";
           return null;
        }
   }
    
}
