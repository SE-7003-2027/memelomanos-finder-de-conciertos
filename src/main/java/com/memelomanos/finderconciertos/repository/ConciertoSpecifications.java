
package com.memelomanos.finderconciertos.repository;

import com.memelomanos.finderconciertos.model.Concierto;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public class ConciertoSpecifications {

    private ConciertoSpecifications() {
    }

    public static Specification<Concierto> porArtista(String artista) {
        return (concierto, busqueda, constructor) ->
                constructor.like(
                        constructor.lower(concierto.get("artista")),
                        "%" + artista.strip().toLowerCase() + "%"
                );
    }

    public static Specification<Concierto> porCiudad(String ciudad) {
        return (concierto, busqueda, constructor) ->
                constructor.like(
                        constructor.lower(concierto.get("ciudad")),
                        "%" + ciudad.strip().toLowerCase() + "%"
                );
    }

    public static Specification<Concierto> desdeFecha(LocalDate fechaDesde) {
        return (concierto, busqueda, constructor) ->
                constructor.greaterThanOrEqualTo(concierto.get("fecha"), fechaDesde);
    }

    public static Specification<Concierto> hastaFecha(LocalDate fechaHasta) {
        return (concierto, busqueda, constructor) ->
                constructor.lessThanOrEqualTo(concierto.get("fecha"), fechaHasta);
    }
}
