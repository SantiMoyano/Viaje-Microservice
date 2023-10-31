package poroto.po.viaje.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import poroto.po.viaje.entity.TerminarViajeDTO;


@RestController
public class MonopatinService {

    @Value("${monopatinURL}")
	private String monopatinURL;

    private final RestTemplate rest;

    @Autowired
    public MonopatinService(RestTemplate rest){
        this.rest=rest;
    }

  
    @PutMapping("/apagar")
    public String apagar(Long idMono, TerminarViajeDTO finViaje){
        ResponseEntity<TerminarViajeDTO> res=new ResponseEntity<>(finViaje,HttpStatus.OK);
        String r= rest.exchange(monopatinURL+"/apagar/"+idMono, HttpMethod.PUT, res, String.class).getBody();
        System.out.println(r);
        return r;
    }

    @PutMapping("/encender")
    public String encender(Long idMono){

        String r= rest.exchange(monopatinURL+"/encender/"+idMono, HttpMethod.PUT, null, String.class).getBody();
        System.out.println(r);
        return r;
    }


    public Boolean estaListoParaUsar(Long idMono) {
        Boolean l=rest.getForEntity(monopatinURL+"/estaListoParaUsar/"+idMono, Boolean.class).getBody();
        System.out.println("listo: "+ l);
        return l;
        
    }


    public void setStandBy(Long idMono) {
         rest.getForEntity(monopatinURL+"/ponerEnStandBy/"+idMono,String.class);
    }



    
}
