package poroto.po.viaje.repsitory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import poroto.po.viaje.entity.Viaje;

public interface ViajeRepo extends JpaRepository<Viaje, Long> {
    @Query("SELECT v FROM Viaje v WHERE v.monopatin=:idMono AND v.estaEnViaje=true")
    Viaje dameViajeXMono(@Param("idMono") Long idMono);

    @Query("SELECT v.id_viaje FROM Viaje v WHERE v.monopatin=:idMono AND v.estaEnViaje=true")
    Long dameMonopatinAndId_cuenta(Long idMono);

    @Query("SELECT v FROM Viaje v WHERE YEAR(v.fecha_inicio) = :year")
    List<Viaje> findByYear(@Param("year") int year);

    @Query("SELECT SUM(v.facturado) FROM Viaje v WHERE MONTH(v.fecha_inicio) >= :mesInicio AND MONTH(v.fecha_inicio) <= :mesFin AND YEAR(v.fecha_inicio) = :anio")
    Double getTotalFacturadoEnRangoDeMeses(@Param("mesInicio") int mesInicio, @Param("mesFin") int mesFin,
            @Param("anio") int anio);
}
