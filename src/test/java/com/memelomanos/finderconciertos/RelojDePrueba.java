package com.memelomanos.finderconciertos;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Reloj que las pruebas pueden adelantar para simular que pasa el tiempo. */
public class RelojDePrueba extends Clock {

    private Instant ahora;

    public RelojDePrueba(Instant inicio) {
        this.ahora = inicio;
    }

    public void avanzar(Duration tiempo) {
        ahora = ahora.plus(tiempo);
    }

    @Override
    public Instant instant() {
        return ahora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return this;
    }
}
