package com.memelomanos.finderconciertos.integracion.soundcloud;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Nombres y URLs candidatas de SoundCloud (Plan C)")
class NombresSoundCloudTest {

    @Test
    @DisplayName("Genera las URLs probables sin acentos, juntas y con guiones")
    void candidatas_NombreDeDosPalabras() {
        assertThat(NombresSoundCloud.candidatas("Bad Bunny")).containsExactly(
                "https://soundcloud.com/badbunny",
                "https://soundcloud.com/bad-bunny",
                "https://soundcloud.com/bad_bunny",
                "https://soundcloud.com/badbunnyofficial");
        assertThat(NombresSoundCloud.candidatas("  Café   Tacvba ")).first()
                .isEqualTo("https://soundcloud.com/cafetacvba");
        assertThat(NombresSoundCloud.candidatas("AC/DC")).contains("https://soundcloud.com/ac-dc");
    }

    @Test
    @DisplayName("No repite candidatas y nunca pasa del maximo")
    void candidatas_SinRepetidosYConTope() {
        assertThat(NombresSoundCloud.candidatas("Queen")).containsExactly(
                "https://soundcloud.com/queen",
                "https://soundcloud.com/queenofficial");
        assertThat(NombresSoundCloud.candidatas("Los Ángeles Azules de Iztapalapa"))
                .hasSize(NombresSoundCloud.MAXIMO_CANDIDATAS);
    }

    @Test
    @DisplayName("No genera candidatas para un nombre sin letras ni numeros")
    void candidatas_NombreVacio() {
        assertThat(NombresSoundCloud.candidatas("¡¡!!")).isEmpty();
        assertThat(NombresSoundCloud.candidatas(null)).isEmpty();
    }

    @Test
    @DisplayName("Compara nombres sin importar mayusculas, acentos ni signos")
    void coinciden_IgnoraFormato() {
        assertThat(NombresSoundCloud.coinciden("Café Tacvba", "cafe tacvba")).isTrue();
        assertThat(NombresSoundCloud.coinciden("Bad Bunny", "bad-bunny")).isTrue();
        assertThat(NombresSoundCloud.coinciden("Queen", "Queen Tributo")).isFalse();
        assertThat(NombresSoundCloud.coinciden("", "")).isFalse();
        assertThat(NombresSoundCloud.coinciden("Queen", null)).isFalse();
    }
}
