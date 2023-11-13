package poroto.po.viaje.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
// import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import poroto.po.viaje.dtos.ReporteMonopatinesPorViajeDTO;
import poroto.po.viaje.entity.TerminarViajeDTO;
import poroto.po.viaje.entity.Viaje;
import poroto.po.viaje.repsitory.ViajeRepo;
import poroto.po.viaje.service.CuentaService;
import poroto.po.viaje.service.MonopatinService;
import poroto.po.viaje.service.PausaService;
import poroto.po.viaje.service.TarifaService;
import poroto.po.viaje.service.TokenService;

@RestController
@Tag(name = "Servicio Viaje", description = "Todo lo relacionado a los datos y detalles del viaje")

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

    @Autowired
    private TokenService token;

    @GetMapping("/dameViajes")
    @Operation(summary = "Lista de todos los viajes", description = "Listado completo tanto de los viajes en curso como los ya finalizados y sus detalles")

    public List<Viaje> dameViajes(@RequestHeader("Authorization") String authorization) {
        if (token.autorizado(authorization) == null)
            return null;
        return viajeRepo.findAll();
    }

    // @GetMapping({ "/iniciar/{idMono}/{idCuenta}" })

    // public String inicio() {
    // return "anduvo";
    // }

    @SuppressWarnings("unchecked")
    @Operation(summary = "Inicio de viaje", description = "Da por comenzado un viaje, registrara sus pausas, kmts y duraciones ")
    @PostMapping("/iniciar")
    public String iniciarViaje(@RequestBody String v, @RequestHeader("Authorization") String authorization)
            throws JsonMappingException, JsonProcessingException {
        if (token.autorizado(authorization) == null)
            return null;

        ObjectMapper jsonMap = new ObjectMapper();
        Map<String, Integer> data = jsonMap.readValue(v, Map.class);
        Long idMono = (long) data.get("idMono");
        Long idCuenta = (long) data.get("idCuenta");

        Float saldo = cuentaService.dameSaldo(idCuenta);
        // Float saldo = (float) 4;

        if (saldo > (float) 0) {
            if (monoService.estaListoParaUsar(idMono)) {
                LocalDate fecha = LocalDate.now();
                LocalTime hora = LocalTime.now();
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
    @Operation(summary = "Fin da viaje", description = "Da por finalizado un viaje, registrando los detalles")

    public String terminarViaje(@PathVariable Long idMono, @PathVariable Long kmts,
            @RequestHeader("Authorization") String authorization) {
        if (token.autorizado(authorization) == null)
            return null;

        Viaje v = viajeRepo.dameViajeXMono(idMono);
        if (v == null)
            return "No existe tal viaje";

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

        v.setEstaEnViaje(false);

        TerminarViajeDTO finDelViaje = new TerminarViajeDTO(kmts, v.getTiempoConPausa(), tiempo);

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
    @Operation(summary = "Monopatin detenido", description = "Registrara y computará tiempos de pausa ")

    public void pausarMono(@PathVariable Long idMono, @RequestHeader("Authorization") String authorization) {
        if (token.autorizado(authorization) == null)
            return;

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
    @Operation(summary = "Fin de l Puasa", description = "Se tiene en cuenta este mensaje recibido por cuestios de computos de saldo")

    public String terminarPausaMono(@PathVariable Long idMono, @RequestHeader("Authorization") String authorization) {
        if (token.autorizado(authorization) == null)
            return null;

        Viaje viaje = viajeRepo.dameViajeXMono(idMono);
        Long id_viaje = viaje.getId_viaje();
        monoService.setStandBy(idMono);
        LocalTime tiempoDeInfraccion = pausaService.terminarPausa(id_viaje);
        if (viaje.getHoraDeInfraccion() == null) {
            viaje.setHoraDeInfraccion(tiempoDeInfraccion);
            LocalTime ahora = LocalTime.now();
            viaje.setHoraDeReincorporacion(ahora);
            viajeRepo.save(viaje);
        }

        return "";
    }

    @GetMapping("/obtenerReporteMonopatinesPorViaje/{cantViajes}/{anio}")
    @Operation(summary = "Reporte de viajes por año ", description = "Lista viajes de determinado año")

    public List<ReporteMonopatinesPorViajeDTO> generarReportePorViaje(@PathVariable int cantViajes,
            @PathVariable int anio, @RequestHeader("Authorization") String authorization) {
        if (token.autorizado(authorization) == null)
            return null;

        List<ReporteMonopatinesPorViajeDTO> reporte = new ArrayList<ReporteMonopatinesPorViajeDTO>();

        // Obtiene todos los viajes en un año específico
        List<Viaje> viajes = viajeRepo.findByYear(anio);

        // Crea un map para realizar el seguimiento de la cantidad de viajes por
        // monopatín
        Map<Long, Long> cantidadViajesPorMonopatin = new HashMap<>();

        // Itera a través de los viajes y cuenta la cantidad de viajes por monopatín
        for (Viaje viaje : viajes) {
            Long monopatinId = viaje.getMonopatin();
            cantidadViajesPorMonopatin.put(monopatinId, cantidadViajesPorMonopatin.getOrDefault(monopatinId, 0L) + 1);
        }

        // Filtra los monopatines con más de la cantidad deseada de viajes
        for (Map.Entry<Long, Long> entry : cantidadViajesPorMonopatin.entrySet()) {
            if (entry.getValue() > cantViajes) {
                ReporteMonopatinesPorViajeDTO dto = new ReporteMonopatinesPorViajeDTO();
                dto.setCantViajes(entry.getValue());
                dto.setAnio(anio);
                dto.setMonopatin(entry.getKey());
                reporte.add(dto);
            }
        }

        return reporte;
    }

    @GetMapping("/totalFacturadoEnRangoDeMeses/{mesInicio}/{mesFin}/{anio}")
    @Operation(summary = "Reporte de facturas", description = "Valores precisos de los saldos en viajes por mes y año")

    public Double obtenerTotalFacturadoEnRangoDeMeses(@PathVariable int mesInicio, @PathVariable int mesFin,
            @PathVariable int anio, @RequestHeader("Authorization") String authorization) {
        if (token.autorizado(authorization) == null)
            return null;

        Double totalFacturado = viajeRepo.getTotalFacturadoEnRangoDeMeses(mesInicio, mesFin, anio);

        if (totalFacturado != null) {
            return totalFacturado;
        }
        return 0.0;
    }
}
