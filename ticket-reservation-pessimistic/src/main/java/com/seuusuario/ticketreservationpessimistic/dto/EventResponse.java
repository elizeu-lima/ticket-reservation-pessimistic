package com.seuusuario.ticketreservationpessimistic.dto;

import com.seuusuario.ticketreservationpessimistic.model.Event;
import java.time.OffsetDateTime;


public class EventResponse {

    private Long id;
    private String name;
    private OffsetDateTime date;

    public EventResponse() {
    }

    public EventResponse(Long id, String name, OffsetDateTime date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }

    // Fábrica estática para facilitar a conversão a partir da Entidade
    public static EventResponse fromEntity(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDate()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getDate() {
        return date;
    }

}
