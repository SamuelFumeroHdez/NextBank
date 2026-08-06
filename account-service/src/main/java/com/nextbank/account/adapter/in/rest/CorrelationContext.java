package com.nextbank.account.adapter.in.rest;

import java.util.UUID;

/**
 * Contexto de correlacion asociado al hilo que atiende la peticion.
 * Permite que la infraestructura (outbox) acceda al correlationId sin que
 * el dominio ni la capa de aplicacion tengan que transportarlo. Ver ADR-0006.
 */
public class CorrelationContext {

    private final static ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private CorrelationContext() {

    }

    public static void set(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static String getOrGenerate(){
        String current = CORRELATION_ID.get();
        if (current == null) {
            current = UUID.randomUUID().toString();
            CORRELATION_ID.set(current);
        }
        return current;
    }

    public static void clear(){
        CORRELATION_ID.remove();
    }
}
