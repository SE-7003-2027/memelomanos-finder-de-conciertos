package com.memelomanos.finderconciertos.exception;

// Se lanza cuando no hay perfil de SoundCloud para un artista, o cuando
// SoundCloud no respondio. ManejadorGlobalDeExcepciones lo convierte en
// un 404 Not Found para que la interfaz lo trate como "sin resultados".
public class ArtistaSinSoundCloudException extends RuntimeException {

    public ArtistaSinSoundCloudException(String mensaje) {
        super(mensaje);
    }
}
