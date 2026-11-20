package com.memelomanos.finderconciertos.integracion.viagogo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Proveedor de Viagogo")
public class ViagogoProveedorTest {

    private final ViagogoProveedor proveedor = new ViagogoProveedor();

    @Test
    @DisplayName("Debe generar la URL de búsqueda para un artista")
    void generarUrlBusqueda_Exito() {
        String resultado = proveedor.generarUrlBusqueda("Coldplay");

        assertEquals(
                "https://www.viagogo.com.mx/secure/Search?q=Coldplay",
                resultado
        );
    }

    @Test
    @DisplayName("Debe codificar espacios en el nombre del artista")
    void generarUrlBusqueda_ConEspacios() {
        String resultado = proveedor.generarUrlBusqueda("Elton John");

        assertEquals(
                "https://www.viagogo.com.mx/secure/Search?q=Elton+John",
                resultado
        );
    }

    @Test
    @DisplayName("Debe eliminar espacios al inicio y al final")
    void generarUrlBusqueda_ConEspaciosAlrededor() {
        String resultado = proveedor.generarUrlBusqueda("  Elton John  ");

        assertEquals(
                "https://www.viagogo.com.mx/secure/Search?q=Elton+John",
                resultado
        );
    }

    @Test
    @DisplayName("Debe rechazar un nombre vacío")
    void generarUrlBusqueda_NombreVacio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> proveedor.generarUrlBusqueda("   ")
        );
    }

    @Test
    @DisplayName("Debe rechazar un nombre nulo")
    void generarUrlBusqueda_NombreNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> proveedor.generarUrlBusqueda(null)
        );
    }
}
