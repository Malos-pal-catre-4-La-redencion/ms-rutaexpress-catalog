package com.rutaexpress.catalog.exception;

/** Error NO recuperable: el comando está mal formado o viola una regla. Reintentar no lo arregla. */
public class ComandoInvalidoException extends RuntimeException {

    public ComandoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
