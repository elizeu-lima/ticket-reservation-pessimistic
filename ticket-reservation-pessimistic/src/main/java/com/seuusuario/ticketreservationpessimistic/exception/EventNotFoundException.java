package com.seuusuario.ticketreservationpessimistic.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(Long id) {
        super("Evento não encontrado com o ID: " + id);
    }
}
