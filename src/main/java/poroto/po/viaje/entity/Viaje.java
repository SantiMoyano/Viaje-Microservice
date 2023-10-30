package poroto.po.viaje.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Viaje {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id_viaje;

    @Column
    private Long id_cuenta;

    @Column
    private LocalDate fecha_inicio;

    @Column
    private LocalDate fecha_fin;

    @Column
    private LocalTime hora_inicio;

    @Column
    private LocalTime hora_fin;

    @Column
    private Long kms;

    @Column
    private Long monopatin;

    @Column
    private Boolean estaEnViaje;

    @Column
    private LocalTime tiempoSinPausa;

    @Column
    private LocalTime tiempoConPausa;

    @Column 
    private LocalTime horaInicioParcial;

    @Column
    private double facturado;

    @Column
    private LocalTime horaDeInfraccion;

    @Column
    private LocalTime horaDeReincorporacion;


    public Viaje() {
    }

    public Viaje(Long id_cuenta, LocalDate fecha_inicio, LocalDate fecha_fin, LocalTime hora_inicio, LocalTime hora_fin,
            Long kms, Long monopatin,Boolean estaEnViaje) {
        this.id_cuenta = id_cuenta;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.hora_inicio = hora_inicio;
        this.hora_fin = hora_fin;
        this.kms = kms;
        this.monopatin = monopatin;
        this.estaEnViaje=estaEnViaje;
        LocalTime horaCero=LocalTime.of(0, 0, 0, 0);
        this.tiempoConPausa=horaCero;
        this.tiempoSinPausa=horaCero;
        this.horaInicioParcial=hora_inicio;
        this.horaDeReincorporacion=hora_inicio;
        this.facturado=(double) 0;
    }
    
    // public Viaje(Long id_cuenta,String fecha_inicio, String hora_inicio, Long monopatin) {
    //     this.id_cuenta = id_cuenta;
    //     this.fecha_inicio = fecha_inicio;
    //     this.fecha_fin = null;
    //     this.hora_inicio = hora_inicio;
    //     this.hora_fin = null;
    //     this.kms = 0;
    //     this.monopatin = monopatin;
    // }

    public Long getId_viaje() {
        return id_viaje;
    }

    public void setId_viaje(Long id_viaje) {
        this.id_viaje = id_viaje;
    }

    public Long getId_cuenta() {
        return id_cuenta;
    }

    public void setId_cuenta(Long id_cuenta) {
        this.id_cuenta = id_cuenta;
    }

    public LocalDate getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(LocalDate fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public LocalDate getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(LocalDate fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public LocalTime getHora_inicio() {
        return hora_inicio;
    }

    public void setHora_inicio(LocalTime hora_inicio) {
        this.hora_inicio = hora_inicio;
    }

    public LocalTime getHora_fin() {
        return hora_fin;
    }

    public void setHora_fin(LocalTime hora_fin) {
        this.hora_fin = hora_fin;
    }

    public Long getKms() {
        return kms;
    }

    public void setKms(Long kms) {
        this.kms = kms;
    }

    public Long getMonopatin() {
        return monopatin;
    }

    public void setMonopatin(Long monopatin) {
        this.monopatin = monopatin;
    }

   

    public Boolean getEstaEnViaje() {
        return estaEnViaje;
    }

    public void setEstaEnViaje(Boolean estaEnViaje) {
        this.estaEnViaje = estaEnViaje;
    }

    public LocalTime getTiempoSinPausa() {
        return tiempoSinPausa;
    }

    public void setTiempoSinPausa(LocalTime tiempoSinPausa) {
        this.tiempoSinPausa = tiempoSinPausa;
    }

    public LocalTime getTiempoConPausa() {
        return tiempoConPausa;
    }

    public void setTiempoConPausa(LocalTime tiempoConPausa) {
        this.tiempoConPausa = tiempoConPausa;
    }

    public LocalTime getHoraInicioParcial() {
        return horaInicioParcial;
    }

    public void setHoraInicioParcial(LocalTime horaInicioParcial) {
        this.horaInicioParcial = horaInicioParcial;
    }

    public double getFacturado() {
        return facturado;
    }

    public void setFacturado(double facturado) {
        this.facturado = facturado;
    }

    public LocalTime getHoraDeInfraccion() {
        return horaDeInfraccion;
    }

    public void setHoraDeInfraccion(LocalTime horaDeInfraccion) {
        this.horaDeInfraccion = horaDeInfraccion;
    }

    public LocalTime getHoraDeReincorporacion() {
        return horaDeReincorporacion;
    }

    public void setHoraDeReincorporacion(LocalTime horaDeReincorporacion) {
        this.horaDeReincorporacion = horaDeReincorporacion;
    }

    

}
