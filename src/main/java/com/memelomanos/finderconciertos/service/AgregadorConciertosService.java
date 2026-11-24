package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.integracion.ConciertoExterno;
import com.memelomanos.finderconciertos.integracion.CriteriosBusqueda;
import com.memelomanos.finderconciertos.integracion.ProveedorConciertos;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.OpcionCompra;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Consulta en paralelo todos los {@link ProveedorConciertos} habilitados,
 * aísla fallas y timeouts por proveedor, y deduplica los conciertos por
 * artista normalizado + fecha + ciudad agrupando sus {@link OpcionCompra}.
 */
@Service
public class AgregadorConciertosService {

    private static final Logger LOG = LoggerFactory.getLogger(AgregadorConciertosService.class);
    private static final Set<String> FUENTES_REVENTA = Set.of("VIAGOGO", "STUBHUB");

    private final List<ProveedorConciertos> proveedores;
    private final Duration timeoutProveedor;
    private final Executor ejecutor;
    private final ExecutorService ejecutorPropio;

    @Autowired
    public AgregadorConciertosService(
            List<ProveedorConciertos> proveedores,
            @Value("${agregador.timeout:10s}") Duration timeoutProveedor
    ) {
        this(proveedores, timeoutProveedor, Executors.newVirtualThreadPerTaskExecutor(), true);
    }

    AgregadorConciertosService(
            List<ProveedorConciertos> proveedores,
            Duration timeoutProveedor,
            Executor ejecutor
    ) {
        this(proveedores, timeoutProveedor, ejecutor, false);
    }

    private AgregadorConciertosService(
            List<ProveedorConciertos> proveedores,
            Duration timeoutProveedor,
            Executor ejecutor,
            boolean esEjecutorPropio
    ) {
        if (timeoutProveedor == null || timeoutProveedor.isNegative() || timeoutProveedor.isZero()) {
            throw new IllegalArgumentException("El timeout por proveedor debe ser mayor a cero");
        }
        this.proveedores = proveedores == null
                ? List.of()
                : proveedores.stream().filter(Objects::nonNull).toList();
        this.timeoutProveedor = timeoutProveedor;
        this.ejecutor = Objects.requireNonNull(ejecutor, "El ejecutor no puede ser nulo");
        this.ejecutorPropio = esEjecutorPropio && ejecutor instanceof ExecutorService servicio
                ? servicio
                : null;
    }

    @PreDestroy
    void cerrarEjecutor() {
        if (ejecutorPropio != null) {
            ejecutorPropio.shutdownNow();
        }
    }

    /**
     * Resultado de la descarga agregada de catálogos para sincronización:
     * conciertos deduplicados y, por cada fuente que entregó un catálogo completo
     * y no vacío, el conjunto de IDs externos recibidos.
     */
    public record CatalogoAgregado(
            List<Concierto> conciertos,
            Map<String, Set<String>> idsPorFuenteCompleta
    ) {
    }

    /**
     * Consulta todos los proveedores habilitados en paralelo y devuelve la
     * lista unificada y deduplicada de conciertos con sus opciones de compra.
     *
     * @param criterios filtros opcionales de búsqueda; {@code null} equivale a sin filtros
     * @return lista deduplicada de conciertos con sus opciones de compra
     */
    public List<Concierto> buscar(CriteriosBusqueda criterios) {
        CriteriosBusqueda filtros = criterios == null ? CriteriosBusqueda.vacio() : criterios;

        List<ProveedorConciertos> activos = proveedoresActivos();
        if (activos.isEmpty()) {
            return List.of();
        }

        List<CompletableFuture<ResultadoProveedor>> tareas = activos.stream()
                .map(proveedor -> consultarConTimeout(proveedor, filtros))
                .toList();

        List<ResultadoProveedor> resultados = tareas.stream()
                .map(CompletableFuture::join)
                .toList();

        return deduplicar(resultados);
    }

    /**
     * Descarga en paralelo los catálogos de todos los proveedores habilitados,
     * preservando eventos cancelados y las fuentes cuya descarga fue completa.
     */
    public CatalogoAgregado descargarCatalogos() {
        List<ProveedorConciertos> activos = proveedoresActivos();
        if (activos.isEmpty()) {
            return new CatalogoAgregado(List.of(), Map.of());
        }

        List<CompletableFuture<ResultadoProveedor>> tareas = activos.stream()
                .map(this::descargarConTimeout)
                .toList();

        List<ResultadoProveedor> resultados = tareas.stream()
                .map(CompletableFuture::join)
                .toList();

        Map<String, Set<String>> idsPorFuenteCompleta = new LinkedHashMap<>();
        for (ResultadoProveedor resultado : resultados) {
            if (!resultado.completo() || resultado.eventos().isEmpty()) {
                continue;
            }
            Set<String> ids = resultado.eventos().stream()
                    .filter(Objects::nonNull)
                    .map(ConciertoExterno::idExterno)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toSet());
            if (!ids.isEmpty()) {
                idsPorFuenteCompleta.put(nombreSeguro(resultado.proveedor()), ids);
            }
        }

