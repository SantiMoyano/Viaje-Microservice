package poroto.po.viaje.repsitory;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poroto.po.viaje.entity.Viaje;

public interface ViajeRepo extends JpaRepository<Viaje,Long>{
    @Query("SELECT v FROM Viaje v WHERE v.monopatin=:idMono AND v.estaEnViaje=true")
    Viaje dameViajeXMono(@Param("idMono") Long idMono);

    @Query("SELECT v.id_viaje FROM Viaje v WHERE v.monopatin=:idMono AND v.estaEnViaje=true")
    Long dameMonopatinAndId_cuenta(Long idMono);
    
}
