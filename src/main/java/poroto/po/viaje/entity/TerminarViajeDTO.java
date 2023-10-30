package poroto.po.viaje.entity;

import java.time.LocalTime;

public class TerminarViajeDTO {
    private Long kmts;
    private LocalTime tiempoDeUsoConPausa; 
    private LocalTime tiempoDeUsoSinPausa;
    public TerminarViajeDTO() {
    }
    public TerminarViajeDTO(Long kmts, LocalTime tiempoDeUsoConPausa, LocalTime tiempoDeUsoSinPausa) {
        this.kmts = kmts;
        this.tiempoDeUsoConPausa = tiempoDeUsoConPausa;
        this.tiempoDeUsoSinPausa = tiempoDeUsoSinPausa;
    }
    public Long getKmts() {
        return kmts;
    }
    public void setKmts(Long kmts) {
        this.kmts = kmts;
    }
    public LocalTime getTiempoDeUsoConPausa() {
        return tiempoDeUsoConPausa;
    }
    public void setTiempoDeUsoConPausa(LocalTime tiempoDeUsoConPausa) {
        this.tiempoDeUsoConPausa = tiempoDeUsoConPausa;
    }
    public LocalTime getTiempoDeUsoSinPausa() {
        return tiempoDeUsoSinPausa;
    }
    public void setTiempoDeUsoSinPausa(LocalTime tiempoDeUsoSinPausa) {
        this.tiempoDeUsoSinPausa = tiempoDeUsoSinPausa;
    }
     
    
}
