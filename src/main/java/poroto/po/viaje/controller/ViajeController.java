package poroto.po.viaje.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
// import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import poroto.po.viaje.entity.TerminarViajeDTO;
import poroto.po.viaje.entity.Viaje;
import poroto.po.viaje.repsitory.ViajeRepo;
import poroto.po.viaje.service.CuentaService;
import poroto.po.viaje.service.MonopatinService;
import poroto.po.viaje.service.PausaService;
import poroto.po.viaje.service.TarifaService;

@RestController
// @RequestMapping("/viaje")
public class ViajeController {
    @Autowired
    private ViajeRepo viajeRepo;

    @Autowired
    private MonopatinService monoService;

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private PausaService pausaService;

    @Autowired
    private TarifaService tarifaService;

    @GetMapping("/dameViajes")
    public List<Viaje> dameViajes() {
        return viajeRepo.findAll();
    }

    @GetMapping({ "/iniciar/{idMono}/{idCuenta}" })
    public String inicio() {
        return "anduvo";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/iniciar")
    public String iniciarViaje(@RequestBody String v) throws JsonMappingException, JsonProcessingException {

        ObjectMapper jsonMap = new ObjectMapper();
        Map<String, Integer> data = jsonMap.readValue(v, Map.class);
        Long idMono = (long) data.get("idMono");
        Long idCuenta = (long) data.get("idCuenta");

        Float saldo = cuentaService.dameSaldo(idCuenta);
        // Float saldo = (float) 4;

        if (saldo > (float) 0) {
            if (monoService.estaListoParaUsar(idMono)) {

                LocalDate fecha = LocalDate.now();
                // DateTimeFormatter fecha = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                LocalTime hora = LocalTime.now();
                // DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm:ss");
                // Viaje viaje = new Viaje(idCuenta, fecha, "", hora, "", (double) 0, idMono,
                // true);

                Viaje viaje = new Viaje(idCuenta, fecha, null, hora, null, (long) 0, idMono, true);
                this.viajeRepo.save(viaje);
                String r = monoService.encender(idMono);
                return r;
            } else
                return "Monopatin no disponible";
        } else if (saldo == (float) -1)
            return "Cuenta no esta habilitada";
        else {
            return "Saldo insuficiente";
        }
    }

    @PutMapping("/terminar/{idMono}/{kmts}")
    public String terminarViaje(@PathVariable Long idMono, @PathVariable Long kmts) {

        Viaje v = viajeRepo.dameViajeXMono(idMono);
        if (v == null)
            return "No existe tal viaje";

        LocalDate fecha = LocalDate.now();
        LocalTime hora = LocalTime.now();
        v.setFecha_fin(fecha);
        v.setHora_fin(hora);
        v.setKms(kmts);

        Duration diferencia = Duration.between(v.getHora_inicio(), hora);

        // // Convierte la diferencia en horas, minutos y segundos
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

        v.setEstaEnViaje(false);

        TerminarViajeDTO finDelViaje = new TerminarViajeDTO(kmts, v.getTiempoConPausa(),tiempo);

        // Calcula costo viaje
        LocalTime tiempoConPausas = v.getTiempoConPausa();
        LocalTime horaInfraccion = v.getHoraDeInfraccion();
        Double costoViaje = tarifaService.calcularCostoViaje(tiempoConPausas, horaInfraccion);
        v.setFacturado(costoViaje);

        // Facturar en la cuenta
        cuentaService.descontarCostoViaje(v.getId_cuenta(), costoViaje);

        String x = monoService.apagar(idMono, finDelViaje);
        if (x.equals("se estaciono correctamente")) {
            this.viajeRepo.save(v);
            return "viaje finalizado con exito";
        }
        return "";
    }

    @GetMapping("/pausar/{idMono}")
    public void pausarMono(@PathVariable Long idMono) {
        Viaje viaje = viajeRepo.dameViajeXMono(idMono);
        Long id_viaje = viaje.getId_viaje();

        pausaService.empezarPausa(id_viaje);
        monoService.setStandBy(idMono);

        LocalTime horaActual = LocalTime.now();
        LocalTime horaInicioParcial = viaje.getHoraInicioParcial();

        // // Calcula la diferencia en segundos
        Duration diferencia = Duration.between(horaInicioParcial, horaActual);
        System.out.println(diferencia);
        // // Convierte la diferencia en horas, minutos y segundos
        int horas = (int) diferencia.toHours();
        int minutos = (int) diferencia.toMinutes() % 60;
        int segundos = (int) diferencia.getSeconds() % 60;

        LocalTime tiempo = LocalTime.of(horas, minutos, segundos);
        viaje.setTiempoConPausa(viaje.getTiempoConPausa().plusHours(tiempo.getHour()).plusMinutes(tiempo.getMinute())
                .plusSeconds(tiempo.getSecond()));
        viaje.setHoraInicioParcial(horaActual);
        viajeRepo.save(viaje);
    }

    @GetMapping("/terminarPausa/{idMono}")
    public String terminarPausaMono(@PathVariable Long idMono) {
        Viaje viaje = viajeRepo.dameViajeXMono(idMono);
        Long id_viaje = viaje.getId_viaje();
        monoService.setStandBy(idMono);
        LocalTime tiempoDeInfraccion = pausaService.terminarPausa(id_viaje);
        if (viaje.getHoraDeInfraccion() == null) {
            viaje.setHoraDeInfraccion(tiempoDeInfraccion);
            LocalTime ahora=LocalTime.now();
            viaje.setHoraDeReincorporacion(ahora);
            viajeRepo.save(viaje);
        }

        return "";
    }
}