        return new CatalogoAgregado(deduplicar(resultados), idsPorFuenteCompleta);
    }

    private List<ProveedorConciertos> proveedoresActivos() {
        return proveedores.stream()
                .filter(ProveedorConciertos::estaHabilitado)
                .toList();
    }

    /**
     * Normaliza un texto para comparación: recorta espacios, elimina acentos,
     * pasa a minúsculas y colapsa espacios múltiples.
     */
    public static String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinAcentos
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    /**
     * Determina si dos conciertos representan el mismo evento bajo la regla
     * artista normalizado + fecha + ciudad normalizada.
     */
    public static boolean mismoConcierto(Concierto primero, Concierto segundo) {
        if (primero == null || segundo == null) {
            return false;
        }
        String clavePrimero = claveCanonizada(
                primero.getArtista(), primero.getFecha(), primero.getCiudad()
        );
        String claveSegundo = claveCanonizada(
                segundo.getArtista(), segundo.getFecha(), segundo.getCiudad()
        );
        return clavePrimero != null && clavePrimero.equals(claveSegundo);
    }

    private CompletableFuture<ResultadoProveedor> consultarConTimeout(
            ProveedorConciertos proveedor,
            CriteriosBusqueda filtros
    ) {
        return CompletableFuture
                .supplyAsync(() -> {
                    List<ConciertoExterno> encontrados = proveedor.buscar(filtros);
                    return new ResultadoProveedor(
                            proveedor,
                            encontrados == null ? List.of() : encontrados,
                            Set.of(),
                            false
                    );
                }, ejecutor)
                .orTimeout(timeoutProveedor.toMillis(), TimeUnit.MILLISECONDS)
                .handle((resultado, error) -> {
                    if (error != null) {
                        registrarFallo(proveedor, error);
                        return new ResultadoProveedor(proveedor, List.of(), Set.of(), false);
                    }
                    return resultado;
                });
    }

    private CompletableFuture<ResultadoProveedor> descargarConTimeout(ProveedorConciertos proveedor) {
        return CompletableFuture
                .supplyAsync(() -> {
                    ProveedorConciertos.CatalogoProveedor catalogo = proveedor.descargarCatalogoCompleto();
                    if (catalogo == null) {
                        return new ResultadoProveedor(proveedor, List.of(), Set.of(), false);
                    }
                    return new ResultadoProveedor(
                            proveedor,
                            catalogo.eventos() == null ? List.of() : catalogo.eventos(),
                            catalogo.cancelados() == null ? Set.of() : catalogo.cancelados(),
                            catalogo.completo()
                    );
                }, ejecutor)
                .orTimeout(timeoutProveedor.toMillis(), TimeUnit.MILLISECONDS)
                .handle((resultado, error) -> {
                    if (error != null) {
                        registrarFallo(proveedor, error);
                        return new ResultadoProveedor(proveedor, List.of(), Set.of(), false);
                    }
                    return resultado;
                });
    }

    private void registrarFallo(ProveedorConciertos proveedor, Throwable error) {
        Throwable causa = error instanceof CompletionException && error.getCause() != null
                ? error.getCause()
                : error;
        String nombre = nombreSeguro(proveedor);

        if (causa instanceof TimeoutException) {
            LOG.warn("El proveedor '{}' excedió el tiempo límite de {} ms y fue omitido",
                    nombre, timeoutProveedor.toMillis());
            return;
        }

        LOG.warn("El proveedor '{}' falló al buscar conciertos y fue omitido: {}",
                nombre, causa.getMessage());
    }

    private static String nombreSeguro(ProveedorConciertos proveedor) {
        try {
            String nombre = proveedor.nombre();
            return nombre == null || nombre.isBlank()
                    ? proveedor.getClass().getSimpleName()
                    : nombre;
        } catch (RuntimeException ex) {
            return proveedor.getClass().getSimpleName();
        }
    }

    private List<Concierto> deduplicar(List<ResultadoProveedor> resultados) {
        Map<String, Concierto> conciertosPorClave = new LinkedHashMap<>();

        for (ResultadoProveedor resultado : resultados) {
            ProveedorConciertos proveedor = resultado.proveedor();
            for (ConciertoExterno evento : resultado.eventos()) {
                if (evento == null) {
                    continue;
                }

                String fuente = resolverFuente(evento, proveedor);
                boolean esReventa = proveedor.esReventa() || esFuenteDeReventa(fuente);
                boolean cancelado = evento.idExterno() != null
                        && resultado.cancelados().contains(evento.idExterno());
                String clave = claveDeDeduplicacion(evento, fuente);

                Concierto existente = conciertosPorClave.get(clave);
                if (existente == null) {
                    Concierto nuevo = crearConcierto(evento, fuente);
                    nuevo.setDisponible(!cancelado);
                    agregarOOActualizarOpcion(nuevo, fuente, evento.urlCompra(), evento.precioMinimo(), esReventa);
                    conciertosPorClave.put(clave, nuevo);
                } else {
                    combinarDatos(existente, evento);
                    if (!cancelado) {
                        existente.setDisponible(true);
                    }
                    agregarOOActualizarOpcion(existente, fuente, evento.urlCompra(), evento.precioMinimo(), esReventa);
                }
            }
        }

        return new ArrayList<>(conciertosPorClave.values());
    }

    private static String resolverFuente(ConciertoExterno evento, ProveedorConciertos proveedor) {
        if (evento.fuente() != null && !evento.fuente().isBlank()) {
            return evento.fuente().strip();
        }
        return nombreSeguro(proveedor);
    }

    private static boolean esFuenteDeReventa(String fuente) {
        return fuente != null && FUENTES_REVENTA.contains(fuente.strip().toUpperCase(Locale.ROOT));
    }

    private static String claveCanonizada(String artista, LocalDate fecha, String ciudad) {
        String artistaNormalizado = normalizarTexto(artista);
        String ciudadNormalizada = normalizarTexto(ciudad);
        if (artistaNormalizado.isEmpty() || fecha == null || ciudadNormalizada.isEmpty()) {
            return null;
        }
        return artistaNormalizado + "|" + fecha + "|" + ciudadNormalizada;
    }

    private static String claveDeDeduplicacion(ConciertoExterno evento, String fuente) {
        String clave = claveCanonizada(evento.artista(), evento.fecha(), evento.ciudad());
        if (clave != null) {
            return clave;
        }
        // Si faltan datos para deduplicar de forma segura, conserva el evento sin fusionarlo con otros.
        String id = evento.idExterno() != null && !evento.idExterno().isBlank()
                ? evento.idExterno().strip()
                : String.valueOf(System.identityHashCode(evento));
        return "sin-clave|" + normalizarTexto(fuente) + "|" + id;
    }

    private static Concierto crearConcierto(ConciertoExterno evento, String fuente) {
        Concierto concierto = new Concierto(
                evento.artista(),
                evento.fecha(),
                evento.ciudad(),
                evento.lugar()
        );
        concierto.setFuente(fuente);
        concierto.setIdExterno(evento.idExterno());
        concierto.setUrlCompra(evento.urlCompra());
        concierto.setImagenUrl(evento.imagenUrl());
        concierto.setLatitud(evento.latitud());
        concierto.setLongitud(evento.longitud());
        concierto.setPrecioMinimo(evento.precioMinimo());
        return concierto;
    }

    private static void combinarDatos(Concierto destino, ConciertoExterno origen) {
        if (estaVacio(destino.getLugar()) && !estaVacio(origen.lugar())) {
            destino.setLugar(origen.lugar());
        }
        if (estaVacio(destino.getImagenUrl()) && !estaVacio(origen.imagenUrl())) {
            destino.setImagenUrl(origen.imagenUrl());
        }
        if (destino.getLatitud() == null && origen.latitud() != null) {
            destino.setLatitud(origen.latitud());
        }
        if (destino.getLongitud() == null && origen.longitud() != null) {
            destino.setLongitud(origen.longitud());
        }
        if (estaVacio(destino.getUrlCompra()) && !estaVacio(origen.urlCompra())) {
            destino.setUrlCompra(origen.urlCompra());
        }
        destino.setPrecioMinimo(menorPrecio(destino.getPrecioMinimo(), origen.precioMinimo()));
    }

    private static void agregarOOActualizarOpcion(
            Concierto concierto,
            String fuente,
            String url,
            BigDecimal precioMinimo,
            boolean esReventa
    ) {
        for (OpcionCompra existente : concierto.getOpcionesCompra()) {
            if (mismaOpcion(existente, fuente, url)) {
                existente.setPrecioMinimo(menorPrecio(existente.getPrecioMinimo(), precioMinimo));
                return;
            }
        }
        concierto.agregarOpcionCompra(new OpcionCompra(fuente, url, precioMinimo, esReventa));
    }

    private static boolean mismaOpcion(OpcionCompra opcion, String fuente, String url) {
        return Objects.equals(normalizarTexto(opcion.getFuente()), normalizarTexto(fuente))
                && Objects.equals(opcion.getUrl(), url);
    }

    private static BigDecimal menorPrecio(BigDecimal actual, BigDecimal candidato) {
        if (actual == null) {
            return candidato;
        }
        if (candidato == null) {
            return actual;
        }
        return candidato.compareTo(actual) < 0 ? candidato : actual;
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private record ResultadoProveedor(
            ProveedorConciertos proveedor,
            List<ConciertoExterno> eventos,
            Set<String> cancelados,
            boolean completo
    ) {
    }
}
