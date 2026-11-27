package com.memelomanos.finderconciertos.integracion.soundcloud;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reglas para comparar nombres de artistas y para deducir las URLs de
 * perfil mas probables a partir del nombre (Plan C de ADR-005).
 */
public final class NombresSoundCloud {

    static final String PERFIL_URL = "https://soundcloud.com/";

    // Cada candidata es una consulta a SoundCloud; con mas de cuatro el
    // detalle del concierto tardaria demasiado cuando nadie coincide.
    static final int MAXIMO_CANDIDATAS = 4;

    private static final String SUFIJO_OFICIAL = "official";
    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}+");
    private static final Pattern NO_ALFANUMERICOS = Pattern.compile("[^a-z0-9]+");

    private NombresSoundCloud() {
    }

    /**
     * Minusculas, sin acentos y sin nada que no sea letra o numero:
     * "Café Tacvba" y "cafe-tacvba" quedan igual ("cafetacvba").
     */
    public static String normalizar(String nombre) {
        return String.join("", palabras(nombre));
    }

    /** true si los dos nombres son iguales una vez normalizados. */
    public static boolean coinciden(String nombre, String otro) {
        String normalizado = normalizar(nombre);
        return !normalizado.isEmpty() && normalizado.equals(normalizar(otro));
    }

    /**
     * URLs de perfil mas probables, de la mas comun a la menos comun. Por
     * ejemplo, para "Bad Bunny": soundcloud.com/badbunny, /bad-bunny,
     * /bad_bunny y /badbunnyofficial.
     */
    public static List<String> candidatas(String nombre) {
        List<String> palabras = palabras(nombre);
        if (palabras.isEmpty()) {
            return List.of();
        }
        String juntas = String.join("", palabras);
        Set<String> perfiles = new LinkedHashSet<>();
        perfiles.add(juntas);
        perfiles.add(String.join("-", palabras));
        perfiles.add(String.join("_", palabras));
        perfiles.add(juntas + SUFIJO_OFICIAL);
        return perfiles.stream()
                .limit(MAXIMO_CANDIDATAS)
                .map(perfil -> PERFIL_URL + perfil)
                .toList();
    }

    private static List<String> palabras(String nombre) {
        if (nombre == null) {
            return List.of();
        }
        String sinAcentos = MARCAS_DIACRITICAS
                .matcher(Normalizer.normalize(nombre, Normalizer.Form.NFD))
                .replaceAll("");
        return Arrays.stream(NO_ALFANUMERICOS.split(sinAcentos.toLowerCase(Locale.ROOT)))
                .filter(palabra -> !palabra.isEmpty())
                .toList();
    }
}
