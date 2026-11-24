package com.memelomanos.finderconciertos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Opción de compra de boletos para un {@link Concierto} ofrecida por un
 * proveedor externo (boletera oficial o sitio de reventa).
 */
@Entity
@Table(name = "opcion_compra")
public class OpcionCompra {

    private static final int LONGITUD_MAXIMA_URL = 2048;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fuente;

    @Column(length = LONGITUD_MAXIMA_URL)
    private String url;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioMinimo;

    private boolean esReventa;

    // Se omite en JSON para evitar recursión infinita al serializar Concierto -> opcionesCompra.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concierto_id", nullable = false)
    @JsonIgnore
    private Concierto concierto;

    public OpcionCompra() {
    }

    public OpcionCompra(String fuente, String url, BigDecimal precioMinimo, boolean esReventa) {
        this.fuente = fuente;
        this.url = url;
        this.precioMinimo = precioMinimo;
        this.esReventa = esReventa;
    }

    public Long getId() {
        return id;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BigDecimal getPrecioMinimo() {
        return precioMinimo;
    }

    public void setPrecioMinimo(BigDecimal precioMinimo) {
        this.precioMinimo = precioMinimo;
    }

    public boolean isEsReventa() {
        return esReventa;
    }

    public void setEsReventa(boolean esReventa) {
        this.esReventa = esReventa;
    }

    public Concierto getConcierto() {
        return concierto;
    }

    public void setConcierto(Concierto concierto) {
        this.concierto = concierto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpcionCompra otra)) {
            return false;
        }
        return id != null && id.equals(otra.getId());
    }

    @Override
    public int hashCode() {
        return OpcionCompra.class.hashCode();
    }
}
