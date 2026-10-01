package com.memelomanos.finderconciertos.model;

import jakarta.validation.constraints.NotNull;

// Representa el body JSON que se recibe al marcar un concierto como
// favorito. No es una entidad de base de datos, solo el "molde" de
// la peticion.
public class FavoritoRequest {

    @NotNull
    private Long usuarioId;

    @NotNull
    private Long conciertoId;

    public FavoritoRequest() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getConciertoId() {
        return conciertoId;
    }

    public void setConciertoId(Long conciertoId) {
        this.conciertoId = conciertoId;
    }
}
